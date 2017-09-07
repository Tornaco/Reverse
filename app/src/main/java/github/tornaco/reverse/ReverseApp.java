package github.tornaco.reverse;

import android.app.Application;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.newstand.logger.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import github.tornaco.reverse.common.FFmpegLoadBinaryResponseHandlerAdapter;
import github.tornaco.reverse.common.SettingsProvider;

/**
 * Created by guohao4 on 2017/9/6.
 * Email: Tornaco@163.com
 */

public class ReverseApp extends Application {

    private AtomicBoolean ffmpegReady;
    private CountDownLatch loadingLatch = new CountDownLatch(1);

    @Override
    public void onCreate() {
        super.onCreate();
        SettingsProvider.init(this);
        try {
            FFmpeg.getInstance(this).loadBinary(new FFmpegLoadBinaryResponseHandlerAdapter() {
                @Override
                public void onFailure() {
                    Toast.makeText(getApplicationContext(), "Fail load bin for ffmpeg", Toast.LENGTH_LONG).show();
                    ffmpegReady = new AtomicBoolean(false);
                    loadingLatch.countDown();
                    Logger.i("FFmpeg, loadBinary: onFailure");
                }

                @Override
                public void onSuccess() {
                    super.onSuccess();
                    ffmpegReady = new AtomicBoolean(true);
                    loadingLatch.countDown();
                    Logger.i("FFmpeg, loadBinary: onSuccess");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            Toast.makeText(this, "Fail load bin for ffmpeg", Toast.LENGTH_LONG).show();
            ffmpegReady = new AtomicBoolean(false);
            loadingLatch.countDown();
        }
    }

    public void waitForLoadComplete() {
        try {
            loadingLatch.await();
        } catch (InterruptedException ignored) {

        }
    }

    public boolean isFfmpegReady() {
        return ffmpegReady != null && ffmpegReady.get();
    }
}
