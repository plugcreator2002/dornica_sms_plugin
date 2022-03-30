import 'dart:async';
import 'package:flutter/services.dart';

class DornicaSmsPlugin {
  static const MethodChannel _channel = MethodChannel('DORNICA_SMS_PLUGIN');

  static Future<String?> registerReceiver(List<String> addresses) async {
    late String output;
    final result = await _channel.invokeMethod<String>(
      'registerReceiver',
      <String, List<String>>{
        "addresses": addresses,
        // "+13335556789",
        // "+14445556789",
      },
    );

    if (result == null) return "";

    output = result.substring(1, result.length - 1);

    return output;
  }

  static Future<void> unregisterReceiver() async {
    await _channel.invokeMethod('unregisterReceiver');
  }
}
