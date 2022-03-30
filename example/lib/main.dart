// ignore_for_file: deprecated_member_use
import 'package:flutter/material.dart';
import 'dart:async';
import 'package:flutter/services.dart';
import 'package:dornica_sms_plugin/dornica_sms_plugin.dart';

void main() => runApp(const MyApp());

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String message = 'Unknown';

  Future<void> register() async {
    String message = 'Unknown';
    try {
      message = await DornicaSmsPlugin.registerReceiver([]) ?? 'Unknown';
    } on PlatformException {
      message = 'Failed to get platform version.';
    }

    if (!mounted) return;

    setState(() {
      this.message = message;
    });
  }

  @override
  void dispose() {
    super.dispose();
    DornicaSmsPlugin.unregisterReceiver();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: Center(
          child: Ink(
            child: InkWell(
              child: Text('Received Message: $message\n'),
              onTap: () => register(),
            ),
          ),
        ),
      ),
    );
  }
}
