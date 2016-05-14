package ru.ifmo.rain.abduqodir.photoframe.directory_content;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;
import com.yandex.disk.client.ListParsingHandler;
import com.yandex.disk.client.TransportClient;
import com.yandex.disk.client.exceptions.CancelledPropfindException;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DirectoryContentLoader extends AsyncTaskLoader<List<ListItem>> {

  private static Collator collator = Collator.getInstance();

  static {
    collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
  }

  private final Comparator<ListItem> LIST_ITEM_COMPARATOR = (f1, f2) -> {
    if (f1.isCollection() && !f2.isCollection()) {
      return -1;
    } else if (f2.isCollection() && !f1.isCollection()) {
      return 1;
    } else {
      return collator.compare(f1.getDisplayName(), f2.getDisplayName());
    }
  };

  @NonNull
  private Credentials credentials;
  @NonNull
  private String directory;
  @Nullable
  private List<ListItem> listItems;
  private boolean hasException;

  public DirectoryContentLoader(Context context, @NonNull Credentials credentials,
                                @NonNull String directory) {
    super(context);
    this.credentials = credentials;
    this.directory = directory;
  }

  public boolean hasExceptionOccurred() {
    return hasException;
  }

  @Override
  protected void onStartLoading() {
    if (listItems != null) {
      deliverResult(listItems);
    } else {
      forceLoad();
    }
  }

  @Override
  public List<ListItem> loadInBackground() {
    listItems = new ArrayList<>();
    TransportClient transportClient = null;
    hasException = false;
    try {
      transportClient = TransportClient.getInstance(getContext(), credentials);
      transportClient.getList(directory, new ListParsingHandler() {
        boolean first = true;

        @Override
        public boolean handleItem(ListItem item) {
          if (!first) {
            listItems.add(item);
          }
          first = false;
          return false;
        }
      });
    } catch (CancelledPropfindException ex) {
      hasException = true;
      return listItems;
    } catch (Exception e) {
      hasException = true;
    } finally {
      TransportClient.shutdown(transportClient);
    }
    Collections.sort(listItems, LIST_ITEM_COMPARATOR);
    return listItems;
  }
}
