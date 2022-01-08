package com.example.mooncalendar.activities.calendar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.children
import com.example.mooncalendar.R
import com.example.mooncalendar.databinding.CalendarActivityBinding
import com.example.mooncalendar.utils.*
import net.time4j.android.ApplicationStarter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private companion object {
        const val CALENDAR_RANGE_YEARS: Long = 20
        const val NOTIFICATION_CHANNEL_ID = "notifications"
        const val NOTIFICATION_ID = 0
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
        setupNotifications()
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

    private fun setupNotifications() {
        binding.notificationSwitch.isChecked = false
        binding.notificationMoonPhases.isEnabled = false
        binding.notificationMoonPhases.adapter = ArrayAdapter(
            baseContext,
            R.layout.support_simple_spinner_dropdown_item,
            MoonPhase.values().map { it.toString() }
        )

        binding.settingsButton.setOnClickListener {
            val visible = binding.notificationPanel.visibility
            if (visible == View.VISIBLE) {
                binding.notificationPanel.visibility = View.GONE
            } else {
                binding.notificationPanel.visibility = View.VISIBLE
            }
        }
        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.notificationMoonPhases.isEnabled = isChecked
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_ID,
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun setupDateChangedListener() {
        registerReceiver(dateChangedNotifier, dateChangedNotifier.getIntentFilter())
        dateChangedNotifier.subscribe {
            binding.calendarView.notifyDateChanged(it.before)
            binding.calendarView.notifyDateChanged(it.after)
            binding.calendarView.scrollToDate(it.after)
            handleNotification(it)
        }
    }

    private fun handleNotification(event: BroadcastEventsNotifier.ChangedEvent<LocalDate>) {
        val enabled = binding.notificationSwitch.isChecked
        if (!enabled) return

        val stateView = binding.notificationMoonPhases
        val state = stateView.getItemAtPosition(stateView.selectedItemPosition).toString()
            .let { MoonPhase.valueOf(it) }
        val phase = moonPhaseCalculator.getPhase(event.after, ZoneId.systemDefault())
        if (state == phase) {
            showNotification(phase)
        }
    }

    private fun showNotification(moonPhase: MoonPhase) {
        val resultIntent = Intent(this, CalendarActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(moonPhase.imageId)
            .setContentTitle("Moon phase happened: $moonPhase")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(NOTIFICATION_ID, notification)
    }

    private fun setupTimeZoneChangedListener() {
        registerReceiver(timeZoneChangedNotifier, timeZoneChangedNotifier.getIntentFilter())
        timeZoneChangedNotifier.subscribe {
            binding.calendarView.notifyCalendarChanged()
        }
    }
}

