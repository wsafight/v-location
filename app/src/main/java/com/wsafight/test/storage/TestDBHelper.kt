package com.wsafight.test.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TestDBHelper(
    val context: Context, name: String, version: Int
) :
    SQLiteOpenHelper(context, name, null, version) {

    private val createTable1 = "create table table1 (" +
        " id integer primary key autoincrement," +
        "name text)"

    private val createTable2 = "create table table2 (" +
            " id integer primary key autoincrement," +
            "name text)"


    /**
     * 当前已经升级几个版本了，如版本 6。但是用户从未下载过应用，直接执行
     */
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createTable1)
        db.execSQL(createTable2)
    }

    /**
     * 如果用户很久没有打开过程序，从版本 1 升级到 版本 6。则需要一步步升级
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion <= 1) {
            // doSomeThing
        }
        if (oldVersion <= 2) {
            // doSomeThing
        }
    }
}