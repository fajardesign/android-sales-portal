package com.example.and_sales_portal.data.partner

import com.example.and_sales_portal.data.demo.DemoDirectory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Sample partners, the same as the web portal (`admin-sales-portal/src/api/db.js`): REG2026-0131 … 0148 with their
 * statuses, submitters, toko, SA/SR assignments, documents and status history. Times are relative to the portal's
 * reference time (7 Oct 2026, 10:30 WIB). Random values of the portal (omzet, RT/RW, coordinates) are fixed here.
 */
object PartnerSeed {

    private val BASE: Long = Instant.parse("2026-10-07T03:30:00Z").toEpochMilli()
    private const val HOUR = 3_600_000L
    private const val ADMIN = "Rina Saraswati (Admin)"

    private val ADDR = listOf(
        "Jl. Boulevard No. 12", "Jl. Ir. H. Juanda No. 88", "Jl. Gatot Subroto No. 45", "Jl. Basuki Rahmat No. 21",
        "Jl. Tanjung Duren Raya No. 7", "Jl. Pettarani No. 30", "Jl. Dago No. 101", "Jl. Sudirman No. 5",
    )
    private val PIC_NAMES = listOf("Hendra Gunawan", "Lina Marlina", "Ahmad Fauzi", "Rudi Hartono", "Sri Wahyuni", "Yusuf Maulana", "Diana Putri", "Bambang Irawan")
    private val BANK_CODES = listOf("BCA", "BRI", "MANDIRI", "BNI", "BSI", "CIMB")
    private val BRANCHES = listOf("Sudirman", "Panakkukang", "Dago", "Petisah", "Darmo")

    private fun ago(hours: Long) = BASE - hours * HOUR
    private fun day(iso: String) = LocalDate.parse(iso).atStartOfDay().toInstant(ZoneOffset.ofHours(7)).toEpochMilli()

    private class StoreCfg(val branch: String? = null, val assigned: List<Long> = emptyList(), val addedBy: Long? = null, val addedAt: Long? = null, val dLat: Double = 0.0, val dLng: Double = 0.0)

    private class Cfg(
        val submittedAt: Long,
        val stores: List<StoreCfg> = listOf(StoreCfg()),
        val privyId: String? = null,
        val docs: Map<String, Verification> = emptyMap(),
        val allDocs: Verification = Verification.UNVERIFIED,
        val optionalUploaded: Boolean = false,
        val notes: Map<String, String> = emptyMap(),
        val picStatus: String = Keys.PIC_OWNER,
        val accountName: String? = null,
    )

    fun partners(): List<Partner> {
        val list = mutableListOf<Partner>()
        fun add(p: Partner) = p.also { list += it }

        add(partner(148, "Sinar Jaya Ponsel", Keys.ENTITY_INDIVIDU, 1, 6, Cfg(ago(30), privyId = "PRV70231", allDocs = Verification.VALID)))
        add(partner(147, "CV Maju Bersama Elektronik", Keys.ENTITY_CV, 2, 12, Cfg(ago(52), docs = mapOf("KTP_PIC" to Verification.VALID, "NPWP_COMPANY" to Verification.VALID, "AKTA" to Verification.NEEDS_REVISION), optionalUploaded = true)))
        add(partner(146, "Berkah Cell Panakkukang", Keys.ENTITY_INDIVIDU, 1, 11, Cfg(ago(75), picStatus = Keys.PIC_KARYAWAN, accountName = "Fitriani Lestari")))
        add(partner(145, "PT Toko Gadget Nusantara", Keys.ENTITY_PT, 5, 9, Cfg(ago(6))))
        val p144 = add(partner(144, "Mitra Abadi Gadget", Keys.ENTITY_INDIVIDU, 3, 8, Cfg(ago(24 * 6), allDocs = Verification.VALID, docs = mapOf("FOTO_DEPAN" to Verification.NEEDS_REVISION), notes = mapOf("FOTO_DEPAN" to "Foto buram dan terpotong di bagian bawah"))))
        val p143 = add(partner(143, "CV Cahaya Digital", Keys.ENTITY_CV, 2, 7, Cfg(ago(24 * 5), allDocs = Verification.VALID, docs = mapOf("SK_KEMENKUMHAM" to Verification.NEEDS_REVISION), notes = mapOf("SK_KEMENKUMHAM" to "SK Kemenkumham kedaluwarsa, mohon unggah versi terbaru"))))
        val p142 = add(partner(142, "Prima Phone Store", Keys.ENTITY_INDIVIDU, 1, 6, Cfg(ago(24 * 8), privyId = "PRV55120", allDocs = Verification.VALID)))
        val p141 = add(partner(141, "PT Sentosa Retail Indonesia", Keys.ENTITY_PT, 5, 13, Cfg(ago(24 * 9), allDocs = Verification.VALID)))
        val p140 = add(partner(140, "Anugerah Gadget", Keys.ENTITY_INDIVIDU, 4, 10, Cfg(ago(24 * 12), privyId = "PRV48802", allDocs = Verification.VALID)))
        val p139 = add(partner(139, "Jaya Abadi Cellular", Keys.ENTITY_INDIVIDU, 1, 6, Cfg(day("2026-04-20"), privyId = "PRV31877", allDocs = Verification.VALID, stores = listOf(
            StoreCfg(assigned = listOf(15), dLat = -0.0055, dLng = 0.0054),
            StoreCfg("Mall Panakkukang", listOf(11), 11, day("2026-06-02"), -0.0094, 0.0139),
            StoreCfg("Sudiang", listOf(11), 6, day("2026-07-15"), 0.0127, 0.0193),
        ))))
        val p138 = add(partner(138, "CV Sumber Rejeki Elektronik", Keys.ENTITY_CV, 2, 7, Cfg(day("2026-05-11"), allDocs = Verification.VALID, stores = listOf(
            StoreCfg(assigned = listOf(17), dLat = 0.0061, dLng = -0.0042),
            StoreCfg("Dago", listOf(12), 12, day("2026-07-20"), -0.0087, 0.0071),
        ))))
        val p137 = add(partner(137, "Galaxy Phone Center", Keys.ENTITY_INDIVIDU, 5, 9, Cfg(day("2026-06-01"), allDocs = Verification.VALID, stores = listOf(
            StoreCfg(assigned = listOf(18), dLat = -0.0048, dLng = 0.0066),
            StoreCfg("Central Park", listOf(13), 13, day("2026-08-03"), 0.0092, -0.0081),
        ))))
        val p136 = add(partner(136, "Medan Selular", Keys.ENTITY_INDIVIDU, 3, 8, Cfg(day("2026-07-06"), allDocs = Verification.VALID, stores = listOf(StoreCfg(dLat = 0.0058, dLng = 0.0047)))))
        val p132 = add(partner(132, "PT Bintang Gadget Store", Keys.ENTITY_PT, 1, 6, Cfg(day("2026-05-25"), allDocs = Verification.VALID, stores = listOf(
            StoreCfg(assigned = listOf(16), dLat = 0.0075, dLng = -0.0058),
            StoreCfg("Tamalanrea", listOf(11), 6, day("2026-08-10"), -0.0133, -0.0077),
        ))))
        val p131 = add(partner(131, "Ponsel Kita", Keys.ENTITY_INDIVIDU, 2, 12, Cfg(day("2026-08-04"), allDocs = Verification.VALID, stores = listOf(StoreCfg(assigned = listOf(12), dLat = 0.0112, dLng = 0.0035)))))
        val p135 = add(partner(135, "Mega Cell Makassar", Keys.ENTITY_INDIVIDU, 1, 11, Cfg(ago(24 * 20))))
        val p134 = add(partner(134, "Toko Harapan Baru", Keys.ENTITY_INDIVIDU, 4, 10, Cfg(ago(24 * 18))))
        val p133 = add(partner(133, "Global Phone Center", Keys.ENTITY_INDIVIDU, 4, 10, Cfg(day("2026-03-10"), privyId = "PRV22014", allDocs = Verification.VALID)))

        val byId = list.associateBy { it.id }.toMutableMap()
        fun set(p: Partner) { byId[p.id] = p }

        set(p144.withRevision(ago(24 * 2), "Mohon diperbaiki maksimal 2 hari kerja.", listOf(
            RevisionItem(RevisionItem.DOC, "FOTO_DEPAN", "Foto Toko – Tampak Depan", "Foto buram dan terpotong di bagian bawah"),
            RevisionItem(RevisionItem.SEC, Sections.BANK, "Data Rekening", "Nomor rekening tidak sesuai dengan buku rekening"),
        )))
        set(p143.withRevision(ago(30), null, listOf(
            RevisionItem(RevisionItem.DOC, "SK_KEMENKUMHAM", "SK Kemenkumham", "SK Kemenkumham kedaluwarsa, mohon unggah versi terbaru"),
            RevisionItem(RevisionItem.SEC, Sections.PIC, "Informasi PIC", "Nomor handphone PIC tidak aktif"),
        )))
        set(p142.advance(40 to (PartnerStatus.VERIFIED to VERIFIED_REASON)))
        set(p141.advance(30 to (PartnerStatus.VERIFIED to VERIFIED_REASON)))
        set(p140.advance(26 to (PartnerStatus.VERIFIED to VERIFIED_REASON), 50 to (PartnerStatus.WAITING_PKS to "PKS dikirim di Privy web via Privy ID PRV48802")))
        listOf(p139 to "via Privy ID PRV31877", p138 to "via Email PIC ${p138.picEmail}", p137 to "via Email PIC ${p137.picEmail}", p136 to "via Email PIC ${p136.picEmail}",
            p132 to "via Email PIC ${p132.picEmail}", p131 to "via Email PIC ${p131.picEmail}", p133 to "via Privy ID PRV22014").forEach { (p, via) ->
            set(p.advance(22 to (PartnerStatus.VERIFIED to VERIFIED_REASON), 46 to (PartnerStatus.WAITING_PKS to "PKS dikirim di Privy web $via"), 24 * 6 to (PartnerStatus.ACTIVE to ACTIVE_REASON)))
        }
        set(p135.advance(30 to (PartnerStatus.REJECTED to "Usaha tidak sesuai kriteria partner (bukan toko gadget)")))
        set(p134.advance(50 to (PartnerStatus.CANCELLED to "Partner mengundurkan diri sebelum review selesai")))
        set(byId.getValue(p133.id).advance(24 * 150 to (PartnerStatus.INACTIVE to "Partner berhenti bekerja sama per Oktober 2026")))
        // Kursi TL (PRD Scope 1 v1.1 C6, same as the web sample): only partners that were activated have one, filled at
        // activation (the TL who submitted, or the TL of the SR who did); partners in the pipeline have none. Kode Referral
        // and QR per toko: toko of Active, Inactive and Rejected partners have them (`OF{area}{3 digits}{n}`, no prefix rule).
        val withCodes = setOf(139, 138, 137, 136, 132, 131, 133, 135).map { "REG2026-0$it" }.toSet()
        val out = list.map { byId.getValue(it.id) }.map { p ->
            val area = requireNotNull(DemoDirectory.area(p.areaId))
            val activated = p.history.any { it.to == PartnerStatus.ACTIVE }
            p.copy(stores = p.stores.mapIndexed { i, s ->
                val code = if (p.id in withCodes) "OF${area.code}${p.id.takeLast(3)}${i + 1}" else null
                s.copy(
                    tlId = if (activated) Partner.defaultTl(if (s.addedBy != 0L) s.addedBy else p.submittedBy) { id -> DemoDirectory.samplePerson(id) } else null,
                    referralCode = code,
                    referralQr = code?.let { "qr_${it.lowercase()}.png" },
                )
            })
        }.map { p ->
            when (p.id) {
                // Partner Inactive released every kursi (FR-013, "partner nonaktif"); the log keeps the past.
                "REG2026-0133" -> p.copy(stores = p.stores.map { s ->
                    s.tlId?.let { s.copy(tlId = null, tlLog = listOf(TlChange(p.statusUpdatedAt, it, null, "Sistem", "partner nonaktif"))) } ?: s
                })
                // 1.7 Mapping TL sample: two days ago the APL released Andi from Tamalanrea, and Siti with him ([assignments]).
                "REG2026-0132" -> {
                    val s = p.stores[1]
                    p.copy(
                        stores = p.stores.map { if (it.id == s.id) it.copy(tlId = null, assigned = emptyList(), tlLog = listOf(TlChange(TAMALANREA_AT, s.tlId, null, APL_HASAN, "dilepas APL"))) else it },
                        history = p.history + StatusEvent(TAMALANREA_AT, null, p.status, APL_HASAN, "Andi Pratama dilepas dari ${s.name} (dilepas APL)"),
                    )
                }
                // A toko added after Jaya Abadi Cellular became Active, waiting for its Kode Referral and QR (OQ-25 option 1);
                // no kursi (C6): "Belum ada TL" once active, until the APL assigns one.
                "REG2026-0139" -> p.copy(stores = p.stores + antang(p), outletCount = 4)
                else -> p
            }
        }
        return out
    }

    /** APL Hasan Basri released TL Andi (and SR Siti) from Tamalanrea 48 hours before the base time (web `db.js`). */
    private val TAMALANREA_AT = ago(48)
    private const val APL_HASAN = "Hasan Basri (APL)"

    /**
     * Assignment history of the sample: Siti (SR 11) released from Tamalanrea with its TL, so loans before that stay hers
     * ([PartnerRepository.holderAt]), with the notifications to Siti and Andi.
     */
    fun assignments(): AssignmentData {
        val store = "REG2026-0132-2"
        val sales = AssignmentEvent(TAMALANREA_AT, AssignmentAction.RELEASE, 11, store, "PT Bintang Gadget Store Tamalanrea", "REG2026-0132", "PT Bintang Gadget Store", APL_HASAN, AssignmentEvent.TL_RELEASED, "TL dilepas")
        val tl = AssignmentEvent(TAMALANREA_AT, AssignmentAction.RELEASE, 6, store, sales.storeName, sales.partnerId, sales.partnerName, APL_HASAN, AssignmentEvent.TL_RELEASED, "dilepas APL", AssignmentEvent.TL, listOf("Siti Rahmawati"))
        // Older than the 30 days kept (Designer Q3, 2026-10-19): in the data but not shown. Only a notification, so the
        // holder history of the toko is not changed.
        val old = AssignmentEvent(ago(24 * 40), AssignmentAction.ASSIGN, 11, "REG2026-0139-3", "Jaya Abadi Cellular Sudiang", "REG2026-0139", "Jaya Abadi Cellular", "6")
        return AssignmentData(
            events = listOf(sales, tl),
            notifications = listOf(
                AppNotification("N-${old.at}-11-REG2026-0139-3-SALES-ASSIGN", 11, old),
                AppNotification("N-$TAMALANREA_AT-11-$store-SALES-RELEASE", 11, sales, read = true),
                AppNotification("N-$TAMALANREA_AT-6-$store-TL-RELEASE", 6, tl, read = true),
            ),
        )
    }

    /** "Jaya Abadi Cellular Antang" (`REG2026-0139-4`): added by TL Bagus (22) 26 hours before the base time, no kursi. */
    private fun antang(p: Partner): Store {
        val area = requireNotNull(DemoDirectory.area(p.areaId))
        val main = p.stores.first()
        return main.copy(
            id = "${p.id}-4", code = null, name = "${p.name} Antang",
            address = main.address.replace(Regex("No\\. \\d+"), "No. 31"),
            lat = round6(area.lat + 0.006), lng = round6(area.lng + 0.004),
            omzet = 82_000_000, scale = "TRADITIONAL", productSold = "BRAND_NEW", location = "PINGGIR_JALAN",
            primary = false, status = Store.WAITING_REFERRAL, assigned = emptyList(),
            addedBy = 22, addedAt = ago(26), tlId = null, tlLog = emptyList(), referralCode = null, referralQr = null,
        )
    }

    private const val VERIFIED_REASON = "Seluruh dokumen wajib dan data rekening valid"
    private const val ACTIVE_REASON = "PKS ditandatangani (dicek di Privy web); partner diaktifkan"

    private fun partner(n: Int, name: String, entity: String, areaId: Int, submittedBy: Long, cfg: Cfg): Partner {
        val area = requireNotNull(DemoDirectory.area(areaId))
        val reg = "REG2026-0$n"
        val picName = PIC_NAMES[n % 8]
        val bare = name.lowercase().removePrefix("pt ").removePrefix("cv ")
        val picEmail = "${picName.lowercase().substringBefore(' ')}.${bare.substringBefore(' ').filter { it in 'a'..'z' }}@gmail.com"
        val street = ADDR[n % ADDR.size]
        val stores = cfg.stores.mapIndexed { i, s ->
            Store(
                id = "$reg-${i + 1}",
                name = if (s.branch == null && i == 0) name else "$name ${s.branch ?: "Cabang ${i + 1}"}",
                address = "${street.replace(Regex("No\\. \\d+"), "No. ${10 + i * 7}")}, ${area.village}, ${area.district}, ${area.city}, ${area.province}",
                province = area.province, regency = area.city, district = area.district, village = area.village,
                rt = "003", rw = "005",
                lat = round6(area.lat + if (s.dLat != 0.0) s.dLat else (n % 7 - 3) * 0.002),
                lng = round6(area.lng + if (s.dLng != 0.0) s.dLng else (n % 5 - 2) * 0.002),
                omzet = (60L + (n * 37 + i * 53) % 160) * 1_000_000,
                scale = if (i % 2 == 0) "MODERN" else "TRADITIONAL",
                productSold = listOf("BRAND_NEW", "BLENDED", "USED")[i % 3],
                location = listOf("PINGGIR_JALAN", "MALL", "SEPARATE")[i % 3],
                primary = i == 0,
                assigned = s.assigned,
                addedBy = s.addedBy ?: submittedBy,
                addedAt = s.addedAt ?: cfg.submittedAt,
            )
        }
        val docs = DocType.forEntity(entity, cfg.picStatus).mapNotNull { (d, mandatory) ->
            if (!mandatory && !cfg.optionalUploaded) return@mapNotNull null
            val ext = if (d.store || d.name.startsWith("KTP") || d == DocType.BUKU_REKENING) "jpg" else "pdf"
            val v = cfg.docs[d.name] ?: cfg.allDocs
            PartnerDoc(d.name, "${d.name.lowercase()}_${reg.takeLast(4)}.$ext", sizeBytes = if (ext == "pdf") 1_843_200 else 865_280, verification = v, note = cfg.notes[d.name], uploadedAt = cfg.submittedAt)
        }
        return Partner(
            id = reg, name = name, entity = entity,
            address = street, province = area.province, regency = area.city, district = area.district, village = area.village,
            rt = "003", rw = "005",
            // Same as the web portal sample: AMR + submitter id + number.
            email = "admin@${bare.filter { it in 'a'..'z' }}.co.id", channel = "STORE", outletCount = stores.size,
            picName = picName, picEmail = picEmail, picPhone = "85" + (300_000_000 + n * 104_729).toString().take(9),
            picStatus = cfg.picStatus, privyId = cfg.privyId,
            bankCode = BANK_CODES[n % BANK_CODES.size], bankName = Banks.name(BANK_CODES[n % BANK_CODES.size]),
            bankBranch = "KCP ${area.name} ${BRANCHES[n % BRANCHES.size]}",
            accountNumber = (1_000_000_000L + n * 7_654_321L).toString().take(10),
            accountName = cfg.accountName ?: if (entity == Keys.ENTITY_INDIVIDU) picName else name,
            areaId = areaId, submittedBy = submittedBy, submittedAt = cfg.submittedAt,
            status = PartnerStatus.UNDER_REVIEW, statusUpdatedAt = cfg.submittedAt,
            lat = stores.first().lat, lng = stores.first().lng,
            stores = stores, documents = docs,
            history = listOf(StatusEvent(cfg.submittedAt, null, PartnerStatus.UNDER_REVIEW, submittedBy.toString(), "Pengajuan dikirim dari aplikasi mobile")),
        )
    }

    /** Status steps `(hours after submission) to (status to reason)`, as the portal's `advance`. */
    private fun Partner.advance(vararg steps: Pair<Int, Pair<PartnerStatus, String>>): Partner = steps.fold(this) { p, (h, step) ->
        val at = submittedAt + h * HOUR
        PartnerActions.move(p, step.first, at, ADMIN, step.second)
    }

    private fun Partner.withRevision(at: Long, general: String?, items: List<RevisionItem>) = copy(
        status = PartnerStatus.REVISION_REQUIRED,
        statusUpdatedAt = at,
        revision = RevisionRequest(at, ADMIN, general, items),
        history = history + StatusEvent(at, PartnerStatus.UNDER_REVIEW, PartnerStatus.REVISION_REQUIRED, ADMIN, "${items.size} item diminta revisi: ${items.joinToString { it.label }}"),
    )

    private fun round6(v: Double) = Math.round(v * 1e6) / 1e6
}
