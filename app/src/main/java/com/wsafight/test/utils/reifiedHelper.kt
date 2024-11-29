package com.wsafight.test.utils

import android.content.Context
import android.content.Intent

/**
 * 范型实化
 */
inline fun <reified T> simpleStartService(context: Context, block: Intent.() -> Unit) {
    // 其实也就是替代代码，不存在编译后还存在类型的可能
    val intent = Intent(context, T::class.java)
    intent.block()
    context.startService(intent)
}