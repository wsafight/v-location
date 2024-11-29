package com.wsafight.test.utils

import android.content.UriMatcher
import kotlin.reflect.KProperty


/**
 * 使用范型 + 委托属性
 *
 */
class Later<T>(val block: () -> T) {
    var value: Any? = null

    operator fun getValue(any: Any?, prop: KProperty<*>): T {
        // 如果当前为空，则执行 block
        if (value == null) {
            value = block()
        }

        return value as T
    }
}

class TestLater {
    /**
     * uriMatcher 属性的具体实现委托给了Delegate类去完成
     * 当调用属性的时候会自调用 Delegate 类的 getValue 方法
     * 当给p属性赋值的时候会自动调用 Delegate 类的 setValue 方法
     */
    private val uriMatcher by Later {
        val matcher = UriMatcher(UriMatcher.NO_MATCH)
        matcher
    }
}

/**
 * 开发者也可以委托类
 */
class DelegateSet<T>(private val helperSet: HashSet<T>) : Set<T> by helperSet {
    fun helloWorld() = println("Hello World")
}

class TestMySet {
    fun test() {
        val realSet = HashSet<String>()
        val delegateSet = DelegateSet<String>(realSet);
        delegateSet.isEmpty();
        delegateSet.helloWorld()

    }
}
