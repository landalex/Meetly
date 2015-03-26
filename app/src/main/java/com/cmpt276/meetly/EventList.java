package com.cmpt276.meetly;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
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
 * Provides the events in a list of cards, with the ability to select events to view
 * Scroll bar (draggable), Swipe for options (View, edit, delete, invite)
 * Each item shows title, date/time and duration (location as well?)
 */
public class EventList extends Fragment {

    private final String TAG = "EventListFragment";

    private OnFragmentInteractionListener mListener;

    private CardArrayRecyclerViewAdapter mCardArrayAdapter;
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

        ArrayList<Card> cards = new ArrayList<>();
        for (Event event: eventList) {
            final long eventID = event.getID();
            ArrayList<BaseSupplementalAction> actions = makeCardActions(eventID);

            MaterialLargeImageCard card = MaterialLargeImageCard.with(getActivity())
                    .setTextOverImage(event.getTitle())
                    .setTitle(event.getDate().toString())
                    .setSubTitle(timeUntil(event.getDate()) + "\n" + getString(R.string.eventlist_card_unshared))
                    .useDrawableId(R.drawable.card_picture)
                    .setupSupplementalActions(R.layout.fragment_card_view_actions, actions)
                    .build();
            card.addCardHeader(new CardHeader(getActivity()));

            // Pass the event ID with the intent to ViewEvent
            card.setOnClickListener(new Card.OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    Intent intent = new Intent(getActivity(), ViewEvent.class);
                    intent.putExtra("eventID", eventID);
                    startActivity(intent);
                }
            });

            cards.add(card);
        }
        Log.i(TAG, "Cards generated.");
        return cards;
    }

    private ArrayList<BaseSupplementalAction> makeCardActions(final long eventID) {
        ArrayList<BaseSupplementalAction> actions = new ArrayList<>();
        IconSupplementalAction editEvent = new IconSupplementalAction(getActivity(), R.id.editEvent);
        editEvent.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Intent intent = new Intent(getActivity(), EditEvent.class);
                intent.putExtra("eventID", eventID);
                startActivity(intent);
            }
        });
        actions.add(editEvent);

        IconSupplementalAction deleteEvent = new IconSupplementalAction(getActivity(), R.id.deleteEvent);
        deleteEvent.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
            @Override
            public void onClick(Card card, View view) {
                EventsDataSource db = new EventsDataSource(getActivity());
                db.deleteEvent(db.findEventByID(eventID));
                new UpdateCards().execute();
            }
        });
        actions.add(deleteEvent);

        IconSupplementalAction shareEvent = new IconSupplementalAction(getActivity(), R.id.shareEvent);
        shareEvent.setOnActionClickListener(new BaseSupplementalAction.OnActionClickListener() {
            @Override
            public void onClick(Card card, View view) {
                String username = null;
                int userToken = -1;
                boolean loggedIn = checkLoggedIn(username, userToken);
                if (loggedIn) {
                    EventsDataSource db = new EventsDataSource(getActivity());
                    Event event = db.findEventByID(eventID);
                    MeetlyTestServer server = new MeetlyTestServer();
                    LatLng location = event.getLocation();
//                    try {
//                        int sharedEventID = server.publishEvent(username, userToken, event.getTitle(), event.getDate(),
//                                                                event.getDuration(), location.latitude, location.longitude);
//                        event.setSharedID(sharedEventID);
//                        db.updateEvent(event);
//                        MaterialLargeImageCard mCard = (MaterialLargeImageCard) card;
//                        mCard.setSubTitle(timeUntil(event.getDate()) + "\n" + getString(R.string.eventlist_card_shared));
//                    }
//                    catch (IMeetlyServer.FailedPublicationException e) {
//                        Log.e(TAG, "Failed to publish event: " + event.getTitle());
//                    }
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

    private boolean checkLoggedIn(String username, int userToken) {
        SharedPreferences preferences = getActivity().getSharedPreferences(Meetly.MEETLY_PREFERENCES, Context.MODE_PRIVATE);
        boolean loggedIn = preferences.getBoolean(Meetly.MEETLY_PREFERENCES_ISLOGGEDIN, false);
        if (loggedIn) {
            username = preferences.getString(Meetly.MEETLY_PREFERENCES_USERNAME,
                        getResources().getText(R.string.main_defaultLoginMessage).toString());
            userToken = preferences.getInt(Meetly.MEETLY_PREFERENCES_USERTOKEN, -1);
        }

        return loggedIn;
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

        return String.format(getString(R.string.eventlist_timeuntil_formatted_string), daysUntil, hoursUntil, minutesUntil);
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

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

        // Criteria specifies the 'criteria' of how granular the location is
        Criteria criteria = new Criteria();

        // Get the name of the best provider. AKA Returns the name of the provider that best meets the given criteria.
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
            Log.i(TAG, "Updating cards...");
            cards.removeAll(cards);
            cards.addAll(makeCards(this.database));
            Log.i(TAG, "Card update finished.");
            return 1;
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
