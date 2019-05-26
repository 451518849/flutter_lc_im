package cn.leancloud.chatkit.cache;


import android.os.AsyncTask;
import android.text.TextUtils;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by wli on 15/9/29.
 * 用于下载文件，会主动合并重复的下载
 */
public class LCIMLocalCacheUtils {

  /**
   * 用于记录 DownLoadCallback，如果对于同一个 url 有多个请求，则下载完后应该执行所有回调
   * 此变量就是用于记录这些请求
   */
  private static Map<String, ArrayList<DownLoadCallback>> downloadCallBackMap;

  /**
   * 判断当前 url 是否正在下载，如果已经在下载，则没有必要再去做请求
   */
  private static Set<String> isDownloadingFile;

  /**
   * OkHttpClient 的 实例，官方不建议创建多个，所以这里搞了一个 static 实例
   */
  private static OkHttpClient okHttpClient;

  static {
    downloadCallBackMap = new HashMap<String, ArrayList<DownLoadCallback>>();
    isDownloadingFile = new HashSet<String>();
    okHttpClient = new OkHttpClient();
  }

  private static synchronized void addDownloadCallback(String path, DownLoadCallback callback) {
    if (null != callback) {
      if (downloadCallBackMap.containsKey(path)) {
        downloadCallBackMap.get(path).add(callback);
      } else {
        downloadCallBackMap.put(path, new ArrayList<DownLoadCallback>(Arrays.asList(callback)));
      }
    }
  }

  private static synchronized void executeDownloadCallBack(String path, Exception e) {
    if (downloadCallBackMap.containsKey(path)) {
      ArrayList<DownLoadCallback> callbacks = downloadCallBackMap.get(path);
      downloadCallBackMap.remove(path);
      for (DownLoadCallback callback : callbacks) {
        callback.done(e);
      }
    }
  }

  /**
   * 异步下载文件到指定位置
   *
   * @param url       需要下载远程地址
   * @param localPath 下载到本地的文件存放的位置
   */
  public static void downloadFileAsync(final String url, final String localPath) {
    downloadFileAsync(url, localPath, false);
  }

  /**
   * 异步下载文件到指定位置
   *
   * @param url       需要下载远程地址
   * @param localPath 下载到本地的文件存放的位置
   * @param overlay   是否覆盖原文件
   */
  public static void downloadFileAsync(final String url, final String localPath, boolean overlay) {
    downloadFile(url, localPath, overlay, null);
  }

  /**
   * 异步下载文件到指定位置
   *
   * @param url       需要下载远程地址
   * @param localPath 下载到本地的文件存放的位置
   * @param overlay   是否覆盖原文件
   * @param callback  下载完后的回调
   */
  public static void downloadFile(final String url, final String localPath,
                                  boolean overlay, final DownLoadCallback callback) {
    if (TextUtils.isEmpty(url) || TextUtils.isEmpty(localPath)) {
      throw new IllegalArgumentException("url or localPath can not be null");
    } else if (!overlay && isFileExist(localPath)) {
      if (null != callback) {
        callback.done(null);
      }
    } else {
      addDownloadCallback(url, callback);
      if (!isDownloadingFile.contains(url)) {
        new AsyncTask<Void, Void, Exception>() {
          @Override
          protected Exception doInBackground(Void... params) {
            return downloadWithOKHttp(url, localPath);
          }

          @Override
          protected void onPostExecute(Exception e) {
            executeDownloadCallBack(url, e);
            isDownloadingFile.remove(url);
          }
        }.execute();
      }
    }
  }

  private static Exception downloadWithOKHttp(String url, String localPath) {
    File file = new File(localPath);
    Exception result = null;
    Call call = okHttpClient.newCall(new Request.Builder().url(url).get().build());
    FileOutputStream outputStream = null;
    InputStream inputStream = null;
    try {
      Response response = call.execute();
      if (response.code() == 200) {
        outputStream = new FileOutputStream(file);
        inputStream = response.body().byteStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, len);
        }
      } else {
        result = new Exception("response code is " + response.code());
      }
    } catch (IOException e) {
      result = e;
      if (file.exists()) {
        file.delete();
      }
    } finally {
      closeQuietly(inputStream);
      closeQuietly(outputStream);
    }
    return result;
  }

  private static void closeQuietly(Closeable closeable) {
    try {
      closeable.close();
    } catch (Exception e) {
    }
  }

  private static boolean isFileExist(String localPath) {
    File file = new File(localPath);
    return file.exists();
  }


  public static class DownLoadCallback {
    public void done(Exception e) {
    }
  }
}
