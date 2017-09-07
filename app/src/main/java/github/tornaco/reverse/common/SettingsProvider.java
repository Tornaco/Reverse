package github.tornaco.reverse.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.google.common.io.Files;

import java.io.File;
import java.util.Observable;

import lombok.Getter;

/**
 * Created by Tornaco on 2017/7/25.
 * Licensed with Apache.
 */
public class SettingsProvider extends Observable {

    private static SettingsProvider sMe;

    public enum Key {
        REVERSE_AUDIO(true),
        OUTPUT_DIR(Environment.getExternalStorageDirectory().getPath() + File.separator
                + Environment.DIRECTORY_PICTURES + File.separator + "Reversed");

        @Getter
        Object defValue;

        Key(Object defValue) {
            this.defValue = defValue;
        }
    }

    @Getter
    private SharedPreferences pref;

    public static SettingsProvider get() {
        return sMe;
    }

    private SettingsProvider(Context context) {
        this.pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void init(Context context) {
        sMe = new SettingsProvider(context);
    }

    public String toPrefKey(Key key) {
        return key.name().toLowerCase();
    }

    public boolean getBoolean(Key key) {
        return getPref().getBoolean(toPrefKey(key), (Boolean) key.getDefValue());
    }

    public void putBoolean(Key key, boolean value) {
        getPref().edit().putBoolean(toPrefKey(key), value).apply();
        setChanged();
        notifyObservers(key);
    }

    public int getInt(Key key) {
        return getPref().getInt(toPrefKey(key), (Integer) key.getDefValue());
    }

    public void putInt(Key key, int value) {
        getPref().edit().putInt(toPrefKey(key), value).apply();
        setChanged();
        notifyObservers(key);
    }

    public String getString(Key key) {
        return getPref().getString(toPrefKey(key), (String) key.getDefValue());
    }

    public void putString(Key key, String value) {
        getPref().edit().putString(toPrefKey(key), value).apply();
        setChanged();
        notifyObservers(key);
    }

    public static String createOutputPath(String inputPath) {
        String outDir = get().getString(Key.OUTPUT_DIR);
        return outDir
                + File.separator
                + Files.getNameWithoutExtension(inputPath)
                + "_reversed."
                + Files.getFileExtension(inputPath);
    }

    public static final int REQUEST_CODE_FILE_PICKER = 0x100;
}
