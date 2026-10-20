package com.example.and_sales_portal.ui.partnerregistration

import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.partner.DocType
import com.example.and_sales_portal.data.partner.Keys
import com.example.and_sales_portal.ui.partnerregistration.data.Bank
import com.example.and_sales_portal.ui.partnerregistration.data.Region
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Field rules of PRD v3 C2 (REG-02 … REG-06) and the document table. */
class PartnerRegistrationValidatorTest {

    private val region = Region("x", "X")
    private val full = PartnerInfoForm("Erafone", "Jl. Satrio 1", region, region, region, region, "001", "002")

    @Test
    fun partnerInfoRules() {
        assertEquals(RegField.entries.take(8).toSet(), PartnerRegistrationValidator.partnerInfo(PartnerInfoForm()).keys)
        assertTrue(PartnerRegistrationValidator.partnerInfo(full).isEmpty())
        assertEquals(FieldError(R.string.reg_err_min, 3), PartnerRegistrationValidator.partnerInfo(full.copy(name = "Ab"))[RegField.PartnerName])
        assertEquals(FieldError(R.string.reg_err_min, 5), PartnerRegistrationValidator.partnerInfo(full.copy(address = "Jl 1"))[RegField.Address])
        assertEquals(FieldError(R.string.reg_err_digits3), PartnerRegistrationValidator.partnerInfo(full.copy(rt = "1"))[RegField.Rt])
        assertEquals(FieldError(R.string.reg_required), PartnerRegistrationValidator.partnerInfo(full.copy(rw = " "))[RegField.Rw])
        // PRD Scope 1 v2 C2: Kode Referral is no longer asked.
        assertTrue(PartnerRegistrationValidator.partnerInfo(full).keys.isEmpty())
    }

    @Test
    fun businessRules() {
        assertEquals(setOf(RegField.Email, RegField.Revenue, RegField.OutletCount), PartnerRegistrationValidator.business(BusinessForm()).keys)
        val ok = BusinessForm("toko@mail.com", "1000000", "1")
        assertTrue(PartnerRegistrationValidator.business(ok).isEmpty())
        assertEquals(FieldError(R.string.reg_err_email), PartnerRegistrationValidator.business(ok.copy(email = "toko@"))[RegField.Email])
        assertEquals(FieldError(R.string.reg_err_revenue), PartnerRegistrationValidator.business(ok.copy(revenue = "0"))[RegField.Revenue])
        assertEquals(FieldError(R.string.reg_err_outlet), PartnerRegistrationValidator.business(ok.copy(outletCount = "0"))[RegField.OutletCount])
    }

    @Test
    fun offlineChannelOnlyForOfflineStores() {
        val online = mapOf(RegChoice.StoreType to Keys.STORE_ONLINE)
        assertFalse(RegChoice.OfflineChannel in RegChoice.business(online))
        assertTrue(RegChoice.OfflineChannel in RegChoice.business(mapOf(RegChoice.StoreType to Keys.STORE_OFFLINE)))
    }

    @Test
    fun picAndBankRules() {
        val pic = PicForm("Budi", "budi@mail.com", "81234567890")
        assertTrue(PartnerRegistrationValidator.pic(pic).isEmpty())
        assertTrue(PartnerRegistrationValidator.pic(pic.copy(privyId = "")).isEmpty())
        // PRD Scope 1 v2 C3: Email PIC stays required (the PKS goes to it or to the Privy ID).
        assertEquals(setOf(RegField.PicEmail), PartnerRegistrationValidator.pic(pic.copy(email = "x")).keys)
        assertEquals(setOf(RegField.Bank, RegField.Branch, RegField.AccountNumber, RegField.AccountName), PartnerRegistrationValidator.bank(BankForm()).keys)
        assertTrue(PartnerRegistrationValidator.bank(BankForm(Bank("BCA", "BCA"), "KCP Makassar", "1234567890", "Budi")).isEmpty())
    }

    @Test
    fun documentTable() {
        val individu = DocType.forEntity(Keys.ENTITY_INDIVIDU, Keys.PIC_OWNER).map { it.first }
        assertEquals(listOf(DocType.KTP_PIC, DocType.NPWP, DocType.NIB, DocType.BUKU_REKENING, DocType.PDP, DocType.FOTO_DEPAN, DocType.FOTO_SAMPING), individu)
        assertTrue(DocType.KTP_OWNER in DocType.forEntity(Keys.ENTITY_INDIVIDU, Keys.PIC_KARYAWAN).map { it.first })
        val company = DocType.forEntity(Keys.ENTITY_PT, Keys.PIC_KARYAWAN)
        assertEquals(12, company.size)
        assertFalse(company.first { it.first == DocType.NIB }.second)
        assertTrue(DocType.forEntity(null, null).isEmpty())
    }

    @Test
    fun documentsCompleteIgnoresOptional() {
        val list = DocType.forEntity(Keys.ENTITY_INDIVIDU, Keys.PIC_OWNER)
        val uploaded = list.filter { it.second }.associate { it.first to DocumentUpload("/x", "a.jpg", 1, UploadStatus.Uploaded) }
        assertTrue(PartnerRegistrationValidator.documentsComplete(list, uploaded))
        val oneUploading = uploaded + (DocType.NIB to DocumentUpload("/y", "b.pdf", 1, UploadStatus.Uploading))
        assertFalse(PartnerRegistrationValidator.documentsComplete(list, oneUploading))
        assertFalse(PartnerRegistrationValidator.documentsComplete(list, uploaded - DocType.FOTO_DEPAN))
    }

    @Test
    fun fileRules() {
        assertNull(PartnerRegistrationValidator.file(DocType.AKTA, "akta.pdf", 5L * 1024 * 1024))
        assertEquals(FileError.TooLarge, PartnerRegistrationValidator.file(DocType.AKTA, "akta.pdf", 5L * 1024 * 1024 + 1))
        assertEquals(FileError.WrongType, PartnerRegistrationValidator.file(DocType.FOTO_DEPAN, "foto.pdf", 1000))
        assertEquals(FileError.TooLarge, PartnerRegistrationValidator.file(DocType.FOTO_DEPAN, "foto.jpg", 2L * 1024 * 1024 + 1))
        assertEquals(FileError.WrongType, PartnerRegistrationValidator.file(DocType.KTP_PIC, "ktp.docx", 1000))
    }
}
