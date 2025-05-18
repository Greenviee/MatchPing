package com.example.matchping

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RecordViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val matchDao = db.matchResultDao()

    private val _matches = MutableLiveData<List<MatchResult>>()
    val matches: LiveData<List<MatchResult>> get() = _matches

    private val _searchResults = MutableLiveData<List<MatchResult>>()
    val searchResults: LiveData<List<MatchResult>> get() = _searchResults

    fun loadRecent() {
        viewModelScope.launch {
            _matches.postValue(matchDao.getRecentMatches())
        }
    }

    fun searchByName(name: String) {
        viewModelScope.launch {
            _searchResults.postValue(matchDao.searchMatchesByName("%$name%"))
        }
    }

    fun filterByTag(tag: String) {
        viewModelScope.launch {
            val all = matchDao.getAllMatches()
            _searchResults.postValue(all.filter { it.detail.contains(tag) })
        }
    }
}
