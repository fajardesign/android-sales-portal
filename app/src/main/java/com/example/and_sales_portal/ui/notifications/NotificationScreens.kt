package com.example.and_sales_portal.ui.notifications

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.and_sales_portal.ui.partner.PartnerDetailFragment
import com.example.and_sales_portal.ui.partner.SubmissionDetailFragment
import java.time.Instant

/** "Rab, 7 Okt 2026, 10:30 WIB". */
fun eventTime(at: Long): String =
    "${Fmt.shortDate(Instant.ofEpochMilli(at).atZone(ActivityRules.ZONE).toLocalDate())}, ${Fmt.timeLabel(at)}"

fun eventTime(e: AssignmentEvent): String = eventTime(e.at)

/**
 * Assignment messages (1.5 FR-MP-009, 1.7 FR-006). SA/SR: "Anda ditugaskan ke {toko} ({partner})." / "Penugasan Anda di
 * {toko} ({partner}) dicabut ({alasan})." TL: "Anda ditugaskan ke {toko} ({partner}) oleh {APL}." / "Anda dilepas dari
 * {toko} ({partner}). Penugasan {SA/SR} di toko ini juga dicabut."
 */
@Composable
fun notificationText(e: AssignmentEvent): String = when {
    e.seat == AssignmentEvent.TL && e.action == AssignmentAction.ASSIGN -> stringResource(R.string.notif_tl_assigned, e.storeName, e.partnerName, PartnerRepository.actorName(e.by))
    e.seat == AssignmentEvent.TL && e.withSales.isNotEmpty() -> stringResource(R.string.notif_tl_released_sales, e.storeName, e.partnerName, e.withSales.joinToString())
    e.seat == AssignmentEvent.TL -> stringResource(R.string.notif_tl_released, e.storeName, e.partnerName)
    e.action == AssignmentAction.ASSIGN -> stringResource(R.string.notif_assigned, e.storeName, e.partnerName)
    else -> stringResource(R.string.notif_sales_released, e.storeName, e.partnerName, e.source ?: PartnerRepository.actorName(e.by))
}

/** Assignment or revision message (DD review point 2: "Pengajuan <partner> perlu diperbaiki."). */
@Composable
fun notificationText(n: AppNotification): String =
    n.event?.let { notificationText(it) }
        ?: n.revision?.let { stringResource(R.string.notif_revision, it.partnerName) }
        ?: n.storeCancelled?.let { stringResource(R.string.notif_store_cancelled, it.storeName, it.partnerName, it.reason) }.orEmpty()

/**
 * Spec: Notifikasi (FR-MP-004; PRD 1.1 §1.7 FR-006, AC-014, decision 2026-10-20): the last 30 days, newest first; an
 * unread one shows "Baru". Opening the list marks them all read (the badges stay until the list is left). Tapping an
 * assignment or a cancelled request opens the partner (PRT-01) at that toko; a revision request opens REG-10.
 */
@Composable
fun NotificationsScreen(items: List<AppNotification>, onBack: () -> Unit, onOpen: (AppNotification) -> Unit = {}) {
    val c = AmarTheme.colors
    ScreenScaffold(stringResource(R.string.notif_title), onBack) {
        if (items.isEmpty()) AmarInfoBox(stringResource(R.string.notif_empty), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
        Text(stringResource(R.string.notif_retention), color = c.content.tertiary, style = AmarTheme.typography.captionSm)
        items.forEach { n ->
            AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border, onClick = { onOpen(n) }) {
                if (!n.read) AmarBadge(stringResource(R.string.notif_new_badge), type = AmarBadgeType.Info, size = AmarBadgeSize.Small)
                Text(notificationText(n), color = c.content.primary, style = AmarTheme.typography.bodyMd)
                Text(eventTime(n.at), color = c.content.tertiary, style = AmarTheme.typography.bodySm)
            }
        }
    }
}

/** Spec: Notifikasi. Opening the list marks every notification read (AC-014); the list keeps the "Baru" badges until left. */
class NotificationsFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val s = session ?: return
        val items = remember { container.partners.notificationsOf(s.user.id) }
        LaunchedEffect(Unit) { container.partners.markNotificationsRead(s.user.id) }
        NotificationsScreen(items, ::back) { n ->
            when {
                n.revision != null -> navigate(R.id.submission_detail, SubmissionDetailFragment.args(n.revision.partnerId))
                n.event != null -> navigate(R.id.partner_detail, PartnerDetailFragment.args(n.event.partnerId, n.event.storeId))
                n.storeCancelled != null -> navigate(R.id.partner_detail, PartnerDetailFragment.args(n.storeCancelled.partnerId, n.storeCancelled.storeId))
            }
        }
    }
}
