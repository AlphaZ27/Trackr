package com.example.trackr.feature_admin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.feature_admin.domain.TrackrUser
import com.example.trackr.feature_admin.domain.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<TrackrUser>>(emptyList())
    val users: StateFlow<List<TrackrUser>> = _users

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _users.value = usersRepository.getAllUsers()
        }
    }

    fun changeUserRole(uid: String, newRole: String) {
        viewModelScope.launch {
            usersRepository.updateUserRole(uid, newRole)
            // Refresh the list to show the change immediately
            loadUsers()
        }
    }
}