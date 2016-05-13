package ru.ifmo.rain.abduqodir.photoframe;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;

import java.io.File;

public class Utils {

  public static final String DATE_FORMAT = "M/d/yyyy hh:mm";
  private static final int STORAGE_AVAILABILITY_CONST = 4096;

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
}
