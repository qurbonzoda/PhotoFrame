package ru.ifmo.rain.abduqodir.photoframe.slideshow;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;

import java.io.File;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import ru.ifmo.rain.abduqodir.photoframe.R;
import ru.ifmo.rain.abduqodir.photoframe.directory_content.DirectoryContentActivity;
import ru.ifmo.rain.abduqodir.photoframe.directory_content.DirectoryContentLoader;

public class SlideshowActivity extends AppCompatActivity {

  public static final int SLIDE_CHANGE_DELAY = 5000;

  private static final boolean AUTO_HIDE = true;
  private static final int AUTO_HIDE_DELAY_MILLIS = 5000;
  private static final int UI_ANIMATION_DELAY = 300;
  private static final String LAST_SHOWN_SLIDE = "last_shown_slide";

  private ImageView contentView;
  private ProgressBar progressBar;
  private boolean isVisible;
  private Credentials credentials;
  private String currentDirectory;
  private Queue<ListItem> imageItems;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_slideshow);

    isVisible = true;
    contentView = (ImageView) findViewById(R.id.fullscreenContent);
    progressBar = (ProgressBar) findViewById(R.id.progressBar);
    progressBar.setVisibility(View.VISIBLE);

    if (savedInstanceState == null) {
      credentials = getIntent().getParcelableExtra(DirectoryContentActivity.CREDENTIALS);
      currentDirectory = getIntent().getStringExtra(DirectoryContentActivity.DIRECTORY);
    } else {
      credentials = savedInstanceState.getParcelable(DirectoryContentActivity.CREDENTIALS);
      currentDirectory = savedInstanceState.getString(DirectoryContentActivity.DIRECTORY);
    }
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    contentView.setOnClickListener(view -> toggle());
    getLoaderManager().initLoader(0, null, contentLoaderListener);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    delayedHide(200);
  }

  private final LoaderManager.LoaderCallbacks<List<ListItem>> contentLoaderListener =
      new LoaderManager.LoaderCallbacks<List<ListItem>>() {

        @Override
        public Loader<List<ListItem>> onCreateLoader(int id, Bundle args) {
          return new DirectoryContentLoader(contentView.getContext(), credentials, currentDirectory);
        }

        @Override
        public void onLoadFinished(Loader<List<ListItem>> loader, List<ListItem> data) {
          imageItems = new ArrayDeque<>();
          for (ListItem item : data) {
            if ("image".equals(item.getMediaType())) {
              imageItems.add(item);
            }
          }

          if (!imageItems.isEmpty()) {
            Bundle args = new Bundle();
            args.putParcelable(LAST_SHOWN_SLIDE, imageItems.poll());
            getLoaderManager().initLoader(1, args, fileLoaderListener);
          } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(SlideshowActivity.this,
                R.string.no_photos_in_folder, Toast.LENGTH_SHORT).show();
          }
        }

        @Override
        public void onLoaderReset(Loader<List<ListItem>> loader) {
          imageItems = null;
        }
      };

  private final LoaderManager.LoaderCallbacks<File> fileLoaderListener =
      new LoaderManager.LoaderCallbacks<File>() {
        @Override
        public Loader<File> onCreateLoader(int id, @NonNull Bundle args) {
          return new PhotoLoader(contentView.getContext(), credentials,
              (ListItem) args.getParcelable(LAST_SHOWN_SLIDE));
        }

        @Override
        public void onLoadFinished(Loader<File> loader, @Nullable File file) {
          if (file != null) {
            progressBar.setVisibility(View.GONE);

            Glide.with(SlideshowActivity.this)
                .load(file)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .animate(android.R.anim.slide_in_left)
                .fitCenter()
                .listener(new RequestListener<File, GlideDrawable>() {
                  @Override
                  public boolean onException(Exception e, File model, Target<GlideDrawable> target,
                                             boolean isFirstResource) {
                    file.delete();
                    return true;
                  }

                  @Override
                  public boolean onResourceReady(GlideDrawable resource, File model,
                                                 Target<GlideDrawable> target,
                                                 boolean isFromMemoryCache,
                                                 boolean isFirstResource) {
                    file.delete();
                    return false;
                  }
                })
                .into(contentView);
          }

          if (!imageItems.isEmpty()) {
            Bundle args = new Bundle();
            args.putParcelable(LAST_SHOWN_SLIDE, imageItems.poll());
            getLoaderManager().restartLoader(1, args, this);
          } else {
            Toast.makeText(SlideshowActivity.this, R.string.thats_all, Toast.LENGTH_LONG).show();
          }
        }

        @Override
        public void onLoaderReset(Loader<File> loader) {
        }
      };

  @Override
  protected void onSaveInstanceState(Bundle savedInstanceState) {
    super.onSaveInstanceState(savedInstanceState);

    savedInstanceState.putParcelable(DirectoryContentActivity.CREDENTIALS, credentials);
    savedInstanceState.putString(DirectoryContentActivity.DIRECTORY, currentDirectory);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == android.R.id.home) {
      Intent intent = new Intent(this, DirectoryContentActivity.class);
      intent.putExtra(DirectoryContentActivity.CREDENTIALS, credentials);
      int lastDir = currentDirectory.lastIndexOf('/');
      lastDir = lastDir == 0 ? 1 : lastDir;
      intent.putExtra(DirectoryContentActivity.DIRECTORY, currentDirectory.substring(0, lastDir));
      navigateUpTo(intent);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void toggle() {
    if (isVisible) {
      hide();
    } else {
      show();
    }
  }

  private final Handler taskHandler = new Handler();

  private void hide() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }
    isVisible = false;

    taskHandler.removeCallbacks(showPart2Runnable);
    taskHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY);
  }

  private final Runnable hidePart2Runnable = new Runnable() {
    @SuppressLint("InlinedApi")
    @Override
    public void run() {
      contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
          | View.SYSTEM_UI_FLAG_FULLSCREEN
          | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
          | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
  };

  @SuppressLint("InlinedApi")
  private void show() {
    contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    isVisible = true;

    taskHandler.removeCallbacks(hidePart2Runnable);
    taskHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY);

    if (AUTO_HIDE) {
      delayedHide(AUTO_HIDE_DELAY_MILLIS);
    }
  }

  private final Runnable showPart2Runnable = () -> {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.show();
    }
  };

  private void delayedHide(int delayMillis) {
    taskHandler.removeCallbacks(this::hide);
    taskHandler.postDelayed(this::hide, delayMillis);
  }
}
