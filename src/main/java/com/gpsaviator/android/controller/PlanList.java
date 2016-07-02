package com.gpsaviator.android.controller;

import android.app.Activity;
import android.location.Location;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ToggleButton;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.gpsaviator.Route;
import com.gpsaviator.android.R;
import com.gpsaviator.android.Utils;
import com.gpsaviator.android.model.LegListAdapter;

import java.util.ArrayList;

/**
 * Created by khaines on 26/05/2015.
 */
public class PlanList  {

    final private Activity parent;
    final private ListView planList;
    final private EventBus eventBus;
    private Route route;
    private Location location = null;

    private class SummaryClickHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            setupPlanList(false);
        }
    }

    private class ReverseClickHandler implements  View.OnClickListener {
        @Override
        public void onClick(View v) {
            route.reverse();
            eventBus.post(route);
            if (route.isActive()) planList.setItemChecked(route.getActiveLeg(), true);
        }
    }

    private class ListItemClickHandler implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int item, long id) {
            LegListAdapter adapter = (LegListAdapter) parent.getAdapter();

            if (route.isActive() && route.getActiveLeg() == item) {
                route.clearActiveLeg();
                planList.clearChoices();
            } else {
                route.setActiveLeg(item);
            }
            setupPlanList(false);
        }
    }

    public PlanList(View view, Route route, EventBus eventBus) {
        this.parent = (Activity) view.getContext();
        this.eventBus = eventBus;
        eventBus.register(this);
        this.route = route;
        this.planList = (ListView) parent.findViewById(R.id.planWaypointList);
        planList.setOnItemClickListener(new ListItemClickHandler());

        Utils.setClickHandler(parent, new SummaryClickHandler(), R.id.summaryTotalSwitch);
        Utils.setClickHandler(parent, new ReverseClickHandler(), R.id.reverseRoute);
        LegListAdapter adapter = new LegListAdapter(parent, R.layout.leg_item);
        planList.setAdapter(adapter);
        setupPlanList(true);
    }

    @Subscribe
    public void updateRoute(Route route) {
        this.route = route;
        setupPlanList(true);
    }

    @Subscribe
    public void updateLocation(Location newLocation) {
        this.location = newLocation;
        setupPlanList(false);
    }

    private void setupPlanList(boolean refreshList) {
        boolean showTotals = ((ToggleButton) parent.findViewById(R.id.summaryTotalSwitch)).isChecked();
        LegListAdapter adapter = (LegListAdapter) planList.getAdapter();
        if (refreshList) {
            ArrayList<Route.Leg> list = route.getLegs();
            planList.clearChoices();
            adapter.updateLegs(list);
            if (route.isActive()) {
                planList.setItemChecked(route.getActiveLeg(), true);
            }
        }
        adapter.setShowTotals(showTotals);
        adapter.updateLegAndLocation(route.getActiveLeg(), location);
        adapter.notifyDataSetChanged();
    }
}
