package com.cmpt276.meetly;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public boolean showingCrouton;
    private List<Event> eventList = new ArrayList<>(0);
//    ProgressDialog dialog = null;
    private Map<String, Integer> drawableMap;


    public static EventList newInstance() {
        return new EventList();
    }

    public EventList() {
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
        createFloatingActionButtonListeners();
        configureRecyclerView();

//        dialog = new ProgressDialog(getActivity());
//        dialog.setIndeterminate(true);
//        dialog.setMessage(getString(R.string.fragment_event_update_loading_text));

        createDrawableMap();
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
//        if (getEvents(new EventsDataSource(getActivity())).size() - eventList.size() == 1
//                && eventList.size() != 1) {
//            UpdateCards updater = new UpdateCards();
//            updater.execute(updater.ADD_MODE, eventList.size());
//        }
//        else {
            UpdateCards updater = new UpdateCards();
            updater.execute(updater.CREATE_MODE, eventList.size());
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (dialog != null) {
//            dialog.dismiss();
//        }
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

        Log.d(TAG, "eventList size: " + eventList.size());
        ArrayList<Card> cards = new ArrayList<>();
        ArrayList<BaseSupplementalAction> actions = makeCardActions(database);
        int eventIndex = 0;
        for (Event event: eventList) {
            MaterialLargeImageCard card = makeMaterialLargeImageCard(actions, event, eventIndex);
            cards.add(card);
            eventIndex++;
        }
        Log.i(TAG, "Cards generated.");
        return cards;
    }

    private MaterialLargeImageCard makeMaterialLargeImageCard(ArrayList<BaseSupplementalAction> actions, Event event, int eventIndex) {
        final long eventID = event.getID();
        Log.d(TAG, "eventID: " + eventID);

        MaterialLargeImageCard card = MaterialLargeImageCard.with(getActivity())
                .setTextOverImage(event.getTitle())
                .setTitle(event.getDate().toString())
                .setSubTitle(timeUntil(event.getDate()) + "\n" + getString(R.string.eventlist_card_unshared))
                .useDrawableId(pickDrawableForCard(event.getTitle()))
                .setupSupplementalActions(R.layout.fragment_card_view_actions, actions)
                .build();
        card.addCardHeader(new CardHeader(getActivity()));

        card.setId("" + eventIndex);
        card.setCardElevation(10);

        Log.d(TAG, "Card ID: " + card.getId());
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
        final int MILLIS_IN_HOUR = 3600000;

        EventsDataSource db = new EventsDataSource(getActivity());
        Event event = db.findEventByID(eventList.get(ID.intValue()).getID());
        MeetlyTestServer server = new MeetlyTestServer();
        LatLng location = event.getLocation();
//                    try {
                        Calendar startTime = new GregorianCalendar();
                        startTime.setTime(event.getDate());
                        Calendar endTime = new GregorianCalendar();
                        Long endTimeInMillis = event.getDate().getTime() + (event.getDuration() * MILLIS_IN_HOUR);
                        endTime.setTimeInMillis(endTimeInMillis);
                        int sharedEventID = server.publishEvent(username, userToken, event.getTitle(), startTime,
                                                                endTime, location.latitude, location.longitude);
//                        event.setSharedID(sharedEventID);
//                        db.updateDatabaseEvent(event);
                        return true;

//                    }
//                    catch (IMeetlyServer.FailedPublicationException e) {
//                        Log.e(TAG, "Failed to publish event: " + event.getTitle());
//                        return false;
//                    }
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
                .setHeight(150)
                .setConfiguration(config)
                .build();

        final Crouton crouton = Crouton.makeText(getActivity(), getLocation(), style);
        crouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crouton.hide();
            }
        });
        crouton.setLifecycleCallback(callback);
        return crouton;
    }

    private String getLocation() {
        String location = getString(R.string.no_location_found);

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Activity.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        Location myLocation = locationManager.getLastKnownLocation(provider);
        Geocoder geocoder = new Geocoder(getActivity());
        try {
            List<Address> addresses = geocoder.getFromLocation(myLocation.getLatitude(), myLocation.getLongitude(), 5);
            location = addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea();
        }
        catch (Exception e) {
            Log.e(TAG, "geocoder.getFromLocation failed");
        }
        return location;
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
        EventsDataSource database = new EventsDataSource(getActivity());

        protected void onPreExecute() {
//            dialog.show();
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
                cards.remove(params[1].intValue());
                eventList.remove(params[1].intValue());
                for (int i = params[1]; i < cards.size(); i++) {
                    Long oldPos = Long.parseLong(cards.get(i).getId());
                    Long newPos = oldPos - 1;
                    cards.get(i).setId("" + newPos);
                }
            }
            else if (params[0] == EDIT_MODE) {
                Log.i(TAG, "Refreshing card at index: " + params[1]);
            }

            return params;
        }

        @Override
        protected void onPostExecute(Integer[] result) {
//            if (dialog.isShowing()) {
//                dialog.dismiss();
//            }

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
        }

    }


}
