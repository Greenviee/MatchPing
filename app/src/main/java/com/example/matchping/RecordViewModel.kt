package com.example.matchping

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RecordViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getDatabase(app).matchResultDao()

    private val _matches = MutableLiveData<List<MatchResult>>()
    val matches: LiveData<List<MatchResult>> = _matches

    private val _searchResults = MutableLiveData<List<MatchResult>>()
    val searchResults: LiveData<List<MatchResult>> = _searchResults

    fun loadRecent() = viewModelScope.launch {
        _matches.postValue(dao.getRecentMatches(20))
    }

    fun searchByName(name: String) = viewModelScope.launch {
        _searchResults.postValue(dao.getMatchesByOpponent(name))
    }

    fun filterByTag(tag: String) = viewModelScope.launch {
        val all = dao.getAllMatches()
        _searchResults.postValue(all.filter { it.tags.contains(tag) })
    }
}
