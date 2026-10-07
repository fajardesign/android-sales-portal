package com.example.and_sales_portal

import android.app.Application
import com.example.and_sales_portal.data.activity.ActivityRepository
import com.example.and_sales_portal.data.activity.OperationsRepository
import com.example.and_sales_portal.data.auth.AuthRepository
import com.example.and_sales_portal.data.auth.SalesSession
import com.example.and_sales_portal.data.device.Connectivity
import com.example.and_sales_portal.data.device.LocationService
import com.example.and_sales_portal.data.device.SimulationSettings
import com.example.and_sales_portal.data.partner.PartnerRepository
import com.example.and_sales_portal.data.sales.SalesRepository
import com.example.and_sales_portal.data.team.TeamService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.example.and_sales_portal.data.auth.SecureStore
import com.example.and_sales_portal.data.demo.DemoStore
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/** Application with a minimal service container for the prototype (no DI framework). */
class SalesApp : Application() {

    /** Created on first use (not in `onCreate`), so tests that only start the Application do not touch the Keystore. */
    val container: AppContainer by lazy { AppContainer(this) }
}

/** Shared singletons: the Supabase client (prototype backend) and the repositories built on it. */
class AppContainer(app: Application) {
    val secureStore = SecureStore(app)

    val supabase = createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_PUBLISHABLE_KEY) {
        install(Auth) {
            sessionManager = secureStore
            alwaysAutoRefresh = true
            autoLoadFromStorage = true
        }
        install(Postgrest)
    }

    val demo = DemoStore(app)
    val partners = PartnerRepository(demo)
    val operations = OperationsRepository(demo, partners)
    val team = TeamService(partners, operations)
    val sales = SalesRepository(partners, operations)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val simulation = SimulationSettings(app)
    val connectivity = Connectivity(app, simulation, scope)
    val location = LocationService(app, simulation)

    private var activityRepo: ActivityRepository? = null

    /** Attendance and visits of the signed-in user (one repository per account, rebuilt when the account changes). */
    fun activity(session: SalesSession): ActivityRepository {
        val user = session.user
        activityRepo?.takeIf { it.userId == user.id }?.let { return it }
        return ActivityRepository(operations, partners, user.id, session.role).also { activityRepo = it }
    }

    val auth = AuthRepository(supabase, secureStore, BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_PUBLISHABLE_KEY)
}

/** The app's [AppContainer]. */
val android.content.Context.appContainer: AppContainer get() = (applicationContext as SalesApp).container
