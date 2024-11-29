package com.wsafight.test.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 权限辅助工具，是单例模式
 * kt 直接使用 object 即可，无需 class
 */
object PermissionHelper {
    fun requestPermissionThenAction(
        context: Activity,
        needPermission: String,
        requestCode: Int = 1,
        /**
         * 这里是闭包，大家参考 JavaScript 即可，但是有两个不同
         *
         * 第一点首先是 用 {} 包裹
         * 没有参数直接写方法体即可，无需写 () -> this.bb()
         * 有且只有一个参数时候，无需写 params -> this.bb(params)
         * 直接写 this.bb(it) it 代表第一个参数，类似于
         *  this.requestPermissionThenAction(context, "test", 1, {
         *      this.bb()
         *  })
         * 可以参数 arrayOf("123", "12345").map { it.length }.filter { it > 3 }
         *
         * 第二点
         * 如果最后一个参数是闭包函数，开发这可以这样写
         *  this.requestPermissionThenAction(context, "test", 1) {
         *      this.bb()
         *  }
         *  大家可以对比一下
         */
        action: () -> Unit,
    ) {
        // 检查当前安卓应用是否授权操作
        if (ContextCompat.checkSelfPermission(context,
                needPermission) != PackageManager.PERMISSION_GRANTED) {
            // 没有则请求权限
            ActivityCompat.requestPermissions(
                context,
                arrayOf(needPermission), requestCode)
        } else {
            action()
        }
    }

     fun onRequestPermissionsResult(grantResults: IntArray, fail: () -> Unit = {}, success: () -> Unit) {
         // 为空或者没有授权成功直接返回
         if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
             fail()
             return
         }
         success()
    }


}