package com.lguipeng.notes.adpater;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lguipeng.notes.R;

/**
 * Created by lgp on 2015/4/6.
 */
public class NotesItemViewHolder extends RecyclerView.ViewHolder{

    private final TextView mNoteLabelTextView;
    private final TextView mNoteContentTextView;
    private final TextView mNoteTimeTextView;
    public NotesItemViewHolder(View parent) {
        super(parent);
        mNoteLabelTextView = (TextView) parent.findViewById(R.id.note_label_text);
        mNoteContentTextView = (TextView) parent.findViewById(R.id.note_content_text);
        mNoteTimeTextView = (TextView) parent.findViewById(R.id.note_last_edit_text);
    }

    public void setLabelText(CharSequence text){
        setTextView(mNoteLabelTextView, text);
    }

    public void setLabelText(int text){
        setTextView(mNoteLabelTextView, text);
    }

    public void setContentText(CharSequence text){
        setTextView(mNoteContentTextView, text);
    }

    public void setContentText(int text){
        setTextView(mNoteContentTextView, text);
    }

    public void setTimeText(CharSequence text){
        setTextView(mNoteTimeTextView, text);
    }

    public void setTimeText(int text){
        setTextView(mNoteTimeTextView, text);
    }

    private void setTextView(TextView view, CharSequence text){
        if (view == null || TextUtils.isEmpty(text))
            return;
        view.setText(text);
    }

    private void setTextView(TextView view, int text){
        if (view == null || text <= 0)
            return;
        view.setText(text);
    }
}
