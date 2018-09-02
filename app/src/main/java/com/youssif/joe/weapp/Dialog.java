package com.youssif.joe.weapp;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Dialog extends AppCompatDialogFragment {

    private TextView textView;
    private EditText editText ;
    private DialogListener listener;

    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog , null);
        textView = view.findViewById(R.id.textView);
        editText = view.findViewById(R.id.edit_secure);



        builder.setView(view)
        .setTitle("")
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String secure_code = editText.getText().toString();
                listener.applyTexts(secure_code);
            }
        });



        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener =(DialogListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }

    }

    public interface DialogListener{
        void applyTexts(String secure_code);
    }
}
