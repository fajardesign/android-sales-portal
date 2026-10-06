# Komponen Design System — Figma → Kotlin

Satu baris per komponen Figma yang sudah diimplementasikan. Path Kotlin relatif terhadap
`app/src/main/java/com/example/and_sales_portal/designsystem/`. Inventaris lengkap: [inventory.md](./inventory.md).

| Kelompok | Komponen Figma | Node | Varian | File Kotlin | Composable | Preview | Status |
|---|---|---|---|---|---|---|---|
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
