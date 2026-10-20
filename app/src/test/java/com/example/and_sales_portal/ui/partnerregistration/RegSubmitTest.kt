package com.example.and_sales_portal.ui.partnerregistration

import androidx.test.core.app.ApplicationProvider
import com.example.and_sales_portal.data.auth.AppUser
import com.example.and_sales_portal.data.demo.DemoStore
import com.example.and_sales_portal.data.demo.PlainDraftCipher
import com.example.and_sales_portal.data.device.LocationResult
import com.example.and_sales_portal.data.partner.DocType
import com.example.and_sales_portal.data.partner.Keys
import com.example.and_sales_portal.data.partner.PartnerRepository
import com.example.and_sales_portal.data.partner.PartnerStatus
import com.example.and_sales_portal.ui.partnerregistration.data.FakePartnerRegistrationRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

/**
 * PRD Scope 1 v1.1 C5 (decision 2026-10-20): no consent — the summary and the location are enough to send, also when
 * resubmitting a revision; the "Formulir Persetujuan PDP" is a mandatory document for every entity instead.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RegSubmitTest {

    private val store = DemoStore(ApplicationProvider.getApplicationContext(), PlainDraftCipher())
    private val now = Instant.parse("2026-10-20T03:00:00Z")
    private val siti = AppUser(11, "Siti Rahmawati", "SR", email = "siti@demo", status = "ACTIVE", areaIds = listOf(1), supervisorId = 6)
    private lateinit var partners: PartnerRepository

    @Before
    fun setUp() {
        partners = PartnerRepository(store) { now }.also { it.reset() }
    }

    @Test
    fun kirimPengajuanWithoutConsent() {
        val vm = PartnerRegistrationViewModel(FakePartnerRegistrationRepository(latencyMillis = 0), partners, store, siti)
        vm.setLocation(LocationResult.Found(-5.1478, 119.4326, "Panakkukang, Kota Makassar", false))
        val before = partners.partners.value.size
        vm.submit()
        val id = requireNotNull(vm.state.value.submitted).registrationNumber
        assertEquals(before + 1, partners.partners.value.size)
        assertEquals(PartnerStatus.UNDER_REVIEW, partners.get(id)!!.status)
    }

    @Test
    fun kirimUlangWithoutConsent() {
        val p = partners.submit(partners.get("REG2026-0148")!!.copy(id = "", submittedBy = 11, submittedAt = now.toEpochMilli()))
        partners.adminRequestRevision(p.id)
        partners.resubmit(partners.get(p.id)!!.copy(accountNumber = "999"), 11)
        assertEquals(PartnerStatus.UNDER_REVIEW, partners.get(p.id)!!.status)
    }

    @Test
    fun pdpIsMandatoryForEveryEntity() {
        listOf(Keys.ENTITY_INDIVIDU, Keys.ENTITY_PT, Keys.ENTITY_CV).forEach { e ->
            assertTrue(DocType.forEntity(e, Keys.PIC_OWNER).contains(DocType.PDP to true))
        }
        assertEquals(5L * 1024 * 1024, DocType.PDP.maxBytes)
        assertTrue(!DocType.PDP.store)
        // Every mandatory document but the PDP uploaded: Lanjut Ringkasan waits for it.
        val docs = DocType.forEntity(Keys.ENTITY_INDIVIDU, Keys.PIC_OWNER).filter { it.second }.map { it.first }
        val list = DocType.forEntity(Keys.ENTITY_INDIVIDU, Keys.PIC_OWNER)
        val uploaded = docs.filter { it != DocType.PDP }.associateWith { DocumentUpload("/x.pdf", "x.pdf", 1, UploadStatus.Uploaded) }
        assertTrue(!PartnerRegistrationValidator.documentsComplete(list, uploaded))
        assertTrue(PartnerRegistrationValidator.documentsComplete(list, uploaded + (DocType.PDP to DocumentUpload("/pdp.pdf", "pdp.pdf", 1, UploadStatus.Uploaded))))
        // The sample partners carry it too (same as the web DOC_TYPES).
        assertNotNull(partners.get("REG2026-0148")!!.documents.firstOrNull { it.key == "PDP" })
    }
}
