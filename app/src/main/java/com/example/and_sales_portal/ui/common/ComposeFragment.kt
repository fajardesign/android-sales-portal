package com.example.and_sales_portal.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.and_sales_portal.appContainer
import com.example.and_sales_portal.data.activity.ActivityRepository
import com.example.and_sales_portal.data.auth.SalesSession
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import kotlinx.coroutines.flow.MutableStateFlow

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

    /** Attendance and visits of the signed-in user. */
    protected val activityRepo: ActivityRepository get() = container.activity(requireNotNull(session))

    protected fun navigate(@IdRes destination: Int, args: Bundle? = null) = findNavController().navigate(destination, args)

    protected fun back() {
        findNavController().popBackStack()
    }

    /** Closes this screen and shows [message] as a success toast on the previous one. */
    protected fun backWithToast(message: String) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(KEY_TOAST, message)
        back()
    }

    /** Toast sent by the screen that just closed ([backWithToast]), and how to clear it. */
    @Composable
    protected fun incomingToast(): Pair<String?, () -> Unit> {
        val handle = remember { findNavController().currentBackStackEntry?.savedStateHandle }
        val flow = remember { handle?.getStateFlow<String?>(KEY_TOAST, null) ?: MutableStateFlow(null) }
        val message by flow.collectAsState()
        return message to { handle?.set<String?>(KEY_TOAST, null) }
    }

    private companion object {
        const val KEY_TOAST = "toast"
    }
}
