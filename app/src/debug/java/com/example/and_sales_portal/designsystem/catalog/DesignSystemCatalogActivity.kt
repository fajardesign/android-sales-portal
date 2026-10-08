package com.example.and_sales_portal.designsystem.catalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/** Debug-only entry point ("DS Catalog" launcher icon) for [DesignSystemCatalogScreen]. Not part of release builds. */
class DesignSystemCatalogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { AmarTheme { DesignSystemCatalogScreen() } }
    }
}
