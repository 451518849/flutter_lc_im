package cn.leancloud.chatkit.cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.leancloud.chatkit.utils.LCIMLogUtils;

/**
 * Created by wli on 16/2/25.
 * key value 形式的存储，只能存储 String，其他数据也要转化成 String 存储
 * 因为忽略了具体数据格式，所以更新具体属性的时候必须更新整条记录
 * <p/>
 * 因为最终读与写的操作都是在 readDbThread 线程中进行，所以不需要考虑线程安全问题
 */
class LCIMLocalStorage extends SQLiteOpenHelper {

  /**
   * db 的名字，加前缀避免与用户自己的逻辑冲突
   */
  private static final String DB_NAME_PREFIX = "LeanCloudChatKit_DB";

  /**
   * 具体 id 的 key，文本、主键、不能为空
   */
  private static final String TABLE_KEY_ID = "id";

  /**
   * 具体内容的 key，文本（非文本的可以通过转化成 json 存进来）
   */
  private static final String TABLE_KEY_CONTENT = "content";

  private static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS %s(" +
    TABLE_KEY_ID + " TEXT PRIMARY KEY NOT NULL, " +
    TABLE_KEY_CONTENT + " TEXT " +
    ")";
  private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS %s";

  private static final int DB_VERSION = 1;

  private String tableName;

  private HandlerThread readDbThread;
  private Handler readDbHandler;

  public LCIMLocalStorage(Context context, String clientId, String tableName) {
    super(context, DB_NAME_PREFIX, null, DB_VERSION);

    if (TextUtils.isEmpty(tableName)) {
      throw new IllegalArgumentException("tableName can not be null");
    }
    if (TextUtils.isEmpty(clientId)) {
      throw new IllegalArgumentException("clientId can not be null");
    }

    final String md5ClientId = AVUtils.md5(clientId);
    this.tableName = tableName + "_" + md5ClientId;

    createTable();

    readDbThread = new HandlerThread("LCIMLocalStorageReadThread");
    readDbThread.start();
    readDbHandler = new Handler(readDbThread.getLooper());
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(String.format(SQL_CREATE_TABLE, tableName));
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (!isIgnoreUpgrade()) {
      db.execSQL(String.format(SQL_DROP_TABLE, tableName));
      onCreate(db);
    }
  }

  /**
   * 因为 onCreate 为初始化 db 的时候才调用的，所以多表的情况下需要主动调用此函数来创建表
   */
  private void createTable() {
    getWritableDatabase().execSQL(String.format(SQL_CREATE_TABLE, tableName));
  }

  protected boolean isIgnoreUpgrade() {
    return true;
  }

  /**
   * 获取所有的 Key 值
   *
   * @param callback 获取后会执行此回调
   */
  public void getIds(final AVCallback<List<String>> callback) {
    if (null != callback) {
      readDbHandler.post(new Runnable() {
        @Override
        public void run() {
          callback.internalDone(getIdsSync(), null);
        }
      });
    }
  }

  /**
   * 根据 key 值获对应的 values
   * 注意：并不保证回调 data 与 id 顺序一致
   *
   * @param ids      需要的获取数据的 key
   * @param callback 获取后会执行此回调
   */
  public void getData(final List<String> ids, final AVCallback<List<String>> callback) {
    if (null != callback) {
      if (null != ids && ids.size() > 0) {
        readDbHandler.post(new Runnable() {
          @Override
          public void run() {
            callback.internalDone(getDataSync(ids), null);
          }
        });
      } else {
        callback.internalDone(null, null);
      }
    }
  }

  /**
   * 插入数据,注意 idList 与 valueList 是一一对应的
   *
   * @param idList
   * @param valueList
   */
  public void insertData(final List<String> idList, final List<String> valueList) {
    if (null != idList && null != valueList && idList.size() == valueList.size()) {
      readDbHandler.post(new Runnable() {
        @Override
        public void run() {
          insertSync(idList, valueList);
        }
      });
    }
  }

  /**
   * 插入数据,注意 id 与 value 是对应的
   *
   * @param id
   * @param value
   */
  public void insertData(String id, String value) {
    if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(value)) {
      insertData(Arrays.asList(id), Arrays.asList(value));
    }
  }

  /**
   * 删除数据
   *
   * @param ids
   */
  public void deleteData(final List<String> ids) {
    if (null != ids && !ids.isEmpty()) {
      readDbHandler.post(new Runnable() {
        @Override
        public void run() {
          deleteSync(ids);
        }
      });
    }
  }

  public void deleteAllData() {
    readDbHandler.post(new Runnable() {
      @Override
      public void run() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(tableName, null, null);
      }
    });
  }

  /**
   * 获取 key 值，此为同步方法
   */
  private List<String> getIdsSync() {
    String queryString = "SELECT " + TABLE_KEY_ID + " FROM " + tableName;
    SQLiteDatabase database = getReadableDatabase();
    Cursor cursor = database.rawQuery(queryString, null);
    List<String> dataList = new ArrayList<>();
    while (cursor.moveToNext()) {
      dataList.add(cursor.getString(cursor.getColumnIndex(TABLE_KEY_ID)));
    }
    cursor.close();
    return dataList;
  }

  /**
   * 获取数据，此为同步方法
   * 注意：并不保证回调 data 与 id 顺序一致
   */
  private List<String> getDataSync(List<String> ids) {
    String queryString = "SELECT * FROM " + tableName;
    if (null != ids && !ids.isEmpty()) {
      queryString += (" WHERE " + TABLE_KEY_ID + " in ('" + AVUtils.joinCollection(ids, "','") + "')");
    }

    SQLiteDatabase database = getReadableDatabase();
    Cursor cursor = database.rawQuery(queryString, null);
    List<String> dataList = new ArrayList<>();
    while (cursor.moveToNext()) {
      dataList.add(cursor.getString(cursor.getColumnIndex(TABLE_KEY_CONTENT)));
    }
    cursor.close();
    return dataList;
  }

  /**
   * 插入数据，此为同步方法
   */
  private void insertSync(List<String> idList, List<String> valueList) {
    if(idList.size() != valueList.size()) {
        LCIMLogUtils.i("idList.size is not equal to valueList.size");
    }
    SQLiteDatabase db = getWritableDatabase();
    db.beginTransaction();
    for (int i = 0; i < valueList.size(); i++) {
      ContentValues values = new ContentValues();
      values.put(TABLE_KEY_ID, idList.get(i));
      values.put(TABLE_KEY_CONTENT, valueList.get(i));
      db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
    db.setTransactionSuccessful();
    db.endTransaction();
  }

  /**
   * 输出数据，此为同步方法
   */
  private void deleteSync(List<String> ids) {
    if (null != ids && !ids.isEmpty()) {
      String queryString = joinListWithApostrophe(ids);
      getWritableDatabase().delete(tableName, TABLE_KEY_ID + " in (" + queryString + ")", null);
    }
  }

  private static String joinListWithApostrophe(List<String> strList) {
    String queryString = TextUtils.join("','", strList);
    if (!TextUtils.isEmpty(queryString)) {
      queryString = "'" + queryString + "'";
    }
    return queryString;
  }
}
