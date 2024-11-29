package com.wsafight.test

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import com.wsafight.test.constants.SecondPage
import com.wsafight.test.ui.theme.Theme
import com.wsafight.test.utils.BaseActivity


/**
 * 要在 activity 中注册，否则不可用
 * <activity
 *     android:name=".SecondActivity"
 *     android:exported="true"
 *     android:theme="@style/Theme.Test" >
 *        <intent-filter>
 *            <!-- 当前的动作  -->
 *            <action android:name="com.example.activitytest.ACTION_START" />
 *            <category android:name="android.intent.category.DEFAULT" />
 *        </intent-filter>
 *  </activity>
 */
class SecondActivity : BaseActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extraData = intent.getStringExtra("extra_data") ?: "test"
        Log.d("SecondActivity", "extra data is $extraData")
        //  在应用中全屏显示内容
        enableEdgeToEdge();
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
                        Text(text = extraData)
                        Button(
                            onClick = {
                                val intent = Intent()
                                intent.putExtra("data_return", "Hello FirstActivity")
                                setResult(RESULT_OK, intent)
                                finish()
                            }
                        ) {
                            Text("返回")
                        }
                        Button(
                            onClick = {
                                finishAll();
                            }
                        ) {
                            Text("关闭")
                        }
                    }
                }
            }
        }
    }


    /**
     * kotlin 中没有 static 方法，直接使用伴侣类，是单例的
     *
     *
     */
    companion object {
        /**
         * 在这里写的话，方便外部知晓当前页面需要的数据，其他页面跳转调用这个即可
         */
        fun gotoCurrent(context: Activity, extraData: String) {
            val intent = Intent(SecondPage)
            //加入数据
            intent.putExtra("extra_data", extraData);
            context.startActivity(intent)
        }
    }

}
