package com.example.and_sales_portal.ui.shell

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.designsystem.components.extended.AmarNavIconLayer
import com.example.and_sales_portal.ui.auth.LoginFragment

/** A bottom navigation tab: destination in `nav_main`, label and icons. */
enum class SalesTab(
    @IdRes val destination: Int,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    val activeIcon: List<AmarNavIconLayer>,
    val inactiveIcon: List<AmarNavIconLayer> = listOf(AmarNavIconLayer(icon)),
) {
    TlHome(R.id.tl_home, R.string.nav_beranda, R.drawable.ds_ic_menu_home_24, homeActive),
    SalesHome(R.id.sales_home, R.string.nav_beranda, R.drawable.ds_ic_menu_home_24, homeActive),
    Partner(
        R.id.partner_tab, R.string.nav_partner, R.drawable.ds_ic_menu_store_24,
        (0..5).map { AmarNavIconLayer(storeLayers[it], inverse = it % 2 == 1) },
    ),
    Team(R.id.team_tab, R.string.nav_tim, R.drawable.ds_ic_contact_24, listOf(AmarNavIconLayer(R.drawable.ds_ic_contact_24))),
    Activity(R.id.activity_tab, R.string.nav_aktivitas, R.drawable.ds_ic_todo_line_24, listOf(AmarNavIconLayer(R.drawable.ds_ic_todo_fill_24))),
    Visits(R.id.visits_tab, R.string.nav_kunjungan, R.drawable.ds_ic_location_24, listOf(AmarNavIconLayer(R.drawable.ds_ic_location_24))),
    Performance(
        R.id.performance_tab, R.string.nav_kinerja, R.drawable.ds_ic_target_24_l0,
        targetLayers, targetLayers,
    ),
    Account(R.id.account_tab, R.string.nav_akun, R.drawable.ds_ic_menu_profile_24, listOf(AmarNavIconLayer(R.drawable.ds_ic_menu_profile_filled_24))),
    ;

    companion object {
        /** PRD v3 "Login on mobile": TL Beranda, Partner, Tim, Aktivitas, Akun; SA/SR Beranda, Kunjungan, Partner, Kinerja, Akun. */
        fun forRole(role: SalesRole): List<SalesTab> = when (role) {
            SalesRole.TL -> listOf(TlHome, Partner, Team, Activity, Account)
            SalesRole.SR, SalesRole.SA -> listOf(SalesHome, Visits, Partner, Performance, Account)
        }

        fun home(role: SalesRole): SalesTab = forRole(role).first()

        fun of(@IdRes destination: Int): SalesTab? = entries.firstOrNull { it.destination == destination }
    }
}

private val targetLayers = listOf(
    AmarNavIconLayer(R.drawable.ds_ic_target_24_l0), AmarNavIconLayer(R.drawable.ds_ic_target_24_l1, inverse = true), AmarNavIconLayer(R.drawable.ds_ic_target_24_l2),
)
private val homeActive = listOf(AmarNavIconLayer(R.drawable.ds_ic_menu_home_filled_24_l0), AmarNavIconLayer(R.drawable.ds_ic_menu_home_filled_24_l1, inverse = true))
private val storeLayers = listOf(
    R.drawable.ds_ic_menu_store_filled_24_l0, R.drawable.ds_ic_menu_store_filled_24_l1, R.drawable.ds_ic_menu_store_filled_24_l2,
    R.drawable.ds_ic_menu_store_filled_24_l3, R.drawable.ds_ic_menu_store_filled_24_l4, R.drawable.ds_ic_menu_store_filled_24_l5,
)

/** Navigation between the shell's top-level places (splash, login, tabs). */
class ShellNavigator(private val nav: NavController) {
    constructor(fragment: Fragment) : this(fragment.findNavController())

    /** After login or a restored session: the role's Beranda, with nothing to go back to. */
    fun toHome(role: SalesRole) = nav.navigate(SalesTab.home(role).destination, null, clearAll())

    /** After logout or an expired session. */
    fun toLogin(expired: Boolean) = nav.navigate(R.id.login, LoginFragment.args(expired), clearAll())

    /** Bottom navigation: one copy per tab, state of each tab kept, Back from a tab returns to Beranda. */
    fun toTab(tab: SalesTab, home: SalesTab) = nav.navigate(
        tab.destination,
        null,
        NavOptions.Builder().setLaunchSingleTop(true).setRestoreState(true).setPopUpTo(home.destination, inclusive = false, saveState = true).build(),
    )

    private fun clearAll() = NavOptions.Builder().setPopUpTo(R.id.nav_main, inclusive = true).build()
}
