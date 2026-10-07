package com.example.and_sales_portal

import android.app.Application
import com.example.and_sales_portal.data.auth.AuthRepository
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

    val auth = AuthRepository(supabase, secureStore, BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_PUBLISHABLE_KEY)
}

/** The app's [AppContainer]. */
val android.content.Context.appContainer: AppContainer get() = (applicationContext as SalesApp).container
