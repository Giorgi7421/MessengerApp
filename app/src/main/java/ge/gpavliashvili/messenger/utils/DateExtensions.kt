package ge.gpavliashvili.messenger.utils

import java.text.SimpleDateFormat
import java.util.*

fun Long.toFormattedDate(): String {
    val currentTime = System.currentTimeMillis()
    val timeDifference = currentTime - this
    
    val oneMinute = 60 * 1000L
    val oneHour = 60 * oneMinute
    val oneDay = 24 * oneHour
    
    return when {
        timeDifference < oneHour -> {
            val minutes = (timeDifference / oneMinute).toInt()
            if (minutes <= 1) "1 min" else "$minutes min"
        }
        timeDifference < oneDay -> {
            val hours = (timeDifference / oneHour).toInt()
            if (hours == 1) "1 hour" else "$hours hours"
        }
        else -> {
            val date = Date(this)
            val formatter = SimpleDateFormat("d MMM", Locale.ENGLISH)
            formatter.format(date).uppercase()
        }
    }
}

fun Long.toFullDate(): String {
    val date = Date(this)
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
} 