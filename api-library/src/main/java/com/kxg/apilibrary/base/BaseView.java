package com.kxg.apilibrary.base;

import android.content.Context;

/**
 * Created by kuangxiaoguo on 2018/3/24.
 */

@Deprecated
public interface BaseView<T> {

    Context getMyContext();

    void onError(Throwable e);

    void showProgress();

    void dismissProgress();

    void setData(T result);
}
