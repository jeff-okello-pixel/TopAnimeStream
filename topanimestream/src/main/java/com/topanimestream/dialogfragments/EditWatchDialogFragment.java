package com.topanimestream.dialogfragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import com.topanimestream.R;
import com.topanimestream.models.WatchedAnime;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EditWatchDialogFragment extends DialogFragment implements View.OnClickListener {

    @Bind(R.id.numberPickerProgress)
    NumberPicker numberPickerProgress;

    @Bind(R.id.btnSave)
    Button btnSave;

    private EditWatchCallback mEditWatchCallback;
    WatchedAnime watchedAnime;
    int position; //position in the adapter, needs to be returned in the callback

    public static EditWatchDialogFragment newInstance(WatchedAnime watchedAnime, int position) {
        EditWatchDialogFragment dialogFragment = new EditWatchDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("watchedAnime", watchedAnime);
        args.putInt("position", position);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        watchedAnime = getArguments().getParcelable("watchedAnime");
        position = getArguments().getInt("position");

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_edit_watch, null, false);
        ButterKnife.bind(this, view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle("Edit entry");
        builder.setCancelable(true);

        numberPickerProgress.setMaxValue(watchedAnime.getAnime().getEpisodeCount());
        numberPickerProgress.setValue(watchedAnime.getTotalWatchedEpisodes());


        btnSave.setOnClickListener(this);

        return builder.create();
    }

    public void setEditWatchCallback(EditWatchCallback callback)
    {
        this.mEditWatchCallback = callback;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btnSave:
                //Perform Save
                break;
        }
    }

    public interface EditWatchCallback
    {
        void onEditSuccess(WatchedAnime updatedWatchedAnime, int position);
    }
}
