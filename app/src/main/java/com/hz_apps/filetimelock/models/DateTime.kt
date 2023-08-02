package com.hz_apps.filetimelock.models

import java.time.LocalDate
import java.time.LocalTime

class DateTime {

    private lateinit var date: LocalDate
    private lateinit var time: LocalTime


    fun InitializeWithCurrentTime() {
        time = LocalTime.now()

    }

}