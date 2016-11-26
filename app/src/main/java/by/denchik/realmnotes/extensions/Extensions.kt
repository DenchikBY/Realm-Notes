package by.denchik.realmnotes.extensions

fun <T> t(expr: Boolean, r1: T, r2: T): T {
    return if (expr) r1 else r2
}
