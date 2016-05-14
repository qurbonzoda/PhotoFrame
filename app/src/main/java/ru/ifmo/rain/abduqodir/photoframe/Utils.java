package ru.ifmo.rain.abduqodir.photoframe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;

import java.io.File;
import java.util.Locale;

import ru.ifmo.rain.abduqodir.photoframe.directory_content.DirectoryContentActivity;
import ru.ifmo.rain.abduqodir.photoframe.directory_content.NoticeDialogFragment;
import ru.ifmo.rain.abduqodir.photoframe.slideshow.SlideshowActivity;

public class Utils {

  public static final String DATE_FORMAT = "M/d/yyyy hh:mm";
  private static final int STORAGE_AVAILABILITY_CONST = 4096;
  private static final long KILO = 1024;
  private static final long MEGA = KILO * KILO;
  private static final long GIGA = MEGA * KILO;

  public static int getFileResourceId(String mediaType, String contentType) {
    switch (mediaType) {
      case "document":
        switch (contentType) {
          case "application/msword":
            return R.drawable.doc_48;
          case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
            return R.drawable.docx_128;
          case "application/pdf":
            return R.drawable.pdf_48;
          case "image/vnd.djvu":
            return R.drawable.djvu_50;
          case "text/plain":
            return R.drawable.txt_48;
          case "application/vnd.ms-powerpoint":
          case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
            return R.drawable.power_point_48;
          default:
            return R.drawable.document_48;
        }
      case "audio":
        return R.drawable.audio_file_48;
      case "image":
        return R.drawable.image_file_48;
      case "video":
        return R.drawable.video_file_48;
      case "compressed":
        switch (contentType) {
          case "application/zip":
            return R.drawable.zip_48;
          case "application/rar":
            return R.drawable.rar_48;
          default:
            return R.drawable.archive_48;
        }
      default:
        return R.drawable.file_48;
    }
  }

  // Note: The folder names doesn't depend on locale.
  public static int getFolderResourceId(String displayName) {
    switch (displayName) {
      case "Downloads":
      case "Загрузки":
        return R.drawable.downloads_folder_48;
      case "Music":
      case "Музыка":
        return R.drawable.music_folder_48;
      case "Photo":
      case "Фото":
      case "Pictures":
      case "Картинки":
      case "Фотокамера":
        return R.drawable.pictures_folder_48;
      case "Archives":
      case "Архивы":
        return R.drawable.archive_folder_48;
      case "Documents":
      case "Документы":
        return R.drawable.documents_folder_48;
      case "Private":
      case "Личный":
        return R.drawable.user_folder_48;
      default:
        return R.drawable.folder_48;
    }
  }

  public static boolean isSupportedByImageView(ListItem item) {
    String fileName = item.getFullPath();

    int lastDot = fileName.lastIndexOf('.');
    if (lastDot == -1) {
      return false;
    }

    String format = fileName.substring(lastDot).toLowerCase();
    return ".jpg".equals(format) || (".gif".equals(format) || (".png".equals(format)
        || (".bmp".equals(format) || (".webp".equals(format)))));
  }

  @Nullable
  public static File getSavingFile(Context context, long contentLength, String filePath) {
    String fileName = Uri.parse(filePath).getLastPathSegment();
    if (context.getCacheDir().getFreeSpace() > contentLength + STORAGE_AVAILABILITY_CONST) {
      return new File(context.getCacheDir(), fileName);
    }
    if (context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) != null
        && context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        .getFreeSpace() > contentLength + STORAGE_AVAILABILITY_CONST) {
      return new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
    }
    return null;
  }

  public static void showInSlideShow(Context context, Credentials credentials, String directory) {
    Intent intent = new Intent(context, SlideshowActivity.class);
    intent.putExtra(DirectoryContentActivity.CREDENTIALS, credentials);
    intent.putExtra(DirectoryContentActivity.DIRECTORY, directory);
    context.startActivity(intent);
  }

  public static void showDirectoryContent(Context context, Credentials credentials, String path,
                                          boolean isFolderShared) {
    Intent intent = new Intent(context, DirectoryContentActivity.class);
    intent.putExtra(DirectoryContentActivity.CREDENTIALS, credentials);
    intent.putExtra(DirectoryContentActivity.DIRECTORY, path);
    intent.putExtra(DirectoryContentActivity.IS_SHARED, isFolderShared);
    context.startActivity(intent);
  }

  public static String getPreviousDirectory(String directory) {
    int lastDir = directory.lastIndexOf('/');
    lastDir = lastDir == 0 ? 1 : lastDir;
    return directory.substring(0, lastDir);
  }

  public static String getFolderName(String directory) {
    int lastSlash = directory.lastIndexOf('/');
    return directory.substring(lastSlash + 1);
  }

  public static void navigateBack(Activity activity, Credentials credentials, String directory) {
    Intent intent = new Intent(activity, DirectoryContentActivity.class);
    intent.putExtra(DirectoryContentActivity.CREDENTIALS, credentials);
    intent.putExtra(DirectoryContentActivity.DIRECTORY, Utils.getPreviousDirectory(directory));

    activity.navigateUpTo(intent);
  }

  public static String getFileSize(long contentLength) {
    if (contentLength >= GIGA / 10) {
      return String.format(Locale.getDefault(), "%.1f GB", (double) contentLength / GIGA);
    } else if (contentLength >= MEGA / 10) {
      return String.format(Locale.getDefault(), "%.1f MB", (double) contentLength / MEGA);
    } else {
      return String.format(Locale.getDefault(), "%.1f KB", (double) contentLength / KILO);
    }
  }

  public static void showDialog(AppCompatActivity activity, int messageId, int negativeId,
                                int positiveId, int neutralId, Bundle callbackArgs) {
    NoticeDialogFragment.newInstance(activity.getString(messageId),
        activity.getString(negativeId), activity.getString(positiveId),
        activity.getString(neutralId), callbackArgs)
        .show(activity.getSupportFragmentManager(), "NoticeDialogFragment");
  }

  public static void showUnpositiveDialog(AppCompatActivity activity, int messageId, int negativeId,
                                          int neutralId, Bundle callbackArgs) {
    NoticeDialogFragment.newInstance(activity.getString(messageId),
        activity.getString(negativeId), null,
        activity.getString(neutralId), callbackArgs)
        .show(activity.getSupportFragmentManager(), "NoticeDialogFragment");
  }

  public static void showMenuDialog(AppCompatActivity activity, int negativeId, int positiveId,
                                    int neutralId, Bundle callbackArgs) {
    NoticeDialogFragment.newInstance(null, activity.getString(negativeId),
        activity.getString(positiveId), activity.getString(neutralId), callbackArgs)
        .show(activity.getSupportFragmentManager(), "NoticeDialogFragment");
  }
}
