package com.example.and_sales_portal.ui.partnerregistration

import android.net.Uri
import androidx.compose.runtime.Composable
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.partnerregistration.data.Bank
import com.example.and_sales_portal.ui.partnerregistration.data.Region
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** REG screens in the states of the Figma frames ("Error | Incomplete Information", "Complete", "Account Verified", …). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h760dp-mdpi")
class RegScreensScreenshotTest {

    private fun capture(name: String, content: @Composable () -> Unit) =
        captureRoboImage("build/outputs/roborazzi/reg_$name.png") { AmarTheme { content() } }

    private val errors = PartnerRegistrationState(showErrors = RegStep.entries.toSet())

    @Test
    fun partnerInfo() {
        capture("01_error") { RegPartnerInfoScreen(errors, PartnerInfoActions()) }
        val r = { name: String -> Region(name, name) }
        val complete = PartnerRegistrationState(
            partner = PartnerInfoForm("Erafone", "Jln prof dr. satrio", r("Sumatera Utara"), r("Kabupaten Deli Serdang"), r("Deli Tua"), r("Mekar Sari"), "000", "000"),
            regions = RegionLists(listOf(r("Sumatera Utara")), listOf(r("Kabupaten Deli Serdang")), listOf(r("Deli Tua")), listOf(r("Mekar Sari"))),
        )
        capture("01_complete") { RegPartnerInfoScreen(complete, PartnerInfoActions()) }
    }

    @Test
    fun businessAndPic() {
        capture("02_error") { RegBusinessScreen(errors, DetailsActions()) }
        capture("03_error") { RegPicScreen(errors, DetailsActions()) }
    }

    @Test
    fun bankAccount() {
        val bank = Bank("008", "Bank Mandiri")
        val verified = PartnerRegistrationState(bankAccount = BankAccountForm(bank, "27485745748", AccountInquiry.Found("BUDI SANTOSO")), banks = listOf(bank))
        capture("04_verified") { RegBankAccountScreen(verified, BankAccountActions()) }
        val notFound = verified.copy(bankAccount = verified.bankAccount.copy(inquiry = AccountInquiry.NotFound))
        capture("04_not_found") { RegBankAccountScreen(notFound, BankAccountActions()) }
    }

    @Test
    fun documents() {
        val uri = Uri.parse("content://x")
        val pt = PartnerRegistrationState(choices = mapOf(RegChoice.EntityType to ENTITY_PT))
        capture("05_pt") { RegDocumentsScreen(pt, DocumentsActions()) }
        val oneUploaded = pt.copy(documents = mapOf(RegDocument.Articles to DocumentUpload(uri, "Anggaran_Dasar_PT_Nusantara.pdf", 2_516_582, UploadStatus.Uploaded)))
        capture("05_one_uploaded") { RegDocumentsScreen(oneUploaded, DocumentsActions()) }
        val individual = PartnerRegistrationState(choices = mapOf(RegChoice.EntityType to ENTITY_INDIVIDUAL))
        capture("05_individual") { RegDocumentsScreen(individual, DocumentsActions()) }
    }

    @Test
    fun success() {
        val millis = java.util.Calendar.getInstance().apply { set(2026, java.util.Calendar.SEPTEMBER, 29, 14, 32) }.timeInMillis
        capture("06_success") { RegSuccessScreen("PT Sumber Niaga Jaya", "REG-2026-1048", millis, {}) }
    }
}
