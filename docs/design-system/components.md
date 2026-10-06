# Komponen Design System — Figma → Kotlin

**Katalog**: semua komponen di bawah dapat dilihat dan dicoba di `designsystem/catalog/DesignSystemCatalogScreen.kt`. Di build debug ada ikon launcher terpisah **"DS Catalog"** (`DesignSystemCatalogActivity`, hanya di `src/debug`, tidak ikut ke release). Aset: [assets.md](./assets.md).

Satu baris per komponen Figma yang sudah diimplementasikan. Path Kotlin relatif terhadap
`app/src/main/java/com/example/and_sales_portal/designsystem/`. Inventaris lengkap: [inventory.md](./inventory.md).

| Kelompok | Komponen Figma | Node | Varian | File Kotlin | Composable | Preview | Status |
|---|---|---|---|---|---|---|---|
| Navigation | Navigation Bar - Partner Dashboard | [3306:2306](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=3306-2306) | 3 (Active) | `components/navigation/AmarPartnerNavigationBar.kt` | `AmarPartnerNavigationBar` | 3 | ✅ Terverifikasi visual (ikon rata-rata ≤1,1/255) |
| Navigation | App Bar | [133:32](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=133-32) | 8 (Type × State × Stepper) | `components/navigation/AmarAppBar.kt` | `AmarAppBar` | 6 (Type × State) | ✅ Terverifikasi visual (4 varian, posisi ikon sama persis) |
| Navigation | Tab | [1833:13848](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1833-13848) | 2 (Scrim) | `components/navigation/AmarTab.kt` | `AmarTab` | 2 | ✅ Terverifikasi visual |
| Navigation | Element / Tab Item | [1825:10926](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1825-10926) | 4 (Active × Scrim) | `components/navigation/AmarTab.kt` | `AmarTabItem` | lewat Tab | ✅ Terverifikasi visual (indikator, scrim) |
| Feedback | Modal / Default | [1034:656](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1034-656) | 3 (Scroll State) | `components/feedback/AmarModal.kt` | `AmarModal` | Default + scroll | ✅ Terverifikasi visual (Default: ukuran & posisi tombol sama) |
| Feedback | Element / Illustration Modal | [486:1867](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=486-1867) | 2 (Fullscreen) | `components/feedback/AmarModal.kt` | `AmarModalIllustration` | lewat Modal | ✅ dari spesifikasi Figma |
| Feedback | Info Box | [676:2342](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=676-2342) | 20 (Type × Button × Show Label) | `components/feedback/AmarInfoBox.kt` | `AmarInfoBox` | 5 (per Type) × 4 | ✅ Terverifikasi visual (4 varian) |
| Feedback | Badge | [981:4166](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=981-4166) | 96 (Type × Size × Supporting × Solid) | `components/feedback/AmarBadge.kt` | `AmarBadge` | 8 (Type × Solid) × 12 | ✅ Terverifikasi visual (7 varian) |
| Feedback | Overlay | [163:334](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=163-334) | 2 (State) | `components/feedback/AmarOverlay.kt` | `AmarOverlay` | 3 | ✅ Warna token; spinner `Icon/Inverse` (disetujui) |
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
| Inputs | Element / Progress Photos | [1052:6555](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1052-6555) | 5 (Status) | `components/inputs/AmarUpload.kt` | `AmarProgressPhotos` | 5 varian | ✅ Terverifikasi visual (5 varian: ukuran sama; ikon, progress bar, aksi selisih ≤1,9/255, 2026-10-06) |
| Inputs | Element / Upload | [1052:8467](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1052-8467) | 4 (File Added × Multiple Files) | `components/inputs/AmarUpload.kt` | `AmarUpload` | 4 varian + Show Supporting | ✅ Terverifikasi visual (4 varian: ukuran sama; isi dropzone 1,0/255, posisi konten ±1 px; fase dash berbeda, 2026-10-06) |
| Inputs | Upload Files | [1060:11764](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1060-11764) | 4 (File Added × Multiple Files) | `components/inputs/AmarUpload.kt` | `AmarUploadFiles` | 4 varian | ✅ Terverifikasi visual (4 varian: ukuran sama, border Card selisih 0; isi = Element / Upload, 2026-10-06) |
| Actions | Button | [60:337](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=60-337) | 180 (Type × Size × State × Icon Only × Inverse) | `components/actions/AmarButton.kt` | `AmarButton` | 10 (Type × Inverse), masing-masing 18 varian | ✅ Terverifikasi visual (8 varian representatif, 2026-10-06) |
| Actions | Elements / Button Group | [455:432](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=455-432) | 2 (Stack, Side) | `components/actions/AmarButtonGroup.kt` | `AmarButtonGroup` | 5 kombinasi | ✅ Terverifikasi visual (geometri sama persis) |
| Feedback | Spinner (ikon) | [536:1213](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=536-1213) | 1 | `components/feedback/AmarSpinner.kt` | `AmarSpinner` | lewat Button | ✅ Geometri & opacity sama |
| Feedback | Alert | [683:300](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=683-300) | 12 (Style × Type) | `components/feedback/AmarAlert.kt` | `AmarAlert` | 12 varian + opsi | ✅ Terverifikasi visual (Roborazzi vs Figma, 2026-10-06) |
| Layout & Content | Divider | [1123:142](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1123-142) | 6 (Orientation × Weight) | `components/layout/AmarDivider.kt` | `AmarDivider` | 6 varian | ✅ Terverifikasi visual (warna & ukuran sama dengan Figma, 2026-10-06) |
| Layout & Content | Card | [954:2473](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=954-2473) | 7 (Type × Style; Default + Selected tidak ada) | `components/layout/AmarCard.kt` | `AmarCard` (2 overload: Type=Default, Type=Custom) | 4 (per Style) = 7 varian | ✅ Terverifikasi visual (Default/Default & Custom/Selected: ukuran sama, selisih di luar teks rata-rata 0,17/255, 2026-10-06) |

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
- **Warna spinner**: `Icon/Inverse` (putih), disetujui 2026-10-06. Override di Figma tidak bisa dibaca karena layernya tersembunyi.

### Catatan aset (berlaku umum)
Ketebalan stroke ikon berbeda per instance di Figma: close 20 di Alert stroke 2, di Info Box 1,67, close 12 di Badge 1,5. Ikon `default` juga diekspor dengan potongan berbeda di 16/20/24. Karena itu aset diambil dari ekspor instance tiap komponen, bukan dari satu master yang diskalakan.

### Card
- **Properti Figma → parameter**:
  - `Type` → overload: `AmarCard(label, …)` = Default, `AmarCard(style) { … }` = Custom (isi = `Slot`).
  - `Style` → `AmarCardStyle` (Default, Border, Shadow, Selected). `Type=Default` + `Selected` tidak ada di Figma; overload Default menolaknya dengan `require`.
  - Default: `Edit Description`/`Description` → `description`; `Show Icon`/`Icon` → `icon`; `Show Action` → `showAction` (chevron right); `Show Slot`/`Slot` → `slot`; `Button` → `buttonLabel` + `onButtonClick` (Primary Medium, lebar penuh).
  - `onClick` (bukan properti Figma): seluruh kartu bisa diketuk, tanpa ripple (mengikuti keputusan Button). Selected memberi semantik `selected`.
- **Token** (`AmarCardTokens`):
  - Padding 16 (`Card/Padding`), gap 16 (`Spacing/Gap/LG`) antara heading, slot, dan tombol.
  - Ikon ↔ teks 12 dan label ↔ deskripsi 4 (nilai mentah).
  - Radius `Radius/Container` (16). Latar `Surface/Base`; Selected `Background/Brand Subtle`.
  - Border 1 di sisi dalam: Border `Border/Color/Strong`, Selected `Action/Focus`. Shadow `Shadow/Floating`.
  - Label `Heading/SM` + `Content/Primary`; deskripsi `Label/MD` + `Content/Secondary`; chevron `Icon/Primary`.
- **Lebar**: komponen tidak mengunci lebar (328 di Figma).
- **Verifikasi** (`AmarCardScreenshotTest`, 1x, margin 16): ukuran 328×144 dan 328×66 sama; selisih di luar teks rata-rata 0,17/255; selisih hanya di glyph teks (rasterizer) dan anti-aliasing sudut.
- **Temuan Figma**:
  1. `Type=Default, Style=Shadow` **tidak punya efek shadow** (tampil sama dengan Style=Default); hanya `Type=Custom, Style=Shadow` yang memakai `Shadow/Floating`. Kode memberi `Shadow/Floating` ke kedua Type agar Style konsisten. Perlu konfirmasi desainer.
  2. `Show Icon` default `True` dengan slot 32 kosong, sehingga label di master bergeser 44. Di kode `icon = null` menyembunyikan slot.
  3. Slot di `Type=Default` lebarnya tetap 326 (bukan Fill 296 seperti di Custom). Di kode slot mengisi lebar konten.
  4. Label heading tidak diekspos sebagai properti TEXT di Figma; di kode tetap menjadi parameter `label`.
- **Pemakaian di layar produk**: Custom/Border (Document Upload, 45×), Custom/Shadow (TL Dashboard, 8×).

### Upload Files
- **Komposisi**: `AmarCard(style = Border)` (Type=Custom, Style=Border) berisi `AmarUpload`. Tidak ada token atau override tambahan.
- **Properti Figma → parameter**: `File Added` dan `Multiple Files` diteruskan ke Element / Upload, sama seperti di Figma. Semua parameter lain sama dengan `AmarUpload`.
- **Lebar**: 328 di Figma; komponen tidak mengunci lebar (di layar pakai `fillMaxWidth()` dengan margin 16).
- **Verifikasi** (`AmarUploadScreenshotTest`, 1x, lebar 328): ukuran keempat varian sama (328×104/112/128/256), border Card selisih 0. Selisih sisanya berasal dari isi Element / Upload (fase dash dan glyph teks), lihat catatan Element / Upload.
- **Pemakaian di layar produk**: 45× di Document Upload, semuanya Single (29 kosong, 16 dengan file Uploaded Default).

### Element / Upload
- **Properti Figma → parameter**:
  - `File Added` → diturunkan dari `files` (`null` = False).
  - `Multiple Files` → `multipleFiles`.
  - `Slot` → `files` (biasanya `AmarProgressPhotos`).
  - `Show Supporting` + `Supporting Label` → `supportingLabel`.
- **Parameter tambahan**: `label`, `buttonLabel`, `onUploadClick`, `buttonIcon` (default `camera`), `helperText` (teks di atas dropzone pada Multiple Files; di Figma teks tetap "Kamu bisa upload foto sebanyak 3 kali", tanpa properti).
- **Perilaku**:
  - Single: dropzone **atau** baris file.
  - Multiple: baris file (gap 16), lalu helper + dropzone yang selalu tampil.
  - Batas jumlah file diatur pemanggil.
- **Dropzone**: di Figma ini instance `Button` (Primary, Small, Left Icon) yang di-override, bukan varian Button. Karena itu dibuat sebagai composable privat:
  - Latar `Component/Upload/Dropzone/Background` (`Background/Brand Subtle`).
  - Border putus-putus `Component/Upload/Dropzone/Border` (`Border/Color/Focus`) 1,5 di sisi dalam, dash 6 / jarak 6.
  - Radius `Radius/CTA` 16, tinggi 40, padding 12, gap 4.
  - Ikon 16 `Icon/Brand`; label `Label/Button/SM`.
- **Token**: `AmarUploadTokens` (gap label 12 `Spacing/Gap/MD`, seksi 8 `Spacing/Gap/SM`, baris file 16 `Spacing/Gap/LG`; nilai mentah dropzone: tebal border 1,5 dan dash 6/6). Ukuran tombol diambil dari `AmarButtonTokens.Small`.
- **Keputusan**: label dropzone memakai `Content/Link` (Primary 700), bukan Primary 600 seperti di Figma (`Action/Primary/Background`), sesuai aturan kontras teks #6 (sama dengan Navigation Bar).
- **Verifikasi** (`AmarUploadScreenshotTest`, 1x, lebar 296):
  - Ukuran keempat varian sama (296×72/80/96/224).
  - Isi dropzone selisih 1,0/255; posisi ikon, teks, dan label ±1 px.
  - **Fase dash berbeda**: periodenya sama (6/6), tetapi titik mulai dan arah jalur garis di Figma dan Android berbeda, sehingga tidak ada satu fase yang cocok untuk keempat sisi. Percobaan fase 6 memperbaiki tepi atas tetapi memperburuk tepi bawah (rata-rata 9,48 → 10,69), jadi tetap memakai fase 0.
- **Temuan Figma**:
  1. Teks dropzone Primary 600 (gagal AA untuk teks); di kode Primary 700.
  2. `Supporting Label` lebarnya tetap 326, melebihi komponen (296). Di kode mengisi lebar komponen, rata tengah.
  3. Helper "Kamu bisa upload foto sebanyak 3 kali" berupa teks tetap tanpa properti.
  4. Ikon kamera di dropzone tetap memakai stroke 1,2 (skala dari 1,8 di master 24), sama dengan aset `ds_ic_camera_24` di ukuran 16.
- **Pemakaian di layar produk**: 45× lewat Upload Files, semuanya Single. Label tombol "Ambil Dokumen" / "Ambil Foto", ikon `upload document`.

### Element / Progress Photos
- **Properti Figma → parameter**: `Status` → `AmarProgressPhotosStatus` (Uploaded, UploadedDefault, Waiting, Loading, Failed); `Edit Name` → `fileName`; `File Size` → `fileSize` (string yang sudah diformat).
- **Parameter tambahan** (bukan properti Figma):
  - `thumbnail` (Uploaded; `null` = kotak `Background/Subtle`).
  - `progress` 0..1 (Loading).
  - `statusText`, `errorText`, `retryLabel` (default = copy Figma di `strings.xml`).
  - `onRetry` (tombol Naked Small di Failed).
  - `onDelete` (Trash; di Loading ikon batal `delete, error, disabled`).
  - `onFileClick` (nama file berwarna link di Uploaded).
- **Layout per Status**:
  - Uploaded: thumbnail 56 (radius 8), nama `Label/Button/SM` + `Content/Link`, Trash; padding vertikal 2.
  - Uploaded Default: ikon `files` 20 `Icon/Primary` menggantikan thumbnail.
  - Waiting / Loading: ikon `camera` 20 `Icon/Disabled`; nama `Label/SM` + `Content/Secondary`; progress bar 6 (track `Background/Subtle`, isi `Feedback/Success/Solid Background`, radius penuh); baris ukuran + spinner 14 `Icon/Tertiary` + status `Label/SM` `Content/Tertiary`. Tanpa padding vertikal. Waiting: bar kosong, aksi Trash.
  - Failed: nama di warna error (`Text Field/Border/Error`), `AmarInputHint` error dengan ikon, tombol Naked Small; padding vertikal 2.
  - Aksi (20) selalu di tengah vertikal baris; konten Waiting/Loading/Failed rata atas.
- **Token** (`AmarUploadTokens`): `Upload/Padding/Vertical` 2, gap 8 (`Spacing/Gap/SM`), 4 (`Spacing/Gap/XS`), `Upload/Gap/Progress` 6. Nilai mentah: tinggi bar 6, thumbnail 56, radius thumbnail 8, spinner 14.
- **Aset**: `ds_ic_{trash,files,camera}_24` dan `ds_ic_error_24_l0/_l1`, ditampilkan 20. Stroke di instance 20 adalah skala proporsional dari master 24 (2 → 1,67; 1,8 → 1,5), jadi tidak perlu ekspor baru.
- **Verifikasi** (`AmarProgressPhotosScreenshotTest`, 1x, lebar 296): ukuran kelima varian sama (296×60/24/48/48/64). Ikon, progress bar, dan aksi selisih rata-rata 0,16–1,9/255 tanpa pergeseran. Selisih sisanya di glyph teks (rasterizer).
- **Temuan Figma**:
  1. Copy status menyebut "foto" ("proses upload foto...", "Gagal upload foto, coba lagi.", "Ambil Foto Ulang"), padahal layar Document Upload memakai komponen ini untuk PDF dengan teks yang sama. Karena itu copy dijadikan parameter.
  2. "Ambil Foto Ulang" memakai Title Case; aturan copy bundle meminta sentence case. Kode mengikuti Figma; perlu konfirmasi.
  3. Ukuran file di Figma memakai titik desimal ("3.2 MB"); format Indonesia memakai koma ("3,2 MB"). Format diserahkan ke pemanggil.
  4. Status Waiting juga menampilkan spinner, sama seperti Loading.
  5. Tidak ada state disabled/pressed untuk Trash dan tombol batal; area sentuhnya 20dp (pedoman Android 48dp).
- **Pemakaian di layar produk**: Uploaded Default 15× dan Loading 1× (Document Upload).

### Modal / Default dan Element / Illustration Modal
- **Properti Figma → parameter**: `Show Title`/`TItle` → `title`; `Show Description`/`Description` → `description`; `Show Illustration` → `illustration`; `Show Slot`/`Slot` → `slot`; Button Group Side → `primaryLabel` + `secondLabel`. `Scroll State` diturunkan otomatis.
- **Keputusan (disetujui 2026-10-06)**:
  - Lebar 328, mengikuti komponen. Variabel `Modal/Default/Width` dan ilustrasi bernilai 342.
  - Efek tepi atas Scroll - Bottom berupa gradasi `Background/Base` 90% → transparan setinggi 40. Background blur dari Figma **tidak bisa** dibuat di Compose di versi Android mana pun (Compose tidak bisa mem-blur isi di belakang layer), jadi gradasi dipakai di semua versi.
- **Layout**:
  - Default: padding 16, gap konten ↔ tombol 24, teks ↔ slot 16, judul ↔ deskripsi 8.
  - Saat konten bisa di-scroll: konten dan footer masing-masing padding 16. Footer diberi `Shadow/Modal` selama masih ada konten di bawah.
- **Token**: radius 16 (`Radius/Container`), latar `background.base`, `Shadow/Modal`. Judul `Heading/MD` + `content.primary`; deskripsi `Body/LG` + `content.secondary`, rata tengah.
- **Ilustrasi**: Fullscreen = selebar modal tanpa padding. Selain itu gambar 80 di tengah dengan padding atas 24, bawah 8.
- **Temuan Figma**: frame teks (310) lebih lebar dari area konten (296).

### App Bar
- **Properti Figma → parameter**:
  - `Type` → `AmarAppBarType`; `State` → `AmarAppBarState`.
  - `Title` + `Change Label` → `title`; subjudul (Primary + Transparent Inverse) → `subtitle`.
  - `Show Navigation` + `Change Navigation` → `onNavigationClick` + `navigationIcon`.
  - `Right Icon` + `Change Icon` → `onActionClick` + `actionIcon`.
  - `Stepper=True` + `Slot` → `slot`.
- **Status bar**: frame Figma memuat status bar mock (24, gaya iOS). Di Android bar digambar di belakang status bar sistem lewat `windowInsets` (default `WindowInsets.statusBars`), dan yang di-layout hanya header 56.
- **Warna (sesuai render Figma dan layar produk)**:
  - Default: bar putih, judul dan ikon #000A14, `Shadow/Floating`.
  - Transparent: Primary gelap, Secondary putih.
  - Transparent Inverse: judul Neutral 900, ikon Neutral 700.
  - **Temuan Figma**: fill bar terikat ke variabel `.../Title` (putih) dan judul ke `.../Background` (#000A14). Nama variabelnya tertukar, sedangkan nilai variabel `App Bar/Type/Default/*` menggambarkan bar gelap. Layar produk (Sales - Partner Dashboard - Android, 36 App Bar) semuanya putih, jadi render itulah yang diikuti.
- **Layout**:
  - Primary: judul kiri + aksi (notification).
  - Secondary: back + judul tengah (maks 294) + aksi (help).
  - Secondary + Transparent Inverse: judul kiri.
  - Primary + Transparent Inverse: judul + subjudul + tombol bulat 28 (`Icon/Close Sheet/Background`).
- **Aset**: `ds_ic_arrow_left_24`, `ds_ic_help_24`, `ds_ic_notification_24`, `ds_ic_search_2_24`. Ikon help dan notification diekspor Figma sebagai grup terpisah, lalu ditempatkan di bingkai 24 sesuai offset Figma.
- **Temuan Figma**: varian Primary + `Stepper=True` identik dengan `Stepper=False` (tidak ada slot).

### Tab dan Element / Tab Item
- **Token**:
  - Tinggi 48, padding samping container 16, divider bawah `Border/Color/Default`.
  - Label aktif `Label/MD Semibold` Primary 700; nonaktif `Label/MD Medium` Neutral 500.
  - Ikon 20: aktif Primary 600 (`icon.brand`), nonaktif `Icon/Tertiary`.
  - Indikator 2 Primary 600, digambar di atas divider. Gap ikon ↔ label 6 (nilai mentah; token `Tab/Gap` = 4 tidak dipakai).
- **Scrim** (`Scrim=True`): placeholder loading 109×20, radius 16, gradasi abu dengan nilai mentah di `AmarTabTokens.ScrimColors`.

### Navigation Bar - Partner Dashboard
- **Properti Figma → parameter**: `Active` → `AmarPartnerDestination` (Beranda, Partner, Tim); label dari `strings.xml`.
- **Keputusan (disetujui 2026-10-06)**:
  - Tinggi 56 sesuai komponen. Variabel `Navigation Bar/Height` = 117 dan `Navigation/Bottom/Height` = 64 tidak dipakai.
  - Warna ikon di Figma memakai variabel dari library lain (`icon/strong-950`, `Icon/Inverse`, `Icon/Disabled`). Dipetakan ke token lokal bernama sama: aktif `Action/Focus` + `Icon/Inverse`, nonaktif `Icon/Disabled`.
  - Label aktif Primary 700 (`Navigation/Bottom/Title`), bukan Primary 600 seperti di Figma (aturan kontras teks #6).
  - Hanya versi Partner Dashboard yang dibangun, karena itu yang dipakai di layar produk.
- **Token**:
  - Latar `surface.base`, `Shadow/Overlay`.
  - Item: padding atas 8, bawah 4, gap 4, ikon 24.
  - Indikator atas 4 `Action/Focus`.
  - Label aktif `Label/Button/SM`, nonaktif `Caption/SM` Neutral 400.
  - Bar digambar di belakang navigation bar sistem lewat `windowInsets`.
- **Aset**: `ds_ic_menu_{home,store,profile}_24` (outline) dan versi `_filled_24`.
  - Ikon aktif dua warna dipecah menjadi layer berurutan (`_l0…_l5`) yang di-tint `Action/Focus` / `Icon/Inverse`, agar urutan tumpukan sama dengan Figma.
  - Mask `user-fill` (stroke di sisi dalam bentuk) dikonversi menjadi `clip-path`.
  - Master `menu / store` tingginya 24,5; drawable-nya diskalakan 24/24,5 secara vertikal seperti render Figma.
- **Temuan Figma**: di varian Partner dan Tim, label "Beranda" nonaktif memakai `Label/Button/SM` (semibold), sedangkan label nonaktif lain `Caption/SM`. Kode memakai `Caption/SM` untuk semua label nonaktif.
