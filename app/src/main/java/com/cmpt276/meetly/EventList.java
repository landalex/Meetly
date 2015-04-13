package com.cmpt276.meetly;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import it.gmariotti.cardslib.library.cards.actions.BaseSupplementalAction;
import it.gmariotti.cardslib.library.cards.actions.IconSupplementalAction;
import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCard;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;

/**
 * Provides the events in a list of cards, with the ability to select events to view
 * Scroll bar (draggable), Swipe for options (View, edit, delete, invite)
 * Each item shows title, date/time and duration (location as well?)
 */
public class EventList extends Fragment {

    private final String TAG = "EventListFragment";

    private OnFragmentInteractionListener mListener;

    private RecyclerViewAdapter mCardArrayAdapter;
    private ArrayList<Card> cards = new ArrayList<>(0);
    private List<Event> eventList = new ArrayList<>(0);
    private Map<String, Integer> drawableMap;
    private List<Boolean> eventListViewed;
    private SwipeRefreshLayout swipeRefreshLayout;
    public boolean cardsUpdating;



    public static EventList newInstance() {
        return new EventList();
    }

    public EventList() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        setCardsViewed();
        return inflater.inflate(R.layout.fragment_layout, container, false);
    }

    private void setCardsViewed() {
        int index = 0;
        for (Card card: cards) {
            boolean viewed;
            viewed = eventListViewed.get(index++);
            pickSupplementalActionsLayout(viewed);
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        createCardAdapter(cards);
        createFloatingActionButtonListeners();
        configureRecyclerView();
        configureSwipeToRefresh();

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setEventUpdateObserver(new EventUpdateObserver() {
            @Override
            public void eventsUpdated() {
                onResume();
            }
        });
        createDrawableMap();
    }

    private void configureSwipeToRefresh() {
        swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                final Handler handler = new Handler();
                cardsUpdating = true;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity activity = (MainActivity) getActivity();
                        activity.syncWithServerNow();
                        if (cardsUpdating) {
                            handler.postDelayed(this, 1000);
                        } else {
                            swipeRefreshLayout.setRefreshing(false);
                        }

                    }
                });
            }
        });
        final CardRecyclerView recyclerView = (CardRecyclerView) getActivity().findViewById(R.id.fragment_recyclerview);
        recyclerView.setOnScrollListener(new CardRecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView view, int scrollState) {

            }

            @Override
            public void onScrolled(RecyclerView view, int firstVisibleItem, int visibleItemCount) {
                int topRowVerticalPosition = (recyclerView == null || recyclerView.getChildCount() == 0) ?
                        0 : recyclerView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled((topRowVerticalPosition >= 0));
            }
        });

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.green));
    }

    private void createDrawableMap() {
        drawableMap = new HashMap<>();
        drawableMap.put("coffee", R.drawable.card_picture_coffee);
        drawableMap.put("drink", R.drawable.card_picture_alcohol);
        drawableMap.put("noodles", R.drawable.card_picture_noodles);
        drawableMap.put("pizza", R.drawable.card_picture_pizza);
        drawableMap.put("sandwich", R.drawable.card_picture_sandwich);
        drawableMap.put("breakfast", R.drawable.card_picture_breakfast);

        // For ambiguous food words, assign pictures randomly
        List<Integer> foodPictures = Arrays.asList(R.drawable.card_picture_noodles, R.drawable.card_picture_pizza,
                            R.drawable.card_picture_plate, R.drawable.card_picture_sandwich);

        Collections.shuffle(foodPictures);

        drawableMap.put("lunch", foodPictures.get(0));
        drawableMap.put("food", foodPictures.get(1));
        drawableMap.put("dinner", foodPictures.get(2));
    }

    private void createFloatingActionButtonListeners() {
        final FloatingActionsMenu menu = (FloatingActionsMenu)getActivity().findViewById(R.id.create_action_menu);
        getActivity().findViewById(R.id.create_normal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.collapse();
                Intent intent = new Intent(getActivity(), CreateEvent.class);
                startActivity(intent);
            }
        });

        getActivity().findViewById(R.id.create_spontaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.collapse();
                Intent intent = new Intent(getActivity(), CreateEvent.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
            UpdateCards updater = new UpdateCards();
            updater.execute(updater.CREATE_MODE, eventList.size());
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void configureRecyclerView() {
        CardRecyclerView mRecyclerView = (CardRecyclerView) getActivity().findViewById(R.id.fragment_recyclerview);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mCardArrayAdapter);
    }

    private void createCardAdapter(ArrayList<Card> cards) {
        mCardArrayAdapter = new RecyclerViewAdapter(getActivity(), cards);
    }

    private ArrayList<Card> makeCards(EventsDataSource database) {
        eventList = getEvents(database);

        ArrayList<Card> cards = new ArrayList<>();
        ArrayList<BaseSupplementalAction> actions = makeCardActions(database);
        int eventIndex = 0;
        for (Event event: eventList) {
            MaterialLargeImageCard card = makeMaterialLargeImageCard(actions, event, eventIndex);
            cards.add(card);
            eventIndex++;
        }
        return cards;
    }

    private MaterialLargeImageCard makeMaterialLargeImageCard(ArrayList<BaseSupplementalAction> actions, Event event, int eventIndex) {
        final long eventID = event.getID();

        int supplementalActionsLayout = pickSupplementalActionsLayout(event.isViewed());

        MaterialLargeImageCard card = MaterialLargeImageCard.with(getActivity())
                .setTextOverImage(event.getTitle())
                .setTitle(getTimestringForEvent(event))
                .setSubTitle(timeUntil(event.getStartDate().getTime()) + "\n" + getString(R.string.eventlist_card_unshared))
                .useDrawableId(pickDrawableForCard(event.getTitle()))
                .setupSupplementalActions(supplementalActionsLayout, actions)
                .build();
        card.addCardHeader(new CardHeader(getActivity()));
        card.setId("" + eventIndex);
        card.setCardElevation(10);
        event.setViewed(true);

        // Pass the event ID with the intent to ViewEvent
        card.setOnClickListener(new Card.OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Intent intent = new Intent(getActivity(), ViewEvent.class);
                Long eventIndex = Long.parseLong(card.getId());
                intent.putExtra("eventID", eventList.get(eventIndex.intValue()).getID());
                startActivity(intent);
            }
        });
        return card;
    }

    private String getTimestringForEvent(Event event) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE',' MMMM dd 'at' hh:mm aa");
        return formatter.format(event.getStartDate().getTime());
    }

    private int pickSupplementalActionsLayout(boolean viewed) {
        if (viewed) {
            return R.layout.fragment_card_view_actions_viewed;
        }
        else {
            return R.layout.fragment_card_view_actions_unviewed;
        }
    }

    private int pickDrawableForCard(String title) {
        for (String key : drawableMap.keySet()) {
            if (title.toLowerCase().contains(key)) {
                return drawableMap.get(key);
            }
        }

        return R.drawable.card_picture_default;
    }

    private ArrayList<BaseSupplementalAction> makeCardActions(final EventsDataSource db) {
        ArrayList<BaseSupplementalAction> actions = new ArrayList<>();
        IconSupplementalAction editEvent = new IconSupplementalAction(getActivity(), R.id.editEvent);
        editEvent.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
            @Override
            public void onClick(Card card, View view) {
                editEventFromID(Long.parseLong(card.getId()));
            }
        });
        actions.add(editEvent);

        IconSupplementalAction deleteEvent = new IconSupplementalAction(getActivity(), R.id.deleteEvent);
        deleteEvent.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
            @Override
            public void onClick(Card card, View view) {
                removeEventByIndex(Long.parseLong(card.getId()), db);
            }
        });
        actions.add(deleteEvent);

        IconSupplementalAction shareEvent = new IconSupplementalAction(getActivity(), R.id.shareEvent);
        shareEvent.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
            @Override
            public void onClick(Card card, View view) {
                boolean loggedIn = checkLoggedIn();
                if (loggedIn) {

                    // Checking for duplicate events
                    for (int i = 0; i < eventList.size(); i++) {
                        Long eventIndexLong = Long.parseLong(card.getId());
                        int eventIndexInt = eventIndexLong.intValue();

                        // Make sure we dont look at the same event as the one we're checking with
                        if (i != eventIndexInt) {
                            Calendar currentCardEventDate = db.findEventByID(eventList.get(eventIndexInt).getID()).getStartDate();
                            boolean sameDate = eventList.get(i).getStartDate().equals(currentCardEventDate);

                            if (sameDate) {
                                AlertDialog dialog = makeDuplicateEventAlertDialog(eventIndexLong, getString(R.string.duplicate_event_heading),
                                        getString(R.string.duplicate_event_msg) + " \"" + eventList.get(i).getTitle() + "\"", getString(R.string.decline_cancel_duplicate_event), getString(R.string.accept_edit_duplicate_event));
                                dialog.show();

                                Log.e("ANOTHER EVENT-SAME TIME", "SAME TIME " + eventList.get(i).getStartDate() + " AS " + eventList.get(i).getTitle());
                            }
                        }
                    }

                    String username = getUserName();
                    Integer userToken = getUserToken();
                    boolean published = publishEventByID(username, userToken, Long.parseLong(card.getId()));
                    if (published) {
                        MaterialLargeImageCard mCard = (MaterialLargeImageCard) card;
                        mCard.setSubTitle(mCard.getSubTitle() + "\n" + getString(R.string.eventlist_card_shared));
                    }
                }

                else {
                    AlertDialog dialog = makeLoginAlertDialog(getString(R.string.main_not_logged_in),
                            getString(R.string.main_not_logged_in_message), getString(R.string.app_login), getString(R.string.dialog_cancel));
                    dialog.show();

                }
            }
        });
        actions.add(shareEvent);
        return actions;
    }

    private void removeEventByIndex(Long eventIndex, EventsDataSource db) {
        int eventIndexInt = eventIndex.intValue();
        db.deleteEvent(db.findEventByID(eventList.get(eventIndexInt).getID()));
        UpdateCards updater = new UpdateCards();
        updater.execute(updater.REMOVE_MODE, eventIndexInt);
    }

    private void editEventFromID(Long eventIndex) {
        Intent intent = new Intent(getActivity(), EditEvent.class);
        intent.putExtra("eventID", eventList.get(eventIndex.intValue()).getID());
        startActivity(intent);
    }

    private boolean publishEventByID(String username, Integer userToken, Long ID) {
        EventsDataSource db = new EventsDataSource(getActivity());
        Event event = db.findEventByID(eventList.get(ID.intValue()).getID());
        MeetlyServer server = new MeetlyServer();
        LatLng location = event.getLocation();
                    try {
                        int sharedEventID = server.publishEvent(username, userToken, event.getTitle(), event.getStartDate(),
                                                                event.getEndDate(), location.latitude, location.longitude);
                        event.setSharedEventID(sharedEventID);
                        db.updateDatabaseEvent(event);
                        return true;

                    }
                    catch (MeetlyServer.FailedPublicationException e) {
                        Log.e(TAG, "Failed to publish event: " + event.getTitle());
                        return false;
                    }
    }

    private AlertDialog makeDuplicateEventAlertDialog(final Long eventIndex, String title, String message, String positiveButtonLabel, String negativeButtonLabel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveButtonLabel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getActivity(), EditEvent.class);
                intent.putExtra("eventID", eventList.get(eventIndex.intValue()).getID());
                startActivity(intent);
            }
        });
        builder.setNegativeButton(negativeButtonLabel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    private AlertDialog makeLoginAlertDialog(String title, String message, String positiveButtonLabel, String negativeButtonLabel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveButtonLabel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(negativeButtonLabel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    private boolean checkLoggedIn() {
        SharedPreferences preferences = getActivity().getSharedPreferences(Meetly.MEETLY_PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getBoolean(Meetly.MEETLY_PREFERENCES_ISLOGGEDIN, false);
    }

    private Integer getUserToken() {
        SharedPreferences preferences = getActivity().getSharedPreferences(Meetly.MEETLY_PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getInt(Meetly.MEETLY_PREFERENCES_USERTOKEN, -1);
    }

    private String getUserName() {
        SharedPreferences preferences = getActivity().getSharedPreferences(Meetly.MEETLY_PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getString(Meetly.MEETLY_PREFERENCES_USERNAME,
                getResources().getText(R.string.main_defaultLoginMessage).toString());
    }

    private String timeUntil(Date date) {
        long now = new Date().getTime();
        long eventTime = date.getTime();
        long diff = eventTime - now;

        if (diff <= 0) {
            return getString(R.string.eventlist_timeuntil_now);
        }

        final long hoursInDay = TimeUnit.DAYS.toHours(1);
        final long minutesInHour = TimeUnit.HOURS.toMinutes(1);

        long daysUntil = TimeUnit.MILLISECONDS.toDays(diff);
        long hoursUntil = TimeUnit.MILLISECONDS.toHours(diff) % hoursInDay;
        long minutesUntil = TimeUnit.MILLISECONDS.toMinutes(diff) % minutesInHour;

        return String.format(getString(R.string.eventlist_timeuntil_formatted_string_1) + "%02d"
                + getString(R.string.eventlist_timeuntil_formatted_string_2) + "%02d"
                + getString(R.string.eventlist_timeuntil_formatted_string_3) + "%02d"
                + getString(R.string.eventlist_timeuntil_formatted_string_4), daysUntil, hoursUntil, minutesUntil);
    }

    private List getEvents(EventsDataSource database) {
        return database.getAllEvents();
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

    /**
     * Inner class to allow for updating cards in the background (using the AsyncTask interface)
     */

    private class UpdateCards extends AsyncTask<Integer, Void, Integer[]> {
        public final int ADD_MODE = 1;
        public final int REMOVE_MODE = 2;
        public final int EDIT_MODE = 3;
        public final int CREATE_MODE = 0;
        public final int CLEAR_MODE = 4;
        EventsDataSource database = new EventsDataSource(getActivity());

        protected void onPreExecute() {
            cardsUpdating = true;
        }

        @Override
        protected Integer[] doInBackground(Integer... params) {
            if (params[0] == CREATE_MODE) {
                Log.i(TAG, "Creating cards...");
                cards.clear();
                cards.addAll(makeCards(this.database));
            }
            else if (params[0] == ADD_MODE) {
                Log.i(TAG, "Adding card at index: " + params[1]);
                eventList = getEvents(database);
                MaterialLargeImageCard card = makeMaterialLargeImageCard(makeCardActions(database), eventList.get(params[1]), params[1]);
                cards.add(card);
                params[1] = cards.indexOf(card);
            }
            else if (params[0] == REMOVE_MODE) {
                Log.i(TAG, "Removing card at index: " + params[1]);
                if (eventList.size() > 1) {
                    cards.remove(params[1].intValue());
                    eventList.remove(params[1].intValue());
                    for (int i = params[1]; i < cards.size(); i++) {
                        Long oldPos = Long.parseLong(cards.get(i).getId());
                        Long newPos = oldPos - 1;
                        cards.get(i).setId("" + newPos);
                    }
                }
                else {
                    cards.clear();
                    params[0] = CLEAR_MODE;
                }
            }
            else if (params[0] == EDIT_MODE) {
                Log.i(TAG, "Refreshing card at index: " + params[1]);
            }

            return params;
        }

        @Override
        protected void onPostExecute(Integer[] result) {

            if (result[0] == CREATE_MODE) {
                Log.i(TAG, "Card update finished.");
                mCardArrayAdapter.notifyDataSetChanged();
            }
            else if (result[0] == ADD_MODE) {
                Log.i(TAG, "Card added at index: " + result[1]);
                mCardArrayAdapter.notifyItemInserted(result[1]);
            }
            else if (result[0] == REMOVE_MODE) {
                Log.i(TAG, "Card removed at index: " + result[1]);
                mCardArrayAdapter.notifyItemRemoved(result[1]);
            }
            else if (result[0] == EDIT_MODE) {
                Log.i(TAG, "Card refreshed at index: " + result[1]);
                mCardArrayAdapter.notifyItemChanged(result[1]);
            }
            else if (result[0] == CLEAR_MODE) {
                Log.i(TAG, "Cards cleared");
                mCardArrayAdapter.notifyDataSetChanged();
            }

            cardsUpdating = false;
        }

    }


}
