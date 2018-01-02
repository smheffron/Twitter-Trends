package com.shelby.twittertrends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mopub.common.util.Json;

import java.util.ArrayList;

/**
 * Created by Shelby on 12/31/17.
 */
public class CustomAdapter extends ArrayAdapter<JsonData> implements View.OnClickListener{

    private ArrayList<JsonData> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtType;

    }

    public CustomAdapter(ArrayList<JsonData> data, Context context) {
        super(context, R.layout.column_data, data);
        this.dataSet = data;
        this.mContext=context;

    }

    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        JsonData dataModel=(JsonData) object;


    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        JsonData dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.column_data, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.name);
            viewHolder.txtType = (TextView) convertView.findViewById(R.id.type);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }



        try {
            viewHolder.txtName.setText(dataModel.getName());
            viewHolder.txtType.setText(dataModel.getType());
        }
        catch (NullPointerException e)
        {

        }

        // Return the completed view to render on screen
        return convertView;
    }
}