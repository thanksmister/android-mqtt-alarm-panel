package com.thanksmister.iot.mqtt.alarmpanel.ui.adapters

import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Datum
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.WeatherUtils

import kotlinx.android.synthetic.main.adapter_forcast_card.view.*

class ForecastCardAdapter(private val items: List<Datum>?) : RecyclerView.Adapter<ForecastCardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastCardAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.adapter_forcast_card, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        if (items == null) return 0
        return if (items.isNotEmpty()) items.size else 0
    }

    override fun onBindViewHolder(holder: ForecastCardAdapter.ViewHolder, position: Int) {
        holder.bindItems(items!![position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(datum: Datum) {
            val highTemp = Math.round(datum.apparentTemperatureMax!!).toString()
            val lowTemp = Math.round(datum.apparentTemperatureMin!!).toString()
            itemView.temperatureText.text = itemView.context.getString(R.string.text_temperature_range, highTemp, lowTemp)
            itemView.dayText.text = DateUtils.dayOfWeek(datum.time!!)
            itemView.outlookText.text = datum.summary
            itemView.iconImage.setImageDrawable(ResourcesCompat.getDrawable(itemView.context.resources,
                    WeatherUtils.getIconForWeatherCondition(datum.icon), itemView.context.theme))
        }
    }
}