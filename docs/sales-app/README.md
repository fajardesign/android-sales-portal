# Aplikasi Sales (TL, SR, SA) — Prototype

Prototype Android yang bisa diklik untuk Team Leader (TL), Sales Representative (SR), dan Sales Agent (SA). Tampilan mengikuti role. Akun memakai backend prototype Supabase; data operasional (partner, absensi, kunjungan, pinjaman, insentif) dibuat di perangkat dan disamakan dengan contoh data portal web.

Spesifikasi: knowledge bundle `okf_repository_design/products/sales_dashboard/flows_android/` (lihat tabel Flow ID di bawah). Sumber requirement: PRD v3 *Final Prompt – Sales and Partner Dashboard (v3, full journeys)* → bundle → Figma *Sales - Partner Dashboard - Android*.

## Menjalankan

1. Tambahkan ke `local.properties` (tidak di-commit):
   ```
   SUPABASE_URL=https://rhjanqgbvmetrjbfrzwh.supabase.co
   SUPABASE_PUBLISHABLE_KEY=sb_publishable_...
   ```
   Keduanya dibaca `app/build.gradle.kts` menjadi `BuildConfig.SUPABASE_URL` / `SUPABASE_PUBLISHABLE_KEY`.
2. `./gradlew assembleDebug` lalu pasang APK debug (menu **Simulasi (debug)** hanya ada di build debug).
3. `./gradlew testDebugUnitTest` untuk unit test, aturan DS, dan screenshot (Roborazzi, output di `app/build/outputs/roborazzi/`).

## Akun demo (password `Demo1234`)

| Akun | Role | Isi |
|---|---|---|
| `andi.pratama` | TL Senior, Makassar | Tim: Siti (SR), Eko (SA), Nurul (SA); partner Jaya Abadi Cellular dan PT Bintang Gadget Store |
| `siti.rahmawati` | SR, Makassar | 3 toko di 2 partner, 3 kunjungan per hari |
| `eko.saputra` | SA, Makassar | 1 toko (Jaya Abadi Cellular), 1 kunjungan per hari |
| `nurul.hidayah` | SA, Makassar | 1 toko (PT Bintang Gadget Store) |
| `fajar.nugroho` | TL Pending | Login ditolak: *Akun belum diaktivasi* |
| `rizky.ramadhan` | SA Disabled | Login ditolak: *Akun Anda tidak aktif* |

Akun lain dari portal web (mis. `budi.santoso`, `dewi.lestari`) juga bisa login dengan data area masing-masing.

## Navigasi per role

| Role | Bottom navigation |
|---|---|
| TL | Beranda · Partner · Tim · Aktivitas · Akun |
| SR | Beranda · Kunjungan · Partner (+ Pengajuan, Daftarkan, Tambah Toko) · Kinerja · Akun |
| SA | Beranda · Kunjungan · Partner (Partner Saya saja) · Kinerja · Akun |

## Peta Flow ID → kode

| Flow ID | Layar | Kode |
|---|---|---|
| MLG-00 … MLG-02 | Splash, Login, sesi | `ui/auth/`, `data/auth/` |
| ATT-01 … ATT-05 | Absensi, Clock In/Out, Kunjungan, Check-in/out | `ui/attendance/`, `ui/visits/`, `data/activity/` |
| REG-01 … REG-11 | Tab Partner, pendaftaran, Pengajuan, revisi | `ui/partner/`, `ui/partnerregistration/`, `data/partner/` |
| PRT-01, PRT-02 | Detail Partner, Tambah Toko | `ui/partner/` |
| TL-01 … TL-05 | Tim, Assign SA/SR, Jadwal Kunjungan | `ui/team/`, `data/team/` |
| TL-06 … TL-10 | Beranda TL, Produktivitas, Kinerja Penjualan, Pinjaman, Insentif | `ui/dashboard/`, `data/sales/` |
| SLS-01 … SLS-04 | Beranda SA/SR, tab Kinerja | `ui/dashboard/` |
| AKN-01 | Akun | `ui/account/`, `ui/shell/TabFragments.kt` |

## Arsitektur singkat

- `MainActivity` + `nav_main` (satu Fragment per Flow ID, isi Compose lewat `ComposeFragment`); bottom navigation per role (`ui/shell/Shell.kt`). Pendaftaran partner adalah nav graph `nav_partner_registration` yang di-include, ViewModel di-scope ke graph.
- `SalesApp.AppContainer`: Supabase (akun), `PartnerRepository` (partner bersama di perangkat), `OperationsRepository` (absensi, kunjungan, libur semua SA/SR/TL), `TeamService`, `SalesRepository` (pinjaman, target, insentif), `LocationService`, `Connectivity`, `SimulationSettings`.
- Data disimpan di `filesDir/demo/` (`partners.json`, `operations.json`, draf pendaftaran per akun, dokumen dan selfie). **Akun › Reset data demo** mengembalikan semuanya ke data contoh.
- Design system: hanya komponen `designsystem/components/`; komponen baru di `components/extended/` (`AmarBottomNavigation`, `AmarProgressBar`), tercatat di `docs/design-system/components.md`.

## Simulasi (debug)

**Akun › Simulasi (debug)**: lokasi (Di lokasi toko / GPS perangkat / Di luar radius), lokasi palsu, mode offline, liveness gagal. Di **Detail Pengajuan** ada **Simulasi Admin (debug)** untuk menjalankan aksi Admin portal (Minta Revisi, Verifikasi, Kirim PKS, Aktifkan, Tolak, Batalkan, Nonaktifkan).

## Batasan prototype

- Backend hanya untuk akun (Supabase); data operasional lokal per perangkat, tidak tersinkron antarperangkat atau dengan portal web.
- Peta berupa placeholder (koordinat + "Buka di Maps"); liveness disimulasikan; kamera memakai aplikasi kamera bawaan.
- Pinjaman dikreditkan ke SA/SR yang saat ini ditugaskan di toko (riwayat ikut pindah saat penugasan berubah).
- Target paid out dan MFP adalah data contoh (sumber target masih TBD di PRD).
- Keputusan yang masih terbuka dicatat di bagian *Pertanyaan Terbuka* tiap file flow di bundle.

Hasil uji jalur per role: [test-report.md](./test-report.md).
