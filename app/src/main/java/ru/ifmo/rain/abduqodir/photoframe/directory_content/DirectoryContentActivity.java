package ru.ifmo.rain.abduqodir.photoframe.directory_content;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.rain.abduqodir.photoframe.R;
import ru.ifmo.rain.abduqodir.photoframe.slideshow.SlideshowActivity;

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
      setTitle(currentDirectory.substring(1));
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

  @Override
  public void onDialogPositiveClick(DialogFragment dialog, String path) {
    Intent intent = new Intent(this, SlideshowActivity.class);
    intent.putExtra(DirectoryContentActivity.CREDENTIALS, credentials);
    intent.putExtra(DirectoryContentActivity.DIRECTORY, path);
    startActivity(intent);
  }

  @Override
  public void onDialogNegativeClick(DialogFragment dialog, String path) {
  }
}
