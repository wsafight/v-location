package com.wsafight.test

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
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
import com.wsafight.test.constants.SecondPage
import com.wsafight.test.ui.theme.Theme
import com.wsafight.test.utils.BaseActivity
import com.wsafight.test.utils.PermissionHelper


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


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  在应用中全屏显示内容
        enableEdgeToEdge();

        // 生命周期是一个管理工具，不要把太多逻辑写入，分散到不同的方法中去
        this.registerTimeChangeReceiver()

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
                                requestPermissionThenReadContacts()
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
        startActivityForResult(intent, 1)
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
            1 -> if (resultCode == RESULT_OK) {
                val returnedData = data?.getStringExtra("data_return")
                Toast.makeText(this, "123123", Toast.LENGTH_SHORT).show()
                Log.d("FirstActivity", "returned data is $returnedData")
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