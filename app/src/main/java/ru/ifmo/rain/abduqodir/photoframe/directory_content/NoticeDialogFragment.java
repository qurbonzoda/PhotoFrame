package ru.ifmo.rain.abduqodir.photoframe.directory_content;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import ru.ifmo.rain.abduqodir.photoframe.R;

public class NoticeDialogFragment extends DialogFragment {

  public interface NoticeDialogListener {
    void onDialogPositiveClick(DialogFragment dialog, String path);
    void onDialogNegativeClick(DialogFragment dialog, String path);
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

    builder.setTitle(R.string.dialog_watch_slideshow)
        .setPositiveButton(R.string.yes,
            (dialog, id) -> mListener.onDialogPositiveClick(NoticeDialogFragment.this,
                this.getArguments().getString(DirectoryContentActivity.DIRECTORY)))
        .setNeutralButton(R.string.cancel,
            (dialog, id) -> mListener.onDialogNegativeClick(NoticeDialogFragment.this,
                this.getArguments().getString(DirectoryContentActivity.DIRECTORY)));

    return builder.create();

  }
}
