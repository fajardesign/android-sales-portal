package com.example.and_sales_portal.designsystem.components.inputs

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure logic of [AmarDropdownPicker]: filtering and the Scroll=True rule, checked against the product screens. */
class AmarDropdownPickerTest {

    /** `Heading/MD` line height. */
    private val titleLine = 28.dp

    @Test
    fun scrollRuleMatchesProductScreens() {
        // Pilih Kelurahan: 6 options, Scroll=False, sheet 464 high.
        assertFalse(pickerNeedsScroll(6, searchable = false, titleLineHeight = titleLine))
        // Pilih Provinsi: 38 options, Scroll=True.
        assertTrue(pickerNeedsScroll(38, searchable = false, titleLineHeight = titleLine))
        // Pilih Bank: search field + 8 options, Scroll=True.
        assertTrue(pickerNeedsScroll(8, searchable = true, titleLineHeight = titleLine))
        // Boundary: 8 rows = 24 + 28 + 16 + 8×52 + 7×12 + 24 = 592 ≤ 600; 9 rows = 656.
        assertFalse(pickerNeedsScroll(8, searchable = false, titleLineHeight = titleLine))
        assertTrue(pickerNeedsScroll(9, searchable = false, titleLineHeight = titleLine))
    }

    @Test
    fun filterIgnoresCaseAndSurroundingSpaces() {
        val banks = listOf("Bank Central Asia (BCA)", "Bank Rakyat Indonesia (BRI)", "Bank Mandiri")
        assertEquals(banks, filterPickerOptions(banks, "  ", { it }))
        assertEquals(listOf("Bank Mandiri"), filterPickerOptions(banks, " mandiri ", { it }))
        assertEquals(listOf("Bank Central Asia (BCA)"), filterPickerOptions(banks, "bca", { it }))
        assertEquals(emptyList<String>(), filterPickerOptions(banks, "jago", { it }))
    }
}
