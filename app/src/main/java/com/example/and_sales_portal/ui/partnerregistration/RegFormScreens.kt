package com.example.and_sales_portal.ui.partnerregistration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.components.inputs.AmarDropdownPicker
import com.example.and_sales_portal.designsystem.components.inputs.AmarTextField
import com.example.and_sales_portal.designsystem.components.inputs.AmarTextFieldType
import com.example.and_sales_portal.designsystem.theme.AmarFormTokens
import com.example.and_sales_portal.ui.partnerregistration.data.Region

/** Callbacks of REG-02. */
class PartnerInfoActions(
    val onName: (String) -> Unit = {},
    val onAddress: (String) -> Unit = {},
    val onProvince: (Region) -> Unit = {},
    val onRegency: (Region) -> Unit = {},
    val onDistrict: (Region) -> Unit = {},
    val onVillage: (Region) -> Unit = {},
    val onRt: (String) -> Unit = {},
    val onRw: (String) -> Unit = {},
    val onContinue: () -> Unit = {},
    val onBack: () -> Unit = {},
)

/**
 * Spec: REG-02 Partner info (Figma "01 | Partner Information"). Region Dropdowns cascade: each one stays disabled until
 * its parent is chosen and its list is loaded. No search in the region sheets (Figma has none).
 */
@Composable
fun RegPartnerInfoScreen(state: PartnerRegistrationState, actions: PartnerInfoActions) {
    val form = state.partner
    val regions = state.regions
    val missing = if (RegStep.PartnerInfo in state.showErrors) PartnerRegistrationValidator.missingPartnerInfo(form) else emptySet()
    RegStepScaffold(
        step = RegStep.PartnerInfo,
        title = stringResource(R.string.reg_partner_title),
        description = stringResource(R.string.reg_partner_description),
        onBack = actions.onBack,
        cta = stringResource(R.string.reg_partner_cta),
        onCta = actions.onContinue,
    ) {
        RegTextField(form.name, actions.onName, R.string.reg_partner_name, R.string.reg_partner_name_placeholder, RegField.PartnerName in missing)
        RegTextField(form.address, actions.onAddress, R.string.reg_address, R.string.reg_address_placeholder, RegField.Address in missing)
        RegRegionPicker(form.province, regions.provinces, actions.onProvince, R.string.reg_province, R.string.reg_province_placeholder, R.string.reg_province_sheet, RegField.Province in missing, enabled = regions.provinces.isNotEmpty())
        RegRegionPicker(form.regency, regions.regencies, actions.onRegency, R.string.reg_regency, R.string.reg_regency_placeholder, R.string.reg_regency_sheet, RegField.Regency in missing, enabled = form.province != null && regions.regencies.isNotEmpty())
        RegRegionPicker(form.district, regions.districts, actions.onDistrict, R.string.reg_district, R.string.reg_district_placeholder, R.string.reg_district_sheet, RegField.District in missing, enabled = form.regency != null && regions.districts.isNotEmpty())
        RegRegionPicker(form.village, regions.villages, actions.onVillage, R.string.reg_village, R.string.reg_village_placeholder, R.string.reg_village_sheet, RegField.Village in missing, enabled = form.district != null && regions.villages.isNotEmpty())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AmarFormTokens.SideBySideGap)) {
            RegTextField(form.rt, actions.onRt, R.string.reg_rt, R.string.reg_rt_rw_placeholder, RegField.Rt in missing, Modifier.weight(1f))
            RegTextField(form.rw, actions.onRw, R.string.reg_rw, R.string.reg_rt_rw_placeholder, RegField.Rw in missing, Modifier.weight(1f))
        }
    }
}

/** Callbacks of REG-03 and REG-04. */
class DetailsActions(
    val onEmail: (String) -> Unit = {},
    val onRevenue: (String) -> Unit = {},
    val onOutletCount: (String) -> Unit = {},
    val onPicName: (String) -> Unit = {},
    val onPicEmail: (String) -> Unit = {},
    val onPicPhone: (String) -> Unit = {},
    val onChoose: (RegChoice, String) -> Unit = { _, _ -> },
    val onContinue: () -> Unit = {},
    val onBack: () -> Unit = {},
)

/**
 * Spec: REG-03 Data Bisnis (Figma "02 | Business Information"). Follows Figma (decision 2026-10-06): Email, Omzet Toko and
 * Jumlah Tempat Usaha, then eight Chip groups; no Nama Toko and no Radio Buttons or Dropdowns.
 */
@Composable
fun RegBusinessScreen(state: PartnerRegistrationState, actions: DetailsActions) {
    val form = state.business
    val showErrors = RegStep.Business in state.showErrors
    val missing = if (showErrors) PartnerRegistrationValidator.missingBusiness(form) else emptySet()
    val missingChoices = if (showErrors) PartnerRegistrationValidator.missingChoices(state.choices, RegChoice.business) else emptySet()
    RegStepScaffold(
        step = RegStep.Business,
        title = stringResource(R.string.reg_business_title),
        description = stringResource(R.string.reg_business_description),
        onBack = actions.onBack,
        cta = stringResource(R.string.reg_business_cta),
        onCta = actions.onContinue,
    ) {
        RegTextField(form.email, actions.onEmail, R.string.reg_email, R.string.reg_email_placeholder, RegField.Email in missing, keyboardType = KeyboardType.Email)
        RegTextField(form.revenue, actions.onRevenue, R.string.reg_revenue, R.string.reg_revenue_placeholder, RegField.Revenue in missing, type = AmarTextFieldType.Currency)
        RegTextField(form.outletCount, actions.onOutletCount, R.string.reg_outlet_count, R.string.reg_outlet_count_placeholder, RegField.OutletCount in missing, keyboardType = KeyboardType.Number)
        RegChoice.business.forEach { group ->
            RegChoiceField(group, state.choices[group], { actions.onChoose(group, it) }, group in missingChoices)
        }
    }
}

/** Spec: REG-04 Informasi PIC (Figma "03 | PIC Information"), including the Email field from Figma. */
@Composable
fun RegPicScreen(state: PartnerRegistrationState, actions: DetailsActions) {
    val form = state.pic
    val showErrors = RegStep.Pic in state.showErrors
    val missing = if (showErrors) PartnerRegistrationValidator.missingPic(form) else emptySet()
    val statusMissing = showErrors && state.choices[RegChoice.PicStatus] == null
    RegStepScaffold(
        step = RegStep.Pic,
        title = stringResource(R.string.reg_pic_title),
        description = stringResource(R.string.reg_pic_description),
        onBack = actions.onBack,
        cta = stringResource(R.string.reg_pic_cta),
        onCta = actions.onContinue,
    ) {
        RegTextField(form.name, actions.onPicName, R.string.reg_pic_name, R.string.reg_pic_name_placeholder, RegField.PicName in missing)
        RegTextField(form.email, actions.onPicEmail, R.string.reg_email, R.string.reg_email_placeholder, RegField.PicEmail in missing, keyboardType = KeyboardType.Email)
        RegTextField(form.phone, actions.onPicPhone, R.string.reg_phone, R.string.reg_phone_placeholder, RegField.PicPhone in missing, type = AmarTextFieldType.Phone)
        RegChoiceField(RegChoice.PicStatus, state.choices[RegChoice.PicStatus], { actions.onChoose(RegChoice.PicStatus, it) }, statusMissing)
    }
}

/** A required text field with the step's error hint. */
@Composable
fun RegTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: Int,
    placeholder: Int,
    error: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth(),
    type: AmarTextFieldType = AmarTextFieldType.Text,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
) {
    AmarTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        type = type,
        label = stringResource(label),
        required = true,
        placeholder = stringResource(placeholder),
        hint = requiredHint(error),
        error = error,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    )
}

@Composable
private fun RegRegionPicker(
    value: Region?,
    options: List<Region>,
    onSelect: (Region) -> Unit,
    label: Int,
    placeholder: Int,
    sheetTitle: Int,
    error: Boolean,
    enabled: Boolean,
) {
    AmarDropdownPicker(
        value = value,
        options = options,
        onSelect = onSelect,
        sheetTitle = stringResource(sheetTitle),
        modifier = Modifier.fillMaxWidth(),
        optionLabel = { it.name },
        label = stringResource(label),
        required = true,
        placeholder = stringResource(placeholder),
        hint = requiredHint(error),
        error = error,
        enabled = enabled,
    )
}
