package com.lguipeng.notes.adpater;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.lguipeng.notes.R;
import com.lguipeng.notes.model.Note;
import com.lguipeng.notes.utils.TimeUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lgp on 2015/4/6.
 */
public class NotesAdapter extends BaseRecyclerViewAdapter<Note> implements Filterable{
    private final List<Note> originalList;
    private int upDownFactor = 1;
    private boolean isShowScaleAnimate = true;
    public NotesAdapter(List<Note> list) {
        super(list);
        originalList = new ArrayList<>(list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        final View view = LayoutInflater.from(context).inflate(R.layout.notes_item_layout, parent, false);
        return new NotesItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
        NotesItemViewHolder holder = (NotesItemViewHolder) viewHolder;
        Note note = list.get(position);
        if (note == null)
            return;
        holder.setLabelText(note.getLabel());
        holder.setContentText(note.getContent());
        holder.setTimeText(TimeUtils.getConciseTime(note.getLastOprTime()));
        animate(viewHolder, position);
    }

    @Override
    public Filter getFilter() {
        return new NoteFilter(this, originalList);
    }

    @Override
    protected Animator[] getAnimators(View view) {
        if (view.getMeasuredHeight() <=0 || isShowScaleAnimate){
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.1f, 1f);
            return new ObjectAnimator[]{scaleX, scaleY};
        }
        return new Animator[]{
                ObjectAnimator.ofFloat(view, "scaleX", 1.1f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 1.1f, 1f),
                ObjectAnimator.ofFloat(view, "translationY", upDownFactor * 1.5f * view.getMeasuredHeight(), 0)
        };
    }

    @Override
    public void setList(List<Note> list) {
        super.setList(list);
        this.originalList.clear();
        originalList.addAll(list);
        setUpFactor();
        isShowScaleAnimate = true;
    }

    public void setDownFactor(){
        upDownFactor = -1;
        isShowScaleAnimate = false;
    }

    public void setUpFactor(){
        upDownFactor = 1;
        isShowScaleAnimate = false;
    }

    private static class NoteFilter extends Filter{

        private final NotesAdapter adapter;

        private final List<Note> originalList;

        private final List<Note> filteredList;

        private NoteFilter(NotesAdapter adapter, List<Note> originalList) {
            super();
            this.adapter = adapter;
            this.originalList = new LinkedList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();
            if (constraint.length() == 0) {
                filteredList.addAll(originalList);
            } else {
                for ( Note note : originalList) {
                    if (note.getContent().contains(constraint) || note.getLabel().contains(constraint)) {
                        filteredList.add(note);
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.list.clear();
            adapter.list.addAll((ArrayList<Note>) results.values);
            adapter.notifyDataSetChanged();
        }
    }
}
