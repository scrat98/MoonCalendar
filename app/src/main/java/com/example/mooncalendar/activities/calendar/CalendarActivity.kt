package com.example.mooncalendar.activities.calendar

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.mooncalendar.databinding.CalendarActivityBinding
import com.example.mooncalendar.utils.DateChangedNotifier
import com.example.mooncalendar.utils.MoonPhaseCalculator
import com.example.mooncalendar.utils.TimeZoneChangedNotifier
import net.time4j.android.ApplicationStarter
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private companion object {
        const val CALENDAR_RANGE_YEARS: Long = 20
    }

    private lateinit var binding: CalendarActivityBinding

    private val dateChangedNotifier = DateChangedNotifier()

    private val timeZoneChangedNotifier = TimeZoneChangedNotifier()

    private val moonPhaseCalculator = MoonPhaseCalculator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationStarter.initialize(this, true);

        binding = CalendarActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val daysOfWeek = setupDaysLegend()
        setupCalendar(daysOfWeek.first(), CALENDAR_RANGE_YEARS)
        setupDateChangedListener()
        setupTimeZoneChangedListener()
    }

    private fun setupDaysLegend(): Array<DayOfWeek> {
        val daysOfWeek = daysOfWeekFromLocale()
        binding.legendLayout.root.children.forEachIndexed { index, view ->
            (view as TextView).apply {
                val dayName = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                text = dayName.uppercase()
            }
        }
        return daysOfWeek
    }

    private fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }

    private fun setupCalendar(
        firstDayOfWeek: DayOfWeek,
        calendarRangeYears: Long
    ) {
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusYears(calendarRangeYears)
        val lastMonth = currentMonth.plusYears(calendarRangeYears)
        setupCalendar(firstDayOfWeek, firstMonth, lastMonth)
    }

    private fun setupCalendar(
        firstDayOfWeek: DayOfWeek,
        firstMonth: YearMonth,
        lastMonth: YearMonth
    ) {
        binding.calendarView.dayBinder = MoonCalendarDayBinder(
            applicationContext,
            dateChangedNotifier::currentState,
            moonPhaseCalculator::getPhase
        )

        binding.calendarView.monthScrollListener = {
            val month = it.yearMonth.month
            val year = it.yearMonth.year
            binding.currentMonthText.text = "$month $year".uppercase()
        }

        binding.todayButton.setOnClickListener {
            binding.calendarView.smoothScrollToDate(dateChangedNotifier.currentState)
        }

        binding.calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
    }

    private fun setupDateChangedListener() {
        registerReceiver(dateChangedNotifier, dateChangedNotifier.getIntentFilter())
        dateChangedNotifier.subscribe {
            binding.calendarView.notifyDateChanged(it.before)
            binding.calendarView.notifyDateChanged(it.after)
            binding.calendarView.scrollToDate(it.after)
        }
    }

    private fun setupTimeZoneChangedListener() {
        registerReceiver(timeZoneChangedNotifier, timeZoneChangedNotifier.getIntentFilter())
        timeZoneChangedNotifier.subscribe {
            binding.calendarView.notifyCalendarChanged()
        }
    }
}

