import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

class FlutterLcIm {
  static const MethodChannel _channel = const MethodChannel('flutter_lc_im');

  /// leancloud 注册
  /// @param appdId 由leancloud账号得
  /// @param appKey 由leancloud账号得
  /// @param api 由leancloud账号得，api为leancloud上绑定的域名
  /// @param debug 是否开启leancloud sdk的debug模式
  static Future<dynamic> register(
      String appId, String appKey, String api, bool debug) async {
    var result = await _channel.invokeMethod('register',
        {'app_id': appId, 'app_key': appKey, 'api': api, 'debug': debug});
    return result;
  }

  /// 登陆聊天
  /// @param clientId 为当前用户的uid
  /// @param notification 是否显示在通知栏，只对android设备有用
  static Future<dynamic> login(String clientId,{bool notification = false}) async {
    var result = await _channel.invokeMethod('login', {
      'client_id': clientId,
      'notification':notification
    });
    return result;
  }

  /// 退出登陆
  static Future<dynamic> logout() async {
    var result = await _channel.invokeMethod('logout');
    return result;
  }

  /// 创建一个单聊会话
  /// 创建策略：根据client_id和peer_id判断服务器上是存在会话记录，如果存在则返回以前的会话，如果不存则创建新的会话。
  /// @param peer_id 聊天对象的uid
  /// @param limit 加载初始化聊天信息的条数，默认10条
  /// @param attributes 对话的额外属性
  static Future<dynamic> createConversation(String peerId,
      {int limit = 10, Map attributes}) async {
    var result = await _channel.invokeMethod('createConversation', {
      'peer_id': peerId,
      'limit': limit,
      'attributes': attributes,
    });
    return result;
  }

  /// 发送文字信息
  /// @param text 文字
  /// @param atrributes 自定义附带信息（可选）
  static Future<dynamic> sendTextMessage(String text,
      {Map<String, dynamic> atrributes}) async {
    var result = await _channel.invokeMethod('sendTextMessage', {
      'text': text,
      'attributes': atrributes,
    });
    return result;
  }

  /// 发送图片信息
  /// @param path 图片路径
  /// @param atrributes 自定义附带信息（可选）
  static Future<dynamic> sendImageMessage(String path,
      {Map<String, dynamic> atrributes}) async {
    var result = await _channel.invokeMethod('sendImageMessage', {
      'path': path,
      'attributes': atrributes,
    });
    return result;
  }

  /// 发送语音信息
  /// @param path 音频路径
  /// @param atrributes 自定义附带信息（可选）
  static Future<dynamic> sendVoiceMessage(String path, String duration,
      {Map<String, dynamic> atrributes}) async {
    var result = await _channel.invokeMethod('sendVoiceMessage', {
      'path': path,
      'duration': duration,
      'attributes': atrributes,
    });
    return result;
  }

  /// 发送视频信息
  /// @param path 视频路径
  /// @param atrributes 自定义附带信息（可选）
  static Future<dynamic> sendVideoMessage(String path,
      {String duration = "0", Map<String, dynamic> atrributes}) async {
    var result = await _channel.invokeMethod('sendVideoMessage', {
      'path': path,
      'duration': duration,
      'attributes': atrributes,
    });
    return result;
  }

  /// 查询会话记录
  /// @param limit 每次返回的条数
  /// @param offset 偏移位置，从当前位置返回后面的数据
  static Future<dynamic> queryHistoryConversations(
      int limit, int offset) async {
    var result = await _channel.invokeMethod('queryHistoryConversations', {
      'limit': limit,
      'offset': offset,
    });
    return result;
  }

  /// 查询会话中的聊天记录
  /// 查询策略：第一次查询是在创建会话的时候自动查询，第二次以后的查询是根据当前第一条消息的message_id和timestamp来查询
  /// @param limit 每次返回的条数
  /// @param message_id 消息的id，为null表示查询最新的消息
  /// @param timestamp 发送消息的时间
  static Future<dynamic> queryHistoryConversationMessages(
      int limit, String messageId, int timestamp) async {
    var result =
        await _channel.invokeMethod('queryHistoryConversationMessages', {
      'limit': limit,
      'message_id': messageId,
      'timestamp': timestamp,
    });
    return result;
  }
}
