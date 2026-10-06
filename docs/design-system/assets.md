# Aset Design System — ikon & ilustrasi

Dibuat di langkah H (2026-10-06) dari Figma **Internal Project DS Lib - Android**. Semua aset berupa VectorDrawable di `app/src/main/res/drawable/`.

## Cara pakai

- **Ikon satu warna** (`ds_ic_*`): warna di file = hitam; beri tint token, contoh `Icon(painterResource(R.drawable.ds_ic_calendar_24), null, tint = AmarTheme.colors.icon.primary)`.
- **Ikon multi-warna** (`ds_ic_*_l0`, `_l1`, …): tumpuk layer berurutan dalam satu `Box`, masing-masing di-tint sesuai kolom *Peran warna* (sama seperti `AmarPartnerNavigationBar`).
- **Ilustrasi** (`ds_ill_*`): berwarna penuh, **jangan di-tint**: `Image(painterResource(R.drawable.ds_ill_medium_otp), null)`. Warna ilustrasi adalah aset gambar, bukan token UI (pengecualian aturan #4).
- Konversi: `Svg2Vector` (Android Studio, `com.android.tools:sdk-common:31.9.1`) dari ekspor SVG Figma; tidak ada error konversi.

## Ikon umum (57 ikon, 67 file)

| Ikon | Node | Ukuran | File | Peran warna (Figma) |
|---|---|---|---|---|
| account_card | [1262:5391](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1262-5391) | 24 | `ds_ic_account_card_24` | tint bebas (Figma: Icon/Primary) |
| add | [1644:6206](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1644-6206) | 24 | `ds_ic_add_24_l0`<br>`ds_ic_add_24_l1` | Icon/Primary → Icon/Inverse |
| arrow_left_right | [1177:186](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1177-186) | 24 | `ds_ic_arrow_left_right_24` | tint bebas (Figma: Icon/Primary) |
| arrow_right | [46:407](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=46-407) | 24 | `ds_ic_arrow_right_24` | tint bebas (Figma: Icon/Primary) |
| arrow_up_down | [3094:6795](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=3094-6795) | 24 | `ds_ic_arrow_up_down_24` | tint bebas (Figma: Icon/Primary) |
| backward | [492:461](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=492-461) | 24 | `ds_ic_backward_24` | tint bebas (Figma: Icon/Primary) |
| bank | [1121:419](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1121-419) | 24 | `ds_ic_bank_24` | tint bebas (Figma: Icon/Primary) |
| calendar | [732:1299](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=732-1299) | 24 | `ds_ic_calendar_24` | tint bebas (Figma: Icon/Primary) |
| camera | [492:462](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=492-462) | 24 | `ds_ic_camera_24` | tint bebas (Figma: Icon/Primary) |
| check | [46:426](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=46-426) | 24 | `ds_ic_check_24` | tint bebas (Figma: Icon/Primary) |
| chevron_down_double | [1125:363](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1125-363) | 24 | `ds_ic_chevron_down_double_24` | tint bebas (Figma: Icon/Primary) |
| chevron_left | [46:405](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=46-405) | 24 | `ds_ic_chevron_left_24` | tint bebas (Figma: Icon/Primary) |
| chevron_right | [46:412](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=46-412) | 24 | `ds_ic_chevron_right_24` | tint bebas (Figma: Icon/Primary) |
| chevron_up | [46:416](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=46-416) | 24 | `ds_ic_chevron_up_24` | tint bebas (Figma: Icon/Primary) |
| clock | [1103:321](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1103-321) | 24 | `ds_ic_clock_24_l0`<br>`ds_ic_clock_24_l1` | Icon/Primary → Icon/Inverse |
| close | [46:434](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=46-434) | 24 | `ds_ic_close_24` | tint bebas (Figma: Icon/Primary) |
| close_sheet_28 | [792:2290](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=792-2290) | 28 | `ds_ic_close_sheet_28_l0`<br>`ds_ic_close_sheet_28_l1` | Background/Subtle → Icon/Secondary |
| contact | [1080:12859](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1080-12859) | 24 | `ds_ic_contact_24` | tint bebas (Figma: Icon/Primary) |
| copy | [1289:1082](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1289-1082) | 24 | `ds_ic_copy_24` | tint bebas (Figma: Icon/Primary) |
| edit_content | [1098:589](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1098-589) | 24 | `ds_ic_edit_content_24` | tint bebas (Figma: Icon/Primary) |
| email | [1145:814](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1145-814) | 24 | `ds_ic_email_24` | tint bebas (Figma: Icon/Primary) |
| error | [46:428](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=46-428) | 24 | `ds_ic_error_24_l0`<br>`ds_ic_error_24_l1` | Icon/Primary → Icon/Inverse |
| face_id | [146:513](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=146-513) | 24 | `ds_ic_face_id_24` | tint bebas (Figma: Icon/Primary) |
| files | [1050:4674](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1050-4674) | 24 | `ds_ic_files_24` | tint bebas (Figma: Icon/Primary) |
| files_search | [1098:550](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1098-550) | 24 | `ds_ic_files_search_24` | tint bebas (Figma: Icon/Primary) |
| filter_16 | [3078:6719](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=3078-6719) | 16 | `ds_ic_filter_16` | tint bebas (Figma: Icon/Primary) |
| fingerprint | [1365:660](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1365-660) | 24 | `ds_ic_fingerprint_24` | tint bebas (Figma: Icon/Primary) |
| folder_fill | [3163:4011](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=3163-4011) | 24 | `ds_ic_folder_fill_24` | tint bebas (Figma: Icon/Primary) |
| folder_line | [3163:4010](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=3163-4010) | 24 | `ds_ic_folder_line_24` | tint bebas (Figma: Icon/Primary) |
| hourglass | [1749:4774](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1749-4774) | 24 | `ds_ic_hourglass_24` | tint bebas (Figma: Icon/Primary) |
| list | [1831:13775](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1831-13775) | 24 | `ds_ic_list_24` | tint bebas (Figma: Icon/Primary) |
| location | [896:251](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=896-251) | 24 | `ds_ic_location_24` | tint bebas (Figma: Icon/Primary) |
| lock | [1120:363](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1120-363) | 24 | `ds_ic_lock_24` | tint bebas (Figma: Icon/Primary) |
| logout | [1173:1599](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1173-1599) | 24 | `ds_ic_logout_24` | tint bebas (Figma: Icon/Primary) |
| minus | [280:688](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=280-688) | 24 | `ds_ic_minus_24` | tint bebas (Figma: Icon/Primary) |
| notification_active | [954:1741](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=954-1741) | 24 | `ds_ic_notification_active_24_l0`<br>`ds_ic_notification_active_24_l1` | Icon/Primary → Icon/Danger |
| phone | [3113:7305](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=3113-7305) | 24 | `ds_ic_phone_24` | tint bebas (Figma: Icon/Primary) |
| photo | [3372:516](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=3372-516) | 24 | `ds_ic_photo_24` | tint bebas (Figma: Icon/Primary) |
| pin_edit | [1178:257](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1178-257) | 24 | `ds_ic_pin_edit_24` | tint bebas (Figma: Icon/Primary) |
| plus | [3084:6753](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=3084-6753) | 24 | `ds_ic_plus_24` | tint bebas (Figma: Icon/Inverse) |
| protection | [1739:2374](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1739-2374) | 24 | `ds_ic_protection_24_l0`<br>`ds_ic_protection_24_l1` | Icon/Primary → Icon/Inverse |
| qr | [1178:267](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1178-267) | 24 | `ds_ic_qr_24` | tint bebas (Figma: Icon/Primary) |
| refresh | [1276:5523](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1276-5523) | 24 | `ds_ic_refresh_24` | tint bebas (Figma: Icon/Primary) |
| share | [1278:5535](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1278-5535) | 24 | `ds_ic_share_24` | tint bebas (Figma: Icon/Primary) |
| star | [1655:7495](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1655-7495) | 24 | `ds_ic_star_24` | tint bebas (Figma: Content/Quaternary) |
| success | [46:423](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=46-423) | 24 | `ds_ic_success_24_l0`<br>`ds_ic_success_24_l1` | Icon/Primary → Icon/Inverse |
| target | [3110:7287](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=3110-7287) | 24 | `ds_ic_target_24_l0`<br>`ds_ic_target_24_l1`<br>`ds_ic_target_24_l2` | Icon/Primary → Icon/Inverse → Icon/Primary |
| todo_fill | [3163:3866](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=3163-3866) | 24 | `ds_ic_todo_fill_24` | tint bebas (Figma: Icon/Primary) |
| todo_line | [3163:3865](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=3163-3865) | 24 | `ds_ic_todo_line_24` | tint bebas (Figma: Icon/Disabled) |
| trash | [1050:3821](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1050-3821) | 24 | `ds_ic_trash_24` | tint bebas (Figma: Icon/Primary) |
| upload | [492:460](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=492-460) | 24 | `ds_ic_upload_24` | tint bebas (Figma: Icon/Primary) |
| upload_cloud | [1019:1703](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1019-1703) | 24 | `ds_ic_upload_cloud_24` | tint bebas (Figma: Icon/Primary) |
| upload_document | [1098:517](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1098-517) | 24 | `ds_ic_upload_document_24` | tint bebas (Figma: Icon/Primary) |
| warning | [509:747](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=509-747) | 24 | `ds_ic_warning_24_l0`<br>`ds_ic_warning_24_l1` | Icon/Primary → Icon/Inverse |
| whatsapp_fill | [3199:2298](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=3199-2298) | 24 | `ds_ic_whatsapp_fill_24` | tint bebas (Figma: Icon/Inverse) |
| wifi_off | [719:130](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=719-130) | 24 | `ds_ic_wifi_off_24` | tint bebas (Figma: Icon/Primary) |
| wifi_on | [719:141](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=719-141) | 24 | `ds_ic_wifi_on_24` | tint bebas (Figma: Icon/Primary) |

Ikon yang sudah ada sebelumnya (dibuat bersama komponen): `ds_ic_default_16/20/24`, `ds_ic_close_20`, `ds_infobox_close_20`, `ds_badge_close_12`, `ds_ic_search_24`, `ds_ic_search_2_24`, `ds_ic_eye_reveal_24`, `ds_ic_eye_hide_24`, `ds_ic_chevron_down_24`, `ds_ic_info_2_24`, `ds_ic_info_sm_20`, `ds_infobox_info_*_20`, `ds_ic_arrow_left_24`, `ds_ic_help_24`, `ds_ic_notification_24`, `ds_ic_menu_*`, `ds_selection_*`.

Catatan: `star_filled` (1655:7501) = geometri `star` dengan warna #F2CF53 → pakai `ds_ic_star_24` dengan tint. `whatsapp_fill` dan `plus` berwarna putih di Figma (untuk latar berwarna).

## Logo (6 file)

| Logo | Node | Ukuran | File | Catatan |
|---|---|---|---|---|
| Amar Bank Color=True, Horizontal | [33:176](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=33-176) | 165×30 | `ds_logo_amarbank_color_horizontal` | dipakai di Login |
| Amar Bank Color=True, Vertical | [33:277](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=33-277) | 64×32 | `ds_logo_amarbank_color_vertical` | - |
| Amar Bank Color=True, Logo | [33:378](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=33-378) | 32×24 | `ds_logo_amarbank_color_logo` | - |
| Amar Bank Color=False, Horizontal | [33:178](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=33-178) | 165×30 | `ds_logo_amarbank_white_horizontal` | nama putih |
| Amar Bank Color=False, Vertical | [33:311](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=33-311) | 64×32 | `ds_logo_amarbank_white_vertical` | nama putih |
| Amar Bank Color=False, Logo | [33:421](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=33-421) | 32×24 | `ds_logo_amarbank_white_logo` | putih + `Background/Subtle` |

Diekspor lewat `download_assets` (SVG), latar kanvas `#F5F5F5` dan garis component set `#8A38F5` dibuang, lalu dikonversi dengan `Svg2Vector` (classpath: sdk-common, common 31.9.1, guava, kotlin-stdlib). Berwarna penuh, jangan di-tint; pakai lewat `AmarLogo`.

## Ilustrasi (16) — semua terverifikasi visual (rata-rata Δ ≤ 1,12/255 vs screenshot Figma)

| Ilustrasi | Node | Ukuran | File | Ukuran file | Catatan |
|---|---|---|---|---|---|
| medium_stopper | [822:227](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=822-227) | 138×100 | `ds_ill_medium_stopper` | 11 KB | mask alpha buram → clip-path |
| medium_wifi_problem | [1654:7233](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1654-7233) | 138×100 | `ds_ill_medium_wifi_problem` | 14 KB | mask alpha buram → clip-path |
| medium_system_problem | [1654:7378](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1654-7378) | 138×100 | `ds_ill_medium_system_problem` | 17 KB | mask alpha buram → clip-path |
| medium_rejected | [1114:1254](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1114-1254) | 138×100 | `ds_ill_medium_rejected` | 12 KB | mask alpha buram → clip-path |
| medium_otp | [1241:1237](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1241-1237) | 138×100 | `ds_ill_medium_otp` | 15 KB | mask alpha buram → clip-path |
| medium_in_process | [819:128](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=819-128) | 138×100 | `ds_ill_medium_in_process` | 4 KB | - |
| large_failed_get_data | [1224:2161](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1224-2161) | 236×160 | `ds_ill_large_failed_get_data` | 11 KB | - |
| large_success | [1264:5509](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1264-5509) | 236×160 | `ds_ill_large_success` | 5 KB | mask alpha buram → clip-path |
| medium_email_notif | [1260:5383](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1260-5383) | 138×100 | `ds_ill_medium_email_notif` | 4 KB | 13 layer Figma disusun ulang |
| large_email_notif | [1264:5510](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1264-5510) | 236×160 | `ds_ill_large_email_notif` | 4 KB | 13 layer Figma disusun ulang |
| file_document | [1050:3694](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1050-3694) | 40×40 | `ds_ill_file_document` | 1 KB | 2 layer Figma disusun ulang; mask alpha buram → clip-path |
| decorative_check | [1107:2478](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1107-2478) | 32×32 | `ds_ill_decorative_check` | 2 KB | 10 layer Figma disusun ulang |
| medium_permission_location | [1219:1485](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1219-1485) | 138×100 | `ds_ill_medium_permission_location` | 13 KB | 7 layer Figma disusun ulang; mask alpha buram → clip-path; lencana gradien CSS → circle SVG |
| medium_permission_camera | [1294:1125](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1294-1125) | 138×100 | `ds_ill_medium_permission_camera` | 12 KB | 7 layer Figma disusun ulang; mask alpha buram → clip-path; lencana gradien CSS → circle SVG |
| medium_permission_device_data | [896:535](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=896-535) | 138×100 | `ds_ill_medium_permission_device_data` | 15 KB | 9 layer Figma disusun ulang; mask alpha buram → clip-path; lencana gradien CSS → circle SVG |
| medium_empty_search | [1841:14168](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1841-14168) | 138×100 | `ds_ill_medium_empty_search` | 13 KB | 7 layer Figma disusun ulang; mask alpha buram → clip-path; lencana gradien CSS → circle SVG |

## Gagal / tidak dikonversi (tidak dibuang diam-diam)

| Aset | Node | Alasan | Alternatif |
|---|---|---|---|
| Permission / Accessibility, Tracking, Camera, Photos, Microphone, Contact, Location; Face ID (80×80) | 406:562, 406:744, 406:742, 406:740, 406:743, 406:741, 412:172, 452:278 | Berisi **gambar raster tertanam**; SVG 1,6–3,6 MB per aset. Tidak bisa menjadi vector, dan terlalu berat. | Desainer mengekspor WebP/PNG @2x/@3x dari Figma (±5–20 KB per densitas). Lewat MCP hanya tersedia screenshot 1x (80×80), yang buram di layar xxhdpi, jadi tidak dibuat dari sini. |
| Medium / Max Attempt OTP, Biometric, PIN, Verif Activity | 1138:784, 1355:1450, 1163:1601, 1260:5318 | Tersusun dari banyak layer dengan **CSS mask + rotasi**; Figma tidak mengekspor satu SVG, dan VectorDrawable tidak mendukung mask. | Desainer meratakan (flatten) layer di Figma, lalu konversi ulang; atau WebP. |

## Sengaja tidak dibuat

| Aset | Alasan |
|---|---|
| Ilustrasi Tunaiku (37), ikon banking 32×32 (Action, Biller, Account), Account / ISP, Amar Bank Bisnis | Dicoret di inventaris (produk lain / banking). |
| Medium / Add Account, Add Favorites, Add PLN; Decorative / Money, Bank; Touch ID | Bukan prioritas aplikasi sales (banking / iOS). |
| menu / Visit, Workplan (filled & outline) | Navigation Bar versi Visit/Workplan tidak dibangun. |
| Spinner | Diimplementasikan sebagai `AmarSpinner` (Canvas, beranimasi). |
| Search Button 28, Add 28 | Komposit tombol (latar + ikon), bukan ikon; tombol bulat ada di `AmarAppBar`. |
| Duplikat: chevron / down double 288:735, lock 1120:390 | Varian kedua dari nama yang sama; dipakai 1125:363 dan 1120:363. |
