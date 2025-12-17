package com.example.test_lab_week_12

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test_lab_week_12.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MovieViewModel(private val movieRepository: MovieRepository) : ViewModel() {

    // Ganti LiveData dengan StateFlow
    private val _popularMovies = MutableStateFlow<List<Movie>>(emptyList())
    val popularMovies: StateFlow<List<Movie>> = _popularMovies

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    // API Key (biasanya ditaruh di tempat aman/local.properties, tapi untuk lab hardcode dulu gapapa)
    private val apiKey = "64139c4f46e9d0a9d0e90e3c8261c3d2" 

    init {
        getPopularMovies()
    }

    private fun getPopularMovies() {
        viewModelScope.launch {
            movieRepository.getPopularMovies(apiKey)
                .catch { e ->
                    // Tangkap error flow di sini
                    _error.value = e.message ?: "Unknown Error"
                }
                .collect { movies ->
                    // Ambil data dari flow dan update state
                    _popularMovies.value = movies
                }
        }
    }
}