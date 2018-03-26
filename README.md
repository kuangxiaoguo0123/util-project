# util-project
Common util functions, for example MVP, Retrofit, ScreenUtil and CollectionUtil.

# Start a new project?
You can use this when you start a new project because it contains almost all the util functions you need to use in your project.

我们在开始一个新项目的时候，肯定会涉及到一些通用工具的使用，比如一些util工具类、图片加载库、网络请求库等。本篇博文就是对这些通用工具类的一些封装，这样可以节省很多开发时间。下面就以Retrofit的封装为例向大家介绍如何使用这些library。
# Api
```
public class Api {

    private static final int TIME_OUT = 10000;
    private static final Map<String, Object> mServiceMap = new HashMap<>();

    private Api() {
    }

    public <S> S getService(Class<S> serviceClass) {
        String className = serviceClass.getName();
        if (!(mServiceMap.containsKey(className))) {
            throw new RuntimeException("You must init " + className + " first when application init !");
        }
        return (S) mServiceMap.get(className);
    }

    public <S> void init(Class<S> serviceClass, String baseUrl, OkHttpClient client) {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .build();
        }
        if (baseUrl == null || TextUtils.isEmpty(baseUrl)) {
            throw new NullPointerException("baseUrl can not be null");
        }
        if (!baseUrl.endsWith("/")) {
            throw new IllegalArgumentException("baseUrl must be end with /");
        }
        createService(serviceClass, baseUrl, client);
    }

    public static Api getInstance() {
        return SingleHolder.API_INSTANCE;
    }

    private static class SingleHolder {
        private static final Api API_INSTANCE = new Api();
    }

    private <S> void createService(Class<S> serviceClass, String baseUrl, OkHttpClient client) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
        S service = retrofit.create(serviceClass);
        mServiceMap.put(serviceClass.getName(), service);
    }
}
```
# 开始使用
首先你需要下载一份源码：
[https://github.com/kuangxiaoguo0123/util-library](https://github.com/kuangxiaoguo0123/util-library)

## 目录结构
![](https://github.com/kuangxiaoguo0123/util-project/blob/master/screenshots/catalog.png)

# 引用方式

## 1. library

-  Import Module
![](https://github.com/kuangxiaoguo0123/util-project/blob/master/screenshots/import_module.png)
![](https://github.com/kuangxiaoguo0123/util-project/blob/master/screenshots/impore_module2.png)
- 添加依赖
在build.gradle文件中添加依赖
```
implementation project(':apilibrary')
```

## 2. aar文件
- 编译apilibrary库生成aar文件
![](https://github.com/kuangxiaoguo0123/util-project/blob/master/screenshots/get_aar01.png)

![](https://github.com/kuangxiaoguo0123/util-project/blob/master/screenshots/get_aar02.png)

- 复制aar文件到项目libs文件夹下
![](https://github.com/kuangxiaoguo0123/util-project/blob/master/screenshots/copy_aar.png)

- 在build.gradle文件中添加依赖
根目录下添加
```
repositories {
    flatDir {
        dirs 'libs'
    }
}
```
dependencies下添加
```
implementation(name: 'apilibrary-release', ext: 'aar')
```
# 开始使用

- 添加依赖  
>这里我们需要在项目的app目录下的build.gradle文件中引入apilibrary中使用到的库，比如Retrofit、RxJava。
```
implementation 'com.squareup.retrofit2:retrofit:2.3.0'
implementation 'com.squareup.retrofit2:converter-gson:2.3.0'
implementation 'com.squareup.retrofit2:adapter-rxjava2:2.2.0'
implementation 'io.reactivex.rxjava2:rxjava:2.1.3'
implementation 'io.reactivex.rxjava2:rxandroid:2.0.1'
```
-  创建ApiService
> 创建ApiService用于存放我们的请求方法。
```
public interface ApiService {
	//模拟方法，根据实际网络请求添加
	@GET("category/query")
	Observable<DataResponse> getData(@Query("key") String key);
}
```
- Application中初始化apilibrary
>这里base url和client根据项目需求定义。
```
public class ProjectApplication extends Application {

    private static final String BASE_URL = "http://base.url/";
    private OkHttpClient client = new OkHttpClient.Builder()
            .build();

    @Override
    public void onCreate() {
        super.onCreate();
        Api.getInstance().init(ApiService.class, BASE_URL, client);
    }
}
```
# 使用MVP模式加载数据
apilibrary中同时封装了BaseView和BasePresenter，所以我们可以用MVP模式实现数据请求。

- 创建DataView继承BaseView
```
public interface DataView extends BaseView<DataResponse> {
}
```
- 创建DataPresenter继承BasePresenter

```
public class DataPresenter implements BasePresenter<DataView> {

    private DataView mView;

    @Override
    public void attachView(DataView dataView) {
        mView = dataView;
    }

    @Override
    public void detachView() {
        mView = null;
    }
}
```
- Activity实现DataView接口并初始化DataPresenter
```
public class MainActivity extends AppCompatActivity implements DataView {

    private DataPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new DataPresenter();
        mPresenter.attachView(this);
    }

    @Override
    public Context getMyContext() {
        return this;
    }

    @Override
    public void onError(Throwable throwable) {
        //异常错误处理
    }

    @Override
    public void showProgress() {
        //显示加载dialog
    }

    @Override
    public void dismissProgress() {
        //消失dialog
    }

    @Override
    public void setData(DataResponse dataResponse) {
        //展示数据
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Activity销毁时取消网络请求
        if (mPresenter != null) {
            mPresenter.detachView();
        }
    }
}
```
- DataPresenter中请求数据
```
public class DataPresenter implements BasePresenter<DataView> {

    private DataView mView;
    private Disposable mDataDisposable;

    @Override
    public void attachView(DataView dataView) {
        mView = dataView;
    }

    @Override
    public void detachView() {
        mView = null;
        if (mDataDisposable != null) {
            mDataDisposable.dispose();
        }
    }

    public void getData() {
        if (mView == null) {
            return;
        }
        mView.showProgress();
        Api.getInstance().getService(ApiService.class)
                .getData("key")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<DataResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDataDisposable = d;
                    }

                    @Override
                    public void onNext(DataResponse dataResponse) {
                        if (mView != null) {
                            //数据加载回调
                            mView.setData(dataResponse);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mView != null) {
                            mView.onError(e);
                            mView.dismissProgress();
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mDataDisposable != null) {
                            mDataDisposable.dispose();
                        }
                        if (mView != null) {
                            mView.dismissProgress();
                        }
                    }
                });

    }
}
```
- MainActivity中请求数据
```
mPresenter.getData();
```

# 项目混淆
如果你的项目需要混淆处理的话，需要在proguard-rules.pro文件中添加以下配置。
```
-keep class com.kxg.apilibrary.**{*;}

# RxJava and RxAndroid
-dontwarn sun.misc.**

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

# OkHttp3
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature

# Gson
-keep class com.google.gson.stream.** { *; }
-keepattributes EnclosingMethod
```
# 多base url支持
- 如果你的项目有多个base url的话，apilibrary也同样支持，只不过你需要再创建一个ApiService2。
```
public interface ApiService2 {
}
```
- 同样的在Application中初始化，其请求使用步骤和上面介绍的ApiService是一样的。
```
public class ProjectApplication extends Application {

    private static final String BASE_URL = "http://base.url";
    private OkHttpClient client = new OkHttpClient.Builder()
            .build();

    private static final String ANOTHER_BASE_URL = "http://another.base.url";

    @Override
    public void onCreate() {
        super.onCreate();
        Api.getInstance().init(ApiService.class, BASE_URL, client);
        
        Api.getInstance().init(ApiService2.class, ANOTHER_BASE_URL, client);
    }
}
```
# 总结
以上就是关于Retrofit的相关封装，并结合MVP模式演示了请求示例，希望你可以用到你的项目中，如集成遇到问题的话可留言或邮件kuangxiaoguo@163.com。

# util-project源码下载
[https://github.com/kuangxiaoguo0123/util-project](https://github.com/kuangxiaoguo0123/util-project)
另外，此项目还包含其他的一些工具库，比如util-library，并会持续更新，希望大家多多star，Thanks ！

# More information
[https://blog.csdn.net/kuangxiaoguo0123/article/details/79693928](https://blog.csdn.net/kuangxiaoguo0123/article/details/79693928)
