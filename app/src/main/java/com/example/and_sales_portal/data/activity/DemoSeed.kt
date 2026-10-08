package com.example.and_sales_portal.data.activity

import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.demo.seededRandom
import com.example.and_sales_portal.data.device.Place
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.PartnerStatus
import com.example.and_sales_portal.data.partner.Store
import java.time.LocalDate
import java.time.LocalTime

/**
 * Initial sample data, mirroring the web portal sample (`admin-sales-portal/src/api/db.js`): attendance (Monday–Saturday,
 * check in kerja 09:25–09:59 on time, 10:01–10:59 late, local time of the area, at the area office or a toko) and visit
 * check-ins (revision 2026-10-08: no hourly plan; about 4 of 5 present days, 12:00–16:59 local, near one of their toko).
 */
object DemoSeed {

    /** Days of history generated before the first day the user opens the app. */
    private const val HISTORY_DAYS = 14L

    /** Toko the user works at: SA/SR their assigned toko, TL the toko of the partners they own. */
    fun storesOf(partners: List<Partner>, userId: Long, role: SalesRole): List<Store> {
        val active = partners.filter { it.status == PartnerStatus.ACTIVE }
        val stores = when (role) {
            SalesRole.TL -> active.filter { it.ownerTlId == userId }.flatMap { it.stores }
            SalesRole.SA, SalesRole.SR -> active.flatMap { p -> p.stores.filter { userId in it.assigned } }
        }
        return stores.filter { it.status == "ACTIVE" }
    }

    /** Office of the person's area first, then their toko (revision 2026-10-08: attendance within 3 km of one of them). */
    fun attendancePlaces(partners: List<Partner>, userId: Long, role: SalesRole): List<Place> {
        val areaId = DemoDirectory.person(userId)?.areaId
        val area = areaId?.let { DemoDirectory.area(it) }
        val office = areaId?.let { DemoDirectory.office(it) }
        return listOfNotNull(office?.let { Place(it.lat, it.lng, area?.let { a -> "${a.district}, ${a.city}, ${a.province}" }, it.name) }) +
            storesOf(partners, userId, role).map { Place(it.lat, it.lng, it.address, it.name) }
    }

    /** Active SA, SR and TL of the directory (the people who clock in and visit). */
    fun employees(): List<DemoDirectory.Person> = DemoDirectory.people.filter { it.role in setOf("TL", "SR", "SA") && it.status == "ACTIVE" }

    /** First data of the device: history of every active SA/SR/TL (today starts empty). */
    fun initial(today: LocalDate, partners: List<Partner>): OpsData {
        val parts = employees().map { p -> initialOf(p.id, SalesRole.valueOf(p.role), p.areaId, today, partners) }
        return OpsData(attendance = parts.flatMap { it.attendance }, visits = parts.flatMap { it.visits })
    }

    /** [HISTORY_DAYS] of attendance and visits before [today] of one person. */
    private fun initialOf(userId: Long, role: SalesRole, areaId: Int?, today: LocalDate, partners: List<Partner>): OpsData {
        val stores = storesOf(partners, userId, role)
        val places = attendancePlaces(partners, userId, role)
        val area = areaId?.let { DemoDirectory.area(it) } ?: return OpsData()
        val zone = ActivityRules.zoneOf(userId)
        val attendance = mutableListOf<Attendance>()
        val visits = mutableListOf<Visit>()
        val days = (HISTORY_DAYS downTo 1).map { today.minusDays(it) }.filter(ActivityRules::isWorkday)
        days.forEachIndexed { i, d ->
            val rnd = seededRandom(userId * 1_000_003 + d.toEpochDay())
            val r = rnd.nextDouble()
            if (r < 0.05) return@forEachIndexed // absent: no attendance, no visit
            val late = r < 0.2
            val inAt = d.atTime(if (late) LocalTime.of(10, 1 + rnd.nextInt(59)) else LocalTime.of(9, 25 + rnd.nextInt(35)))
            val outAt = d.atTime(LocalTime.of(18 + rnd.nextInt(2), rnd.nextInt(60)))
            // Like the portal: 40% at a toko, otherwise at the office, within a few hundred metres.
            val ref = if (places.size > 1 && rnd.nextDouble() < 0.4) places[1 + rnd.nextInt(places.size - 1)] else places.first()
            val lat = round6(ref.lat + (rnd.nextDouble() - 0.5) * 0.006)
            val lng = round6(ref.lng + (rnd.nextDouble() - 0.5) * 0.006)
            val distance = ActivityRules.distanceMeters(lat, lng, ref.lat, ref.lng)
            attendance += Attendance(
                employeeId = userId,
                date = d.toString(),
                clockIn = Punch(inAt.atZone(zone).toInstant().toEpochMilli(), lat, lng, place = ref.name, distance = distance),
                clockOut = Punch(outAt.atZone(zone).toInstant().toEpochMilli(), lat, lng, place = ref.name, distance = distance),
            )
            // Like the portal: about 4 of 5 present days have one visit, 12:00–16:59 local, at one of their toko.
            if (stores.isEmpty() || rnd.nextDouble() > 0.82) return@forEachIndexed
            val s = stores[i % stores.size]
            val vIn = d.atTime(LocalTime.of(12 + rnd.nextInt(5), rnd.nextInt(60))).atZone(zone).toInstant().toEpochMilli()
            val vOut = vIn + (25 + rnd.nextInt(86)) * 60_000L
            val vLat = round6(s.lat + (rnd.nextDouble() - 0.5) * 0.004)
            val vLng = round6(s.lng + (rnd.nextDouble() - 0.5) * 0.004)
            val vDistance = ActivityRules.distanceMeters(vLat, vLng, s.lat, s.lng)
            visits += Visit(
                id = "V-$userId-$d-${s.id}", employeeId = userId, storeId = s.id, date = d.toString(), state = VisitState.DONE,
                checkIn = Punch(vIn, vLat, vLng, s.address, place = s.name, distance = vDistance),
                checkOut = Punch(vOut, vLat, vLng, s.address, place = s.name, distance = vDistance),
            )
        }
        return OpsData(attendance = attendance, visits = visits)
    }

    private fun round6(v: Double) = Math.round(v * 1e6) / 1e6
}
