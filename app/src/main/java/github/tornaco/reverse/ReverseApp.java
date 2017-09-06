package github.tornaco.reverse;

import android.app.Application;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import github.tornaco.reverse.common.FFmpegLoadBinaryResponseHandlerAdapter;
import github.tornaco.reverse.common.SettingsProvider;

/**
 * Created by guohao4 on 2017/9/6.
 * Email: Tornaco@163.com
 */

public class ReverseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SettingsProvider.init(this);
        try {
            FFmpeg.getInstance(this).loadBinary(new FFmpegLoadBinaryResponseHandlerAdapter() {
                @Override
                public void onFailure() {
                    Toast.makeText(getApplicationContext(), "Fail load bin for ffmpeg", Toast.LENGTH_LONG).show();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            Toast.makeText(this, "Fail load bin for ffmpeg", Toast.LENGTH_LONG).show();
        }
    }
}
