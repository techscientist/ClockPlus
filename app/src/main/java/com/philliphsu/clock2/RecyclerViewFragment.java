package com.philliphsu.clock2;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philliphsu.clock2.alarms.ScrollHandler;
import com.philliphsu.clock2.model.BaseItemCursor;
import com.philliphsu.clock2.model.ObjectWithId;

import butterknife.Bind;

/**
 * Created by Phillip Hsu on 7/26/2016.
 */
public abstract class RecyclerViewFragment<
        T extends ObjectWithId,
        VH extends BaseViewHolder<T>,
        C extends BaseItemCursor<T>,
        A extends BaseCursorAdapter<T, VH, C>>
    extends BaseFragment implements
        LoaderManager.LoaderCallbacks<C>,
        OnListItemInteractionListener<T>,
        ScrollHandler {

    private A mAdapter;
    private long mScrollToStableId = RecyclerView.NO_ID;

    // TODO: Rename id to recyclerView?
    // TODO: Rename variable to mRecyclerView?
    @Bind(R.id.list) RecyclerView mList;

    public abstract void onFabClick();

    /**
     * Callback invoked when we have scrolled to the stable id as set in
     * {@link #setScrollToStableId(long)}.
     * @param id the stable id we have scrolled to
     * @param position the position of the item with this stable id
     */
    protected abstract void onScrolledToStableId(long id, int position);

    /**
     * @return the adapter to set on the RecyclerView. SUBCLASSES MUST OVERRIDE THIS, BECAUSE THE
     * DEFAULT IMPLEMENTATION WILL ALWAYS RETURN AN UNINITIALIZED ADAPTER INSTANCE.
     */
    @Nullable
    protected A getAdapter() {
        return mAdapter;
    }

    /**
     * @return the LayoutManager to set on the RecyclerView. The default implementation
     * returns a vertical LinearLayoutManager.
     */
    protected RecyclerView.LayoutManager getLayoutManager() {
        // Called in onCreateView(), so the host activity is alive already.
        return new LinearLayoutManager(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mList.setLayoutManager(getLayoutManager());
        mList.setAdapter(mAdapter = getAdapter());
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // http://stackoverflow.com/a/14632434/5055032
        // A Loader's lifecycle is bound to its Activity, not its Fragment.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onLoadFinished(Loader<C> loader, C data) {
        mAdapter.swapCursor(data);
        // This may have been a requery due to content change. If the change
        // was an insertion, scroll to the last modified alarm.
        performScrollToStableId();
    }

    @Override
    public void onLoaderReset(Loader<C> loader) {
        mAdapter.swapCursor(null);
    }

    /**
     * @return a layout resource that MUST contain a RecyclerView. The default implementation
     * returns a layout that has just a single RecyclerView in its hierarchy.
     */
    @Override
    protected int contentLayout() {
        return R.layout.fragment_recycler_view;
    }

    @Override
    public void setScrollToStableId(long id) {
        mScrollToStableId = id;
    }

    @Override
    public void scrollToPosition(int position) {
        mList.smoothScrollToPosition(position);
    }

    private void performScrollToStableId() {
        if (mScrollToStableId != RecyclerView.NO_ID) {
            int position = -1;
            for (int i = 0; i < mAdapter.getItemCount(); i++) {
                if (mAdapter.getItemId(i) == mScrollToStableId) {
                    position = i;
                    break;
                }
            }
            if (position >= 0) {
                scrollToPosition(position);
                onScrolledToStableId(mScrollToStableId, position);
            }
        }
        // Reset
        mScrollToStableId = RecyclerView.NO_ID;
    }
}