package com.mindsapp.test.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mindsapp.test.R;

import java.util.List;

/**
 * Created by Daniele on 18/01/2016.
 *
 */
public class ThresholdAdapter extends ArrayAdapter<Threshold> {

    public ThresholdAdapter(Context context, int textViewResourceId,
                                 List<Threshold> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getViewOptimize(position, convertView, parent);
    }

    public View getViewOptimize(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.rowcustom, null);
            viewHolder = new ViewHolder();
            viewHolder.place = (TextView)convertView.findViewById(R.id.textViewPlace);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Threshold threshold = getItem(position);
        viewHolder.place.setText(threshold.getPlace());
        return convertView;
    }

    private class ViewHolder {
        public TextView place;
    }
}
