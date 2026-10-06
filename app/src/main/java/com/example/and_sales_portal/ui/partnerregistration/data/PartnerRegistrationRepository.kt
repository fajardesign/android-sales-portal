package com.example.and_sales_portal.ui.partnerregistration.data

import android.net.Uri

/** A region option (province, regency/city, district, village) shown in a Dropdown picker. */
data class Region(val id: String, val name: String)

/** A bank option for REG-05. */
data class Bank(val code: String, val name: String)

/** Result of the account number inquiry ("Periksa") in REG-05. */
sealed interface AccountInquiryResult {
    data class Found(val accountName: String) : AccountInquiryResult
    data object NotFound : AccountInquiryResult
}

/** Progress of one document upload in REG-06. */
sealed interface UploadEvent {
    data class Progress(val fraction: Float) : UploadEvent
    data class Done(val documentId: String) : UploadEvent
}

/** Server answer after "Kirim Dokumen". */
data class SubmissionResult(val submissionId: String, val submittedAtMillis: Long)

/**
 * Data needed by the Partner Registration flow (REG-02 … REG-06).
 *
 * There is no mobile API contract yet (CLAUDE.md §1); [FakePartnerRegistrationRepository] stands in until it exists.
 */
interface PartnerRegistrationRepository {
    suspend fun provinces(): List<Region>
    suspend fun regencies(provinceId: String): List<Region>
    suspend fun districts(regencyId: String): List<Region>
    suspend fun villages(districtId: String): List<Region>
    suspend fun banks(): List<Bank>
    suspend fun inquireAccount(bankCode: String, accountNumber: String): AccountInquiryResult

    /** Uploads one document, reporting [UploadEvent.Progress] then [UploadEvent.Done]. */
    suspend fun uploadDocument(uri: Uri, onEvent: (UploadEvent) -> Unit)

    suspend fun submit(registration: PartnerRegistrationSubmission): SubmissionResult
}

/** Everything collected in REG-02 … REG-06, sent with "Kirim Dokumen". */
data class PartnerRegistrationSubmission(
    val partnerName: String,
    val address: String,
    val villageId: String,
    val rt: String,
    val rw: String,
    val email: String,
    val revenue: Long,
    val outletCount: String,
    val business: Map<String, String>,
    val picName: String,
    val picEmail: String,
    val picPhone: String,
    val picStatus: String,
    val bankCode: String,
    val accountNumber: String,
    val accountName: String,
    val documentIds: Map<String, String>,
)
