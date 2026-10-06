package com.example.and_sales_portal.designsystem.catalog

import androidx.annotation.DrawableRes
import com.example.and_sales_portal.R

/** Generated from the step H conversion report (docs/design-system/assets.md). */
internal class CatalogIcon(val name: String, @DrawableRes val res: Int)

/** Colour role of one layer of a multi-colour icon (Figma colour → token). */
internal enum class IconRole { Primary, Inverse, Danger, Subtle, Secondary, Disabled, Quaternary }

internal class CatalogLayeredIcon(val name: String, val layers: List<Pair<Int, IconRole>>)

internal object CatalogAssets {
    val icons = listOf(
        CatalogIcon("account_card", R.drawable.ds_ic_account_card_24),
        CatalogIcon("arrow_left_right", R.drawable.ds_ic_arrow_left_right_24),
        CatalogIcon("arrow_right", R.drawable.ds_ic_arrow_right_24),
        CatalogIcon("arrow_up_down", R.drawable.ds_ic_arrow_up_down_24),
        CatalogIcon("backward", R.drawable.ds_ic_backward_24),
        CatalogIcon("bank", R.drawable.ds_ic_bank_24),
        CatalogIcon("calendar", R.drawable.ds_ic_calendar_24),
        CatalogIcon("camera", R.drawable.ds_ic_camera_24),
        CatalogIcon("check", R.drawable.ds_ic_check_24),
        CatalogIcon("chevron_down_double", R.drawable.ds_ic_chevron_down_double_24),
        CatalogIcon("chevron_left", R.drawable.ds_ic_chevron_left_24),
        CatalogIcon("chevron_right", R.drawable.ds_ic_chevron_right_24),
        CatalogIcon("chevron_up", R.drawable.ds_ic_chevron_up_24),
        CatalogIcon("close", R.drawable.ds_ic_close_24),
        CatalogIcon("contact", R.drawable.ds_ic_contact_24),
        CatalogIcon("copy", R.drawable.ds_ic_copy_24),
        CatalogIcon("edit_content", R.drawable.ds_ic_edit_content_24),
        CatalogIcon("email", R.drawable.ds_ic_email_24),
        CatalogIcon("face_id", R.drawable.ds_ic_face_id_24),
        CatalogIcon("files", R.drawable.ds_ic_files_24),
        CatalogIcon("files_search", R.drawable.ds_ic_files_search_24),
        CatalogIcon("filter_16", R.drawable.ds_ic_filter_16),
        CatalogIcon("fingerprint", R.drawable.ds_ic_fingerprint_24),
        CatalogIcon("folder_fill", R.drawable.ds_ic_folder_fill_24),
        CatalogIcon("folder_line", R.drawable.ds_ic_folder_line_24),
        CatalogIcon("hourglass", R.drawable.ds_ic_hourglass_24),
        CatalogIcon("list", R.drawable.ds_ic_list_24),
        CatalogIcon("location", R.drawable.ds_ic_location_24),
        CatalogIcon("lock", R.drawable.ds_ic_lock_24),
        CatalogIcon("logout", R.drawable.ds_ic_logout_24),
        CatalogIcon("minus", R.drawable.ds_ic_minus_24),
        CatalogIcon("phone", R.drawable.ds_ic_phone_24),
        CatalogIcon("photo", R.drawable.ds_ic_photo_24),
        CatalogIcon("pin_edit", R.drawable.ds_ic_pin_edit_24),
        CatalogIcon("plus", R.drawable.ds_ic_plus_24),
        CatalogIcon("qr", R.drawable.ds_ic_qr_24),
        CatalogIcon("refresh", R.drawable.ds_ic_refresh_24),
        CatalogIcon("share", R.drawable.ds_ic_share_24),
        CatalogIcon("star", R.drawable.ds_ic_star_24),
        CatalogIcon("todo_fill", R.drawable.ds_ic_todo_fill_24),
        CatalogIcon("todo_line", R.drawable.ds_ic_todo_line_24),
        CatalogIcon("trash", R.drawable.ds_ic_trash_24),
        CatalogIcon("upload", R.drawable.ds_ic_upload_24),
        CatalogIcon("upload_cloud", R.drawable.ds_ic_upload_cloud_24),
        CatalogIcon("upload_document", R.drawable.ds_ic_upload_document_24),
        CatalogIcon("whatsapp_fill", R.drawable.ds_ic_whatsapp_fill_24),
        CatalogIcon("wifi_off", R.drawable.ds_ic_wifi_off_24),
        CatalogIcon("wifi_on", R.drawable.ds_ic_wifi_on_24),
        CatalogIcon("default", R.drawable.ds_ic_default_24),
        CatalogIcon("arrow_left", R.drawable.ds_ic_arrow_left_24),
        CatalogIcon("chevron_down", R.drawable.ds_ic_chevron_down_24),
        CatalogIcon("search", R.drawable.ds_ic_search_24),
        CatalogIcon("search_2", R.drawable.ds_ic_search_2_24),
        CatalogIcon("eye_reveal", R.drawable.ds_ic_eye_reveal_24),
        CatalogIcon("eye_hide", R.drawable.ds_ic_eye_hide_24),
        CatalogIcon("help", R.drawable.ds_ic_help_24),
        CatalogIcon("notification", R.drawable.ds_ic_notification_24),
        CatalogIcon("info_2", R.drawable.ds_ic_info_2_24),
    )

    val layeredIcons = listOf(
        CatalogLayeredIcon("add", listOf(R.drawable.ds_ic_add_24_l0 to IconRole.Primary, R.drawable.ds_ic_add_24_l1 to IconRole.Inverse)),
        CatalogLayeredIcon("clock", listOf(R.drawable.ds_ic_clock_24_l0 to IconRole.Primary, R.drawable.ds_ic_clock_24_l1 to IconRole.Inverse)),
        CatalogLayeredIcon("close_sheet_28", listOf(R.drawable.ds_ic_close_sheet_28_l0 to IconRole.Subtle, R.drawable.ds_ic_close_sheet_28_l1 to IconRole.Secondary)),
        CatalogLayeredIcon("error", listOf(R.drawable.ds_ic_error_24_l0 to IconRole.Primary, R.drawable.ds_ic_error_24_l1 to IconRole.Inverse)),
        CatalogLayeredIcon("notification_active", listOf(R.drawable.ds_ic_notification_active_24_l0 to IconRole.Primary, R.drawable.ds_ic_notification_active_24_l1 to IconRole.Danger)),
        CatalogLayeredIcon("protection", listOf(R.drawable.ds_ic_protection_24_l0 to IconRole.Primary, R.drawable.ds_ic_protection_24_l1 to IconRole.Inverse)),
        CatalogLayeredIcon("success", listOf(R.drawable.ds_ic_success_24_l0 to IconRole.Primary, R.drawable.ds_ic_success_24_l1 to IconRole.Inverse)),
        CatalogLayeredIcon("target", listOf(R.drawable.ds_ic_target_24_l0 to IconRole.Primary, R.drawable.ds_ic_target_24_l1 to IconRole.Inverse, R.drawable.ds_ic_target_24_l2 to IconRole.Primary)),
        CatalogLayeredIcon("warning", listOf(R.drawable.ds_ic_warning_24_l0 to IconRole.Primary, R.drawable.ds_ic_warning_24_l1 to IconRole.Inverse)),
    )

    val illustrations = listOf(
        "medium_stopper" to R.drawable.ds_ill_medium_stopper,
        "medium_wifi_problem" to R.drawable.ds_ill_medium_wifi_problem,
        "medium_system_problem" to R.drawable.ds_ill_medium_system_problem,
        "medium_rejected" to R.drawable.ds_ill_medium_rejected,
        "medium_otp" to R.drawable.ds_ill_medium_otp,
        "medium_in_process" to R.drawable.ds_ill_medium_in_process,
        "large_failed_get_data" to R.drawable.ds_ill_large_failed_get_data,
        "large_success" to R.drawable.ds_ill_large_success,
        "medium_email_notif" to R.drawable.ds_ill_medium_email_notif,
        "large_email_notif" to R.drawable.ds_ill_large_email_notif,
        "file_document" to R.drawable.ds_ill_file_document,
        "decorative_check" to R.drawable.ds_ill_decorative_check,
        "medium_permission_location" to R.drawable.ds_ill_medium_permission_location,
        "medium_permission_camera" to R.drawable.ds_ill_medium_permission_camera,
        "medium_permission_device_data" to R.drawable.ds_ill_medium_permission_device_data,
        "medium_empty_search" to R.drawable.ds_ill_medium_empty_search,
    )
}
