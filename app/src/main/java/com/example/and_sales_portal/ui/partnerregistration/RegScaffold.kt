package com.example.and_sales_portal.ui.partnerregistration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarChip
import com.example.and_sales_portal.designsystem.components.inputs.AmarInputHint
import com.example.and_sales_portal.designsystem.components.inputs.AmarInputLabel
import com.example.and_sales_portal.designsystem.components.navigation.AmarAppBar
import com.example.and_sales_portal.designsystem.components.navigation.AmarAppBarType
import com.example.and_sales_portal.designsystem.theme.AmarFormTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.designsystem.theme.amarShadow

/**
 * Frame shared by REG-02 … REG-06 (Figma "Sales - Partner Dashboard - Android"): App Bar Secondary "Pendaftaran Partner"
 * with back, `Form introduction` ("LANGKAH n DARI 5", title, description), the scrollable form and the
 * `Persistent action` button. The step header replaces the Stepper of the spec (decision 2026-10-06: follow Figma).
 *
 * @param cta Bottom button label; `null` hides the bar (REG-05 before the account is verified).
 */
@Composable
fun RegStepScaffold(
    step: RegStep,
    title: String,
    description: String,
    onBack: () -> Unit,
    cta: String?,
    onCta: () -> Unit,
    modifier: Modifier = Modifier,
    ctaEnabled: Boolean = true,
    ctaLoading: Boolean = false,
    windowInsets: WindowInsets = WindowInsets.navigationBars.union(WindowInsets.ime),
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = AmarTheme.colors
    val t = AmarFormTokens
    Column(modifier.fillMaxSize().background(c.background.base)) {
        AmarAppBar(
            title = stringResource(R.string.reg_app_bar_title),
            type = AmarAppBarType.Secondary,
            onNavigationClick = onBack,
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = t.PaddingHorizontal, vertical = t.PaddingVertical),
            verticalArrangement = Arrangement.spacedBy(t.FieldGap),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(t.IntroGap)) {
                Text(stringResource(R.string.reg_step, step.number, RegStep.count), color = c.content.link, style = AmarTheme.typography.labelSm)
                Text(title, color = c.content.primary, style = AmarTheme.typography.headingSm)
                Text(description, color = c.content.secondary, style = AmarTheme.typography.bodySm)
            }
            content()
        }
        if (cta != null) RegPersistentAction(cta, onCta, ctaEnabled, ctaLoading, windowInsets)
    }
}

/** Figma `Persistent action`: full-width Primary Large button on `Background/Base` with `Shadow/Bottom`. */
@Composable
fun RegPersistentAction(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false,
    windowInsets: WindowInsets = WindowInsets.navigationBars.union(WindowInsets.ime),
) {
    Box(
        Modifier
            .fillMaxWidth()
            .amarShadow(AmarTheme.elevation.bottom, RectangleShape)
            .background(AmarTheme.colors.background.base)
            .windowInsetsPadding(windowInsets)
            .padding(AmarFormTokens.ActionPadding),
    ) {
        AmarButton(label, onClick, Modifier.fillMaxWidth(), size = AmarButtonSize.Large, enabled = enabled, loading = loading)
    }
}

/** Figma `Choice field`: required label, single-choice Chips (wrapping, gap 8) and the required hint on error. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegChoiceField(group: RegChoice, selected: String?, onSelect: (String) -> Unit, error: Boolean) {
    val t = AmarFormTokens
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(t.ChoiceFieldGap)) {
        AmarInputLabel(stringResource(group.label), required = true, style = AmarTheme.typography.labelButtonMd)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(t.ChoicesGap), verticalArrangement = Arrangement.spacedBy(t.ChoicesGap)) {
            group.options.forEach { option ->
                AmarChip(stringResource(option.label), selected == option.key, { onSelect(option.key) })
            }
        }
        if (error) AmarInputHint(stringResource(R.string.reg_required), error = true)
    }
}

/** Hint for a required field: "Informasi wajib diisi" when [error], otherwise none. */
@Composable
fun requiredHint(error: Boolean): String? = if (error) stringResource(R.string.reg_required) else null
