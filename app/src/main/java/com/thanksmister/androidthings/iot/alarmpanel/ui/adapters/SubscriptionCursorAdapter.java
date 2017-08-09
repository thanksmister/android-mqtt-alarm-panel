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

package com.thanksmister.androidthings.iot.alarmpanel.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.thanksmister.androidthings.iot.alarmpanel.R;
import com.thanksmister.androidthings.iot.alarmpanel.data.database.Db;
import com.thanksmister.androidthings.iot.alarmpanel.data.database.model.SubscriptionModel;
import com.thanksmister.androidthings.iot.alarmpanel.utils.DateUtils;

public class SubscriptionCursorAdapter extends CursorAdapter {

    /**
     * Constructor that allows control over auto-requery. 
     *
     * @param context The context
     * @param c The cursor from which to get the data.
     * @param autoRequery If true the adapter will call requery() on the
     * cursor whenever it changes so the most recent
     */
    public SubscriptionCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    /**
     * Makes a new view to hold the data pointed to by cursor.
     *
     * @param context Interface to application's global information
     * @param cursor The cursor from which to get the data. The cursor is already
     * moved to the correct position.
     * @param parent The parent to which the new view is attached to
     * @return the newly created view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.adapter_data_row, parent, false);
    }

    /**
     * Bind an existing view to the data pointed to by cursor
     *
     * @param view Existing view, returned earlier by newView
     * @param context Interface to application's global information
     * @param cursor The cursor from which to get the data. The cursor is already
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView topicTextView = (TextView) view.findViewById(R.id.topicText);
        TextView messageTextView = (TextView) view.findViewById(R.id.messageText);
        TextView dateTextView = (TextView) view.findViewById(R.id.dateText);
        
        String topic = Db.getString(cursor, SubscriptionModel.TOPIC);
        String payload = Db.getString(cursor, SubscriptionModel.PAYLOAD);
        String date = DateUtils.parseCreatedAtDate(Db.getString(cursor, SubscriptionModel.CREATED_AT));
        
        topicTextView.setText(topic);
        messageTextView.setText(payload);
        dateTextView.setText(date);
    }
}