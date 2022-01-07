package com.example.mooncalendar.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

abstract class BroadcastEventsNotifier<T : Any>(
    initValue: T
) : BroadcastReceiver() {

    data class ChangedEvent<T>(
        val before: T,
        val after: T
    )

    var currentState: T = initValue
        private set

    private val subscriber = AtomicReference(Consumer<ChangedEvent<T>> { })

    override fun onReceive(ctx: Context, intent: Intent) {
        if (actions.contains(intent.action)) {
            val before = currentState
            currentState = updateState(intent)
            subscriber.get().accept(ChangedEvent(before, currentState))
        }
    }

    fun subscribe(consumer: Consumer<ChangedEvent<T>>) {
        subscriber.set(consumer)
        consumer.accept(ChangedEvent(currentState, currentState))
    }

    abstract val actions: Set<String>

    abstract fun updateState(action: Intent): T

    fun getIntentFilter() = IntentFilter().apply {
        actions.forEach { addAction(it) }
    }
}

class DateChangedNotifier : BroadcastEventsNotifier<LocalDate>(LocalDate.now()) {

    override val actions = setOf(
        Intent.ACTION_DATE_CHANGED,
        Intent.ACTION_TIME_CHANGED
    )

    override fun updateState(action: Intent): LocalDate {
        return LocalDate.now()
    }
}

class TimeZoneChangedNotifier : BroadcastEventsNotifier<ZoneId>(ZoneOffset.systemDefault()) {

    override val actions = setOf(
        Intent.ACTION_TIMEZONE_CHANGED
    )

    override fun updateState(action: Intent): ZoneId {
        return ZoneOffset.systemDefault()
    }
}
