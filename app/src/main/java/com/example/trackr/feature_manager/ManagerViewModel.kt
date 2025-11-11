package com.example.trackr.feature_manager


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.repository.TicketRepository
import com.example.trackr.domain.model.Ticket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ManagerViewModel @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _teamTickets = MutableStateFlow<List<Ticket>>(emptyList())
    val teamTickets: StateFlow<List<Ticket>> = _teamTickets

    private val _managerName = MutableStateFlow("")
    val managerName: StateFlow<String> = _managerName

    init {
        loadManagerData()
    }

    private fun loadManagerData() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            // Fetch manager's own data to get their name and teamId
            val managerDoc = firestore.collection("users").document(userId).get().await()
            _managerName.value = managerDoc.getString("name") ?: "Manager"
            val teamId = managerDoc.getString("teamId")

            // If a teamId exists, fetch the tickets for that team
            if (teamId != null) {
                _teamTickets.value = ticketRepository.getTicketsForTeam(teamId)
            }
        }
    }
}