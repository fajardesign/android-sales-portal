package com.example.and_sales_portal.ui.partner

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.and_sales_portal.BuildConfig
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.device.Place
import com.example.and_sales_portal.data.partner.DocType
import com.example.and_sales_portal.data.partner.PartnerRepository
import com.example.and_sales_portal.data.partner.PartnerStatus
import com.example.and_sales_portal.data.partner.RevisionItem
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadge
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeSize
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeType
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.common.ComposeFragment
import com.example.and_sales_portal.ui.common.ConfirmDialog
import com.example.and_sales_portal.ui.common.LocationCapture
import com.example.and_sales_portal.ui.common.PersistentAction
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.example.and_sales_portal.ui.common.WithToast
import com.example.and_sales_portal.ui.partnerregistration.BankAccountActions
import com.example.and_sales_portal.ui.partnerregistration.DetailsActions
import com.example.and_sales_portal.ui.partnerregistration.DocumentField
import com.example.and_sales_portal.ui.partnerregistration.DocumentPicker
import com.example.and_sales_portal.ui.partnerregistration.LocalRegFrame
import com.example.and_sales_portal.ui.partnerregistration.PartnerInfoActions
import com.example.and_sales_portal.ui.partnerregistration.PartnerRegistrationViewModel
import com.example.and_sales_portal.ui.partnerregistration.RegBankAccountScreen
import com.example.and_sales_portal.ui.partnerregistration.RegBusinessScreen
import com.example.and_sales_portal.ui.partnerregistration.RegFrame
import com.example.and_sales_portal.ui.partnerregistration.RegMode
import com.example.and_sales_portal.ui.partnerregistration.RegPartnerInfoScreen
import com.example.and_sales_portal.ui.partnerregistration.RegPicScreen
import com.example.and_sales_portal.ui.partnerregistration.RegStep
import com.example.and_sales_portal.ui.partnerregistration.RevisionState
import com.example.and_sales_portal.ui.partnerregistration.UploadMethodSheet
import com.example.and_sales_portal.ui.partnerregistration.data.FakePartnerRegistrationRepository
import com.example.and_sales_portal.ui.visits.openMaps

/** Spec: REG-01 Partner tab (PRD v3 C2–C4, D4). */
class PartnerTabFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val user = session ?: return
        val partners by container.partners.partners.collectAsState()
        val (toast, clearToast) = incomingToast()
        var tab by rememberSaveable { mutableStateOf(PartnerTab.Mine) }
        var filter by rememberSaveable { mutableStateOf<PartnerStatus?>(null) }
        val repo = container.partners
        val mine = remember(partners) { repo.myPartners(user.user.id, user.role) }
        // PRD D4: SA does not register partners.
        val submissions = remember(partners) { if (user.role == SalesRole.SA) null else repo.submissionsFor(user.user.id, user.role) }
        WithToast(toast, clearToast) {
            PartnerTabScreen(
                tab, { tab = it }, mine, submissions, filter, { filter = it },
                onOpenPartner = { navigate(R.id.partner_detail, PartnerDetailFragment.args(it.id)) },
                onOpenSubmission = { navigate(R.id.submission_detail, SubmissionDetailFragment.args(it.id)) },
                onRegister = { navigate(R.id.nav_partner_registration) },
            )
        }
    }
}

/** Spec: REG-10 Detail Pengajuan. */
class SubmissionDetailFragment : ComposeFragment() {
    private val id by lazy { requireArguments().getString(ARG_ID)!! }

    @Composable
    override fun ScreenContent() {
        val user = session ?: return
        val partners by container.partners.partners.collectAsState()
        val (toast, clearToast) = incomingToast()
        val p = remember(partners) { container.partners.get(id) } ?: return
        val repo = container.partners
        WithToast(toast, clearToast) {
            SubmissionDetailScreen(
                p,
                canFix = user.role != SalesRole.SA,
                onBack = ::back,
                onFix = { navigate(R.id.revision, RevisionFragment.args(id)) },
                adminActions = if (BuildConfig.DEBUG) adminActions(repo, p.status, id) else null,
            )
        }
    }

    private fun adminActions(repo: PartnerRepository, status: PartnerStatus, id: String): List<AdminAction> = when (status) {
        PartnerStatus.UNDER_REVIEW -> listOf(
            AdminAction(R.string.sim_admin_revision) { repo.adminRequestRevision(id) },
            AdminAction(R.string.sim_admin_verify) { repo.adminVerify(id) },
            AdminAction(R.string.sim_admin_reject) { repo.adminReject(id) },
            AdminAction(R.string.sim_admin_cancel) { repo.adminCancel(id) },
        )
        PartnerStatus.REVISION_REQUIRED -> listOf(
            AdminAction(R.string.sim_admin_reject) { repo.adminReject(id) },
            AdminAction(R.string.sim_admin_cancel) { repo.adminCancel(id) },
        )
        PartnerStatus.VERIFIED -> listOf(AdminAction(R.string.sim_admin_send_pks) { repo.adminSendPks(id) }, AdminAction(R.string.sim_admin_cancel) { repo.adminCancel(id) })
        // PRD Scope 1 v2 C2: Aktifkan stays locked until every toko has its Kode Referral.
        PartnerStatus.WAITING_PKS -> listOfNotNull(
            AdminAction(R.string.sim_admin_fill_referral) { repo.adminFillReferral(id) }.takeIf { repo.referralMissing(id) },
            AdminAction(R.string.sim_admin_activate, enabled = !repo.referralMissing(id)) { repo.adminActivate(id) },
            AdminAction(R.string.sim_admin_cancel) { repo.adminCancel(id) },
        )
        PartnerStatus.ACTIVE -> listOfNotNull(
            AdminAction(R.string.sim_admin_fill_referral) { repo.adminFillReferral(id) }.takeIf { repo.referralMissing(id) },
            AdminAction(R.string.sim_admin_deactivate) { repo.adminDeactivate(id) },
        )
        else -> emptyList()
    }

    companion object {
        private const val ARG_ID = "partner_id"
        fun args(id: String) = bundleOf(ARG_ID to id)
    }
}

/**
 * Spec: REG-11 Perbaiki & Kirim Ulang (PRD v3 C3): only the flagged items are editable. Sections open the same forms as
 * the registration ("Simpan Perubahan"); flagged documents are uploaded again and become a new version.
 */
class RevisionFragment : ComposeFragment() {
    private val id by lazy { requireArguments().getString(ARG_ID)!! }

    private val vm: PartnerRegistrationViewModel by viewModels {
        viewModelFactory {
            initializer { PartnerRegistrationViewModel(FakePartnerRegistrationRepository(), container.partners, container.demo, requireNotNull(session).user, RegMode.Revision(id)) }
        }
    }

    private val picker = DocumentPicker(this, { container.demo.documentDir }) { doc, path, name, size -> vm.attach(doc, path, name, size) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        picker.restore(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        picker.save(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (vm.revision.value?.editing != null) vm.editSection(null) else back()
        }
    }

    @Composable
    override fun ScreenContent() {
        val state by vm.state.collectAsState()
        val rev by vm.revision.collectAsState()
        val r = rev ?: return
        var confirm by remember { mutableStateOf(false) }
        var methodFor by remember { mutableStateOf<DocType?>(null) }
        val editing = r.editing
        if (editing != null) {
            val close = { vm.editSection(null) }
            val save = { vm.saveSection(editing); Unit }
            CompositionLocalProvider(LocalRegFrame provides RegFrame(stringResource(R.string.rev_title), showStep = false, cta = stringResource(R.string.rev_save))) {
                when (editing) {
                    RegStep.PartnerInfo -> RegPartnerInfoScreen(state, PartnerInfoActions(vm::setPartnerName, vm::setAddress, vm::selectProvince, vm::selectRegency, vm::selectDistrict, vm::selectVillage, vm::setRt, vm::setRw, save, close))
                    RegStep.Business -> RegBusinessScreen(state, DetailsActions(onEmail = vm::setEmail, onRevenue = vm::setRevenue, onOutletCount = vm::setOutletCount, onChoose = vm::choose, onContinue = save, onBack = close))
                    RegStep.Pic -> RegPicScreen(state, DetailsActions(onPicName = vm::setPicName, onPicEmail = vm::setPicEmail, onPicPhone = vm::setPicPhone, onPrivyId = vm::setPrivyId, onChoose = vm::choose, onContinue = save, onBack = close))
                    RegStep.BankAccount -> RegBankAccountScreen(state, BankAccountActions(vm::selectBank, vm::setBranch, vm::setAccountNumber, vm::setAccountName, save, close))
                    RegStep.Documents -> Unit
                }
            }
            return
        }
        val c = AmarTheme.colors
        // Read here (from the collected states), so the button follows every change.
        val ready = remember(state, rev) { vm.revisionReady() }
        ScreenScaffold(
            stringResource(R.string.rev_title),
            ::back,
            action = { PersistentAction(stringResource(R.string.rev_cta), { confirm = true }, enabled = ready) },
        ) {
            Text(stringResource(R.string.rev_description), color = c.content.secondary, style = AmarTheme.typography.bodySm)
            r.items.filter { it.kind == RevisionItem.SEC }.forEach { item ->
                val step = RevisionState.stepOf(item.ref)
                AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
                            Text(item.label, color = c.content.primary, style = AmarTheme.typography.labelMdSemibold)
                            Text(item.note, color = c.feedback.error.text, style = AmarTheme.typography.bodySm)
                        }
                    }
                    if (step != null && step in r.fixed) AmarBadge(stringResource(R.string.rev_fixed), type = AmarBadgeType.Success, size = AmarBadgeSize.Small)
                    step?.let { AmarButton(stringResource(R.string.rev_fix_section), { vm.editSection(it) }, Modifier.fillMaxWidth(), type = AmarButtonType.Secondary, size = AmarButtonSize.Medium) }
                }
            }
            r.items.filter { it.kind == RevisionItem.DOC }.forEach { item ->
                val doc = DocType.of(item.ref) ?: return@forEach
                Text(item.note, color = c.feedback.error.text, style = AmarTheme.typography.bodySm)
                DocumentField(doc, true, state.documents[doc], state.fileErrors[doc],
                    onUpload = { if (doc.store) picker.camera(doc) else methodFor = doc },
                    onRemove = { vm.removeDocument(doc) },
                    onRetry = { vm.retryUpload(doc) },
                )
            }
        }
        methodFor?.let { doc -> UploadMethodSheet({ methodFor = null }, { picker.camera(doc) }, { picker.gallery(doc) }, { picker.file(doc) }) }
        if (confirm) {
            ConfirmDialog(
                title = stringResource(R.string.rev_confirm_title),
                message = stringResource(R.string.rev_confirm),
                confirmLabel = stringResource(R.string.rev_cta),
                onConfirm = {
                    confirm = false
                    if (vm.resubmit()) backWithToast(getString(R.string.rev_done))
                },
                onDismiss = { confirm = false },
            )
        }
    }

    companion object {
        private const val ARG_ID = "partner_id"
        fun args(id: String) = bundleOf(ARG_ID to id)
    }
}

/** Spec: PRT-01 Detail Partner. */
class PartnerDetailFragment : ComposeFragment() {
    private val id by lazy { requireArguments().getString(ARG_ID)!! }

    @Composable
    override fun ScreenContent() {
        val user = session ?: return
        val partners by container.partners.partners.collectAsState()
        val (toast, clearToast) = incomingToast()
        val p = remember(partners) { container.partners.get(id) } ?: return
        WithToast(toast, clearToast) {
            PartnerDetailScreen(
                // PRT-01 (PRD Scope 1 v2): a TL sees only the toko they hold.
                if (user.role == SalesRole.TL) p.copy(stores = p.stores.filter { it.tlId == user.user.id }) else p,
                // PRD C4 / D4: TL and SR add toko; SA does not.
                canAddStore = user.role != SalesRole.SA,
                onBack = ::back,
                onOpenMaps = { lat, lng -> openMaps(lat, lng) },
                onAddStore = { navigate(R.id.add_store, AddStoreFragment.args(id)) },
            )
        }
    }

    companion object {
        private const val ARG_ID = "partner_id"
        fun args(id: String) = bundleOf(ARG_ID to id)
    }
}

/** Spec: PRT-02 Tambah Toko. */
class AddStoreFragment : ComposeFragment() {
    private val id by lazy { requireArguments().getString(ARG_ID)!! }

    private val vm: AddStoreViewModel by viewModels {
        viewModelFactory {
            initializer { AddStoreViewModel(requireNotNull(container.partners.get(id)), container.partners, FakePartnerRegistrationRepository(), requireNotNull(session).user.id) }
        }
    }

    private val location = LocationCapture(this, { container.location }) {
        val p = container.partners.get(id)
        val s = p?.primaryStore
        if (s != null) Place(s.lat + NEW_STORE_OFFSET, s.lng + NEW_STORE_OFFSET, null) else {
            val a = DemoDirectory.areas.first()
            Place(a.lat, a.lng, null)
        }
    }

    override fun onResume() {
        super.onResume()
        location.onResume()
    }

    @Composable
    override fun ScreenContent() {
        val p = container.partners.get(id) ?: return
        val state by vm.state.collectAsState()
        val loc by location.state.collectAsState()
        var confirm by remember { mutableStateOf(false) }
        AddStoreScreen(
            p.name, state, vm.errors(state), vm.missingChoices(state), vm.addressActions, vm::setRevenue, vm::choose,
            loc, location.actions { lat, lng -> openMaps(lat, lng) }, vm.nearby(loc.found),
            onBack = ::back,
            onSave = { if (vm.validate(loc.found)) confirm = true },
        )
        if (confirm) {
            ConfirmDialog(
                title = stringResource(R.string.ts_confirm_title),
                message = stringResource(R.string.ts_confirm, p.name),
                confirmLabel = stringResource(R.string.ts_cta),
                onConfirm = {
                    confirm = false
                    loc.found?.let { found -> backWithToast(getString(R.string.ts_done, vm.save(found).name)) }
                },
                onDismiss = { confirm = false },
            )
        }
    }

    companion object {
        private const val ARG_ID = "partner_id"
        /**
         * Simulated "at the new toko" position (Simulasi › Di lokasi toko): about 300 m from the primary toko, so the
         * 50 m warning only shows with real GPS next to another toko.
         */
        private const val NEW_STORE_OFFSET = 0.002
        fun args(id: String) = bundleOf(ARG_ID to id)
    }
}
