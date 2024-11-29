package com.wsafight.test.services

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.wsafight.test.MainActivity
import com.wsafight.test.constants.SecondPage
import kotlin.concurrent.thread

class VLocationService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    private lateinit var locationManager: LocationManager

    /**
     * 第一次创建时调用
     */
    override fun onCreate() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    /**
     * 在每次 Service 启动的时候调用
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // 此处会自我停止运行
        // stopSelf();

        var time = 60;

        thread {
            // doSomeThing
            // 我们可以通过不断 pull 来确认当前 Service 是否执行了然后在外部关闭
            // 有需要可以直接在内部开启一个线程来处理，完毕后直接关闭即可
            // 也可以使用 intentService,不过不推荐使用
            val longitude = intent.getDoubleExtra("longitude", 0.0)

            val latitude = intent.getDoubleExtra("latitude", 0.0)


            while (time > 0) {
                Thread.sleep(500)
                try {
                    setLocation(longitude, latitude)

                    Log.d("VLocationService", "查看数据")
                } catch (e: Exception) {
                    e.printStackTrace();
                }
                time--
            }

            // 30 s 后停止操作
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun stopMockLocation() {
        locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
    }

    /**
     * GPS定位需要不停的刷新经纬度值
     */
    private fun setLocation(longitude: Double, latitude: Double) {
        try {
            val providerStr = LocationManager.GPS_PROVIDER
            val mockLocation = Location(providerStr)
            // 经度
            mockLocation.longitude = longitude
            // 纬度
            mockLocation.latitude = latitude
            mockLocation.time = System.currentTimeMillis() // 本地时间

            mockLocation.setAltitude(0.0) // 高程（米）
            mockLocation.setBearing(0F) // 方向（度）
            mockLocation.setSpeed(0F) //速度（米/秒）
            mockLocation.setAccuracy(2F) // 精度（米）

            //api 16以上的需要加上这一句才能模拟定位
            mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos())

            locationManager.setTestProviderLocation(providerStr, mockLocation)
        } catch (e: java.lang.Exception) {
            // 防止用户在软件运行过程中关闭模拟位置或选择其他应用
            stopMockLocation()
            throw e
        }
    }

    /**
     * 在 Service 销毁的时候调用
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyService", "onDestroy")
    }

    companion object {
        fun startVLocationService (context: Activity,longitude: Double, latitude: Double ) {
            val intent = Intent(context, VLocationService::class.java)
            intent.putExtra("longitude", longitude)
            intent.putExtra("latitude", latitude)
            context.startService(intent)
        }

    }

}