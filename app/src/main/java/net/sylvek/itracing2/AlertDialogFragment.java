package net.sylvek.itracing2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by sylvek on 21/12/2015.
 */
public class AlertDialogFragment extends DialogFragment {

    public static final String TITLE = "title";
    public static final String MESSAGE = "message";

    public static AlertDialogFragment instance(int title)
    {
        AlertDialogFragment frag = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt(TITLE, title);
        frag.setArguments(args);
        return frag;
    }

    public static AlertDialogFragment instance(int title, int message)
    {
        AlertDialogFragment frag = new AlertDialogFragment();
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

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setNeutralButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                dialog.dismiss();
                            }
                        }
                );

        if (getArguments().containsKey(MESSAGE)) {
            int message = getArguments().getInt(MESSAGE);
            builder.setMessage(message);
        }

        return builder.create();
    }
}
