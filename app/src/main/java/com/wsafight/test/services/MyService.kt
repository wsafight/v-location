package com.wsafight.test.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.wsafight.test.MainActivity
import kotlin.concurrent.thread

/**
 * 安卓 4 大组件 Service
 * Service 默认在主线程中使用
 * 从Android 8.0系统开始，应用的后台功能被大幅削减
 * 现在只有当应用保持在前台可见状态的情况下，Service才能保证稳定运行，一旦进入后台之后，Service随时都有可能被系统回收
 * 所以需要使用 前台 Service
 * 每个 Service 只会存在一个实例
 */
class MyService : Service() {

    /**
     * Service 和 Activity 通信需要使用 Binder
     */
    class DownloadBinder : Binder() {
        fun startDownload() {
            Log.d("MyService", "startDownload executed")
        }
        fun getProgress(): Int {
            Log.d("MyService", "getProgress executed")
            return 0
        }
    }

    private val mBinder = DownloadBinder()

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    /**
     * 第一次创建时调用
     */
    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        // 构建一个前台 Service,不被回收
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("my_service", "前台Service通知",
                NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            this,
            0,
            // 意图
            intent,
            //  标记位, FLAG_IMMUTABLE 表示将约束外部应用消费 PendingIntent 修改其中的 Intent
            // 其他的自行查阅文档
            PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, "my_service")
            .setContentTitle("This is content title")
            .setContentText("This is content text")
            .setContentIntent(pi)
            .build()
        /**
         * 通过该方法就让其变成前台服务，会被用户看到通知
         * 必须添加如下代码
         * <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
         */
        startForeground(1, notification)
    }

    /**
     * 在每次 Service 启动的时候调用
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // 此处会自我停止运行
        // stopSelf();
        thread {
            // doSomeThing
            // 我们可以通过不断 pull 来确认当前 Service 是否执行了然后在外部关闭
            // 有需要可以直接在内部开启一个线程来处理，完毕后直接关闭即可
            // 也可以使用 intentService,不过不推荐使用
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 在 Service 销毁的时候调用
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyService", "onDestroy")
    }
}