package com.example.mooncalendar.activities.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.mooncalendar.R
import com.example.mooncalendar.activities.calendar.CalendarActivity
import com.example.mooncalendar.utils.MoonPhase
import java.time.LocalDate

class CalendarWidget : AppWidgetProvider() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val manager = AppWidgetManager.getInstance(context)
        val extras = intent.extras!!
        val ids = extras.get(AppWidgetManager.EXTRA_APPWIDGET_IDS) as IntArray
        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.calendar_widget)

            val intent = Intent(context, CalendarActivity::class.java)
            val openAppIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            views.setOnClickPendingIntent(R.id.layout, openAppIntent)

            val date = extras.get("date") as LocalDate
            val phase = extras.get("phase") as MoonPhase
            views.setTextViewText(R.id.date, date.toString())
            views.setImageViewResource(R.id.image, phase.imageId)
            manager.updateAppWidget(id, views)
        }
    }
}