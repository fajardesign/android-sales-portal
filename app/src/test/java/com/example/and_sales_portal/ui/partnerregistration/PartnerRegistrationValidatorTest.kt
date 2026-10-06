package com.example.and_sales_portal.ui.partnerregistration

import android.net.Uri
import com.example.and_sales_portal.ui.partnerregistration.data.Bank
import com.example.and_sales_portal.ui.partnerregistration.data.Region
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Required-field rules of REG-02 … REG-06 and the document list per Jenis Badan Usaha. */
@RunWith(RobolectricTestRunner::class)
class PartnerRegistrationValidatorTest {

    private val region = Region("x", "X")

    @Test
    fun partnerInfoRequiresEveryField() {
        assertEquals(RegField.entries.take(8).toSet(), PartnerRegistrationValidator.missingPartnerInfo(PartnerInfoForm()))
        val full = PartnerInfoForm("Erafone", "Jln prof dr. satrio", region, region, region, region, "001", "002")
        assertTrue(PartnerRegistrationValidator.missingPartnerInfo(full).isEmpty())
        assertEquals(setOf(RegField.Rw), PartnerRegistrationValidator.missingPartnerInfo(full.copy(rw = "  ")))
    }

    @Test
    fun businessNeedsTextFieldsAndEveryChipGroup() {
        assertEquals(setOf(RegField.Email, RegField.Revenue, RegField.OutletCount), PartnerRegistrationValidator.missingBusiness(BusinessForm()))
        val chosen = RegChoice.business.associateWith { it.options.first().key }
        assertTrue(PartnerRegistrationValidator.missingChoices(chosen, RegChoice.business).isEmpty())
        assertEquals(setOf(RegChoice.OfflineChannel), PartnerRegistrationValidator.missingChoices(chosen - RegChoice.OfflineChannel, RegChoice.business))
    }

    @Test
    fun bankAccountNeedsBankAndNumber() {
        assertEquals(setOf(RegField.Bank, RegField.AccountNumber), PartnerRegistrationValidator.missingBankAccount(BankAccountForm()))
        assertTrue(PartnerRegistrationValidator.missingBankAccount(BankAccountForm(Bank("008", "Bank Mandiri"), "27485745748")).isEmpty())
    }

    @Test
    fun documentsDependOnEntityType() {
        assertEquals(7, RegDocument.requiredFor(ENTITY_PT).size)
        assertEquals(RegDocument.requiredFor(ENTITY_PT), RegDocument.requiredFor(ENTITY_CV))
        assertEquals(listOf(RegDocument.Ktp, RegDocument.Npwp, RegDocument.Passbook), RegDocument.requiredFor(ENTITY_INDIVIDUAL))
        assertTrue(RegDocument.requiredFor(null).isEmpty())
    }

    @Test
    fun submitNeedsEveryRequiredDocumentUploaded() {
        val required = RegDocument.requiredFor(ENTITY_INDIVIDUAL)
        val uri = Uri.parse("content://x")
        val uploaded = required.associateWith { DocumentUpload(uri, "a.jpg", 1, UploadStatus.Uploaded) }
        assertTrue(PartnerRegistrationValidator.documentsComplete(required, uploaded))
        val oneUploading = uploaded + (RegDocument.Npwp to DocumentUpload(uri, "b.jpg", 1, UploadStatus.Uploading))
        assertFalse(PartnerRegistrationValidator.documentsComplete(required, oneUploading))
        assertFalse(PartnerRegistrationValidator.documentsComplete(emptyList(), emptyMap()))
    }

    @Test
    fun submittedAtUsesFigmaFormat() {
        val millis = java.util.Calendar.getInstance().apply { set(2026, java.util.Calendar.SEPTEMBER, 29, 14, 32) }.timeInMillis
        assertEquals("29 Sep 2026, 14:32", formatSubmittedAt(millis))
    }
}
