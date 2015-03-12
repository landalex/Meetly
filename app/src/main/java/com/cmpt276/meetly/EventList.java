package com.cmpt276.meetly;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;


import com.cmpt276.meetly.dummy.DummyContent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.LifecycleCallback;
import de.keyboardsurfer.android.widget.crouton.Style;
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

    private OnFragmentInteractionListener mListener;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private CardArrayRecyclerViewAdapter mCardArrayAdapter;

    /**
     * The database helper
     */
    private EventsDataSource database;
    private ArrayList<Card> cards = new ArrayList<>(0);
    public boolean showingCrouton;

    public static EventList newInstance() {
        return new EventList();
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
        database = new EventsDataSource(getActivity());
//        makeTestEvents();
    }

    private void makeTestEvents() {
        for (int i = 1; i < 4; i++) {
            database.createEvent("Test" + i, new Date(1430000000000l), "A place", new ArrayList<String>(), "Notes");
            Log.i(TAG, "Event" + i + " added");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        createCardAdapter(cards);
        configureRecyclerView();

    }

    @Override
    public void onResume() {
        super.onResume();
        new UpdateCards().execute();
    }

    private void configureRecyclerView() {
        CardRecyclerView mRecyclerView = (CardRecyclerView) getActivity().findViewById(R.id.fragment_recyclerview);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mCardArrayAdapter);
    }

    private void createCardAdapter(ArrayList<Card> cards) {
        mCardArrayAdapter = new CardArrayRecyclerViewAdapter(getActivity(), cards);
    }

    private ArrayList<Card> makeCards(EventsDataSource database) {
        List<Event> eventList = getEvents(database);
        ArrayList<BaseSupplementalAction> actions = new ArrayList<>();

        IconSupplementalAction ic1 = new IconSupplementalAction(getActivity(), R.id.ic1);
        ic1.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Toast.makeText(getActivity(), " Click on icon 1 ", Toast.LENGTH_SHORT).show();
            }
        });
        actions.add(ic1);

        IconSupplementalAction ic2 = new IconSupplementalAction(getActivity(), R.id.ic2);
        ic2.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Toast.makeText(getActivity(), " Click on icon 2 ", Toast.LENGTH_SHORT).show();
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
                    .useDrawableId(R.drawable.card_picture)
                    .setupSupplementalActions(R.layout.fragment_card_view_actions, actions)
                    .build();
            card.addCardHeader(new CardHeader(getActivity()));
            cards.add(card);
        }
        return cards;
    }

    private String timeUntil(Date date) {
        long now = new Date().getTime();
        long eventTime = date.getTime();
        long diff = eventTime - now;

        if (diff <= 0) {
            return "Happening now";
        }

        final long hoursInDay = TimeUnit.DAYS.toHours(1);
        final long minutesInHour = TimeUnit.HOURS.toMinutes(1);

        long daysUntil = TimeUnit.MILLISECONDS.toDays(diff);
        long hoursUntil = TimeUnit.MILLISECONDS.toHours(diff) % hoursInDay;
        long minutesUntil = TimeUnit.MILLISECONDS.toMinutes(diff) % minutesInHour;


        return String.format("Happening in %02d days, %02d hours, and %02d minutes", daysUntil, hoursUntil, minutesUntil);
    }

    private ArrayList getTestEvents() {
        ArrayList<Event> testEvents = new ArrayList<>();
        ArrayList<String> attendees = new ArrayList<>();
        attendees.add("Alex");
        attendees.add("Hami");
        attendees.add("Tina");
        attendees.add("Jas");
        testEvents.add(new Event(0, "Tims Run", new Date(2015, 3, 10), "Somewhere", attendees, "A note"));
        testEvents.add(new Event(1, "Tims Run", new Date(2015, 3, 11), "Somewhere", attendees, "A note"));
        testEvents.add(new Event(2, "Tims Run", new Date(2015, 3, 12), "Somewhere", attendees, "A note"));


        return testEvents;
    }

    private List getEvents(EventsDataSource database) {
        return database.getAllEvents();
    }

    public Crouton makeLocationCrouton() {
        LifecycleCallback callback = new LifecycleCallback() {
            @Override
            public void onDisplayed() {
                showingCrouton = true;
            }

            @Override
            public void onRemoved() {
                showingCrouton = false;
            }
        };
        Configuration config = new Configuration.Builder()
                .setDuration(Configuration.DURATION_INFINITE)
                .setInAnimation(R.anim.abc_slide_in_top)
                .setOutAnimation(R.anim.abc_slide_out_top)
                .build();

        Style style = new Style.Builder()
                .setBackgroundColorValue(getResources().getColor(R.color.green))
                .setHeight(250)
                .setConfiguration(config)
                .build();

        final Crouton crouton = Crouton.makeText(getActivity(), "Placeholder Location", style);
        crouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crouton.hide();
            }
        });
        crouton.setLifecycleCallback(callback);
        return crouton;
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
        public void onFragmentInteraction(String id);
    }

    private class UpdateCards extends AsyncTask<Boolean, Void, Integer> {
        EventsDataSource database = new EventsDataSource(getActivity());
        ProgressDialog dialog;

        protected void onPreExecute() {
            this.dialog = new ProgressDialog(getActivity());
            this.dialog.setIndeterminate(true);
            this.dialog.setMessage(getString(R.string.fragment_event_update_loading_text));
            this.dialog.show();
        }

        @Override
        protected Integer doInBackground(Boolean... params) {
            cards.removeAll(cards);
            cards.addAll(makeCards(this.database));
            return 1;
        }

        protected void onProgressUpdate() {
        }

        @Override
        protected void onPostExecute(Integer done) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
                mCardArrayAdapter.notifyDataSetChanged();
        }

    }
}
