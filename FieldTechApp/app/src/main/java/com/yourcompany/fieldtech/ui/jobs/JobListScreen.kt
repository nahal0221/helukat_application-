@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.yourcompany.fieldtech.ui.jobs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.fieldtech.data.local.entity.JobCacheEntity
import com.yourcompany.fieldtech.data.repository.FieldActivityRepository
import com.yourcompany.fieldtech.data.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JobListUiState(
    val jobs: List<JobCacheEntity> = emptyList(),
    val pendingSyncCount: Int = 0,
    val refreshing: Boolean = false,
    val offlineNotice: Boolean = false
)

@HiltViewModel
class JobListViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    fieldActivityRepository: FieldActivityRepository
) : ViewModel() {

    private val _state = MutableStateFlow(JobListUiState())
    val state: StateFlow<JobListUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                jobRepository.observeJobs(),
                fieldActivityRepository.observePendingSyncCount()
            ) { jobs, pending -> jobs to pending }
                .collect { (jobs, pending) ->
                    _state.value = _state.value.copy(jobs = jobs, pendingSyncCount = pending)
                }
        }
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(refreshing = true)
        viewModelScope.launch {
            val result = jobRepository.refreshJobs()
            _state.value = _state.value.copy(
                refreshing = false,
                offlineNotice = result.isFailure // stale cache above is still shown
            )
        }
    }
}

@Composable
fun JobListScreen(
    onJobClick: (Long) -> Unit,
    viewModel: JobListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Jobs") },
                actions = {
                    if (state.pendingSyncCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Sync, contentDescription = "Pending sync")
                            Spacer(Modifier.width(4.dp))
                            Text("${state.pendingSyncCount}")
                            Spacer(Modifier.width(12.dp))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (state.offlineNotice) {
                Surface(color = MaterialTheme.colorScheme.errorContainer) {
                    Text(
                        "Offline — showing cached jobs",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (state.refreshing && state.jobs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    items(state.jobs, key = { it.jobId }) { job ->
                        JobRow(job = job, onClick = { onJobClick(job.jobId) })
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun JobRow(job: JobCacheEntity, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(job.siteName ?: "Site #${job.siteId}") },
        supportingContent = { Text("${job.clientName ?: ""}  ·  ${job.status}") },
        trailingContent = { job.scheduledAt?.let { Text(it, style = MaterialTheme.typography.bodySmall) } },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}
