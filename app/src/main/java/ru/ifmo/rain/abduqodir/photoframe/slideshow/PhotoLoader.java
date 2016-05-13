package ru.ifmo.rain.abduqodir.photoframe.slideshow;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.DownloadListener;
import com.yandex.disk.client.ListItem;
import com.yandex.disk.client.TransportClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ru.ifmo.rain.abduqodir.photoframe.Utils;

public class PhotoLoader extends AsyncTaskLoader <File> {

  @NonNull
  private Context context;
  @NonNull
  Credentials credentials;
  @NonNull
  ListItem photoItem;

  public PhotoLoader(@NonNull Context context, @NonNull Credentials credentials,
                     @NonNull ListItem photoItem) {
    super(context);
    this.context = context;
    this.credentials = credentials;
    this.photoItem = photoItem;
  }

  @Override
  protected void onStartLoading() {
    forceLoad();
  }

  @Nullable
  @Override
  public File loadInBackground() {
    long startingTime = System.currentTimeMillis();
    TransportClient transportClient = null;
    File savingFile
        = Utils.getSavingFile(context, photoItem.getContentLength(), photoItem.getFullPath());

    if (savingFile == null) {
      return null;
    }

    try {
      transportClient = TransportClient.getInstance(context, credentials);
      transportClient
          .downloadPreview(TransportClient
                  .makePreviewPath(TransportClient
                      .encodeURL(photoItem.getFullPath()), TransportClient.PreviewSize.XXXL),
          new DownloadListener() {
        @Override
        public OutputStream getOutputStream(boolean append) throws IOException {
          return new FileOutputStream(savingFile);
        }
      });
    } catch (Exception e) {
      savingFile.delete();
      return null;
    } finally {
      if (transportClient != null) {
        transportClient.shutdown();
      }
    }
    long loadingTime = System.currentTimeMillis() - startingTime;
    if (loadingTime < SlideshowActivity.SLIDE_CHANGE_DELAY) {
      try {
        Thread.sleep(SlideshowActivity.SLIDE_CHANGE_DELAY - loadingTime);
      } catch (InterruptedException e) {
        savingFile.delete();
        return null;
      }
    }
    return savingFile;
  }
}
