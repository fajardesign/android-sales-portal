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
 * check-ins (no hourly plan; each toko once a week, 12:00–16:59 local, near the toko; PRD alignment 2026-10-09). The
 * random order is Android's own; only the rules match the portal.
 */
object DemoSeed {

    /** Days of history generated before the first day the user opens the app. */
    private const val HISTORY_DAYS = 14L

    /** Toko the user works at: SA/SR their assigned toko, TL the toko they hold (PRD Scope 1 v2: TL per toko). */
    fun storesOf(partners: List<Partner>, userId: Long, role: SalesRole): List<Store> {
        val active = partners.filter { it.status == PartnerStatus.ACTIVE }
        val stores = when (role) {
            SalesRole.TL -> active.flatMap { p -> p.stores.filter { it.tlId == userId } }
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
        // Q6 (decision 2026-10-19): every day is a workday. Like the web sample, Sundays use a fixed pattern (some check
        // ins at the office, the rest Absen) so the random Monday–Saturday days stay the same.
        val days = (HISTORY_DAYS downTo 1).map { today.minusDays(it) }
        var visitSeq = 0
        days.forEach { d ->
            if (d.dayOfWeek == java.time.DayOfWeek.SUNDAY) {
                val office = places.first()
                if ((userId + d.dayOfMonth) % 4 == 0L) {
                    val z = zone
                    attendance += Attendance(
                        userId, d.toString(),
                        clockIn = Punch(d.atTime(9, 15).atZone(z).toInstant().toEpochMilli(), office.lat, office.lng, place = office.name, distance = 0, zone = z.id),
                        clockOut = Punch(d.atTime(15, 30).atZone(z).toInstant().toEpochMilli(), office.lat, office.lng, place = office.name, distance = 0, zone = z.id),
                    )
                }
                return@forEach
            }
            val rnd = seededRandom(userId * 1_000_003 + d.toEpochDay())
            val r = rnd.nextDouble()
            if (r < 0.05) return@forEach // absent: no attendance, no visit
            val late = r < 0.2
            val inAt = d.atTime(if (late) LocalTime.of(10, 1 + rnd.nextInt(59)) else LocalTime.of(9, 25 + rnd.nextInt(35)))
            val outAt = d.atTime(LocalTime.of(18 + rnd.nextInt(2), rnd.nextInt(60)))
            // Like the portal: 40% at a toko, otherwise at the office, within a few hundred metres.
            val ref = if (places.size > 1 && rnd.nextDouble() < 0.4) places[1 + rnd.nextInt(places.size - 1)] else places.first()
            val lat = round6(ref.lat + (rnd.nextDouble() - 0.5) * 0.006)
            val lng = round6(ref.lng + (rnd.nextDouble() - 0.5) * 0.006)
            val distance = ActivityRules.distanceMeters(lat, lng, ref.lat, ref.lng)
            // Like the web sample: now and then a forgotten check out, so the day is Absen (S13, decision 2026-10-20).
            val noOut = (userId * 7 + d.dayOfMonth) % 29 == 0L
            attendance += Attendance(
                employeeId = userId,
                date = d.toString(),
                clockIn = Punch(inAt.atZone(zone).toInstant().toEpochMilli(), lat, lng, place = ref.name, distance = distance, zone = zone.id),
                clockOut = if (noOut) null else Punch(outAt.atZone(zone).toInstant().toEpochMilli(), lat, lng, place = ref.name, distance = distance, zone = zone.id),
            )
            // PRD alignment 2026-10-09: each toko once a week, on its own weekday (a toko is missed about 1 week in 7),
            // 12:00–16:59 local, near the toko.
            stores.filterIndexed { j, _ -> (j + userId.toInt()) % 6 == d.dayOfWeek.value - 1 }.forEach { s ->
                if (rnd.nextDouble() > 0.86) return@forEach
                val vIn = d.atTime(LocalTime.of(12 + rnd.nextInt(5), rnd.nextInt(60))).atZone(zone).toInstant().toEpochMilli()
                val vOut = vIn + (25 + rnd.nextInt(86)) * 60_000L
                val vLat = round6(s.lat + (rnd.nextDouble() - 0.5) * 0.004)
                val vLng = round6(s.lng + (rnd.nextDouble() - 0.5) * 0.004)
                val vDistance = ActivityRules.distanceMeters(vLat, vLng, s.lat, s.lng)
                // Check-out is optional (S13): some sample visits have none and still count.
                val noCheckOut = visitSeq++ % 7 == 0
                visits += Visit(
                    id = "V-$userId-$d-${s.id}", employeeId = userId, storeId = s.id, date = d.toString(),
                    state = if (noCheckOut) VisitState.IN_PROGRESS else VisitState.DONE,
                    checkIn = Punch(vIn, vLat, vLng, s.address, place = s.name, distance = vDistance, zone = zone.id),
                    checkOut = if (noCheckOut) null else Punch(vOut, vLat, vLng, s.address, place = s.name, distance = vDistance, zone = zone.id),
                )
            }
        }
        return OpsData(attendance = withCorrections(userId, today, attendance), visits = visits)
    }

    /**
     * Koreksi absensi oleh TL, same as the web sample (`admin-sales-portal/src/api/db.js`): SR Siti Rahmawati (11), last
     * week, one Absen day corrected to Tepat Waktu and one Terlambat day corrected to Tepat Waktu by her TL, with reasons.
     */
    private fun withCorrections(userId: Long, today: LocalDate, list: List<Attendance>): List<Attendance> {
        if (userId != SR_SITI) return list
        val tl = DemoDirectory.person(userId)?.supervisorId ?: return list
        val lastMon = ActivityRules.weekStart(today).minusWeeks(1)
        val week = (0L..6L).map { lastMon.plusDays(it) }
        val office = DemoDirectory.person(userId)?.areaId?.let { DemoDirectory.office(it) } ?: return list
        val z = ActivityRules.zoneOf(userId)
        val wib = ActivityRules.LOCK_ZONE
        val out = list.toMutableList()
        week.firstOrNull { d -> out.none { it.date == d.toString() && it.hasCheckIn } }?.let { d ->
            val inAt = d.atTime(8, 50).atZone(z).toInstant().toEpochMilli()
            out.removeAll { it.date == d.toString() }
            out += Attendance(
                userId, d.toString(),
                clockOut = Punch(d.atTime(17, 10).atZone(z).toInstant().toEpochMilli(), office.lat, office.lng, place = office.name, distance = 0, zone = z.id),
                statusOverride = AttendanceStatus.ON_TIME, clockInOverride = inAt,
                corrections = listOf(AttendanceCorrection(d.plusDays(2).atTime(9, 12).atZone(wib).toInstant().toEpochMilli(), tl, AttendanceStatus.ABSENT, null, AttendanceStatus.ON_TIME, inAt, REASON_ABSENT)),
            )
        }
        out.indexOfFirst { a -> LocalDate.parse(a.date) in week && a.statusOverride == null && ActivityRules.attendanceStatus(a, LocalDate.parse(a.date), today) == AttendanceStatus.LATE }.takeIf { it >= 0 }?.let { i ->
            val a = out[i]
            val d = LocalDate.parse(a.date)
            val inAt = d.atTime(9, 40).atZone(z).toInstant().toEpochMilli()
            out[i] = a.copy(
                statusOverride = AttendanceStatus.ON_TIME, clockInOverride = inAt,
                corrections = listOf(AttendanceCorrection(d.plusDays(1).atTime(8, 30).atZone(wib).toInstant().toEpochMilli(), tl, AttendanceStatus.LATE, a.clockIn?.at, AttendanceStatus.ON_TIME, inAt, REASON_LATE)),
            )
        }
        return out.sortedBy { it.date }
    }

    private const val SR_SITI = 11L
    const val REASON_ABSENT = "Ponsel rusak, hadir di kantor sesuai daftar hadir manual"
    const val REASON_LATE = "Gangguan sinyal saat check in, sudah di lokasi sebelum 10:00"

    private fun round6(v: Double) = Math.round(v * 1e6) / 1e6
}
