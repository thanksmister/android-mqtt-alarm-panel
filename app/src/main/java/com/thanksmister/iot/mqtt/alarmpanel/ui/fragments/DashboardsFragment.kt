/*
 * Copyright (c) 2018 ThanksMister LLC
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

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Dashboard
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Sensor
import com.thanksmister.iot.mqtt.alarmpanel.ui.adapters.DashboardAdapter
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.EditDashboardDialogView
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.DashboardsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_dashboards.*
import kotlinx.android.synthetic.main.fragment_dashboards.view.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class DashboardsFragment : BaseFragment(), DashboardAdapter.OnItemClickListener {

    @Inject
    lateinit var viewModel: DashboardsViewModel
    @Inject
    lateinit var dialogUtils: DialogUtils

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)

        observeViewModel(viewModel)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.title = ("Dashboards")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = view.context
        dashboardsList.layoutManager = LinearLayoutManager(context)
        dashboardsList.adapter = DashboardAdapter(ArrayList<Dashboard>(), this)
        addDashboardButton.setOnClickListener {
            showDashboardEditDialog(Dashboard())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboards, container, false)
    }

    private fun observeViewModel(viewModel: DashboardsViewModel) {
        disposable.add(viewModel.getItems()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ items ->
                    dashboardsList.adapter = DashboardAdapter(items, this)
                    dashboardsList.invalidate()
                }, { error -> Timber.e("Unable to get sensors: " + error) }))
    }

    companion object {
        fun newInstance(): DashboardsFragment {
            return DashboardsFragment()
        }
    }

    override fun onItemClick(dashboard: Dashboard) {
        showDashboardEditDialog(dashboard)
    }

    private fun showDashboardEditDialog(dashboard: Dashboard) {
        dialogUtils.showDashboardDialog(requireContext(), dashboard, object : EditDashboardDialogView.ViewListener {
            override fun onUpdate(dashboard: Dashboard) {
                viewModel.insertItem(dashboard)
                dialogUtils.clearDialogs()
            }

            override fun onRemove(dashboard: Dashboard) {
                viewModel.deleteItem(dashboard)
                dialogUtils.clearDialogs()
            }

            override fun onEmpty() {
                Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                dialogUtils.clearDialogs()
            }

            override fun onCancel() {
                dialogUtils.clearDialogs()
            }
        })
    }
}