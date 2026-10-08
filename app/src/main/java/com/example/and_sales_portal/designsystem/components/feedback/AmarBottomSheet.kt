package com.example.and_sales_portal.designsystem.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonGroup
import com.example.and_sales_portal.designsystem.components.inputs.AmarSelectionIcon
import com.example.and_sales_portal.designsystem.components.inputs.AmarSelectionSize
import com.example.and_sales_portal.designsystem.components.inputs.AmarSelectionType
import com.example.and_sales_portal.designsystem.theme.AmarBottomSheetTokens
import com.example.and_sales_portal.designsystem.theme.AmarSelectionTokens
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.designsystem.theme.amarShadow
import kotlinx.coroutines.launch

/** Figma variant property `Type` of `Bottom Sheet`. */
enum class AmarBottomSheetType { Default, Compact }

/**
 * Bottom Sheet surface: the Figma component itself, without the modal behaviour (see [AmarBottomSheet]).
 * Use it directly for previews, screenshot tests or a sheet that is always on screen.
 *
 * Figma: [Bottom Sheet 792:1025](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=792-1025)
 * (4 variants: Default, Default + Illustration, Compact, Compact + Scroll; only `Inverse=False` exists).
 *
 * - Default: centred heading; the close button floats at the top right; optional [illustration] on top.
 * - Compact: left-aligned heading with the close button next to the title.
 * - Compact + [scroll]: the title row and the buttons stay fixed, description and slot scroll.
 * The sheet is at most `Bottom Sheet/Height/Max` (600) tall, plus the navigation bar inset.
 *
 * @param type Figma `Type`.
 * @param title Figma `Show Title` + `Title` (`Heading/MD`).
 * @param description Figma `Show Description` + `Description` (`Body/LG`, `Content/Tertiary`).
 * @param illustration Figma `Show Illustration` + the `Image` slot of [AmarSheetIllustration]; Type=Default only.
 * @param illustrationFullscreen Figma `Fullscreen` of the nested `Element / Sheet / Illustration`.
 * @param showHandle Figma `Show Handle`.
 * @param onClose Figma `Show Close`: the close button is shown when set.
 * @param scroll Figma `Scroll`; Type=Compact only.
 * @param primaryLabel Figma `Show Button`: `Elements / Button Group` (Stack) shown when set.
 * @param secondLabel Second (Secondary) button of the group.
 * @param thirdLabel Third (Secondary) button of the group.
 * @param windowInsets Bottom inset added below the content (the navigation bar by default).
 * @param slot Figma `Show Slot` + `Slot` (Spacing/Gap/MD between children), e.g. [AmarSheetOption] rows.
 */
@Composable
fun AmarBottomSheetContent(
    modifier: Modifier = Modifier,
    type: AmarBottomSheetType = AmarBottomSheetType.Default,
    title: String? = null,
    description: String? = null,
    illustration: (@Composable () -> Unit)? = null,
    illustrationFullscreen: Boolean = false,
    showHandle: Boolean = true,
    onClose: (() -> Unit)? = null,
    scroll: Boolean = false,
    primaryLabel: String? = null,
    onPrimaryClick: () -> Unit = {},
    secondLabel: String? = null,
    onSecondClick: () -> Unit = {},
    thirdLabel: String? = null,
    onThirdClick: () -> Unit = {},
    windowInsets: WindowInsets = WindowInsets.navigationBars,
    slot: (@Composable ColumnScope.() -> Unit)? = null,
) {
    require(illustration == null || type == AmarBottomSheetType.Default) { "Bottom Sheet: Show Illustration exists only for Type=Default" }
    require(!scroll || type == AmarBottomSheetType.Compact) { "Bottom Sheet: Scroll exists only for Type=Compact" }
    val shape = RoundedCornerShape(topStart = AmarBottomSheetTokens.Radius, topEnd = AmarBottomSheetTokens.Radius)
    val buttons: (@Composable () -> Unit)? = primaryLabel?.let {
        {
            AmarButtonGroup(
                primaryLabel = it,
                onPrimaryClick = onPrimaryClick,
                secondLabel = secondLabel,
                onSecondClick = onSecondClick,
                thirdLabel = thirdLabel,
                onThirdClick = onThirdClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    Box(
        modifier
            .fillMaxWidth()
            .amarShadow(AmarTheme.elevation.modal, shape)
            .background(AmarTheme.colors.surface.base, shape)
            .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Bottom)),
    ) {
        Box(Modifier.heightIn(max = AmarBottomSheetTokens.MaxHeight)) {
            when {
                type == AmarBottomSheetType.Default -> DefaultLayout(
                    title, description, illustration, illustrationFullscreen, buttons, slot,
                )
                scroll -> CompactScrollLayout(title, description, onClose, buttons, slot)
                else -> CompactLayout(title, description, onClose, buttons, slot)
            }
            if (showHandle) SheetHandle()
            if (type == AmarBottomSheetType.Default && onClose != null) {
                SheetCloseButton(
                    onClose,
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = AmarBottomSheetTokens.CloseInset, end = AmarBottomSheetTokens.CloseInset),
                )
            }
        }
    }
}

/**
 * Bottom Sheet shown modally (Material3 `ModalBottomSheet` with the [AmarBottomSheetContent] surface).
 *
 * Figma: [Bottom Sheet 792:1025](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=792-1025).
 * Material's own handle, shape, colour and elevation are switched off; the scrim is the Figma `Overlay` colour.
 * The sheet opens fully expanded; its state stays internal so callers need no experimental Material API.
 * Dragging down always dismisses it, also without handle (Material3 1.3 cannot turn sheet gestures off).
 *
 * @param onDismissRequest Called after the sheet is dismissed (scrim tap, back, drag, or the close button).
 * @param scrim `Overlay` state behind the sheet: `Modal` in the form screens, `Default` in the Login screens.
 * @param showClose Figma `Show Close`; the close button hides the sheet, then calls [onDismissRequest].
 * Other parameters: see [AmarBottomSheetContent].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmarBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    scrim: AmarOverlayState = AmarOverlayState.Modal,
    type: AmarBottomSheetType = AmarBottomSheetType.Default,
    title: String? = null,
    description: String? = null,
    illustration: (@Composable () -> Unit)? = null,
    illustrationFullscreen: Boolean = false,
    showHandle: Boolean = true,
    showClose: Boolean = true,
    scroll: Boolean = false,
    primaryLabel: String? = null,
    onPrimaryClick: () -> Unit = {},
    secondLabel: String? = null,
    onSecondClick: () -> Unit = {},
    thirdLabel: String? = null,
    onThirdClick: () -> Unit = {},
    slot: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val overlay = AmarTheme.colors.overlay
    val dismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { if (!sheetState.isVisible) onDismissRequest() }
    }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RectangleShape,
        containerColor = Color.Transparent,
        contentColor = AmarTheme.colors.content.primary,
        tonalElevation = AmarSpace.S0,
        scrimColor = if (scrim == AmarOverlayState.Modal) overlay.modal else overlay.default,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0) },
    ) {
        CompositionLocalProvider(LocalAmarBottomSheetDismiss provides dismiss) {
            AmarBottomSheetContent(
                type = type,
                title = title,
                description = description,
                illustration = illustration,
                illustrationFullscreen = illustrationFullscreen,
                showHandle = showHandle,
                onClose = if (showClose) dismiss else null,
                scroll = scroll,
                primaryLabel = primaryLabel,
                onPrimaryClick = onPrimaryClick,
                secondLabel = secondLabel,
                onSecondClick = onSecondClick,
                thirdLabel = thirdLabel,
                onThirdClick = onThirdClick,
                slot = slot,
            )
        }
    }
}

private val LocalAmarBottomSheetDismiss = staticCompositionLocalOf<() -> Unit> { {} }

/**
 * Inside the content of an [AmarBottomSheet]: a function that hides the sheet with its animation,
 * then calls its `onDismissRequest` (e.g. after an [AmarSheetOption] is picked). Outside a sheet it does nothing.
 */
@Composable
fun amarBottomSheetDismiss(): () -> Unit = LocalAmarBottomSheetDismiss.current

@Composable
private fun DefaultLayout(
    title: String?,
    description: String?,
    illustration: (@Composable () -> Unit)?,
    illustrationFullscreen: Boolean,
    buttons: (@Composable () -> Unit)?,
    slot: (@Composable ColumnScope.() -> Unit)?,
) {
    val t = AmarBottomSheetTokens
    Column(Modifier.fillMaxWidth()) {
        illustration?.let { AmarSheetIllustration(fullscreen = illustrationFullscreen, image = it) }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(
                    start = t.PaddingHorizontal,
                    end = t.PaddingHorizontal,
                    top = if (illustration != null) t.PaddingTop else t.PaddingTopHandle,
                    bottom = t.PaddingBottom,
                ),
            verticalArrangement = Arrangement.spacedBy(t.GapContentToAction),
        ) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(t.GapSection)) {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(t.GapHeading)) {
                    if (illustration == null) Spacer(Modifier.height(t.DefaultTitleSpacer))
                    title?.let { SheetTitle(it, TextAlign.Center, Modifier.fillMaxWidth()) }
                    description?.let { SheetDescription(it, TextAlign.Center) }
                }
                slot?.let { SheetSlot(Modifier, it) }
            }
            buttons?.invoke()
        }
    }
}

@Composable
private fun CompactLayout(
    title: String?,
    description: String?,
    onClose: (() -> Unit)?,
    buttons: (@Composable () -> Unit)?,
    slot: (@Composable ColumnScope.() -> Unit)?,
) {
    val t = AmarBottomSheetTokens
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = t.PaddingHorizontal, end = t.PaddingHorizontal, top = t.PaddingTopHandle, bottom = t.PaddingBottom),
        verticalArrangement = Arrangement.spacedBy(t.GapContentToAction),
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(t.GapSection)) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(t.GapHeading)) {
                CompactTitleRow(title, onClose)
                description?.let { SheetDescription(it, TextAlign.Start) }
            }
            slot?.let { SheetSlot(Modifier, it) }
        }
        buttons?.invoke()
    }
}

@Composable
private fun CompactScrollLayout(
    title: String?,
    description: String?,
    onClose: (() -> Unit)?,
    buttons: (@Composable () -> Unit)?,
    slot: (@Composable ColumnScope.() -> Unit)?,
) {
    val t = AmarBottomSheetTokens
    Column(Modifier.fillMaxWidth().padding(bottom = t.PaddingBottom)) {
        Box(
            Modifier.padding(
                start = t.PaddingHorizontal,
                end = t.PaddingHorizontal,
                top = t.PaddingTopHandle,
                bottom = t.ScrollHeadingPaddingBottom,
            ),
        ) { CompactTitleRow(title, onClose) }
        Column(
            Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = t.PaddingHorizontal),
            verticalArrangement = Arrangement.spacedBy(t.GapSection),
        ) {
            description?.let { SheetDescription(it, TextAlign.Start) }
            slot?.let { SheetSlot(Modifier.padding(bottom = t.ScrollSlotPaddingBottom), it) }
        }
        buttons?.let {
            Box(Modifier.padding(start = t.PaddingHorizontal, end = t.PaddingHorizontal, top = t.ScrollActionPaddingTop)) { it() }
        }
    }
}

@Composable
private fun CompactTitleRow(title: String?, onClose: (() -> Unit)?) {
    if (title == null && onClose == null) return
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AmarBottomSheetTokens.TitleCloseGap)) {
        if (title != null) SheetTitle(title, TextAlign.Start, Modifier.weight(1f)) else Spacer(Modifier.weight(1f))
        onClose?.let { SheetCloseButton(it) }
    }
}

@Composable
private fun SheetTitle(text: String, align: TextAlign, modifier: Modifier) {
    Text(text, modifier, AmarTheme.colors.content.primary, style = AmarTheme.typography.headingMd, textAlign = align)
}

/** `Component/Bottom Sheet/Description`: `Body/LG`, `Content/Tertiary`. */
@Composable
private fun SheetDescription(text: String, align: TextAlign) {
    Text(
        text,
        Modifier.fillMaxWidth(),
        AmarTheme.colors.content.tertiary,
        style = AmarTheme.typography.bodyLg,
        textAlign = align,
    )
}

@Composable
private fun SheetSlot(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AmarBottomSheetTokens.SlotGap), content = content)
}

/** Handle bar: `Handle Color` (`Border/Color/Default`), 34 × 5, centred near the top. */
@Composable
private fun BoxScope.SheetHandle() {
    Box(
        Modifier
            .align(Alignment.TopCenter)
            .padding(top = AmarBottomSheetTokens.HandleTop)
            .size(AmarBottomSheetTokens.HandleWidth, AmarBottomSheetTokens.HandleHeight)
            .background(AmarTheme.colors.border.default, RoundedCornerShape(AmarBottomSheetTokens.HandleRadius)),
    )
}

/** `close sheet` 28: circle `Background/Subtle` with a cross in `Icon/Secondary`. */
@Composable
private fun SheetCloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val size = AmarBottomSheetTokens.CloseSize
    val label = stringResource(R.string.ds_alert_dismiss)
    Box(
        modifier
            .size(size)
            .clip(AmarTheme.shapes.full)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
    ) {
        Icon(painterResource(R.drawable.ds_ic_close_sheet_28_l0), label, Modifier.size(size), tint = AmarTheme.colors.background.subtle)
        Icon(painterResource(R.drawable.ds_ic_close_sheet_28_l1), null, Modifier.size(size), tint = AmarTheme.colors.icon.secondary)
    }
}

/**
 * Element / Sheet / Illustration: image area at the top of a Type=Default sheet.
 *
 * Figma: [Element / Sheet / Illustration 804:1339](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=804-1339)
 * (Fullscreen = 2 variants).
 *
 * @param fullscreen Figma `Fullscreen`: the image spans the sheet width without padding; otherwise it is centred
 * with padding 24 top, 16 sides and 8 bottom.
 * @param image Figma `Image` slot (an illustration, e.g. `ds_ill_medium_*` or `ds_ill_large_*`).
 */
@Composable
fun AmarSheetIllustration(
    modifier: Modifier = Modifier,
    fullscreen: Boolean = false,
    image: @Composable () -> Unit,
) {
    val t = AmarBottomSheetTokens
    Column(
        modifier
            .fillMaxWidth()
            .then(
                if (fullscreen) {
                    Modifier
                } else {
                    Modifier.padding(
                        start = t.IllustrationPaddingHorizontal,
                        end = t.IllustrationPaddingHorizontal,
                        top = t.IllustrationPaddingTop,
                        bottom = t.IllustrationPaddingBottom,
                    )
                },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { image() }
}

/**
 * Element / Sheet / Option: one row of a selection list in a sheet (label + radio).
 *
 * Figma: [Element / Sheet / Option 1043:1524](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1043-1524)
 * (Selected = 2 variants). The radio is the Medium Selection Control scaled to 28 (design decision K7).
 * The whole row is the touch target (no ripple, as for Button).
 *
 * @param label Figma `Edit Label` (`Body/MD`, `Content/Secondary`).
 * @param selected Figma `Selected`.
 * @param showIndicator Figma `Indicator`: the radio.
 */
@Composable
fun AmarSheetOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showIndicator: Boolean = true,
) {
    val t = AmarBottomSheetTokens
    Row(
        modifier
            .fillMaxWidth()
            .height(t.OptionHeight)
            .selectable(selected = selected, interactionSource = null, indication = null, role = Role.RadioButton, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(t.OptionGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f), AmarTheme.colors.content.secondary, style = AmarTheme.typography.bodyMd)
        if (showIndicator) {
            val radioScale = t.OptionRadioSize / AmarSelectionTokens.Medium.box
            Box(Modifier.size(t.OptionRadioSize), contentAlignment = Alignment.Center) {
                AmarSelectionIcon(
                    checked = selected,
                    type = AmarSelectionType.Radio,
                    size = AmarSelectionSize.Medium,
                    modifier = Modifier.graphicsLayer {
                        scaleX = radioScale
                        scaleY = radioScale
                    },
                )
            }
        }
    }
}

/**
 * Element / Sheet / Text: centred body text inside a sheet slot.
 *
 * Figma: [Element / Sheet / Text 895:735](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=895-735)
 * (`Body/LG`, `Component/Bottom Sheet/Description`).
 */
@Composable
fun AmarSheetText(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier.fillMaxWidth(),
        AmarTheme.colors.content.tertiary,
        style = AmarTheme.typography.bodyLg,
        textAlign = TextAlign.Center,
    )
}

/**
 * Element / Sheet / Selection: a tappable choice row with icon, title, description and chevron
 * (e.g. the upload method sheet: camera or gallery).
 *
 * Figma: [Element / Sheet / Selection 1284:2123](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1284-2123).
 *
 * @param title Figma `Label` (`Heading/SM`, `Content/Primary`).
 * @param description Figma `Description` + `Edit Description` (`Label/MD`, `Content/Tertiary`); `null` hides it.
 * @param icon Figma `Icon` + `Change Icon` (24, `Icon/Primary`); `null` hides it.
 * @param showAction Figma `Action`: chevron right.
 */
@Composable
fun AmarSheetSelection(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: Painter? = painterResource(R.drawable.ds_ic_default_24),
    showAction: Boolean = true,
) {
    val t = AmarBottomSheetTokens
    val c = AmarTheme.colors
    val iconSize = AmarTheme.iconSizes.lg
    Row(
        modifier
            .fillMaxWidth()
            .clickable(interactionSource = null, indication = null, role = Role.Button, onClick = onClick)
            .padding(vertical = t.SelectionPaddingVertical),
        horizontalArrangement = Arrangement.spacedBy(t.SelectionGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(t.SelectionHeadingGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let { Icon(it, null, Modifier.size(iconSize), tint = c.icon.primary) }
            Column(verticalArrangement = Arrangement.spacedBy(t.SelectionTextGap)) {
                Text(title, color = c.content.primary, style = AmarTheme.typography.headingSm)
                description?.let { Text(it, color = c.content.tertiary, style = AmarTheme.typography.labelMd) }
            }
        }
        if (showAction) {
            Icon(painterResource(R.drawable.ds_ic_chevron_right_24), null, Modifier.size(iconSize), tint = c.icon.primary)
        }
    }
}

private const val SampleTitle = "Bottom Sheet Title"
private const val SampleDescription =
    "Provide a concise message that helps users understand the purpose of this dialog and the action they can take."

/** Placeholder for the empty 80 `Image` slot, as in Figma. */
@Composable
private fun SampleImage() {
    Box(Modifier.size(AmarSpace.S80))
}

/** The 4 Figma variants with the Figma defaults (title, description, handle, close, one button). */
@Preview(name = "Bottom Sheet", widthDp = 360)
@Composable
private fun AmarBottomSheetPreview() {
    AmarTheme {
        Column(
            Modifier.background(AmarTheme.colors.background.canvas),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S24),
        ) {
            AmarBottomSheetContent(
                title = SampleTitle, description = SampleDescription, onClose = {}, primaryLabel = "Button",
                windowInsets = WindowInsets(0),
            )
            AmarBottomSheetContent(
                title = SampleTitle, description = SampleDescription, onClose = {}, primaryLabel = "Button",
                illustration = { SampleImage() }, windowInsets = WindowInsets(0),
            )
            AmarBottomSheetContent(
                type = AmarBottomSheetType.Compact, title = SampleTitle, description = SampleDescription,
                onClose = {}, primaryLabel = "Button", windowInsets = WindowInsets(0),
            )
            AmarBottomSheetContent(
                type = AmarBottomSheetType.Compact, scroll = true, title = SampleTitle, description = SampleDescription,
                onClose = {}, primaryLabel = "Button", windowInsets = WindowInsets(0),
            )
        }
    }
}

/** Sub-elements: Illustration (both), Option (both), Text, Selection. */
@Preview(name = "Sheet elements", widthDp = 360)
@Composable
private fun AmarSheetElementsPreview() {
    AmarTheme {
        Column(
            Modifier
                .background(AmarTheme.colors.background.base)
                .padding(vertical = AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S16),
        ) {
            AmarSheetIllustration { Image80() }
            AmarSheetIllustration(fullscreen = true) {
                Box(Modifier.fillMaxWidth().height(AmarSpace.S80).background(AmarTheme.colors.background.subtle))
            }
            Column(Modifier.padding(horizontal = AmarSpace.S16)) {
                AmarSheetOption("Option Label", selected = false, onClick = {})
                AmarSheetOption("Option Label", selected = true, onClick = {})
                AmarSheetText(SampleDescription)
                AmarSheetSelection("Title", onClick = {}, description = "Description")
            }
        }
    }
}

@Composable
private fun Image80() {
    Box(Modifier.width(AmarSpace.S80).height(AmarSpace.S80).background(AmarTheme.colors.background.subtle))
}
