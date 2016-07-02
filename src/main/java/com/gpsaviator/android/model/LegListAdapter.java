package com.gpsaviator.android.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gpsaviator.Coordinate;
import com.gpsaviator.Route.Leg;
import com.gpsaviator.Unit;
import com.gpsaviator.UnitConversion;
import com.gpsaviator.android.R;
import lombok.Getter;
import lombok.Setter;

public class LegListAdapter extends BaseAdapter {


	private List<Leg> legs;
	private Location location;

	public void updateLegs(ArrayList<Leg> list) {
		this.legs = list;
	}

	private static class ViewHolder {

		public TextView distanceToWaypoint;
		public TextView toWaypoint;
		public TextView trackToWaypoint;
		public TextView timeToWaypoint;
	}

	private final int viewResourceID;

	@Getter
	@Setter
	private boolean showTotals;

	private Integer activeLeg;
	private double totalOffset;

	private Context context;

	public LegListAdapter(Context context, int textViewResourceId) {
		super();
		viewResourceID = textViewResourceId;
		this.context = context;
		this.showTotals = false;
		this.activeLeg = null;
		this.location = null;
		this.legs = null;
	}

	private Coordinate getLocationCoordinate() {
		return CoordinateImplFactory.getInstance().create(location.getLatitude(), location.getLongitude());
	}

	private void updateTotalOffset() {
		if (activeLeg != null && location != null) {
			this.totalOffset = legs.get(activeLeg).getDistanceFromPreviousLegs() + legs.get(activeLeg).getRange() - getLocationCoordinate().rangeTo(this.legs.get(activeLeg).getEnd().getCoord());
		} else {
			this.totalOffset = 0.0;
		}
	}

	public void updateLegAndLocation(Integer newActiveLeg, Location location) {
		this.activeLeg = newActiveLeg;
		this.location = location;
		updateTotalOffset();
	}

	@Override
	public int getCount() {
		return legs != null ? legs.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return legs.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getView(final int pos, final View convertView, final ViewGroup parent) {

		// We need to get the best view (re-used if possible) and then
		// retrieve its corresponding ViewHolder, which optimizes lookup
		// efficiency
		final View view = getWorkingView(convertView);
		final ViewHolder viewHolder = getViewHolder(view);

		final Leg leg = legs.get(pos);

		viewHolder.toWaypoint.setText(leg.getEnd().getIdent());
		viewHolder.trackToWaypoint.setText(String.format("%03.0f%c", Math.toDegrees(leg.getMagCourse()),
				(char) 0x00B0));
		double range = 0;
		String timeTo = context.getResources().getString(R.string.blankTime);
		if (activeLeg == null || pos >= activeLeg) {
			range = leg.getRange();
			if (showTotals) {
				range += leg.getDistanceFromPreviousLegs() - totalOffset;
				if (range < 0) range = 0;
			} else if (activeLeg != null && pos == activeLeg && location != null) {
				range = getLocationCoordinate().rangeTo(this.legs.get(activeLeg).getEnd().getCoord());
			}

//			if (location != null) location.setSpeed((float)UnitConversion.convert(60, Unit.NAUTICAL_MILES, Unit.HOURS, Unit.METRES, Unit.SECONDS));
			if (activeLeg != null && location != null && location.getSpeed() > 2) {
				double timeInMinutes = 0.5 + (UnitConversion.convert(range, Unit.RADIANS, Unit.METRES) / location.getSpeed()) / 60;
				if (timeInMinutes < 600) {	// Only show if less than 10 hours
					int hours = (int) timeInMinutes / 60;
					int mins = (int) timeInMinutes % 60;
					timeTo = String.format("%d:%02d", hours, mins);
				}
			}
		}
		viewHolder.distanceToWaypoint.setText(String.format("%.1f", UnitConversion.convert(range, Unit.RADIANS, Unit.NAUTICAL_MILES)+0.05));
		viewHolder.timeToWaypoint.setText(timeTo);

		return view;
	}

	private ViewHolder getViewHolder(View workingView) {
		// The viewHolder allows us to avoid re-looking up view references
		// Since views are recycled, these references will never change
		final Object tag = workingView.getTag();
		ViewHolder viewHolder = null;

		if (null == tag || !(tag instanceof ViewHolder)) {
			viewHolder = new ViewHolder();
			viewHolder.toWaypoint = (TextView) workingView.findViewById(R.id.toWaypoint);
			viewHolder.trackToWaypoint = (TextView) workingView.findViewById(R.id.trackToWaypoint);
			viewHolder.distanceToWaypoint = (TextView) workingView.findViewById(R.id.distanceToWaypoint);
			viewHolder.timeToWaypoint = (TextView) workingView.findViewById(R.id.timeToWaypoint);
			workingView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) tag;
		}
		return viewHolder;
	}

	private View getWorkingView(View convertView) {
		// The workingView is basically just the convertView re-used if possible
		// or inflated new if not possible
		View workingView = null;

		if (null == convertView) {
			final LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			workingView = inflater.inflate(viewResourceID, null);
		} else {
			workingView = convertView;
		}

		return workingView;

	}

}
