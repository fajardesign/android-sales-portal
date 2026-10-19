package com.example.and_sales_portal.ui.partnerregistration.data

import com.example.and_sales_portal.data.partner.Banks
import kotlinx.coroutines.delay

/**
 * Stand-in for the missing mobile API (CLAUDE.md §1). Sample data follows the Figma screens
 * (Sumatera Utara → Kabupaten Deli Serdang → Deli Tua → Mekar Sari) and the demo area Makassar; banks of the web portal.
 *
 * - Regions: real names for Sulawesi Selatan (Makassar), Sumatera Utara (Deli Serdang) and DKI Jakarta; other provinces
 *   return clearly marked samples.
 * - Upload: progress in 10 steps over about one second.
 */
class FakePartnerRegistrationRepository(
    private val latencyMillis: Long = 400,
    /** Simulasi (debug) "Halaman persetujuan gagal dimuat". */
    private val consentFails: () -> Boolean = { false },
) : PartnerRegistrationRepository {

    override suspend fun loadConsentPage(): Boolean = respond(!consentFails())

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

    override suspend fun banks(): List<Bank> = respond(Banks.all.map { (code, name) -> Bank(code, name) })

    override suspend fun uploadDocument(path: String, onEvent: (UploadEvent) -> Unit) {
        for (step in 1..10) {
            delay(latencyMillis / 4)
            onEvent(UploadEvent.Progress(step / 10f))
        }
        onEvent(UploadEvent.Done)
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
            "Sulawesi Selatan" to listOf("Kota Makassar", "Kabupaten Gowa", "Kabupaten Maros"),
            "Sumatera Utara" to listOf("Kabupaten Deli Serdang", "Kota Medan", "Kota Binjai"),
            "DKI Jakarta" to listOf("Kota Jakarta Pusat", "Kota Jakarta Selatan", "Kota Jakarta Barat"),
        )
        val DISTRICTS = mapOf(
            "Kota Makassar" to listOf("Panakkukang", "Tamalanrea", "Biringkanaya", "Rappocini"),
            "Kabupaten Deli Serdang" to listOf("Deli Tua", "Lubuk Pakam", "Tanjung Morawa"),
            "Kota Jakarta Selatan" to listOf("Kebayoran Baru", "Setiabudi", "Tebet"),
        )
        val VILLAGES = mapOf(
            "Panakkukang" to listOf("Pandang", "Masale", "Karuwisi", "Tamamaung"),
            "Tamalanrea" to listOf("Tamalanrea", "Kapasa", "Bira"),
            "Deli Tua" to listOf("Deli Tua", "Deli Tua Barat", "Deli Tua Timur", "Kedai Durian", "Mekar Sari", "Suka Makmur"),
            "Setiabudi" to listOf("Karet", "Kuningan Timur", "Setiabudi"),
        )
    }
}
