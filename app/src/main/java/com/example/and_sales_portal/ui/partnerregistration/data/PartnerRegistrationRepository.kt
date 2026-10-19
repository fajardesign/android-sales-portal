package com.example.and_sales_portal.ui.partnerregistration.data

import kotlinx.serialization.Serializable

/** A region option (province, regency/city, district, village) shown in a Dropdown picker. */
@Serializable
data class Region(val id: String, val name: String)

/** A bank option for REG-05 (web portal bank keys). */
@Serializable
data class Bank(val code: String, val name: String)

/** Progress of one document upload in REG-06. */
sealed interface UploadEvent {
    data class Progress(val fraction: Float) : UploadEvent
    data object Done : UploadEvent
}

/**
 * Reference data and uploads of the Partner Registration flow. There is no mobile API contract yet (CLAUDE.md §1);
 * [FakePartnerRegistrationRepository] stands in until it exists. Submissions go to
 * [com.example.and_sales_portal.data.partner.PartnerRepository].
 */
interface PartnerRegistrationRepository {
    suspend fun provinces(): List<Region>
    suspend fun regencies(provinceId: String): List<Region>
    suspend fun districts(regencyId: String): List<Region>
    suspend fun villages(districtId: String): List<Region>
    suspend fun banks(): List<Bank>

    /** Uploads one document, reporting [UploadEvent.Progress] then [UploadEvent.Done]. */
    suspend fun uploadDocument(path: String, onEvent: (UploadEvent) -> Unit)

    /** REG-12 consent page (decision 2026-10-19 Q2: it is loaded and can fail); `false` = could not be loaded. */
    suspend fun loadConsentPage(): Boolean
}
