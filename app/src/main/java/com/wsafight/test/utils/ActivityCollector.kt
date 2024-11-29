package com.wsafight.test.utils

import android.app.Activity

object ActivityCollector {
    // 收集所有的 activity,以便于销毁。否则只是前往上一层
    private val activities = ArrayList<Activity>()

    fun addActivity(activity: Activity) {
        activities.add(activity)
    }

    fun removeActivity(activity: Activity) {
        activities.remove(activity)
    }

    fun finishAll() {
        // 遍历结束
        for (activity in activities) {
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
        activities.clear()
        // 杀死进程
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}

