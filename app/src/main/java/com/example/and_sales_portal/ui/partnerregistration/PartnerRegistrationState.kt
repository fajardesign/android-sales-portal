package com.example.and_sales_portal.ui.partnerregistration

import android.net.Uri
import androidx.annotation.StringRes
import com.example.and_sales_portal.R
import com.example.and_sales_portal.ui.partnerregistration.data.Bank
import com.example.and_sales_portal.ui.partnerregistration.data.Region
import com.example.and_sales_portal.ui.partnerregistration.data.SubmissionResult

/** Steps of the flow as numbered in Figma ("LANGKAH n DARI 5"), with their Flow IDs. */
enum class RegStep(val number: Int, val flowId: String) {
    PartnerInfo(1, "REG-02"),
    Business(2, "REG-03"),
    Pic(3, "REG-04"),
    BankAccount(4, "REG-05"),
    Documents(5, "REG-06"),
    ;

    companion object {
        val count = entries.size
    }
}

/** One Chip option; [key] is what is submitted. */
data class ChoiceOption(val key: String, @StringRes val label: Int)

/** Single-choice Chip groups of REG-03 and REG-04 (Figma `Choice field`), in screen order. */
enum class RegChoice(@StringRes val label: Int, val options: List<ChoiceOption>) {
    EntityType(
        R.string.reg_entity_type,
        listOf(
            ChoiceOption(ENTITY_PT, R.string.reg_entity_pt),
            ChoiceOption(ENTITY_CV, R.string.reg_entity_cv),
            ChoiceOption(ENTITY_INDIVIDUAL, R.string.reg_entity_individual),
        ),
    ),
    Channel(R.string.reg_channel, listOf(ChoiceOption("store", R.string.reg_channel_store), ChoiceOption("non_store", R.string.reg_channel_non_store))),
    StoreType(R.string.reg_store_type, listOf(ChoiceOption("online", R.string.reg_store_type_online), ChoiceOption("offline", R.string.reg_store_type_offline))),
    StoreScale(R.string.reg_store_scale, listOf(ChoiceOption("modern", R.string.reg_store_scale_modern), ChoiceOption("traditional", R.string.reg_store_scale_traditional))),
    ProductSold(
        R.string.reg_product_sold,
        listOf(
            ChoiceOption("brand_new", R.string.reg_product_sold_new),
            ChoiceOption("used", R.string.reg_product_sold_used),
            ChoiceOption("blended", R.string.reg_product_sold_blended),
        ),
    ),
    StoreLocation(
        R.string.reg_store_location,
        listOf(
            ChoiceOption("separate", R.string.reg_store_location_separate),
            ChoiceOption("street", R.string.reg_store_location_street),
            ChoiceOption("mall", R.string.reg_store_location_mall),
        ),
    ),
    ProductType(R.string.reg_product_type, listOf(ChoiceOption("gadget", R.string.reg_product_type_gadget), ChoiceOption("non_gadget", R.string.reg_product_type_non_gadget))),
    OfflineChannel(R.string.reg_offline_channel, listOf(ChoiceOption("agency", R.string.reg_offline_channel_agency), ChoiceOption("non_agency", R.string.reg_offline_channel_non_agency))),
    PicStatus(R.string.reg_pic_status, listOf(ChoiceOption("owner", R.string.reg_pic_status_owner), ChoiceOption("employee", R.string.reg_pic_status_employee))),
    ;

    companion object {
        /** Chip groups shown in REG-03, in Figma order. */
        val business = listOf(EntityType, Channel, StoreType, StoreScale, ProductSold, StoreLocation, ProductType, OfflineChannel)
    }
}

const val ENTITY_PT = "pt"
const val ENTITY_CV = "cv"
const val ENTITY_INDIVIDUAL = "individual"

/** Documents of REG-06. PT and CV upload company documents; Individu uploads photos (Figma frames "PT CV" and "Individual"). */
enum class RegDocument(@StringRes val label: Int) {
    Articles(R.string.reg_doc_articles),
    Deed(R.string.reg_doc_deed),
    SkKemenkumham(R.string.reg_doc_sk_kemenkumham),
    Domicile(R.string.reg_doc_domicile),
    NpwpCompany(R.string.reg_doc_npwp_company),
    Nib(R.string.reg_doc_nib),
    LocationPermit(R.string.reg_doc_location_permit),
    Ktp(R.string.reg_doc_ktp),
    Npwp(R.string.reg_doc_npwp),
    Passbook(R.string.reg_doc_passbook),
    ;

    companion object {
        private val company = listOf(Articles, Deed, SkKemenkumham, Domicile, NpwpCompany, Nib, LocationPermit)
        private val individual = listOf(Ktp, Npwp, Passbook)

        /** Documents required for the chosen `Jenis Badan Usaha`; empty while none is chosen. */
        fun requiredFor(entityType: String?): List<RegDocument> = when (entityType) {
            ENTITY_PT, ENTITY_CV -> company
            ENTITY_INDIVIDUAL -> individual
            else -> emptyList()
        }
    }
}

/** Fields of REG-02 … REG-05 that can show "Informasi wajib diisi". */
enum class RegField {
    PartnerName, Address, Province, Regency, District, Village, Rt, Rw,
    Email, Revenue, OutletCount,
    PicName, PicEmail, PicPhone,
    Bank, AccountNumber,
}

data class PartnerInfoForm(
    val name: String = "",
    val address: String = "",
    val province: Region? = null,
    val regency: Region? = null,
    val district: Region? = null,
    val village: Region? = null,
    val rt: String = "",
    val rw: String = "",
)

data class BusinessForm(
    val email: String = "",
    /** Digits only; shown as "Rp 100.000.000" by the Currency field. */
    val revenue: String = "",
    val outletCount: String = "",
)

data class PicForm(
    val name: String = "",
    val email: String = "",
    /** Digits after the fixed "+62" prefix. */
    val phone: String = "",
)

sealed interface AccountInquiry {
    data object Idle : AccountInquiry
    data object Loading : AccountInquiry
    data class Found(val accountName: String) : AccountInquiry
    data object NotFound : AccountInquiry
}

data class BankAccountForm(
    val bank: Bank? = null,
    val accountNumber: String = "",
    val inquiry: AccountInquiry = AccountInquiry.Idle,
)

enum class UploadStatus { Uploading, Uploaded, Failed }

data class DocumentUpload(
    val uri: Uri,
    val fileName: String,
    val sizeBytes: Long,
    val status: UploadStatus,
    val progress: Float = 0f,
    val documentId: String? = null,
)

/** Region lists loaded for the cascading Dropdowns of REG-02. */
data class RegionLists(
    val provinces: List<Region> = emptyList(),
    val regencies: List<Region> = emptyList(),
    val districts: List<Region> = emptyList(),
    val villages: List<Region> = emptyList(),
)

data class PartnerRegistrationState(
    val partner: PartnerInfoForm = PartnerInfoForm(),
    val regions: RegionLists = RegionLists(),
    val business: BusinessForm = BusinessForm(),
    val pic: PicForm = PicForm(),
    /** Chosen option key per Chip group (REG-03 and Status PIC). */
    val choices: Map<RegChoice, String> = emptyMap(),
    val bankAccount: BankAccountForm = BankAccountForm(),
    val banks: List<Bank> = emptyList(),
    val documents: Map<RegDocument, DocumentUpload> = emptyMap(),
    /** Steps whose CTA was pressed with missing fields: their empty fields show "Informasi wajib diisi". */
    val showErrors: Set<RegStep> = emptySet(),
    val submitting: Boolean = false,
    val submission: SubmissionResult? = null,
) {
    val entityType: String? get() = choices[RegChoice.EntityType]
    val requiredDocuments: List<RegDocument> get() = RegDocument.requiredFor(entityType)
}
