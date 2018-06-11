package com.kxg.apilibrary.base;

import android.content.Context;

public interface BaseContract {

    interface BaseView {

        void showProgress();

        void dismissProgress();

        Context getMyContext();

        void onError(Throwable e);
    }

    interface BasePresenter<T> {

        void attachView(T view);

        void detachView();
    }
}
