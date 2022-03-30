package com.dornica.sms.dornica_sms_plugin;

import android.app.Activity;
import android.provider.Telephony;
import java.lang.ref.WeakReference;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.PluginRegistry;

import android.telephony.SmsMessage;
import android.os.*;
import java.util.*;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.Manifest;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;

import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.embedding.android.FlutterActivity;

/** DornicaSmsPlugin */
public class DornicaSmsPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.RequestPermissionsResultListener {
  private MethodChannel channel;
  private static final String CHANNEL = "DORNICA_SMS_PLUGIN";
  private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
  static String phone = "";
  static String message = "";
  private Activity activity;
  private int permissionStatus = 1;
  private boolean permissionGranted = false;
  private Result result;
  private MethodCall call;
  private MessageBroadcastReceive broadcastReceiver;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), CHANNEL);
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    this.result = result;
    this.call = call;
    if (call.method.equals("registerReceiver")) {
      checkPermission(activity);
      if (!permissionGranted) {
        ActivityCompat.requestPermissions(activity,
          new String[]{Manifest.permission.RECEIVE_SMS},
          permissionStatus
        );
      } else {
        broadcastReceiver = new MessageBroadcastReceive (new WeakReference<>(DornicaSmsPlugin.this));
        broadcastReceiver.bindListener(listener);
        IntentFilter intentfilter = new IntentFilter(SMS_RECEIVED);
        activity.registerReceiver(broadcastReceiver, intentfilter);
      }
    } else if (call.method.equals("unregisterReceiver")){
      try {
        activity.unregisterReceiver(broadcastReceiver);
      } catch (Exception e) {}
    } else {
      result.notImplemented();
    }
  }

  private MessageListener listener = new MessageListener() {
    @Override
    public void messageReceived(String messages) {
      try {
        ArrayList<String> addresses;
        addresses = call.argument("addresses");
        if (addresses.size() == 0) {
          result.success(messages);
        } else {
          boolean output = addresses.contains(phone);
          if (output == true) {
            result.success(messages);
          }
        }
      } catch (Exception e) {
        System.out.println(e.toString());
      }
    }
  };

  private void checkPermission(Activity context) {
    permissionGranted = ContextCompat.checkSelfPermission(
      context, Manifest.permission.RECEIVE_SMS
    ) == PackageManager.PERMISSION_GRANTED;
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
    binding.addRequestPermissionsResultListener(this);

  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
    binding.addRequestPermissionsResultListener(this);
  }

  @Override
  public void onDetachedFromActivity() {

  }

  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      broadcastReceiver = new MessageBroadcastReceive (new WeakReference<>(DornicaSmsPlugin.this));
      broadcastReceiver.bindListener(listener);
      activity.registerReceiver(broadcastReceiver, new IntentFilter(SMS_RECEIVED));
      return true;
    } else {
      return false;
    }
  }

  public static class MessageBroadcastReceive extends BroadcastReceiver {
    private static MessageListener messageListener;
    final WeakReference<DornicaSmsPlugin> plugin;

    private MessageBroadcastReceive (WeakReference<DornicaSmsPlugin> plugin) {
      this.plugin = plugin;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      if (SMS_RECEIVED.equals(intent.getAction())) {
        if (plugin.get() == null) {
          return;
        } else {
          plugin.get().activity.unregisterReceiver(this);
        }
        final Bundle bundle = intent.getExtras();

        if (bundle != null) {
          final Object[] pdusObject = (Object[]) bundle.get("pdus");
          String[] messages = new String[pdusObject.length];
          
          for (int i = 0; i < pdusObject.length; i++) {
              SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObject[i]);

              messages[i] = currentMessage.getDisplayMessageBody();

              phone = currentMessage.getDisplayOriginatingAddress();
          }

          message = Arrays.toString(messages);

          messageListener.messageReceived(message);
        }
      }
    }

    public void bindListener(MessageListener listener) {
      messageListener = listener;
    }
  }
}
