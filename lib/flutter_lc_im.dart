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
      String appId, String appKey, String api,bool debug) async {
    var result = await _channel.invokeMethod('register', {
      'app_id': appId,
      'app_key': appKey,
      'api': api,
      'debug': debug
    });
    return result;
  }

/// 登陆聊天
/// @param clientId 为当前用户的uid
  static Future<dynamic> login(String clientId) async {
    var result = await _channel.invokeMethod('login', {
      'client_id': clientId,
    });
    return result;
  }

  /// 创建一个单聊会话
  /// 创建策略：根据client_id和peer_id判断服务器上是存在会话记录，如果存在则返回以前的会话，如果不存则创建新的会话。
  /// @param client_id 当前活用的uid
  /// @param peer_id 聊天对象的uid
  static Future<dynamic> createConversation(
      String clientId, String peerId) async {
    var result = await _channel.invokeMethod('createConversation', {
      'client_id': clientId,
      'peer_id': peerId,
    });
    return result;
  }

  static Future<dynamic> sendMessage(
      String text, Uint8List file, int messageType) async {
    var result = await _channel.invokeMethod('sendMessage',
        {'text': text, "file": file, "messageType": messageType});
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
