package com.wsafight.test

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.wsafight.test.constants.CameraTestProviderAuthority
import com.wsafight.test.constants.SecondPage
import com.wsafight.test.ui.theme.Theme
import com.wsafight.test.utils.BaseActivity
import com.wsafight.test.utils.ImgHelper
import com.wsafight.test.utils.PermissionHelper
import java.io.File
import kotlin.concurrent.thread


/**
 * 安卓 4 大组件 Activity
 * 一个 Activity 通常就是一个单独的屏幕(窗口)
 * Android 应用中每一个 Activity 都必须要在 AndroidManifest.xml 配置文件中声明，否则系统将不识别也不执行该Activity
 */
class MainActivity : BaseActivity() {

    /**
     * 安卓 4 大组件 Broadcast Receiver
     * Broadcast Receiver( 广播接收者 )顾名思义就是用来接收来自系统和应用中的广播的系统组件
     *
     * 从注册方法分为 静态和动态
     *
     * 静态广播需要在 Androidmanifest.xml 中进行注册，这中方式注册的广播，不受页面生命周期的影响，即使退出了页面，也可以收到广播这种广播一般用于想开机自启动
     * 由于这种注册的方式的广播是常驻型广播，所以会占用CPU的资源
     * 动态广播是在代码中注册的，这种注册方式也叫非常驻型广播，收到生命周期的影响
     *
     * 从使用上分为有序广播和无序广播
     * 有序广播的接收有优先级，并且优先级高的可以组织阻止继续传递
     * 无序广播，可以在同一时刻(逻辑上)被所有接收者接收到
     *
     * 此处接收安卓系统广播的时间改变，进行 toast 展示
     *
     * 如果需要全局性的广播，可以直接写在 BaseActivity 中去，如用户授权失效等功能
     *
     */
    inner class TimeChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Toast.makeText(context, "Time has changed", Toast.LENGTH_SHORT).show()
        }


    }

    /**
     * lastinit 延迟初始化，lateinit
     */
    lateinit var timeChangeReceiver: TimeChangeReceiver


    /**
     * 在 onCreate 中注册
     */
    private fun registerTimeChangeReceiver () {
        val intentFilter = IntentFilter()
        intentFilter.addAction("android.intent.action.TIME_TICK")
        timeChangeReceiver = TimeChangeReceiver()
        registerReceiver(timeChangeReceiver, intentFilter)
    }

    /**
     * 关闭后注销接受通知
     */
    private fun unRegisterTimeChangeReceiver () {
        unregisterReceiver(timeChangeReceiver)
    }


    /**
     * 安卓生命周期 onDestroy
     */
    override fun onDestroy() {
        super.onDestroy()
        this.unRegisterTimeChangeReceiver();
    }

    private val gotoSecondRequestCode = 1

    /**
     * 去次要页面
     *
     */
    fun gotoSecondPageForResult() {
        // 设置意图
        val intent = Intent(SecondPage)
        //加入数据
        intent.putExtra("extra_data", "123");
        // 开启里
        startActivityForResult(intent, gotoSecondRequestCode)
    }

    private fun gotoSecondPage () {
        SecondActivity.gotoCurrent(this, "123");
    }

    /**
     * 对应  gotoSecondPageForResult startActivityForResult 中获取返回的数据
     * 如果不需要建议，使用 gotoSecondPage 中的方法
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            gotoSecondRequestCode -> if (resultCode == RESULT_OK) {
                val returnedData = data?.getStringExtra("data_return")
                Toast.makeText(this, "123123", Toast.LENGTH_SHORT).show()
                Log.d("FirstActivity", "returned data is $returnedData")
            }
            takePhotoRequestCode -> if (resultCode == Activity.RESULT_OK) {
                // 将拍摄的照片显示出来
                val bitmap = BitmapFactory.decodeStream(contentResolver.
                openInputStream(imageUri))
                // 调用照相机程序去拍照有可能会在一些手机上发生照片旋转的情况。这是因为
                // 这些手机认为打开摄像头进行拍摄时手机就应该是横屏的，因此回到竖屏的情况下就会发生90度的旋转。
                // 为此，这里我们又加上了判断图片方向的代码
                val fixedBitmap =  ImgHelper.rotateIfRequired(outputImage.path, bitmap)
                // TODO 将修正后的 bitMap 放入控件中
            }
            openAlbumRequestCode -> if (resultCode == Activity.RESULT_OK && data != null) {
                data.data?.let { uri ->
                    // 将选择的图片显示
                    val bitmap = contentResolver
                        .openFileDescriptor(uri, "r")?.use {
                            BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
                        }
                    // TODO 将修正后的 bitMap 放入控件中
                }
            }
        }
    }



    /**
     * 需要在 AndroidManifest 中注册并授权，注册后，安卓系统方便告知应用需要的权限
     *     <uses-feature
     *         android:name="android.hardware.telephony"
     *         android:required="true" />
     *     <uses-permission android:name="android.permission.CALL_PHONE" />
     *  安卓权限较为复杂，这里简单介绍一下
     */
    private fun tel() {
        try {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:10086")
            startActivity(intent)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * 可变用 var，不可变用 val，类比 JavaScript 中 const
     * 当然还有 const val。这个是在编译时确定好且不可变，val 是在运行时
     */
    private val phonePermissionRequestCode = 1;
    private val contactPermissionRequestCode = 2;


    // 一般来说调用这个方法,有权限直接拨打，没有就请求
    private fun requestPermissionThenTel() {
        PermissionHelper.requestPermissionThenAction(this, android.Manifest.permission.CALL_PHONE,
            phonePermissionRequestCode
        )  {
            this.tel()
        }
    }

    /**
     * 请求之后用户同意或者拒绝会回调此函数
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 类似于 switch 可以直接返回数据
        when (requestCode) {
            phonePermissionRequestCode -> {
                PermissionHelper.onRequestPermissionsResult(
                    grantResults, {
                        // 拒绝后弹出弹窗
                        Toast.makeText(this, "You denied the permission",
                            Toast.LENGTH_SHORT).show()
                    }) {
                    // 同意了直接拨打电话
                    this.tel()
                }
            }
            contactPermissionRequestCode -> {
                PermissionHelper.onRequestPermissionsResult(
                    grantResults, {
                        Toast.makeText(this, "You denied the permission",
                            Toast.LENGTH_SHORT).show()
                    }
                ) {
                    this.readContacts();
                }

            }
        }
    }

    @SuppressLint("Range")
    fun readContacts(): ArrayList<String> {
        val constants = ArrayList<String>();

        /**
         * 对于每一个应用程序来说，如果想要访问ContentProvider中共享的数据，就一定要借助 ContentResolver 类
         */
        contentResolver.query(
            /**
             * 不同于 SQLite，ContentResolver 中的增删改查方法都是不接收表名参数的
             * 而是使用一个Uri参数代替，这个参数被称为内容URI
             *
             * 内容 URI 最标准的格式如下：
             * content://com.wsafight.test.provider/table1
             * content://com.wsafight.test.provider/table2
             *
             * 指定查询某个应用程序下的某一张表
             */
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            /**
             * 指定查询的列名
             */
            null,
            /**
             * 指定where的约束条件
             */
            null,
            /**
             * 为 where 中的占位符提供具体的值
             */
            null,
            /**
             * 指定查询结果的排序方式
             */
            null)
            /**
             * ?. 就是 JavaScript 中的这个
             * apply 方便调用
             * 下文中的 contentResolver.moveToNext 和 contentResolver.close
             * 可以直接用 moveToNext 和 close
             * 同样的有 run，let，also
             *
             * run- 返回您想要的任何内容，并重新确定其使用的变量的范围 this
             * apply - 类似，但它将返回this
             * let- 主要用于避免空检查，但也可以用作 run 的替代品
             * also- 当你想使用apply但不想阴影时使用它this
             */
            ?.apply {
                while (moveToNext()) {
                    // 获取联系人姓名
                    val displayName = getString(getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    // 获取联系人手机号
                    val number = getString(getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER))
                    constants.add("$displayName\n$number")
                }
                close()
            }

        Log.d("MainActivity::readContacts", constants.toString())

        return constants;

    }


    /**
     * 安卓 4 大组件 ContentProvider
     * ContentProvider的用法一般有两种：
     * 一种是使用现有的 ContentProvider 读取和操作相应程序的数据(用别人的)，注：需要权限
     * 一种是创建自己的 ContentProvider，给程序的数据提供外部访问接口(给别人用)
     */
    fun requestPermissionThenReadContacts() {
        PermissionHelper.requestPermissionThenAction(this, android.Manifest.permission.READ_CONTACTS,
            contactPermissionRequestCode
        )  {
            this.readContacts()
        }
    }

    /**
     * 安卓通知渠道（可以订阅具体的通知类型）
     * 每一个应用程序都可以自由地创建当前应用拥有哪些通知渠道，但是这些通知渠道的控制权是掌握在用户手上的。
     * 安卓系统 13 以上需要添加权限申请
     * <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
     */
    private fun sendSystemNotice () {
        // 通过字符串确定使用哪一个系统服务
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

        //  createNotificationChannel()方法都是 Android 8.0 系统中新增的API，
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /**
             * 渠道id、渠道名称重要等级
             * 通知的重要等级从高到低主要有 IMPORTANCE_HIGH、IMPORTANCE_DEFAULT、 IMPORTANCE_LOW、IMPORTANCE_MIN
             * 开发者只能在创建通知渠道的时候为它指定初始的重要等级，如果用户不认可这个重要等级的话，可以随时进行修改
             * 开发者对此无权再进行调整和变更，因为通知渠道一旦创建就不能再通过代码修改了
             */
            val channel = NotificationChannel("normal", "Normal",NotificationManager.
            IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        // 跳转进入当前 Activity
        val intent = Intent(this, MainActivity::class.java)

        // 添加一个延迟执行的 intent(意图),
        val pi = PendingIntent.getActivity(
            this,
            0,
            // 意图
            intent,
            //  标记位, FLAG_IMMUTABLE 表示将约束外部应用消费 PendingIntent 修改其中的 Intent
            // 其他的自行查阅文档
            PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, "normal")
            // setContentTitle 方法用于指定通知的标题内容,下拉系统状态栏就可以看到这部分内容
            .setContentTitle("This is content title")
            // setContentText 指定通知的正文内容
            .setContentText("This is content text")
            // setSmallIcon 方法设置小图标
            .setSmallIcon(R.drawable.ic_launcher_background)
            //
            .setContentIntent(pi)
            // 点击后会自动取消
            .setAutoCancel(true)
            // setLargeIcon 方法设置大图标
            //.setLargeIcon(R.drawable)
            .build()
        // 发送通知，保证每一个 id 不相同的,如果需要调用 取消则需要传入对应的 id
        manager.notify(1, notification)
        // 取消通知
        // manager.cancel(1)
    }

    private val takePhotoRequestCode = 2



    // 晚点初始化，可以用 ::imageUri.isInitialized 来判断
    lateinit var imageUri: Uri
    lateinit var outputImage: File

    /**
     * 多媒体拍照
     */
    fun startPhotograph () {
        // 因为从Android 6.0系统开始，读写SD卡
        // 被列为了危险权限，如果将图片存放在SD卡的任何其他目录，都要进行运行时权限处理才行，
        // 使用应用关联目录则可以跳过这一步。默认保存在 /sdcard/Android/data/<package name>/cache
        outputImage = File(externalCacheDir, "xxx.jpg")
        if (outputImage.exists()) {
            outputImage.delete()
        }


        outputImage.createNewFile()

        // 从Android 7.0系统开始，直接使用本地真实路径的Uri被认为是不安全的，会抛出一个FileUriExposedException异常
        imageUri =
            FileProvider.getUriForFile(this, CameraTestProviderAuthority, outputImage)

        // 启动相机程序
        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, takePhotoRequestCode)
    }

    private val openAlbumRequestCode = 3

    /**
     * 打开相册
     */
    fun openAlbum () {
        // 打开文件选择器
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        // 指定只显示图片
        intent.type = "image/*"
        startActivityForResult(intent, openAlbumRequestCode)
    }


    val mediaPlayer = MediaPlayer()

    /**
     * 播放音频
     */
    fun prepareMedia () {
        // 读取 src main 下面的额 assets 文件夹
        val assetManager = assets
        // 获取 mp3 句柄
        val fd = assetManager.openFd("mp3.mp3")
        // 设置资源
        mediaPlayer.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
        mediaPlayer.prepare()
        // 其他操作
        mediaPlayer.start()
        mediaPlayer.stop()
        // 释放资源
        mediaPlayer.release();
    }

    fun prepareVideo () {
        // 视频无法放入 assets 中，在 res raw 里面。注：视频不可用
        val uri = Uri.parse("android.resource://$packageName/${R.raw.video}")
        // 类似与 dom 设置 attr
        // videoView.setVideoURI(uri)
        // 开始播放
        // videoView.start()
        // pause 暂停
        // resume 从头开始
        // seekTo 指定位置播放
        // isPlaying 是否正在播放
        // getDuration 获取播放时长
        // suspend 释放资源
    }


    /**
     * 和许多其他的GUI库一样，Android的 UI 也是线程不安全的。也就是说，如果想要更新应用程序
     * 里的UI元素，必须在主线程中进行，否则就会出现异常。这点和 浏览器 是一致的，没啥可说的
     */
    fun changeBtnNameErr () {
        // 语法糖，开启一个线程
        thread {
            // 报错
            // btnView.text = "123";
        }
    }

    val updateBtnText = 1

    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            // 在这里可以进行UI操作
            when (msg.what) {
                updateBtnText -> {
                    //  textView.text = "Nice to meet you"
                }
            }
        }
    }

    fun changeBtnName () {
        // 语法糖，开启一个线程
        thread {
            val msg = Message()
            msg.what = updateBtnText;
            /**
             * 消息队列发消息
             */
            handler.sendMessage(msg)
        }

        // 可以使用 AsyncTask，不过已经废弃了。
    }


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  在应用中全屏显示内容
        enableEdgeToEdge();

        // 生命周期是一个管理工具，不要把太多逻辑写入，分散到不同的方法中去
        this.registerTimeChangeReceiver()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // 设置内容
        setContent {
            Theme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text("虚拟地址工具")
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding),
                    ) {

                        MainContent(
                            gotoSecond= {

//                                gotoSecondPage()
//                                requestPermissionThenReadContacts()
                                sendSystemNotice()
                            },
                            finish = {
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }



}


/**
 * 没啥好说的，基本上和前端 DOM 树差距不大。不需要用 xml 写了
 */
@Composable
fun MainContent(gotoSecond: () -> Unit, finish: () -> Unit) {

    var xVal by rememberSaveable { mutableStateOf("") }
    var yVal by rememberSaveable { mutableStateOf("") }

    TextField(value = xVal, onValChange = { xVal = it }, label = "经度")
    TextField(value = yVal, onValChange = { yVal = it }, label = "纬度")

    Log.i("22", "2423")

    Row (modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Button(
            onClick = {
                Log.v("2313", "1231")
                gotoSecond()
            }
        ) {
            Text("开始设置")
        }

        Button(
            onClick = {
                finish()
            }
        ) {
            Text("取消设置".testCancelText())
        }

    }
}

@Composable
fun TextField(value: String, onValChange: (String) -> Unit, label: String = "地址") {
    OutlinedTextField(
        value = value,
        onValueChange = onValChange,
        label = { Text(label) }
    )
}

/**
 * 类似 JavaScript 在原型链上进行修改，不建议使用
 * 在上面可以直接调用 "".testCancelText() 获取取消设置
 */
fun String.testCancelText (): String {
    return "取消设置"
}

/**
 * 此处为预览，可以直接运行，方便调试 Composable
 */
@Preview(showBackground = false)
@Composable
fun GreetingPreview() {
    Theme {
    }
}