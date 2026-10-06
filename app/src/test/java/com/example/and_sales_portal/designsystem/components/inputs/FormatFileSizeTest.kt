package com.example.and_sales_portal.designsystem.components.inputs

import org.junit.Assert.assertEquals
import org.junit.Test

/** [formatFileSize]: Indonesian decimal comma, binary units, at most one decimal. */
class FormatFileSizeTest {

    @Test
    fun formatsWithIndonesianDecimalComma() {
        assertEquals("0 B", formatFileSize(0))
        assertEquals("512 B", formatFileSize(512))
        assertEquals("1 KB", formatFileSize(1024))
        assertEquals("1,5 KB", formatFileSize(1536))
        assertEquals("3,2 MB", formatFileSize((3.2 * 1024 * 1024).toLong()))
        assertEquals("2 MB", formatFileSize(2L * 1024 * 1024))
        assertEquals("1,2 GB", formatFileSize((1.2 * 1024 * 1024 * 1024).toLong()))
    }
}
