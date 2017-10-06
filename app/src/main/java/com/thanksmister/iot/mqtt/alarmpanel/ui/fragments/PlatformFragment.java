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
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment;
import com.thanksmister.iot.mqtt.alarmpanel.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PlatformFragment extends BaseFragment {
    
    @Bind(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    
    @Bind(R.id.webView)
    WebView webView;
    
    public PlatformFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static PlatformFragment newInstance() {
        return new PlatformFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadWebPage();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Timber.i("onRefresh called from SwipeRefreshLayout");
                        loadWebPage();
                    }
                }
        );
    }
    
    private void loadWebPage(){
        if(getConfiguration().showHassModule()
                && !TextUtils.isEmpty(getConfiguration().getHassUrl()) && webView != null) {
            webView.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int newProgress){
                    if(newProgress == 100){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            webView.loadUrl(getConfiguration().getHassUrl());
        } else if (webView != null) {
            webView.loadUrl("about:blank");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_platform, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ButterKnife.unbind(this);
    }

    public class PlatformWebChromeClient extends WebChromeClient {

        public void onPageFinished(WebView view, String url) {
            // do your stuff here
        }
    }
}