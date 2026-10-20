package com.example.and_sales_portal.ui.partnerregistration

import androidx.annotation.StringRes
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.device.LocationResult
import com.example.and_sales_portal.data.partner.DocType
import com.example.and_sales_portal.data.partner.Keys
import com.example.and_sales_portal.ui.partnerregistration.data.Bank
import com.example.and_sales_portal.ui.partnerregistration.data.Region
import kotlinx.serialization.Serializable

/** Steps of the form ("LANGKAH n DARI 5"), with their Flow IDs (PRD v3 C2 steps 1–5). */
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

/** One Chip option; [key] is what is submitted (web portal keys). */
data class ChoiceOption(val key: String, @StringRes val label: Int)

/** Single-choice Chip groups of REG-03 and REG-04 (Figma `Choice field`), in screen order. */
enum class RegChoice(@StringRes val label: Int, val options: List<ChoiceOption>) {
    EntityType(
        R.string.reg_entity_type,
        listOf(ChoiceOption(Keys.ENTITY_PT, R.string.reg_entity_pt), ChoiceOption(Keys.ENTITY_CV, R.string.reg_entity_cv), ChoiceOption(Keys.ENTITY_INDIVIDU, R.string.reg_entity_individual)),
    ),
    Channel(R.string.reg_channel, listOf(ChoiceOption("STORE", R.string.reg_channel_store), ChoiceOption("NON_STORE", R.string.reg_channel_non_store))),
    StoreType(R.string.reg_store_type, listOf(ChoiceOption(Keys.STORE_ONLINE, R.string.reg_store_type_online), ChoiceOption(Keys.STORE_OFFLINE, R.string.reg_store_type_offline))),
    StoreScale(R.string.reg_store_scale, listOf(ChoiceOption("MODERN", R.string.reg_store_scale_modern), ChoiceOption("TRADITIONAL", R.string.reg_store_scale_traditional))),
    ProductSold(
        R.string.reg_product_sold,
        listOf(ChoiceOption("BRAND_NEW", R.string.reg_product_sold_new), ChoiceOption("USED", R.string.reg_product_sold_used), ChoiceOption("BLENDED", R.string.reg_product_sold_blended)),
    ),
    StoreLocation(
        R.string.reg_store_location,
        listOf(ChoiceOption("SEPARATE", R.string.reg_store_location_separate), ChoiceOption("PINGGIR_JALAN", R.string.reg_store_location_street), ChoiceOption("MALL", R.string.reg_store_location_mall)),
    ),
    ProductType(R.string.reg_product_type, listOf(ChoiceOption("GADGET", R.string.reg_product_type_gadget), ChoiceOption("NON_GADGET", R.string.reg_product_type_non_gadget))),
    OfflineChannel(R.string.reg_offline_channel, listOf(ChoiceOption("AGENCY", R.string.reg_offline_channel_agency), ChoiceOption("NON_AGENCY", R.string.reg_offline_channel_non_agency))),
    PicStatus(R.string.reg_pic_status, listOf(ChoiceOption(Keys.PIC_OWNER, R.string.reg_pic_status_owner), ChoiceOption(Keys.PIC_KARYAWAN, R.string.reg_pic_status_employee))),
    ;

    fun labelOf(key: String?): Int? = options.firstOrNull { it.key == key }?.label

    companion object {
        /** Partner-level groups of REG-03. */
        val partnerLevel = listOf(EntityType, Channel)

        /** Toko-level groups (REG-03 and Tambah Toko). PRD C2: Channel Offline only when Tipe Toko = OFFLINE. */
        fun storeLevel(choices: Map<RegChoice, String>): List<RegChoice> =
            listOf(StoreType, StoreScale, ProductSold, StoreLocation, ProductType) +
                if (choices[StoreType] == Keys.STORE_OFFLINE) listOf(OfflineChannel) else emptyList()

        /** Groups shown in REG-03, in Figma order. */
        fun business(choices: Map<RegChoice, String>): List<RegChoice> = partnerLevel + storeLevel(choices)
    }
}

/** Fields that can show an error under them. */
enum class RegField {
    PartnerName, Address, Province, Regency, District, Village, Rt, Rw,
    Email, Revenue, OutletCount,
    PicName, PicEmail, PicPhone,
    Bank, Branch, AccountNumber, AccountName,
    StoreName,
}

/** Error under a field: a message, with an optional number in it ("Minimal 3 karakter"). */
data class FieldError(@StringRes val message: Int, val arg: Int? = null)

@Serializable
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

@Serializable
data class BusinessForm(
    val email: String = "",
    /** Digits only; shown as "Rp 100.000.000" by the Currency field. */
    val revenue: String = "",
    val outletCount: String = "",
)

@Serializable
data class PicForm(
    val name: String = "",
    val email: String = "",
    /** Digits after the fixed "+62" prefix. */
    val phone: String = "",
    val privyId: String = "",
)

@Serializable
data class BankForm(
    val bank: Bank? = null,
    val branch: String = "",
    val accountNumber: String = "",
    val accountName: String = "",
)

@Serializable
enum class UploadStatus { Uploading, Uploaded, Failed }

/** A document picked for upload: a local copy of the file (kept with the draft). */
@Serializable
data class DocumentUpload(
    val path: String,
    val fileName: String,
    val sizeBytes: Long,
    val status: UploadStatus,
    val progress: Float = 0f,
)

/** Why a picked file was refused. */
enum class FileError { TooLarge, WrongType }

/** Region lists loaded for the cascading Dropdowns of REG-02. */
data class RegionLists(
    val provinces: List<Region> = emptyList(),
    val regencies: List<Region> = emptyList(),
    val districts: List<Region> = emptyList(),
    val villages: List<Region> = emptyList(),
)

/** Saved between steps (PRD C2 "draft is saved locally between steps"). */
@Serializable
data class RegDraft(
    val partner: PartnerInfoForm,
    val business: BusinessForm,
    val pic: PicForm,
    val bank: BankForm,
    val choices: Map<String, String>,
    val documents: Map<String, DocumentUpload>,
)

/** Result of "Kirim Pengajuan". */
data class Submitted(val registrationNumber: String, val partnerName: String, val atMillis: Long)

data class PartnerRegistrationState(
    val partner: PartnerInfoForm = PartnerInfoForm(),
    val regions: RegionLists = RegionLists(),
    val business: BusinessForm = BusinessForm(),
    val pic: PicForm = PicForm(),
    /** Chosen option key per Chip group (REG-03 and Status PIC). */
    val choices: Map<RegChoice, String> = emptyMap(),
    val bank: BankForm = BankForm(),
    val banks: List<Bank> = emptyList(),
    val documents: Map<DocType, DocumentUpload> = emptyMap(),
    val fileErrors: Map<DocType, FileError> = emptyMap(),
    /** Steps whose CTA was pressed with errors: their fields show the error. */
    val showErrors: Set<RegStep> = emptySet(),
    /** No. Handphone already used by another partner (checked on the step's CTA, review 2026-10-09). */
    val taken: Set<RegField> = emptySet(),
    /** REG-08 location (PRD C2 step 6). */
    val location: LocationResult.Found? = null,
    /** Name of a saved draft offered when the flow opens; `null` when there is none or it was answered. */
    val draftOffer: String? = null,
    val submitting: Boolean = false,
    val submitted: Submitted? = null,
) {
    val entityType: String? get() = choices[RegChoice.EntityType]

    /** Documents for the entity type and Status PIC, with whether each is mandatory. */
    val documentList: List<Pair<DocType, Boolean>> get() = DocType.forEntity(entityType, choices[RegChoice.PicStatus])
}
