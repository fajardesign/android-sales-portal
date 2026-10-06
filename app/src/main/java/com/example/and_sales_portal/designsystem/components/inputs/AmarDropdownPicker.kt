package com.example.and_sales_portal.designsystem.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.example.and_sales_portal.designsystem.components.feedback.AmarBottomSheet
import com.example.and_sales_portal.designsystem.components.feedback.AmarBottomSheetType
import com.example.and_sales_portal.designsystem.components.feedback.AmarSheetOption
import com.example.and_sales_portal.designsystem.components.feedback.amarBottomSheetDismiss
import com.example.and_sales_portal.designsystem.theme.AmarBottomSheetTokens
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/**
 * Dropdown picker (pattern, not a separate Figma component): an [AmarDropdown] that opens a Compact
 * [AmarBottomSheet] with one [AmarSheetOption] per option, as in the product screens (Pilih Provinsi, Pilih Bank).
 * Bundle mapping: `[Dropdown]` and `[Dropdown / Search]` (CLAUDE.md §2).
 *
 * Figma: [Dropdown 640:1400](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=640-1400),
 * [Bottom Sheet 792:1025](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=792-1025),
 * [Element / Sheet / Option 1043:1524](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1043-1524).
 *
 * - The field shows `State=Active` while the sheet is open.
 * - The sheet has a handle, no close button and no buttons; picking an option calls [onSelect] and hides the sheet.
 * - `Scroll=True` is used when [searchPlaceholder] is set or when the options do not fit the 600 sheet height
 *   without scrolling (computed from the Bottom Sheet tokens, see [pickerNeedsScroll]), as in the product screens.
 * - [searchPlaceholder] adds an Input `Type=Search` as the first slot row and filters by label, ignoring case.
 *   The design has no empty-result state; with no match the list is empty.
 *
 * @param value selected option; `null` shows [placeholder].
 * @param sheetTitle Sheet title, e.g. "Pilih Provinsi".
 * @param optionLabel Text shown for an option, in the field and in the sheet.
 * Field parameters: see [AmarDropdown].
 */
@Composable
fun <T> AmarDropdownPicker(
    value: T?,
    options: List<T>,
    onSelect: (T) -> Unit,
    sheetTitle: String,
    modifier: Modifier = Modifier,
    optionLabel: (T) -> String = { it.toString() },
    label: String? = null,
    required: Boolean = false,
    placeholder: String = "",
    hint: String? = null,
    error: Boolean = false,
    enabled: Boolean = true,
    leftIcon: Painter? = null,
    searchPlaceholder: String? = null,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    AmarDropdown(
        value = value?.let(optionLabel),
        onClick = { open = true },
        modifier = modifier,
        label = label,
        required = required,
        placeholder = placeholder,
        hint = hint,
        error = error,
        enabled = enabled,
        expanded = open,
        leftIcon = leftIcon,
    )
    if (!open) return
    val titleLineHeight = with(LocalDensity.current) { AmarTheme.typography.headingMd.lineHeight.toDp() }
    var query by remember { mutableStateOf("") }
    AmarBottomSheet(
        onDismissRequest = { open = false },
        type = AmarBottomSheetType.Compact,
        scroll = pickerNeedsScroll(options.size, searchPlaceholder != null, titleLineHeight),
        title = sheetTitle,
        showClose = false,
    ) {
        val dismiss = amarBottomSheetDismiss()
        searchPlaceholder?.let {
            AmarTextField(query, { query = it }, type = AmarTextFieldType.Search, placeholder = it)
        }
        filterPickerOptions(options, query, optionLabel).forEach { option ->
            AmarSheetOption(
                label = optionLabel(option),
                selected = option == value,
                onClick = {
                    onSelect(option)
                    dismiss()
                },
            )
        }
    }
}

/** Options whose label contains [query] (trimmed, ignoring case); all options for a blank query. */
internal fun <T> filterPickerOptions(options: List<T>, query: String, optionLabel: (T) -> String): List<T> {
    val q = query.trim()
    return if (q.isEmpty()) options else options.filter { optionLabel(it).contains(q, ignoreCase = true) }
}

/**
 * Whether the picker sheet uses `Scroll=True`: always with search (as the Pilih Bank screen), otherwise when the
 * non-scrolling Compact layout (padding top + one title line + heading ↔ slot gap + option rows + padding bottom)
 * is taller than `Bottom Sheet/Height/Max`. With the current tokens that is from 9 options.
 */
internal fun pickerNeedsScroll(optionCount: Int, searchable: Boolean, titleLineHeight: Dp): Boolean {
    if (searchable) return true
    val t = AmarBottomSheetTokens
    val rows = t.OptionHeight * optionCount + t.SlotGap * (optionCount - 1).coerceAtLeast(0)
    val height = t.PaddingTopHandle + titleLineHeight + t.GapSection + rows + t.PaddingBottom
    return height > t.MaxHeight
}

/** The field in its states; the sheet itself opens on tap (see the DS catalog). */
@Preview(name = "Dropdown picker", widthDp = 360)
@Composable
private fun AmarDropdownPickerPreview() {
    AmarTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .background(AmarTheme.colors.background.base)
                .padding(AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S16),
        ) {
            val provinces = listOf("Aceh", "Sumatera Utara", "Sumatera Barat")
            AmarDropdownPicker(null, provinces, {}, "Pilih Provinsi", label = "Provinsi", required = true, placeholder = "Pilih provinsi")
            AmarDropdownPicker("Sumatera Utara", provinces, {}, "Pilih Provinsi", label = "Provinsi", required = true)
            AmarDropdownPicker(
                null, provinces, {}, "Pilih Kabupaten/Kota", label = "Kabupaten/Kota", required = true,
                placeholder = "Pilih kabupaten/kota", enabled = false,
            )
        }
    }
}
