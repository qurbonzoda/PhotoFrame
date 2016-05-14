package ru.ifmo.rain.abduqodir.photoframe.directory_content;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.rain.abduqodir.photoframe.R;
import ru.ifmo.rain.abduqodir.photoframe.Utils;

public class DirectoryContentActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<List<ListItem>>,
    NoticeDialogFragment.NoticeDialogListener {

  public static final String ROOT_DIRECTORY = "/";
  public static final String DIRECTORY = "directory";
  public static final String CREDENTIALS = "credentials";
  public static final String IS_SHARED = "is_shared";

  private RecyclerView recyclerView;
  private ProgressBar progressBar;

  private Credentials credentials;
  private String currentDirectory;
  private List<ListItem> listItems;
  private DirectoryContentAdapter adapter;
  private boolean isContentShared;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_directory_content);

    recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    progressBar = (ProgressBar) findViewById(R.id.progressBar);

    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    progressBar.setVisibility(View.VISIBLE);

    if (savedInstanceState == null) {
      credentials = getIntent().getParcelableExtra(CREDENTIALS);
      currentDirectory = getIntent().getStringExtra(DIRECTORY);
      isContentShared = getIntent().getBooleanExtra(IS_SHARED, false);
    } else {
      credentials = savedInstanceState.getParcelable(CREDENTIALS);
      currentDirectory = savedInstanceState.getString(DIRECTORY);
      isContentShared = savedInstanceState.getBoolean(IS_SHARED);
    }

    if (!ROOT_DIRECTORY.equals(currentDirectory)) {
      setTitle(Utils.getFolderName(currentDirectory));
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    } else {
      getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    listItems = new ArrayList<>();
    adapter = new DirectoryContentAdapter(this, listItems, credentials, isContentShared);
    recyclerView.setAdapter(adapter);
    getLoaderManager().initLoader(0, null, this);
  }

  @Override
  public Loader<List<ListItem>> onCreateLoader(int id, Bundle args) {
    return new DirectoryContentLoader(this, credentials, currentDirectory);
  }

  @Override
  public void onLoadFinished(Loader<List<ListItem>> loader, List<ListItem> data) {
    if (((DirectoryContentLoader) loader).hasExceptionOccurred()) {
      Toast.makeText(this, R.string.toast_unable_to_load, Toast.LENGTH_LONG).show();
    }
    listItems.addAll(data);
    adapter.notifyDataSetChanged();
    progressBar.setVisibility(View.GONE);
  }

  @Override
  public void onLoaderReset(Loader<List<ListItem>> loader) {
    listItems.clear();
    adapter.notifyDataSetChanged();
  }

  @Override
  protected void onSaveInstanceState(Bundle savedInstanceState) {
    super.onSaveInstanceState(savedInstanceState);

    savedInstanceState.putParcelable(CREDENTIALS, credentials);
    savedInstanceState.putString(DIRECTORY, currentDirectory);
    savedInstanceState.putBoolean(IS_SHARED, isContentShared);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.directory_content_activity, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        Utils.navigateBack(this, credentials, currentDirectory);
        return true;

      case R.id.menu_view_in_slideshow:
        Utils.showInSlideShow(this, credentials, currentDirectory);
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onDialogPositiveClick(DialogFragment dialog, Bundle args) {
    Utils.showInSlideShow(this, credentials, args.getString(DIRECTORY));
  }

  @Override
  public void onDialogNegativeClick(DialogFragment dialog, Bundle args) {
  }

  @Override
  public void onDialogNeutralClick(DialogFragment dialog, Bundle args) {
    Utils.showDirectoryContent(this, credentials, args.getString(DIRECTORY), args.getBoolean(IS_SHARED));
  }
}
