package com.wsafight.test

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
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
import com.wsafight.test.ui.theme.Theme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  在应用中全屏显示内容
//        enableEdgeToEdge();
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
                                val intent = Intent("com.example.activitytest.ACTION_START")
                                intent.putExtra("extra_data", "123");
                                startActivityForResult(intent, 1)
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


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    /**
     * 参数1: 请求码
     * 参数2：返回代码
     * 参数3: 数据
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

}


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
            Text("取消设置")
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


@Preview(showBackground = false)
@Composable
fun GreetingPreview() {
    Theme {
    }
}