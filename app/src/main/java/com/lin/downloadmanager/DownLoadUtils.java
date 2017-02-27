package com.lin.downloadmanager;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * Created by Administrator on 2017/2/27.
 */

public class DownLoadUtils {
    private DownloadManager mDownloadManager;
    private DownloadManager.Request mRequest;
    private String mTitle;
    private final static String sLoadPath = "/myLoadApk/";
    Timer timer;
    long id;
    TimerTask task;
    private Activity mActivity;
    public File mFile;

    public DownLoadUtils(Activity activity, String downloadUrl, String title) {
        this.mActivity = activity;
        this.mTitle = title;
        mDownloadManager = (DownloadManager) activity.getSystemService(DOWNLOAD_SERVICE);
        mRequest = new DownloadManager.Request(Uri.parse(downloadUrl));
    }

    public void downLoad() {
        mRequest.setTitle(mTitle);
        mRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        mRequest.setAllowedOverRoaming(false);
        mRequest.setMimeType("application/vnd.android.package-archive");
        mRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        mFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!mFile.exists()) {
            mFile.mkdirs();
        }
        //设置文件存放路径
        mRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app-release.apk");
        final DownloadManager.Query query = new DownloadManager.Query();
        id = mDownloadManager.enqueue(mRequest);
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                Cursor cursor = mDownloadManager.query(query.setFilterById(id));
                if (cursor != null && cursor.moveToFirst()) {

                    if (cursor.getInt(
                            cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        install(mFile);
                        task.cancel();

                    }
                }
                cursor.close();
            }
        };
        timer.schedule(task, 0, 1000);
    }


    private void install(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//4.0以上系统弹出安装成功打开界面
        intent.setDataAndType(Uri.parse("file://" + file.getAbsolutePath() + "/app-release.apk"), "application/vnd.android.package-archive");
        mActivity.startActivity(intent);
    }
}
