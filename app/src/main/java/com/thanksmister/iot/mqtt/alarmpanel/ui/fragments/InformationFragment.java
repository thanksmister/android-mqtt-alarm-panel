/*
 * <!--
 *   ~ Copyright (c) 2017. ThanksMister LLC
 *   ~
 *   ~ Licensed under the Apache License, Version 2.0 (the "License");
 *   ~ you may not use this file except in compliance with the License. 
 *   ~ You may obtain a copy of the License at
 *   ~
 *   ~ http://www.apache.org/licenses/LICENSE-2.0
 *   ~
 *   ~ Unless required by applicable law or agreed to in writing, software distributed 
 *   ~ under the License is distributed on an "AS IS" BASIS, 
 *   ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *   ~ See the License for the specific language governing permissions and 
 *   ~ limitations under the License.
 *   -->
 */

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment;
import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyRequest;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Daily;
import com.thanksmister.iot.mqtt.alarmpanel.ui.modules.WeatherModule;
import com.thanksmister.iot.mqtt.alarmpanel.utils.WeatherUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static android.os.Looper.getMainLooper;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_ARM_AWAY_PENDING;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_ARM_HOME_PENDING;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_PENDING_TIME;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_TRIGGERED_PENDING;
import static java.lang.Math.round;

public class InformationFragment extends BaseFragment {

    public static final long WEATHER_DELAY_INTERVAL = 900000; // 1 hour

    @Bind(R.id.temperatureText)
    TextView temperatureText;

    @Bind(R.id.outlookText)
    TextView outlookText;
    
    @Bind(R.id.conditionImage)
    ImageView conditionImage;

    @Bind(R.id.timeText)
    TextView timeText;

    @Bind(R.id.dateText)
    TextView dateText;
    
    @Bind(R.id.weatherLayout)
    View weatherLayout;

    @OnClick(R.id.weatherLayout)
    void weatherLayoutClicked() {
        if(extendedDaily != null) {
            showExtendedForecastDialog(extendedDaily);
        }
    }
    
    private WeatherModule weatherModule;
    private Daily extendedDaily;
    private Handler weatherHandler;
    private Handler timeHandler;
    
    public InformationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static InformationFragment newInstance() {
        return new InformationFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        timeHandler = new Handler(getMainLooper());
        timeHandler.postDelayed(timeRunnable, 1000);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_information, container, false);
        ButterKnife.bind(this, fragmentView);
        return fragmentView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume");
        if(getConfiguration().showWeatherModule()
                && readWeatherOptions().isValid()) {
            weatherLayout.setVisibility(View.VISIBLE);
            if(weatherModule == null) {
                weatherModule = new WeatherModule();
            }
            checkAndDelayWeatherByMode();
        } else if(readWeatherOptions().hasUpdates()
                && getConfiguration().showWeatherModule()
                && readWeatherOptions().isValid()) {
            weatherLayout.setVisibility(View.VISIBLE);
            checkAndDelayWeatherByMode();
        } else if (!getConfiguration().showWeatherModule()
                || !readWeatherOptions().isValid()) {
            disconnectWeatherModule();
            weatherLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Timber.d("onDetach");
        disconnectWeatherModule();
        if(timeHandler != null) {
            timeHandler.removeCallbacks(timeRunnable);
            timeHandler = null;
        }
        if(weatherHandler != null) {
            weatherHandler.removeCallbacks(weatherRunnable);
            weatherHandler = null;
        }
    }
    
    private Runnable weatherRunnable = new Runnable() {
        @Override
        public void run() {
            connectWeatherModule();
            weatherHandler.removeCallbacks(weatherRunnable);
            weatherHandler = null;
        }
    };

    private Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            String currentDateString = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(new Date());
            String currentTimeString = DateFormat.getTimeInstance(DateFormat.DEFAULT, Locale.getDefault()).format(new Date());
            dateText.setText(currentDateString);
            timeText.setText(currentTimeString);
            if(timeHandler != null) {
                timeHandler.postDelayed(timeRunnable, 1000);
            }
        }
    };
    
    private void disconnectWeatherModule() {
        if(weatherModule != null) {
            weatherModule.cancelDarkSkyHourlyForecast();
            weatherModule = null;
        }
    }

    /**
     * We want to delay making a network call if we have a pending mode.
     */
    private void checkAndDelayWeatherByMode() {
        if(getConfiguration().getAlarmMode().equals(PREF_TRIGGERED_PENDING)
                || getConfiguration().getAlarmMode().equals(PREF_ARM_AWAY_PENDING)
                || getConfiguration().getAlarmMode().equals(PREF_ARM_HOME_PENDING)
                || getConfiguration().getAlarmMode().equals(PREF_PENDING_TIME) ) {
            if(weatherHandler != null) {
                weatherHandler = new Handler();
                weatherHandler.postDelayed(weatherRunnable, WEATHER_DELAY_INTERVAL);
            }
        } else {
            connectWeatherModule();
        }
    }
    
    private void connectWeatherModule() {
        if(weatherModule != null) {
            final String apiKey = readWeatherOptions().getDarkSkyKey();
            final String units = readWeatherOptions().getWeatherUnits();
            final String lat = readWeatherOptions().getLatitude();
            final String lon = readWeatherOptions().getLongitude();
            weatherModule.getDarkSkyHourlyForecast(apiKey, units, lat, lon, new WeatherModule.ForecastListener() {
                @Override
                public void onWeatherToday(String icon, double temperature, String summary) {
                    Timber.d("onWeatherToday icon: " + icon);
                    weatherLayout.setVisibility(View.VISIBLE);
                    outlookText.setText(summary);
                    String displayUnits = (units.equals(DarkSkyRequest.UNITS_US) ? getString(R.string.text_f) : getString(R.string.text_c));
                    temperatureText.setText(getString(R.string.text_temperature, String.valueOf(round(temperature)), displayUnits));
                    conditionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), WeatherUtils.getIconForWeatherCondition(icon), getActivity().getTheme()));
                }
                @Override
                public void onExtendedDaily(Daily daily) {
                    extendedDaily = daily;
                }
                @Override
                public void onShouldTakeUmbrella(boolean takeUmbrella) {
                    if (takeUmbrella) {
                        conditionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_rain_umbrella, getActivity().getTheme()));
                    }
                }
            });
        }
    }
}