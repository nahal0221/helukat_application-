@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.yourcompany.fieldtech.ui.jobdetail

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dpimport androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.fieldtech.data.remote.dto.JobDetailDto
import com.yourcompany.fieldtech.data.repository.FieldActivityRepository
import com.yourcompany.fieldtech.data.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JobDetailUiState(
    val job: JobDetailDto? = null,
    val loading: Boolean = true,
    val statusNote: String = "",
    val actionInFlight: Boolean = false
)

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val jobRepository: JobRepository,
    private val fieldActivityRepository: FieldActivityRepository
) : ViewModel() {

    private val jobId: Long = savedStateHandle.get<Long>("jobId") ?: 0L

    private val _state = MutableStateFlow(JobDetailUiState())
    val state: StateFlow<JobDetailUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            jobRepository.getJobDetail(jobId).onSuccess { job ->
                _state.value = _state.value.copy(job = job, loading = false)
            }.onFailure {
                _state.value = _state.value.copy(loading = false)
            }
        }
    }

    fun onStatusNoteChange(value: String) { _state.value = _state.value.copy(statusNote = value) }

    /** GPS coordinates should come from FusedLocationProviderClient in production; stubbed here. */
    fun logArrival(latitude: Double, longitude: Double) = logTimeEvent("arrival", latitude, longitude)
    fun logDeparture(latitude: Double, longitude: Double) = logTimeEvent("departure", latitude, longitude)

    private fun logTimeEvent(eventType: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionInFlight = true)
            fieldActivityRepository.logTimeEvent(jobId, eventType, latitude, longitude)
            _state.value = _state.value.copy(actionInFlight = false)
        }
    }

    fun submitStatusNote() {
        val note = _state.value.statusNote
        if (note.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(actionInFlight = true)
            fieldActivityRepository.postStatusNote(jobId, note)
            _state.value = _state.value.copy(actionInFlight = false, statusNote = "")
        }
    }

    fun markInProgress() {
        viewModelScope.launch { jobRepository.updateStatus(jobId, "in_progress") }
    }

    fun markComplete() {
        viewModelScope.launch { jobRepository.updateStatus(jobId, "completed") }
    }
}

@SuppressLint("MissingPermission") // location permission handling omitted from this scaffold
@Composable
fun JobDetailScreen(
    jobId: Long,
    onBack: () -> Unit,
    viewModel: JobDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job #$jobId") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            state.job?.let { job ->
                Text("Status: ${job.status}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.logArrival(25.2048, 55.2708) }, enabled = !state.actionInFlight) {
                        Text("Log Arrival")
                    }
                    Button(onClick = { viewModel.logDeparture(25.2048, 55.2708) }, enabled = !state.actionInFlight) {
                        Text("Log Departure")
                    }
                }
                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = viewModel::markInProgress) { Text("Mark In Progress") }
                    OutlinedButton(onClick = viewModel::markComplete) { Text("Mark Complete") }
                }
                Spacer(Modifier.height(24.dp))

                Text("Checklist", style = MaterialTheme.typography.titleMedium)
                job.checklist.forEach { item ->
                    Text("${if (item.completed) "✓" else "○"} ${item.label}")
                }
                Spacer(Modifier.height(24.dp))

                Text("Status Note", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = state.statusNote,
                    onValueChange = viewModel::onStatusNoteChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Progress update") }
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = viewModel::submitStatusNote,
                    enabled = !state.actionInFlight && state.statusNote.isNotBlank()
                ) {
                    Text("Post Update")
                }
            }
        }
    }
}
