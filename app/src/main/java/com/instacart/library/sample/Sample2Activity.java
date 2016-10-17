package com.instacart.library.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ismartv.truetime.TrueTime;
import cn.ismartv.truetime.TrueTimeRx;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class Sample2Activity
        extends AppCompatActivity {

    private static final String TAG = Sample2Activity.class.getSimpleName();

    @Bind(R.id.tt_btn_refresh)
    Button refreshBtn;
    @Bind(R.id.tt_time_gmt)
    TextView timeGMT;
    @Bind(R.id.tt_time_pst)
    TextView timePST;
    @Bind(R.id.tt_time_device)
    TextView timeDeviceTime;
    final List<String> ntpHosts = Arrays.asList("http://skytest.tvxio.com/v3_0/YOGA/tos/api/currenttime/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        ButterKnife.bind(this);
        refreshBtn.setEnabled(false);


        Observable.interval(2, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .map(new Func1<Long, Object>() {
                    @Override
                    public Object call(Long aLong) {
                        TrueTimeRx.clearCachedInfo(Sample2Activity.this);
                        TrueTimeRx.build()
                                .withConnectionTimeout(31_428)
                                .withRetryCount(100)
                                .withSharedPreferences(Sample2Activity.this)
                                .withLoggingEnabled(true)
                                .initialize(ntpHosts)
                                .subscribe();
                        return null;
                    }
                })
                .takeUntil(new Func1<Object, Boolean>() {
                    @Override
                    public Boolean call(Object o) {
                        return false;
                    }
                })
                .subscribe();
    }


    @OnClick(R.id.tt_btn_refresh)
    public void onBtnRefresh() {
        if (!TrueTimeRx.isInitialized()) {
            Toast.makeText(this, "Sorry TrueTime not yet initialized.", Toast.LENGTH_SHORT).show();
            return;
        }

        Date trueTime = TrueTime.now();
        Date deviceTime = new Date();

        Log.d("kg",
                String.format(" [trueTime: %d] [devicetime: %d] [drift_sec: %f]",
                        trueTime.getTime(),
                        deviceTime.getTime(),
                        (trueTime.getTime() - deviceTime.getTime()) / 1000F));

        timeGMT.setText(getString(R.string.tt_time_gmt,
                _formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT"))));
        timePST.setText(getString(R.string.tt_time_pst,
                _formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT+08:00"))));
        timeDeviceTime.setText(getString(R.string.tt_time_device,
                _formatDate(deviceTime,
                        "yyyy-MM-dd HH:mm:ss",
                        TimeZone.getTimeZone("GMT+08:00"))));
    }

    private String _formatDate(Date date, String pattern, TimeZone timeZone) {
        DateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
        format.setTimeZone(timeZone);
        return format.format(date);
    }
}
