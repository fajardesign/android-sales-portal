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

Login dengan **email atau nomor telepon** (revisi stakeholder 2026-10-08; username lama ditolak). Nomor boleh ditulis `08…`, `62…`, `+62…`, atau `8…`.

| Email | No. telepon | Role | Isi |
|---|---|---|---|
| `andi.pratama@amarbank.co.id` | `081200047514` | TL Senior, Makassar | Tim: Siti (SR), Eko (SA), Nurul (SA); partner Jaya Abadi Cellular dan PT Bintang Gadget Store |
| `siti.rahmawati@amarbank.co.id` | `081200087109` | SR, Makassar | 3 toko di 2 partner (di bawah minimum SR 4 toko) |
| `eko.saputra@amarbank.co.id` | `081200118785` | SA, Makassar | 1 toko (Jaya Abadi Cellular) |
| `nurul.hidayah@amarbank.co.id` | `081200126704` | SA, Makassar | 1 toko (PT Bintang Gadget Store) |
| `fajar.nugroho@amarbank.co.id` | `081200079190` | TL Pending | Login ditolak: *Akun belum diaktivasi* |
| `rizky.ramadhan@amarbank.co.id` | `081200166299` | SA Disabled | Login ditolak: *Akun Anda tidak aktif* |

Akun lain dari portal web (mis. `budi.santoso@amarbank.co.id`, `dewi.lestari@amarbank.co.id`) juga bisa login dengan data area masing-masing.

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
| ATT-01 … ATT-05 | Absensi, Check In/Out Kerja, Kunjungan (target mingguan), Check-in/out | `ui/attendance/`, `ui/visits/`, `data/activity/` |
| REG-01 … REG-11 | Tab Partner, pendaftaran, Pengajuan, revisi | `ui/partner/`, `ui/partnerregistration/`, `data/partner/` |
| PRT-01, PRT-02 | Detail Partner, Tambah Toko | `ui/partner/` |
| TL-01 … TL-04 | Tim, Assign SA/SR (per toko), Jadwal Kunjungan (mingguan + libur; TL-05 dihapus) | `ui/team/`, `data/team/` |
| TL-06 … TL-10 | Beranda TL, Produktivitas, Kinerja Penjualan, Pinjaman, Insentif | `ui/dashboard/`, `data/sales/` |
| SLS-01 … SLS-04 | Beranda SA/SR, tab Kinerja | `ui/dashboard/` |
| AKN-01 | Akun | `ui/account/`, `ui/shell/TabFragments.kt` |

## Arsitektur singkat

- `MainActivity` + `nav_main` (satu Fragment per Flow ID, isi Compose lewat `ComposeFragment`); bottom navigation per role (`ui/shell/Shell.kt`). Pendaftaran partner adalah nav graph `nav_partner_registration` yang di-include, ViewModel di-scope ke graph.
- `SalesApp.AppContainer`: Supabase (akun), `PartnerRepository` (partner bersama di perangkat), `OperationsRepository` (absensi, kunjungan, libur semua SA/SR/TL), `TeamService`, `SalesRepository` (pinjaman, target, insentif), `LocationService`, `Connectivity`, `SimulationSettings`.
- Data disimpan di `filesDir/demo/` (`partners.json`, `operations.json`, draf pendaftaran per akun, dokumen dan selfie). **Akun › Reset data demo** mengembalikan semuanya ke data contoh.
- Design system: hanya komponen `designsystem/components/`; komponen baru di `components/extended/` (`AmarBottomNavigation`, `AmarProgressBar`), tercatat di `docs/design-system/components.md`.

## Simulasi (debug)

**Akun › Simulasi (debug)**: lokasi (Di kantor / toko / GPS perangkat / Di luar radius ±9 km), lokasi palsu, mode offline, flag consent pendaftaran (REG-07, default mati). **Simulasi Admin: akun** meniru Nonaktifkan di web (ACC-05): TL Andi Pratama (partner dan tim pindah ke Bagus Wicaksono) dan SA Eko Saputra (tokonya dilepas). Di **Detail Pengajuan** ada **Simulasi Admin (debug)** untuk menjalankan aksi Admin portal (Minta Revisi, Verifikasi, Kirim PKS, Aktifkan, Tolak, Batalkan, Nonaktifkan).

## Batasan prototype

- Backend hanya untuk akun (Supabase); data operasional lokal per perangkat, tidak tersinkron antarperangkat atau dengan portal web.
- Peta berupa placeholder (koordinat + "Buka di Maps"); kamera memakai aplikasi kamera bawaan. Absensi tanpa biometrik/liveness, radius 3 km dari kantor contoh area atau toko (revisi stakeholder 2026-10-08).
- Pinjaman dikreditkan ke SA/SR yang saat ini ditugaskan di toko (riwayat ikut pindah saat penugasan berubah).
- Target paid out dan MFP adalah data contoh (sumber target masih TBD di PRD).
- Keputusan yang masih terbuka dicatat di bagian *Pertanyaan Terbuka* tiap file flow di bundle.

Hasil uji jalur per role: [test-report.md](./test-report.md).
