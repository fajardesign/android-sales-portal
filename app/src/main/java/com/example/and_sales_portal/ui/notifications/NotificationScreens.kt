package com.example.and_sales_portal.ui.notifications

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.partner.AppNotification
import com.example.and_sales_portal.data.partner.AssignmentAction
import com.example.and_sales_portal.data.partner.AssignmentEvent
import com.example.and_sales_portal.data.partner.PartnerRepository
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadge
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeSize
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeType
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBox
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBoxType
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.common.ComposeFragment
import com.example.and_sales_portal.ui.common.Fmt
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.example.and_sales_portal.ui.partner.SubmissionDetailFragment
import java.time.Instant

/** "Rab, 7 Okt 2026, 10:30 WIB". */
fun eventTime(at: Long): String =
    "${Fmt.shortDate(Instant.ofEpochMilli(at).atZone(ActivityRules.ZONE).toLocalDate())}, ${Fmt.timeLabel(at)}"

fun eventTime(e: AssignmentEvent): String = eventTime(e.at)

/** "Anda ditugaskan ke <toko> (<partner>) oleh <TL>." and the other assignment messages (FR-MP-004). */
@Composable
fun notificationText(e: AssignmentEvent): String = when {
    e.action == AssignmentAction.ASSIGN -> stringResource(R.string.notif_assigned, e.storeName, e.partnerName, PartnerRepository.actorName(e.by))
    e.by == AssignmentEvent.SYSTEM && e.reason == AssignmentEvent.ACCOUNT_DISABLED -> stringResource(R.string.notif_released_disabled, e.storeName, e.partnerName)
    e.by == AssignmentEvent.SYSTEM && e.reason == AssignmentEvent.LEADER_CHANGED -> stringResource(R.string.notif_released_leader, e.storeName, e.partnerName, e.source.orEmpty())
    e.by == AssignmentEvent.SYSTEM -> stringResource(R.string.notif_released_system, e.storeName, e.partnerName)
    else -> stringResource(R.string.notif_released, e.storeName, e.partnerName, PartnerRepository.actorName(e.by))
}

/** Assignment or revision message (DD review point 2: "Pengajuan <partner> perlu diperbaiki."). */
@Composable
fun notificationText(n: AppNotification): String =
    n.event?.let { notificationText(it) } ?: n.revision?.let { stringResource(R.string.notif_revision, it.partnerName) }.orEmpty()

/**
 * Spec: Notifikasi (FR-MP-004, decision 2026-10-09: in-app inbox; DD review point 2: revision requests to the TL/SR),
 * newest first. A revision notification opens the submission (REG-10).
 */
@Composable
fun NotificationsScreen(items: List<AppNotification>, onBack: () -> Unit, onOpenSubmission: (String) -> Unit = {}) {
    val c = AmarTheme.colors
    ScreenScaffold(stringResource(R.string.notif_title), onBack) {
        if (items.isEmpty()) AmarInfoBox(stringResource(R.string.notif_empty), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
        items.forEach { n ->
            val open = n.revision?.let { r -> { onOpenSubmission(r.partnerId) } }
            AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border, onClick = open) {
                if (!n.read) AmarBadge(stringResource(R.string.notif_new_badge), type = AmarBadgeType.Info, size = AmarBadgeSize.Small)
                Text(notificationText(n), color = c.content.primary, style = AmarTheme.typography.bodyMd)
                Text(eventTime(n.at), color = c.content.tertiary, style = AmarTheme.typography.bodySm)
            }
        }
    }
}

/** Spec: Notifikasi. Opening the inbox marks the notifications as read (the list keeps the "Baru" badge until left). */
class NotificationsFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val s = session ?: return
        val data by container.partners.assignments.collectAsState()
        val items = remember { container.partners.notificationsOf(s.user.id) }
        LaunchedEffect(data) { if (container.partners.notificationsOf(s.user.id).any { !it.read }) container.partners.markNotificationsRead(s.user.id) }
        NotificationsScreen(items, ::back) { id -> navigate(R.id.submission_detail, SubmissionDetailFragment.args(id)) }
    }
}
