package com.blueflybee.mqttdroidlibrary.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.blueflybee.mqttdroidlibrary.data.MQMessage;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * <pre>
 *     author : shaojun
 *     e-mail : wusj@qtec.cn
 *     time   : 2018/01/08
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MQTTService extends Service {

  public static final String TAG = MQTTService.class.getSimpleName();
  public static final String EXTRA_MQTT_MESSENGER = "extra_mqtt_messenger";
  public static final int MSG_RECEIVE_SUCCESS = 1;

  private final MQTTBinder mBinder = new MQTTBinder();

  private PowerManager.WakeLock mWakeLock = null;

  private MqttAndroidClient mMqttAndroidClient;

  private final String mServerUri = "tcp://192.168.90.200:61613";

  private String mClientId = "ExampleAndroidClient";
  private final String mSubscriptionTopic = "exampleAndroidTopic";
  private final String publishTopic = "exampleAndroidPublishTopic";
  private final String mPublishMessage = "Hello World!";
  private final String mUserName = "admin";
  private final String mPassword = "password";

  private Messenger mMessenger;

  @Override
  public void onCreate() {
    System.out.println(TAG + ".onCreate+++++++++++++++++++++++++++++++++++");
    acquireWakeLock();
    initMQQT();
//    registerReceiver(getBleReceiver(), BleService.getIntentFilter());
  }

  @Override
  public void onDestroy() {
    System.out.println(TAG + ".onDestroy+++++++++++++++++++++++++");
    releaseWakeLock();
    super.onDestroy();
    close();
//    unregisterReceiver(getBleReceiver());
  }


  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    System.out.println(TAG + ".onBind+++++++++++++++++++++++++++++++++++");
    return mBinder;
  }

  @Override
  public boolean onUnbind(Intent intent) {
    System.out.println(TAG + ".onUnbind+++++++++++++++++++++++++++++++++++");
    return super.onUnbind(intent);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    System.out.println(TAG + ".onStartCommand+++++++++++++++++++++++++++++++++++");
    if (mMessenger == null) {
      mMessenger = intent.getParcelableExtra(EXTRA_MQTT_MESSENGER);
    }
    return START_STICKY;
  }

  private void initMQQT() {
    mClientId = mClientId + System.currentTimeMillis();

    mMqttAndroidClient = new MqttAndroidClient(getApplicationContext(), mServerUri, mClientId);
    mMqttAndroidClient.setCallback(new MqttCallbackExtended() {
      @Override
      public void connectComplete(boolean reconnect, String serverURI) {

        if (reconnect) {
          showLog("Reconnected to : " + serverURI);
          // Because Clean Session is true, we need to re-subscribe
          subscribeToTopic();
        } else {
          showLog("Connected to: " + serverURI);
        }
      }

      @Override
      public void connectionLost(Throwable cause) {
        showLog("The Connection was lost.");
      }

      @Override
      public void messageArrived(String topic, MqttMessage message) throws Exception {
        showLog("Incoming message: " + new String(message.getPayload()));
      }

      @Override
      public void deliveryComplete(IMqttDeliveryToken token) {

      }
    });

    connect();
  }

  public void publishMessage() {

    try {
      MqttMessage message = new MqttMessage();
      message.setPayload(mPublishMessage.getBytes());
      mMqttAndroidClient.publish(mSubscriptionTopic, message);
      showLog("Message Published");
      if (!mMqttAndroidClient.isConnected()) {
        showLog(mMqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
      }
    } catch (MqttException e) {
      System.err.println("Error Publishing: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void connect() {
    MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
    mqttConnectOptions.setAutomaticReconnect(true);
    mqttConnectOptions.setCleanSession(false);
    mqttConnectOptions.setUserName(mUserName);
    mqttConnectOptions.setPassword(mPassword.toCharArray());
//        mqttConnectOptions.setConnectionTimeout(1);


    try {
      //addToHistory("Connecting to " + mServerUri);
      mMqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
          DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
          disconnectedBufferOptions.setBufferEnabled(true);
          disconnectedBufferOptions.setBufferSize(100);
          disconnectedBufferOptions.setPersistBuffer(false);
          disconnectedBufferOptions.setDeleteOldestMessages(false);
          mMqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
          subscribeToTopic();
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
          showLog("Failed to connect to: " + mServerUri);
        }
      });

    } catch (MqttException ex) {
      ex.printStackTrace();
    }
  }

  private void subscribeToTopic() {
    try {
      mMqttAndroidClient.subscribe(mSubscriptionTopic, 0, null, new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
          showLog("Subscribed!");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
          showLog("Failed to subscribe");
        }
      });

      // THIS DOES NOT WORK!
      mMqttAndroidClient.subscribe(mSubscriptionTopic, 0, new IMqttMessageListener() {
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
          // message Arrived!
          MQMessage mqMessage = new MQMessage(topic, new String(message.getPayload()));
          System.out.println("mqMessage = " + mqMessage);
          sendMQMessage(MSG_RECEIVE_SUCCESS, mqMessage);
        }
      });

    } catch (MqttException ex) {
      System.err.println("Exception whilst subscribing");
      ex.printStackTrace();
    }
  }

  private void sendMQMessage(int what, MQMessage mqMessage) {
    try {
      if (mMessenger == null) {
        showLog("mMessenger should not be null!!!");
        return;
      }

      Message message = Message.obtain();
      message.what = what;
      message.obj = mqMessage;
      mMessenger.send(message);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  private void showLog(String mainText) {
    System.out.println("LOG: " + mainText);
  }

  private void close() {
    try {
      mMqttAndroidClient.close();
      mMqttAndroidClient.disconnect();
      mMqttAndroidClient = null;
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

  private void releaseWakeLock() {
    if (mWakeLock != null) {
      mWakeLock.release();
      mWakeLock = null;
    }
  }

  private void acquireWakeLock() {
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getContext().getClass().getName());
    mWakeLock.acquire();
  }

  private Context getContext() {
    return this;
  }

  public class MQTTBinder extends Binder {
    public MQTTService getService() {
      return MQTTService.this;
    }
  }


}
