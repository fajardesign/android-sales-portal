package com.example.and_sales_portal.designsystem.catalog

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.brand.AmarLogo
import com.example.and_sales_portal.designsystem.components.brand.AmarLogoType
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonGroup
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonGroupLayout
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.feedback.AmarAlert
import com.example.and_sales_portal.designsystem.components.feedback.AmarAlertStyle
import com.example.and_sales_portal.designsystem.components.feedback.AmarAlertType
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadge
import com.example.and_sales_portal.designsystem.components.feedback.AmarBottomSheet
import com.example.and_sales_portal.designsystem.components.feedback.AmarBottomSheetContent
import com.example.and_sales_portal.designsystem.components.feedback.AmarBottomSheetType
import com.example.and_sales_portal.designsystem.components.feedback.AmarSheetOption
import com.example.and_sales_portal.designsystem.components.feedback.AmarSheetSelection
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeSize
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeType
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBox
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBoxType
import com.example.and_sales_portal.designsystem.components.feedback.AmarModal
import com.example.and_sales_portal.designsystem.components.feedback.AmarModalIllustration
import com.example.and_sales_portal.designsystem.components.feedback.AmarOverlay
import com.example.and_sales_portal.designsystem.components.feedback.AmarOverlayState
import com.example.and_sales_portal.designsystem.components.feedback.AmarSpinner
import com.example.and_sales_portal.designsystem.components.inputs.AmarCodeType
import com.example.and_sales_portal.designsystem.components.inputs.AmarCodeVerification
import com.example.and_sales_portal.designsystem.components.inputs.AmarDigit
import com.example.and_sales_portal.designsystem.components.inputs.AmarDigitState
import com.example.and_sales_portal.designsystem.components.inputs.AmarDropdown
import com.example.and_sales_portal.designsystem.components.inputs.AmarDropdownPicker
import com.example.and_sales_portal.designsystem.components.inputs.AmarInputHint
import com.example.and_sales_portal.designsystem.components.inputs.AmarInputLabel
import com.example.and_sales_portal.designsystem.components.inputs.AmarSelectionControl
import com.example.and_sales_portal.designsystem.components.inputs.AmarProgressPhotos
import com.example.and_sales_portal.designsystem.components.inputs.AmarProgressPhotosStatus
import com.example.and_sales_portal.designsystem.components.inputs.AmarUpload
import com.example.and_sales_portal.designsystem.components.inputs.AmarUploadFiles
import com.example.and_sales_portal.designsystem.components.inputs.formatFileSize
import com.example.and_sales_portal.designsystem.components.inputs.AmarSelectionIcon
import com.example.and_sales_portal.designsystem.components.inputs.AmarSelectionSize
import com.example.and_sales_portal.designsystem.components.inputs.AmarSelectionType
import com.example.and_sales_portal.designsystem.components.inputs.AmarTextArea
import com.example.and_sales_portal.designsystem.components.inputs.AmarTextField
import com.example.and_sales_portal.designsystem.components.inputs.AmarTextFieldType
import com.example.and_sales_portal.designsystem.components.inputs.AmarToggle
import com.example.and_sales_portal.designsystem.components.inputs.AmarToggleSize
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.components.layout.AmarDivider
import com.example.and_sales_portal.designsystem.components.layout.AmarDividerOrientation
import com.example.and_sales_portal.designsystem.components.layout.AmarDividerWeight
import com.example.and_sales_portal.designsystem.components.navigation.AmarAppBar
import com.example.and_sales_portal.designsystem.components.navigation.AmarAppBarState
import com.example.and_sales_portal.designsystem.components.navigation.AmarAppBarType
import com.example.and_sales_portal.designsystem.components.navigation.AmarPartnerDestination
import com.example.and_sales_portal.designsystem.components.navigation.AmarPartnerNavigationBar
import com.example.and_sales_portal.designsystem.components.navigation.AmarTab
import com.example.and_sales_portal.designsystem.components.navigation.AmarTabItem
import com.example.and_sales_portal.designsystem.theme.AmarShadow
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.designsystem.theme.amarShadow

/** Catalog sections, in display order. */
internal enum class CatalogSection(val title: String) {
    Foundation("Foundation"),
    Actions("Actions"),
    Inputs("Inputs"),
    Feedback("Feedback"),
    Navigation("Navigation"),
    Layout("Layout"),
    Assets("Ikon & Ilustrasi"),
}

/**
 * Design system catalog: every implemented component with its variants, interactive where it makes sense.
 * Opened from the debug-only launcher "DS Catalog" (`DesignSystemCatalogActivity`).
 * Mapping to Figma: docs/design-system/components.md and assets.md.
 */
@Composable
fun DesignSystemCatalogScreen(modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .background(AmarTheme.colors.background.canvas),
    ) {
        AmarAppBar(title = "Design System", type = AmarAppBarType.Primary)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .windowInsetsPadding(WindowInsets.navigationBars),
            contentPadding = PaddingValues(vertical = AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S24),
        ) {
            CatalogSection.entries.forEach { section ->
                item(key = section.name) { CatalogSectionContent(section) }
            }
        }
    }
}

@Composable
internal fun CatalogSectionContent(section: CatalogSection) {
    SectionCard(section.title) {
        when (section) {
            CatalogSection.Foundation -> FoundationSection()
            CatalogSection.Actions -> ActionsSection()
            CatalogSection.Inputs -> InputsSection()
            CatalogSection.Feedback -> FeedbackSection()
            CatalogSection.Navigation -> NavigationSection()
            CatalogSection.Layout -> LayoutSection()
            CatalogSection.Assets -> AssetsSection()
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(AmarTheme.colors.background.base)
            .padding(vertical = AmarSpace.S16),
        verticalArrangement = Arrangement.spacedBy(AmarSpace.S16),
    ) {
        Text(
            title,
            Modifier.padding(horizontal = AmarSpace.S16),
            AmarTheme.colors.content.primary,
            style = AmarTheme.typography.headingLg,
        )
        content()
    }
}

@Composable
private fun Group(title: String, padded: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AmarSpace.S8)) {
        Text(
            title,
            Modifier.padding(horizontal = AmarSpace.S16),
            AmarTheme.colors.content.tertiary,
            style = AmarTheme.typography.labelMdSemibold,
        )
        Column(
            if (padded) Modifier.padding(horizontal = AmarSpace.S16) else Modifier,
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S12),
            content = content,
        )
    }
}

/** Horizontally scrolling row for wide variant sets. */
@Composable
private fun ScrollRow(content: @Composable () -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AmarSpace.S12),
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}

// region Foundation

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FoundationSection() {
    val c = AmarTheme.colors
    Group("Warna semantik") {
        val swatches = listOf(
            "action.primary" to c.action.primary.background, "content.link" to c.content.link,
            "content.primary" to c.content.primary, "content.secondary" to c.content.secondary,
            "content.tertiary" to c.content.tertiary, "content.disabled" to c.content.disabled,
            "background.canvas" to c.background.canvas, "background.subtle" to c.background.subtle,
            "background.inverse" to c.background.inverse, "border.default" to c.border.default,
            "border.strong" to c.border.strong, "info" to c.feedback.info.icon,
            "success" to c.feedback.success.icon, "warning" to c.feedback.warning.icon, "error" to c.feedback.error.icon,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AmarSpace.S8), verticalArrangement = Arrangement.spacedBy(AmarSpace.S8)) {
            swatches.forEach { (name, color) -> Swatch(name, color) }
        }
    }
    Group("Tipografi (Inter)") {
        val t = AmarTheme.typography
        listOf(
            "Display" to t.display, "Heading/2XL" to t.heading2xl, "Heading/XL" to t.headingXl, "Heading/LG" to t.headingLg,
            "Heading/MD" to t.headingMd, "Heading/SM" to t.headingSm, "Heading/2SM" to t.heading2sm,
            "Body/LG" to t.bodyLg, "Body/MD" to t.bodyMd, "Body/MD Medium" to t.bodyMdMedium, "Body/SM" to t.bodySm,
            "Label/LG" to t.labelLg, "Label/LG Medium" to t.labelLgMedium, "Label/Button/LG" to t.labelButtonLg,
            "Label/Button/MD" to t.labelButtonMd, "Label/Button/SM" to t.labelButtonSm, "Label/MD" to t.labelMd,
            "Label/MD Medium" to t.labelMdMedium, "Label/MD Semibold" to t.labelMdSemibold, "Label/SM" to t.labelSm,
            "Caption/MD" to t.captionMd, "Caption/SM" to t.captionSm,
        ).forEach { (name, style) -> TypeSample(name, style) }
    }
    Group("Spacing") {
        listOf(AmarSpace.S4, AmarSpace.S8, AmarSpace.S12, AmarSpace.S16, AmarSpace.S24, AmarSpace.S32, AmarSpace.S48, AmarSpace.S64)
            .forEach { SpaceBar(it) }
    }
    Group("Radius") {
        val s = AmarTheme.shapes
        ScrollRow {
            listOf("field 10" to s.field, "card 12" to s.card, "container 16" to s.container, "full" to s.full)
                .forEach { (name, shape) -> ShapeSample(name, shape) }
        }
    }
    Group("Elevation") {
        val e = AmarTheme.elevation
        ScrollRow {
            listOf("subtle" to e.subtle, "surface" to e.surface, "floating" to e.floating, "modal" to e.modal, "overlay" to e.overlay)
                .forEach { (name, shadow) -> ShadowSample(name, shadow) }
        }
    }
}

@Composable
private fun Swatch(name: String, color: Color) {
    Column(Modifier.width(AmarSpace.S96), verticalArrangement = Arrangement.spacedBy(AmarSpace.S4)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(AmarSpace.S40)
                .background(color, AmarTheme.shapes.field)
                .border(AmarTheme.borderWidths.subtle, AmarTheme.colors.border.default, AmarTheme.shapes.field),
        )
        Text(name, style = AmarTheme.typography.captionSm, color = AmarTheme.colors.content.tertiary, maxLines = 1)
    }
}

@Composable
private fun TypeSample(name: String, style: TextStyle) {
    Column {
        Text(name, style = style, color = AmarTheme.colors.content.primary, maxLines = 1)
        Text(
            "${style.fontSize.value.toInt()}/${style.lineHeight.value.toInt()} · ${style.fontWeight?.weight}",
            style = AmarTheme.typography.captionSm,
            color = AmarTheme.colors.content.tertiary,
        )
    }
}

@Composable
private fun SpaceBar(space: Dp) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AmarSpace.S8)) {
        Text("${space.value.toInt()}", Modifier.width(AmarSpace.S32), AmarTheme.colors.content.tertiary, style = AmarTheme.typography.captionSm)
        Box(Modifier.size(space, AmarSpace.S12).background(AmarTheme.colors.action.primary.background))
    }
}

@Composable
private fun ShapeSample(name: String, shape: Shape) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(AmarSpace.S4)) {
        Box(Modifier.size(AmarSpace.S64).background(AmarTheme.colors.background.brandSubtle, shape).border(AmarTheme.borderWidths.subtle, AmarTheme.colors.border.focus, shape))
        Text(name, style = AmarTheme.typography.captionSm, color = AmarTheme.colors.content.tertiary)
    }
}

@Composable
private fun ShadowSample(name: String, shadow: AmarShadow) {
    Column(
        Modifier.padding(AmarSpace.S16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AmarSpace.S8),
    ) {
        Box(Modifier.size(AmarSpace.S56).amarShadow(shadow, AmarTheme.shapes.card).background(AmarTheme.colors.surface.base, AmarTheme.shapes.card))
        Text(name, style = AmarTheme.typography.captionSm, color = AmarTheme.colors.content.tertiary)
    }
}

// endregion

// region Actions

@Composable
private fun ActionsSection() {
    val icon = painterResource(R.drawable.ds_ic_default_24)
    AmarButtonType.entries.forEach { type ->
        listOf(false, true).forEach { inverse ->
            Group("Button · $type${if (inverse) " · Inverse" else ""}", padded = false) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(if (inverse) AmarTheme.colors.background.inverse else AmarTheme.colors.background.base)
                        .padding(AmarSpace.S16),
                    verticalArrangement = Arrangement.spacedBy(AmarSpace.S12),
                ) {
                    AmarButtonSize.entries.forEach { size ->
                        ScrollRow {
                            AmarButton("Button", {}, type = type, size = size, inverse = inverse, leftIcon = icon, rightIcon = icon)
                            AmarButton("Button", {}, type = type, size = size, inverse = inverse, enabled = false)
                            AmarButton("Button", {}, type = type, size = size, inverse = inverse, loading = true)
                            AmarButton(null, {}, type = type, size = size, inverse = inverse)
                            AmarButton(null, {}, type = type, size = size, inverse = inverse, enabled = false)
                        }
                    }
                }
            }
        }
    }
    Group("Button Group") {
        AmarButtonGroup("Simpan", {}, secondLabel = "Batal", thirdLabel = "Lainnya")
        AmarButtonGroup("Simpan", {}, layout = AmarButtonGroupLayout.Side, secondLabel = "Batal")
    }
}

// endregion

// region Inputs

@Composable
private fun InputsSection() {
    Group("Input (coba ketik)") {
        AmarTextFieldType.entries.forEach { type ->
            var value by remember { mutableStateOf("") }
            AmarTextField(
                value, { value = it }, Modifier.fillMaxWidth(), type = type,
                label = "Input $type", required = type == AmarTextFieldType.Text, placeholder = "Placeholder text",
            )
        }
        AmarTextField("", {}, Modifier.fillMaxWidth(), label = "Error", placeholder = "Placeholder text", error = true, hint = "Wajib isi")
        AmarTextField("Nilai terisi", {}, Modifier.fillMaxWidth(), label = "Disabled - Filled", enabled = false)
        AmarTextField("", {}, Modifier.fillMaxWidth(), label = "Disabled", placeholder = "Placeholder text", enabled = false)
    }
    Group("Label & Hint") {
        AmarInputLabel("Nama Partner", required = true, subLabel = "(Jika ada)", showIcon = true)
        AmarInputHint("Pastikan nama sesuai KTP.")
        AmarInputHint("Wajib isi", error = true)
    }
    Group("Dropdown") {
        var expanded by remember { mutableStateOf(false) }
        AmarDropdown(null, { expanded = !expanded }, Modifier.fillMaxWidth(), label = "Provinsi", placeholder = "Pilih provinsi", expanded = expanded)
        AmarDropdown("DKI Jakarta", {}, Modifier.fillMaxWidth(), label = "Error", error = true, hint = "Wajib isi")
        AmarDropdown("DKI Jakarta", {}, Modifier.fillMaxWidth(), label = "Disabled", enabled = false)
    }
    Group("Area") {
        var value by remember { mutableStateOf("") }
        AmarTextArea(value, { value = it }, Modifier.fillMaxWidth(), label = "Catatan", placeholder = "Tulis catatan", maxLength = 200)
        AmarTextArea("Isi catatan", {}, Modifier.fillMaxWidth(), label = "Error", error = true, hint = "Wajib isi")
    }
    Group("Code Verification (coba ketik)") {
        var otp by remember { mutableStateOf("") }
        var pin by remember { mutableStateOf("") }
        AmarCodeVerification(otp, { otp = it }, hint = "Masukkan kode OTP")
        AmarCodeVerification(pin, { pin = it }, type = AmarCodeType.PIN)
        AmarCodeVerification("12", {}, error = true, hint = "Kode salah")
        AmarCodeVerification("123456", {}, enabled = false)
    }
    Group("Digit") {
        listOf(false, true).forEach { masking ->
            ScrollRow { AmarDigitState.entries.forEach { AmarDigit(it, digit = '1', masking = masking) } }
        }
    }
    Group("Toggle") {
        AmarToggleSize.entries.forEach { size ->
            var on by remember { mutableStateOf(true) }
            ScrollRow {
                AmarToggle(on, { on = it }, size = size)
                AmarToggle(true, null, size = size, enabled = false)
                AmarToggle(false, null, size = size, enabled = false)
            }
        }
    }
    Group("Selection Control") {
        AmarSelectionSize.entries.forEach { size ->
            var checked by remember { mutableStateOf(true) }
            var radio by remember { mutableIntStateOf(0) }
            AmarSelectionControl(checked, { checked = it }, size = size, label = "Checkbox $size", description = "Deskripsi opsional.")
            listOf("Radio A", "Radio B").forEachIndexed { i, label ->
                AmarSelectionControl(radio == i, { radio = i }, type = AmarSelectionType.Radio, size = size, label = label)
            }
            AmarSelectionControl(true, null, size = size, enabled = false, label = "Disabled")
        }
        ScrollRow {
            AmarSelectionSize.entries.forEach { size ->
                AmarSelectionType.entries.forEach { type ->
                    AmarSelectionIcon(false, type = type, size = size)
                    AmarSelectionIcon(true, type = type, size = size)
                    AmarSelectionIcon(true, type = type, size = size, indeterminate = true)
                    AmarSelectionIcon(true, type = type, size = size, enabled = false)
                }
            }
        }
    }
    Group("Dropdown picker (ketuk untuk membuka sheet)") {
        val provinces = listOf(
            "Aceh", "Sumatera Utara", "Sumatera Barat", "Riau", "Jambi", "Sumatera Selatan", "Bengkulu", "Lampung",
            "Kepulauan Bangka Belitung", "Kepulauan Riau", "DKI Jakarta", "Jawa Barat", "Jawa Tengah", "DI Yogyakarta",
        )
        val regencies = listOf("Kabupaten Deli Serdang", "Kota Medan", "Kota Binjai")
        var province by remember { mutableStateOf<String?>(null) }
        var regency by remember { mutableStateOf<String?>(null) }
        AmarDropdownPicker(
            province, provinces, { province = it; regency = null }, "Pilih Provinsi",
            label = "Provinsi", required = true, placeholder = "Pilih provinsi",
        )
        AmarDropdownPicker(
            regency, regencies, { regency = it }, "Pilih Kabupaten/Kota",
            label = "Kabupaten/Kota", required = true, placeholder = "Pilih kabupaten/kota", enabled = province != null,
        )
        var bank by remember { mutableStateOf<String?>(null) }
        AmarDropdownPicker(
            bank,
            listOf(
                "Bank Central Asia (BCA)", "Bank Rakyat Indonesia (BRI)", "Bank Mandiri", "Bank Negara Indonesia (BNI)",
                "Bank Tabungan Negara (BTN)", "Bank CIMB Niaga", "Bank Danamon", "Bank Permata",
            ),
            { bank = it }, "Pilih Bank",
            label = "Bank", required = true, placeholder = "Pilih bank", searchPlaceholder = "Cari bank",
        )
    }
    Group("Element / Upload (ketuk untuk menambah file)") {
        var single by remember { mutableStateOf(false) }
        AmarUpload(
            label = "KTP / Paspor",
            buttonLabel = "Ambil Foto",
            onUploadClick = { single = true },
            buttonIcon = painterResource(R.drawable.ds_ic_upload_document_24),
            files = if (single) {
                { AmarProgressPhotos("ktp.jpg", AmarProgressPhotosStatus.UploadedDefault, onDelete = { single = false }) }
            } else {
                null
            },
        )
        var count by remember { mutableIntStateOf(1) }
        AmarUpload(
            label = "Foto toko",
            buttonLabel = "Ambil Foto",
            onUploadClick = { if (count < 3) count++ },
            multipleFiles = true,
            helperText = "Kamu bisa upload foto sebanyak 3 kali",
            supportingLabel = "Supporting Label",
        ) {
            repeat(count) { i ->
                AmarProgressPhotos(
                    "foto_toko_${i + 1}.jpg",
                    AmarProgressPhotosStatus.Uploaded,
                    thumbnail = painterResource(R.drawable.ds_ill_medium_in_process),
                    onDelete = { count-- },
                )
            }
        }
    }
    Group("Upload Files (seperti layar Document Upload)") {
        var uploaded by remember { mutableStateOf(false) }
        AmarUploadFiles(
            label = "Anggaran Dasar Perusahaan",
            buttonLabel = "Ambil Dokumen",
            onUploadClick = { uploaded = true },
            buttonIcon = painterResource(R.drawable.ds_ic_upload_document_24),
            modifier = Modifier.fillMaxWidth(),
            files = if (uploaded) {
                {
                    AmarProgressPhotos(
                        "Anggaran_Dasar_PT_Nusantara.pdf",
                        AmarProgressPhotosStatus.UploadedDefault,
                        onDelete = { uploaded = false },
                    )
                }
            } else {
                null
            },
        )
    }
    Group("Element / Progress Photos") {
        AmarProgressPhotosStatus.entries.forEach { status ->
            AmarProgressPhotos(
                fileName = if (status == AmarProgressPhotosStatus.UploadedDefault) "Anggaran_Dasar_PT_Nusantara.pdf" else "foto_toko.jpg",
                status = status,
                thumbnail = painterResource(R.drawable.ds_ill_medium_in_process),
                fileSize = formatFileSize(2_516_582),
                progress = 0.4f,
                onRetry = {},
                onDelete = {},
            )
        }
    }
}

// endregion

// region Feedback

@Composable
private fun FeedbackSection() {
    Group("Alert") {
        AmarAlertStyle.entries.forEach { style ->
            AmarAlertType.entries.forEach { type ->
                AmarAlert(
                    "Alert $style · $type", Modifier.fillMaxWidth(), style = style, type = type,
                    actionLabel = "Action", onAction = {}, onDismiss = {},
                )
            }
        }
    }
    Group("Info Box") {
        AmarInfoBoxType.entries.forEach { type ->
            AmarInfoBox(
                "Insert the description here.", Modifier.fillMaxWidth(), type = type, label = "Info Box $type",
                buttonLabel = "Button", onButtonClick = {}, onClose = {},
            )
        }
    }
    Group("Badge") {
        val icon = painterResource(R.drawable.ds_ic_default_16)
        AmarBadgeType.entries.forEach { type ->
            listOf(false, true).forEach { solid ->
                AmarBadgeSize.entries.forEach { size ->
                    ScrollRow {
                        AmarBadge("Label", type = type, size = size, solid = solid)
                        AmarBadge("Label", type = type, size = size, solid = solid, onClose = {})
                        AmarBadge("Label", type = type, size = size, solid = solid, count = 3)
                        AmarBadge("Label", type = type, size = size, solid = solid, leftIcon = icon)
                    }
                }
            }
        }
    }
    Group("Spinner & Overlay") {
        ScrollRow {
            AmarSpinner(AmarTheme.colors.action.primary.background)
            Box(Modifier.size(AmarSpace.S96)) { AmarOverlay(spinner = true) }
            Box(Modifier.size(AmarSpace.S96)) { AmarOverlay(state = AmarOverlayState.Modal) }
        }
    }
    Group("Modal") {
        AmarModal(
            "Simpan", {}, title = "Modal Title", description = "Description text goes here.", secondLabel = "Batal",
            illustration = { AmarModalIllustration { Image(painterResource(R.drawable.ds_ill_medium_in_process), null) } },
        )
        AmarModal(
            "Simpan", {}, Modifier.height(AmarSpace.S128 + AmarSpace.S128 + AmarSpace.S32),
            title = "Modal yang di-scroll", description = "Konten panjang. ".repeat(30), secondLabel = "Batal",
        )
    }
    Group("Bottom Sheet (statis)", padded = false) {
        AmarBottomSheetContent(
            title = "Terjadi Gangguan",
            description = "Server sedang tidak tersedia untuk sementara. Coba lagi beberapa saat lagi.",
            illustration = { Image(painterResource(R.drawable.ds_ill_medium_system_problem), null) },
            showHandle = false,
            onClose = {},
            primaryLabel = "Coba Lagi",
            windowInsets = WindowInsets(0),
        )
        AmarBottomSheetContent(
            type = AmarBottomSheetType.Compact,
            title = "Metode Upload Dokumen",
            showHandle = false,
            onClose = {},
            windowInsets = WindowInsets(0),
        ) {
            AmarSheetSelection("Ambil foto", onClick = {}, icon = painterResource(R.drawable.ds_ic_camera_24))
            AmarSheetSelection("Pilih dari galeri", onClick = {}, icon = painterResource(R.drawable.ds_ic_photo_24))
        }
    }
    Group("Bottom Sheet (ketuk untuk membuka)") {
        var open by remember { mutableStateOf<String?>(null) }
        var province by remember { mutableStateOf("DKI Jakarta") }
        AmarButton("Pilih provinsi: $province", { open = "province" }, type = AmarButtonType.Secondary, size = AmarButtonSize.Medium)
        AmarButton("Sheet error (Default)", { open = "error" }, type = AmarButtonType.Secondary, size = AmarButtonSize.Medium)
        when (open) {
            "province" -> AmarBottomSheet(
                onDismissRequest = { open = null },
                type = AmarBottomSheetType.Compact,
                scroll = true,
                title = "Pilih Provinsi",
                showClose = false,
            ) {
                listOf(
                    "Aceh", "Bali", "Banten", "Bengkulu", "DI Yogyakarta", "DKI Jakarta", "Gorontalo", "Jambi",
                    "Jawa Barat", "Jawa Tengah", "Jawa Timur", "Kalimantan Barat", "Kalimantan Selatan",
                ).forEach { name ->
                    AmarSheetOption(name, selected = name == province, onClick = { province = name; open = null })
                }
            }
            "error" -> AmarBottomSheet(
                onDismissRequest = { open = null },
                scrim = AmarOverlayState.Default,
                title = "Tidak Ada Koneksi Internet",
                description = "Periksa koneksi internet Anda, lalu coba lagi.",
                illustration = { Image(painterResource(R.drawable.ds_ill_medium_wifi_problem), null) },
                showHandle = false,
                primaryLabel = "Coba Lagi",
                onPrimaryClick = { open = null },
            )
        }
    }
}

// endregion

// region Navigation

@Composable
private fun NavigationSection() {
    Group("App Bar", padded = false) {
        AmarAppBarType.entries.forEach { type ->
            AmarAppBarState.entries.forEach { state ->
                Box(if (state == AmarAppBarState.Transparent && type == AmarAppBarType.Secondary) Modifier.background(AmarTheme.colors.background.inverse) else Modifier) {
                    AmarAppBar(
                        title = "$type · $state", type = type, state = state, windowInsets = WindowInsets(0),
                        subtitle = if (state == AmarAppBarState.TransparentInverse) "Subjudul" else null,
                        onNavigationClick = if (type == AmarAppBarType.Secondary) ({}) else null,
                        onActionClick = {},
                    )
                }
            }
        }
    }
    Group("Tab (coba ketuk)", padded = false) {
        var selected by remember { mutableIntStateOf(0) }
        AmarTab {
            listOf("Aktif", "Riwayat").forEachIndexed { i, label ->
                AmarTabItem(label, selected == i, { selected = i })
            }
        }
        AmarTab {
            AmarTabItem("Placeholder", true, {}, scrim = true)
            AmarTabItem("Placeholder", false, {}, scrim = true)
        }
    }
    Group("Navigation Bar - Partner Dashboard (coba ketuk)", padded = false) {
        var selected by remember { mutableStateOf(AmarPartnerDestination.Beranda) }
        AmarPartnerNavigationBar(selected, { selected = it }, windowInsets = WindowInsets(0))
    }
}

// endregion

// region Layout

@Composable
private fun LayoutSection() {
    Group("Divider") {
        AmarDividerWeight.entries.forEach { AmarDivider(weight = it) }
        Row(Modifier.height(AmarSpace.S40), horizontalArrangement = Arrangement.spacedBy(AmarSpace.S16)) {
            AmarDividerWeight.entries.forEach { AmarDivider(orientation = AmarDividerOrientation.Vertical, weight = it) }
        }
    }
    Group("Card / Type=Default") {
        listOf(AmarCardStyle.Default, AmarCardStyle.Border, AmarCardStyle.Shadow).forEach { style ->
            AmarCard(
                label = "Card ${style.name}",
                style = style,
                description = "Insert the description here.",
                icon = {
                    Icon(
                        painterResource(R.drawable.ds_ic_files_24), null,
                        Modifier.size(AmarTheme.iconSizes.xl), tint = AmarTheme.colors.icon.brand,
                    )
                },
                showAction = true,
                buttonLabel = "Button",
                onButtonClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    Group("Card / Type=Custom (ketuk untuk Selected)") {
        var selected by remember { mutableStateOf(AmarCardStyle.Default) }
        listOf(AmarCardStyle.Default, AmarCardStyle.Border, AmarCardStyle.Shadow).forEach { style ->
            AmarCard(
                style = if (selected == style) AmarCardStyle.Selected else style,
                onClick = { selected = style },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Style=${style.name}", style = AmarTheme.typography.labelMd, color = AmarTheme.colors.content.primary)
            }
        }
    }
}

// endregion

// region Assets

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AssetsSection() {
    val c = AmarTheme.colors
    Group("Logo / Amar Bank (Color=True, lalu Color=False di latar gelap)") {
        Row(horizontalArrangement = Arrangement.spacedBy(AmarSpace.S24), verticalAlignment = Alignment.CenterVertically) {
            AmarLogoType.entries.forEach { AmarLogo(type = it) }
        }
        Row(
            Modifier
                .background(c.background.inverse, AmarTheme.shapes.card)
                .padding(AmarSpace.S12),
            horizontalArrangement = Arrangement.spacedBy(AmarSpace.S24),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AmarLogoType.entries.forEach { AmarLogo(type = it, color = false) }
        }
    }
    Group("Ikon (tint Icon/Primary)") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AmarSpace.S8), verticalArrangement = Arrangement.spacedBy(AmarSpace.S8)) {
            CatalogAssets.icons.forEach { icon ->
                AssetTile(icon.name) {
                    // White-only icons (plus, whatsapp_fill) are shown on the inverse background.
                    val inverse = icon.name == "plus" || icon.name == "whatsapp_fill"
                    Box(
                        Modifier.background(if (inverse) c.background.inverse else c.background.base, AmarTheme.shapes.field).padding(AmarSpace.S4),
                    ) {
                        Icon(painterResource(icon.res), null, Modifier.size(AmarTheme.iconSizes.lg), tint = if (inverse) c.icon.inverse else c.icon.primary)
                    }
                }
            }
        }
    }
    Group("Ikon multi-warna (layer)") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AmarSpace.S8), verticalArrangement = Arrangement.spacedBy(AmarSpace.S8)) {
            CatalogAssets.layeredIcons.forEach { icon ->
                AssetTile(icon.name) {
                    Box {
                        icon.layers.forEach { (res, role) ->
                            Icon(painterResource(res), null, Modifier.size(AmarTheme.iconSizes.lg), tint = role.color())
                        }
                    }
                }
            }
        }
    }
    Group("Ilustrasi (warna asli)") {
        CatalogAssets.illustrations.forEach { (name, res) ->
            Column(verticalArrangement = Arrangement.spacedBy(AmarSpace.S4)) {
                Image(painterResource(res), null)
                Text(name, style = AmarTheme.typography.captionSm, color = c.content.tertiary)
            }
        }
    }
}

@Composable
private fun AssetTile(name: String, content: @Composable () -> Unit) {
    Column(Modifier.width(AmarSpace.S80), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(AmarSpace.S4)) {
        content()
        Text(name, style = AmarTheme.typography.captionSm, color = AmarTheme.colors.content.tertiary, maxLines = 1)
    }
}

@Composable
private fun IconRole.color(): Color {
    val c = AmarTheme.colors
    return when (this) {
        IconRole.Primary -> c.icon.primary
        IconRole.Inverse -> c.icon.inverse
        IconRole.Danger -> c.icon.danger
        IconRole.Subtle -> c.background.subtle
        IconRole.Secondary -> c.icon.secondary
        IconRole.Disabled -> c.icon.disabled
        IconRole.Quaternary -> c.content.quaternary
    }
}

// endregion

@Preview(name = "Catalog", widthDp = 360, heightDp = 800)
@Composable
private fun DesignSystemCatalogPreview() {
    AmarTheme { DesignSystemCatalogScreen() }
}
