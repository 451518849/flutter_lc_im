import 'package:flutter_lc_im_example/model/message.dart';
import 'package:flutter_lc_im_example/utils/date.dart';

class ImConversations {
  final List<ImConversation> conversations;
  ImConversations({this.conversations});

  factory ImConversations.fromJson(List<dynamic> parsedJson) {
    List<ImConversation> conversations = List<ImConversation>();
    conversations = parsedJson.map((i) => ImConversation.fromJson(i)).toList();
    return ImConversations(conversations: conversations);
  }
}

class ImConversation {
  final String conversationId;
  final String clientId;
  final String username;
  final String peerId;
  int unreadMessagesCount;
  final String lastMessageAt;
  String peerName;
  String peerAvatarUrl; // 这个是对方的头像,需要到自己的服务器上获取，然后重新赋值
  ImMessage lastMessage;

  ImConversation(
      {this.conversationId,
      this.clientId,
      this.peerId,
      this.username,
      this.unreadMessagesCount,
      this.lastMessageAt,
      this.peerAvatarUrl,
      this.lastMessage,
      this.peerName});

  factory ImConversation.fromJson(Map<dynamic, dynamic> json) {
    List<dynamic> memebrs = json['members'];
    String peerId = "";
    if (memebrs != null && memebrs.length == 2) {
      if (memebrs[0] == json['clientId']) {
        peerId = memebrs[1];
      } else {
        peerId = memebrs[0];
      }
    }
    return ImConversation(
      conversationId: json['conversationId'],
      clientId: json['clientId'],
      username: json['username'],
      peerId: json['peerId'] ?? peerId,
      peerName: json['peerName'] ?? '测试',
      peerAvatarUrl: json['peerAvatarUrl'] ??
          'http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100',
      unreadMessagesCount: json['unreadMessagesCount'],
      lastMessageAt: tranImTime(json['lastMessageAt']),
      lastMessage: ImMessage.fromString(json['lastMessage']),
    );
  }
}
