package github.tornaco.reverse.common;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.google.common.io.Files;

import org.newstand.logger.Logger;

import java.io.File;

/**
 * Created by guohao4 on 2017/9/6.
 * Email: Tornaco@163.com
 */

public class Reverser {

    public static void reverseAsync(final Context context, final String from, final String to,
                                    final FFmpegExecuteResponseHandler handler) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "reverser");
                wakeLock.acquire();
                try {
                    Files.createParentDirs(new File(to));
                    // -i inputfile.mp4 -vf reverse reversed.mp4
                    // -i inputfile.mp4 -vf reverse -af areverse reversed.mp4
                    boolean audio = SettingsProvider.get().getBoolean(SettingsProvider.Key.REVERSE_AUDIO);
                    String command = !audio
                            ? String.format("-y -i %s -vf reverse %s", from, to)
                            : String.format("-y -i %s -vf reverse -af areverse %s", from, to);
                    Logger.i("command: %s", command);
                    String[] commands = command.split(" ");
                    FFmpeg.getInstance(context)
                            .execute(commands, handler);
                } catch (Throwable e) {
                    handler.onFailure(e.getLocalizedMessage());
                } finally {
                    wakeLock.release();
                }
            }
        });
    }
}
