package com.ntua.ote.logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ntua.ote.logger.models.rs.AsyncResponseUpdateDetails;
import com.ntua.ote.logger.utils.AsyncResponse;
import com.ntua.ote.logger.utils.CommonUtils;
import com.ntua.ote.logger.utils.Constants;
import com.ntua.ote.logger.utils.PermissionsMapping;
import com.ntua.ote.logger.utils.RequestType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class UpdateController implements AsyncResponse<AsyncResponseUpdateDetails> {
    private static final String TAG = UpdateController.class.getName();

    private Activity context;

    private static UpdateController ourInstance;

    public static UpdateController getInstance(Activity context) {
        if(ourInstance == null) {
            ourInstance = new UpdateController(context);
        } else {
            ourInstance.context = context;
        }
        return ourInstance;
    }

    private UpdateController(Activity context) {
        this.context = context;
    }

    public void checkVersion(){
        if(CommonUtils.haveNetworkConnection(context)) {
            new UpdateClient(this).execute(RequestType.CHECK_VERSION);
        } else {
            Toast.makeText(context, "Please enable internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void newVersionAlert(Context context, final String filename){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("   A Newer Version is Available \n   (" + filename + ").\n   Do you Want to Download it?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        alertOnClick();
                    }
                });

        builder.setNegativeButton("No",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void alertOnClick(){
        CommonUtils.requestPermission(PermissionsMapping.DOWNLOAD_PERMISSIONS, context);
    }

    public void download() {

        if(CommonUtils.haveNetworkConnection(context)) {
            new UpdateClient(this).execute(RequestType.UPDATE);
        } else {
            Toast.makeText(context, "Please enable internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void install_apk() {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        File file = new File(context.getFilesDir(), Constants.FILE_NAME);
        install.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        context.startActivity(install);
        context.finish();
    }

    @Override
    public void processFinish(AsyncResponseUpdateDetails output) {
        if(output != null) {
            switch (output.getRequestType()) {
                case CHECK_VERSION: {
                    if (!TextUtils.isEmpty(output.getFilename()) &&
                            ApplicationController.getVersion().compareTo(CommonUtils.getVersion(output.getFilename())) < 0) {
                        newVersionAlert(context, output.getFilename());
                    } else {
                        Toast.makeText(context, "You Have the Most Recent Version", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case UPDATE: {
                    try {
                        Log.i(context.getFilesDir().getAbsolutePath(), TAG);
                        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        File file = new File(path, Constants.FILE_NAME);
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(output.getContent());
                        fos.close();
                        Toast.makeText(context, "Download completed", Toast.LENGTH_SHORT).show();
                        //install_apk();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        } else {
            Toast.makeText(context, "Connection to server failed. Please try again", Toast.LENGTH_SHORT).show();
        }
    }
}
