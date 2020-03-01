import 'dart:convert';

import 'package:flutter_lc_im_example/model/user.dart';

class ImMessageType {
  static const int text = 1;
  static const int image = 2;
}

class ImMessageIOType {
  static const int messageIOTypeIn = 1;
  static const int messageIOTypeOut = 2;
}

class ImMessages {
  final List<ImMessage> messages;
  ImMessages({this.messages});

  factory ImMessages.fromJson(List<dynamic> parsedJson) {
    List<ImMessage> messages = List<ImMessage>();
    messages = parsedJson.map((i) => ImMessage.fromJson(i)).toList();
    return ImMessages(messages: messages);
  }
}

class ImMessage {
  final String conversationId;
  final String messageId;
  final ImUser fromUser;
  final ImUser toUser;
  final String text;
  final int timestamp;
  final int messageType;
  final int ioType;
  final Map<String, dynamic>
      attributes; // 如果最后一条消息是当前用户，则attributes中包含用户的姓名，否则为空
  ImMessage(
      {this.messageId,
      this.conversationId,
      this.fromUser,
      this.toUser,
      this.text,
      this.ioType,
      this.timestamp,
      this.attributes,
      this.messageType});

  //用于获取消息记录
  factory ImMessage.fromJson(Map<dynamic, dynamic> jsonMap) {
    Map contentMap = json.decode(jsonMap['content']);

    return ImMessage(
        conversationId: jsonMap['conversationId'],
        messageId: jsonMap['messageId'],
        ioType: jsonMap['ioType'],
        messageType: contentMap['_lctype'],
        text: contentMap['_lctext'] ?? '[暂不支持图片]',
        timestamp: jsonMap['timestamp']);
  }

  // 用于convetsation中读取lastMessage
  factory ImMessage.fromString(String value) {
    Map valueMap = json.decode(value);

    return ImMessage(
      conversationId: valueMap['conversationId'],
      messageId: valueMap['messageId'],
      messageType: valueMap['_lctype'],
      text: valueMap['_lctext'] ?? '[暂不支持图片]',
      attributes: valueMap['_lcattrs'],
    );
  }
}

//{unreadMessagesCount: 0, clientId: 1050, lastMessageAt: 2020-01-15 10:20:10, conversationId: 5e09f44896cd6fcb669c5861, lastMessage: {"_lctype":-1,"_lctext":"好的"}}
