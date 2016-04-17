package com.zyc.softkey.operate;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;

import com.zyc.softkey.utils.ILog;

public class Operations {
    private static final String TAG = "Operations";

    private Context mContext;
    
    private static Timer timer;

    public Operations(Context context) {
        mContext = context;
    }

    public static void goToHome(Context context) {
        ILog.d(TAG, "goToHome");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void doExec() {
//        loopExec();
        doCmdCommand();
    
        TimerTask task = new TimerTask() {
            public void run() {
//                loopExec();
                doCmdCommand();
            }
        };

        timer = new Timer();
        timer.schedule(task, 1000, 1000);
    }
    
    public static void stopExec(){
        if (null != timer) {
            timer.cancel();
        }
    }

    private static void loopExec() {
        final String command[] = new String[] { "/system/xbin/su",
                "adb shell input tap 550 1300" };

        try {
            String result = run(command, "/system/xbin/");
            ILog.i(TAG, "deExec result : " + result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void doCmdCommand() {
        // adb push core code
        String command = "input tap 550 1190";
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行一个shell命令，并返回字符串值
     * 
     * @param cmd
     *            命令名称&参数组成的数组（例如：{"/system/bin/cat", "/proc/version"}）
     * @param workdirectory
     *            命令执行路径（例如："system/bin/"）
     * @return 执行结果组成的字符串
     * @throws IOException
     */
    public static synchronized String run(String[] cmd, String workdirectory)
            throws IOException {
        StringBuffer result = new StringBuffer();
        try {
            // 创建操作系统进程（也可以由Runtime.exec()启动）
            // Runtime runtime = Runtime.getRuntime();
            // Process proc = runtime.exec(cmd);
            // InputStream inputstream = proc.getInputStream();
            ProcessBuilder builder = new ProcessBuilder(cmd);

            InputStream in = null;
            // 设置一个路径（绝对路径了就不一定需要）
            if (workdirectory != null) {
                // 设置工作目录（同上）
                builder.directory(new File(workdirectory));
                // 合并标准错误和标准输出
                builder.redirectErrorStream(true);
                // 启动一个新进程
                Process process = builder.start();

                // 读取进程标准输出流
                in = process.getInputStream();
                byte[] re = new byte[1024];
                while (in.read(re) != -1) {
                    result = result.append(new String(re));
                }
            }
            // 关闭输入流
            if (in != null) {
                in.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result.toString();
    }
}
