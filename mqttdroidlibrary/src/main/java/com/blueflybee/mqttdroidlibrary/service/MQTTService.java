package com.blueflybee.mqttdroidlibrary.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import com.blueflybee.mqttdroidlibrary.MQQTUtils;
import com.blueflybee.mqttdroidlibrary.data.MQMessage;
import com.blueflybee.mqttdroidlibrary.util.LogTestUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Arrays;

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
  public static final String EXTRA_CLIENT_ID = "extra_client_id";
  public static final String EXTRA_TOPICS = "extra_topics";

  public static final int MSG_RECEIVE_SUCCESS = 1;
  public static final int MSG_MQTT_STATUS = 2;
  public static final long TIME = 1000 * 60 * 60 * 200;

  private final MQTTBinder mBinder = new MQTTBinder();

  private PowerManager.WakeLock mWakeLock = null;

  private MqttAndroidClient mMqttAndroidClient;

  //  private final String mServerUri = "tcp://192.168.90.200:61613";
  private final String mServerUri = "tcp://mqtt.3caretec.com:1883";

  //android id
  private String mClientId = "";
  //  smarthome/server/s/{android id}
//  private String mSubscriptionTopics = "smarthome.server.s.";
  private String[] mSubscriptionTopics = new String[]{};

  private final String publishTopic = "exampleAndroidPublishTopic";
  private final String mPublishMessage = "Hello World!";
  private final String mUserName = "mqtt_client";
  private final String mPassword = "mqtt_client_pass";

  private Messenger mMessenger;
  private CountDownTimer mTimer;

  @Override
  public void onCreate() {
    System.out.println(TAG + ".onCreate+++++++++++++++++++++++++++++++++++");
    LogTestUtils.testLogToFile(getContext(), "mqtt_log.txt", LogTestUtils.getNowString() + "   " + TAG + ".onCreate");
    acquireWakeLock();

    mTimer = new CountDownTimer(TIME, 2000) {

      @Override
      public void onTick(long millisUntilFinished) {
        LogTestUtils.testLogToFile(getContext(), "mqtt_log.txt", LogTestUtils.getNowString() + "   " + "onTick...");
      }

      @Override
      public void onFinish() {

      }
    }.start();
  }

  @Override
  public void onDestroy() {
    System.out.println(TAG + ".onDestroy+++++++++++++++++++++++++");
    LogTestUtils.testLogToFile(getContext(), "mqtt_log.txt", LogTestUtils.getNowString() + "   " + TAG + ".onDestroy");
    releaseWakeLock();
    super.onDestroy();
    close();
    if (mTimer != null) {
      mTimer.cancel();
    }
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    System.out.println(TAG + ".onBind+++++++++++++++++++++++++++++++++++");
    LogTestUtils.testLogToFile(getContext(), "mqtt_log.txt", LogTestUtils.getNowString() + "   " + TAG + ".onBind");
    return mBinder;
  }

  @Override
  public boolean onUnbind(Intent intent) {
    System.out.println(TAG + ".onUnbind+++++++++++++++++++++++++++++++++++");
    LogTestUtils.testLogToFile(getContext(), "mqtt_log.txt", LogTestUtils.getNowString() + "   " + TAG + ".onUnbind");
    return super.onUnbind(intent);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    try {
      System.out.println(TAG + ".onStartCommand+++++++++++++++++++++++++++++++++++");
      LogTestUtils.testLogToFile(getContext(), "mqtt_log.txt", LogTestUtils.getNowString() + "   " + TAG + ".onStartCommand");
      mMessenger = intent.getParcelableExtra(EXTRA_MQTT_MESSENGER);
      mClientId = intent.getStringExtra(EXTRA_CLIENT_ID);
      mSubscriptionTopics = intent.getStringArrayExtra(EXTRA_TOPICS);
      initMQQT();
    } catch (Exception e) {
      e.printStackTrace();
    }



    return START_STICKY;
  }

  private void initMQQT() {
    if (mMqttAndroidClient != null && mMqttAndroidClient.isConnected()) return;

//    mSubscriptionTopics = new String[2];
//    mSubscriptionTopics[0] = "smarthome.server.s." + MQQTUtils.getAndroidID(getContext());
//    mSubscriptionTopics[1] = "smarthome.server.s.123.specfocu";
    mClientId = MQQTUtils.getAndroidID(getContext());

    System.out.println("mSubscriptionTopics = " + Arrays.toString(mSubscriptionTopics));
    System.out.println("mClientId = " + mClientId);

    mMqttAndroidClient = new MqttAndroidClient(getApplicationContext(), mServerUri, mClientId);
    mMqttAndroidClient.setCallback(new MqttCallbackExtended() {
      @Override
      public void connectComplete(boolean reconnect, String serverURI) {

        if (reconnect) {
          showLog("Reconnected to : " + serverURI);
          LogTestUtils.testLogToFile(getContext(), "mqtt_log.txt", LogTestUtils.getNowString() + "   " + "Reconnected to: " + serverURI);
          // Because Clean Session is true, we need to re-subscribe
          subscribeToTopic();
        } else {
          showLog("Connected to: " + serverURI);
          LogTestUtils.testLogToFile(getContext(), "mqtt_log.txt", LogTestUtils.getNowString() + "   " + "Connected to: " + serverURI);
        }
      }

      @Override
      public void connectionLost(Throwable cause) {
        showLog("The Connection was lost.");
        LogTestUtils.testLogToFile(getContext(), "mqtt_log.txt", LogTestUtils.getNowString() + "   The Connection was lost.");
      }

      @Override
      public void messageArrived(String topic, MqttMessage message) throws Exception {
//        showLog("Incoming message: " + new String(message.getPayload()));
        // message Arrived!
        MQMessage mqMessage = new MQMessage(topic, new String(message.getPayload()));
        System.out.println(TAG + " mqMessage = " + mqMessage);
        LogTestUtils.testLogToFile(getContext(), "mqtt_log.txt", LogTestUtils.getNowString() + "   " + TAG + "messageArrived mqMessage = " + mqMessage);

        sendMQMessage(MSG_RECEIVE_SUCCESS, mqMessage);
      }

      @Override
      public void deliveryComplete(IMqttDeliveryToken token) {

      }
    });

    connect();
  }

  public void publishMessage() {

    try {
      if (mMqttAndroidClient == null) return;
      MqttMessage message = new MqttMessage();
      message.setQos(1);
      message.setPayload(mPublishMessage.getBytes());
      mMqttAndroidClient.publish(mSubscriptionTopics[0], message);
      showLog("Message Published");
      if (!mMqttAndroidClient.isConnected()) {
        showLog(mMqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
      }
    } catch (Exception e) {
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
          LogTestUtils.testLogToFile(getContext(), "mqtt_log.txt", LogTestUtils.getNowString() + "   " + "Failed to connect to: " + mServerUri);
          close();
        }
      });

    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void subscribeToTopic() {
    try {
      if (mMqttAndroidClient == null) return;
      mMqttAndroidClient.subscribe(mSubscriptionTopics, initQos(), null, new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
          showLog("Subscribed!");
          LogTestUtils.testLogToFile(getContext(), "mqtt_log.txt", LogTestUtils.getNowString() + "   Subscribed!");

        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
          showLog("Failed to subscribe");
          LogTestUtils.testLogToFile(getContext(), "mqtt_log.txt", LogTestUtils.getNowString() + "   Failed to subscribe");
        }
      });

      // THIS DOES NOT WORK!
//      mMqttAndroidClient.subscribe(mSubscriptionTopics, 0, new IMqttMessageListener() {
//        @Override
//        public void messageArrived(String topic, MqttMessage message) throws Exception {
//          // message Arrived!
//          MQMessage mqMessage = new MQMessage(topic, new String(message.getPayload()));
//          System.out.println("mqMessage = " + mqMessage);
//          sendMQMessage(MSG_RECEIVE_SUCCESS, mqMessage);
//        }
//      });

    } catch (Exception ex) {
      System.err.println("Exception whilst subscribing");
      ex.printStackTrace();
    }
  }

  private int[] initQos() {
    int[] qos = new int[mSubscriptionTopics.length];
    for (int i = 0; i < qos.length; i++) {
      qos[i] = 1;
    }
    return qos;
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
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void showLog(String mainText) {
    System.out.println(TAG + " mqqt_status = " + mainText);
    MQMessage mqMessage = new MQMessage("mqqt_status", mainText);
    sendMQMessage(MSG_MQTT_STATUS, mqMessage);
  }

  private void close() {
    try {
      if (mMqttAndroidClient != null) {
        mMqttAndroidClient.close();
        mMqttAndroidClient.disconnect();
        mMqttAndroidClient = null;
      }
    } catch (Exception e) {
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
