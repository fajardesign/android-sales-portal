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

/** Callbacks of REG-02 (also the address part of Tambah Toko). */
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
    /** REG-02 only. */
    val onReferral: (String) -> Unit = {},
)

/**
 * Spec: REG-02 Informasi Partner (PRD v3 C2 step 1; Figma "01 | Partner Information"): Nama Partner (3–100), Alamat
 * sesuai legalitas (5–255), Provinsi → Kabupaten/Kota → Kecamatan → Kelurahan, RT and RW (3 digits), Kode Referral
 * (revision 2026-10-08: required, letters and digits, max. 20). Region Dropdowns
 * cascade: each one stays disabled until its parent is chosen and its list is loaded.
 */
@Composable
fun RegPartnerInfoScreen(state: PartnerRegistrationState, actions: PartnerInfoActions) {
    RegStepScaffold(
        step = RegStep.PartnerInfo,
        title = stringResource(R.string.reg_partner_title),
        description = stringResource(R.string.reg_partner_description),
        onBack = actions.onBack,
        cta = stringResource(R.string.reg_partner_cta),
        onCta = actions.onContinue,
    ) {
        val errors = if (RegStep.PartnerInfo in state.showErrors) PartnerRegistrationValidator.partnerInfo(state.partner) else emptyMap()
        RegTextField(state.partner.name, actions.onName, R.string.reg_partner_name, R.string.reg_partner_name_placeholder, errors[RegField.PartnerName])
        AddressFields(state.partner, state.regions, actions, errors)
        RegTextField(state.partner.referralCode, actions.onReferral, R.string.reg_referral, R.string.reg_referral_placeholder, errors[RegField.ReferralCode])
    }
}

/** Alamat, region Dropdowns and RT/RW (REG-02 and Tambah Toko). */
@Composable
fun AddressFields(form: PartnerInfoForm, regions: RegionLists, actions: PartnerInfoActions, errors: Map<RegField, FieldError>) {
    RegTextField(form.address, actions.onAddress, R.string.reg_address, R.string.reg_address_placeholder, errors[RegField.Address])
    RegRegionPicker(form.province, regions.provinces, actions.onProvince, R.string.reg_province, R.string.reg_province_placeholder, R.string.reg_province_sheet, errors[RegField.Province], enabled = regions.provinces.isNotEmpty())
    RegRegionPicker(form.regency, regions.regencies, actions.onRegency, R.string.reg_regency, R.string.reg_regency_placeholder, R.string.reg_regency_sheet, errors[RegField.Regency], enabled = form.province != null && regions.regencies.isNotEmpty())
    RegRegionPicker(form.district, regions.districts, actions.onDistrict, R.string.reg_district, R.string.reg_district_placeholder, R.string.reg_district_sheet, errors[RegField.District], enabled = form.regency != null && regions.districts.isNotEmpty())
    RegRegionPicker(form.village, regions.villages, actions.onVillage, R.string.reg_village, R.string.reg_village_placeholder, R.string.reg_village_sheet, errors[RegField.Village], enabled = form.district != null && regions.villages.isNotEmpty())
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AmarFormTokens.SideBySideGap)) {
        RegTextField(form.rt, actions.onRt, R.string.reg_rt, R.string.reg_rt_rw_placeholder, errors[RegField.Rt], Modifier.weight(1f), keyboardType = KeyboardType.Number)
        RegTextField(form.rw, actions.onRw, R.string.reg_rw, R.string.reg_rt_rw_placeholder, errors[RegField.Rw], Modifier.weight(1f), keyboardType = KeyboardType.Number)
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
    val onPrivyId: (String) -> Unit = {},
    val onPksEmail: (String) -> Unit = {},
    val onChoose: (RegChoice, String) -> Unit = { _, _ -> },
    val onContinue: () -> Unit = {},
    val onBack: () -> Unit = {},
)

/**
 * Spec: REG-03 Data Bisnis (PRD v3 C2 step 2; Figma "02 | Business Information"): Email, Omzet Toko (Rp > 0), Jumlah
 * Tempat Usaha (≥ 1), then the Chip groups; Channel Offline appears only when Tipe Toko = Offline.
 */
@Composable
fun RegBusinessScreen(state: PartnerRegistrationState, actions: DetailsActions) {
    val showErrors = RegStep.Business in state.showErrors
    val errors = if (showErrors) PartnerRegistrationValidator.business(state.business) else emptyMap()
    val groups = RegChoice.business(state.choices)
    val missingChoices = if (showErrors) PartnerRegistrationValidator.missingChoices(state.choices, groups) else emptySet()
    RegStepScaffold(
        step = RegStep.Business,
        title = stringResource(R.string.reg_business_title),
        description = stringResource(R.string.reg_business_description),
        onBack = actions.onBack,
        cta = stringResource(R.string.reg_business_cta),
        onCta = actions.onContinue,
    ) {
        val form = state.business
        RegTextField(form.email, actions.onEmail, R.string.reg_email, R.string.reg_email_placeholder, errors[RegField.Email], keyboardType = KeyboardType.Email)
        RegTextField(form.revenue, actions.onRevenue, R.string.reg_revenue, R.string.reg_revenue_placeholder, errors[RegField.Revenue], type = AmarTextFieldType.Currency)
        RegTextField(form.outletCount, actions.onOutletCount, R.string.reg_outlet_count, R.string.reg_outlet_count_placeholder, errors[RegField.OutletCount], keyboardType = KeyboardType.Number)
        groups.forEach { group ->
            RegChoiceField(group, state.choices[group], { actions.onChoose(group, it) }, group in missingChoices)
        }
    }
}

/**
 * Spec: REG-04 Informasi PIC & PKS (PRD v3 C2 step 3): Nama PIC, Email PIC and No. Handphone (+62; both used to sign in
 * to the Partner account, revision 2026-10-08), Status PIC, Privy ID (optional) and Email undangan PKS (defaults to Email PIC).
 */
@Composable
fun RegPicScreen(state: PartnerRegistrationState, actions: DetailsActions) {
    val form = state.pic
    val showErrors = RegStep.Pic in state.showErrors
    val errors = if (showErrors) PartnerRegistrationValidator.pic(form) else emptyMap()
    RegStepScaffold(
        step = RegStep.Pic,
        title = stringResource(R.string.reg_pic_title),
        description = stringResource(R.string.reg_pic_description),
        onBack = actions.onBack,
        cta = stringResource(R.string.reg_pic_cta),
        onCta = actions.onContinue,
    ) {
        RegTextField(form.name, actions.onPicName, R.string.reg_pic_name, R.string.reg_pic_name_placeholder, errors[RegField.PicName])
        RegTextField(form.email, actions.onPicEmail, R.string.reg_pic_email, R.string.reg_pic_email_placeholder, errors[RegField.PicEmail], keyboardType = KeyboardType.Email, hint = R.string.reg_pic_email_hint)
        RegTextField(form.phone, actions.onPicPhone, R.string.reg_phone, R.string.reg_phone_placeholder, errors[RegField.PicPhone], type = AmarTextFieldType.Phone)
        RegChoiceField(RegChoice.PicStatus, state.choices[RegChoice.PicStatus], { actions.onChoose(RegChoice.PicStatus, it) }, showErrors && state.choices[RegChoice.PicStatus] == null)
        RegTextField(form.privyId, actions.onPrivyId, R.string.reg_privy_id, R.string.reg_privy_id_placeholder, null, required = false, hint = R.string.reg_privy_id_hint)
        RegTextField(form.pksEmail, actions.onPksEmail, R.string.reg_pks_email, R.string.reg_pks_email_placeholder, errors[RegField.PksEmail], keyboardType = KeyboardType.Email, hint = R.string.reg_pks_email_hint)
    }
}

/** A form text field with its error message (or [hint] when there is no error). */
@Composable
fun RegTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: Int,
    placeholder: Int,
    error: FieldError?,
    modifier: Modifier = Modifier.fillMaxWidth(),
    type: AmarTextFieldType = AmarTextFieldType.Text,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    required: Boolean = true,
    hint: Int? = null,
) {
    AmarTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        type = type,
        label = stringResource(label),
        required = required,
        placeholder = stringResource(placeholder),
        hint = errorHint(error, hint?.let { stringResource(it) }),
        error = error != null,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    )
}

@Composable
fun RegRegionPicker(
    value: Region?,
    options: List<Region>,
    onSelect: (Region) -> Unit,
    label: Int,
    placeholder: Int,
    sheetTitle: Int,
    error: FieldError?,
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
        hint = errorHint(error),
        error = error != null,
        enabled = enabled,
    )
}
