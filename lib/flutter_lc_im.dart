import 'dart:async';

import 'package:flutter/services.dart';

class FlutterLcIm {
  static const MethodChannel _channel = const MethodChannel('flutter_lc_im');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<dynamic> register(
      String appId, String appKey) async {
    var result = await _channel.invokeMethod('register', {
      'app_id': appId,
      'app_key': appKey,
    });
    return result;
  }

  static Future<dynamic> login(String userId) async {
    var result = await _channel.invokeMethod('login', {
      'user_id': userId,
    });
    return result;
  }

  static Future<dynamic> pushToConversationView(Map user, Map peer) async {
    var result = await _channel.invokeMethod('pushToConversationView', {
      'user': user,
      'peer': peer,
    });
    return result;
  }

  static Future<dynamic> getConversationList() async {
    var result = await _channel.invokeMethod('getConversationList');
    return result;
  }
}
