package com.example.and_sales_portal.ui.partnerregistration

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.and_sales_portal.R

/**
 * Host of the Partner Registration TL Assisted flow (REG-02 … REG-06 and the success screen), edge-to-edge with dark
 * system bar icons over the white App Bar. Entry point: TL Dashboard "Daftarkan Partner" (temporary button for now).
 */
class PartnerRegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partner_registration)
    }
}
