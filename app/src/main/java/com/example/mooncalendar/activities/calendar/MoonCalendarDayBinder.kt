package com.example.mooncalendar.activities.calendar

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import com.example.mooncalendar.R
import com.example.mooncalendar.databinding.CalendarDayLayoutBinding
import com.example.mooncalendar.utils.MoonPhase
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.time.LocalDate
import java.time.ZoneId

class MoonCalendarDayBinder(
    private val context: Context,
    private val now: () -> LocalDate,
    private val phaseProvider: (LocalDate, ZoneId) -> MoonPhase
) : DayBinder<MoonCalendarDayBinder.DayViewContainer> {
    class DayViewContainer(view: View) : ViewContainer(view) {
        private val binding = CalendarDayLayoutBinding.bind(view)
        val container = binding.calendarDayContainer
        val textView = binding.calendarDayText
        val imageView = binding.calendarDayImage
    }

    override fun create(view: View): DayViewContainer {
        return DayViewContainer(view)
    }

    override fun bind(container: DayViewContainer, day: CalendarDay) {
        setupContent(container, day)
        markInAndOutDates(container, day)
        markCurrentDay(container, day)
    }

    private fun setupContent(container: DayViewContainer, day: CalendarDay) {
        val phase = phaseProvider(day.date, ZoneId.systemDefault())
        container.imageView.setImageResource(phase.imageId)
        container.textView.text = day.date.dayOfMonth.toString()
    }

    private fun markInAndOutDates(container: DayViewContainer, day: CalendarDay) {
        if (day.owner == DayOwner.THIS_MONTH) {
            container.textView.setTextColor(ContextCompat.getColor(context, R.color.black))
        } else {
            container.textView.setTextColor(ContextCompat.getColor(context, R.color.gray))
        }
    }

    private fun markCurrentDay(container: DayViewContainer, day: CalendarDay) {
        if (now() == day.date) {
            container.container.background = getDrawable(context, R.drawable.border)
        } else {
            container.container.background = null
        }
    }
}