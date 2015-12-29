package net.sylvek.itracing2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

/**
 * Created by sylvek on 29/12/2015.
 */
public class EditTextAlertDialogFragment extends DialogFragment {

    public static final String TITLE = "title";
    public static final String NAME = "name";
    public static final String ADDRESS = "address";

    public static EditTextAlertDialogFragment instance(int title, String name, String address)
    {
        EditTextAlertDialogFragment frag = new EditTextAlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt(TITLE, title);
        args.putString(NAME, name);
        args.putString(ADDRESS, address);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final int title = getArguments().getInt(TITLE);
        final String name = getArguments().getString(NAME);
        final String address = getArguments().getString(ADDRESS);

        final EditText editText = new EditText(getActivity());
        editText.setText(name);
        editText.setSingleLine();
        editText.setPadding(25, 25, 25, 25);

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(editText)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                ((OnConfirmAlertDialogListener) getActivity()).doPositiveClick(address, editText.getText().toString());
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

        void doPositiveClick(final String address, final String text);

        void doNegativeClick();
    }
}
