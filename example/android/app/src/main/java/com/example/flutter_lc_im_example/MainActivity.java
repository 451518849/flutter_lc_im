package com.example.flutter_lc_im_example;

import android.os.Bundle;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.SaveCallback;

import io.flutter.app.FlutterActivity;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GeneratedPluginRegistrant.registerWith(this);

//    AVInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
//      public void done(AVException e) {
//        if (e == null) {
//          // 保存成功
//          String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
//          // 关联  installationId 到用户表等操作……
//        } else {
//          // 保存失败，输出错误信息
//        }
//      }
//    });
  }
}
