package com.example.and_sales_portal.ui.partnerregistration.data

import android.net.Uri
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Stand-in for the missing mobile API (CLAUDE.md §1). Sample data follows the Figma screens
 * (Sumatera Utara → Kabupaten Deli Serdang → Deli Tua → Mekar Sari, bank list, "BUDI SANTOSO").
 *
 * - Regions: real names for Sumatera Utara (Deli Serdang) and DKI Jakarta; other provinces return clearly marked samples.
 * - Account inquiry: an account number ending in `0` is not found; any other number belongs to "BUDI SANTOSO".
 * - Upload: progress in 10 steps over about one second.
 */
class FakePartnerRegistrationRepository(private val latencyMillis: Long = 400) : PartnerRegistrationRepository {

    override suspend fun provinces(): List<Region> = respond(PROVINCES.mapIndexed { i, name -> Region("p$i", name) })

    override suspend fun regencies(provinceId: String): List<Region> = respond(
        REGENCIES[provinceName(provinceId)]?.mapIndexed { i, name -> Region("$provinceId-r$i", name) }
            ?: sample(provinceId, "Kabupaten/Kota contoh"),
    )

    override suspend fun districts(regencyId: String): List<Region> = respond(
        DISTRICTS[regionName(regencyId)]?.mapIndexed { i, name -> Region("$regencyId-d$i", name) }
            ?: sample(regencyId, "Kecamatan contoh"),
    )

    override suspend fun villages(districtId: String): List<Region> = respond(
        VILLAGES[regionName(districtId)]?.mapIndexed { i, name -> Region("$districtId-v$i", name) }
            ?: sample(districtId, "Kelurahan contoh"),
    )

    override suspend fun banks(): List<Bank> = respond(BANKS.map { (code, name) -> Bank(code, name) })

    override suspend fun inquireAccount(bankCode: String, accountNumber: String): AccountInquiryResult {
        delay(latencyMillis)
        return if (accountNumber.endsWith("0")) AccountInquiryResult.NotFound else AccountInquiryResult.Found("BUDI SANTOSO")
    }

    override suspend fun uploadDocument(uri: Uri, onEvent: (UploadEvent) -> Unit) {
        for (step in 1..10) {
            delay(latencyMillis / 4)
            onEvent(UploadEvent.Progress(step / 10f))
        }
        onEvent(UploadEvent.Done("doc-${Random.nextInt(100_000, 999_999)}"))
    }

    override suspend fun submit(registration: PartnerRegistrationSubmission): SubmissionResult {
        delay(latencyMillis * 3)
        return SubmissionResult("REG-2026-${Random.nextInt(1000, 9999)}", System.currentTimeMillis())
    }

    private suspend fun <T> respond(value: T): T {
        delay(latencyMillis)
        return value
    }

    private val names = mutableMapOf<String, String>()

    private fun provinceName(id: String) = PROVINCES.getOrNull(id.removePrefix("p").toIntOrNull() ?: -1)

    /** Name of a region id returned earlier (ids of real regions are remembered when listed). */
    private fun regionName(id: String): String? = names[id]

    private fun sample(parentId: String, label: String) = (1..3).map { Region("$parentId-s$it", "$label $it") }

    init {
        // Remember the names of the real sample regions so their children can be found by id.
        PROVINCES.forEachIndexed { pi, province ->
            REGENCIES[province]?.forEachIndexed { ri, regency ->
                val regencyId = "p$pi-r$ri"
                names[regencyId] = regency
                DISTRICTS[regency]?.forEachIndexed { di, district -> names["$regencyId-d$di"] = district }
            }
        }
    }

    private companion object {
        val PROVINCES = listOf(
            "Aceh", "Sumatera Utara", "Sumatera Barat", "Riau", "Jambi", "Sumatera Selatan", "Bengkulu", "Lampung",
            "Kepulauan Bangka Belitung", "Kepulauan Riau", "DKI Jakarta", "Jawa Barat", "Jawa Tengah", "DI Yogyakarta",
            "Jawa Timur", "Banten", "Bali", "Nusa Tenggara Barat", "Nusa Tenggara Timur", "Kalimantan Barat",
            "Kalimantan Tengah", "Kalimantan Selatan", "Kalimantan Timur", "Kalimantan Utara", "Sulawesi Utara",
            "Sulawesi Tengah", "Sulawesi Selatan", "Sulawesi Tenggara", "Gorontalo", "Sulawesi Barat", "Maluku",
            "Maluku Utara", "Papua", "Papua Barat", "Papua Selatan", "Papua Tengah", "Papua Pegunungan", "Papua Barat Daya",
        )
        val REGENCIES = mapOf(
            "Sumatera Utara" to listOf("Kabupaten Deli Serdang", "Kota Medan", "Kota Binjai"),
            "DKI Jakarta" to listOf("Kota Jakarta Pusat", "Kota Jakarta Selatan", "Kota Jakarta Barat"),
        )
        val DISTRICTS = mapOf(
            "Kabupaten Deli Serdang" to listOf("Deli Tua", "Lubuk Pakam", "Tanjung Morawa"),
            "Kota Jakarta Selatan" to listOf("Kebayoran Baru", "Setiabudi", "Tebet"),
        )
        val VILLAGES = mapOf(
            "Deli Tua" to listOf("Deli Tua", "Deli Tua Barat", "Deli Tua Timur", "Kedai Durian", "Mekar Sari", "Suka Makmur"),
            "Setiabudi" to listOf("Karet", "Kuningan Timur", "Setiabudi"),
        )
        val BANKS = listOf(
            "014" to "Bank Central Asia (BCA)", "002" to "Bank Rakyat Indonesia (BRI)", "008" to "Bank Mandiri",
            "009" to "Bank Negara Indonesia (BNI)", "200" to "Bank Tabungan Negara (BTN)", "022" to "Bank CIMB Niaga",
            "011" to "Bank Danamon", "013" to "Bank Permata", "427" to "Bank Syariah Indonesia (BSI)",
        )
    }
}
