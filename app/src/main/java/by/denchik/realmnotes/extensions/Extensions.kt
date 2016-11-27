package by.denchik.realmnotes.extensions

import android.view.Window
import android.view.WindowManager

fun <T> t(expr: Boolean, r1: T, r2: T): T {
    return if (expr) r1 else r2
}

fun Window.changeStatusBarColor(resourceId: Int) {
    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    statusBarColor = context.getColor(resourceId)
}
