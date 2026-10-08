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
import com.example.and_sales_portal.data.activity.VisitOutcome
import com.example.and_sales_portal.data.activity.VisitStatus
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.Store
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
import com.example.and_sales_portal.ui.common.Fmt
import com.example.and_sales_portal.ui.common.InfoRow
import com.example.and_sales_portal.ui.common.MapPreview
import com.example.and_sales_portal.ui.common.PersistentAction
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.example.and_sales_portal.ui.common.SectionTitle
import kotlinx.coroutines.delay

/** A visit with its toko and partner, as shown on ATT-03 … ATT-05. */
data class VisitItem(val visit: Visit, val store: Store, val partner: Partner, val status: VisitStatus, val outcome: VisitOutcome?) {
    val pendingSync: Boolean get() = visit.checkOut?.synced == false
}

@Composable
fun VisitStatusBadge(status: VisitStatus) {
    val (label, type, solid) = when (status) {
        VisitStatus.NOT_STARTED -> Triple(R.string.vis_status_not_started, AmarBadgeType.Info, false)
        VisitStatus.ONGOING -> Triple(R.string.vis_status_ongoing, AmarBadgeType.Info, true)
        VisitStatus.DONE -> Triple(R.string.vis_status_done, AmarBadgeType.Success, false)
        VisitStatus.MISSED -> Triple(R.string.vis_status_missed, AmarBadgeType.Error, false)
        VisitStatus.CANCELLED -> Triple(R.string.vis_status_cancelled, AmarBadgeType.Warning, false)
    }
    AmarBadge(stringResource(label), type = type, size = AmarBadgeSize.Small, solid = solid)
}

@Composable
fun VisitOutcomeBadge(outcome: VisitOutcome) = when (outcome) {
    VisitOutcome.ON_TIME -> AmarBadge(stringResource(R.string.vis_outcome_on_time), type = AmarBadgeType.Success, size = AmarBadgeSize.Small)
    VisitOutcome.LATE -> AmarBadge(stringResource(R.string.vis_outcome_late), type = AmarBadgeType.Warning, size = AmarBadgeSize.Small)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Badges(item: VisitItem) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap), verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
        VisitStatusBadge(item.status)
        item.outcome?.let { VisitOutcomeBadge(it) }
        if (item.pendingSync) AmarBadge(stringResource(R.string.vis_pending_sync), type = AmarBadgeType.Warning, size = AmarBadgeSize.Small)
    }
}

/**
 * Spec: ATT-03 Jadwal Kunjungan hari ini (PRD v3 D3: toko, partner, planned time, status). SA/SR: tab Kunjungan
 * (no back); TL: Aktivitas › Kunjungan Saya.
 */
@Composable
fun VisitListScreen(title: String, date: String, items: List<VisitItem>, online: Boolean, onBack: (() -> Unit)?, onOpen: (VisitItem) -> Unit) {
    val c = AmarTheme.colors
    ScreenScaffold(title, onBack) {
        if (!online) AmarInfoBox(stringResource(R.string.vis_offline_check_in), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning)
        Column(verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
            SectionTitle(stringResource(R.string.vis_today))
            Text(date, color = c.content.secondary, style = AmarTheme.typography.bodySm)
        }
        if (items.isEmpty()) {
            AmarInfoBox(stringResource(R.string.vis_empty), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
        }
        items.forEach { item ->
            AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border, onClick = { onOpen(item) }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.vis_planned, Fmt.withZone(item.visit.plannedTime)), Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.headingSm)
                }
                Column(verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
                    Text(item.store.name, color = c.content.primary, style = AmarTheme.typography.labelMdSemibold)
                    Text(item.partner.name, color = c.content.secondary, style = AmarTheme.typography.bodySm)
                    Text(item.store.address, color = c.content.tertiary, style = AmarTheme.typography.bodySm, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Badges(item)
            }
        }
    }
}

/**
 * Spec: ATT-04 Detail Kunjungan (PRD v3 D3): toko, partner, planned time and status; "Check-In Sekarang" before the
 * visit (only after today's check in kerja, SOP decision 2026-10-08), the running timer during it, the summary after it.
 */
@Composable
fun VisitDetailScreen(
    item: VisitItem,
    online: Boolean,
    now: () -> Long,
    onBack: () -> Unit,
    onOpenMaps: (Double, Double) -> Unit,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
    workCheckedIn: Boolean = true,
    onWorkCheckIn: () -> Unit = {},
) {
    val c = AmarTheme.colors
    val v = item.visit
    val action: (@Composable () -> Unit)? = when (item.status) {
        VisitStatus.NOT_STARTED -> { { PersistentAction(stringResource(R.string.vis_check_in_now), onCheckIn, enabled = online && workCheckedIn) } }
        VisitStatus.ONGOING -> { { PersistentAction(stringResource(R.string.vis_check_out), onCheckOut) } }
        else -> null
    }
    ScreenScaffold(stringResource(R.string.vis_detail_title), onBack, action = action) {
        when (item.status) {
            VisitStatus.NOT_STARTED -> when {
                !online -> AmarInfoBox(stringResource(R.string.vis_offline_check_in), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning)
                // Decision 2026-10-08 (SOP): check in kerja first, then the visit check-in at the toko.
                !workCheckedIn -> AmarInfoBox(
                    stringResource(R.string.vis_need_work_check_in), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning,
                    buttonLabel = stringResource(R.string.att_clock_in), onButtonClick = onWorkCheckIn,
                )
                else -> Unit
            }
            VisitStatus.MISSED -> AmarInfoBox(stringResource(R.string.vis_missed_info, ActivityRules.ZONE_LABEL), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Error)
            VisitStatus.CANCELLED -> AmarInfoBox(v.cancelReason ?: stringResource(R.string.vis_cancelled_info), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning)
            else -> Unit
        }
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
                InfoRow(stringResource(R.string.vis_check_in_at), stringResource(R.string.att_time, Fmt.timeLabel(checkIn.at)))
            }
        }
        val checkOut = v.checkOut
        if (item.status == VisitStatus.DONE && checkIn != null && checkOut != null) {
            AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
                SectionTitle(stringResource(R.string.vis_summary))
                InfoRow(stringResource(R.string.vis_check_in_at), stringResource(R.string.att_time, Fmt.timeLabel(checkIn.at)))
                InfoRow(stringResource(R.string.vis_check_out_at), stringResource(R.string.att_time, Fmt.timeLabel(checkOut.at)))
                InfoRow(stringResource(R.string.vis_duration), duration(checkOut.at - checkIn.at))
                v.note?.let { InfoRow(stringResource(R.string.vis_note), it) }
            }
        }
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
            Text(item.store.name, color = c.content.primary, style = AmarTheme.typography.headingSm)
            Badges(item)
            InfoRow(stringResource(R.string.vis_partner), item.partner.name)
            InfoRow(stringResource(R.string.vis_store_code), item.store.code ?: "-")
            InfoRow(stringResource(R.string.vis_planned_time), stringResource(R.string.vis_planned, Fmt.withZone(v.plannedTime)))
            InfoRow(stringResource(R.string.vis_pic), "${item.partner.picName} · +62 ${item.partner.picPhone}")
            Text(item.store.address, color = c.content.secondary, style = AmarTheme.typography.bodySm)
            MapPreview(item.store.lat, item.store.lng, { onOpenMaps(item.store.lat, item.store.lng) })
        }
    }
}

/**
 * Spec: ATT-05 Check Out Kunjungan (PRD v3 D3: optional note → summary). Works offline: saved and sent later.
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
                InfoRow(stringResource(R.string.vis_check_in_at), stringResource(R.string.att_time, Fmt.timeLabel(it.at)))
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

/** Visit with its toko, partner, status and outcome; `null` when the toko is no longer in the data. */
fun visitItem(v: Visit, store: Store?, partner: Partner?, today: java.time.LocalDate): VisitItem? {
    if (store == null || partner == null) return null
    return VisitItem(v, store, partner, ActivityRules.visitStatus(v, today), ActivityRules.visitOutcome(v))
}

private const val TICK_MILLIS = 1_000L
