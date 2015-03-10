package com.cmpt276.meetly;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.cmpt276.meetly.dummy.DummyContent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.gmariotti.cardslib.library.cards.actions.BaseSupplementalAction;
import it.gmariotti.cardslib.library.cards.actions.IconSupplementalAction;
import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCard;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.recyclerview.internal.CardArrayRecyclerViewAdapter;
import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;

/**
 * Scroll bar (draggable), Swipe for options (View, edit, delete, invite)
 * Each item shows title, date/time and duration (location as well?)
 */
public class EventList extends Fragment implements AbsListView.OnItemClickListener {

    private final String TAG = "EventListFragment";
    private static final String EVENT_TITLE = "text1";
    private static final String EVENT_DATE = "text2";
    private static final String EVENT_LOCATION = "text3";
    private static final String EVENT_COUNTDOWN = "text4";

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private CardRecyclerView mRecyclerView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private CardArrayRecyclerViewAdapter mCardArrayAdapter;

    /**
     * The database helper
     */
    private EventsDataSource database = new EventsDataSource(getActivity());

    public static EventList newInstance(String param1, String param2) {
        EventList fragment = new EventList();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventList() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] testArray = {"Test string 1", "Test string 2", "Test string 3"};

//        createAdapter();
        ArrayList<Card> cards = makeCards();
        createCardAdapter(cards);

    }

    private void createCardAdapter(ArrayList<Card> cards) {
        mCardArrayAdapter = new CardArrayRecyclerViewAdapter(getActivity(), cards);
    }

    private ArrayList<Card> makeCards() {
        List<Event> eventList = getTestEvents();
        ArrayList<BaseSupplementalAction> actions = new ArrayList<>();

        IconSupplementalAction ic1 = new IconSupplementalAction(getActivity(), R.id.ic1);
        ic1.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Toast.makeText(getActivity()," Click on icon 1 ", Toast.LENGTH_SHORT).show();
            }
        });
        actions.add(ic1);

        IconSupplementalAction ic2 = new IconSupplementalAction(getActivity(), R.id.ic2);
        ic2.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Toast.makeText(getActivity()," Click on icon 2 ", Toast.LENGTH_SHORT).show();
            }
        });
        actions.add(ic2);

        IconSupplementalAction ic3 = new IconSupplementalAction(getActivity(), R.id.ic3);
        ic3.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Toast.makeText(getActivity()," Click on icon 3 ", Toast.LENGTH_SHORT).show();
            }
        });
        actions.add(ic3);

        ArrayList<Card> cards = new ArrayList<>();
        for (Event event: eventList) {
            MaterialLargeImageCard card = MaterialLargeImageCard.with(getActivity())
                    .setTextOverImage(event.getTitle())
                    .setTitle(event.getDate())
                    .setSubTitle(timeUntil(event.getDateAsDate()))
                    .useDrawableId(R.drawable.card_background)
                    .setupSupplementalActions(R.layout.fragment_card_view_actions, actions)
                    .build();
            card.addCardHeader(new CardHeader(getActivity()));
            cards.add(card);
        }

        return cards;
    }

    private void createAdapter() {
        final String[] fromMapKey = {EVENT_TITLE, EVENT_DATE, EVENT_LOCATION, EVENT_COUNTDOWN};
        final int[] toLayoutId = {android.R.id.text1, android.R.id.text2/*, android.R.id.text3, android.R.id.text4*/};
        List<Map<String, String>> eventList = getEventList();


//        mAdapter = new SimpleAdapter(getActivity(), eventList, android.R.layout.simple_list_item_2,
//                fromMapKey, toLayoutId);
    }

    private List getEventList() {
        final List<Map<String, String>> eventMapList = new ArrayList<>();

        List<Event> eventList = getTestEvents();

        for (Event event : eventList) {
            Map<String, String> eventMap = new HashMap<>();
            eventMap.put(EVENT_TITLE, event.getTitle());
            eventMap.put(EVENT_DATE, event.getDate());
            eventMap.put(EVENT_LOCATION, event.getLocation());
            eventMap.put(EVENT_COUNTDOWN, timeUntil(event.getDateAsDate()));
            eventMapList.add(eventMap);
        }

        return Collections.unmodifiableList(eventMapList);
    }

    private String timeUntil(Date date) {
        Date now = new Date();
        long hoursUntil = date.getHours() - now.getHours();
        long minutesUntil = date.getMinutes() - now.getMinutes();
        long daysUntil = hoursUntil / 24;
        hoursUntil = hoursUntil % 24;

        return daysUntil + ":" + hoursUntil + ":" + minutesUntil;
    }

    private ArrayList getTestEvents() {
        ArrayList<Event> testEvents = new ArrayList<>();
        ArrayList<String> attendees = new ArrayList<>();
        attendees.add("Alex");
        attendees.add("Hami");
        attendees.add("Tina");
        attendees.add("Jas");
        testEvents.add(new Event(0, "Tims Run", new Date(2015, 03, 10), "Somewhere", attendees, "A note"));
        testEvents.add(new Event(1, "Tims Run", new Date(2015, 03, 11), "Somewhere", attendees, "A note"));
        testEvents.add(new Event(2, "Tims Run", new Date(2015, 03, 12), "Somewhere", attendees, "A note"));


        return testEvents;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

//        // Set the adapter
//        mListView = (AbsListView) view.findViewById(android.R.id.list);
//        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
//
//        // Set OnItemClickListener so we can be notified on item clicks
//        mListView.setOnItemClickListener(this);

        mRecyclerView = (CardRecyclerView) getActivity().findViewById(R.id.fragment_recyclerview);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(mCardArrayAdapter);
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
