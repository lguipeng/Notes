package com.lguipeng.notes.adpater;

import android.animation.Animator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.lguipeng.notes.utils.ViewHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lgp on 2015/5/24.
 */
public abstract class BaseRecyclerViewAdapter<E> extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private int mDuration = 300;

    private Interpolator mInterpolator = new LinearInterpolator();

    private int mLastPosition = -1;

    private boolean isFirstOnly = true;

    protected List<E> list;
    private Map<Integer, onInternalClickListener<E>> canClickItem;
    public BaseRecyclerViewAdapter(List<E> list) {
        this.list = list;
    }

    public void add(E e) {
        this.list.add(0, e);
        notifyItemInserted(0);
    }

    public void remove(E e) {
        this.list.remove(e);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        this.list.remove(position);
        notifyDataSetChanged();
    }


    public void setList(List<E> list) {
        this.list.clear();
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    public List<E> getList() {
        return list;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder != null){
            addInternalClickListener(holder.itemView, position, list.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    private void addInternalClickListener(final View itemV, final Integer position, final E valuesMap) {
        if (canClickItem != null) {
            for (Integer key : canClickItem.keySet()) {
                View inView = itemV.findViewById(key);
                final onInternalClickListener<E> listener = canClickItem.get(key);
                if (inView != null && listener != null) {
                    inView.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            listener.OnClickListener(itemV, v, position,
                                    valuesMap);

                        }
                    });
                    inView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            listener.OnLongClickListener(itemV, v, position,
                                    valuesMap);
                            return true;
                        }
                    });
                }
            }
        }
    }

    public void setOnInViewClickListener(Integer key, onInternalClickListener<E> onClickListener) {
        if (canClickItem == null)
            canClickItem = new HashMap<>();
        canClickItem.put(key, onClickListener);
    }

    public interface onInternalClickListener<T> {
        void OnClickListener(View parentV, View v, Integer position,
                                    T values);
        void OnLongClickListener(View parentV, View v, Integer position,
                                        T values);
    }

    public static class onInternalClickListenerImpl<T> implements onInternalClickListener<T>{
        @Override
        public void OnClickListener(View parentV, View v, Integer position, T values) {

        }

        @Override
        public void OnLongClickListener(View parentV, View v, Integer position, T values) {

        }
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    public void setStartPosition(int start) {
        mLastPosition = start;
    }

    public void setFirstOnly(boolean firstOnly) {
        isFirstOnly = firstOnly;
    }

    protected void animate(RecyclerView.ViewHolder holder, int position){
        if (!isFirstOnly || position > mLastPosition) {
            for (Animator anim : getAnimators(holder.itemView)) {
                anim.setDuration(mDuration).start();
                anim.setInterpolator(mInterpolator);

            }
            mLastPosition = position;
        } else {
            ViewHelper.clear(holder.itemView);
        }
    }

    protected abstract Animator[] getAnimators(View view);
}
