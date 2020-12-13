package android.ricardoflor.turistdroid.activities.ui.mySites

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

/**
 * Clase DatePickerFragment.
 * Nos muestra un Dialog de un DatePicker
 */
class DatePickerFragment(val listener: (day: Int, month: Int, year: Int) -> Unit): DialogFragment(),
    DatePickerDialog.OnDateSetListener {

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        listener(dayOfMonth, month, year)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val cal = Calendar.getInstance()
        val dia = cal.get(Calendar.DAY_OF_MONTH)
        val mes = cal.get(Calendar.MONTH)
        val anio = cal.get(Calendar.YEAR)

        //val picker = DatePickerDialog(activity as Context, R.style.AppTheme_datePickerTheme, this, anio, mes, dia)
        val picker = DatePickerDialog(activity as Context, this, anio, mes, dia)
        // Coche como fecha maxima la de hoy
        picker.datePicker.maxDate = cal.timeInMillis

        return picker
    }
}