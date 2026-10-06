# Komponen Design System — Figma → Kotlin

Satu baris per komponen Figma yang sudah diimplementasikan. Path Kotlin relatif terhadap
`app/src/main/java/com/example/and_sales_portal/designsystem/`. Inventaris lengkap: [inventory.md](./inventory.md).

| Kelompok | Komponen Figma | Node | Varian | File Kotlin | Composable | Preview | Status |
|---|---|---|---|---|---|---|---|
| Feedback | Info Box | [676:2342](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=676-2342) | 20 (Type × Button × Show Label) | `components/feedback/AmarInfoBox.kt` | `AmarInfoBox` | 5 (per Type) × 4 | ✅ Terverifikasi visual (4 varian) |
| Feedback | Badge | [981:4166](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=981-4166) | 96 (Type × Size × Supporting × Solid) | `components/feedback/AmarBadge.kt` | `AmarBadge` | 8 (Type × Solid) × 12 | ✅ Terverifikasi visual (7 varian) |
| Feedback | Overlay | [163:334](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=163-334) | 2 (State) | `components/feedback/AmarOverlay.kt` | `AmarOverlay` | 3 | ✅ Warna token; ⚠ warna spinner menunggu konfirmasi |
| Inputs | Input | [236:235](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=236-235) | 60 (Type × State × Error) | `components/inputs/AmarTextField.kt` | `AmarTextField` | 5 (per Type), masing-masing 8 state statis | ✅ Terverifikasi visual (8 varian) |
| Inputs | Dropdown | [640:1400](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=640-1400) | 8 (State × Error) | `components/inputs/AmarDropdown.kt` | `AmarDropdown` | 8 varian | ✅ Terverifikasi visual (2 varian) |
| Inputs | Area | [311:744](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=311-744) | 12 (State × Error) | `components/inputs/AmarTextArea.kt` | `AmarTextArea` | 8 state statis | ✅ Terverifikasi visual (2 varian) |
| Inputs | Code Verification | [321:502](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=321-502) | 16 (Type × State) | `components/inputs/AmarCodeVerification.kt` | `AmarCodeVerification` | state statis OTP & PIN | ✅ Terverifikasi visual (3 varian) |
| Inputs | Element / Digit | [317:1499](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=317-1499) | 12 (State × Masking) | `components/inputs/AmarCodeVerification.kt` | `AmarDigit` | 12 varian | ✅ lewat Code Verification |
| Inputs | Input / Label | [200:539](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=200-539) | 1 | `components/inputs/AmarFieldParts.kt` | `AmarInputLabel` | ya | ✅ lewat field |
| Inputs | Input / Hint | [206:511](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=206-511) | 1 | `components/inputs/AmarFieldParts.kt` | `AmarInputHint` | ya | ✅ lewat field |
| Inputs | Element / Toggle | [671:2227](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=671-2227) | 8 (State × Size × Active) | `components/inputs/AmarToggle.kt` | `AmarToggle` | 8 varian | ✅ Terverifikasi visual (frame 304×100) |
| Inputs | Selection Control | [466:277](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=466-277) | 32 (Checked × Size × Type × Disabled × Icon Only) | `components/inputs/AmarSelectionControl.kt` | `AmarSelectionControl` | 4 (Type × Size), masing-masing 8 | ✅ Terverifikasi visual (6 varian) |
| Inputs | Element / Icon | [463:320](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=463-320) | 24 | `components/inputs/AmarSelectionControl.kt` | `AmarSelectionIcon` | 24 varian | ✅ lewat Selection Control |
| Actions | Button | [60:337](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=60-337) | 180 (Type × Size × State × Icon Only × Inverse) | `components/actions/AmarButton.kt` | `AmarButton` | 10 (Type × Inverse), masing-masing 18 varian | ✅ Terverifikasi visual (8 varian representatif, 2026-10-06) |
| Actions | Elements / Button Group | [455:432](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=455-432) | 2 (Stack, Side) | `components/actions/AmarButtonGroup.kt` | `AmarButtonGroup` | 5 kombinasi | ✅ Terverifikasi visual (geometri sama persis) |
| Feedback | Spinner (ikon) | [536:1213](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=536-1213) | 1 | `components/feedback/AmarSpinner.kt` | `AmarSpinner` | lewat Button | ✅ Geometri & opacity sama |
| Feedback | Alert | [683:300](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=683-300) | 12 (Style × Type) | `components/feedback/AmarAlert.kt` | `AmarAlert` | 12 varian + opsi | ✅ Terverifikasi visual (Roborazzi vs Figma, 2026-10-06) |
| Layout & Content | Divider | [1123:142](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1123-142) | 6 (Orientation × Weight) | `components/layout/AmarDivider.kt` | `AmarDivider` | 6 varian | ✅ Terverifikasi visual (warna & ukuran sama dengan Figma, 2026-10-06) |

## Catatan per komponen

### Alert
- **Properti Figma → parameter**:
  - `Style` → `AmarAlertStyle` (Default, Solid, Inverse)
  - `Type` → `AmarAlertType` (Info, Success, Warning, Error)
  - `Message` → `message`
  - `Icon` → `showIcon`
  - `Change Icon` → `icon: Painter`
  - `Action` → `actionLabel` + `onAction`
  - `Dismiss` → `onDismiss`
- **Token**: warna dari `AmarTheme.colors.feedback.*`, `background.inverse`, `content.inverse`, `action.nakedInverse`, dan `icon.tertiary`. Layout dari `AmarAlertTokens` (padding 16×14, gap 12, radius 12, lebar 328). Shadow dari `elevation.floating` (effect style `Shadow/Floating`).
- **Aset**: `res/drawable/ds_ic_default_20.xml` dan `ds_ic_close_20.xml`. Geometrinya identik di 12 varian; warnanya diatur lewat tint.
- **Lebar**: komponen tidak mengunci lebar sendiri. Di Figma lebarnya 328 (`Position/Width`); di layar pakai `fillMaxWidth()` dengan margin `AmarAlertTokens.MarginHorizontal`.
- **Shadow**: tidak tampil di API 24–27 (keterbatasan shadow layer Android).
- **Verifikasi visual** (`AmarAlertScreenshotTest`, 1x, framing 352×72 seperti render Figma):
  - Latar 12 varian sama persis dengan Figma.
  - Posisi ikon (x 28–47, y 22–41) dan close (x 308–319) sama persis.
  - Radius sudut dan padding cocok. Shadow berbeda paling banyak 3/255 per kanal.
  - Teks: posisi awal sama; lebar dan tepi glyph berbeda ≤1 px karena rasterizer Figma dan Skia berbeda. Tidak bisa disamakan lewat token.
- **Pertanyaan ke desainer**:
  1. Close di Solid Warning putih di atas `#F2CF53` (kontras ±1,5:1), sedangkan ikon dan teks `#854D0E`.
  2. Label bawaan action di Style Inverse "Button", di Style lain "Action".
  3. Area sentuh action dan close 20dp (pedoman Android 48dp).

### Divider
- **Properti Figma → parameter**: `Orientation` → `AmarDividerOrientation`; `Weight` → `AmarDividerWeight`. `Weight` menentukan warna (`Border/Color/Subtle|Default|Strong`), bukan ketebalan.
- **Token**: tebal `borderWidths.subtle` (1dp), bentuk `shapes.full`. Panjangnya mengikuti parent (`fillMaxWidth`/`fillMaxHeight`); 328 dan 80 di Figma hanya ukuran contoh.
- **Verifikasi**: warna ketiga Weight sama persis dengan Figma (#E2E8EC, #F1F4F6, #CDD5DC). Ukuran 328×1 dan 1×80 cocok.

### Button
- **Properti Figma → parameter**:
  - `Type` → `AmarButtonType` (`Error` memakai token `Destructive`)
  - `Size` → `AmarButtonSize`
  - `State` → `enabled` + `loading`
  - `Icon Only` → `label = null` + `icon`
  - `Inverse` → `inverse`
  - `Left/Right Icon` + `Change Left/Right` → `leftIcon` / `rightIcon`
  - `Label` → `label`
- **Keputusan (disetujui 2026-10-06)**:
  - Lebar hug, bukan 140 tetap; maksimum 640 (`AmarButtonTokens.MaxWidth`).
  - Loading mempertahankan ukuran tombol dan tidak bisa diklik.
  - Spinner berputar dalam langkah 45°, 800 ms per putaran (tidak ada di Figma).
  - Warna Pressed memakai token `... Pressed`. Tidak ada ripple.
- **Aturan warna Figma yang diikuti**:
  - Loading memakai background *Pressed* untuk Primary, Error, dan Ghost/Inverse. Secondary dan Ghost biasa memakai background default.
  - Ghost/Disabled/Inverse memakai `Secondary/Inverse/Disabled/Background`.
- **Aset**: `ds_ic_default_24.xml` (master 24 `46:450`; di M/S diperkecil ke 20/16).
- **Verifikasi**:
  - Warna kontainer, border Secondary, radius 16, tinggi 56, Icon Only 56×56, serta posisi ikon dan spinner sama dengan Figma.
  - Teks sekitar 1 px lebih tinggi dan 1 px lebih lebar (rasterizer).
- **Catatan**: Small (40dp) di bawah area sentuh 48dp. Visual tetap mengikuti Figma.

### Button Group
- **Properti Figma → parameter**: `State` → `AmarButtonGroupLayout`; `2nd Button` → `secondLabel`; `3rd Button` → `thirdLabel` (Stack saja).
- **Urutan Stack** dari atas: 2nd (Secondary), 3rd (Secondary), Primary. Side: 2nd, Primary dengan lebar sama.
- **Gap**: 12 (`Component/Modal/Default/Gap/Button`).

### Spinner
- 8 batang 3,27 × 7,64 (radius 1,64) dalam bingkai 24, searah jarum jam dari atas, dengan opacity 1 / .15 / .27 / .39 / .51 / .63 / .75 / .87.
- Diskalakan sesuai ukuran ikon. Animasi tidak ada di Figma (lihat keputusan Button).

### Toggle
- **Properti Figma → parameter**: `Active` → `checked`; `State` → `enabled`; `Size` → `AmarToggleSize`.
- **Token**: track Active `action.primary.background`, Inactive `background.subtle`; knob `surface.base` dengan shadow `Shadow/Floating`. Ukuran 44×24 (knob 20) dan 36×20 (knob 16), padding 2.
- **Disabled**: seluruh toggle opacity 0,5. Ini opacity layer mentah di Figma, tanpa token (`AmarToggleTokens.DisabledOpacity`).
- Knob berpindah tanpa animasi karena Figma tidak mendefinisikan motion.
- **Temuan Figma**: varian Disabled Small Active memakai `Background/Active`, sedangkan Medium memakai `Background/Disabled Active`. Nilainya sama, jadi tidak ada beda visual.

### Selection Control dan Element / Icon
- **Properti Figma → parameter**: `Checked` → `checked`; `Type` → `AmarSelectionType`; `Size` → `AmarSelectionSize`; `Disabled` → `enabled`; `Edit/Show Label` → `label`; `Edit/Show Description` → `description`; `Icon Only` → keduanya `null`. `Indeterminate` hanya ada di `AmarSelectionIcon`.
- **Token**:
  - Kotak checked `Selection/Background`, tanda `Selection/Icon`, border `Selection/Border Color` (1dp).
  - Disabled: kotak opacity 0,5. Kotak unchecked disabled ditambah `Selection/Background Disabled`. Teks memakai `Content/Disabled`.
  - Medium: kotak 24 (radius 6), gap 12, label `Label/LG Medium`, deskripsi `Label/LG`.
  - Small: kotak 16 (radius 4), gap 8, label `Label/MD Medium`, deskripsi `Label/MD`.
- **Aset**: `ds_selection_{check,minus}_{md,sm}.xml` digambar dalam koordinat kotak. Alasannya, Figma mempertahankan ketebalan stroke (2 / 1,67) saat ikon diperkecil, dan koordinat kotak menghindari pembulatan offset 3,6dp. Titik radio digambar sebagai lingkaran float.
- **Verifikasi**: kotak, tanda centang, dan titik radio cocok (rata-rata selisih ≤1,2/255). Posisi X teks sama; Y 1 px lebih tinggi; lebar deskripsi 3 px lebih sempit (rasterizer).
- **Temuan Figma**: varian Radio + `Indeterminate` ada, tetapi tampilannya sama dengan Checked.

### Input, Dropdown, Area (field bersama)
- **State Figma diturunkan otomatis**:
  - Default: kosong.
  - Active: fokus dan kosong (Dropdown: `expanded`).
  - Typing: fokus dan berisi.
  - Filled: tidak fokus dan berisi.
  - Disabled / Disabled - Filled: `enabled = false`.
  - `Error` → `error`.
- **Token** (`Component/Text Field/...` → semantik):
  - Latar `surface.base` / Disabled `background.subtle`.
  - Border `border.strong` (1), fokus `border.focus` (1,5), error `feedback.error.border`, disabled `border.disabled`.
  - Teks `content.primary`, placeholder `content.quaternary`, kursor `border.focus`.
  - Field tinggi 48, radius 10, padding 12/10 × 12, gap 8.
  - Label / field / hint dipisah gap 4.
  - Area: field tetap 96, counter `Caption/SM` rata kanan.
- **Perilaku per Type**:
  - Currency: prefix "Rp", hanya digit, format 24.000.000. Format ini tidak ada di Figma dan diambil dari panduan konten bundle.
  - Phone: prefix "+62", hanya digit.
  - Password: masking "• " (bullet + spasi) seperti di Figma, dengan tombol mata.
  - Search: ikon search di kiri.
- **Aset**: `ds_ic_{search,eye_reveal,eye_hide,chevron_down,info_2}_24.xml`, `ds_ic_info_sm_20.xml`.
- **Verifikasi**:
  - Ukuran semua varian sama.
  - Warna teks Disabled cocok (±1/255).
  - Teks 1 px lebih tinggi (rasterizer).

### Code Verification dan Element / Digit
- 6 kotak 44×44 (radius 10, gap 8). Digit memakai `Heading/SM`. PIN (`Masking`) memakai titik 20 (`Icon/Primary`). Active menampilkan caret statis "|".
- Hint: non-error `Caption/MD` + `Helper/Default` (ikon opsional), error `Caption/SM` + `Helper/Error`.
- Border error: master `Element / Digit` 1,5, tetapi instance di Code Verification di-override ke 1. Keduanya diikuti (`AmarDigit` = 1,5, `AmarCodeVerification` = 1).

### Temuan Figma di kelompok Inputs (mengikuti Figma kecuali disebut lain)
1. **Tidak diikuti, perlu konfirmasi**: jarak antar item di field berubah dari 8 menjadi 4 saat fokus (Input Active/Typing, Dropdown Active). Di kode tetap 8 agar teks dan prefix tidak bergeser saat field diketuk.
2. Label memakai `Label/Default` di semua state. Token `Label/Focused`, `Label/Error`, dan `Label/Disabled` ada tetapi tidak dipakai.
3. Master `Input / Label` memakai `Label/MD Medium`, tetapi semua instance di field memakai `Label/Button/MD` (semibold). Field mengikuti instance.
4. Teks Disabled memakai opacity layer mentah: 0,7 untuk nilai, 0,6 untuk placeholder.
5. Border error setebal 1 saat tidak fokus dan 1,5 saat fokus.
6. Ikon search dan chevron memakai `Icon/Primary` juga saat disabled.
7. Instance ikon info di `Input / Label` kosong (tidak merender apa pun). Kode memakai master `info sm` (`Icon/Primary`).
8. Code Verification `Disabled - Error` (OTP dan PIN) tidak berisi kotak digit, hanya hint. Di kode: digit disabled + hint error.
9. Placeholder `content.quaternary` (#CDD5DC) di atas putih kontrasnya sekitar 1,5:1, di bawah pedoman aksesibilitas.

### Info Box
- **Properti Figma → parameter**: `Type` → `AmarInfoBoxType` (termasuk Neutral); `Message` → `message`; `Show Label` + `Label` → `label`; `Icon` / `Change Icon` → `showIcon` / `icon`; `Button` → `buttonLabel` + `onButtonClick`; `Close` → `onClose`.
- **Token**:
  - Latar `feedback.<type>.surface`, border `feedback.<type>.border` (1), radius 12 (`Radius/Card`).
  - Padding 12; padding bawah 16 bila ada tombol. Gap 12; label ↔ deskripsi 4.
  - Label `Label/MD Medium` + `content.primary`; deskripsi `Label/MD` + `content.secondary`.
  - Tombol: teks naked `Label/Button/MD` berwarna `feedback.<type>.text`.
  - Close `icon.tertiary`.
- **Aset**: ikon `info` dua warna = `ds_infobox_info_circle_20` (warna tipe) + `ds_infobox_info_mark_20` (`Info Box/Action Icon`, putih). Close memakai `ds_infobox_close_20` (stroke 1,67).

### Badge
- **`Supporting` → parameter opsional**: Default (tidak ada), Close (`onClose`), Counter (`count`), Left Icon (`leftIcon`).
- **Padding** per Size × Supporting disalin dari Figma (`AmarBadgeTokens`). Contoh: Medium Default 10/2, Close 10/2/4/2.
- **Teks**: Small `Label/Button/SM`, lainnya `Label/Button/MD`.
- **Warna non-solid**:
  - Latar `feedback.<t>.background` (50), border `borderSubtle` (1), teks `text` (700).
  - Ikon kiri `iconStrong`, close `iconSubtle`, counter `text`.
- **Warna solid**: latar `solidBackground`, teks dan ikon `solidText`, tanpa border.
- **Counter**: lingkaran 16 dengan angka `Label/SM` yang "dilubangi" (Figma: Exclude). Di kode, angka digambar dengan warna latar badge.
- **Aset**: `ds_badge_close_12` (stroke 1,5), `ds_ic_default_16` (ekspor 16 dari Badge).
- **Temuan Figma**: Small + Left Icon tingginya 20 (padding Y 2), sedangkan Small lainnya 22.

### Overlay
- Default `overlay.default` (#0B1220 @40%); Modal `overlay.modal`. Menahan sentuhan; `onClick` opsional untuk menutup.
- Spinner 56 di tengah (hanya State=Default).
- ⚠ **Warna spinner**: instance di Figma meng-override warnanya pada layer tersembunyi, sehingga nilainya tidak terbaca. Master `Icon/Primary` (gelap). Sementara memakai `Icon/Inverse` sampai dikonfirmasi.

### Catatan aset (berlaku umum)
Ketebalan stroke ikon berbeda per instance di Figma: close 20 di Alert stroke 2, di Info Box 1,67, close 12 di Badge 1,5. Ikon `default` juga diekspor dengan potongan berbeda di 16/20/24. Karena itu aset diambil dari ekspor instance tiap komponen, bukan dari satu master yang diskalakan.
