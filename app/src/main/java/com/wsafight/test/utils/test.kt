package top.littlerich.virtuallocation.util

import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Log

/**
 * Created by xuqingfu on 2017/4/24.
 */
object LocationUtil {
    private const val TAG = "silence"

    private var mLatitude = 30.6363334898
    private var mLongitude = 104.0486168861

    private var locationManager: LocationManager? = null
    private var canMockPosition = false

    /**
     * 判断在Android6.0+上是否将本程序添加到ADB模拟定位中
     */
    var hasAddTestProvider: Boolean = false
    private var mMockThread: Thread? = null


    /**
     * 初始化模拟定位，并检测是否开启ADB模拟定位
     * @param context
     * @return
     */
    fun initLocation(context: Context): Boolean {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        canMockPosition = (Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.ALLOW_MOCK_LOCATION,
            0
        ) != 0) || Build.VERSION.SDK_INT > 22
        Log.d(TAG, "hasAddTestProvider:" + canMockPosition)
        return canMockPosition
    }

    /**
     * 配置LocationManger参数
     */
    @Throws(Exception::class)
    fun initLocationManager() {
        if (canMockPosition && !hasAddTestProvider) {
            try {
                val providerStr = LocationManager.GPS_PROVIDER
                val provider = locationManager!!.getProvider(providerStr)
                if (provider != null) {
                    locationManager!!.addTestProvider(
                        provider.name,
                        provider.requiresNetwork(),
                        provider.requiresSatellite(),
                        provider.requiresCell(),
                        provider.hasMonetaryCost(),
                        provider.supportsAltitude(),
                        provider.supportsSpeed(),
                        provider.supportsBearing(),
                        provider.powerRequirement,
                        provider.accuracy
                    )
                } else {
                    locationManager!!.addTestProvider(
                        providerStr,
                        true, true, false, false, true, true, true,
                        Criteria.POWER_HIGH,
                        Criteria.ACCURACY_FINE
                    )
                }
                locationManager!!.setTestProviderEnabled(providerStr, true)
                locationManager!!.requestLocationUpdates(
                    providerStr,
                    0,
                    0f,
                    LocationStatuListener()
                )
                locationManager!!.setTestProviderStatus(
                    providerStr,
                    LocationProvider.AVAILABLE,
                    null,
                    System.currentTimeMillis()
                )
                Log.i(TAG, "already open GPS!")
                // 模拟位置可用
                hasAddTestProvider = true
                Log.d(TAG, "hasAddTestProvider：" + hasAddTestProvider)
                canMockPosition = true
            } catch (e: Exception) {
                canMockPosition = false
                Log.d(TAG, "初始化异常：$e")
                throw e
            }
        }
    }

    /**
     * 开启虚拟定位线程
     */
    fun startLocaton() {
        if (mMockThread == null) {
            mMockThread = Thread {
                while (true) {
                    try {
                        Thread.sleep(500)
                        if (!hasAddTestProvider) {
                            Log.d("xqf", "定位服务未打开")
                            continue
                        }
                        setLocation(mLatitude, mLongitude)
                        Log.d(
                            TAG,
                            "setLocation240=latitude:" + mLatitude + "?longitude:" + mLongitude
                        )
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            mMockThread!!.start()
        }
    }

    /**
     * GPS定位需要不停的刷新经纬度值
     */
    @Throws(Exception::class)
    private fun setLocation(latitude: Double, longitude: Double) {
        try {
            val providerStr = LocationManager.GPS_PROVIDER
            val mockLocation = Location(providerStr)
            mockLocation.latitude = latitude
            mockLocation.longitude = longitude
            mockLocation.altitude = 0.0 // 高程（米）
            mockLocation.bearing = 0f // 方向（度）
            mockLocation.speed = 0f //速度（米/秒）
            mockLocation.accuracy = 2f // 精度（米）
            mockLocation.time = System.currentTimeMillis() // 本地时间
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                //api 16以上的需要加上这一句才能模拟定位 , 也就是targetSdkVersion > 16
                mockLocation.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            }
            locationManager!!.setTestProviderLocation(providerStr, mockLocation)
        } catch (e: Exception) {
            // 防止用户在软件运行过程中关闭模拟位置或选择其他应用
            stopMockLocation()
            throw e
        }
    }

    fun stopMockLocation() {
        if (hasAddTestProvider) {
            try {
                locationManager!!.removeTestProvider(LocationManager.GPS_PROVIDER)
            } catch (ex: Exception) {
                // 若未成功addTestProvider，或者系统模拟位置已关闭则必然会出错
            }
            hasAddTestProvider = false
        }
    }

    /**
     * 设置地理经纬度值
     * @param mLongitude
     * @param mLatitude
     */
    fun setLongitudeAndLatitude(mLongitude: Double, mLatitude: Double) {
        LocationUtil.mLatitude = mLatitude
        LocationUtil.mLongitude = mLongitude
    }

    /**
     * 监听Location经纬度值的修改状态
     */
    private class LocationStatuListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            val lat = location.latitude
            val lng = location.longitude
            Log.i(TAG, String.format("location: x=%s y=%s", lat, lng))
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        }

        override fun onProviderEnabled(provider: String) {
        }

        override fun onProviderDisabled(provider: String) {
        }
    }
}