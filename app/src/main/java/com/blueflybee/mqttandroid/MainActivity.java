package com.blueflybee.mqttandroid;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.blueflybee.mqttdroidlibrary.MQQTUtils;
import com.blueflybee.mqttdroidlibrary.data.MQMessage;
import com.blueflybee.mqttdroidlibrary.service.MQTTService;

public class MainActivity extends AppCompatActivity {

  private static final int NOTIFICATION_ID = 0;

  private MQTTService mMQTTService;

  private String[] mSubscriptionTopics;

  private final ServiceConnection mServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder rawBinder) {
      mMQTTService = ((MQTTService.MQTTBinder) rawBinder).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName classname) {
      mMQTTService = null;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSubscriptionTopics = new String[]{
//        "smarthome.server.s." + MQQTUtils.getAndroidID(this)
        "smarthome.server.s.123"
    };
    //    mSubscriptionTopics = new String[2];
//    mSubscriptionTopics[0] = "smarthome.server.s." + MQQTUtils.getAndroidID(getContext());
//    mSubscriptionTopics[1] = "smarthome.server.s.123.specfocu";

    findViewById(R.id.btn_start_service).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this, MQTTService.class);
        intent.putExtra(MQTTService.EXTRA_MQTT_MESSENGER, new Messenger(mHandler));
        intent.putExtra(MQTTService.EXTRA_CLIENT_ID, clientId());
        intent.putExtra(MQTTService.EXTRA_TOPICS, mSubscriptionTopics);
        startService(intent);

        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
      }
    });

    findViewById(R.id.btn_stop_service).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mMQTTService != null) {
          Intent intent = new Intent(MainActivity.this, MQTTService.class);
          unbindService(mServiceConnection);
          stopService(intent);
          mMQTTService = null;
        }
      }
    });

    findViewById(R.id.btn_send_msg).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mMQTTService != null) {
          mMQTTService.publishMessage();
        }
      }
    });

  }

  private String clientId() {
    return ((EditText) findViewById(R.id.et_client_id)).getText().toString().trim();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unbindService(mServiceConnection);
  }

  @SuppressLint("HandlerLeak")
  private Handler mHandler = new Handler() {
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case MQTTService.MSG_RECEIVE_SUCCESS:
          MQMessage mqMessage = (MQMessage) msg.obj;
          Toast.makeText(MainActivity.this, mqMessage.toString(), Toast.LENGTH_SHORT).show();
          showNotification(mqMessage.toString());
          break;

        case MQTTService.MSG_MQTT_STATUS:
          MQMessage mqStatusMessage = (MQMessage) msg.obj;
          Toast.makeText(MainActivity.this, mqStatusMessage.getMessage(), Toast.LENGTH_SHORT).show();
          break;
        default:
          break;
      }
    }
  };

  private void showNotification(String msg) {
    NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
    String appName = getString(R.string.app_name);
    builder.setContentTitle(appName)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentText(msg);

    PendingIntent pIntent = PendingIntent.getActivity(this, 1, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
    builder.setContentIntent(pIntent);
    builder.setAutoCancel(true);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      builder.setVisibility(Notification.VISIBILITY_PUBLIC);
      // 关联PendingIntent
      builder.setFullScreenIntent(pIntent, false);
    }
    Notification notification = builder.build();

    /**
     * sound属性是一个 Uri 对象。 可以在通知发出的时候播放一段音频，这样就能够更好地告知用户有通知到来.
     * 如：手机的/system/media/audio/ringtones 目录下有一个 Basic_tone.ogg音频文件，
     * 可以写成： Uri soundUri = Uri.fromFile(new
     * File("/system/media/audio/ringtones/Basic_tone.ogg"));
     * notification.sound = soundUri; 我这里为了省事，就去了手机默认设置的铃声
     */
    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    notification.sound = uri;
    /**
     * 手机处于锁屏状态时， LED灯就会不停地闪烁， 提醒用户去查看手机,下面是绿色的灯光一 闪一闪的效果
     */
    notification.ledARGB = Color.GREEN;// 控制 LED 灯的颜色，一般有红绿蓝三种颜色可选
    notification.ledOnMS = 1000;// 指定 LED 灯亮起的时长，以毫秒为单位
    notification.ledOffMS = 1000;// 指定 LED 灯暗去的时长，也是以毫秒为单位
    notification.flags = Notification.FLAG_SHOW_LIGHTS;// 指定通知的一些行为，其中就包括显示

    /**
     * vibrate属性是一个长整型的数组，用于设置手机静止和振动的时长，以毫秒为单位。
     * 参数中下标为0的值表示手机静止的时长，下标为1的值表示手机振动的时长， 下标为2的值又表示手机静止的时长，以此类推。
     */
    long[] vibrates = { 0, 1000, 1000, 1000 };
    notification.vibrate = vibrates;


    notifyManager.notify(NOTIFICATION_ID, notification);

  }

}
