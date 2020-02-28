package app.licy.androidutils.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import app.licy.androidutils.BuildConfig;

/**
 * LogUtils
 * description: log 打印类
 *
 * @author : Licy
 * @date : 2020/2/27
 * email ：licy3051@qq.com
 */
public class LogUtils {

    private static final int LOG_SAVE_DAYS = 7;
    private static boolean LOG_SWITCH = BuildConfig.DEBUG;

    private static boolean sLogToFile = !BuildConfig.DEBUG;

    private static final String LOG_TAG = "LogUtils";

    char log_level = 'v';

    /**
     * 日志的输出格式
     */
    private final static SimpleDateFormat LOG_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    /**
     * 日志文件格式
     */
    private final static SimpleDateFormat FILE_SUFFIX = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    private static String sLogFilePath;
    private static String sLogFileName;

    public static void init(Context context) {
        sLogFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + context.getPackageName();
        sLogFileName = "Log";
    }

    public static void setsLogToFile(boolean status) {
        sLogToFile = status;
    }

    public static void v(@NonNull Object msg) {
        v(LOG_TAG, msg);
    }

    public static void v(String tag, @NonNull Object msg) {
        v(tag, msg, null);
    }

    public static void v(String tag, @NonNull Object msg, Throwable tr) {
        log(tag, msg.toString(), tr, 'v');
    }

    public static void d(@NonNull Object msg) {
        d(LOG_TAG, msg);
    }

    public static void d(String tag, @NonNull Object msg) {
        d(tag, msg, null);
    }

    public static void d(String tag, @NonNull Object msg, Throwable tr) {
        log(tag, msg.toString(), tr, 'd');
    }

    public static void w(@NonNull Object msg) {
        w(LOG_TAG, msg);
    }

    public static void w(String tag, @NonNull Object msg) {
        w(tag, msg, null);
    }

    public static void w(String tag, @NonNull Object msg, Throwable tr) {
        log(tag, msg.toString(), tr, 'w');
    }

    public static void i(@NonNull Object msg) {
        i(LOG_TAG, msg);
    }

    public static void i(String tag, @NonNull Object msg) {
        i(tag, msg, null);
    }

    public static void i(String tag, @NonNull Object msg, Throwable tr) {
        log(tag, msg.toString(), tr, 'i');
    }

    public static void e(@NonNull Object msg) {
        e(LOG_TAG, msg);
    }

    public static void e(String tag, @NonNull Object msg) {
        e(tag, msg, null);
    }

    public static void e(String tag, @NonNull Object msg, Throwable tr) {
        log(tag, msg.toString(), tr, 'e');
    }

    private static String dealMsg(String msg) {
        String funcName = getFunctionName();
        if (TextUtils.isEmpty(funcName)) {
            return msg;
        } else {
            return funcName + "--" + msg;
        }
    }

    private static String getFunctionName() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements.length == 0) {
            return "";
        }

        for (StackTraceElement element : elements) {
            if (element.isNativeMethod()) {
                continue;
            }
            if (TextUtils.equals(element.getClassName(), Thread.currentThread().getName())) {
                continue;
            }
            if (TextUtils.equals(element.getFileName(), "LogUtils.java")) {
                continue;
            }

            return "["
                    + Thread.currentThread().getName()
                    + "("
                    + Thread.currentThread().getId()
                    + "):"
                    + element.getFileName()
                    + ":"
                    + element.getLineNumber()
                    + "]";
        }

        return "";
    }

    /**
     * @param tag
     * @param msg
     * @param throwable
     * @param level
     */
    private static void log(String tag, String msg, Throwable throwable, char level) {
        // 开关，为true 则打印log
        if (!LOG_SWITCH) {
            return;
        }

        if (TextUtils.isEmpty(tag)) {
            tag = LOG_TAG;
        }

        // 因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
        // 把4*1024的MAX字节打印长度改为2001字符数
        int maxStrLength = 2001 - tag.length();

        // 大于4000时
        while (msg.length() > maxStrLength) {
            doLog(tag, msg.substring(0, maxStrLength), throwable, level);
            msg = msg.substring(maxStrLength);
        }
        // 剩余部分
        doLog(tag, msg, throwable, level);
    }

    private static void doLog(String tag, String msg, Throwable throwable, char level) {
        switch (level) {
            case 'i':
                Log.i(tag, dealMsg(msg), throwable);
                break;
            case 'w':
                Log.w(tag, dealMsg(msg), throwable);
                break;
            case 'd':
                Log.d(tag, dealMsg(msg), throwable);
                log2file(msg, level, tag, throwable);
                break;
            case 'e':
                Log.e(tag, dealMsg(msg), throwable);
                log2file(msg, level, tag, throwable);
                break;
            case 'v':
            default:
                Log.v(tag, dealMsg(msg), throwable);
                break;
        }
    }

    private static void log2file(String msg, char level, String tag, Throwable throwable) {
        if (sLogToFile) {
            String stackTrace;
            if (throwable == null) {
                stackTrace = "";
            } else {
                stackTrace = Log.getStackTraceString(throwable);
            }
            log2File(msg + stackTrace, level, tag);
        }
    }

    private static synchronized void log2File(String log, char level, String tag) {

        Date date = new Date();

        String filename = FILE_SUFFIX.format(date);

        String content = LOG_FORMAT.format(date)
                + ":"
                + String.valueOf(level).toUpperCase()
                + ":"
                + tag
                + ":"
                + log;

        File fileDir = new File(sLogFilePath);
        if (!fileDir.exists()) {
            boolean mkdir = fileDir.mkdir();
        }
        File fileLog = new File(sLogFilePath, sLogFileName + filename);
        try {
            FileWriter fileWriter = new FileWriter(fileLog, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(content);
            bufferedWriter.newLine();
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void delFile() {
        String needDelFile = FILE_SUFFIX.format(getDateBefore());
        File file = new File(sLogFilePath, needDelFile + sLogFileName);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 得到LOG_SAVE_DAYS天前的日期
     *
     * @return
     */
    private static Date getDateBefore() {
        Date nowTime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowTime);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - LOG_SAVE_DAYS);
        return now.getTime();
    }

}