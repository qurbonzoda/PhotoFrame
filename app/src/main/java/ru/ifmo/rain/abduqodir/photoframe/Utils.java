package ru.ifmo.rain.abduqodir.photoframe;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;

import java.io.File;
import java.util.Locale;

import ru.ifmo.rain.abduqodir.photoframe.directory_content.DirectoryContentActivity;
import ru.ifmo.rain.abduqodir.photoframe.slideshow.SlideshowActivity;

public class Utils {

  public static final String DATE_FORMAT = "M/d/yyyy hh:mm";
  private static final int STORAGE_AVAILABILITY_CONST = 4096;
  private static final long KILO = 1024;
  private static final long MEGA = KILO * KILO;
  private static final long GIGA = MEGA * KILO;
  private static final char PATH_SEPARATOR = '/';

  public static int getFileResourceId(@NonNull String mediaType, @NonNull String contentType) {
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
  public static int getFolderResourceId(@NonNull String displayName) {
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

  public static boolean isSupportedByImageView(@NonNull ListItem item) {
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
  public static File getSavingFile(@NonNull Context context, long contentLength,
                                   @NonNull String filePath) {
    String fileName = Uri.parse(filePath).getLastPathSegment();
    if (fileName == null) {
      return null;
    }
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

  public static void showInSlideShow(@NonNull Context context, @NonNull Credentials credentials,
                                     @NonNull String directory) {
    Intent intent = new Intent(context, SlideshowActivity.class);
    intent.putExtra(DirectoryContentActivity.CREDENTIALS, credentials);
    intent.putExtra(DirectoryContentActivity.DIRECTORY, directory);
    context.startActivity(intent);
  }

  public static void showDirectoryContent(@NonNull Context context, @NonNull Credentials credentials,
                                          @NonNull String path) {
    Intent intent = new Intent(context, DirectoryContentActivity.class);
    intent.putExtra(DirectoryContentActivity.CREDENTIALS, credentials);
    intent.putExtra(DirectoryContentActivity.DIRECTORY, path);
    context.startActivity(intent);
  }

  @NonNull
  public static String getParentDirectory(@NonNull String directory) {
    int lastDir = directory.lastIndexOf(PATH_SEPARATOR);
    lastDir = lastDir == 0 ? 1 : lastDir;
    return directory.substring(0, lastDir);
  }

  @NonNull
  public static String getFolderName(@NonNull String directory) {
    int lastSlash = directory.lastIndexOf(PATH_SEPARATOR);
    if (lastSlash == -1 || lastSlash == directory.length() - 1) {
      return DirectoryContentActivity.ROOT_DIRECTORY;
    }
    return directory.substring(lastSlash + 1);
  }

  public static void navigateTo(@NonNull Activity activity, @NonNull Credentials credentials,
                                @NonNull String directory) {
    Intent intent = new Intent(activity, DirectoryContentActivity.class);
    intent.putExtra(DirectoryContentActivity.CREDENTIALS, credentials);
    intent.putExtra(DirectoryContentActivity.DIRECTORY, directory);

    activity.navigateUpTo(intent);
  }

  @NonNull
  public static String getFileSize(long contentLength) {
    if (contentLength >= GIGA / 10) {
      return String.format(Locale.getDefault(), "%.1f GB", (double) contentLength / GIGA);
    } else if (contentLength >= MEGA / 10) {
      return String.format(Locale.getDefault(), "%.1f MB", (double) contentLength / MEGA);
    } else {
      return String.format(Locale.getDefault(), "%.1f KB", (double) contentLength / KILO);
    }
  }

  public static void showNeutralDialog(@NonNull AppCompatActivity activity, int messageId,
                                       int neutralId) {
    NoticeDialogFragment.newInstance(activity.getString(messageId),
        null, null, activity.getString(neutralId), null)
        .show(activity.getSupportFragmentManager(), "NoticeDialogFragment");
  }

  public static void showDialog(@NonNull AppCompatActivity activity, int messageId, int negativeId,
                                int neutralId) {
    NoticeDialogFragment.newInstance(activity.getString(messageId),
        activity.getString(negativeId), null, activity.getString(neutralId), null)
        .show(activity.getSupportFragmentManager(), "NoticeDialogFragment");
  }

  public static void showMenuDialog(@NonNull AppCompatActivity activity, int negativeId, int positiveId,
                                    int neutralId, @NonNull Bundle callbackArgs) {
    NoticeDialogFragment.newInstance(null, activity.getString(negativeId),
        activity.getString(positiveId), activity.getString(neutralId), callbackArgs)
        .show(activity.getSupportFragmentManager(), "NoticeDialogFragment");
  }


  public static class NoticeDialogFragment extends DialogFragment {

    public static final String MESSAGE = "message";
    public static final String NEGATIVE_BUTTON = "negative_button";
    public static final String POSITIVE_BUTTON = "positive_button";
    public static final String NEUTRAL_BUTTON = "neutral_button";
    public static final String CALLBACK_ARGS = "callback_args";

    @NonNull
    public static NoticeDialogFragment newInstance(@Nullable String message,
                                                   @Nullable String negativeButton,
                                                   @Nullable String positiveButton,
                                                   @Nullable String neutralButton,
                                                   @Nullable Bundle callbackArgs) {
      Bundle bundle = new Bundle();
      bundle.putString(MESSAGE, message);
      bundle.putString(NEGATIVE_BUTTON, negativeButton);
      bundle.putString(POSITIVE_BUTTON, positiveButton);
      bundle.putString(NEUTRAL_BUTTON, neutralButton);
      bundle.putBundle(CALLBACK_ARGS, callbackArgs);

      NoticeDialogFragment dialog = new NoticeDialogFragment();
      dialog.setArguments(bundle);

      return dialog;
    }

    public interface NoticeDialogListener {
      void onDialogPositiveClick(DialogFragment dialog, Bundle args);

      void onDialogNegativeClick(DialogFragment dialog, Bundle args);

      void onDialogNeutralClick(DialogFragment dialog, Bundle args);
    }

    NoticeDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
      super.onAttach(activity);
      try {
        mListener = (NoticeDialogListener) activity;
      } catch (ClassCastException e) {
        throw new ClassCastException(activity.toString()
            + " must implement NoticeDialogListener");
      }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

      String message = getArguments().getString(MESSAGE);
      String negativeButton = getArguments().getString(NEGATIVE_BUTTON);
      String positiveButton = getArguments().getString(POSITIVE_BUTTON);
      String neutralButton = getArguments().getString(NEUTRAL_BUTTON);
      Bundle callbackArgs = getArguments().getBundle(CALLBACK_ARGS);

      builder.setMessage(message)
          .setPositiveButton(positiveButton,
              (dialog, id) -> mListener.onDialogPositiveClick(NoticeDialogFragment.this, callbackArgs))
          .setNegativeButton(negativeButton,
              (dialog, id) -> mListener.onDialogNegativeClick(NoticeDialogFragment.this, callbackArgs))
          .setNeutralButton(neutralButton,
              (dialog, id) -> mListener.onDialogNeutralClick(NoticeDialogFragment.this, callbackArgs));

      return builder.create();

    }
  }
}
