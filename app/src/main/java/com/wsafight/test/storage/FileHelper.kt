package com.wsafight.test.storage

import android.app.Activity
import android.content.Context
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 */
object FileHelper {

    private fun save (
        context: Activity,
        // 文件名
        fileName: String,
        // 文件内容
        inputText: String,
        // 格式，可以替换和追加
        mode: Int = Context.MODE_APPEND
    ) {
        val output = context.openFileOutput(fileName, mode)
        // 使用 stream 处理输入
        val writer = BufferedWriter(OutputStreamWriter(output))
        // 使用 use 就可以不关闭 writer，自动关闭
        writer.use {
            it.write(inputText)
        }
    }

    private fun load (context: Activity, fileName: String): String {
        val content = StringBuilder()
        try {
            // 防止没有建立过文件
            val input = context.openFileInput(fileName)
            val reader = BufferedReader(InputStreamReader(input))
            reader.use {
                reader.forEachLine {
                    content.append(it)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return content.toString()
    }

}