package github.tornaco.reverse;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.newstand.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dev.tornaco.vangogh.Vangogh;
import dev.tornaco.vangogh.display.appliers.ResAnimationApplier;
import dev.tornaco.vangogh.loader.LoaderObserverAdapter;
import github.tornaco.reverse.common.FFmpegExecuteResponseHandlerAdapter;
import github.tornaco.reverse.common.MediaScannerClient;
import github.tornaco.reverse.common.MediaTools;
import github.tornaco.reverse.common.Reverser;
import github.tornaco.reverse.common.SettingsProvider;
import github.tornaco.reverse.common.Video;
import github.tornaco.reverse.common.VideoLoader;
import lombok.Getter;

public class MyRevActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERM = 0x100;


    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_template_with_fab);
        setupView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requestPermission()) {
            startLoading();
        }
    }

    @Getter
    class VideoViewHolder extends RecyclerView.ViewHolder {

        private TextView titleView, descriptionView;
        private ImageView imageView;

        VideoViewHolder(View itemView) {
            super(itemView);
            titleView = (TextView) itemView.findViewById(android.R.id.title);
            descriptionView = (TextView) itemView.findViewById(android.R.id.text1);
            imageView = (ImageView) itemView.findViewById(R.id.thumb);
        }
    }


    public boolean requestPermission() {
        String[] perms = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        boolean allGrant = true;
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                allGrant = false;
            }
        }
        if (allGrant) return true;
        ActivityCompat.requestPermissions(this, perms, REQUEST_CODE_PERM);
        return false;
    }

    public void setupView() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setTitle(R.string.my_reverse);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.polluted_waves));
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startLoading();
            }
        });
        setupAdapter();
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), PickerActivity.class));
            }
        });
    }

    private void startLoading() {
        swipeRefreshLayout.setRefreshing(true);
        new VideoLoader(this).loadAsync(new VideoLoader.Callback() {
            @Override
            public void onComplete(final List<Video> videos) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.update(videos);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int code : grantResults) {
            if (code != PackageManager.PERMISSION_GRANTED) {
                finish();
                return;
            }
        }
        startLoading();
    }

    protected void setupAdapter() {
        recyclerView.setHasFixedSize(true);
        setupLayoutManager();
        adapter = new Adapter();
        recyclerView.setAdapter(adapter);

    }

    protected void setupLayoutManager() {
        recyclerView.setLayoutManager(getLayoutManager());
    }

    protected RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    }

    private class Adapter extends RecyclerView.Adapter<VideoViewHolder> {

        private final List<Video> data;

        public Adapter(List<Video> data) {
            this.data = data;
        }

        public Adapter() {
            this(new ArrayList<Video>());
        }

        public void update(List<Video> data) {
            this.data.clear();
            this.data.addAll(data);
            notifyDataSetChanged();
        }

        public void remove(int position) {
            this.data.remove(position);
            notifyItemRemoved(position);
        }

        @Override
        public VideoViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(MyRevActivity.this).inflate(R.layout.simple_card_item, parent, false);
            return new VideoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final VideoViewHolder holder, int position) {
            final Video video = data.get(position);
            holder.getTitleView().setText(video.getName());
            // holder.getDescriptionView().setText(getString(R.string.video_path, video.getPath()));

            Vangogh.with(getApplicationContext())
                    .load(video.getPath())
                    .applier(new ResAnimationApplier(getApplicationContext(),
                            R.anim.grow_fade_in))
                    .placeHolder(0)
                    .skipMemoryCache(true)
                    .fallback(R.drawable.aio_image_fail)
                    .observer(new LoaderObserverAdapter())
                    .into(holder.getImageView());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(MyRevActivity.this, holder.getImageView());
                    popupMenu.inflate(R.menu.menu_list_item_actions);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.action_reverse:
                                    onRequestReverse(video);
                                    break;
                                case R.id.action_view:
                                    startActivity(MediaTools.buildOpenIntent(MyRevActivity.this, new File(video.getPath())));
                                    break;
                                case R.id.action_share:
                                    startActivity(MediaTools.buildSharedIntent(MyRevActivity.this, new File(video.getPath())));
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

    }

    private void onRequestReverse(Video video) {

        final String toPath = SettingsProvider.createOutputPath(video.getPath());

        final ProgressDialog p = new ProgressDialog(MyRevActivity.this);
        p.setTitle(R.string.action_reverse);
        p.setCancelable(false);
        p.setIndeterminate(true);

        Reverser.reverseAsync(getApplicationContext(), video.getPath(), toPath,
                new FFmpegExecuteResponseHandlerAdapter() {
                    @Override
                    public void onProgress(final String message) {
                        super.onProgress(message);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                p.setMessage(message);
                            }
                        });
                    }

                    @Override
                    public void onSuccess(String message) {
                        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        p.setMessage(getString(R.string.updating_media_store));
                                    }
                                });
                                MediaScannerClient.scanSync(getApplicationContext(), toPath);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Snackbar.make(recyclerView, getString(R.string.result_reverse_ok),
                                                Snackbar.LENGTH_INDEFINITE)
                                                .setAction(R.string.more, new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        onVideoOptions(toPath);
                                                    }
                                                })
                                                .show();
                                    }
                                });
                            }
                        });

                    }

                    @Override
                    public void onFailure(final String message) {
                        Logger.e(message);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(recyclerView, getString(R.string.result_reverse_fail),
                                        Snackbar.LENGTH_INDEFINITE).show();
                            }
                        });
                    }

                    @Override
                    public void onStart() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                p.show();
                            }
                        });
                    }

                    @Override
                    public void onFinish() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                p.dismiss();
                            }
                        });
                    }
                }
        );
    }

    private void onVideoOptions(final String path) {
        String[] items = getResources().getStringArray(R.array.more_options);
        new AlertDialog.Builder(MyRevActivity.this)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onVideoOpSelect(path, i);
                    }
                })
                .setCancelable(true)
                .setPositiveButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    private void onVideoOpSelect(String path, int i) {
        switch (i) {
            case 0:
                startActivity(MediaTools.buildOpenIntent(this, new File(path)));
                break;
            case 1:
                startActivity(MediaTools.buildSharedIntent(this, new File(path)));
                break;
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
