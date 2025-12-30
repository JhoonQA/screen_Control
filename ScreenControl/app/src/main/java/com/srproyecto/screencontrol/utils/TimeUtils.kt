package com.srproyecto.screencontrol.utils

import java.util.concurrent.TimeUnit

object TimeUtils {
    fun formatMillisToHHmm(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        return String.format("%02dh:%02dm", hours, minutes)
    }
}