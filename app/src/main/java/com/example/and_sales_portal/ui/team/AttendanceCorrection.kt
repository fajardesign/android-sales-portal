package com.example.and_sales_portal.ui.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.os.bundleOf
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.activity.CorrectionError
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.designsystem.components.actions.AmarChip
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBox
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBoxType
import com.example.and_sales_portal.designsystem.components.inputs.AmarTextArea
import com.example.and_sales_portal.designsystem.components.inputs.AmarTextField
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.attendance.AttendanceDay
import com.example.and_sales_portal.ui.attendance.AttendanceFragment
import com.example.and_sales_portal.ui.attendance.HistoryRow
import com.example.and_sales_portal.ui.attendance.statusLabel
import com.example.and_sales_portal.ui.common.ComposeFragment
import com.example.and_sales_portal.ui.common.Fmt
import com.example.and_sales_portal.ui.common.PersistentAction
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.example.and_sales_portal.ui.common.SectionTitle
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

/** What TL-11 shows: the day as the SA/SR sees it, whether it can still be corrected, and until when. */
data class CorrectionUiState(val name: String, val day: AttendanceDay, val canCorrect: Boolean, val deadline: LocalDate)

/** The form of TL-11: new status, check-in time "HH:mm" (not for Absen), reason. */
data class CorrectionForm(val status: AttendanceStatus? = null, val time: String = "", val reason: String = "", val error: CorrectionError? = null, val badTime: Boolean = false)

/**
 * Spec: TL-11 Koreksi Absensi (decision 2026-10-19, Q6; new): the TL corrects a subordinate's day — status Tepat Waktu /
 * Terlambat / Absen and the check-in time — with a required reason, up to the end of the following week. The day card
 * shows the earlier corrections ("Dikoreksi TL"). Past the deadline the form is disabled with the reason.
 */
@Composable
fun AttendanceCorrectionScreen(s: CorrectionUiState, form: CorrectionForm, onForm: (CorrectionForm) -> Unit, onBack: () -> Unit, onSave: () -> Unit) {
    val c = AmarTheme.colors
    ScreenScaffold(
        stringResource(R.string.cor_title), onBack,
        action = { PersistentAction(stringResource(R.string.cor_save), onSave, enabled = s.canCorrect && form.status != null) },
    ) {
        Text(s.name, color = c.content.primary, style = AmarTheme.typography.headingSm)
        HistoryRow(s.day)
        if (!s.canCorrect) {
            AmarInfoBox(stringResource(R.string.cor_expired, Fmt.longDate(s.deadline)), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
            return@ScreenScaffold
        }
        AmarInfoBox(stringResource(R.string.cor_deadline, Fmt.longDate(s.deadline)), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Info)
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
            SectionTitle(stringResource(R.string.cor_new_status))
            Row(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
                listOf(AttendanceStatus.ON_TIME, AttendanceStatus.LATE, AttendanceStatus.ABSENT).forEach { st ->
                    AmarChip(stringResource(statusLabel(st)), form.status == st, { onForm(form.copy(status = st, error = null)) })
                }
            }
            if (form.status == AttendanceStatus.ON_TIME || form.status == AttendanceStatus.LATE) {
                val timeError = when {
                    form.badTime -> stringResource(R.string.cor_err_time_format)
                    form.error == CorrectionError.TimeRequired -> stringResource(R.string.reg_required)
                    form.error == CorrectionError.TimeMismatch -> stringResource(if (form.status == AttendanceStatus.ON_TIME) R.string.cor_err_on_time else R.string.cor_err_late)
                    else -> null
                }
                AmarTextField(
                    form.time, { onForm(form.copy(time = it.filter { ch -> ch.isDigit() || ch == ':' }.take(5), badTime = false, error = null)) },
                    Modifier.fillMaxWidth(), label = stringResource(R.string.cor_time), required = true, placeholder = stringResource(R.string.cor_time_placeholder),
                    hint = timeError ?: stringResource(R.string.cor_time_hint, ActivityRules.label(ActivityRules.zoneOf(s.day.attendance?.employeeId ?: 0))),
                    error = timeError != null, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            AmarTextArea(
                form.reason, { onForm(form.copy(reason = it, error = null)) }, Modifier.fillMaxWidth(),
                label = stringResource(R.string.cor_reason), required = true, placeholder = stringResource(R.string.cor_reason_placeholder),
                hint = if (form.error == CorrectionError.ReasonRequired) stringResource(R.string.reg_required) else null,
                error = form.error == CorrectionError.ReasonRequired, maxLength = REASON_MAX,
            )
            if (form.error == CorrectionError.NoChange) AmarInfoBox(stringResource(R.string.cor_err_no_change), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning)
        }
    }
}

private const val REASON_MAX = 255

/** Spec: TL-11 Koreksi Absensi. Arguments: the SA/SR and the date. */
class AttendanceCorrectionFragment : ComposeFragment() {
    private val employeeId by lazy { requireArguments().getLong(ARG_EMPLOYEE) }
    private val date by lazy { LocalDate.parse(requireArguments().getString(ARG_DATE)) }

    @Composable
    override fun ScreenContent() {
        val tl = session ?: return
        val ops by container.operations.data.collectAsState()
        val o = container.operations
        var form by remember { mutableStateOf(CorrectionForm()) }
        var initialised by rememberSaveable { mutableStateOf(false) }
        val today = o.todayOf(employeeId)
        val a = remember(ops) { o.attendanceOf(employeeId, date) }
        if (!initialised) {
            // Start from the current values, so the TL changes only what is wrong.
            val st = ActivityRules.attendanceStatus(a, date, today).takeIf { it != AttendanceStatus.NOT_YET }
            val t = a?.checkInAt?.let { Fmt.time(it, a.clockIn?.let { p -> ActivityRules.zoneOf(p, employeeId) } ?: ActivityRules.zoneOf(employeeId)) }.orEmpty()
            form = CorrectionForm(st, t)
            initialised = true
        }
        val state = CorrectionUiState(
            DemoDirectory.person(employeeId)?.fullName.orEmpty(),
            AttendanceFragment.attendanceDay(a, date, today),
            o.canCorrect(employeeId, date),
            Instant.ofEpochMilli(o.correctionDeadline(date)).atZone(ActivityRules.LOCK_ZONE).toLocalDate(),
        )
        AttendanceCorrectionScreen(state, form, { form = it }, ::back) {
            val time = if (form.status == AttendanceStatus.ABSENT || form.time.isBlank()) null else parseTime(form.time)
            if (form.status != AttendanceStatus.ABSENT && form.time.isNotBlank() && time == null) {
                form = form.copy(badTime = true)
                return@AttendanceCorrectionScreen
            }
            val err = o.correct(tl.user.id, employeeId, date, requireNotNull(form.status), time, form.reason)
            if (err == null) backWithToast(getString(R.string.cor_done)) else form = form.copy(error = err)
        }
    }

    private fun parseTime(v: String): LocalTime? = try {
        LocalTime.parse(if (v.length == 4 && ':' !in v) "${v.take(2)}:${v.drop(2)}" else v)
    } catch (e: DateTimeParseException) {
        null
    }

    companion object {
        private const val ARG_EMPLOYEE = "employee_id"
        private const val ARG_DATE = "date"
        fun args(employeeId: Long, date: LocalDate) = bundleOf(ARG_EMPLOYEE to employeeId, ARG_DATE to date.toString())
    }
}
