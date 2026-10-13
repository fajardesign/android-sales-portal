package com.example.and_sales_portal.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.auth.SalesSession
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadge
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeSize
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.components.navigation.AmarAppBar
import com.example.and_sales_portal.designsystem.components.navigation.AmarAppBarType
import com.example.and_sales_portal.designsystem.theme.AmarFormTokens
import com.example.and_sales_portal.designsystem.theme.AmarSuccessTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/**
 * Spec: AKN-01 Akun (PRD v3 "Login on mobile": header shows name, role and "Keluar"). Profile from `app_user`
 * (MLG-02); leader and area names from [DemoDirectory]. "Reset data demo" restores the prototype's local sample data.
 */
@Composable
fun AccountScreen(session: SalesSession, onLogout: () -> Unit, onResetDemo: () -> Unit, onSimulation: (() -> Unit)? = null) {
    val c = AmarTheme.colors
    val user = session.user
    Column(Modifier.fillMaxSize().background(c.background.canvas)) {
        AmarAppBar(title = stringResource(R.string.account_title), type = AmarAppBarType.Primary)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AmarFormTokens.PaddingHorizontal, vertical = AmarFormTokens.PaddingVertical),
            verticalArrangement = Arrangement.spacedBy(AmarFormTokens.FieldGap),
        ) {
            AmarCard(style = AmarCardStyle.Border, modifier = Modifier.fillMaxWidth()) {
                Text(user.fullName, color = c.content.primary, style = AmarTheme.typography.headingSm)
                Row(horizontalArrangement = Arrangement.spacedBy(AmarSuccessTokens.RowGap)) {
                    AmarBadge(stringResource(roleLabel(session.role)), size = AmarBadgeSize.Small)
                    user.tlLevel?.let { AmarBadge(stringResource(R.string.account_level, it.lowercase().replaceFirstChar(Char::uppercase)), size = AmarBadgeSize.Small) }
                }
                InfoRow(R.string.account_email, user.email)
                user.phone?.let { InfoRow(R.string.account_phone, "+62 $it") }
                InfoRow(R.string.account_area, user.areaIds.mapNotNull { DemoDirectory.area(it)?.name }.joinToString().ifEmpty { "-" })
                InfoRow(R.string.account_supervisor, DemoDirectory.person(DemoDirectory.person(user.id)?.supervisorId ?: user.supervisorId)?.fullName ?: "-")
            }
            AmarButton(stringResource(R.string.account_logout), onLogout, Modifier.fillMaxWidth(), type = AmarButtonType.Secondary, size = AmarButtonSize.Large)
            AmarButton(stringResource(R.string.account_reset_demo), onResetDemo, Modifier.fillMaxWidth(), type = AmarButtonType.Naked, size = AmarButtonSize.Large)
            onSimulation?.let { AmarButton(stringResource(R.string.account_simulation), it, Modifier.fillMaxWidth(), type = AmarButtonType.Naked, size = AmarButtonSize.Large) }
        }
    }
}

@Composable
private fun InfoRow(label: Int, value: String) = com.example.and_sales_portal.ui.common.InfoRow(stringResource(label), value)

fun roleLabel(role: SalesRole): Int = when (role) {
    SalesRole.TL -> R.string.account_role_tl
    SalesRole.SR -> R.string.account_role_sr
    SalesRole.SA -> R.string.account_role_sa
}
