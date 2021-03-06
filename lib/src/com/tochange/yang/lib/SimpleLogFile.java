package com.tochange.yang.lib;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tochange.yang.lib.Utils.FileInfos;

import android.content.Context;
import android.util.Log;

public class SimpleLogFile
{
    final static String TAG = "MyLogcatCapture";

    final static String LOGPATH = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/mydebug";

    final static String LOGCONFIGPATH = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath()
            + "/mydebug/config";

    private static boolean getTagListFromFile(String tagFilePath,
            List<String> tagList)
    {
        boolean res = false;
        tagList.clear();
        try
        {
            FileReader in = new FileReader(tagFilePath);
            BufferedReader bufferedReader = new BufferedReader(in);
            String line;
            while (bufferedReader.ready())
            {
                line = bufferedReader.readLine();
                if (line.length() > 0)
                {
                    tagList.add(line);
                }
            }
            in.close();
            if (tagList.size() > 0)
            {
                res = true;
            }
        }
        catch (IOException e)
        {
            Log.d(TAG, e.toString());
        }

        return res;
    }

    public static void captureLogToFile(Context context, String packageName)
    {
        String appName = packageName
                .substring(packageName.lastIndexOf(".") + 1);
        File tagFile = new File(LOGCONFIGPATH + "/" + appName);

        deduceLogFile(appName);

        if (!tagFile.exists())
        {
            Log.e(TAG, "tag config file:" + tagFile.getAbsolutePath()
                    + "  not found!");
            return;
        }
        List<String> tagList = new ArrayList<String>();
        if (!getTagListFromFile(LOGCONFIGPATH + "/" + appName, tagList))
        {
            Log.w(TAG, "get tag config file failed !");
            return;
        }
        String[] LOGCAT_PREFIX = new String[] { "logcat", "-v", "time", "-s" };
        String[] cmdArray = new String[LOGCAT_PREFIX.length + tagList.size()];
        for (int i = 0; i < LOGCAT_PREFIX.length; i++)
        {
            cmdArray[i] = LOGCAT_PREFIX[i];
        }
        for (int j = 0; j < tagList.size(); j++)
        {
            cmdArray[LOGCAT_PREFIX.length + j] = tagList.get(j);
        }
        String logCmd = "";
        for (int k = 0; k < cmdArray.length; k++)
        {
            logCmd = logCmd + cmdArray[k] + " ";
        }
        // create log file

        String logFileName = appName + Utils.getCurTimeToString(1, 0) + ".log";
        File flog = new File(LOGPATH + "/" + logFileName);
        // start write log file
        String param = logCmd + " -p > " + flog.toString();
        String[] comdline = { "/system/bin/sh", "-c", param };
        String cmd = "pkill logcat";
        Log.w(TAG, "log cmd : " + param);
        try
        {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.write(cmd.getBytes());
            os.flush();
            os.close();
            // clear the logcat first
            Runtime.getRuntime().exec("logcat -c");
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            Runtime.getRuntime().exec(comdline);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void deduceLogFile(String appName)
    {
        List<FileInfos> list = Utils.getDeletedFiles(android.os.Environment
                .getExternalStorageDirectory().getAbsolutePath() + "/mydebug/",
                appName);
        if (list != null)
            for (FileInfos f : list)
                f.file.delete();

    }
    
    public static void stopLog(){
        
        String[] cmd = {"adb logcat -d"};
        try
        {
            Runtime.getRuntime().exec(cmd);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
