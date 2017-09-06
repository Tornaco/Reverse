package github.tornaco.reverse.common;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;

import org.newstand.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import github.tornaco.reverse.R;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/9/6.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
public class VideoLoader {
    public interface Callback {
        void onComplete(List<Video> videos);
    }

    @Getter
    private Context context;

    public void loadAsync(final Callback callback, final boolean fromOutput) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                callback.onComplete(loadFromMediaStore(fromOutput));
            }
        });
    }

    public List<Video> loadFromMediaStore(final boolean fromOutput) {
        final List<Video> records = new ArrayList<>();
        consumeCursor(createCursor(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null,
                null, MediaStore.Video.Media.DATE_MODIFIED + " desc"), new Consumer<Cursor>() {
            @Override
            public void accept(@NonNull Cursor cursor) {
                Video record = recordVideoFromCursor(cursor, fromOutput);
                if (record != null) records.add(record);
            }
        });
        return records;
    }

    private Cursor createCursor(@NonNull Uri uri,
                                @Nullable String[] projection, @Nullable String selection,
                                @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        ContentResolver cr = getContext().getContentResolver();
        return cr.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    private void consumeCursor(Cursor cursor, Consumer<Cursor> cursorConsumer) {
        if (cursor != null && cursor.getCount() > 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                    .moveToNext()) {
                cursorConsumer.accept(cursor);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private Video recordVideoFromCursor(Cursor cursor, boolean fromPutput) {

        Video record = new Video();

        int id = cursor.getInt(cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media._ID));
        String title = cursor
                .getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
        String album = cursor
                .getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));
        String artist = cursor
                .getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
        String displayName = cursor
                .getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
        String mimeType = cursor
                .getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
        String path = cursor
                .getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
        long duration = cursor
                .getInt(cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
        long size = cursor
                .getLong(cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));

        File reversedDir = new File(SettingsProvider.get().getString(SettingsProvider.Key.OUTPUT_DIR));

        if (fromPutput && !reversedDir.equals(new File(path).getParentFile())) {
            Logger.i("Not fromPutput: %s", path);
            return null;
        }

        record.setPath(path);
        record.setName(new File(path).getName()); // Fix file name.
        record.setDuration(formatDuration(context, duration));
        return record;
    }

    private String formatDuration(Context c, long time) {
        return c.getString(R.string.video_length,
                DateUtils.formatElapsedTime(time / 1000));
    }
}
