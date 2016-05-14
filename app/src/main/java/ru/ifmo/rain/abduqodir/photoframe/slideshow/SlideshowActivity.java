package ru.ifmo.rain.abduqodir.photoframe.slideshow;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

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
import ru.ifmo.rain.abduqodir.photoframe.Utils;
import ru.ifmo.rain.abduqodir.photoframe.directory_content.DirectoryContentActivity;
import ru.ifmo.rain.abduqodir.photoframe.directory_content.DirectoryContentLoader;
import ru.ifmo.rain.abduqodir.photoframe.directory_content.NoticeDialogFragment;

public class SlideshowActivity extends AppCompatActivity
    implements NoticeDialogFragment.NoticeDialogListener {

  public static final int SLIDE_CHANGE_DELAY = 5000;

  private static final boolean AUTO_HIDE = true;
  private static final int AUTO_HIDE_DELAY_MILLIS = 5000;
  private static final int UI_ANIMATION_DELAY = 300;

  private static final int DIRECTORY_CONTENT_LOADER = 0;
  private static final int PHOTO_LOADER = 1;

  private final Handler taskHandler = new Handler();

  private ImageView contentView;
  private ProgressBar progressBar;
  private Credentials credentials;
  private String currentDirectory;
  private Queue<ListItem> imageItems;
  private Queue<String> collectionItemPaths;
  private boolean isActionBarVisible;
  private boolean isSlidesFinished;
  private boolean isContentLoadingFinished;
  private boolean isAnySlidesShowed;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_slideshow);

    if (savedInstanceState == null) {
      credentials = getIntent().getParcelableExtra(DirectoryContentActivity.CREDENTIALS);
      currentDirectory = getIntent().getStringExtra(DirectoryContentActivity.DIRECTORY);
    } else {
      credentials = savedInstanceState.getParcelable(DirectoryContentActivity.CREDENTIALS);
      currentDirectory = savedInstanceState.getString(DirectoryContentActivity.DIRECTORY);
    }

    imageItems = new ArrayDeque<>();
    collectionItemPaths = new ArrayDeque<>();
    collectionItemPaths.add(currentDirectory);

    isActionBarVisible = true;
    isSlidesFinished = true;
    isContentLoadingFinished = false;
    isAnySlidesShowed = false;

    contentView = (ImageView) findViewById(R.id.fullscreenContent);
    progressBar = (ProgressBar) findViewById(R.id.progressBar);

    progressBar.setVisibility(View.VISIBLE);
    contentView.setOnClickListener(view -> toggle());

    getLoaderManager().restartLoader(DIRECTORY_CONTENT_LOADER, null, contentLoaderListener);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    delayedHide(200);
  }

  @Override
  protected void onSaveInstanceState(Bundle savedInstanceState) {
    super.onSaveInstanceState(savedInstanceState);

    savedInstanceState.putParcelable(DirectoryContentActivity.CREDENTIALS, credentials);
    savedInstanceState.putString(DirectoryContentActivity.DIRECTORY, currentDirectory);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      Utils.navigateBack(this, credentials, currentDirectory);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onDialogPositiveClick(DialogFragment dialog, Bundle args) {
    dialog.dismiss();
    recreate();
  }

  @Override
  public void onDialogNegativeClick(DialogFragment dialog, Bundle args) {
    Utils.navigateBack(this, credentials, currentDirectory);
  }

  @Override
  public void onDialogNeutralClick(DialogFragment dialog, Bundle args) {
    Utils.showDirectoryContent(this, credentials, currentDirectory, true);
  }


  private final LoaderManager.LoaderCallbacks<File> fileLoaderListener =
      new LoaderManager.LoaderCallbacks<File>() {
        @Override
        public Loader<File> onCreateLoader(int id, @NonNull Bundle args) {
          ListItem next = imageItems.poll();
          return new PhotoLoader(SlideshowActivity.this, credentials, next);
        }

        @Override
        public void onLoadFinished(Loader<File> loader, @Nullable File file) {
          if (file != null) {
            progressBar.setVisibility(View.GONE);
            isAnySlidesShowed = true;


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
                    model.delete();
                    return true;
                  }

                  @Override
                  public boolean onResourceReady(GlideDrawable resource, File model,
                                                 Target<GlideDrawable> target,
                                                 boolean isFromMemoryCache,
                                                 boolean isFirstResource) {
                    model.delete();
                    return false;
                  }
                })
                .into(contentView);
          }

          isSlidesFinished = imageItems.isEmpty();

          if (!isSlidesFinished) {
            getLoaderManager().restartLoader(PHOTO_LOADER, null, this);
          } else {
            checkFinish();
          }
        }

        @Override
        public void onLoaderReset(Loader<File> loader) {
        }
      };


  private final LoaderManager.LoaderCallbacks<List<ListItem>> contentLoaderListener =
      new LoaderManager.LoaderCallbacks<List<ListItem>>() {

        @Override
        public Loader<List<ListItem>> onCreateLoader(int id, Bundle args) {
          return new DirectoryContentLoader(SlideshowActivity.this, credentials, collectionItemPaths.poll());
        }

        @Override
        public void onLoadFinished(Loader<List<ListItem>> loader, List<ListItem> data) {
          for (ListItem item : data) {
            if (Utils.isSupportedByImageView(item)) {
              imageItems.add(item);
            } else if (item.isCollection()) {
              collectionItemPaths.add(item.getFullPath());
            }
          }

          if (isSlidesFinished && !imageItems.isEmpty()) {
            isSlidesFinished = false;
            getLoaderManager().restartLoader(PHOTO_LOADER, null, fileLoaderListener);
          }

          if (!collectionItemPaths.isEmpty()) {
            getLoaderManager().restartLoader(DIRECTORY_CONTENT_LOADER, null, this);
          } else {
            isContentLoadingFinished = true;
            checkFinish();
          }
        }

        @Override
        public void onLoaderReset(Loader<List<ListItem>> loader) {
        }
      };

  private void checkFinish() {
    if (isContentLoadingFinished && isSlidesFinished) {
      if (isAnySlidesShowed) {
        taskHandler.postDelayed(() -> {
          try {
            Utils.showDialog(this, R.string.dialog_slides_finished,
                R.string.dialog_exit, R.string.dialog_restart, R.string.dialog_show_content, null);
          } catch (Exception e) {
            // the activity may be destroyed after SLIDE_CHANGE_DELAY
          }
        }, SLIDE_CHANGE_DELAY);
      } else {
        progressBar.setVisibility(View.GONE);
        Utils.showUnpositiveDialog(this, R.string.dialog_no_photos, R.string.dialog_exit,
            R.string.dialog_show_content, null);
      }
    }
  }




  private final Runnable showPart2Runnable = () -> {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.show();
    }
  };

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

  private final Runnable hideRunnable = () -> {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }
    isActionBarVisible = false;

    taskHandler.removeCallbacks(showPart2Runnable);
    taskHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY);
  };

  private void toggle() {
    if (isActionBarVisible) {
      delayedHide(0);
    } else {
      show();
    }
  }

  @SuppressLint("InlinedApi")
  private void show() {
    contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    isActionBarVisible = true;

    taskHandler.removeCallbacks(hidePart2Runnable);
    taskHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY);

    if (AUTO_HIDE) {
      delayedHide(AUTO_HIDE_DELAY_MILLIS);
    }
  }

  private void delayedHide(int delayMillis) {
    taskHandler.removeCallbacks(hideRunnable);
    taskHandler.postDelayed(hideRunnable, delayMillis);
  }
}
