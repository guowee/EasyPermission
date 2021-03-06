package com.muse.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.Fragment;


import com.muse.permission.util.PermissionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GuoWee on 2018/3/15.
 */

public class EasyPermissionHelper {
    private static final String SUFFIX = "$$PermissionProxy";

    private String[] permissions;
    private int requestCode;
    private Object object;

    private EasyPermissionHelper(Object obj) {
        this.object = obj;
    }

    public static EasyPermissionHelper with(Activity activity) {
        return new EasyPermissionHelper(activity);
    }

    public static EasyPermissionHelper with(Fragment fragment) {
        return new EasyPermissionHelper(fragment);
    }

    public EasyPermissionHelper addRequestCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    public EasyPermissionHelper permissions(String... permissions) {
        this.permissions = permissions;
        return this;
    }


    public void request() {
        requestPermissions(object, requestCode, permissions);
    }


    public static void onRequestPermissionsResult(Object obj, int requestCode, String[] permissions, int[] grantResults) {
        List<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[i]);
            }
        }

        if (deniedPermissions.size() > 0) {
            executeFail(obj, requestCode);
        } else {
            executeSuccess(obj, requestCode);
        }

    }


    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions(Object obj, int requestCode, String[] permissions) {
        if (!PermissionUtil.isOverMarshmallow()) {
            //如果运行在Android 6.0以下的版本上，直接执行方法
            executeSuccess(obj, requestCode);
            return;
        }
        //寻找是否有未被允许的权限
        List<String> deniedPermissions = PermissionUtil.findDeniedPermissions(getActivity(obj), permissions);
        if (deniedPermissions.size() > 0) {
            //存在未允许的权限，需要申请权限
            if (obj instanceof Activity) {
                ((Activity) obj).requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]), requestCode);
            } else if (obj instanceof Fragment) {
                ((Fragment) obj).requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]), requestCode);
            } else {
                throw new IllegalArgumentException(object.getClass().getName() + " is not supported");
            }
        } else {
            // 权限都已经被允许，直接执行方法
            executeSuccess(obj, requestCode);
        }

    }


    private static void executeSuccess(Object obj, int requestCode) {
        findPermissionProxy(obj).grant(obj, requestCode);
    }

    private static void executeFail(Object obj, int requestCode) {
        findPermissionProxy(obj).denied(obj, requestCode);
    }


    private static PermissionProxy findPermissionProxy(Object activity) {
        try {
            Class clazz = activity.getClass();
            Class injectorClazz = Class.forName(clazz.getName() + SUFFIX);
            return (PermissionProxy) injectorClazz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new RuntimeException(String.format("can not find %s , something when compiler.", activity.getClass().getSimpleName() + SUFFIX));
    }


    private Activity getActivity(Object object) {
        if (object instanceof Activity) {
            return (Activity) object;
        } else if (object instanceof Fragment) {
            return ((Fragment) object).getActivity();
        }

        return null;
    }
}
