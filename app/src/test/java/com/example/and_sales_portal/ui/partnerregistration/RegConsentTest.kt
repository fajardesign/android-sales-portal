package com.example.and_sales_portal.ui.partnerregistration

import androidx.test.core.app.ApplicationProvider
import com.example.and_sales_portal.data.auth.AppUser
import com.example.and_sales_portal.data.demo.DemoStore
import com.example.and_sales_portal.data.demo.PlainDraftCipher
import com.example.and_sales_portal.data.device.LocationResult
import com.example.and_sales_portal.data.partner.Consent
import com.example.and_sales_portal.data.partner.PartnerRepository
import com.example.and_sales_portal.ui.partnerregistration.data.FakePartnerRegistrationRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

/** PRD Scope 1 v2 C5: the REG-08 consent is required and saved (time, version, submitter) with the first submission only. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RegConsentTest {

    private val store = DemoStore(ApplicationProvider.getApplicationContext(), PlainDraftCipher())
    private val now = Instant.parse("2026-10-16T03:00:00Z")
    private val siti = AppUser(11, "Siti Rahmawati", "SR", email = "siti@demo", status = "ACTIVE", areaIds = listOf(1), supervisorId = 6)
    private lateinit var partners: PartnerRepository

    @Before
    fun setUp() {
        partners = PartnerRepository(store) { now }.also { it.reset() }
    }

    @Test
    fun kirimPengajuanNeedsTheConsentAndSavesIt() {
        val vm = PartnerRegistrationViewModel(FakePartnerRegistrationRepository(), partners, store, siti)
        vm.setLocation(LocationResult.Found(-5.1478, 119.4326, "Panakkukang, Kota Makassar", false))
        val before = partners.partners.value.size
        vm.submit()
        assertNull(vm.state.value.submitted)
        assertEquals(before, partners.partners.value.size)

        vm.setConsent(true)
        vm.submit()
        val id = requireNotNull(vm.state.value.submitted).registrationNumber
        assertEquals(Consent(now.toEpochMilli(), "1.0", 11), partners.get(id)!!.consent)
    }

    @Test
    fun kirimUlangKeepsTheFirstConsent() {
        val first = Consent(now.toEpochMilli() - 86_400_000, Consent.VERSION, 11)
        val p = partners.submit(partners.get("REG2026-0148")!!.copy(id = "", submittedBy = 11, submittedAt = first.at, consent = first))
        partners.adminRequestRevision(p.id)
        partners.resubmit(partners.get(p.id)!!.copy(accountNumber = "999"), 11)
        assertEquals(first, partners.get(p.id)!!.consent)
        // Revision mode does not ask again: the REG-11 state starts without the REG-08 checkbox.
        val vm = PartnerRegistrationViewModel(FakePartnerRegistrationRepository(), partners, store, siti, RegMode.Revision(p.id))
        assertNotNull(vm.revision.value)
    }
}
