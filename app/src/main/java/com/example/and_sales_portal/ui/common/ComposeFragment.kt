package com.example.and_sales_portal.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.example.and_sales_portal.appContainer
import com.example.and_sales_portal.data.auth.SalesSession
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/** Fragment whose UI is Compose (CLAUDE.md §2: one Fragment per Flow ID, content through `ComposeView`). */
abstract class ComposeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { AmarTheme { ScreenContent() } }
        }

    @Composable
    protected abstract fun ScreenContent()

    protected val container get() = requireContext().appContainer

    /** The signed-in user; screens behind the shell are only reachable with a session. */
    protected val session: SalesSession? get() = container.auth.session.value
}
