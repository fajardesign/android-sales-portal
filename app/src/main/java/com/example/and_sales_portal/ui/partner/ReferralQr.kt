package com.example.and_sales_portal.ui.partner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/**
 * Sample QR of a Kode Referral (PRD Scope 1 v1.1 C2, read-only): the same 21×21 pattern as the web portal's
 * `admin-sales-portal/src/lib/qr.js` (three finder squares and cells from a hash of the code). Not a scannable QR; the
 * real one is the image the Admin uploads (FR-019).
 */
object ReferralQr {
    const val N = 21

    /** `cells[y][x]` = dark module. */
    fun cells(code: String): Array<BooleanArray> {
        var h = 0L
        code.forEach { h = (h * 31 + it.code) and MASK }
        val grid = Array(N) { BooleanArray(N) }
        val finders = listOf(0 to 0, N - 7 to 0, 0 to N - 7)
        fun inFinder(x: Int, y: Int) = finders.any { (fx, fy) -> x in fx until fx + 7 && y in fy until fy + 7 }
        for (y in 0 until N) for (x in 0 until N) {
            if (inFinder(x, y)) continue
            h = (h * 1103515245 + 12345) and MASK
            grid[y][x] = h and 0x10000L != 0L
        }
        // Finder: 7×7 ring, white ring, 3×3 centre.
        finders.forEach { (fx, fy) ->
            for (dy in 0 until 7) for (dx in 0 until 7) {
                val ring = dx == 0 || dy == 0 || dx == 6 || dy == 6
                val centre = dx in 2..4 && dy in 2..4
                grid[fy + dy][fx + dx] = ring || centre
            }
        }
        return grid
    }

    private const val MASK = 0xFFFFFFFFL
}

@Composable
fun ReferralQrImage(code: String, description: String, modifier: Modifier = Modifier) {
    val cells = remember(code) { ReferralQr.cells(code) }
    val dark = AmarTheme.colors.content.primary
    Canvas(
        modifier
            .size(AmarActivityTokens.QrSize)
            .background(AmarTheme.colors.background.base)
            .semantics { contentDescription = description },
    ) {
        // One quiet module around the code, as the web SVG's viewBox.
        val unit = size.minDimension / (ReferralQr.N + 2)
        cells.forEachIndexed { y, row ->
            row.forEachIndexed { x, on ->
                if (on) drawRect(dark, Offset((x + 1) * unit, (y + 1) * unit), Size(unit, unit))
            }
        }
    }
}
