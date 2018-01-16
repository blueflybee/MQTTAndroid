package com.blueflybee.mqttdroidlibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * <pre>
 *     author : shaojun
 *     e-mail : wusj@qtec.cn
 *     time   : 2018/01/16
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MQQTUtils {
  /**
   * 获取设备AndroidID
   *
   * @return AndroidID
   */
  @SuppressLint("HardwareIds")
  public static String getAndroidID(Context context) {
    return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
  }
}
