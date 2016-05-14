package ru.ifmo.rain.abduqodir.photoframe.directory_content;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.ifmo.rain.abduqodir.photoframe.R;
import ru.ifmo.rain.abduqodir.photoframe.Utils;

public class DirectoryContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private final static int TYPE_FOLDER = 0;
  private final static int TYPE_FILE = 1;

  @NonNull
  private final List<ListItem> listItems;
  @NonNull
  private final Credentials credentials;
  @NonNull
  private final DirectoryContentActivity activity;
  private final boolean isContentShared;

  public DirectoryContentAdapter(@NonNull DirectoryContentActivity activity,
                                 @NonNull List<ListItem> listItems,
                                 @NonNull Credentials credentials, boolean isContentShared) {
    this.activity = activity;
    this.listItems = listItems;
    this.credentials = credentials;
    this.isContentShared = isContentShared;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v;
    if (viewType == TYPE_FOLDER) {
      v = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_item, parent, false);
      return new FolderItemHolder(v, credentials, isContentShared, activity);
    } else if (viewType == TYPE_FILE) {
      v = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item, parent, false);
      return new FileItemHolder(v, isContentShared);
    } else {
      throw new IllegalStateException("unknown type: " + viewType);
    }
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    if (getItemViewType(position) == TYPE_FOLDER) {
      ((FolderItemHolder) holder).bind(listItems.get(position));
    } else {
      ((FileItemHolder) holder).bind(listItems.get(position));
    }
  }

  @Override
  public int getItemCount() {
    return listItems.size();
  }

  @Override
  public int getItemViewType(int position) {
    return listItems.get(position).isCollection() ? TYPE_FOLDER : TYPE_FILE;
  }


  static class FileItemHolder extends RecyclerView.ViewHolder {

    @NonNull
    ImageView icon;
    @NonNull
    ImageView shared;
    @NonNull
    TextView name;
    @NonNull
    TextView metaInfo;
    final boolean isParentShared;

    public FileItemHolder(View itemView, boolean isContentShared) {
      super(itemView);
      isParentShared = isContentShared;
      icon = (ImageView) itemView.findViewById(R.id.icon);
      shared = (ImageView) itemView.findViewById(R.id.shared);
      name = (TextView) itemView.findViewById(R.id.fileName);
      metaInfo = (TextView) itemView.findViewById(R.id.fileMetaInfo);
    }

    public void bind(ListItem listItem) {
      name.setText(listItem.getDisplayName());
      SimpleDateFormat dateFormat = new SimpleDateFormat(Utils.DATE_FORMAT, Locale.getDefault());
      String lastUpdatedTime = dateFormat.format(new Date(listItem.getLastUpdated()));

      metaInfo.setText(Utils.getFileSize(listItem.getContentLength()) + " " + lastUpdatedTime);

      int resourceId = (Utils.getFileResourceId(listItem.getMediaType(), listItem.getContentType()));
      icon.setImageResource(resourceId);

      int visibility = !isParentShared && listItem.isShared() ? View.VISIBLE : View.GONE;
      shared.setVisibility(visibility);
    }
  }


  static class FolderItemHolder extends RecyclerView.ViewHolder implements
      View.OnLongClickListener, View.OnClickListener {

    @NonNull
    final ImageView icon;
    @NonNull
    final ImageView shared;
    @NonNull
    final TextView name;
    @SuppressWarnings("NullableProblems")
    @NonNull
    String path;
    final boolean isParentShared;
    @NonNull
    private final DirectoryContentActivity activity;
    boolean isFolderShared;
    @NonNull
    final Credentials credentials;

    public FolderItemHolder(@NonNull View itemView, @NonNull Credentials credentials,
                            boolean isParentShared, @NonNull DirectoryContentActivity activity) {
      super(itemView);
      this.credentials = credentials;
      this.isParentShared = isParentShared;
      this.activity = activity;

      icon = (ImageView) itemView.findViewById(R.id.icon);
      shared = (ImageView) itemView.findViewById(R.id.shared);
      name = (TextView) itemView.findViewById(R.id.folderName);

      itemView.setOnClickListener(this);
      itemView.setOnLongClickListener(this);
    }

    public void bind(@NonNull ListItem listItem) {
      path = listItem.getFullPath();

      String displayName = listItem.getDisplayName();
      name.setText(displayName);

      int resourceId = Utils.getFolderResourceId(displayName);
      icon.setImageResource(resourceId);

      isFolderShared = listItem.isShared();
      int visibility = !isParentShared && isFolderShared ? View.VISIBLE : View.GONE;
      shared.setVisibility(visibility);
    }

    @Override
    public boolean onLongClick(View v) {
      Bundle callbackArgs = new Bundle();
      callbackArgs.putString(DirectoryContentActivity.DIRECTORY, path);

      Utils.showMenuDialog(activity, R.string.dialog_cancel, R.string.dialog_view_slideshow,
          R.string.open, callbackArgs);

      return false;
    }

    @Override
    public void onClick(View v) {
      Utils.showDirectoryContent(v.getContext(), credentials, path, isFolderShared);
    }
  }
}
