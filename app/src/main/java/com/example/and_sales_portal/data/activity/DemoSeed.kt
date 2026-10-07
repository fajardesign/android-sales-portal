package com.example.and_sales_portal.data.activity

import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoDirectory
import java.time.LocalDate
import java.time.LocalTime
import java.util.Random

/**
 * Initial sample data, mirroring the web portal sample (`admin-sales-portal/src/api/db.js`): the Active partners, their
 * toko and SA/SR assignments, attendance (Monday–Saturday, clock in 07:25–07:59 on time, 08:01–08:59 late) and the
 * TL's visit plans (SA: its toko at 10:00; SR: up to 3 toko at 10:00, 13:00, 16:00; TL: one own toko at 14:00).
 * Toko coordinates are fixed near the area centre (the portal draws them at random).
 */
object DemoSeed {

    /** Days of history generated before the first day the user opens the app. */
    private const val HISTORY_DAYS = 14L

    fun partners(): List<DemoPartner> = listOf(
        partner(139, "Jaya Abadi Cellular", 1, 6, "Rudi Hartono", "Jl. Basuki Rahmat", listOf(
            StoreSeed(null, listOf(15), -0.0055, 0.0054),
            StoreSeed("Mall Panakkukang", listOf(11), -0.0094, 0.0139),
            StoreSeed("Sudiang", listOf(11), 0.0127, 0.0193),
        )),
        partner(132, "PT Bintang Gadget Store", 1, 6, "Sri Wahyuni", "Jl. Tanjung Duren Raya", listOf(
            StoreSeed(null, listOf(16), 0.0075, -0.0058),
            StoreSeed("Tamalanrea", listOf(11), -0.0133, -0.0077),
        )),
        partner(138, "CV Sumber Rejeki Elektronik", 2, 7, "Ahmad Fauzi", "Jl. Gatot Subroto", listOf(
            StoreSeed(null, listOf(17), 0.0061, -0.0042),
            StoreSeed("Dago", listOf(12), -0.0087, 0.0071),
        )),
        partner(131, "Ponsel Kita", 2, 7, "Rudi Hartono", "Jl. Basuki Rahmat", listOf(StoreSeed(null, listOf(12), 0.0112, 0.0035))),
        partner(137, "Galaxy Phone Center", 5, 9, "Lina Marlina", "Jl. Ir. H. Juanda", listOf(
            StoreSeed(null, listOf(18), -0.0048, 0.0066),
            StoreSeed("Central Park", listOf(13), 0.0092, -0.0081),
        )),
        partner(136, "Medan Selular", 3, 8, "Hendra Gunawan", "Jl. Boulevard", listOf(StoreSeed(null, emptyList(), 0.0058, 0.0047))),
    )

    private class StoreSeed(val branch: String?, val assigned: List<Long>, val dLat: Double, val dLng: Double)

    private fun partner(n: Int, name: String, areaId: Int, tl: Long, pic: String, street: String, stores: List<StoreSeed>): DemoPartner {
        val area = requireNotNull(DemoDirectory.area(areaId))
        val reg = "REG2026-0$n"
        return DemoPartner(
            id = reg,
            name = name,
            areaId = areaId,
            ownerTlId = tl,
            picName = pic,
            picPhone = "85" + (300_000_000 + n * 104_729).toString().take(9),
            stores = stores.mapIndexed { i, s ->
                Store(
                    id = "$reg-${i + 1}",
                    code = "TK${reg.takeLast(4)}-${(i + 1).toString().padStart(2, '0')}",
                    name = if (s.branch == null) name else "$name ${s.branch}",
                    address = "$street No. ${10 + i * 7}, ${area.village}, ${area.district}, ${area.city}, ${area.province}",
                    lat = round6(area.lat + s.dLat),
                    lng = round6(area.lng + s.dLng),
                    assigned = s.assigned,
                )
            },
        )
    }

    /** Toko the user works at: SA/SR their assigned toko, TL the toko of the partners they own. */
    fun storesOf(partners: List<DemoPartner>, userId: Long, role: SalesRole): List<Store> = when (role) {
        SalesRole.TL -> partners.filter { it.ownerTlId == userId }.flatMap { it.stores }
        SalesRole.SA, SalesRole.SR -> partners.flatMap { p -> p.stores.filter { userId in it.assigned } }
    }

    /** First data of a user: partners, then [HISTORY_DAYS] of attendance and visits before [today] plus today's plan. */
    fun initial(userId: Long, role: SalesRole, areaId: Int?, today: LocalDate): DemoData {
        val partners = partners()
        val stores = storesOf(partners, userId, role)
        val area = areaId?.let { DemoDirectory.area(it) }
        val attendance = mutableListOf<Attendance>()
        val visits = mutableListOf<Visit>()
        val days = (HISTORY_DAYS downTo 0).map { today.minusDays(it) }.filter(ActivityRules::isWorkday)
        days.forEach { d ->
            val rnd = Random(userId * 1_000_003 + d.toEpochDay())
            val dayVisits = plan(userId, role, stores, d)
            if (d == today) {
                visits += dayVisits
                return@forEach
            }
            val r = rnd.nextDouble()
            val absent = r < 0.05
            if (!absent && area != null) {
                val late = r < 0.2
                val inAt = d.atTime(if (late) LocalTime.of(8, 1 + rnd.nextInt(59)) else LocalTime.of(7, 25 + rnd.nextInt(35)))
                val outAt = d.atTime(LocalTime.of(17 + rnd.nextInt(2), rnd.nextInt(60)))
                val lat = round6(area.lat + (rnd.nextDouble() - 0.5) * 0.03)
                val lng = round6(area.lng + (rnd.nextDouble() - 0.5) * 0.03)
                attendance += Attendance(
                    date = d.toString(),
                    clockIn = Punch(inAt.atZone(ActivityRules.ZONE).toInstant().toEpochMilli(), lat, lng),
                    clockOut = Punch(outAt.atZone(ActivityRules.ZONE).toInstant().toEpochMilli(), lat, lng),
                )
            }
            visits += dayVisits.map { v ->
                val s = stores.first { it.id == v.storeId }
                val vr = rnd.nextDouble()
                when {
                    absent || vr < 0.07 -> v
                    vr < 0.11 -> v.copy(state = VisitState.CANCELLED)
                    else -> {
                        val minutes = if (vr < 0.25) 16 + rnd.nextInt(55) else -10 + rnd.nextInt(25)
                        val inAt = ActivityRules.plannedAt(v).plusSeconds(minutes * 60L).toEpochMilli()
                        val outAt = inAt + (25 + rnd.nextInt(86)) * 60_000L
                        v.copy(state = VisitState.DONE, checkIn = Punch(inAt, s.lat, s.lng, s.address), checkOut = Punch(outAt, s.lat, s.lng, s.address))
                    }
                }
            }
        }
        return DemoData(partners = partners, attendance = attendance, visits = visits, plannedDays = days.map { it.toString() }.toSet())
    }

    /** The TL's visit plan of [userId] on [date] (same pattern as the web portal sample). */
    fun plan(userId: Long, role: SalesRole, stores: List<Store>, date: LocalDate): List<Visit> {
        if (stores.isEmpty() || !ActivityRules.isWorkday(date)) return emptyList()
        val tlId = when (role) {
            SalesRole.TL -> userId
            else -> DemoDirectory.person(userId)?.supervisorId ?: userId
        }
        val picks = when (role) {
            SalesRole.TL -> listOf(stores[(date.toEpochDay() % stores.size).toInt()] to "14:00")
            SalesRole.SA -> listOf(stores.first() to "10:00")
            SalesRole.SR -> stores.take(ActivityRules.MAX_VISITS_PER_DAY).zip(listOf("10:00", "13:00", "16:00"))
        }
        return picks.map { (s, time) -> Visit(id = "V-$userId-$date-${s.id}", storeId = s.id, date = date.toString(), plannedTime = time, createdBy = tlId) }
    }

    private fun round6(v: Double) = Math.round(v * 1e6) / 1e6
}
