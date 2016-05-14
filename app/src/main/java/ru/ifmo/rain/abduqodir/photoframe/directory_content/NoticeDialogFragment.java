package ru.ifmo.rain.abduqodir.photoframe.directory_content;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class NoticeDialogFragment extends DialogFragment {

  public static final String MESSAGE = "message";
  public static final String NEGATIVE_BUTTON = "negative_button";
  public static final String POSITIVE_BUTTON = "positive_button";
  public static final String NEUTRAL_BUTTON = "neutral_button";
  public static final String CALLBACK_ARGS = "callback_args";

  public static NoticeDialogFragment newInstance(String message, String negativeButton,
                                                 String positiveButton, String neutralButton,
                                                 Bundle callbackArgs) {
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
