class ImUser {
  String uid;
  String username;
  String avatarUrl;

  ImUser({this.uid, this.username, this.avatarUrl = 'http://thirdqq.qlogo.cn/g?b=oidb&k=h22EA0NsicnjEqG4OEcqKyg&s=100'});

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    if (this.uid != null) {
      data['uid'] = this.uid;
    }
    if (this.username != null) {
      data['username'] = this.username;
    }
    if (this.avatarUrl != null) {
      data['avatarUrl'] = this.avatarUrl;
    }
    return data;
  }
}
