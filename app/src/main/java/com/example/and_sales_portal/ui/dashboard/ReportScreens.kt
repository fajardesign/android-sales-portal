package com.example.and_sales_portal.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.Store
import com.example.and_sales_portal.data.sales.Incentive
import com.example.and_sales_portal.data.sales.Loan
import com.example.and_sales_portal.data.sales.LoanStatus
import com.example.and_sales_portal.data.sales.SalesSummary
import com.example.and_sales_portal.data.team.Productivity
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.actions.AmarChip
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBox
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBoxType
import com.example.and_sales_portal.designsystem.components.inputs.AmarDropdownPicker
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.components.navigation.AmarAppBar
import com.example.and_sales_portal.designsystem.components.navigation.AmarAppBarType
import com.example.and_sales_portal.designsystem.components.navigation.AmarTab
import com.example.and_sales_portal.designsystem.components.navigation.AmarTabItem
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarFormTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.attendance.AttendanceBadge
import com.example.and_sales_portal.ui.common.Fmt
import com.example.and_sales_portal.ui.common.InfoRow
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.example.and_sales_portal.ui.partner.rupiah
import com.example.and_sales_portal.ui.visits.VisitItem
import com.example.and_sales_portal.ui.visits.VisitOutcomeBadge
import com.example.and_sales_portal.ui.visits.VisitStatusBadge
import java.time.LocalDate

/** Screen with an App Bar and tabs above scrolling content (Kinerja SA/SR, Insentif TL, Kinerja Penjualan TL). */
@Composable
fun TabbedScaffold(title: String, onBack: (() -> Unit)?, tabs: List<Int>, selected: Int, onTab: (Int) -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(AmarTheme.colors.background.canvas)) {
        AmarAppBar(title = title, type = if (onBack == null) AmarAppBarType.Primary else AmarAppBarType.Secondary, onNavigationClick = onBack)
        AmarTab {
            tabs.forEachIndexed { i, label -> AmarTabItem(stringResource(label), i == selected, { onTab(i) }, icon = null) }
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AmarFormTokens.PaddingHorizontal, vertical = AmarFormTokens.PaddingVertical),
            verticalArrangement = Arrangement.spacedBy(AmarFormTokens.FieldGap),
            content = content,
        )
    }
}

/** Productivity lines of a person (attendance and visits). */
@Composable
fun ProductivityCard(title: String, p: Productivity, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val c = AmarTheme.colors
    AmarCard(modifier.fillMaxWidth(), style = AmarCardStyle.Border, onClick = onClick) {
        Text(title, color = c.content.primary, style = AmarTheme.typography.headingSm)
        Text(stringResource(R.string.prod_attendance, p.present, p.workdays, p.onTime, p.late), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        Text(stringResource(R.string.prod_visits, p.visitsDone, p.visitsPlanned, p.visitsLate, p.visitsMissed), color = c.content.secondary, style = AmarTheme.typography.bodySm)
    }
}

/** Sales per toko (PRD D6 "per toko"). */
data class StoreSales(val store: Store, val summary: SalesSummary)

/**
 * Spec: SLS-02 Kinerja, SLS-03 Pinjaman, SLS-04 Insentif (SA/SR tab Kinerja; PRD v3 D5, D6): own sales per period and
 * per toko, own productivity, loans from assigned toko, own incentive estimate.
 */
@Composable
fun PerformanceTabScreen(
    tab: Int,
    onTab: (Int) -> Unit,
    period: Period,
    onPeriod: (Period) -> Unit,
    summary: SalesSummary,
    perStore: List<StoreSales>,
    productivity: Productivity,
    loans: LoanSectionState,
    incentive: Incentive,
) {
    TabbedScaffold(stringResource(R.string.nav_kinerja), null, listOf(R.string.perf_tab_sales, R.string.perf_tab_loans, R.string.perf_tab_incentive), tab, onTab) {
        when (tab) {
            0 -> {
                PeriodChips(listOf(Period.MONTH, Period.LAST_MONTH, Period.WEEK, Period.TODAY), period, onPeriod)
                SalesSummaryCard(stringResource(period.label), summary)
                SectionHeader(stringResource(R.string.perf_per_store))
                perStore.forEach { StoreSalesCard(it.store.name, it.summary) }
                ProductivityCard(stringResource(R.string.perf_productivity), productivity)
            }
            1 -> LoanSection(loans)
            else -> {
                IncentiveHero(incentive, null)
                IncentiveDetailCard(stringResource(R.string.inc_mine), incentive)
                AmarInfoBox(stringResource(R.string.inc_estimate_note), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
            }
        }
    }
}

@Composable
fun StoreSalesCard(name: String, s: SalesSummary, onClick: (() -> Unit)? = null, extra: (@Composable () -> Unit)? = null) {
    val c = AmarTheme.colors
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border, onClick = onClick) {
        Text(name, color = c.content.primary, style = AmarTheme.typography.labelMdSemibold)
        Text(stringResource(R.string.sales_counts, s.submitted, s.accepted, s.paidOut), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        InfoRow(stringResource(R.string.sales_paid_out_amount), rupiah(s.paidOutAmount))
        extra?.invoke()
    }
}

/** Filters and rows of a loan list (PRD D5: status, toko, period). */
data class LoanSectionState(
    val loans: List<Loan>,
    val stores: List<Store>,
    val status: LoanStatus?,
    val store: Store?,
    val period: Period,
    val onStatus: (LoanStatus?) -> Unit,
    val onStore: (Store?) -> Unit,
    val onPeriod: (Period) -> Unit,
)

/** Spec: SLS-03 / TL-09 Pinjaman: read-only list synced from Tunaiku CRM, masked. Shows the 50 most recent. */
@Composable
fun LoanSection(s: LoanSectionState) {
    val c = AmarTheme.colors
    AmarInfoBox(stringResource(R.string.loan_readonly), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Info)
    PeriodChips(listOf(Period.MONTH, Period.LAST_MONTH, Period.WEEK, Period.TODAY), s.period, s.onPeriod)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
        AmarChip(stringResource(R.string.ptn_filter_all), s.status == null, { s.onStatus(null) })
        LoanStatus.entries.forEach { st -> AmarChip(stringResource(loanStatusLabel(st)), s.status == st, { s.onStatus(st) }) }
    }
    val all = stringResource(R.string.loan_filter_all_stores)
    AmarDropdownPicker(
        value = s.store,
        options = listOf<Store?>(null) + s.stores,
        onSelect = s.onStore,
        sheetTitle = stringResource(R.string.loan_filter_sheet_store),
        modifier = Modifier.fillMaxWidth(),
        optionLabel = { it?.name ?: all },
        label = stringResource(R.string.loan_filter_store),
        placeholder = all,
    )
    Text(stringResource(R.string.loan_count, s.loans.size, rupiah(s.loans.sumOf { it.amount })), color = c.content.secondary, style = AmarTheme.typography.bodySm)
    if (s.loans.isEmpty()) AmarInfoBox(stringResource(R.string.loan_empty), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
    val byId = s.stores.associateBy { it.id }
    s.loans.sortedByDescending { it.updatedAt }.take(MAX_LOANS).forEach { LoanCard(it, byId[it.storeId]) }
}

private const val MAX_LOANS = 50

/** Spec: TL-07 Produktivitas Tim (PRD v3 C9): attendance and visit results per SA/SR, daily, weekly, monthly. */
@Composable
fun TeamProductivityScreen(period: Period, onPeriod: (Period) -> Unit, rows: List<Pair<DemoDirectory.Person, Productivity>>, onBack: () -> Unit, onOpen: (DemoDirectory.Person) -> Unit) {
    ScreenScaffold(stringResource(R.string.prod_title), onBack) {
        PeriodChips(listOf(Period.TODAY, Period.WEEK, Period.MONTH), period, onPeriod)
        if (rows.isEmpty()) AmarInfoBox(stringResource(R.string.team_empty), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
        rows.forEach { (p, pr) -> ProductivityCard(p.fullName, pr) { onOpen(p) } }
    }
}

/** A day of attendance evidence. */
data class EvidenceDay(val date: LocalDate, val status: AttendanceStatus, val clockIn: Long?, val lat: Double?, val lng: Double?, val selfie: String?, val visits: List<VisitItem>)

/** Spec: TL-07 Bukti absensi & kunjungan of one SA/SR (PRD C9 "with selfie and map evidence"). */
@Composable
fun EvidenceScreen(name: String, period: Period, onPeriod: (Period) -> Unit, days: List<EvidenceDay>, onBack: () -> Unit, onMap: (Double, Double) -> Unit) {
    val c = AmarTheme.colors
    ScreenScaffold(name, onBack) {
        Text(stringResource(R.string.prod_evidence), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        PeriodChips(listOf(Period.TODAY, Period.WEEK, Period.MONTH), period, onPeriod)
        if (days.isEmpty()) AmarInfoBox(stringResource(R.string.perf_empty), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
        days.forEach { d ->
            AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(Fmt.shortDate(d.date), Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.labelMdSemibold)
                    AttendanceBadge(d.status)
                }
                Text(d.clockIn?.let { stringResource(R.string.prod_clock_in, Fmt.timeLabel(it)) } ?: stringResource(R.string.prod_no_clock_in), color = c.content.secondary, style = AmarTheme.typography.bodySm)
                if (d.clockIn != null) {
                    Text(if (d.selfie != null) stringResource(R.string.prod_selfie) else stringResource(R.string.prod_selfie_missing), color = c.content.tertiary, style = AmarTheme.typography.bodySm)
                    if (d.lat != null && d.lng != null) AmarButton(stringResource(R.string.prod_map), { onMap(d.lat, d.lng) }, type = AmarButtonType.Naked, size = AmarButtonSize.Small)
                }
                d.visits.forEach { v ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
                        Text(v.visit.plannedTime, color = c.content.primary, style = AmarTheme.typography.bodySm)
                        Text(v.store.name, Modifier.weight(1f), c.content.secondary, style = AmarTheme.typography.bodySm, maxLines = 1)
                        VisitStatusBadge(v.status)
                        v.outcome?.let { VisitOutcomeBadge(it) }
                    }
                }
            }
        }
    }
}

/**
 * Spec: TL-08 Kinerja Penjualan (PRD v3 C9): team totals, then per SA/SR or per partner/toko, with a filter by member or
 * partner.
 */
@Composable
fun TeamSalesScreen(
    period: Period,
    onPeriod: (Period) -> Unit,
    tab: Int,
    onTab: (Int) -> Unit,
    total: SalesSummary,
    members: List<Pair<DemoDirectory.Person, SalesSummary>>,
    partners: List<Pair<Partner, List<StoreSales>>>,
    memberFilter: DemoDirectory.Person?,
    memberOptions: List<DemoDirectory.Person>,
    onMember: (DemoDirectory.Person?) -> Unit,
    partnerFilter: Partner?,
    partnerOptions: List<Partner>,
    onPartner: (Partner?) -> Unit,
    onBack: () -> Unit,
) {
    TabbedScaffold(stringResource(R.string.act_sales), onBack, listOf(R.string.perf_per_member, R.string.perf_per_partner), tab, onTab) {
        PeriodChips(listOf(Period.MONTH, Period.LAST_MONTH, Period.WEEK, Period.TODAY), period, onPeriod)
        SalesSummaryCard(stringResource(R.string.perf_team_total), total)
        if (tab == 0) {
            val all = stringResource(R.string.perf_filter_all_members)
            AmarDropdownPicker(memberFilter, listOf<DemoDirectory.Person?>(null) + memberOptions, onMember, stringResource(R.string.perf_filter_sheet_member), Modifier.fillMaxWidth(), { it?.fullName ?: all }, label = stringResource(R.string.perf_filter_member), placeholder = all)
            members.filter { memberFilter == null || it.first.id == memberFilter.id }.forEach { (p, s) -> StoreSalesCard("${p.fullName} (${p.role})", s) }
        } else {
            val all = stringResource(R.string.perf_filter_all_partners)
            AmarDropdownPicker(partnerFilter, listOf<Partner?>(null) + partnerOptions, onPartner, stringResource(R.string.perf_filter_sheet_partner), Modifier.fillMaxWidth(), { it?.name ?: all }, label = stringResource(R.string.perf_filter_partner), placeholder = all)
            partners.filter { partnerFilter == null || it.first.id == partnerFilter.id }.forEach { (p, stores) ->
                val sum = SalesSummary(stores.sumOf { it.summary.submitted }, stores.sumOf { it.summary.accepted }, stores.sumOf { it.summary.paidOut }, stores.sumOf { it.summary.paidOutUnits }, stores.sumOf { it.summary.paidOutAmount })
                StoreSalesCard(p.name, sum) {
                    stores.forEach { InfoRow(it.store.name, rupiah(it.summary.paidOutAmount)) }
                }
            }
        }
    }
}

/** Spec: TL-09 Pinjaman (PRD v3 C9): loans of the TL's partners. */
@Composable
fun TeamLoansScreen(s: LoanSectionState, onBack: () -> Unit) {
    ScreenScaffold(stringResource(R.string.act_loans), onBack) { LoanSection(s) }
}

/** Spec: TL-10 Insentif (PRD v3 C9): estimates for the TL, team members and own partners. */
@Composable
fun TeamIncentiveScreen(
    tab: Int,
    onTab: (Int) -> Unit,
    mine: Incentive,
    team: List<Pair<DemoDirectory.Person, Incentive>>,
    partners: List<Pair<Partner, Incentive>>,
    onBack: () -> Unit,
) {
    TabbedScaffold(stringResource(R.string.act_incentive), onBack, listOf(R.string.inc_mine, R.string.inc_team, R.string.inc_partners), tab, onTab) {
        when (tab) {
            0 -> {
                IncentiveHero(mine, null)
                IncentiveDetailCard(stringResource(R.string.inc_mine), mine)
            }
            1 -> team.forEach { (p, inc) -> IncentiveDetailCard("${p.fullName} (${p.role})", inc) }
            else -> partners.forEach { (p, inc) -> IncentiveDetailCard(p.name, inc) }
        }
        AmarInfoBox(stringResource(R.string.inc_estimate_note), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
    }
}
