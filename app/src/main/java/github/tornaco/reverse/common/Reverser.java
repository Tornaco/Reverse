package github.tornaco.reverse.common;

import android.content.Context;
import android.os.AsyncTask;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.google.common.io.Files;

import org.newstand.logger.Logger;

import java.io.File;
import java.io.IOException;

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
                try {
                    Files.createParentDirs(new File(to));
                } catch (IOException e) {
                    handler.onFailure(e.getLocalizedMessage());
                }
                // -i inputfile.mp4 -vf reverse reversed.mp4
                String command = String.format("-y -i %s -vf reverse %s", from, to);
                Logger.i("command: %s", command);
                String[] commands = command.split(" ");
                try {
                    FFmpeg.getInstance(context)
                            .execute(commands, handler);
                } catch (FFmpegCommandAlreadyRunningException e) {
                    handler.onFailure(e.getLocalizedMessage());
                }
            }
        });
    }
}
