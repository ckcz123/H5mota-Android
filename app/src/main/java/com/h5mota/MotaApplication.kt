package com.h5mota

import android.app.Application

import com.tencent.bugly.crashreport.CrashReport;


class MotaApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReport.initCrashReport(applicationContext, "7634c02376", false);
    }
}