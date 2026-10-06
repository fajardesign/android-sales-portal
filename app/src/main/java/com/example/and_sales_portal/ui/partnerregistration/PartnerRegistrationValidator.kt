package com.example.and_sales_portal.ui.partnerregistration

/**
 * Required-field rules of the flow: every field is `*Mandatory` in the spec and Figma, and the only error is
 * "Informasi wajib diisi". No other rules (format, length) are specified, so none are added (CLAUDE.md §1).
 */
object PartnerRegistrationValidator {

    /** REG-02 fields that are still empty. */
    fun missingPartnerInfo(form: PartnerInfoForm): Set<RegField> = buildSet {
        if (form.name.isBlank()) add(RegField.PartnerName)
        if (form.address.isBlank()) add(RegField.Address)
        if (form.province == null) add(RegField.Province)
        if (form.regency == null) add(RegField.Regency)
        if (form.district == null) add(RegField.District)
        if (form.village == null) add(RegField.Village)
        if (form.rt.isBlank()) add(RegField.Rt)
        if (form.rw.isBlank()) add(RegField.Rw)
    }

    /** REG-03 text fields that are still empty. */
    fun missingBusiness(form: BusinessForm): Set<RegField> = buildSet {
        if (form.email.isBlank()) add(RegField.Email)
        if (form.revenue.isBlank()) add(RegField.Revenue)
        if (form.outletCount.isBlank()) add(RegField.OutletCount)
    }

    /** Chip groups of [groups] without a chosen option. */
    fun missingChoices(choices: Map<RegChoice, String>, groups: List<RegChoice>): Set<RegChoice> =
        groups.filterTo(mutableSetOf()) { choices[it] == null }

    /** REG-04 text fields that are still empty (Status PIC is a Chip group, see [missingChoices]). */
    fun missingPic(form: PicForm): Set<RegField> = buildSet {
        if (form.name.isBlank()) add(RegField.PicName)
        if (form.email.isBlank()) add(RegField.PicEmail)
        if (form.phone.isBlank()) add(RegField.PicPhone)
    }

    /** REG-05 fields needed before "Periksa" can run. */
    fun missingBankAccount(form: BankAccountForm): Set<RegField> = buildSet {
        if (form.bank == null) add(RegField.Bank)
        if (form.accountNumber.isBlank()) add(RegField.AccountNumber)
    }

    /** REG-06: every document required for the entity type is uploaded ("Kirim Dokumen" is enabled). */
    fun documentsComplete(required: List<RegDocument>, documents: Map<RegDocument, DocumentUpload>): Boolean =
        required.isNotEmpty() && required.all { documents[it]?.status == UploadStatus.Uploaded }
}
