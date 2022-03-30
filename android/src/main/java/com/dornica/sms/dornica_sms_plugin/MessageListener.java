package com.dornica.sms.dornica_sms_plugin;

import android.content.Intent;

public interface MessageListener {
    public void messageReceived(String messages);
}