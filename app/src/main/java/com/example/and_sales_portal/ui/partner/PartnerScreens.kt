package com.example.and_sales_portal.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.PartnerRepository
import com.example.and_sales_portal.data.partner.PartnerStatus
import com.example.and_sales_portal.data.partner.Store
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.actions.AmarChip
import com.example.and_sales_portal.designsystem.components.actions.AmarChipSupporting
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadge
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeSize
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeType
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBox
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBoxType
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.components.navigation.AmarAppBar
import com.example.and_sales_portal.designsystem.components.navigation.AmarAppBarType
import com.example.and_sales_portal.designsystem.components.navigation.AmarTab
import com.example.and_sales_portal.designsystem.components.navigation.AmarTabItem
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarFormTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.common.Fmt
import com.example.and_sales_portal.ui.common.InfoRow
import com.example.and_sales_portal.ui.common.MapPreview
import com.example.and_sales_portal.ui.common.PersistentAction
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.example.and_sales_portal.ui.common.SectionTitle
import java.time.Instant
import java.time.ZoneId

private val WIB = ZoneId.of("Asia/Jakarta")
private fun dateOf(millis: Long) = Fmt.shortDate(Instant.ofEpochMilli(millis).atZone(WIB).toLocalDate())
private fun dateTimeOf(millis: Long) = "${dateOf(millis)}, ${Fmt.time(millis)}"
private fun nameOf(id: Long) = DemoDirectory.person(id)?.fullName ?: "-"

enum class PartnerTab { Mine, Submissions }

/**
 * Spec: REG-01 Partner (PRD v3 C2–C4, D4). TL and SR: "Partner Saya" and "Pengajuan" tabs with "Daftarkan Partner";
 * SA: "Partner Saya" only, read-only.
 *
 * @param submissions `null` for SA (no tabs, no registration).
 */
@Composable
fun PartnerTabScreen(
    tab: PartnerTab,
    onTab: (PartnerTab) -> Unit,
    mine: List<Partner>,
    submissions: List<Partner>?,
    filter: PartnerStatus?,
    onFilter: (PartnerStatus?) -> Unit,
    onOpenPartner: (Partner) -> Unit,
    onOpenSubmission: (Partner) -> Unit,
    onRegister: () -> Unit,
) {
    val c = AmarTheme.colors
    Column(Modifier.fillMaxSize().background(c.background.canvas)) {
        AmarAppBar(title = stringResource(R.string.nav_partner), type = AmarAppBarType.Primary)
        if (submissions != null) {
            AmarTab {
                AmarTabItem(stringResource(R.string.ptn_tab_mine), tab == PartnerTab.Mine, { onTab(PartnerTab.Mine) }, icon = null)
                AmarTabItem(stringResource(R.string.ptn_tab_submissions), tab == PartnerTab.Submissions, { onTab(PartnerTab.Submissions) }, icon = null)
            }
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AmarFormTokens.PaddingHorizontal, vertical = AmarFormTokens.PaddingVertical),
            verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.ListGap),
        ) {
            if (submissions == null || tab == PartnerTab.Mine) {
                if (mine.isEmpty()) AmarInfoBox(stringResource(R.string.ptn_mine_empty), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
                mine.forEach { PartnerCard(it) { onOpenPartner(it) } }
            } else {
                StatusFilter(submissions, filter, onFilter)
                val shown = submissions.filter { filter == null || it.status == filter }
                if (shown.isEmpty()) AmarInfoBox(stringResource(R.string.ptn_submissions_empty), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
                shown.forEach { SubmissionCard(it) { onOpenSubmission(it) } }
            }
        }
        if (submissions != null) PersistentAction(stringResource(R.string.ptn_register), onRegister)
    }
}

/** Status chips with counts (PRD C3 "filter by status"); statuses without submissions are hidden. */
@Composable
private fun StatusFilter(all: List<Partner>, filter: PartnerStatus?, onFilter: (PartnerStatus?) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
        AmarChip(stringResource(R.string.ptn_filter_all), filter == null, { onFilter(null) }, supporting = AmarChipSupporting.Counter, counterValue = all.size.toString())
        PartnerStatus.ordered.forEach { s ->
            val n = all.count { it.status == s }
            if (n > 0) AmarChip(stringResource(statusLabel(s)), filter == s, { onFilter(s) }, supporting = AmarChipSupporting.Counter, counterValue = n.toString())
        }
    }
}

@Composable
private fun PartnerCard(p: Partner, onClick: () -> Unit) {
    val c = AmarTheme.colors
    val active = p.stores.filter { it.status == "ACTIVE" }
    val assigned = active.flatMap { it.assigned }.distinct().map(::nameOf)
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(p.name, Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.headingSm)
            AmarBadge(stringResource(R.string.ptn_store_count, active.size), type = AmarBadgeType.Info, size = AmarBadgeSize.Small)
        }
        Text("${p.id} · ${DemoDirectory.area(p.areaId)?.name ?: "-"}", color = c.content.secondary, style = AmarTheme.typography.bodySm)
        Text(
            if (assigned.isEmpty()) stringResource(R.string.ptn_unassigned) else stringResource(R.string.ptn_assigned, assigned.joinToString()),
            color = c.content.tertiary,
            style = AmarTheme.typography.bodySm,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SubmissionCard(p: Partner, onClick: () -> Unit) {
    val c = AmarTheme.colors
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(p.name, Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.headingSm)
            PartnerStatusBadge(p.status)
        }
        Text(p.id, color = c.content.secondary, style = AmarTheme.typography.bodySm)
        Text(
            stringResource(R.string.ptn_submitted_on, dateOf(p.submittedAt)) + " " + stringResource(R.string.ptn_submitted_by, nameOf(p.submittedBy)),
            color = c.content.tertiary,
            style = AmarTheme.typography.bodySm,
        )
        if (p.status == PartnerStatus.REVISION_REQUIRED) {
            AmarInfoBox(stringResource(R.string.ptn_revision_needed), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning)
        }
    }
}

/** Debug-only Admin actions available for a status (the web portal's transitions, PRD v3 A2). */
data class AdminAction(val label: Int, val enabled: Boolean = true, val run: () -> Unit)

/**
 * Spec: REG-10 Detail Pengajuan (PRD v3 C3): status, the Admin's notes per flagged item (Revision Required), the
 * reason (Rejected / Cancelled / Inactive), the data and documents, and the status timeline.
 */
@Composable
fun SubmissionDetailScreen(p: Partner, canFix: Boolean, onBack: () -> Unit, onFix: () -> Unit, adminActions: List<AdminAction>?) {
    val c = AmarTheme.colors
    val action: (@Composable () -> Unit)? = if (canFix && p.status == PartnerStatus.REVISION_REQUIRED) {
        { PersistentAction(stringResource(R.string.ptn_fix), onFix) }
    } else null
    ScreenScaffold(stringResource(R.string.ptn_detail_title), onBack, action = action) {
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.name, Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.headingSm)
                PartnerStatusBadge(p.status)
            }
            InfoRow(stringResource(R.string.ptn_reg_number), p.id)
            InfoRow(stringResource(R.string.ptn_submitter), nameOf(p.submittedBy))
            InfoRow(stringResource(R.string.ptn_submitted_at), dateTimeOf(p.submittedAt))
            InfoRow(stringResource(R.string.ptn_updated_at), dateTimeOf(p.statusUpdatedAt))
        }
        val revision = p.revision
        if (p.status == PartnerStatus.REVISION_REQUIRED && revision != null) {
            AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
                SectionTitle(stringResource(R.string.ptn_revision_title))
                Text(stringResource(R.string.ptn_revision_desc), color = c.content.secondary, style = AmarTheme.typography.bodySm)
                revision.general?.let { AmarInfoBox(it, Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning) }
                revision.items.forEach { item ->
                    Column(verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
                        Text(item.label, color = c.content.primary, style = AmarTheme.typography.labelMdSemibold)
                        Text(item.note, color = c.feedback.error.text, style = AmarTheme.typography.bodySm)
                    }
                }
            }
        }
        if (p.status.isFinal && p.statusReason != null) {
            AmarInfoBox(p.statusReason, Modifier.fillMaxWidth(), type = AmarInfoBoxType.Error, label = stringResource(R.string.ptn_reason))
        }
        PartnerSections(p, showVerification = true)
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
            SectionTitle(stringResource(R.string.ptn_history))
            p.history.asReversed().forEach { e ->
                Column(verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(dateTimeOf(e.at), Modifier.weight(1f), c.content.secondary, style = AmarTheme.typography.bodySm)
                        PartnerStatusBadge(e.to)
                    }
                    Text(stringResource(R.string.ptn_history_by, PartnerRepository.actorName(e.by)), color = c.content.secondary, style = AmarTheme.typography.bodySm)
                    e.reason?.let { Text(it, color = c.content.primary, style = AmarTheme.typography.bodySm) }
                }
            }
        }
        if (!adminActions.isNullOrEmpty()) AdminSimulation(adminActions)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminSimulation(actions: List<AdminAction>) {
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
        SectionTitle(stringResource(R.string.sim_admin_title))
        Text(stringResource(R.string.sim_admin_desc), color = AmarTheme.colors.content.secondary, style = AmarTheme.typography.bodySm)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap), verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
            actions.forEach { AmarButton(stringResource(it.label), it.run, type = AmarButtonType.Secondary, size = AmarButtonSize.Small, enabled = it.enabled) }
        }
    }
}

/**
 * Spec: PRT-01 Detail Partner (PRD v3 C4, D4): read-only profile, toko with address, map link and assigned SA/SR.
 * "Tambah Toko" for TL and SR on Active partners.
 */
@Composable
fun PartnerDetailScreen(
    p: Partner,
    canAddStore: Boolean,
    onBack: () -> Unit,
    onOpenMaps: (Double, Double) -> Unit,
    onAddStore: () -> Unit,
    /** FR-PA-013: TL/SR without a kursi in this partner see "Tambah Toko" disabled with the reason. */
    addStoreBlocked: Boolean = false,
) {
    val c = AmarTheme.colors
    val action: (@Composable () -> Unit)? = if (canAddStore && p.status == PartnerStatus.ACTIVE) {
        { PersistentAction(stringResource(R.string.prt_add_store), onAddStore, enabled = !addStoreBlocked) }
    } else null
    ScreenScaffold(stringResource(R.string.prt_detail_title), onBack, action = action) {
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.name, Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.headingSm)
                PartnerStatusBadge(p.status)
            }
            InfoRow(stringResource(R.string.ptn_reg_number), p.id)
            InfoRow(stringResource(R.string.ptn_area), DemoDirectory.area(p.areaId)?.name ?: "-")
            InfoRow(stringResource(R.string.reg_pic_name), p.picName)
            InfoRow(stringResource(R.string.reg_phone), "+62 ${p.picPhone}")
            InfoRow(stringResource(R.string.reg_pic_email), p.picEmail)
            InfoRow(stringResource(R.string.reg_address), "${p.address}, ${p.village}, ${p.district}, ${p.regency}")
        }
        if (canAddStore && addStoreBlocked && p.status == PartnerStatus.ACTIVE) {
            AmarInfoBox(stringResource(R.string.prt_add_store_blocked), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Info)
        }
        SectionTitle(stringResource(R.string.prt_stores, p.stores.size))
        p.stores.forEach { StoreCard(it, onOpenMaps) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StoreCard(s: Store, onOpenMaps: (Double, Double) -> Unit) {
    val c = AmarTheme.colors
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
        Text(s.name, color = c.content.primary, style = AmarTheme.typography.labelMdSemibold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
            s.code?.let { AmarBadge(it, type = AmarBadgeType.Info, size = AmarBadgeSize.Small) }
            if (s.primary) AmarBadge(stringResource(R.string.prt_primary), type = AmarBadgeType.Success, size = AmarBadgeSize.Small)
            if (s.status == Store.WAITING_REFERRAL) AmarBadge(stringResource(R.string.prt_store_waiting_referral), type = AmarBadgeType.Warning, size = AmarBadgeSize.Small)
        }
        Text(s.address, color = c.content.secondary, style = AmarTheme.typography.bodySm)
        InfoRow(stringResource(R.string.reg_revenue), rupiah(s.omzet))
        // Kode Referral per toko as text only (Designer Q2, 2026-10-19: no QR graphic; no copy pattern in the app yet), kursi TL ("Belum ada TL") and kursi SA/SR.
        InfoRow(stringResource(R.string.reg_referral), s.referralCode ?: stringResource(R.string.prt_referral_empty))
        InfoRow(stringResource(R.string.account_role_tl), s.tlId?.let(::nameOf) ?: stringResource(R.string.prt_no_tl))
        InfoRow(stringResource(R.string.prt_assigned), s.assigned.map(::nameOf).ifEmpty { listOf(stringResource(R.string.ptn_unassigned)) }.joinToString())
        if (s.status == Store.WAITING_REFERRAL) AmarInfoBox(stringResource(R.string.prt_store_waiting_referral_info), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning)
        MapPreview(s.lat, s.lng, { onOpenMaps(s.lat, s.lng) })
    }
}
