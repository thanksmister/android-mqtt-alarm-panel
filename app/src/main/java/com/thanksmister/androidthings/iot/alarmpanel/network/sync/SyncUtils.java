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

package com.thanksmister.androidthings.iot.alarmpanel.network.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.thanksmister.androidthings.iot.alarmpanel.R;

import static android.content.Context.ACCOUNT_SERVICE;

public class SyncUtils {

    /**
     * Request immediate sync 
     * @param context
     */
    public static void requestSyncNow(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY, true);
        ContentResolver.requestSync(getSyncAccount(context), SyncProvider.CONTENT_AUTHORITY, bundle);
    }

    /**
     * Cancel any ongoing syncs
     * @param context
     */
    public static void cancelSync(Context context) {
        ContentResolver.cancelSync(getSyncAccount(context), SyncProvider.CONTENT_AUTHORITY);
    }

    /**
     * Get existing account or create a new account if non exists
     * @param context
     * @return
     */
    public static Account getSyncAccount(Context context) {
        String acctType = context.getString(R.string.account_type);
        //Account[] accounts = AccountManager.get(context).getAccountsByType(acctType);
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
       /* int j = accounts.length;
        int i = 0;
        while (i < j) {
            Account localAccount = accounts[i];
            if (localAccount.name.equals(context.getString(R.string.app_name))) {
                return localAccount;
            }
            i += 1;
        }*/
        Account account = new Account(context.getString(R.string.app_name), acctType);
        accountManager.addAccountExplicitly(account, null, null);
        return account;
    }

    /**
     * Delete any sync accounts
     * @param context
     * @param paramString
     */
    /*public static void deleteSyncAccounts(Context context, String paramString) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(paramString);
        int j = accounts.length;
        int i = 0;
        while (i < j) {
            accountManager.removeAccount(accounts[i], null, null);
            i += 1;
        }
    }*/
}