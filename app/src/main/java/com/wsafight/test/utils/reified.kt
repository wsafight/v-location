package com.wsafight.test.utils

import android.content.Context
import android.content.Intent
import com.wsafight.test.SecondActivity

/**
 * 范型实化
 */
inline fun <reified T> simpleStartActivity(context: Context, block: Intent.() -> Unit) {
    // 其实也就是替代代码，不存在编译后还存在类型的可能
    val intent = Intent(context, T::class.java)
    intent.block()
    context.startActivity(intent)
}




class TestReified {
    fun source(context: Context) {
        val intent = Intent(context, SecondActivity::class.java)
        //加入数据
        intent.putExtra("extra_data", "123")
        intent.putExtra("extra_data2", "1223")
        // 开启里
        context.startActivity(intent)
    }

    // 上下是相同的
    fun useMethod (context: Context) {
        // 如果最后一个是闭包函数，可以直接不在括号内写
        simpleStartActivity<SecondActivity>(context) {
            putExtra("extra_data", "123")
            putExtra("extra_data2", "1223");
        }
    }
}