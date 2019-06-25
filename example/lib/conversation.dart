
class Conversations {
  final List<Conversation> conversations;
  Conversations({this.conversations});

  factory Conversations.fromJson(List<dynamic> parsedJson) {
    List<Conversation> conversations = List<Conversation>();
    conversations = parsedJson.map((i) => Conversation.fromJson(i)).toList();
    return Conversations(conversations: conversations);
  }
}

class Conversation {
  final String clientId;
  final String peerId;
  final int unreadMessagesCount;
  final String lastMessageAt;
  final String peerName;
  String avatarUrl; // 需要到自己的服务器上获取，然后重新赋值
  final String lastMessageContent;

  Conversation({
    this.clientId,
    this.peerId,
    this.unreadMessagesCount,
    this.lastMessageAt,
    this.lastMessageContent,
    this.peerName});

  factory Conversation.fromJson(Map<dynamic, dynamic> json) {
    return Conversation(
      clientId: json['clientId'],
      peerId: json['peerId'],
      peerName: json['peerName'],
      unreadMessagesCount: json['unreadMessagesCount'],
      lastMessageAt: json['lastMessageAt'],
      lastMessageContent: json['lastMessageContent'],
    );
  }
}