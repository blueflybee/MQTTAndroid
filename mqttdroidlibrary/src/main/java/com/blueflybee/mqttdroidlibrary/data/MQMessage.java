package com.blueflybee.mqttdroidlibrary.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <pre>
 *     author : shaojun
 *     e-mail : wusj@qtec.cn
 *     time   : 2018/01/10
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MQMessage implements Parcelable{

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

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    // 序列化过程：必须按成员变量声明的顺序进行封装
    dest.writeString(topic);
    dest.writeString(message);
  }

  // 反序列过程：必须实现Parcelable.Creator接口，并且对象名必须为CREATOR
  // 读取Parcel里面数据时必须按照成员变量声明的顺序，Parcel数据来源上面writeToParcel方法，读出来的数据供逻辑层使用
  public static final Parcelable.Creator<MQMessage> CREATOR = new Creator<MQMessage>() {

    @Override
    public MQMessage createFromParcel(Parcel source) {
      return new MQMessage(source.readString(), source.readString());
    }

    @Override
    public MQMessage[] newArray(int size) {
      return new MQMessage[size];
    }
  };
}
