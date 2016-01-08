package net.sylvek.itracing2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by sylvek on 29/12/2015.
 */
public class DeviceAlertDialogFragment extends DialogFragment {

    public static final String NAME = "name";
    public static final String ADDRESS = "address";

    public static DeviceAlertDialogFragment instance(String name, String address)
    {
        DeviceAlertDialogFragment frag = new DeviceAlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(NAME, name);
        args.putString(ADDRESS, address);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final String name = getArguments().getString(NAME);
        final String address = getArguments().getString(ADDRESS);

        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View alertView = inflater.inflate(R.layout.device_alertbox, null);

        final EditText editText = (EditText) alertView.findViewById(R.id.editText);
        editText.setText(name);

        final Button alert = (Button) alertView.findViewById(R.id.button);
        alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                ((OnConfirmAlertDialogListener) getActivity()).doNeutralClick(address);
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(alertView)
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

        void doNeutralClick(final String address);
    }
}
