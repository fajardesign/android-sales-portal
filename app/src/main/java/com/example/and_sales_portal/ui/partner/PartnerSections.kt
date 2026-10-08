package com.example.and_sales_portal.ui.partner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.partner.DocType
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.PartnerDoc
import com.example.and_sales_portal.data.partner.PartnerStatus
import com.example.and_sales_portal.data.partner.Store
import com.example.and_sales_portal.data.partner.Verification
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadge
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeSize
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeType
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.common.InfoRow
import com.example.and_sales_portal.ui.common.SectionTitle
import com.example.and_sales_portal.ui.partnerregistration.RegChoice
import com.example.and_sales_portal.ui.partnerregistration.RegStep
import java.text.NumberFormat
import java.util.Locale

/** Status badge with the web portal labels (decision 2026-10-07). */
@Composable
fun PartnerStatusBadge(status: PartnerStatus, size: AmarBadgeSize = AmarBadgeSize.Small) {
    val (label, type) = when (status) {
        PartnerStatus.UNDER_REVIEW -> R.string.status_under_review to AmarBadgeType.Info
        PartnerStatus.REVISION_REQUIRED -> R.string.status_revision_required to AmarBadgeType.Warning
        PartnerStatus.VERIFIED -> R.string.status_verified to AmarBadgeType.Info
        PartnerStatus.WAITING_PKS -> R.string.status_waiting_pks to AmarBadgeType.Info
        PartnerStatus.ACTIVE -> R.string.status_active to AmarBadgeType.Success
        PartnerStatus.REJECTED -> R.string.status_rejected to AmarBadgeType.Error
        PartnerStatus.CANCELLED -> R.string.status_cancelled to AmarBadgeType.Error
        PartnerStatus.INACTIVE -> R.string.status_inactive to AmarBadgeType.Error
    }
    AmarBadge(stringResource(label), type = type, size = size)
}

fun statusLabel(status: PartnerStatus): Int = when (status) {
    PartnerStatus.UNDER_REVIEW -> R.string.status_under_review
    PartnerStatus.REVISION_REQUIRED -> R.string.status_revision_required
    PartnerStatus.VERIFIED -> R.string.status_verified
    PartnerStatus.WAITING_PKS -> R.string.status_waiting_pks
    PartnerStatus.ACTIVE -> R.string.status_active
    PartnerStatus.REJECTED -> R.string.status_rejected
    PartnerStatus.CANCELLED -> R.string.status_cancelled
    PartnerStatus.INACTIVE -> R.string.status_inactive
}

/** "Rp 120.000.000" (id-ID). */
fun rupiah(amount: Long): String = "Rp " + NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount)

@Composable
private fun choice(group: RegChoice, key: String?): String = group.labelOf(key)?.let { stringResource(it) } ?: "-"

/**
 * Data of a partner by section (Informasi Partner, Data Bisnis, Informasi PIC & PKS, Data Rekening, Dokumen): REG-07
 * Ringkasan (with "Ubah" per section) and REG-10 Detail Pengajuan (with document verification).
 */
@Composable
fun PartnerSections(partner: Partner, onEdit: ((RegStep) -> Unit)? = null, showVerification: Boolean = false) {
    val store = partner.primaryStore
    Section(R.string.sec_partner, onEdit?.let { { it(RegStep.PartnerInfo) } }) {
        InfoRow(stringResource(R.string.reg_partner_name), partner.name)
        InfoRow(stringResource(R.string.reg_address), partner.address)
        InfoRow(stringResource(R.string.reg_village), "${partner.village}, ${partner.district}")
        InfoRow(stringResource(R.string.reg_regency), "${partner.regency}, ${partner.province}")
        InfoRow("${stringResource(R.string.reg_rt)} / ${stringResource(R.string.reg_rw)}", "${partner.rt} / ${partner.rw}")
    }
    Section(R.string.sec_business, onEdit?.let { { it(RegStep.Business) } }) {
        InfoRow(stringResource(R.string.reg_email), partner.email)
        InfoRow(stringResource(R.string.reg_revenue), rupiah(store?.omzet ?: 0))
        InfoRow(stringResource(R.string.reg_outlet_count), partner.outletCount.toString())
        InfoRow(stringResource(R.string.reg_entity_type), choice(RegChoice.EntityType, partner.entity))
        InfoRow(stringResource(R.string.reg_channel), choice(RegChoice.Channel, partner.channel))
        store?.let { StoreChoices(it) }
    }
    Section(R.string.sec_pic, onEdit?.let { { it(RegStep.Pic) } }) {
        InfoRow(stringResource(R.string.reg_pic_name), partner.picName)
        InfoRow(stringResource(R.string.reg_pic_email), partner.picEmail)
        InfoRow(stringResource(R.string.reg_phone), "+62 ${partner.picPhone}")
        InfoRow(stringResource(R.string.reg_pic_status), choice(RegChoice.PicStatus, partner.picStatus))
        InfoRow(stringResource(R.string.reg_privy_id), partner.privyId ?: "-")
        InfoRow(stringResource(R.string.reg_pks_email), partner.pksInviteEmail ?: partner.picEmail)
    }
    Section(R.string.sec_bank, onEdit?.let { { it(RegStep.BankAccount) } }) {
        InfoRow(stringResource(R.string.reg_bank), partner.bankName)
        InfoRow(stringResource(R.string.reg_bank_branch), partner.bankBranch)
        InfoRow(stringResource(R.string.reg_account_number), partner.accountNumber)
        InfoRow(stringResource(R.string.reg_account_name), partner.accountName)
    }
    Section(R.string.sec_documents, onEdit?.let { { it(RegStep.Documents) } }) {
        partner.documents.forEach { DocumentRow(it, showVerification) }
    }
}

/** Toko-level choices (REG-03 and Tambah Toko). */
@Composable
fun StoreChoices(store: Store) {
    InfoRow(stringResource(R.string.reg_store_type), choice(RegChoice.StoreType, store.storeType))
    if (store.channelOffline != null && store.storeType == "OFFLINE") InfoRow(stringResource(R.string.reg_offline_channel), choice(RegChoice.OfflineChannel, store.channelOffline))
    InfoRow(stringResource(R.string.reg_store_scale), choice(RegChoice.StoreScale, store.scale))
    InfoRow(stringResource(R.string.reg_product_sold), choice(RegChoice.ProductSold, store.productSold))
    InfoRow(stringResource(R.string.reg_store_location), choice(RegChoice.StoreLocation, store.location))
    InfoRow(stringResource(R.string.reg_product_type), choice(RegChoice.ProductType, store.productType))
}

@Composable
private fun DocumentRow(doc: PartnerDoc, showVerification: Boolean) {
    val c = AmarTheme.colors
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
            Text(DocType.of(doc.key)?.let { stringResource(it.label) } ?: doc.key, Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.labelMdMedium)
            if (showVerification) {
                val (label, type) = when (doc.verification) {
                    Verification.UNVERIFIED -> R.string.doc_unverified to AmarBadgeType.Info
                    Verification.VALID -> R.string.doc_valid to AmarBadgeType.Success
                    Verification.NEEDS_REVISION -> R.string.doc_needs_revision to AmarBadgeType.Error
                }
                AmarBadge(stringResource(label), type = type, size = AmarBadgeSize.Small)
            }
        }
        val version = if (doc.version > 1) " · ${stringResource(R.string.doc_version, doc.version)}" else ""
        Text(doc.fileName + version, color = c.content.secondary, style = AmarTheme.typography.bodySm)
        doc.note?.let { Text(it, color = c.feedback.error.text, style = AmarTheme.typography.bodySm) }
    }
}

@Composable
private fun Section(title: Int, onEdit: (() -> Unit)?, content: @Composable () -> Unit) {
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { SectionTitle(stringResource(title)) }
            onEdit?.let { AmarButton(stringResource(R.string.reg_review_edit), it, type = AmarButtonType.Naked, size = AmarButtonSize.Small) }
        }
        content()
    }
}
