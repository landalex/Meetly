package com.cmpt276.meetly;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;



import com.cmpt276.meetly.dummy.DummyContent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

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

        // TODO: Change Adapter to display your content
//        mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
//                android.R.layout.simple_list_item_2, android.R.id.text1, DummyContent.ITEMS);
        createAdapter();
    }

    private void createAdapter() {
        final String[] fromMapKey = {EVENT_TITLE, EVENT_DATE/*, EVENT_LOCATION, EVENT_COUNTDOWN*/};
        final int[] toLayoutId = {android.R.id.text1, android.R.id.text2/*, android.R.id.text3, android.R.id.text4*/};
        List<Map<String, String>> eventList = getEventList();


        mAdapter = new SimpleAdapter(getActivity(), eventList, android.R.layout.simple_list_item_2,
                fromMapKey, toLayoutId);
    }

    private List getEventList() {
        final List<Map<String, String>> eventMapList = new ArrayList<>();

        List<Event> eventList = database.getAllEvents();

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
        testEvents.add(new Event("Tims Run", new Date()));
        testEvents.add(new Event("Lunch", new Date()));
        testEvents.add(new Event("Dinner", new Date()));

        return testEvents;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

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
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
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
