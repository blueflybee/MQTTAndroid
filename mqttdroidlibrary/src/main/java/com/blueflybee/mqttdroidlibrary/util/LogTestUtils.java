package com.blueflybee.mqttdroidlibrary.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.provider.Settings;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * <pre>
 *     author : shaojun
 *     e-mail : wusj@qtec.cn
 *     time   : 2018/01/16
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class LogTestUtils {
  private static final DateFormat DEFAULT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
  public static void testLogToFile(Context context, String  fileName, String content) {
    String filePath;
    if (Environment.getExternalStorageState().equals(
        Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
      filePath = Environment.getExternalStorageDirectory()
          .getAbsolutePath() + File.separator + "QtecLog" + File.separator + fileName;

    } else {// 如果SD卡不存在，就保存到本应用的目录下
      filePath = context.getFilesDir().getAbsolutePath()

          + File.separator + "QtecLog" + File.separator + fileName;
    }
    writeFileFromString(filePath, content + "\n", true);
  }

  /**
   * 将字符串写入文件
   *
   * @param filePath 文件路径
   * @param content  写入内容
   * @param append   是否追加在文件末
   * @return {@code true}: 写入成功<br>{@code false}: 写入失败
   */
  public static boolean writeFileFromString(String filePath, String content, boolean append) {
    return writeFileFromString(getFileByPath(filePath), content, append);
  }

  /**
   * 将字符串写入文件
   *
   * @param file    文件
   * @param content 写入内容
   * @param append  是否追加在文件末
   * @return {@code true}: 写入成功<br>{@code false}: 写入失败
   */
  public static boolean writeFileFromString(File file, String content, boolean append) {
    if (file == null || content == null) return false;
    if (!createOrExistsFile(file)) return false;
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(file, append));
      bw.write(content);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    } finally {
      closeIO(bw);
    }
  }

  /**
   * 根据文件路径获取文件
   *
   * @param filePath 文件路径
   * @return 文件
   */
  public static File getFileByPath(String filePath) {
    return isSpace(filePath) ? null : new File(filePath);
  }

  /**
   * 判断文件是否存在，不存在则判断是否创建成功
   *
   * @param file 文件
   * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
   */
  public static boolean createOrExistsFile(File file) {
    if (file == null) return false;
    // 如果存在，是文件则返回true，是目录则返回false
    if (file.exists()) return file.isFile();
    if (!createOrExistsDir(file.getParentFile())) return false;
    try {
      return file.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 判断目录是否存在，不存在则判断是否创建成功
   *
   * @param file 文件
   * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
   */
  public static boolean createOrExistsDir(File file) {
    // 如果存在，是目录则返回true，是文件则返回false，不存在则返回是否创建成功
    return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
  }

  /**
   * 关闭IO
   *
   * @param closeables closeables
   */
  public static void closeIO(Closeable... closeables) {
    if (closeables == null) return;
    for (Closeable closeable : closeables) {
      if (closeable != null) {
        try {
          closeable.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static boolean isSpace(String s) {
    if (s == null) return true;
    for (int i = 0, len = s.length(); i < len; ++i) {
      if (!Character.isWhitespace(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public static String getNowString() {
    return millis2String(System.currentTimeMillis(), DEFAULT_FORMAT);
  }

  /**
   * 将时间戳转为时间字符串
   * <p>格式为format</p>
   *
   * @param millis 毫秒时间戳
   * @param format 时间格式
   * @return 时间字符串
   */
  public static String millis2String(long millis, DateFormat format) {
    return format.format(new Date(millis));
  }
}
