package com.muse.permission.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import com.muse.permission.PermissionFail;
import com.muse.permission.PermissionSuccess;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by GuoWee on 2018/3/13.
 */

public class PermissionUtil {

    private PermissionUtil() {
    }

    public static boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }


    @TargetApi(Build.VERSION_CODES.M)
    public static List<String> findDeniedPermissions(Activity activity, String... permissions) {
        List<String> deniedPermissions = new ArrayList<>();

        for (String permission : permissions) {
            if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
            }
        }

        return deniedPermissions;
    }


    public static <T extends Annotation> Method findMethodWithRequestCode(Class clazz, Class<T> annotation, int requestCode) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                if (isEqualRequestCodeFromAnnotation(method, annotation, requestCode)) {
                    return method;
                }
            }
        }
        return null;
    }


    public static boolean isEqualRequestCodeFromAnnotation(Method method, Class clazz, int requestCode) {
        if (clazz.equals(PermissionSuccess.class)) {
            return requestCode == method.getAnnotation(PermissionSuccess.class).requestCode();
        } else if (clazz.equals(PermissionFail.class)) {
            return requestCode == method.getAnnotation(PermissionFail.class).requestCode();
        }

        return false;
    }

}
