package com.example.and_sales_portal.ui.partnerregistration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import com.example.and_sales_portal.data.device.LocationResult
import com.example.and_sales_portal.data.partner.DocType
import com.example.and_sales_portal.data.partner.Keys
import com.example.and_sales_portal.data.partner.PartnerSeed
import com.example.and_sales_portal.data.partner.PartnerStatus
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.attendance.CaptureActions
import com.example.and_sales_portal.ui.common.LocationCaptureState
import com.example.and_sales_portal.ui.partner.AdminAction
import com.example.and_sales_portal.ui.partner.PartnerDetailScreen
import com.example.and_sales_portal.ui.partner.PartnerTab
import com.example.and_sales_portal.ui.partner.PartnerTabScreen
import com.example.and_sales_portal.ui.partner.SubmissionDetailScreen
import com.example.and_sales_portal.ui.partnerregistration.data.Bank
import com.example.and_sales_portal.ui.partnerregistration.data.Region
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** REG-01 … REG-10 and PRT-01 in their main states (PRD v3 C2–C4). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h760dp-mdpi")
class RegScreensScreenshotTest {

    private fun capture(name: String, content: @Composable () -> Unit) =
        captureRoboImage("build/outputs/roborazzi/reg_$name.png") { CompositionLocalProvider(LocalInspectionMode provides true) { AmarTheme { content() } } }

    private val errors = PartnerRegistrationState(showErrors = RegStep.entries.toSet())
    private val partners = PartnerSeed.partners()

    @Test
    fun forms() {
        capture("02_error") { RegPartnerInfoScreen(errors, PartnerInfoActions()) }
        val r = { name: String -> Region(name, name) }
        val short = PartnerRegistrationState(
            partner = PartnerInfoForm("Ab", "Jl 1", r("Sulawesi Selatan"), r("Kota Makassar"), r("Panakkukang"), r("Pandang"), "3", "005", "AMR-1"),
            showErrors = setOf(RegStep.PartnerInfo),
        )
        capture("02_rules") { RegPartnerInfoScreen(short, PartnerInfoActions()) }
        capture("03_error") { RegBusinessScreen(errors, DetailsActions()) }
        val offline = PartnerRegistrationState(choices = mapOf(RegChoice.StoreType to Keys.STORE_OFFLINE, RegChoice.EntityType to Keys.ENTITY_PT))
        capture("03_offline") { RegBusinessScreen(offline, DetailsActions()) }
        capture("04_error") { RegPicScreen(errors, DetailsActions()) }
        val pic = PartnerRegistrationState(pic = PicForm("Budi Santoso", "budi@gmail.com", "81234567890", "", "budi@gmail.com"), choices = mapOf(RegChoice.PicStatus to Keys.PIC_OWNER))
        capture("04_filled") { RegPicScreen(pic, DetailsActions()) }
        capture("05_error") { RegBankAccountScreen(errors, BankAccountActions()) }
        val bank = PartnerRegistrationState(bank = BankForm(Bank("BCA", "BCA"), "KCP Makassar Panakkukang", "1234567890", "Budi Santoso"))
        capture("05_filled") { RegBankAccountScreen(bank, BankAccountActions()) }
    }

    @Test
    fun documents() {
        val individu = PartnerRegistrationState(choices = mapOf(RegChoice.EntityType to Keys.ENTITY_INDIVIDU, RegChoice.PicStatus to Keys.PIC_KARYAWAN))
        capture("06_individu_karyawan") { RegDocumentsScreen(individu, DocumentsActions()) }
        val pt = PartnerRegistrationState(
            choices = mapOf(RegChoice.EntityType to Keys.ENTITY_PT, RegChoice.PicStatus to Keys.PIC_OWNER),
            documents = mapOf(
                DocType.KTP_PIC to DocumentUpload("/x", "ktp_pic.jpg", 865_280, UploadStatus.Uploaded),
                DocType.NPWP_COMPANY to DocumentUpload("/y", "npwp.pdf", 1_843_200, UploadStatus.Uploading, 0.4f),
            ),
            fileErrors = mapOf(DocType.ANGGARAN_DASAR to FileError.TooLarge),
        )
        capture("06_pt") { RegDocumentsScreen(pt, DocumentsActions()) }
    }

    @Test
    fun reviewLocationSuccess() {
        val p = partners.first { it.id == "REG2026-0148" }
        capture("07_review") { RegReviewScreen(p, {}, {}, {}) }
        capture("08_locating") { RegLocationScreen(LocationCaptureState(locating = true), false, {}, CaptureActions(), {}) }
        val found = LocationResult.Found(-5.1478, 119.4326, "Panakkukang, Kota Makassar, Sulawesi Selatan", false)
        capture("08_found") { RegLocationScreen(LocationCaptureState(found = found), false, {}, CaptureActions(), {}) }
        capture("09_success") { RegSuccessScreen("Toko Sinar Baru", "REG2026-0149", 1_791_400_000_000, {}) }
    }

    @Test
    fun partnerTabAndDetails() {
        val mine = partners.filter { it.status == PartnerStatus.ACTIVE && it.ownerTlId == 6L }
        val subs = partners.filter { it.ownerTlId == 6L }
        capture("01_mine") { PartnerTabScreen(PartnerTab.Mine, {}, mine, subs, null, {}, {}, {}, {}) }
        capture("01_submissions") { PartnerTabScreen(PartnerTab.Submissions, {}, mine, subs, null, {}, {}, {}, {}) }
        capture("01_sa") { PartnerTabScreen(PartnerTab.Mine, {}, mine.take(1), null, null, {}, {}, {}, {}) }
        val revision = partners.first { it.id == "REG2026-0144" }
        capture("10_revision") { SubmissionDetailScreen(revision, true, {}, {}, listOf(AdminAction(com.example.and_sales_portal.R.string.sim_admin_reject) {})) }
        capture("10_rejected") { SubmissionDetailScreen(partners.first { it.id == "REG2026-0135" }, true, {}, {}, null) }
        capture("prt_01_detail") { PartnerDetailScreen(partners.first { it.id == "REG2026-0139" }, true, {}, { _, _ -> }, {}) }
    }
}
