package testing.testing.testpicasso;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TimingLogger;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.reactivestreams.Publisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private final static String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private List<String> mImageUrls;

    private Spinner mImageUrlSpinner;
    //    private EditText mImageUrlEditText;
    private Button mPicassoGetButton;
    private Button mPicassoIntoButton;
    private Button mPicassoGetAllButton;
    private ImageView mPicassoImageView;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialImageUrls();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        mImageUrlSpinner = root.findViewById(R.id.imageUrlSpinner);

        ArrayAdapter<String> imageUrlSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, mImageUrls);
        mImageUrlSpinner.setAdapter(imageUrlSpinnerAdapter);
//        mImageUrlEditText = root.findViewById(R.id.imageUrlEditText);
        mPicassoGetButton = root.findViewById(R.id.picassoGetButton);
        mPicassoGetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picassoGet(mImageUrlSpinner.getSelectedItem().toString());
            }
        });
        mPicassoIntoButton = root.findViewById(R.id.picassoIntoButton);
        mPicassoIntoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picassoInto(mImageUrlSpinner.getSelectedItem().toString());
            }
        });
        mPicassoGetAllButton = root.findViewById(R.id.picassoGetAllButton);
        mPicassoGetAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picassoGetAll();
            }
        });
        mPicassoImageView = root.findViewById(R.id.picassoImageView);
        return root;
    }

    private void showMessage(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    private void initialImageUrls() {
        mImageUrls = new ArrayList<>();
        mImageUrls.add("https://gdb.voanews.com/5DFB29B1-2249-491A-AC49-059267FC15F9_cx0_cy5_cw0_w800_h450.jpg");
        mImageUrls.add("https://img.buzzfeed.com/buzzfeed-static/static/2018-06/11/9/campaign_images/buzzfeed-prod-web-05/andrew-garfield-dedicated-his-tony-award-to-the-l-2-7407-1528723572-11_dblbig.jpg");
        mImageUrls.add("https://img.buzzfeed.com/buzzfeed-static/static/2018-06/12/20/asset/buzzfeed-prod-web-02/sub-buzz-4037-1528851390-5.png");
        mImageUrls.add("http://www.nasa.gov/sites/default/files/thumbnails/image/nhq201806080012.jpg");
        mImageUrls.add("https://www.nasa.gov/sites/default/files/thumbnails/image/pia22521-16.jpg");
    }

    private void picassoGet(final String imageUrl) {
        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                try {
                    Log.i(LOG_TAG, "RequestCreator.get:" + imageUrl);
                    Picasso.get()
                            .load(imageUrl)
                            .memoryPolicy(MemoryPolicy.NO_STORE)
                            .get();
                    showMessage("picassoGet success");
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.toString());
                    e.printStackTrace();
                    showMessage("picassoGet error: " + e.toString());
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private void picassoInto(String imageUrl) {
        Picasso.get()
                .load(imageUrl)
                .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                .fit()
                .centerCrop()
                .into(mPicassoImageView);
    }

    private void picassoGetAll() {
        Disposable disposable =
                Flowable.just(mImageUrls)
                        .flatMap(Flowable::fromIterable)
                        .observeOn(Schedulers.io())
                        .flatMap(new Function<String, Publisher<String>>() {
                            @Override
                            public Publisher<String> apply(String imageUrl) {
                                return Flowable.create(new FlowableOnSubscribe<String>() {
                                    @Override
                                    public void subscribe(FlowableEmitter<String> emitter) throws Exception {
                                        Log.d(LOG_TAG, "RequestCreator.get - image url:" + imageUrl);

                                        TimingLogger timings = new TimingLogger("Timing", "DownloadImages");
                                        timings.reset();
                                        try {
                                            Bitmap image = getImageByPicasso(imageUrl, timings);
                                            emitter.onNext(imageUrl);
                                            emitter.onComplete();
                                        } catch (Exception ex) {
                                            emitter.onError(ex);
                                        }

                                        timings.dumpToLog();
                                    }
                                }, BackpressureStrategy.BUFFER);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                imageUrl -> Log.d(LOG_TAG, "picassoGetAll - onNext imageUrl: " + imageUrl),
                                throwable -> {
                                    Log.e(LOG_TAG, "picassoGetAll - error:" + Log.getStackTraceString(throwable));
                                    throwable.printStackTrace();
                                },
                                () -> {
                                    Log.d(LOG_TAG, "picassoGetAll - onComplete");
                                    showMessage("get all completed.");
                                });
    }


    private Bitmap getImageByPicasso(String imageUrl, TimingLogger timings) throws Exception {
        Bitmap image = Picasso.get().load(imageUrl)
                .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                .get();

        timings.addSplit("Picasso.get()");
        return image;
    }
}
