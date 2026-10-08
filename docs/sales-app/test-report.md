# Uji jalur per role (Bagian 6)

Tanggal uji: 2026-10-08, emulator Pixel 6 API 34, build debug, login Supabase (akun demo). Skrip: tiap tombol/entri dibuka, judul layar dicek, lalu Back dan layar asal dicek. Tujuannya memastikan tidak ada layar buntu.

**Hasil: 50/50 jalur lolos.**

## TL (andi.pratama)

| Jalur | Terbuka | Kembali dengan Back |
|---|---|---|
| Beranda › Lihat semua (kunjungan saya) | Ya (Kunjungan Saya) | Ya |
| Beranda › Pengajuan Partner › Lihat semua | Ya (Partner) | Ya |
| Beranda › Estimasi Insentif › Lihat Detail | Ya (Insentif) | Ya |
| Beranda › Kartu Absensi › Lihat riwayat | Ya (Absensi) | Ya |
| Beranda › Clock In | Ya (Clock In) | Ya |
| Beranda › Pintasan Daftarkan Partner | Ya (Pendaftaran Partner) | Ya |
| Beranda › Pintasan Buat Jadwal Kunjungan | Ya (Jadwal Kunjungan) | Ya |
| Beranda › Pintasan Assign SA/SR | Ya (Assign SA/SR) | Ya |
| Partner › Partner Saya › detail | Ya (Detail Partner) | Ya |
| Partner › detail › Tambah Toko | Ya (Tambah Toko) | Ya |
| Partner › detail › Tambah Toko | Ya (Detail Partner) | Ya |
| Partner › Pengajuan › detail | Ya (Detail Pengajuan) | Ya |
| Partner › Daftarkan Partner | Ya (Pendaftaran Partner) | Ya |
| Partner (tab) › Back | Ya (Partner) | Ya |
| Tim › anggota | Ya (Detail Anggota) | Ya |
| Tim › anggota › Atur Partner | Ya (Assign SA/SR) | Ya |
| Tim › anggota › Atur Partner | Ya (Detail Anggota) | Ya |
| Tim › anggota › Atur Jadwal | Ya (Jadwal Kunjungan) | Ya |
| Tim › anggota › Atur Jadwal | Ya (Detail Anggota) | Ya |
| Aktivitas › Absensi | Ya (Absensi) | Ya |
| Aktivitas › Kunjungan Saya | Ya (Kunjungan Saya) | Ya |
| Aktivitas › Jadwal Kunjungan | Ya (Jadwal Kunjungan) | Ya |
| Aktivitas › Assign SA/SR | Ya (Assign SA/SR) | Ya |
| Aktivitas › Produktivitas Tim | Ya (Produktivitas Tim) | Ya |
| Aktivitas › Kinerja Penjualan | Ya (Kinerja Penjualan) | Ya |
| Aktivitas › Pinjaman | Ya (Pinjaman) | Ya |
| Aktivitas › Insentif | Ya (Insentif) | Ya |
| Produktivitas Tim › bukti anggota | Ya (Siti Rahmawati) | Ya |
| Aktivitas › Produktivitas Tim › bukti anggota | Ya (Produktivitas Tim) | Ya |
| Akun › Simulasi (debug) | Ya (Simulasi (debug)) | Ya |

## SR (siti.rahmawati)

| Jalur | Terbuka | Kembali dengan Back |
|---|---|---|
| Beranda › Lihat riwayat | Ya (Absensi) | Ya |
| Beranda › kunjungan hari ini | Ya (Detail Kunjungan) | Ya |
| Kunjungan › detail | Ya (Detail Kunjungan) | Ya |
| Partner › Partner Saya › detail | Ya (Detail Partner) | Ya |
| Partner › Pengajuan › detail | Ya (Detail Pengajuan) | Ya |
| Partner › Daftarkan Partner | Ya (Pendaftaran Partner) | Ya |
| Kinerja › tab Pinjaman | Ya (Kinerja) | Ya |
| Kinerja › tab Insentif | Ya (Kinerja) | Ya |
| Akun › Simulasi (debug) | Ya (Simulasi (debug)) | Ya |

## SA (eko.saputra)

| Jalur | Terbuka | Kembali dengan Back |
|---|---|---|
| Beranda › Lihat riwayat | Ya (Absensi) | Ya |
| Beranda › kunjungan hari ini | Ya (Detail Kunjungan) | Ya |
| Kunjungan › detail | Ya (Detail Kunjungan) | Ya |
| Partner › tab Pengajuan (SA) | Ya (tidak ditampilkan) | Ya |
| Partner › Daftarkan Partner (SA) | Ya (tidak ditampilkan) | Ya |
| Partner › Partner Saya › detail | Ya (Detail Partner) | Ya |
| Detail Partner › Tambah Toko (SA) | Ya (tidak ditampilkan) | Ya |
| Partner › detail tanpa Tambah Toko (SA) | Ya (Detail Partner) | Ya |
| Kinerja › tab Pinjaman | Ya (Kinerja) | Ya |
| Kinerja › tab Insentif | Ya (Kinerja) | Ya |
| Akun › Simulasi (debug) | Ya (Simulasi (debug)) | Ya |

## Alur yang diuji manual di bagian sebelumnya

| Bagian | Alur |
|---|---|
| 1 | Login TL/SR/SA, password salah (inline), akun belum aktivasi, akun nonaktif, offline + Coba Lagi, Lupa password, sesi dipulihkan (online dan offline), Keluar |
| 2 | Izin lokasi ditolak lalu diizinkan, Clock In dengan kamera, liveness gagal, offline (tombol nonaktif), check-in dalam radius, check-out offline lalu sinkron, di luar radius, lokasi palsu (dialog tidak bisa ditutup), GPS mati → pengaturan → kembali, GPS perangkat asli |
| 3 | Minta revisi (Simulasi Admin) → Perbaiki & Kirim Ulang, pendaftaran baru lengkap (validasi, dokumen kamera, ringkasan, lokasi, REG2026-0149), Lihat Pengajuan, Tambah Toko (nama duplikat, pilihan wajib, tersimpan) |
| 4 | Tim, detail anggota, SA dipindahkan (error PRD + Pindahkan, kunjungan lama dibatalkan otomatis), tambah kunjungan, kunjungan muncul di akun SA |
| 5 | Beranda SA dan TL, tab Kinerja (Insentif), Produktivitas Tim, Kinerja Penjualan, Pinjaman |

Unit test dan screenshot: `./gradlew testDebugUnitTest` (semua lulus).
