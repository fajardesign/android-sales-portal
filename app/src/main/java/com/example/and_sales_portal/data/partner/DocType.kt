package com.example.and_sales_portal.data.partner

import androidx.annotation.StringRes
import com.example.and_sales_portal.R

/** Whether a document is needed: mandatory, optional, or mandatory only when Status PIC = Karyawan. */
enum class Need { M, O, K }

/**
 * Documents of a registration (PRD v3 C2 document table, portal `DOC_TYPES`). Legal documents: JPG/PNG/PDF up to 5 MB;
 * store photos: JPG/PNG up to 2 MB, camera only.
 */
enum class DocType(
    @StringRes val label: Int,
    /** Label in the web portal (`DOC_TYPES`), used in texts that come from the Admin. */
    val adminLabel: String,
    val store: Boolean,
    private val individu: Need?,
    private val company: Need?,
) {
    KTP_PIC(R.string.doc_ktp_pic, "KTP PIC", false, Need.M, Need.M),
    KTP_OWNER(R.string.doc_ktp_owner, "KTP Pemilik", false, Need.K, null),
    NPWP(R.string.doc_npwp, "NPWP (pribadi)", false, Need.M, null),
    NPWP_COMPANY(R.string.doc_npwp_company, "NPWP Perusahaan", false, null, Need.M),
    NIB(R.string.doc_nib, "NIB / SIUP", false, Need.O, Need.O),
    ANGGARAN_DASAR(R.string.doc_anggaran_dasar, "Anggaran Dasar", false, null, Need.M),
    AKTA(R.string.doc_akta, "Akta Pendirian", false, null, Need.M),
    SK_KEMENKUMHAM(R.string.doc_sk_kemenkumham, "SK Kemenkumham", false, null, Need.M),
    DOMISILI(R.string.doc_domisili, "Keterangan Domisili", false, null, Need.M),
    IJIN(R.string.doc_ijin, "Ijin Lokasi & Ijin Usaha", false, null, Need.M),
    BUKU_REKENING(R.string.doc_buku_rekening, "Halaman Depan Buku Rekening / Rekening Koran", false, Need.M, Need.M),
    /** Decision 2026-10-20: replaces the consent of PRD 1.1 C5; mandatory for every entity (web `DOC_TYPES` key `PDP`). */
    PDP(R.string.doc_pdp, "Formulir Persetujuan PDP", false, Need.M, Need.M),
    FOTO_DEPAN(R.string.doc_foto_depan, "Foto Toko – Tampak Depan", true, Need.M, Need.M),
    FOTO_SAMPING(R.string.doc_foto_samping, "Foto Toko – Tampak Samping", true, Need.M, Need.M),
    ;

    /** Largest file accepted. */
    val maxBytes: Long get() = if (store) 2L * MB else 5L * MB

    companion object {
        private const val MB = 1024L * 1024L

        /** Documents to show for [entity] and [picStatus], with whether each is mandatory; empty until an entity is chosen. */
        fun forEntity(entity: String?, picStatus: String?): List<Pair<DocType, Boolean>> {
            if (entity == null) return emptyList()
            val individu = entity == Keys.ENTITY_INDIVIDU
            return entries.mapNotNull { d ->
                when (if (individu) d.individu else d.company) {
                    Need.M -> d to true
                    Need.O -> d to false
                    Need.K -> if (picStatus == Keys.PIC_KARYAWAN) d to true else null
                    null -> null
                }
            }
        }

        fun of(key: String): DocType? = entries.firstOrNull { it.name == key }
    }
}
