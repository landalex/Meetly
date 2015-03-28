package com.cmpt276.meetly;

import android.view.View;

/**
 * For implementing onClickListeners to cards in a RecyclerView
 */
public interface RecyclerViewClickListener {
    public void recyclerViewListClicked(View v, int position);
}
