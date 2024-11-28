package com.wsafight.test.utils

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.SystemClock


object LocationHelper {

    private var mMockThread: Thread? = null
    private final var locationManager: LocationManager? = null

    /**
     * 初始化模拟定位，并检测是否开启ADB模拟定位
     * @param context
     * @return
     */
    public fun initLocation(context: Context) {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }


    public  fun startSetLocation(longitude: Double,latitude: Double ) {
        if (mMockThread != null) {
            return
        }
        mMockThread = Thread {
            while (true) {
                try {
                    Thread.sleep(500)
                    setLocation(longitude, latitude)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        mMockThread!!.start()
    }

    fun stopMockLocation() {
        locationManager!!.removeTestProvider(LocationManager.GPS_PROVIDER)
    }

    /**
     * GPS定位需要不停的刷新经纬度值
     */
    @Throws(java.lang.Exception::class)
    private fun setLocation(longitude: Double, latitude: Double) {
        try {
            val providerStr = LocationManager.GPS_PROVIDER
            val mockLocation: Location = Location(providerStr)
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

            locationManager!!.setTestProviderLocation(providerStr, mockLocation)
        } catch (e: java.lang.Exception) {
            // 防止用户在软件运行过程中关闭模拟位置或选择其他应用
            stopMockLocation()
            throw e
        }
    }
}