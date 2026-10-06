package com.example.and_sales_portal.ui.partnerregistration

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.and_sales_portal.ui.partnerregistration.data.AccountInquiryResult
import com.example.and_sales_portal.ui.partnerregistration.data.Bank
import com.example.and_sales_portal.ui.partnerregistration.data.FakePartnerRegistrationRepository
import com.example.and_sales_portal.ui.partnerregistration.data.PartnerRegistrationRepository
import com.example.and_sales_portal.ui.partnerregistration.data.PartnerRegistrationSubmission
import com.example.and_sales_portal.ui.partnerregistration.data.Region
import com.example.and_sales_portal.ui.partnerregistration.data.UploadEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State of the whole Partner Registration flow (REG-02 … REG-06), scoped to `nav_partner_registration` so the data
 * survives moving between steps (CLAUDE.md §2).
 */
class PartnerRegistrationViewModel(private val repository: PartnerRegistrationRepository) : ViewModel() {

    private val _state = MutableStateFlow(PartnerRegistrationState())
    val state: StateFlow<PartnerRegistrationState> = _state.asStateFlow()

    private val uploads = mutableMapOf<RegDocument, Job>()
    private var regionJob: Job? = null
    private var inquiryJob: Job? = null

    init {
        viewModelScope.launch {
            val provinces = repository.provinces()
            _state.update { it.copy(regions = it.regions.copy(provinces = provinces)) }
        }
        viewModelScope.launch {
            val banks = repository.banks()
            _state.update { it.copy(banks = banks) }
        }
    }

    // region REG-02 Partner info

    fun setPartnerName(value: String) = updatePartner { it.copy(name = value) }
    fun setAddress(value: String) = updatePartner { it.copy(address = value) }
    fun setRt(value: String) = updatePartner { it.copy(rt = value) }
    fun setRw(value: String) = updatePartner { it.copy(rw = value) }

    /** Choosing a parent region clears its children and loads the next list (Figma: children stay disabled until then). */
    fun selectProvince(region: Region) {
        updatePartner { it.copy(province = region, regency = null, district = null, village = null) }
        loadRegions { regions -> regions.copy(regencies = repository.regencies(region.id), districts = emptyList(), villages = emptyList()) }
    }

    fun selectRegency(region: Region) {
        updatePartner { it.copy(regency = region, district = null, village = null) }
        loadRegions { regions -> regions.copy(districts = repository.districts(region.id), villages = emptyList()) }
    }

    fun selectDistrict(region: Region) {
        updatePartner { it.copy(district = region, village = null) }
        loadRegions { regions -> regions.copy(villages = repository.villages(region.id)) }
    }

    fun selectVillage(region: Region) = updatePartner { it.copy(village = region) }

    /** CTA "Lanjut Data Bisnis": `true` when the step is complete, otherwise its errors are shown. */
    fun submitPartnerInfo(): Boolean = complete(RegStep.PartnerInfo, PartnerRegistrationValidator.missingPartnerInfo(_state.value.partner).isEmpty())

    // endregion

    // region REG-03 Business and REG-04 PIC

    fun setEmail(value: String) = _state.update { it.copy(business = it.business.copy(email = value)) }
    fun setRevenue(value: String) = _state.update { it.copy(business = it.business.copy(revenue = value)) }
    fun setOutletCount(value: String) = _state.update { it.copy(business = it.business.copy(outletCount = value)) }
    fun choose(group: RegChoice, key: String) = _state.update { it.copy(choices = it.choices + (group to key)) }

    /** CTA "Lanjut Informasi PIC". */
    fun submitBusiness(): Boolean {
        val s = _state.value
        val ok = PartnerRegistrationValidator.missingBusiness(s.business).isEmpty() &&
            PartnerRegistrationValidator.missingChoices(s.choices, RegChoice.business).isEmpty()
        return complete(RegStep.Business, ok)
    }

    fun setPicName(value: String) = _state.update { it.copy(pic = it.pic.copy(name = value)) }
    fun setPicEmail(value: String) = _state.update { it.copy(pic = it.pic.copy(email = value)) }
    fun setPicPhone(value: String) = _state.update { it.copy(pic = it.pic.copy(phone = value)) }

    /** CTA "Lanjut Data Rekening". */
    fun submitPic(): Boolean {
        val s = _state.value
        val ok = PartnerRegistrationValidator.missingPic(s.pic).isEmpty() &&
            PartnerRegistrationValidator.missingChoices(s.choices, listOf(RegChoice.PicStatus)).isEmpty()
        return complete(RegStep.Pic, ok)
    }

    // endregion

    // region REG-05 Bank account

    /** Changing the bank or the number invalidates an earlier inquiry (Nama Rekening and the CTA hide again). */
    fun selectBank(bank: Bank) = updateBankAccount { it.copy(bank = bank, inquiry = AccountInquiry.Idle) }

    fun setAccountNumber(value: String) = updateBankAccount { it.copy(accountNumber = value, inquiry = AccountInquiry.Idle) }

    /** "Periksa": runs the inquiry, or shows the required errors when the bank or number is missing. */
    fun checkAccount() {
        val form = _state.value.bankAccount
        if (PartnerRegistrationValidator.missingBankAccount(form).isNotEmpty()) {
            _state.update { it.copy(showErrors = it.showErrors + RegStep.BankAccount) }
            return
        }
        val bank = form.bank ?: return
        updateBankAccount { it.copy(inquiry = AccountInquiry.Loading) }
        inquiryJob?.cancel()
        inquiryJob = viewModelScope.launch {
            val result = repository.inquireAccount(bank.code, form.accountNumber)
            updateBankAccount {
                it.copy(
                    inquiry = when (result) {
                        is AccountInquiryResult.Found -> AccountInquiry.Found(result.accountName)
                        AccountInquiryResult.NotFound -> AccountInquiry.NotFound
                    },
                )
            }
        }
    }

    /** CTA "Lanjut Dokumen Upload" (only shown after a successful inquiry). */
    fun submitBankAccount(): Boolean = complete(RegStep.BankAccount, _state.value.bankAccount.inquiry is AccountInquiry.Found)

    // endregion

    // region REG-06 Documents

    /** Starts uploading [uri] for [document]; [fileName] and [sizeBytes] come from the picked file. */
    fun upload(document: RegDocument, uri: Uri, fileName: String, sizeBytes: Long) {
        uploads.remove(document)?.cancel()
        setDocument(document, DocumentUpload(uri, fileName, sizeBytes, UploadStatus.Uploading))
        uploads[document] = viewModelScope.launch {
            try {
                repository.uploadDocument(uri) { event ->
                    when (event) {
                        is UploadEvent.Progress -> updateDocument(document) { it.copy(progress = event.fraction) }
                        is UploadEvent.Done -> updateDocument(document) {
                            it.copy(status = UploadStatus.Uploaded, progress = 1f, documentId = event.documentId)
                        }
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                updateDocument(document) { it.copy(status = UploadStatus.Failed) }
            }
        }
    }

    fun retryUpload(document: RegDocument) {
        val doc = _state.value.documents[document] ?: return
        upload(document, doc.uri, doc.fileName, doc.sizeBytes)
    }

    /** Delete (or cancel while uploading). */
    fun removeDocument(document: RegDocument) {
        uploads.remove(document)?.cancel()
        _state.update { it.copy(documents = it.documents - document) }
    }

    /** "Kirim Dokumen": enabled only when every required document is uploaded. */
    fun submitDocuments() {
        val s = _state.value
        if (s.submitting || !PartnerRegistrationValidator.documentsComplete(s.requiredDocuments, s.documents)) return
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            val result = repository.submit(s.toSubmission())
            _state.update { it.copy(submitting = false, submission = result) }
        }
    }

    // endregion

    private fun complete(step: RegStep, ok: Boolean): Boolean {
        if (!ok) _state.update { it.copy(showErrors = it.showErrors + step) }
        return ok
    }

    private fun updatePartner(transform: (PartnerInfoForm) -> PartnerInfoForm) =
        _state.update { it.copy(partner = transform(it.partner)) }

    private fun updateBankAccount(transform: (BankAccountForm) -> BankAccountForm) =
        _state.update { it.copy(bankAccount = transform(it.bankAccount)) }

    private fun setDocument(document: RegDocument, upload: DocumentUpload) =
        _state.update { it.copy(documents = it.documents + (document to upload)) }

    private fun updateDocument(document: RegDocument, transform: (DocumentUpload) -> DocumentUpload) =
        _state.update { s -> s.documents[document]?.let { s.copy(documents = s.documents + (document to transform(it))) } ?: s }

    private fun loadRegions(load: suspend (RegionLists) -> RegionLists) {
        regionJob?.cancel()
        regionJob = viewModelScope.launch {
            val regions = load(_state.value.regions)
            _state.update { it.copy(regions = regions) }
        }
    }

    private fun PartnerRegistrationState.toSubmission() = PartnerRegistrationSubmission(
        partnerName = partner.name,
        address = partner.address,
        villageId = partner.village?.id.orEmpty(),
        rt = partner.rt,
        rw = partner.rw,
        email = business.email,
        revenue = business.revenue.toLongOrNull() ?: 0L,
        outletCount = business.outletCount,
        business = RegChoice.business.associate { it.name to choices[it].orEmpty() },
        picName = pic.name,
        picEmail = pic.email,
        picPhone = pic.phone,
        picStatus = choices[RegChoice.PicStatus].orEmpty(),
        bankCode = bankAccount.bank?.code.orEmpty(),
        accountNumber = bankAccount.accountNumber,
        accountName = (bankAccount.inquiry as? AccountInquiry.Found)?.accountName.orEmpty(),
        documentIds = requiredDocuments.associate { it.name to documents[it]?.documentId.orEmpty() },
    )

    companion object {
        /** Uses [FakePartnerRegistrationRepository] until the mobile API exists (CLAUDE.md §1). */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { PartnerRegistrationViewModel(FakePartnerRegistrationRepository()) }
        }
    }
}
