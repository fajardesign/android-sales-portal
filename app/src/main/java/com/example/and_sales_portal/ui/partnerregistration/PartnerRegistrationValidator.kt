package com.example.and_sales_portal.ui.partnerregistration

import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.partner.DocType

/**
 * Field rules of PRD v3 C2: Nama Partner 3–100, Alamat 5–255, RT/RW 3 digits, valid emails, Omzet > 0, Jumlah Tempat
 * Usaha ≥ 1, every other field required except Privy ID. The empty-field message stays "Informasi wajib diisi" (Figma).
 */
object PartnerRegistrationValidator {

    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val THREE_DIGITS = Regex("^\\d{3}$")
    private val ALPHANUMERIC = Regex("^[A-Za-z0-9]+$")
    const val REFERRAL_MAX = 20

    private val required = FieldError(R.string.reg_required)

    fun length(value: String, min: Int, max: Int): FieldError? = when {
        value.isBlank() -> required
        value.trim().length < min -> FieldError(R.string.reg_err_min, min)
        value.trim().length > max -> FieldError(R.string.reg_err_max, max)
        else -> null
    }

    fun email(value: String): FieldError? = when {
        value.isBlank() -> required
        !EMAIL.matches(value.trim()) -> FieldError(R.string.reg_err_email)
        else -> null
    }

    fun threeDigits(value: String): FieldError? = when {
        value.isBlank() -> required
        !THREE_DIGITS.matches(value) -> FieldError(R.string.reg_err_digits3)
        else -> null
    }

    /** Kode Referral (revision 2026-10-08): required, letters and digits only, max. 20 characters after trimming. */
    fun referralCode(value: String): FieldError? {
        val v = value.trim()
        return when {
            v.isEmpty() -> required
            !ALPHANUMERIC.matches(v) -> FieldError(R.string.reg_err_format)
            v.length > REFERRAL_MAX -> FieldError(R.string.reg_err_max, REFERRAL_MAX)
            else -> null
        }
    }

    fun revenue(value: String): FieldError? = when {
        value.isBlank() -> required
        (value.toLongOrNull() ?: 0L) <= 0L -> FieldError(R.string.reg_err_revenue)
        else -> null
    }

    private fun filled(value: String) = if (value.isBlank()) required else null
    private fun chosen(value: Any?) = if (value == null) required else null

    /** REG-02 (also the address part of Tambah Toko). */
    fun partnerInfo(form: PartnerInfoForm): Map<RegField, FieldError> = errors(
        RegField.PartnerName to length(form.name, 3, 100),
        RegField.Address to length(form.address, 5, 255),
        RegField.Province to chosen(form.province),
        RegField.Regency to chosen(form.regency),
        RegField.District to chosen(form.district),
        RegField.Village to chosen(form.village),
        RegField.Rt to threeDigits(form.rt),
        RegField.Rw to threeDigits(form.rw),
        RegField.ReferralCode to referralCode(form.referralCode),
    )

    /** REG-03 text fields. */
    fun business(form: BusinessForm): Map<RegField, FieldError> = errors(
        RegField.Email to email(form.email),
        RegField.Revenue to revenue(form.revenue),
        RegField.OutletCount to when {
            form.outletCount.isBlank() -> required
            (form.outletCount.toIntOrNull() ?: 0) < 1 -> FieldError(R.string.reg_err_outlet)
            else -> null
        },
    )

    /** Chip groups of [groups] without a chosen option. */
    fun missingChoices(choices: Map<RegChoice, String>, groups: List<RegChoice>): Set<RegChoice> =
        groups.filterTo(mutableSetOf()) { choices[it] == null }

    /** REG-04 text fields (Status PIC is a Chip group, Privy ID is optional). */
    fun pic(form: PicForm): Map<RegField, FieldError> = errors(
        RegField.PicName to filled(form.name),
        RegField.PicEmail to email(form.email),
        RegField.PicPhone to filled(form.phone),
        RegField.PksEmail to email(form.pksEmail),
    )

    /** REG-05: no bank inquiry in phase 1 (PRD C2). */
    fun bank(form: BankForm): Map<RegField, FieldError> = errors(
        RegField.Bank to chosen(form.bank),
        RegField.Branch to filled(form.branch),
        RegField.AccountNumber to filled(form.accountNumber),
        RegField.AccountName to filled(form.accountName),
    )

    /** REG-06: every mandatory document is uploaded and nothing is still uploading ("Lanjut Ringkasan" enabled). */
    fun documentsComplete(list: List<Pair<DocType, Boolean>>, documents: Map<DocType, DocumentUpload>): Boolean =
        list.isNotEmpty() &&
            list.all { (d, mandatory) -> !mandatory || documents[d]?.status == UploadStatus.Uploaded } &&
            documents.values.none { it.status == UploadStatus.Uploading }

    /** PRD C2: legal documents JPG/PNG/PDF up to 5 MB, store photos JPG/PNG up to 2 MB. */
    fun file(doc: DocType, fileName: String, sizeBytes: Long): FileError? {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        val allowed = if (doc.store) setOf("jpg", "jpeg", "png") else setOf("jpg", "jpeg", "png", "pdf")
        return when {
            ext !in allowed -> FileError.WrongType
            sizeBytes > doc.maxBytes -> FileError.TooLarge
            else -> null
        }
    }

    private fun errors(vararg pairs: Pair<RegField, FieldError?>): Map<RegField, FieldError> =
        pairs.mapNotNull { (f, e) -> e?.let { f to it } }.toMap()
}
