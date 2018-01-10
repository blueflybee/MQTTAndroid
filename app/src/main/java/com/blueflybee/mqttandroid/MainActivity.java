package com.blueflybee.mqttandroid;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.blueflybee.mqttdroidlibrary.data.MQMessage;
import com.blueflybee.mqttdroidlibrary.service.MQTTService;

public class MainActivity extends AppCompatActivity {

  private MQTTService mMQTTService;

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

    findViewById(R.id.btn_start_service).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this, MQTTService.class);
        intent.putExtra(MQTTService.EXTRA_MQTT_MESSENGER, new Messenger(mHandler));
        startService(intent);

        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
      }
    });

    findViewById(R.id.btn_stop_service).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this, MQTTService.class);
        unbindService(mServiceConnection);
        stopService(intent);
        mMQTTService = null;
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
          break;
        default:
          break;
      }
    }
  };

}
