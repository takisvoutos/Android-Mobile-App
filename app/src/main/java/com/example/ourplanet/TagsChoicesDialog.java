package com.example.ourplanet;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class TagsChoicesDialog extends DialogFragment
{

    public interface onMultiChoiceListener
    {
        void onPositiveButtonClicked(String[] list, ArrayList<String> selectedItemList);
        void onNegativeButtonClicked();
    }

    onMultiChoiceListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (onMultiChoiceListener) context;
        }catch (Exception e) {
            throw new ClassCastException(getActivity().toString() + " onMultiChoiceListener must implemented");
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        final ArrayList<String> selectedItemsList = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final String[] list = getActivity().getResources().getStringArray(R.array.tags_items);

        builder.setTitle("Select Tags")
                .setMultiChoiceItems(list, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked)
                    {
                        if(isChecked)
                        {
                            selectedItemsList.add(list[which]);
                        }
                        else
                        {
                            selectedItemsList.remove(list[which]);
                        }
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mListener.onPositiveButtonClicked(list, selectedItemsList);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mListener.onNegativeButtonClicked();
                    }
                });

        return builder.create();
    }
}
