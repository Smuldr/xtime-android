@file:JvmName("ActivityUtils")

package com.xebia.xtime.shared

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import android.support.v4.content.ContextCompat
import android.view.WindowManager
import com.xebia.xtime.R

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun Activity.fixStatusBarColor() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        this.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        this.window.statusBarColor = ContextCompat.getColor(this, R.color.primaryDark)
    }
}
