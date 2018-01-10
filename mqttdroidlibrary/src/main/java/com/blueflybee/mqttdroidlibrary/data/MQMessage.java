package com.blueflybee.mqttdroidlibrary.data;

/**
 * <pre>
 *     author : shaojun
 *     e-mail : wusj@qtec.cn
 *     time   : 2018/01/10
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MQMessage {

  private String topic;
  private String message;

  public MQMessage(String topic, String message) {
    this.topic = topic;
    this.message = message;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "MQMessage{" +
        "topic='" + topic + '\'' +
        ", message=" + message +
        '}';
  }
}
