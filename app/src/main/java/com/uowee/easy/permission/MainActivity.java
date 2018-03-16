package com.uowee.easy.permission;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.muse.annotation.PermissionFail;
import com.muse.annotation.PermissionSuccess;
import com.muse.permission.EasyPermissionHelper;


public class MainActivity extends AppCompatActivity {

    private Button cameraBtn;

    private final static int REQUEST_PERMISSION_CAMERA_CODE = 0x01;
    private final static int REQUEST_PERMISSION_TEST_CODE = 0x02;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBtn = findViewById(R.id.camera_btn);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyPermissionHelper.with(MainActivity.this)
                        .addRequestCode(REQUEST_PERMISSION_CAMERA_CODE)
                        .permissions(Manifest.permission.CAMERA)
                        .request();
            }
        });
    }


    @PermissionSuccess(requestCode = REQUEST_PERMISSION_CAMERA_CODE)
    void openSucc() {
        Toast.makeText(this, "Camera is open", Toast.LENGTH_SHORT).show();
    }

    @PermissionFail(requestCode = REQUEST_PERMISSION_CAMERA_CODE)
    void openFail() {
        Toast.makeText(this, "Camera permission is not granted", Toast.LENGTH_SHORT).show();
    }

    @PermissionSuccess(requestCode = REQUEST_PERMISSION_TEST_CODE)
    void test1() {
    }

    @PermissionFail(requestCode = REQUEST_PERMISSION_TEST_CODE)
    void test2() {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissionHelper.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
