package com.kxg.apilibrary.base;

/**
 * Created by kuangxiaoguo on 2018/3/24.
 */

@Deprecated
public interface BasePresenter<T> {

    void attachView(T view);

    void detachView();
}
