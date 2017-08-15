package com.thanksmister.iot.mqtt.alarmpanel.ui.adapters;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Datum;
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils;
import com.thanksmister.iot.mqtt.alarmpanel.utils.WeatherUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ForecastCardAdapter extends RecyclerView.Adapter<ForecastCardAdapter.ViewHolder> {
    
    private List<Datum> items;
    private Context context;
    private String units;

    public ForecastCardAdapter(Context context, List<Datum> items, String units) {
        this.context = context;
        this.items = items;
        this.units = units;
    }
    
    // Create new views (invoked by the layout manager)
    @Override
    public ForecastCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(context).inflate(viewType, parent, false);
        return new ItemViewHolder(itemLayoutView);
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.adapter_forcast_card;
    }

    @Override
    public int getItemCount() {
        if (items == null) return 0;
        return items.size() > 0? items.size():0;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ItemViewHolder) {
            Datum datum = items.get(position);
            ((ItemViewHolder) viewHolder).iconImage.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), 
                    WeatherUtils.getIconForWeatherCondition(datum.getIcon()), context.getTheme()));
            ((ItemViewHolder) viewHolder).dayText.setText(DateUtils.dayOfWeek(datum.getTime()));
            ((ItemViewHolder) viewHolder).outlookText.setText(datum.getSummary());

            //String displayUnits = (units.equals(DarkSkyRequest.UNITS_US)? context.getString(R.string.text_f): context.getString(R.string.text_c));
            String highTemp = String.valueOf(Math.round(datum.getApparentTemperatureMax()));
            String lowTemp = String.valueOf(Math.round(datum.getApparentTemperatureMin()));
            ((ItemViewHolder) viewHolder).temperatureText.setText(context.getString(R.string.text_temperature_range, highTemp, lowTemp));
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class ItemViewHolder extends ViewHolder {
        @Bind(R.id.iconImage)
        ImageView iconImage;

        @Bind(R.id.dayText)
        TextView dayText;

        @Bind(R.id.temperatureText)
        TextView temperatureText;

        @Bind(R.id.outlookText)
        TextView outlookText;

        ItemViewHolder(View itemView) {
            super(itemView);
        }
    }
}