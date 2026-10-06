package com.example.and_sales_portal.ui.partnerregistration

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import java.io.File

/**
 * Base of the Partner Registration fragments: one Fragment per Flow ID with Compose content, sharing
 * [PartnerRegistrationViewModel] scoped to `nav_partner_registration` (CLAUDE.md §2).
 */
abstract class RegFragment : Fragment() {

    protected val viewModel: PartnerRegistrationViewModel by navGraphViewModels(R.id.nav_partner_registration) {
        PartnerRegistrationViewModel.Factory
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AmarTheme {
                    val state by viewModel.state.collectAsState()
                    Content(state)
                }
            }
        }

    @Composable
    protected abstract fun Content(state: PartnerRegistrationState)

    /** App Bar back: previous step, or close the flow from the first step. */
    protected fun back() {
        if (!findNavController().navigateUp()) requireActivity().finish()
    }

    protected fun go(actionId: Int, args: Bundle? = null) = findNavController().navigate(actionId, args)
}

/** Spec: REG-02 Partner info. */
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
                onContinue = { if (viewModel.submitPartnerInfo()) go(R.id.action_reg02_to_reg03) },
                onBack = ::back,
            ),
        )
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
                onContinue = { if (viewModel.submitBusiness()) go(R.id.action_reg03_to_reg04) },
                onBack = ::back,
            ),
        )
    }
}

/** Spec: REG-04 Informasi PIC. */
class RegPicFragment : RegFragment() {
    @Composable
    override fun Content(state: PartnerRegistrationState) {
        RegPicScreen(
            state,
            DetailsActions(
                onPicName = viewModel::setPicName,
                onPicEmail = viewModel::setPicEmail,
                onPicPhone = viewModel::setPicPhone,
                onChoose = viewModel::choose,
                onContinue = { if (viewModel.submitPic()) go(R.id.action_reg04_to_reg05) },
                onBack = ::back,
            ),
        )
    }
}

/** Spec: REG-05 Data Rekening. */
class RegBankAccountFragment : RegFragment() {
    @Composable
    override fun Content(state: PartnerRegistrationState) {
        RegBankAccountScreen(
            state,
            BankAccountActions(
                onBank = viewModel::selectBank,
                onAccountNumber = viewModel::setAccountNumber,
                onCheck = viewModel::checkAccount,
                onContinue = { if (viewModel.submitBankAccount()) go(R.id.action_reg05_to_reg06) },
                onBack = ::back,
            ),
        )
    }
}

/**
 * Spec: REG-06 Dokumen Upload. Camera = system camera app (`TakePicture`, CLAUDE.md §2); the custom capture screen of
 * Figma ("Camera Capture") is not built yet. Gallery = Android Photo Picker (`PickVisualMedia`), as in Figma.
 */
class RegDocumentsFragment : RegFragment() {

    private var pendingDocument: RegDocument? = null
    private var pendingCapture: Uri? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val document = pendingDocument
        val uri = pendingCapture
        if (saved && document != null && uri != null) startUpload(document, uri)
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val document = pendingDocument
        if (uri != null && document != null) startUpload(document, uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingDocument = savedInstanceState?.getString(KEY_DOCUMENT)?.let(RegDocument::valueOf)
        pendingCapture = savedInstanceState?.getString(KEY_CAPTURE)?.let(Uri::parse)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        pendingDocument?.let { outState.putString(KEY_DOCUMENT, it.name) }
        pendingCapture?.let { outState.putString(KEY_CAPTURE, it.toString()) }
    }

    @Composable
    override fun Content(state: PartnerRegistrationState) {
        val submission = state.submission
        LaunchedEffect(submission) {
            if (submission != null) {
                go(
                    R.id.action_reg06_to_success,
                    bundleOf(
                        RegSuccessFragment.ARG_PARTNER to state.partner.name,
                        RegSuccessFragment.ARG_ID to submission.submissionId,
                        RegSuccessFragment.ARG_TIME to submission.submittedAtMillis,
                    ),
                )
            }
        }
        RegDocumentsScreen(
            state,
            DocumentsActions(
                onCamera = ::capture,
                onGallery = { document ->
                    pendingDocument = document
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onRemove = viewModel::removeDocument,
                onRetry = viewModel::retryUpload,
                onSubmit = viewModel::submitDocuments,
                onBack = ::back,
            ),
        )
    }

    private fun capture(document: RegDocument) {
        val dir = File(requireContext().cacheDir, CAPTURE_DIR).apply { mkdirs() }
        val file = File(dir, "${document.name.lowercase()}_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
        pendingDocument = document
        pendingCapture = uri
        takePicture.launch(uri)
    }

    private fun startUpload(document: RegDocument, uri: Uri) {
        var name = uri.lastPathSegment ?: document.name
        var size = 0L
        requireContext().contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(0)?.let { name = it }
                if (!cursor.isNull(1)) size = cursor.getLong(1)
            }
        }
        viewModel.upload(document, uri, name, size)
    }

    private companion object {
        const val KEY_DOCUMENT = "reg_pending_document"
        const val KEY_CAPTURE = "reg_pending_capture"
        const val CAPTURE_DIR = "reg_captures"
    }
}

/** Submission sent (Figma "06 | … | Success"). Back and "Kembali ke Partner" close the flow. */
class RegSuccessFragment : RegFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = requireActivity().finish()
            },
        )
    }

    @Composable
    override fun Content(state: PartnerRegistrationState) {
        val args = requireArguments()
        RegSuccessScreen(
            partnerName = args.getString(ARG_PARTNER).orEmpty(),
            submissionId = args.getString(ARG_ID).orEmpty(),
            submittedAtMillis = args.getLong(ARG_TIME),
            onDone = { requireActivity().finish() },
        )
    }

    companion object {
        const val ARG_PARTNER = "partner"
        const val ARG_ID = "submission_id"
        const val ARG_TIME = "submitted_at"
    }
}
