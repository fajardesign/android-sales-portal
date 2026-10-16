package com.example.and_sales_portal.ui.partner

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.device.LocationResult
import com.example.and_sales_portal.data.device.LocationService
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.PartnerRepository
import com.example.and_sales_portal.data.partner.Store
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBox
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBoxType
import com.example.and_sales_portal.designsystem.components.inputs.AmarTextField
import com.example.and_sales_portal.designsystem.components.inputs.AmarTextFieldType
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.attendance.CaptureActions
import com.example.and_sales_portal.ui.common.LocationCaptureContent
import com.example.and_sales_portal.ui.common.LocationCaptureState
import com.example.and_sales_portal.ui.common.PersistentAction
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.example.and_sales_portal.ui.common.SectionTitle
import com.example.and_sales_portal.ui.partnerregistration.AddressFields
import com.example.and_sales_portal.ui.partnerregistration.FieldError
import com.example.and_sales_portal.ui.partnerregistration.PartnerInfoActions
import com.example.and_sales_portal.ui.partnerregistration.PartnerInfoForm
import com.example.and_sales_portal.ui.partnerregistration.PartnerRegistrationValidator
import com.example.and_sales_portal.ui.partnerregistration.RegChoice
import com.example.and_sales_portal.ui.partnerregistration.RegChoiceField
import com.example.and_sales_portal.ui.partnerregistration.RegField
import com.example.and_sales_portal.ui.partnerregistration.RegTextField
import com.example.and_sales_portal.ui.partnerregistration.RegionLists
import com.example.and_sales_portal.ui.partnerregistration.data.PartnerRegistrationRepository
import com.example.and_sales_portal.ui.partnerregistration.data.Region
import com.example.and_sales_portal.ui.partnerregistration.errorHint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Another toko of the same partner within 50 m (PRD C4 warning). */
data class NearbyStore(val name: String, val meters: Int)

data class AddStoreState(
    /** `name` is Nama Toko; the rest is the toko address. */
    val form: PartnerInfoForm = PartnerInfoForm(),
    val regions: RegionLists = RegionLists(),
    val revenue: String = "",
    val choices: Map<RegChoice, String> = emptyMap(),
    val showErrors: Boolean = false,
)

/**
 * Spec: PRT-02 Tambah Toko (PRD v3 C4): Nama Toko (unique per partner), address and region, RT/RW, Omzet, Tipe,
 * Skala, Lokasi, Produk Dijual, Jenis Produk, Channel Offline (only for Offline), and a mandatory location. A toko within
 * 50 m of another toko of the partner shows a warning (not a block). Saved immediately as active.
 */
class AddStoreViewModel(private val partner: Partner, private val partners: PartnerRepository, private val regions: PartnerRegistrationRepository, private val userId: Long) : ViewModel() {

    private val _state = MutableStateFlow(AddStoreState())
    val state: StateFlow<AddStoreState> = _state.asStateFlow()
    private var regionJob: Job? = null

    init {
        viewModelScope.launch {
            val provinces = regions.provinces()
            _state.update { it.copy(regions = it.regions.copy(provinces = provinces)) }
        }
    }

    val addressActions = PartnerInfoActions(
        onName = { v -> form { it.copy(name = v.take(100)) } },
        onAddress = { v -> form { it.copy(address = v.take(255)) } },
        onProvince = { r -> form { it.copy(province = r, regency = null, district = null, village = null) }; load { l -> l.copy(regencies = regions.regencies(r.id), districts = emptyList(), villages = emptyList()) } },
        onRegency = { r -> form { it.copy(regency = r, district = null, village = null) }; load { l -> l.copy(districts = regions.districts(r.id), villages = emptyList()) } },
        onDistrict = { r -> form { it.copy(district = r, village = null) }; load { l -> l.copy(villages = regions.villages(r.id)) } },
        onVillage = { r: Region -> form { it.copy(village = r) } },
        onRt = { v -> form { it.copy(rt = v.filter(Char::isDigit).take(3)) } },
        onRw = { v -> form { it.copy(rw = v.filter(Char::isDigit).take(3)) } },
    )

    fun setRevenue(v: String) = _state.update { it.copy(revenue = v) }

    fun choose(group: RegChoice, key: String) = _state.update { s ->
        var choices = s.choices + (group to key)
        if (group == RegChoice.StoreType && key != "OFFLINE") choices = choices - RegChoice.OfflineChannel
        s.copy(choices = choices)
    }

    fun errors(s: AddStoreState = _state.value): Map<RegField, FieldError> {
        val name = s.form.name.trim()
        val nameError = when {
            name.isBlank() -> FieldError(R.string.reg_required)
            partner.stores.any { it.name.equals(name, ignoreCase = true) } -> FieldError(R.string.ts_err_name_taken)
            else -> null
        }
        return PartnerRegistrationValidator.partnerInfo(s.form) - RegField.PartnerName +
            listOfNotNull(nameError?.let { RegField.StoreName to it }, PartnerRegistrationValidator.revenue(s.revenue)?.let { RegField.Revenue to it })
    }

    fun missingChoices(s: AddStoreState = _state.value) = PartnerRegistrationValidator.missingChoices(s.choices, RegChoice.storeLevel(s.choices))

    /** "Simpan Toko": `true` when the form is valid and a location was taken. */
    fun validate(location: LocationResult.Found?): Boolean {
        val ok = errors().isEmpty() && missingChoices().isEmpty() && location != null
        if (!ok) _state.update { it.copy(showErrors = true) }
        return ok
    }

    fun nearby(location: LocationResult.Found?): NearbyStore? {
        location ?: return null
        return partner.stores.map { NearbyStore(it.name, LocationService.distance(location.lat, location.lng, it.lat, it.lng).roundToInt()) }
            .filter { it.meters <= NEAR_METERS }.minByOrNull { it.meters }
    }

    fun save(location: LocationResult.Found): Store {
        val s = _state.value
        val f = s.form
        val area = listOfNotNull(f.village?.name, f.district?.name, f.regency?.name, f.province?.name).joinToString()
        return partners.addStore(
            partner.id,
            Store(
                id = "", name = f.name.trim(), address = "${f.address.trim()}, $area",
                province = f.province?.name.orEmpty(), regency = f.regency?.name.orEmpty(), district = f.district?.name.orEmpty(), village = f.village?.name.orEmpty(),
                rt = f.rt, rw = f.rw, lat = location.lat, lng = location.lng,
                omzet = s.revenue.toLongOrNull() ?: 0,
                storeType = s.choices[RegChoice.StoreType].orEmpty(),
                channelOffline = s.choices[RegChoice.OfflineChannel],
                scale = s.choices[RegChoice.StoreScale].orEmpty(),
                productSold = s.choices[RegChoice.ProductSold].orEmpty(),
                location = s.choices[RegChoice.StoreLocation].orEmpty(),
                productType = s.choices[RegChoice.ProductType].orEmpty(),
                addedBy = userId,
                addedAt = partners.now(),
            ),
        )
    }

    private fun form(f: (PartnerInfoForm) -> PartnerInfoForm) = _state.update { it.copy(form = f(it.form)) }

    private fun load(f: suspend (RegionLists) -> RegionLists) {
        regionJob?.cancel()
        regionJob = viewModelScope.launch {
            val l = f(_state.value.regions)
            _state.update { it.copy(regions = l) }
        }
    }

    private companion object {
        /** PRD C4: warning within 50 m of another toko of the same partner. */
        const val NEAR_METERS = 50
    }
}

@Composable
fun AddStoreScreen(
    partnerName: String,
    state: AddStoreState,
    errors: Map<RegField, FieldError>,
    missingChoices: Set<RegChoice>,
    actions: PartnerInfoActions,
    onRevenue: (String) -> Unit,
    onChoose: (RegChoice, String) -> Unit,
    location: LocationCaptureState,
    locationActions: CaptureActions,
    nearby: NearbyStore?,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    val shown = if (state.showErrors) errors else emptyMap()
    ScreenScaffold(stringResource(R.string.ts_title), onBack, action = { PersistentAction(stringResource(R.string.ts_cta), onSave) }) {
        SectionTitle(partnerName)
        Text(stringResource(R.string.ts_description), color = AmarTheme.colors.content.secondary, style = AmarTheme.typography.bodySm)
        RegTextField(state.form.name, actions.onName, R.string.ts_name, R.string.ts_name_placeholder, shown[RegField.StoreName])
        AddressFields(state.form, state.regions, actions, shown)
        val revenueError = shown[RegField.Revenue]
        AmarTextField(
            value = state.revenue,
            onValueChange = onRevenue,
            modifier = Modifier.fillMaxWidth(),
            type = AmarTextFieldType.Currency,
            label = stringResource(R.string.reg_revenue),
            required = true,
            placeholder = stringResource(R.string.reg_revenue_placeholder),
            hint = errorHint(revenueError),
            error = revenueError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        RegChoice.storeLevel(state.choices).forEach { g ->
            RegChoiceField(g, state.choices[g], { onChoose(g, it) }, state.showErrors && g in missingChoices)
        }
        SectionTitle(stringResource(R.string.ts_location))
        LocationCaptureContent(location, locationActions)
        if (location.found == null) {
            AmarButton(
                stringResource(R.string.ts_take_location), locationActions.onLocate, Modifier.fillMaxWidth(),
                type = AmarButtonType.Secondary,
                size = AmarButtonSize.Medium,
                loading = location.locating,
            )
            if (state.showErrors) AmarInfoBox(stringResource(R.string.ts_location_required), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Error)
        }
        nearby?.let { AmarInfoBox(stringResource(R.string.ts_near_warning, it.meters, it.name), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning) }
    }
}
