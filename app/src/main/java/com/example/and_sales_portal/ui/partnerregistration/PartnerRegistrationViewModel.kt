package com.example.and_sales_portal.ui.partnerregistration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.and_sales_portal.data.auth.AppUser
import com.example.and_sales_portal.data.demo.DemoStore
import com.example.and_sales_portal.data.device.LocationResult
import com.example.and_sales_portal.data.partner.DocType
import com.example.and_sales_portal.data.partner.Keys
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.PartnerDoc
import com.example.and_sales_portal.data.partner.PartnerRepository
import com.example.and_sales_portal.data.partner.PartnerStatus
import com.example.and_sales_portal.data.partner.RevisionItem
import com.example.and_sales_portal.data.partner.Sections
import com.example.and_sales_portal.data.partner.Store
import com.example.and_sales_portal.data.partner.Verification
import com.example.and_sales_portal.ui.partnerregistration.data.Bank
import com.example.and_sales_portal.ui.partnerregistration.data.PartnerRegistrationRepository
import com.example.and_sales_portal.ui.partnerregistration.data.Region
import com.example.and_sales_portal.ui.partnerregistration.data.UploadEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

/** A new registration (REG-02 … REG-09), or fixing a Revision Required submission (REG-11). */
sealed interface RegMode {
    data object New : RegMode
    data class Revision(val partnerId: String) : RegMode
}

/** Revision state (REG-11): the flagged items and what has been fixed. */
data class RevisionState(
    val partner: Partner,
    val items: List<RevisionItem>,
    /** Steps whose flagged section was saved. */
    val fixed: Set<RegStep> = emptySet(),
    /** Section being edited, or `null` for the list of items. */
    val editing: RegStep? = null,
) {
    /** Steps that hold a flagged section ("business" and "store" are both on Data Bisnis). */
    val sectionSteps: Set<RegStep> get() = items.filter { it.kind == RevisionItem.SEC }.mapNotNull { stepOf(it.ref) }.toSet()
    val flaggedDocs: List<DocType> get() = items.filter { it.kind == RevisionItem.DOC }.mapNotNull { DocType.of(it.ref) }

    companion object {
        fun stepOf(section: String): RegStep? = when (section) {
            Sections.PARTNER -> RegStep.PartnerInfo
            Sections.BUSINESS, Sections.STORE -> RegStep.Business
            Sections.PIC -> RegStep.Pic
            Sections.BANK -> RegStep.BankAccount
            else -> null
        }
    }
}

/**
 * State of the Partner Registration flow (PRD v3 C2), scoped to `nav_partner_registration` so the data survives moving
 * between steps (CLAUDE.md §2). Each completed step is saved as a local draft; "Kirim Pengajuan" sends it to
 * [PartnerRepository] with the location from REG-08.
 */
class PartnerRegistrationViewModel(
    private val repository: PartnerRegistrationRepository,
    private val partners: PartnerRepository,
    private val store: DemoStore,
    private val user: AppUser,
    private val mode: RegMode = RegMode.New,
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }
    private val _state = MutableStateFlow(PartnerRegistrationState())
    val state: StateFlow<PartnerRegistrationState> = _state.asStateFlow()

    private val _revision = MutableStateFlow<RevisionState?>(null)
    val revision: StateFlow<RevisionState?> = _revision.asStateFlow()

    private val uploads = mutableMapOf<DocType, Job>()
    private var regionJob: Job? = null

    init {
        viewModelScope.launch {
            val provinces = repository.provinces()
            _state.update { it.copy(regions = it.regions.copy(provinces = provinces)) }
            if (mode is RegMode.Revision) loadRegionsFor(_state.value.partner)
        }
        viewModelScope.launch {
            val banks = repository.banks()
            _state.update { it.copy(banks = banks) }
        }
        when (mode) {
            RegMode.New -> loadDraft()?.let { d -> _state.update { it.copy(draftOffer = d.partner.name.ifBlank { "" }) } }
            is RegMode.Revision -> partners.get(mode.partnerId)?.let { p ->
                _state.value = fromPartner(p)
                _revision.value = RevisionState(p, p.revision?.items.orEmpty())
            }
        }
    }

    // region Draft

    private fun loadDraft(): RegDraft? = store.loadDraft(user.id)?.let { runCatching { json.decodeFromString(RegDraft.serializer(), it) }.getOrNull() }

    /** "Lanjutkan": restores the saved steps. */
    fun acceptDraft() {
        val d = loadDraft()
        if (d == null) {
            _state.update { it.copy(draftOffer = null) }
            return
        }
        _state.update { s ->
            s.copy(
                draftOffer = null,
                partner = d.partner,
                business = d.business,
                pic = d.pic,
                bank = d.bank,
                choices = d.choices.mapNotNull { (k, v) -> runCatching { RegChoice.valueOf(k) }.getOrNull()?.let { it to v } }.toMap(),
                documents = d.documents.mapNotNull { (k, v) ->
                    DocType.of(k)?.takeIf { v.status == UploadStatus.Uploaded && File(v.path).exists() }?.let { it to v }
                }.toMap(),
            )
        }
        viewModelScope.launch { loadRegionsFor(_state.value.partner) }
    }

    /** "Mulai Baru". */
    fun discardDraft() {
        store.deleteDraft(user.id)
        _state.update { it.copy(draftOffer = null) }
    }

    private fun saveDraft() {
        if (mode != RegMode.New) return
        val s = _state.value
        val d = RegDraft(s.partner, s.business, s.pic, s.bank, s.choices.mapKeys { it.key.name }, s.documents.filterValues { it.status == UploadStatus.Uploaded }.mapKeys { it.key.name })
        store.saveDraft(user.id, json.encodeToString(RegDraft.serializer(), d))
    }

    // endregion

    // region REG-02 Informasi Partner

    fun setPartnerName(value: String) = updatePartner { it.copy(name = value.take(100)) }
    fun setAddress(value: String) = updatePartner { it.copy(address = value.take(255)) }
    fun setRt(value: String) = updatePartner { it.copy(rt = value.filter(Char::isDigit).take(3)) }
    fun setRw(value: String) = updatePartner { it.copy(rw = value.filter(Char::isDigit).take(3)) }
    fun setReferralCode(value: String) {
        _state.update { it.copy(taken = it.taken - RegField.ReferralCode) }
        updatePartner { it.copy(referralCode = value) }
    }

    /** Choosing a parent region clears its children and loads the next list (children stay disabled until then). */
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

    // endregion

    // region REG-03 Data Bisnis, REG-04 Informasi PIC & PKS, REG-05 Data Rekening

    fun setEmail(value: String) = _state.update { it.copy(business = it.business.copy(email = value.trim())) }
    fun setRevenue(value: String) = _state.update { it.copy(business = it.business.copy(revenue = value)) }
    fun setOutletCount(value: String) = _state.update { it.copy(business = it.business.copy(outletCount = value.filter(Char::isDigit).take(4))) }

    /** Choosing Tipe Toko = Online removes Channel Offline (PRD C2: only for OFFLINE). */
    fun choose(group: RegChoice, key: String) = _state.update { s ->
        var choices = s.choices + (group to key)
        if (group == RegChoice.StoreType && key != Keys.STORE_OFFLINE) choices = choices - RegChoice.OfflineChannel
        s.copy(choices = choices)
    }

    fun setPicName(value: String) = updatePic { it.copy(name = value) }

    /** Email undangan PKS follows Email PIC until it is edited (PRD C2 "defaults to Email PIC"). */
    fun setPicEmail(value: String) = updatePic { it.copy(email = value.trim(), pksEmail = if (it.pksEmailEdited) it.pksEmail else value.trim()) }
    fun setPicPhone(value: String) {
        _state.update { it.copy(taken = it.taken - RegField.PicPhone) }
        updatePic { it.copy(phone = value) }
    }
    fun setPrivyId(value: String) = updatePic { it.copy(privyId = value.trim()) }
    fun setPksEmail(value: String) = updatePic { it.copy(pksEmail = value.trim(), pksEmailEdited = true) }

    fun selectBank(bank: Bank) = updateBank { it.copy(bank = bank) }
    fun setBranch(value: String) = updateBank { it.copy(branch = value) }
    fun setAccountNumber(value: String) = updateBank { it.copy(accountNumber = value.filter(Char::isDigit).take(20)) }
    fun setAccountName(value: String) = updateBank { it.copy(accountName = value) }

    // endregion

    /** CTA of a form step: `true` when the step is valid (and the draft is saved), otherwise its errors are shown. */
    fun submitStep(step: RegStep): Boolean {
        val s = _state.value
        val except = (mode as? RegMode.Revision)?.partnerId
        val taken = when (step) {
            RegStep.PartnerInfo -> setOfNotNull(RegField.ReferralCode.takeIf { partners.referralTaken(s.partner.referralCode, except) })
            RegStep.Pic -> setOfNotNull(RegField.PicPhone.takeIf { partners.picPhoneTaken(s.pic.phone, except) })
            else -> emptySet()
        }
        val ok = stepValid(step, s) && taken.isEmpty()
        _state.update { it.copy(taken = it.taken + taken) }
        if (!ok) _state.update { it.copy(showErrors = it.showErrors + step) } else saveDraft()
        return ok
    }

    // region REG-06 Dokumen Upload

    /** A picked or captured file (already copied to app storage). Checks type and size, then uploads it. */
    fun attach(document: DocType, path: String, fileName: String, sizeBytes: Long) {
        PartnerRegistrationValidator.file(document, fileName, sizeBytes)?.let { error ->
            File(path).delete()
            _state.update { it.copy(fileErrors = it.fileErrors + (document to error)) }
            return
        }
        _state.update { it.copy(fileErrors = it.fileErrors - document) }
        upload(document, DocumentUpload(path, fileName, sizeBytes, UploadStatus.Uploading))
    }

    private fun upload(document: DocType, doc: DocumentUpload) {
        uploads.remove(document)?.cancel()
        _state.value.documents[document]?.takeIf { it.path != doc.path }?.let { File(it.path).delete() }
        setDocument(document, doc.copy(status = UploadStatus.Uploading, progress = 0f))
        uploads[document] = viewModelScope.launch {
            try {
                repository.uploadDocument(doc.path) { event ->
                    when (event) {
                        is UploadEvent.Progress -> updateDocument(document) { it.copy(progress = event.fraction) }
                        UploadEvent.Done -> {
                            updateDocument(document) { it.copy(status = UploadStatus.Uploaded, progress = 1f) }
                            saveDraft()
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateDocument(document) { it.copy(status = UploadStatus.Failed) }
            }
        }
    }

    fun retryUpload(document: DocType) {
        _state.value.documents[document]?.let { upload(document, it) }
    }

    /** Delete (or cancel while uploading). */
    fun removeDocument(document: DocType) {
        uploads.remove(document)?.cancel()
        _state.value.documents[document]?.let { File(it.path).delete() }
        _state.update { it.copy(documents = it.documents - document, fileErrors = it.fileErrors - document) }
        saveDraft()
    }

    /** CTA "Lanjut Ringkasan": enabled when every mandatory document is uploaded (PRD C2). */
    fun documentsReady(): Boolean = _state.value.let { PartnerRegistrationValidator.documentsComplete(it.documentList, it.documents) }

    // endregion

    // region REG-08 Lokasi usaha and submit

    fun setLocation(found: LocationResult.Found?) = _state.update { it.copy(location = found) }

    /** REG-07: what will be sent, as the Admin will see it. */
    fun preview(): Partner = _state.value.toPartner(partners.now(), _state.value.location)

    /** "Kirim Pengajuan": REGYYYY-NNNN, status Under Review, routed to the Admin (PRD C2 step 6). */
    fun submit() {
        val s = _state.value
        val location = s.location ?: return
        if (s.submitting || s.submitted != null || location.mock) return
        _state.update { it.copy(submitting = true) }
        val p = partners.submit(s.toPartner(partners.now(), location))
        store.deleteDraft(user.id)
        _state.update { it.copy(submitting = false, submitted = Submitted(p.registrationNumber, p.name, p.submittedAt)) }
    }

    // endregion

    // region REG-11 Perbaiki & Kirim Ulang

    fun editSection(step: RegStep?) = _revision.update { it?.copy(editing = step) }

    /** "Simpan Perubahan" of a flagged section. */
    fun saveSection(step: RegStep): Boolean {
        if (!submitStep(step)) return false
        _revision.update { it?.copy(fixed = it.fixed + step, editing = null) }
        return true
    }

    /** "Kirim Ulang" is enabled when every flagged section is saved and every flagged document uploaded again. */
    fun revisionReady(): Boolean {
        val r = _revision.value ?: return false
        val s = _state.value
        return r.fixed.containsAll(r.sectionSteps) &&
            r.flaggedDocs.all { s.documents[it]?.status == UploadStatus.Uploaded }
    }

    fun resubmit(): Boolean {
        val r = _revision.value ?: return false
        if (!revisionReady()) return false
        val s = _state.value
        val now = partners.now()
        val sections = r.items.filter { it.kind == RevisionItem.SEC }.map { it.ref }.toSet()
        var p = r.partner
        val formPartner = s.toPartner(now, null)
        if (Sections.PARTNER in sections) {
            p = p.copy(name = formPartner.name, address = formPartner.address, province = formPartner.province, regency = formPartner.regency,
                district = formPartner.district, village = formPartner.village, rt = formPartner.rt, rw = formPartner.rw, referralCode = formPartner.referralCode)
        }
        if (Sections.BUSINESS in sections || Sections.STORE in sections) {
            val form = formPartner.stores.first()
            p = p.copy(email = formPartner.email, outletCount = formPartner.outletCount, entity = formPartner.entity, channel = formPartner.channel,
                stores = p.stores.map { st ->
                    if (st.primary) st.copy(omzet = form.omzet, storeType = form.storeType, channelOffline = form.channelOffline, scale = form.scale,
                        productSold = form.productSold, location = form.location, productType = form.productType) else st
                })
        }
        if (Sections.PIC in sections) {
            p = p.copy(picName = formPartner.picName, picEmail = formPartner.picEmail, picPhone = formPartner.picPhone, picStatus = formPartner.picStatus,
                privyId = formPartner.privyId, pksInviteEmail = formPartner.pksInviteEmail)
        }
        if (Sections.BANK in sections) {
            p = p.copy(bankCode = formPartner.bankCode, bankName = formPartner.bankName, bankBranch = formPartner.bankBranch,
                accountNumber = formPartner.accountNumber, accountName = formPartner.accountName)
        }
        // PRD C3: revised documents become a new version.
        val replaced = r.flaggedDocs.mapNotNull { d -> s.documents[d]?.let { d to it } }.toMap()
        p = p.copy(documents = p.documents.map { doc ->
            DocType.of(doc.key)?.let(replaced::get)?.let { up ->
                doc.copy(fileName = up.fileName, path = up.path, sizeBytes = up.sizeBytes, version = doc.version + 1, verification = Verification.UNVERIFIED, note = null, uploadedAt = now)
            } ?: doc
        })
        partners.resubmit(p, user.id)
        return true
    }

    // endregion

    override fun onCleared() {
        // Files of a flow that was neither submitted nor saved as a draft are not kept.
        if (mode is RegMode.Revision && _state.value.submitted == null) {
            _state.value.documents.values.forEach { File(it.path).delete() }
        }
    }

    private fun PartnerRegistrationState.toPartner(now: Long, location: LocationResult.Found?): Partner {
        val f = partner
        val area = listOfNotNull(f.village?.name, f.district?.name, f.regency?.name, f.province?.name).joinToString()
        val lat = location?.lat ?: 0.0
        val lng = location?.lng ?: 0.0
        val primary = Store(
            id = "",
            name = f.name.trim(),
            address = "${f.address.trim()}, $area",
            province = f.province?.name.orEmpty(), regency = f.regency?.name.orEmpty(), district = f.district?.name.orEmpty(), village = f.village?.name.orEmpty(),
            rt = f.rt, rw = f.rw, lat = lat, lng = lng,
            omzet = business.revenue.toLongOrNull() ?: 0L,
            storeType = choices[RegChoice.StoreType].orEmpty(),
            channelOffline = choices[RegChoice.OfflineChannel],
            scale = choices[RegChoice.StoreScale].orEmpty(),
            productSold = choices[RegChoice.ProductSold].orEmpty(),
            location = choices[RegChoice.StoreLocation].orEmpty(),
            productType = choices[RegChoice.ProductType].orEmpty(),
            primary = true,
            addedBy = user.id,
            addedAt = now,
        )
        return Partner(
            id = "",
            name = f.name.trim(),
            entity = entityType.orEmpty(),
            address = f.address.trim(),
            province = f.province?.name.orEmpty(), regency = f.regency?.name.orEmpty(), district = f.district?.name.orEmpty(), village = f.village?.name.orEmpty(),
            rt = f.rt, rw = f.rw,
            referralCode = f.referralCode.trim(),
            email = business.email.trim(),
            channel = choices[RegChoice.Channel].orEmpty(),
            outletCount = business.outletCount.toIntOrNull() ?: 1,
            picName = pic.name.trim(), picEmail = pic.email.trim(), picPhone = pic.phone,
            picStatus = choices[RegChoice.PicStatus].orEmpty(),
            privyId = pic.privyId.ifBlank { null }, pksInviteEmail = pic.pksEmail.ifBlank { pic.email },
            bankCode = bank.bank?.code.orEmpty(), bankName = bank.bank?.name.orEmpty(), bankBranch = bank.branch.trim(),
            accountNumber = bank.accountNumber, accountName = bank.accountName.trim(),
            areaId = user.areaIds.firstOrNull() ?: 0,
            submittedBy = user.id,
            submittedAt = now,
            status = PartnerStatus.UNDER_REVIEW,
            statusUpdatedAt = now,
            lat = lat, lng = lng,
            stores = listOf(primary),
            documents = documents.filterValues { it.status == UploadStatus.Uploaded }.map { (d, up) ->
                PartnerDoc(d.name, up.fileName, up.path, up.sizeBytes, uploadedAt = now)
            },
            history = emptyList(),
        )
    }

    /** Form state of an existing partner (revision). Documents start empty: only flagged ones are uploaded again. */
    private fun fromPartner(p: Partner): PartnerRegistrationState {
        val st = p.primaryStore
        val choices = buildMap {
            put(RegChoice.EntityType, p.entity)
            put(RegChoice.Channel, p.channel)
            put(RegChoice.PicStatus, p.picStatus)
            st?.let {
                put(RegChoice.StoreType, it.storeType)
                put(RegChoice.StoreScale, it.scale)
                put(RegChoice.ProductSold, it.productSold)
                put(RegChoice.StoreLocation, it.location)
                put(RegChoice.ProductType, it.productType)
                it.channelOffline?.takeIf { _ -> it.storeType == Keys.STORE_OFFLINE }?.let { c -> put(RegChoice.OfflineChannel, c) }
            }
        }.filterValues { it.isNotEmpty() }
        return PartnerRegistrationState(
            partner = PartnerInfoForm(p.name, p.address, Region(p.province, p.province), Region(p.regency, p.regency), Region(p.district, p.district), Region(p.village, p.village), p.rt, p.rw, p.referralCode.orEmpty()),
            business = BusinessForm(p.email, (st?.omzet ?: 0L).toString(), p.outletCount.toString()),
            pic = PicForm(p.picName, p.picEmail, p.picPhone, p.privyId.orEmpty(), p.pksInviteEmail ?: p.picEmail, pksEmailEdited = true),
            bank = BankForm(Bank(p.bankCode, p.bankName), p.bankBranch, p.accountNumber, p.accountName),
            choices = choices,
        )
    }

    /** Loads the region lists of a filled form (draft or revision), matching the saved names. */
    private suspend fun loadRegionsFor(form: PartnerInfoForm) {
        val provinces = _state.value.regions.provinces.ifEmpty { repository.provinces() }
        val province = provinces.firstOrNull { it.name == form.province?.name } ?: return
        val regencies = repository.regencies(province.id)
        val regency = regencies.firstOrNull { it.name == form.regency?.name }
        val districts = regency?.let { repository.districts(it.id) }.orEmpty()
        val district = districts.firstOrNull { it.name == form.district?.name }
        val villages = district?.let { repository.villages(it.id) }.orEmpty()
        val village = villages.firstOrNull { it.name == form.village?.name }
        _state.update {
            it.copy(
                regions = RegionLists(provinces, regencies, districts, villages),
                partner = it.partner.copy(province = province, regency = regency ?: it.partner.regency, district = district ?: it.partner.district, village = village ?: it.partner.village),
            )
        }
    }

    private fun updatePartner(transform: (PartnerInfoForm) -> PartnerInfoForm) = _state.update { it.copy(partner = transform(it.partner)) }
    private fun updatePic(transform: (PicForm) -> PicForm) = _state.update { it.copy(pic = transform(it.pic)) }
    private fun updateBank(transform: (BankForm) -> BankForm) = _state.update { it.copy(bank = transform(it.bank)) }

    private fun setDocument(document: DocType, upload: DocumentUpload) =
        _state.update { it.copy(documents = it.documents + (document to upload)) }

    private fun updateDocument(document: DocType, transform: (DocumentUpload) -> DocumentUpload) =
        _state.update { s -> s.documents[document]?.let { s.copy(documents = s.documents + (document to transform(it))) } ?: s }

    private fun loadRegions(load: suspend (RegionLists) -> RegionLists) {
        regionJob?.cancel()
        regionJob = viewModelScope.launch {
            val regions = load(_state.value.regions)
            _state.update { it.copy(regions = regions) }
        }
    }

    companion object {
        /** Whether a step is complete (REG-02 … REG-06). */
        fun stepValid(step: RegStep, s: PartnerRegistrationState): Boolean = when (step) {
            RegStep.PartnerInfo -> PartnerRegistrationValidator.partnerInfo(s.partner).isEmpty()
            RegStep.Business -> PartnerRegistrationValidator.business(s.business).isEmpty() &&
                PartnerRegistrationValidator.missingChoices(s.choices, RegChoice.business(s.choices)).isEmpty()
            RegStep.Pic -> PartnerRegistrationValidator.pic(s.pic).isEmpty() && s.choices[RegChoice.PicStatus] != null
            RegStep.BankAccount -> PartnerRegistrationValidator.bank(s.bank).isEmpty()
            RegStep.Documents -> PartnerRegistrationValidator.documentsComplete(s.documentList, s.documents)
        }
    }
}
