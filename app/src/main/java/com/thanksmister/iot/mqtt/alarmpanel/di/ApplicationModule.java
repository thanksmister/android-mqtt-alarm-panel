/*
 * Copyright (c) 2017. ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thanksmister.iot.mqtt.alarmpanel.di;

import android.app.Application;
import android.content.Context;

import com.thanksmister.iot.mqtt.alarmpanel.BaseApplication;
import com.thanksmister.iot.mqtt.alarmpanel.persistence.DarkSkyDao;
import com.thanksmister.iot.mqtt.alarmpanel.persistence.DarkSkyDatabase;
import com.thanksmister.iot.mqtt.alarmpanel.persistence.MessageDao;
import com.thanksmister.iot.mqtt.alarmpanel.persistence.MessageDatabase;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
abstract class ApplicationModule {

    @Binds
    abstract Application application(BaseApplication baseApplication);

    @Provides
    @Singleton
    static Context provideContext(Application application) {
        return application;
    }

    @Singleton
    @Provides
    static MessageDatabase provideDatabase(Application app) {
        return MessageDatabase.getInstance(app);
    }

    @Singleton
    @Provides
    static MessageDao provideMessageDao(MessageDatabase database) {
        return database.messageDao();
    }

    @Singleton
    @Provides
    static DarkSkyDatabase provideDarkSkyDatabase(Application app) {
        return DarkSkyDatabase.getInstance(app);
    }

    @Singleton
    @Provides
    static DarkSkyDao provideDarkSkyDao(DarkSkyDatabase database) {
        return database.darkSkyDao();
    }
}