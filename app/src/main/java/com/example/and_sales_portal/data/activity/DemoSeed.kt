package com.example.and_sales_portal.data.activity

import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.demo.seededRandom
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.PartnerStatus
import com.example.and_sales_portal.data.partner.Store
import java.time.LocalDate
import java.time.LocalTime

/**
 * Initial sample data, mirroring the web portal sample (`admin-sales-portal/src/api/db.js`): attendance (Monday–Saturday, clock in 07:25–07:59 on time, 08:01–08:59 late) and the
 * TL's visit plans (SA: its toko at 10:00; SR: up to 3 toko at 10:00, 13:00, 16:00; TL: one own toko at 14:00).
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

    /** Active SA, SR and TL of the directory (the people who clock in and visit). */
    fun employees(): List<DemoDirectory.Person> = DemoDirectory.people.filter { it.role in setOf("TL", "SR", "SA") && it.status == "ACTIVE" }

    /** First data of the device: history and today's plan of every active SA/SR/TL. */
    fun initial(today: LocalDate, partners: List<Partner>): OpsData {
        val parts = employees().map { p -> initialOf(p.id, SalesRole.valueOf(p.role), p.areaId, today, partners) }
        return OpsData(
            attendance = parts.flatMap { it.attendance },
            visits = parts.flatMap { it.visits },
            plannedDays = parts.flatMap { it.plannedDays }.toSet(),
        )
    }

    /** [HISTORY_DAYS] of attendance and visits before [today] plus today's plan of one person. */
    private fun initialOf(userId: Long, role: SalesRole, areaId: Int?, today: LocalDate, partners: List<Partner>): OpsData {
        val stores = storesOf(partners, userId, role)
        val area = areaId?.let { DemoDirectory.area(it) }
        val attendance = mutableListOf<Attendance>()
        val visits = mutableListOf<Visit>()
        val days = (HISTORY_DAYS downTo 0).map { today.minusDays(it) }.filter(ActivityRules::isWorkday)
        days.forEach { d ->
            val rnd = seededRandom(userId * 1_000_003 + d.toEpochDay())
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
                    employeeId = userId,
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
        return OpsData(attendance = attendance, visits = visits, plannedDays = days.map { OpsData.key(userId, it) }.toSet())
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
        return picks.map { (s, time) -> Visit(id = "V-$userId-$date-${s.id}", employeeId = userId, storeId = s.id, date = date.toString(), plannedTime = time, createdBy = tlId) }
    }

    private fun round6(v: Double) = Math.round(v * 1e6) / 1e6
}
