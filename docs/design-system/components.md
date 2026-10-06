# Komponen Design System — Figma → Kotlin

Satu baris per komponen Figma yang sudah diimplementasikan. Path Kotlin relatif terhadap
`app/src/main/java/com/example/and_sales_portal/designsystem/`. Inventaris lengkap: [inventory.md](./inventory.md).

| Kelompok | Komponen Figma | Node | Varian | File Kotlin | Composable | Preview | Status |
|---|---|---|---|---|---|---|---|
| Feedback | Alert | [683:300](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=683-300) | 12 (Style × Type) | `components/feedback/AmarAlert.kt` | `AmarAlert` | 12 varian + opsi | ✅ Terverifikasi visual (Roborazzi vs Figma, 2026-10-06) |

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
