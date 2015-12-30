package net.sylvek.itracing2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by sylvek on 21/12/2015.
 */
public class ConfirmAlertDialogFragment extends DialogFragment {

    public static final String TITLE = "title";
    public static final String MESSAGE = "message";

    public static ConfirmAlertDialogFragment instance(int title)
    {
        ConfirmAlertDialogFragment frag = new ConfirmAlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt(TITLE, title);
        frag.setArguments(args);
        return frag;
    }

    public static ConfirmAlertDialogFragment instance(int title, int message)
    {
        ConfirmAlertDialogFragment frag = new ConfirmAlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt(TITLE, title);
        args.putInt(MESSAGE, message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        int title = getArguments().getInt(TITLE);
        int message = getArguments().getInt(MESSAGE);

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                ((OnConfirmAlertDialogListener) getActivity()).doPositiveClick();
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                ((OnConfirmAlertDialogListener) getActivity()).doNegativeClick();
                            }
                        }
                )
                .create();
    }

    public interface OnConfirmAlertDialogListener {

        void doPositiveClick();

        void doNegativeClick();
    }
}
