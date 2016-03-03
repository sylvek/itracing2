package net.sylvek.itracing2.devices;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import net.sylvek.itracing2.BluetoothLEService;
import net.sylvek.itracing2.R;

/**
 * Created by sylvek on 29/12/2015.
 */
public class DeviceAlertDialogFragment extends DialogFragment {

    public static final String NAME = "name";
    public static final String ADDRESS = "address";
    public static final String CHECKED = "checked";

    public static DeviceAlertDialogFragment instance(String name, String address, boolean checked)
    {
        DeviceAlertDialogFragment frag = new DeviceAlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(NAME, name);
        args.putString(ADDRESS, address);
        args.putBoolean(CHECKED, checked);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final String name = getArguments().getString(NAME);
        final String address = getArguments().getString(ADDRESS);
        final boolean checked = getArguments().getBoolean(CHECKED);

        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View alertView = inflater.inflate(R.layout.device_alertbox, null);

        final EditText editText = (EditText) alertView.findViewById(R.id.editText);
        editText.setText(name);

        final Button button1 = (Button) alertView.findViewById(R.id.button1);
        button1.setEnabled(checked);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
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

        final Button alert = (Button) alertView.findViewById(R.id.button1);
        alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                final String initialText = getActivity().getString(R.string.start_immediate_alert);
                if (alert.getText().equals(initialText)) {
                    ((OnConfirmAlertDialogListener) getActivity()).doAlertClick(address, BluetoothLEService.HIGH_ALERT);
                    alert.setText(R.string.stop_immediate_alert);
                } else {
                    ((OnConfirmAlertDialogListener) getActivity()).doAlertClick(address, BluetoothLEService.NO_ALERT);
                    alert.setText(R.string.start_immediate_alert);
                }
            }
        });

        final Button delete = (Button) alertView.findViewById(R.id.button2);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                ((OnConfirmAlertDialogListener) getActivity()).doDeleteClick(address);
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public interface OnConfirmAlertDialogListener {

        void doPositiveClick(final String address, final String text);

        void doNegativeClick();

        void doAlertClick(final String address, final int alertType);

        void doDeleteClick(final String address);
    }
}
