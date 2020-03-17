import 'package:permission_handler/permission_handler.dart';

Future<bool> checkConversationPermission() async{
  Map<PermissionGroup, PermissionStatus> permissions = await PermissionHandler().requestPermissions([PermissionGroup.microphone]);
  if(permissions[PermissionGroup.microphone] == PermissionStatus.granted){
    return true;
  }
  return false;
}