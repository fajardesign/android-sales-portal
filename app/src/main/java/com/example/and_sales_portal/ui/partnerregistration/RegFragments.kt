package com.example.and_sales_portal.ui.partnerregistration

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.core.os.bundleOf
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.device.Place
import com.example.and_sales_portal.designsystem.components.feedback.AmarModal
import com.example.and_sales_portal.ui.common.ComposeFragment
import com.example.and_sales_portal.ui.common.ConfirmDialog
import com.example.and_sales_portal.ui.common.LocationCapture
import com.example.and_sales_portal.ui.partner.SubmissionDetailFragment
import com.example.and_sales_portal.ui.partnerregistration.data.FakePartnerRegistrationRepository
import com.example.and_sales_portal.ui.visits.openMaps

/**
 * Base of the Partner Registration fragments (PRD v3 C2): one Fragment per Flow ID with Compose content, sharing
 * [PartnerRegistrationViewModel] scoped to `nav_partner_registration` (CLAUDE.md §2), nested in `nav_main`.
 * Entry: Partner tab › "Daftarkan Partner" (TL and SR).
 */
abstract class RegFragment : ComposeFragment() {

    protected val viewModel: PartnerRegistrationViewModel by navGraphViewModels(R.id.nav_partner_registration) {
        viewModelFactory {
            initializer { PartnerRegistrationViewModel(FakePartnerRegistrationRepository(), container.partners, container.demo, requireNotNull(session).user) }
        }
    }

    @Composable
    override fun ScreenContent() {
        val state by viewModel.state.collectAsState()
        Content(state)
    }

    @Composable
    protected abstract fun Content(state: PartnerRegistrationState)

    /** Leaves the whole flow (back to the Partner tab). */
    protected fun closeFlow() {
        findNavController().popBackStack(R.id.nav_partner_registration, true)
    }
}

/** Spec: REG-02 Informasi Partner. Offers the saved draft first (PRD C2). */
class RegPartnerInfoFragment : RegFragment() {
    @Composable
    override fun Content(state: PartnerRegistrationState) {
        RegPartnerInfoScreen(
            state,
            PartnerInfoActions(
                onName = viewModel::setPartnerName,
                onAddress = viewModel::setAddress,
                onProvince = viewModel::selectProvince,
                onRegency = viewModel::selectRegency,
                onDistrict = viewModel::selectDistrict,
                onVillage = viewModel::selectVillage,
                onRt = viewModel::setRt,
                onRw = viewModel::setRw,
                onReferral = viewModel::setReferralCode,
                onContinue = { if (viewModel.submitStep(RegStep.PartnerInfo)) navigate(R.id.action_reg02_to_reg03) },
                onBack = ::back,
            ),
        )
        state.draftOffer?.let { name ->
            Dialog(onDismissRequest = viewModel::discardDraft) {
                AmarModal(
                    primaryLabel = stringResource(R.string.reg_draft_continue),
                    onPrimaryClick = viewModel::acceptDraft,
                    title = stringResource(R.string.reg_draft_title),
                    description = stringResource(R.string.reg_draft_message, name.ifBlank { stringResource(R.string.reg_draft_unnamed) }),
                    secondLabel = stringResource(R.string.reg_draft_new),
                    onSecondClick = viewModel::discardDraft,
                )
            }
        }
    }
}

/** Spec: REG-03 Data Bisnis. */
class RegBusinessFragment : RegFragment() {
    @Composable
    override fun Content(state: PartnerRegistrationState) {
        RegBusinessScreen(
            state,
            DetailsActions(
                onEmail = viewModel::setEmail,
                onRevenue = viewModel::setRevenue,
                onOutletCount = viewModel::setOutletCount,
                onChoose = viewModel::choose,
                onContinue = { if (viewModel.submitStep(RegStep.Business)) navigate(R.id.action_reg03_to_reg04) },
                onBack = ::back,
            ),
        )
    }
}

/** Spec: REG-04 Informasi PIC & PKS. */
class RegPicFragment : RegFragment() {
    @Composable
    override fun Content(state: PartnerRegistrationState) {
        RegPicScreen(state, picActions { if (viewModel.submitStep(RegStep.Pic)) navigate(R.id.action_reg04_to_reg05) })
    }

    private fun picActions(onContinue: () -> Unit) = DetailsActions(
        onPicName = viewModel::setPicName,
        onPicEmail = viewModel::setPicEmail,
        onPicPhone = viewModel::setPicPhone,
        onPrivyId = viewModel::setPrivyId,
        onPksEmail = viewModel::setPksEmail,
        onChoose = viewModel::choose,
        onContinue = onContinue,
        onBack = ::back,
    )
}

/** Spec: REG-05 Data Rekening. */
class RegBankAccountFragment : RegFragment() {
    @Composable
    override fun Content(state: PartnerRegistrationState) {
        RegBankAccountScreen(
            state,
            BankAccountActions(
                onBank = viewModel::selectBank,
                onBranch = viewModel::setBranch,
                onAccountNumber = viewModel::setAccountNumber,
                onAccountName = viewModel::setAccountName,
                onContinue = { if (viewModel.submitStep(RegStep.BankAccount)) navigate(R.id.action_reg05_to_reg06) },
                onBack = ::back,
            ),
        )
    }
}

/** Spec: REG-06 Dokumen Upload. Camera = system camera app (`TakePicture`, CLAUDE.md §2). */
class RegDocumentsFragment : RegFragment() {

    private val picker = DocumentPicker(this, { container.demo.documentDir }) { doc, path, name, size -> viewModel.attach(doc, path, name, size) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        picker.restore(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        picker.save(outState)
    }

    @Composable
    override fun Content(state: PartnerRegistrationState) {
        RegDocumentsScreen(
            state,
            DocumentsActions(
                onCamera = picker::camera,
                onGallery = picker::gallery,
                onFile = picker::file,
                onRemove = viewModel::removeDocument,
                onRetry = viewModel::retryUpload,
                onContinue = { if (viewModel.documentsReady()) navigate(R.id.action_reg06_to_reg07) },
                onBack = ::back,
            ),
        )
    }
}

/** Spec: REG-07 Ringkasan Pengajuan. "Ubah" goes back to that step. */
class RegReviewFragment : RegFragment() {
    @Composable
    override fun Content(state: PartnerRegistrationState) {
        val preview = remember(state) { viewModel.preview() }
        val sim by container.simulation.state.collectAsState()
        RegReviewScreen(
            preview,
            onBack = ::back,
            onEdit = { step -> findNavController().popBackStack(destinationOf(step), false) },
            onContinue = { if (!sim.regConsent || state.consent) navigate(R.id.action_reg07_to_reg08) },
            consentRequired = sim.regConsent,
            consent = state.consent,
            onConsent = viewModel::setConsent,
        )
    }

    private fun destinationOf(step: RegStep) = when (step) {
        RegStep.PartnerInfo -> R.id.reg02_partner_info
        RegStep.Business -> R.id.reg03_business
        RegStep.Pic -> R.id.reg04_pic
        RegStep.BankAccount -> R.id.reg05_bank_account
        RegStep.Documents -> R.id.reg06_documents
    }
}

/** Spec: REG-08 Lokasi Usaha, then "Kirim Pengajuan" with a confirmation (PRD v3 "every status change … confirmation"). */
class RegLocationFragment : RegFragment() {

    private val location = LocationCapture(this, { container.location }) {
        val area = DemoDirectory.area(requireNotNull(session).user.areaIds.firstOrNull() ?: 0) ?: DemoDirectory.areas.first()
        Place(area.lat, area.lng, "${area.district}, ${area.city}, ${area.province}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (location.state.value.found == null) location.start()
    }

    override fun onResume() {
        super.onResume()
        location.onResume()
    }

    @Composable
    override fun Content(state: PartnerRegistrationState) {
        val loc by location.state.collectAsState()
        var confirm by remember { mutableStateOf(false) }
        LaunchedEffect(state.submitted) {
            state.submitted?.let {
                navigate(R.id.action_reg08_to_success, bundleOf(RegSuccessFragment.ARG_PARTNER to it.partnerName, RegSuccessFragment.ARG_ID to it.registrationNumber, RegSuccessFragment.ARG_TIME to it.atMillis))
            }
        }
        RegLocationScreen(loc, state.submitting, ::back, location.actions { lat, lng -> openMaps(lat, lng) }) { confirm = true }
        if (confirm) {
            ConfirmDialog(
                title = stringResource(R.string.reg_submit_confirm_title),
                message = stringResource(R.string.reg_submit_confirm),
                confirmLabel = stringResource(R.string.reg_submit),
                onConfirm = {
                    confirm = false
                    viewModel.setLocation(loc.found)
                    viewModel.submit()
                },
                onDismiss = { confirm = false },
            )
        }
    }
}

/** Spec: REG-09 Pengajuan Terkirim. Back and "Kembali ke Partner" close the flow. */
class RegSuccessFragment : RegFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { closeFlow() }
    }

    @Composable
    override fun Content(state: PartnerRegistrationState) {
        val args = requireArguments()
        val id = args.getString(ARG_ID).orEmpty()
        RegSuccessScreen(
            partnerName = args.getString(ARG_PARTNER).orEmpty(),
            submissionId = id,
            submittedAtMillis = args.getLong(ARG_TIME),
            onDone = ::closeFlow,
            onSeeSubmission = {
                closeFlow()
                navigate(R.id.submission_detail, SubmissionDetailFragment.args(id))
            },
        )
    }

    companion object {
        const val ARG_PARTNER = "partner"
        const val ARG_ID = "submission_id"
        const val ARG_TIME = "submitted_at"
    }
}
