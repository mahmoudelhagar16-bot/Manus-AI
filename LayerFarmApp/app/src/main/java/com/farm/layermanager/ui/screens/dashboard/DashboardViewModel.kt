package com.farm.layermanager.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farm.layermanager.domain.usecase.reports.DashboardSnapshot
import com.farm.layermanager.domain.usecase.reports.GetDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboardUseCase: GetDashboardUseCase
) : ViewModel() {

    val snapshot: StateFlow<DashboardSnapshot?> = getDashboardUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
