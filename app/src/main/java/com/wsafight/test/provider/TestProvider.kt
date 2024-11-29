package com.wsafight.test.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import com.wsafight.test.constants.TestProviderAuthority
import com.wsafight.test.storage.TestDBHelper

/**
 * provider 要在 AndroidManifest.xml 注册才可以使用
 * <provider
 *  android:name=".DatabaseProvider"
 *  android:authorities="com.wsafight.test.provider"
 *  android:enabled="true"
 *  android:exported="true">
 *  </provider>
 *
 *  注册后，在其他应用可以访问修改当前应用的数据
 *
 *  val uri = Uri.parse("content://com.wsafight.test.provider/table1")
 *  contentResolver.query(uri, null, null, null, null)?.apply {
 *      while (moveToNext()) {
 *          val name = getString(getColumnIndex("name"))
 *          Log.d("MainActivity", "name is $name")
 *      }
 *      close()
 *  }
 */
class TestProvider: ContentProvider() {

    // 一个能够匹配任意表的内容URI格式就可以写成：
    // content://com.wsafight.test.provider/table1
    // 一个能够匹配table1表中任意一行数据的内容URI格式就可以写成：
    // content://com.wsafight.test.provider/table1/#
    private val table1Dir = 0
    private val table1Item = 1
    private val table2Dir = 2
    private val table2Item = 3
    private var dbHelper: SQLiteOpenHelper? = null

    /**
     * by lazy 代码块是 Kotlin 提供的一种懒加载技术，代码块中的代码一开始并不会执行
     * 只有当 uriMatcher 变量首次被调用的时候才会执行
     * 会将代码块中最后一行代码的返回值赋给 uriMatcher
     */
    private val uriMatcher by lazy {
        val matcher = UriMatcher(UriMatcher.NO_MATCH)
        matcher.addURI(TestProviderAuthority, "table1", table1Dir)
        matcher.addURI(TestProviderAuthority, "table1/#", table1Item)
        matcher.addURI(TestProviderAuthority, "table2", table2Dir)
        matcher.addURI(TestProviderAuthority, "table2/#", table2Item)
        matcher
    }

    /**
     * onCreate()。初始化ContentProvider的时候调用。通常会在这里完成对数据库的创建和
     * 升级等操作，返回true表示ContentProvider初始化成功，返回false则表示失败。
     */
    override fun onCreate(): Boolean = context?.let {
        dbHelper = TestDBHelper(it, "Test.db",  1)
        true
        /**
         * ?: 相当于 JavaScript 的 ??
         */
    } ?: false

    /**
     * 从ContentProvider中查询数据。uri参数用于确定查询哪张表，projection
     * 参数用于确定查询哪些列，selection和selectionArgs参数用于约束查询哪些行，
     * sortOrder参数用于对结果进行排序，查询的结果存放在Cursor对象中返回。
     *
     * 使用 = when 直接获取对应数据，同样也有 = if
     */
    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? =
        // 如果前面的不存在直接返回 null
        dbHelper?.let {
            // it 指代当前的 this
            // 优先使用 val，需要变化时候再改为 var
            val db = it.readableDatabase;
            val cursor =
                when(uriMatcher.match(uri)) {
                    table1Dir -> {
                        db.query("table1", projection, selection, selectionArgs,
                            null, null, sortOrder)
                    }
                    table1Item -> {
                        val id = uri.pathSegments[1]
                        db.query("table1", projection, "id = ?", arrayOf(id), null, null,
                            sortOrder)
                    }
                    table2Dir -> {
                        db.query("table2", projection, selection, selectionArgs,
                            null, null, sortOrder)
                    }
                    table2Item -> {
                        val id = uri.pathSegments[1]
                        db.query("table2", projection, "id = ?", arrayOf(id), null, null,
                            sortOrder)
                    }
                    else -> {
                        null
                    }

                }
            cursor
    }


    /**
     * insert()。向ContentProvider中添加一条数据。uri参数用于确定要添加到的表，待添
     * 加的数据保存在values参数中。添加完成后，返回一个用于表示这条新记录的URI。
     */
    override fun insert(uri: Uri, values: ContentValues?): Uri? = dbHelper?.let {
        val db = it.writableDatabase;
        val uriReturn = when (uriMatcher.match(uri)) {
            table1Dir, table1Item -> {
                val id = db.insert("table1", null, values)
                // ${} 和 $ 均可以
                Uri.parse("content://${TestProviderAuthority}/table1/${id}")
            }
            table2Dir, table2Item -> {
                val id = db.insert("table2", null, values)
                Uri.parse("content://$TestProviderAuthority/table2/$id")
            }
            else -> null
        }
        uriReturn
    }

    /**
     * update()。更新ContentProvider中已有的数据。uri参数用于确定更新哪一张表中的数
     * 据，新数据保存在values参数中，selection和selectionArgs参数用于约束更新哪些行，
     * 受影响的行数将作为返回值返回。
     */
    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int = dbHelper?.let {
        val db = it.writableDatabase
        val updatedRows = when (uriMatcher.match(uri)) {
            table1Dir -> db.update("table1", values, selection, selectionArgs)
            table1Item -> {
                val id = uri.pathSegments[1]
                db.update("table1", values, "id = ?", arrayOf(id))
            }
            table2Dir -> db.update("table2", values, selection, selectionArgs)
            table2Item -> {
                val id = uri.pathSegments[1]
                db.update("table2", values, "id = ?", arrayOf(id))
            }
            else -> 0
        }
        updatedRows
    } ?: 0

    /**
     * delete()。从ContentProvider中删除数据。uri参数用于确定删除哪一张表中的数据，
     * selection和selectionArgs参数用于约束删除哪些行，被删除的行数将作为返回值返回。
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = dbHelper?.let {
        var db = it.writableDatabase
        val deletedRows = when (uriMatcher.match(uri)) {
            table1Dir -> db.delete("table1", selection, selectionArgs)
            table1Item -> {
                val id = uri.pathSegments[1]
                db.delete("table1", "id = ?", arrayOf(id))
            }
            table2Dir -> db.delete("table2", selection, selectionArgs)
            table2Item -> {
                val id = uri.pathSegments[1]
                db.delete("table2", "id = ?", arrayOf(id))
            }
            else -> 0
        }
        deletedRows
    } ?: 0

    /**
     * 根据传入的内容URI返回相应的MIME类型
     * 一个内容 URI 所对应的MIME字符串主要由 3 部分组成
     * 必须以vnd开头
     * 如果内容URI以路径结尾，则后接android.cursor.dir/；如果内容URI以id结尾，则后接 android.cursor.item/
     * 最后接上vnd.<authority>.<path>
     *
     * 使用 = when 直接获取对应数据，同样也有 = if
     */
    override fun getType(uri: Uri): String? = when (uriMatcher.match(uri)) {
        table1Dir -> "vnd.android.cursor.dir/vnd.com.example.app.provider.table1"
        table1Item -> "vnd.android.cursor.item/vnd.com.example.app.provider.table1"
        table2Dir -> "vnd.android.cursor.dir/vnd.com.example.app.provider.table2"
        table2Item -> "vnd.android.cursor.item/vnd.com.example.app.provider.table2"
        else -> null
    }
}