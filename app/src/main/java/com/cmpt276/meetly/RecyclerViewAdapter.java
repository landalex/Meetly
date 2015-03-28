package com.cmpt276.meetly;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.List;

import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCard;
import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCardThumbnail;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.recyclerview.internal.CardArrayRecyclerViewAdapter;
import it.gmariotti.cardslib.library.view.CardView;
import it.gmariotti.cardslib.library.view.CardViewNative;

/**
 * RecyclerViewAdapter provided for potential modifications to the adapter in the future.
 */
public class RecyclerViewAdapter extends CardArrayRecyclerViewAdapter {

    public RecyclerViewAdapter(Context context, List<Card> cardsList) {
        super(context, cardsList);
    }

}
