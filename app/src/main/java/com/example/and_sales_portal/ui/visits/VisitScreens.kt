package com.example.and_sales_portal.ui.visits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.Visit
import com.example.and_sales_portal.data.activity.VisitCheckInBlock
import com.example.and_sales_portal.data.activity.VisitWeek
import com.example.and_sales_portal.data.activity.WeekPartner
import com.example.and_sales_portal.data.activity.VisitStatus
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.Store
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadge
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeSize
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeType
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBox
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBoxType
import com.example.and_sales_portal.designsystem.components.inputs.AmarTextArea
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.attendance.distanceLabel
import com.example.and_sales_portal.ui.common.Fmt
import com.example.and_sales_portal.ui.common.InfoRow
import com.example.and_sales_portal.ui.common.MapPreview
import com.example.and_sales_portal.ui.common.PersistentAction
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.example.and_sales_portal.ui.common.SectionTitle
import com.example.and_sales_portal.designsystem.components.layout.AmarDivider
import com.example.and_sales_portal.ui.common.ListRow
import com.example.and_sales_portal.ui.common.ProgressSummary
import kotlinx.coroutines.delay
import java.time.LocalDate

/** A visit with its toko and partner, as shown on ATT-03 … ATT-05. */
data class VisitItem(val visit: Visit, val store: Store, val partner: Partner, val status: VisitStatus) {
    val pendingSync: Boolean get() = visit.checkOut?.synced == false
}

@Composable
fun VisitStatusBadge(status: VisitStatus) {
    val (label, type, solid) = when (status) {
        VisitStatus.ONGOING -> Triple(R.string.vis_status_ongoing, AmarBadgeType.Info, true)
        VisitStatus.DONE -> Triple(R.string.vis_status_done, AmarBadgeType.Success, false)
    }
    AmarBadge(stringResource(label), type = type, size = AmarBadgeSize.Small, solid = solid)
}

/** "Lengkap n/n" (Success) or "Belum lengkap n/m" (Warning): toko visited this week over toko to visit. */
@Composable
fun VisitWeekBadge(week: VisitWeek) = if (week.target == 0) {
    AmarBadge(stringResource(R.string.vis_week_no_store), type = AmarBadgeType.Info, size = AmarBadgeSize.Small)
} else if (week.complete) {
    AmarBadge(stringResource(R.string.vis_week_complete, week.visited, week.target), type = AmarBadgeType.Success, size = AmarBadgeSize.Small)
} else {
    AmarBadge(stringResource(R.string.vis_week_incomplete, week.visited, week.target), type = AmarBadgeType.Warning, size = AmarBadgeSize.Small)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Badges(item: VisitItem) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap), verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
        VisitStatusBadge(item.status)
        if (item.pendingSync) AmarBadge(stringResource(R.string.vis_pending_sync), type = AmarBadgeType.Warning, size = AmarBadgeSize.Small)
    }
}

/** What ATT-03 shows (revision 2026-10-08: no hourly plan). */
data class VisitListUiState(
    val date: String,
    val week: VisitWeek,
    val visitedToday: Boolean,
    /** Toko to visit this week (the user's kursi toko, held now) by partner, with their status. */
    val groups: List<WeekPartner>,
    val visits: List<VisitItem>,
    val online: Boolean,
    /** Why no check-in is possible right now (check in kerja first, before 12:00), if so. */
    val block: VisitCheckInBlock?,
)

/**
 * Spec: ATT-03 Kunjungan (PRD v3 D3; PRD Scope 2; UI refresh 2026-10-17): this week's progress (each toko once a week),
 * then the toko to visit as rows (not visited first) with `Check-In` (from 12:00 in the area zone, after check in kerja;
 * a toko visited this week waits until Monday, S12), then today's visits as rows that open ATT-04.
 */
@Composable
fun VisitListScreen(
    title: String,
    s: VisitListUiState,
    onBack: (() -> Unit)?,
    onOpen: (VisitItem) -> Unit,
    onCheckIn: (Store) -> Unit,
    onWorkCheckIn: () -> Unit,
) {
    val c = AmarTheme.colors
    ScreenScaffold(title, onBack) {
        if (!s.online) AmarInfoBox(stringResource(R.string.vis_offline_check_in), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning)
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.vis_week), Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.headingSm)
                VisitWeekBadge(s.week)
            }
            if (s.week.target > 0) ProgressSummary(stringResource(R.string.home_week_progress, s.week.visited, s.week.target), s.week.visited, s.week.target)
            Text(stringResource(if (s.visitedToday) R.string.vis_today_done else R.string.vis_today_not_yet), color = c.content.secondary, style = AmarTheme.typography.bodySm)
            Text(stringResource(R.string.vis_week_rule), color = c.content.tertiary, style = AmarTheme.typography.captionSm)
        }
        if (s.online) {
            when (s.block) {
                // SOP decision 2026-10-08: check in kerja first, then the visit check-in at the toko.
                VisitCheckInBlock.NotWorkCheckedIn -> AmarInfoBox(
                    stringResource(R.string.vis_need_work_check_in), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning,
                    buttonLabel = stringResource(R.string.att_clock_in), onButtonClick = onWorkCheckIn,
                )
                VisitCheckInBlock.BeforeStart -> AmarInfoBox(stringResource(R.string.vis_before_start, ActivityRules.ZONE_LABEL), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Info)
                VisitCheckInBlock.VisitedThisWeek, VisitCheckInBlock.StoreNotAssigned, null -> Unit
            }
        }
        SectionTitle(stringResource(R.string.vis_stores))
        val stores = s.groups.flatMap { g -> g.stores.map { g.partner to it } }.sortedBy { it.second.visited }
        if (stores.isEmpty()) {
            Text(stringResource(R.string.vis_no_stores), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        } else {
            AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
                Column {
                    stores.forEachIndexed { i, (p, ws) ->
                        if (i > 0) AmarDivider()
                        // S12: one visit per toko per week; check-in opens again on Monday.
                        // Q5: a toko that came after Monday 00:00 WIB counts from next week; it can be visited now.
                        val subtitle = when {
                            ws.visited -> stringResource(R.string.vis_row_visited, p.name)
                            !ws.counted -> stringResource(R.string.vis_row_next_week, p.name)
                            else -> p.name
                        }
                        ListRow(ws.store.name, subtitle) {
                            if (ws.visited) {
                                VisitedBadge(true, partner = false)
                            } else {
                                AmarButton(stringResource(R.string.vis_check_in_short), { onCheckIn(ws.store) }, type = AmarButtonType.Primary, size = AmarButtonSize.Small, enabled = s.online && s.block == null)
                            }
                        }
                    }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
            SectionTitle(stringResource(R.string.vis_today))
            Text(s.date, color = c.content.secondary, style = AmarTheme.typography.bodySm)
        }
        if (s.visits.isEmpty()) {
            Text(stringResource(R.string.vis_empty), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        } else {
            AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
                Column {
                    s.visits.forEachIndexed { i, item ->
                        if (i > 0) AmarDivider()
                        val time = item.visit.checkIn?.let { Fmt.timeLabel(it, item.visit.employeeId) }.orEmpty()
                        val subtitle = if (item.pendingSync) stringResource(R.string.vis_row_pending, item.partner.name) else item.partner.name
                        ListRow(stringResource(R.string.vis_row_title, time, item.store.name), subtitle, onClick = { onOpen(item) }) { VisitStatusBadge(item.status) }
                    }
                }
            }
        }
    }
}

/** Partner: "Sudah dikunjungi minggu ini" / "Belum dikunjungi"; toko: "Sudah dikunjungi" / "Belum dikunjungi". */
@Composable
fun VisitedBadge(visited: Boolean, partner: Boolean) = if (visited) {
    AmarBadge(stringResource(if (partner) R.string.vis_partner_visited else R.string.vis_store_visited), type = AmarBadgeType.Success, size = AmarBadgeSize.Small)
} else {
    AmarBadge(stringResource(R.string.vis_not_visited), type = AmarBadgeType.Warning, size = AmarBadgeSize.Small)
}

/**
 * Spec: ATT-04 Detail Kunjungan (PRD v3 D3): the running timer and `Check Out` during the visit, the summary after it,
 * and the toko.
 */
@Composable
fun VisitDetailScreen(
    item: VisitItem,
    now: () -> Long,
    onBack: () -> Unit,
    onOpenMaps: (Double, Double) -> Unit,
    onCheckOut: () -> Unit,
) {
    val c = AmarTheme.colors
    val v = item.visit
    val action: (@Composable () -> Unit)? = if (item.status == VisitStatus.ONGOING) { { PersistentAction(stringResource(R.string.vis_check_out), onCheckOut) } } else null
    ScreenScaffold(stringResource(R.string.vis_detail_title), onBack, action = action) {
        val checkIn = v.checkIn
        if (item.status == VisitStatus.ONGOING && checkIn != null) {
            var elapsed by remember { mutableLongStateOf(now() - checkIn.at) }
            LaunchedEffect(checkIn.at) {
                while (true) {
                    elapsed = now() - checkIn.at
                    delay(TICK_MILLIS)
                }
            }
            AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
                Text(stringResource(R.string.vis_ongoing_duration), color = c.content.secondary, style = AmarTheme.typography.captionSm)
                Text(Fmt.clock(elapsed), color = c.content.primary, style = AmarTheme.typography.headingLg)
                InfoRow(stringResource(R.string.vis_check_in_at), stringResource(R.string.att_time, Fmt.timeLabel(checkIn, v.employeeId)))
            }
        }
        val checkOut = v.checkOut
        if (item.status == VisitStatus.DONE && checkIn != null) {
            AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
                SectionTitle(stringResource(R.string.vis_summary))
                InfoRow(stringResource(R.string.vis_check_in_at), stringResource(R.string.att_time, Fmt.timeLabel(checkIn, v.employeeId)))
                if (checkOut != null) {
                    InfoRow(stringResource(R.string.vis_check_out_at), stringResource(R.string.att_time, Fmt.timeLabel(checkOut, v.employeeId)))
                    InfoRow(stringResource(R.string.vis_duration), duration(checkOut.at - checkIn.at))
                } else {
                    // S13: check-out is optional; the toko counts as visited from the check-in.
                    InfoRow(stringResource(R.string.vis_check_out_at), stringResource(R.string.vis_no_check_out))
                }
                v.note?.let { InfoRow(stringResource(R.string.vis_note), it) }
            }
        }
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
            Text(item.store.name, color = c.content.primary, style = AmarTheme.typography.headingSm)
            Badges(item)
            InfoRow(stringResource(R.string.vis_partner), item.partner.name)
            InfoRow(stringResource(R.string.vis_store_code), item.store.code ?: "-")
            checkIn?.distance?.let { InfoRow(stringResource(R.string.cap_distance), distanceLabel(it)) }
            InfoRow(stringResource(R.string.vis_pic), "${item.partner.picName} · +62 ${item.partner.picPhone}")
            Text(item.store.address, color = c.content.secondary, style = AmarTheme.typography.bodySm)
            MapPreview(item.store.lat, item.store.lng, { onOpenMaps(item.store.lat, item.store.lng) })
        }
    }
}

/**
 * Spec: ATT-05 Check Out Kunjungan (PRD v3 D3; PRD Scope 2 S13: optional, with an optional note, until 23:59 of the visit
 * day). Works offline: saved and sent later.
 */
@Composable
fun VisitCheckOutScreen(item: VisitItem, note: String, online: Boolean, now: Long, onNote: (String) -> Unit, onBack: () -> Unit, onCheckOut: () -> Unit) {
    val c = AmarTheme.colors
    ScreenScaffold(
        stringResource(R.string.vis_check_out_title),
        onBack,
        action = { PersistentAction(stringResource(R.string.vis_check_out), onCheckOut) },
    ) {
        Text(stringResource(R.string.vis_check_out_desc), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        if (!online) AmarInfoBox(stringResource(R.string.vis_offline_check_out), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning)
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
            Text(item.store.name, color = c.content.primary, style = AmarTheme.typography.headingSm)
            item.visit.checkIn?.let {
                InfoRow(stringResource(R.string.vis_check_in_at), stringResource(R.string.att_time, Fmt.timeLabel(it, item.visit.employeeId)))
                InfoRow(stringResource(R.string.vis_duration), duration(now - it.at))
            }
        }
        AmarTextArea(note, onNote, Modifier.fillMaxWidth(), label = stringResource(R.string.vis_note_label), placeholder = stringResource(R.string.vis_note_placeholder))
    }
}

@Composable
fun duration(millis: Long): String {
    val minutes = (millis / 60_000).coerceAtLeast(0)
    return stringResource(R.string.vis_duration_value, (minutes / 60).toInt(), (minutes % 60).toInt())
}

/** Visit with its toko, partner and status on [today] (area zone); `null` when the toko is no longer in the data. */
fun visitItem(v: Visit, store: Store?, partner: Partner?, today: LocalDate = LocalDate.now(ActivityRules.zoneOf(v.employeeId))): VisitItem? {
    if (store == null || partner == null) return null
    return VisitItem(v, store, partner, ActivityRules.visitStatus(v, today))
}

private const val TICK_MILLIS = 1_000L
