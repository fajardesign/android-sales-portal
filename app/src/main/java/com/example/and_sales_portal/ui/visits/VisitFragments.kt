package com.example.and_sales_portal.ui.visits

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.activity.ActivityRepository
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.Punch
import com.example.and_sales_portal.ui.attendance.CaptureFragment
import com.example.and_sales_portal.ui.attendance.CaptureMode
import com.example.and_sales_portal.ui.common.ComposeFragment
import com.example.and_sales_portal.ui.common.ConfirmDialog
import com.example.and_sales_portal.ui.common.Fmt
import com.example.and_sales_portal.ui.common.WithToast
import androidx.fragment.app.Fragment

/**
 * Spec: ATT-03 Kunjungan. [VisitsTabFragment] is the SA/SR tab; [MyVisitsFragment] is TL Aktivitas › Kunjungan Saya (with
 * back).
 */
abstract class VisitListFragment(private val asTab: Boolean) : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val repo = activityRepo
        val data by repo.data.collectAsState()
        val partners by container.partners.partners.collectAsState()
        val online by container.connectivity.online.collectAsState()
        val (incoming, clearIncoming) = incomingToast()
        var synced by remember { mutableStateOf<String?>(null) }
        val syncedMessage = SyncPending(repo, online)
        LaunchedEffect(syncedMessage) { if (syncedMessage != null) synced = syncedMessage }
        val today = repo.today()
        val state = remember(data, partners, online) {
            val groups = repo.visitPartners(today)
            val visits = repo.visitsOn(today).mapNotNull { visitItem(it, repo.store(it.storeId), repo.partnerOf(it.storeId), today) }
            VisitListUiState(
                date = Fmt.longDate(today),
                week = repo.visitWeek(today),
                visitedToday = visits.isNotEmpty(),
                groups = groups,
                visits = visits,
                online = online,
                // The day's gate (check in kerja, 12:00); "visited this week" is shown per toko.
                block = groups.flatMap { it.stores }.firstOrNull { !it.visited }?.let { repo.checkInBlock(it.store.id) },
            )
        }
        WithToast(incoming ?: synced, { clearIncoming(); synced = null }) {
            VisitListScreen(
                title = stringResource(if (asTab) R.string.vis_title else R.string.vis_title_own),
                s = state,
                onBack = if (asTab) null else ::back,
                onOpen = { navigate(R.id.visit_detail, VisitDetailFragment.args(it.visit.id)) },
                onCheckIn = { navigate(R.id.capture, CaptureFragment.args(CaptureMode.CHECK_IN, storeId = it.id)) },
                onWorkCheckIn = { navigate(R.id.capture, CaptureFragment.args(CaptureMode.CLOCK_IN)) },
            )
        }
    }
}

class VisitsTabFragment : VisitListFragment(asTab = true)
class MyVisitsFragment : VisitListFragment(asTab = false)

/** Spec: ATT-04 Detail Kunjungan. */
class VisitDetailFragment : ComposeFragment() {
    private val visitId by lazy { requireArguments().getString(ARG_VISIT)!! }

    @Composable
    override fun ScreenContent() {
        val repo = activityRepo
        val data by repo.data.collectAsState()
        val online by container.connectivity.online.collectAsState()
        val (incoming, clearIncoming) = incomingToast()
        val syncedMessage = SyncPending(repo, online)
        var synced by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(syncedMessage) { if (syncedMessage != null) synced = syncedMessage }
        val item = remember(data) { repo.visit(visitId)?.let { visitItem(it, repo.store(it.storeId), repo.partnerOf(it.storeId), repo.today()) } }
        if (item == null) {
            LaunchedEffect(Unit) { back() }
            return
        }
        WithToast(incoming ?: synced, { clearIncoming(); synced = null }) {
            VisitDetailScreen(
                item,
                now = { repo.now().toEpochMilli() },
                onBack = ::back,
                onOpenMaps = { lat, lng -> openMaps(lat, lng) },
                onCheckOut = { navigate(R.id.visit_checkout, args(visitId)) },
            )
        }
    }

    companion object {
        internal const val ARG_VISIT = "visit_id"
        fun args(visitId: String) = bundleOf(ARG_VISIT to visitId)
    }
}

/** Spec: ATT-05 Check Out Kunjungan. */
class VisitCheckOutFragment : ComposeFragment() {
    private val visitId by lazy { requireArguments().getString(VisitDetailFragment.ARG_VISIT)!! }

    @Composable
    override fun ScreenContent() {
        val repo = activityRepo
        val data by repo.data.collectAsState()
        val online by container.connectivity.online.collectAsState()
        var note by rememberSaveable { mutableStateOf("") }
        var confirm by remember { mutableStateOf(false) }
        val item = remember(data) { repo.visit(visitId)?.let { visitItem(it, repo.store(it.storeId), repo.partnerOf(it.storeId), repo.today()) } }
        val checkIn = item?.visit?.checkIn
        if (item == null || checkIn == null) {
            LaunchedEffect(Unit) { back() }
            return
        }
        VisitCheckOutScreen(item, note, online, repo.now().toEpochMilli(), { note = it }, ::back) { confirm = true }
        if (confirm) {
            ConfirmDialog(
                title = stringResource(R.string.vis_check_out_confirm_title),
                message = stringResource(R.string.vis_check_out_confirm),
                confirmLabel = stringResource(R.string.vis_check_out),
                onConfirm = {
                    confirm = false
                    val at = repo.now().toEpochMilli()
                    // PRD D3: the check-out location is not recorded; the visit keeps the check-in location.
                    val punch = Punch(at, checkIn.lat, checkIn.lng, checkIn.address, synced = online, zone = ActivityRules.zoneOf(repo.userId).id, deviceZone = ActivityRules.systemZone().id)
                    if (repo.checkOut(visitId, punch, note)) {
                        val minutes = (at - checkIn.at) / 60_000
                        val d = getString(R.string.vis_duration_value, (minutes / 60).toInt(), (minutes % 60).toInt())
                        backWithToast(getString(R.string.vis_check_out_done, d))
                    } else {
                        // S13: check-out closes at 23:59 of the visit day.
                        backWithToast(getString(R.string.vis_check_out_closed))
                    }
                },
                onDismiss = { confirm = false },
            )
        }
    }
}

/**
 * Sends check-outs saved offline once the device is online again (PRD D3). Returns the toast to show, once.
 */
@Composable
fun SyncPending(repo: ActivityRepository, online: Boolean): String? {
    var message by remember { mutableStateOf<String?>(null) }
    val resources = LocalContext.current.resources
    LaunchedEffect(online) {
        if (online) {
            val n = repo.syncPending()
            if (n > 0) message = resources.getString(R.string.vis_synced, n)
        }
    }
    return message
}

internal fun Fragment.openMaps(lat: Double, lng: Double) {
    val tries = listOf("geo:$lat,$lng?q=$lat,$lng", "https://www.google.com/maps/search/?api=1&query=$lat,$lng")
    for (uri in tries) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
            return
        } catch (e: ActivityNotFoundException) {
            continue
        }
    }
}
