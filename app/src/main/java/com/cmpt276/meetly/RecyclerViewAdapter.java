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
 * Created by AlexLand on 15-03-27.
 */
public class RecyclerViewAdapter extends CardArrayRecyclerViewAdapter {

    private Context context;
    private static RecyclerViewClickListener itemListener;


    public RecyclerViewAdapter(Context context, List<Card> cardsList, RecyclerViewClickListener itemListener) {
        super(context, cardsList);
        this.context = context;
        this.itemListener = itemListener;
    }


    //ViewHolder class implement OnClickListener,
    //set clicklistener to itemView and,
    //send message back to Activity/Fragment
    public static class ItemViewHolder extends CardViewHolder implements View.OnClickListener {

        public ItemViewHolder(View convertView) {
            super(convertView);
            convertView.setOnClickListener(this);
            if (convertView instanceof CardViewNative) {
                CardViewNative cardView = (CardViewNative) convertView;
                cardView.getCard().setId("" + this.getPosition());
                Log.d("ItemViewHolder", "Set ID");
            }
        }


        @Override
        public void onClick(View v) {
            itemListener.recyclerViewListClicked(v, this.getPosition());
        }
    }
}
