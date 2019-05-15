import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_lc_im/flutter_lc_im.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_lc_im');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await FlutterLcIm.platformVersion, '42');
  });
}
