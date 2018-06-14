package testing.testing.testpicasso;

import android.app.Application;
import android.content.Context;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class CustomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        setupPicasso();
    }

    private void setupPicasso() {
        final Context context = this;

        File cacheDir = new File(context.getCacheDir(), "image-cache");
        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .cache(new Cache(cacheDir, 128 * 1024 * 1024))
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        return chain.proceed(chain.request().newBuilder()
                                .header("User-Agent", getUserAgent(context))
                                .build());
                    }
                })
                .build();


        Picasso picassoInstance;

        picassoInstance = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(client))
                .build();
        picassoInstance.setLoggingEnabled(true);
        picassoInstance.setIndicatorsEnabled(true);

        Picasso.setSingletonInstance(picassoInstance);
    }

    public String getUserAgent(Context context) {
        if (context == null) {
            return null;
        }
        String userAgent = System.getProperty("http.agent");
        return userAgent;
    }
}
