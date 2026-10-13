package com.example.and_sales_portal.data.demo

import java.time.ZoneId

/**
 * Areas and people of the prototype, identical to the web portal sample data
 * (`admin-sales-portal/src/lib/constants.js` AREAS, `src/api/db.js` users) so both apps show the same organisation.
 * Supabase RLS only returns the signed-in user's own `app_user` row, so names of leaders and team members come from here.
 */
object DemoDirectory {

    data class Area(
        val id: Int,
        val code: String,
        val name: String,
        val province: String,
        val city: String,
        val district: String,
        val village: String,
        val lat: Double,
        val lng: Double,
        /** Local time of the area (decision 2026-10-08: work hours follow local time, e.g. Makassar WITA). */
        val zoneId: String = "Asia/Jakarta",
    ) {
        val zone: ZoneId get() = ZoneId.of(zoneId)
        /** WIB / WITA / WIT. */
        val zoneLabel: String get() = when (zoneId) { "Asia/Makassar" -> "WITA"; "Asia/Jayapura" -> "WIT"; else -> "WIB" }
    }

    data class Person(
        val id: Long,
        val fullName: String,
        val role: String,
        val areaId: Int,
        val supervisorId: Long?,
        val status: String,
        val tlLevel: String? = null,
        /** Account created this many hours before the portal's reference time (7 Oct 2026, 10:30 WIB). */
        val createdHoursAgo: Long = 24 * 30,
    ) {
        /** Registration date ("Terdaftar"). */
        val createdAt: Long get() = REFERENCE - createdHoursAgo * 3_600_000L
        val username: String get() = fullName.lowercase().replace(Regex("[^a-z ]"), "").trim().replace(Regex("\\s+"), ".")
        val email: String get() = "$username@amarbank.co.id"
        /** Same formula as the portal sample (`81` + digits), shown with +62. */
        val phone: String get() = "81" + (200_000_000 + id * 7919).toString().take(9)
    }

    val areas = listOf(
        Area(1, "MKS", "Makassar", "Sulawesi Selatan", "Kota Makassar", "Panakkukang", "Pandang", -5.1477, 119.4327, "Asia/Makassar"),
        Area(2, "BDG", "Bandung", "Jawa Barat", "Kota Bandung", "Coblong", "Dago", -6.8915, 107.6107),
        Area(3, "MDN", "Medan", "Sumatera Utara", "Kota Medan", "Medan Petisah", "Sei Sikambing D", 3.5952, 98.6722),
        Area(4, "SBY", "Surabaya", "Jawa Timur", "Kota Surabaya", "Tegalsari", "Kedungdoro", -7.2575, 112.7521),
        Area(5, "JKT", "Jakarta", "DKI Jakarta", "Kota Jakarta Barat", "Grogol Petamburan", "Tanjung Duren Selatan", -6.1767, 106.7905),
        Area(6, "DPS", "Denpasar", "Bali", "Kota Denpasar", "Denpasar Barat", "Dauh Puri", -8.6705, 115.2126, "Asia/Makassar"),
        Area(7, "PLG", "Palembang", "Sumatera Selatan", "Kota Palembang", "Ilir Timur I", "Sungai Pangeran", -2.9761, 104.7754),
    )

    private val basePeople = listOf(
        Person(3, "Hasan Basri", "APL", 1, null, "ACTIVE", createdHoursAgo = 2160),
        Person(4, "Lestari Wulandari", "APL", 2, null, "ACTIVE", createdHoursAgo = 2112),
        Person(5, "Joko Susilo", "APL", 3, null, "ACTIVE", createdHoursAgo = 2040),
        Person(6, "Andi Pratama", "TL", 1, 3, "ACTIVE", "SENIOR", createdHoursAgo = 1920),
        Person(7, "Budi Santoso", "TL", 2, 4, "ACTIVE", "JUNIOR", createdHoursAgo = 1872),
        Person(8, "Rahmat Hidayat", "TL", 3, 5, "ACTIVE", "JUNIOR", createdHoursAgo = 1800),
        Person(9, "Maya Anggraini", "TL", 5, 4, "ACTIVE", "SENIOR", createdHoursAgo = 1680),
        Person(10, "Fajar Nugroho", "TL", 4, 5, "PENDING", "SENIOR", createdHoursAgo = 3),
        Person(11, "Siti Rahmawati", "SR", 1, 6, "ACTIVE", createdHoursAgo = 1560),
        Person(12, "Dewi Lestari", "SR", 2, 7, "ACTIVE", createdHoursAgo = 1536),
        Person(13, "Agus Setiawan", "SR", 5, 9, "ACTIVE", createdHoursAgo = 1440),
        Person(14, "Yohana Sitorus", "SR", 3, 8, "PENDING", createdHoursAgo = 40),
        Person(15, "Eko Saputra", "SA", 1, 6, "ACTIVE", createdHoursAgo = 1488),
        Person(16, "Nurul Hidayah", "SA", 1, 6, "ACTIVE", createdHoursAgo = 1392),
        Person(17, "Taufik Hidayat", "SA", 2, 7, "ACTIVE", createdHoursAgo = 1368),
        Person(18, "Wulan Sari", "SA", 5, 9, "ACTIVE", createdHoursAgo = 1320),
        Person(19, "Putri Ayuningtyas", "SA", 2, 7, "PENDING", createdHoursAgo = 5),
        Person(20, "Nanda Kurniawan", "SA", 3, 8, "PENDING", createdHoursAgo = 30),
        Person(21, "Rizky Ramadhan", "SA", 1, 6, "DISABLED", createdHoursAgo = 2400),
        // Second TL of Area 1 (Makassar), same as the web portal sample, so a replacement leader exists (decision 2026-10-13).
        Person(22, "Bagus Wicaksono", "TL", 1, 3, "ACTIVE", "JUNIOR", createdHoursAgo = 24 * 40),
    )

    /** Status and leader changed after the sample (account disabled, leader replaced; web ACC-05). */
    @kotlinx.serialization.Serializable
    data class Change(val status: String? = null, val supervisorId: Long? = null)

    /** Changes on top of [basePeople], set by [com.example.and_sales_portal.data.team.DirectoryRepository]. */
    @Volatile
    var changes: Map<Long, Change> = emptyMap()

    /** The sample people with [changes] applied. */
    val people: List<Person>
        get() = basePeople.map { p -> changes[p.id]?.let { c -> p.copy(status = c.status ?: p.status, supervisorId = c.supervisorId ?: p.supervisorId) } ?: p }

    /** Registered office of an area (revision 2026-10-08: attendance within 3 km of an office or a toko). */
    data class Office(val areaId: Int, val name: String, val lat: Double, val lng: Double)

    /**
     * One sample office per area, same as the web portal (`admin-sales-portal/src/lib/constants.js` OFFICES: area
     * point +0.004 lat, −0.003 lng). Offices are managed by the Admin on the web later.
     */
    val offices: List<Office> = areas.map { Office(it.id, "Kantor Amar Bank ${it.name}", round6(it.lat + 0.004), round6(it.lng - 0.003)) }

    fun office(areaId: Int): Office? = offices.firstOrNull { it.areaId == areaId }

    private fun round6(v: Double) = Math.round(v * 1e6) / 1e6

    /** Reference time of the web portal sample (`admin-sales-portal/src/api/db.js` BASE). */
    private const val REFERENCE = 1_791_343_800_000L

    fun area(id: Int): Area? = areas.firstOrNull { it.id == id }
    fun person(id: Long?): Person? = id?.let { pid -> people.firstOrNull { it.id == pid } }

    /** A person as in the sample, before [changes] (sample partners keep their sample owner). */
    fun samplePerson(id: Long?): Person? = id?.let { pid -> basePeople.firstOrNull { it.id == pid } }

    /** Active SA/SR led by [tlId] (PRD C5 "Tim"). */
    fun teamOf(tlId: Long): List<Person> = people.filter { it.supervisorId == tlId && it.role in setOf("SA", "SR") && it.status != "DISABLED" }
}
