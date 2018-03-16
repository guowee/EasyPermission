package com.muse.permission;

/**
 * Created by GuoWee on 2018/3/15.
 */

public interface PermissionProxy<T> {
    void grant(T source, int requestCode);

    void denied(T source, int requestCode);
}
