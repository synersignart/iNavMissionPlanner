package de.meek.inavmissionplanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class BoxModeAdapter extends ArrayAdapter<BoxMode> implements View.OnClickListener{

    private ArrayList<BoxMode> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtIndex;
        TextView txtBox;
        TextView txtAux;
        TextView txtStartEnd;
    }

    public BoxModeAdapter(ArrayList<BoxMode> data, Context context) {
        super(context, R.layout.boxmode_item, data);
        this.dataSet = data;
        this.mContext=context;

    }

    public void refreshEvents(ArrayList<BoxMode> newData) {
        this.dataSet.clear();
        this.dataSet.addAll(newData);
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        BoxMode dataModel=(BoxMode)object;
/*
        switch (v.getId())
        {
            case R.id.item_info:
                Snackbar.make(v, "Release date " +dataModel.getFeature(), Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
                break;
        }*/
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BoxMode dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.boxmode_item, parent, false);
           // viewHolder.txtIndex = (TextView) convertView.findViewById(R.id.index);
            viewHolder.txtBox = (TextView) convertView.findViewById(R.id.box);
            viewHolder.txtAux = (TextView) convertView.findViewById(R.id.aux);
            viewHolder.txtStartEnd = (TextView) convertView.findViewById(R.id.startEnd);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        lastPosition = position;

        //viewHolder.txtIndex.setText(""+dataModel.m_index);
        viewHolder.txtBox.setText(Const.BoxModeId2String(dataModel.m_permanentId));
        String aux = "";
        String range = "";
        if (dataModel.m_start != dataModel.m_end) {
            aux = "Aux " + (dataModel.m_aux+1);
            range = ""+dataModel.m_start + ".." + dataModel.m_end;
        }
        viewHolder.txtAux.setText(aux);
        viewHolder.txtStartEnd.setText(range);
//        viewHolder.info.setOnClickListener(this);
//        viewHolder.info.setTag(position);
        // Return the completed view to render on screen
        return convertView;
    }
}

