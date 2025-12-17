package com.example.test_lab_week_12

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.test_lab_week_12.model.Movie
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.movie_list)
        
        // Adapter tidak diubah (tetap menggunakan Interface Object)
        val movieAdapter = MovieAdapter(object : MovieAdapter.MovieClickListener {
            override fun onMovieClick(movie: Movie) {
                val intent = Intent(this@MainActivity, DetailsActivity::class.java)
                intent.putExtra(DetailsActivity.EXTRA_TITLE, movie.title)
                intent.putExtra(DetailsActivity.EXTRA_RELEASE, movie.releaseDate)
                intent.putExtra(DetailsActivity.EXTRA_OVERVIEW, movie.overview)
                intent.putExtra(DetailsActivity.EXTRA_POSTER, movie.posterPath)
                startActivity(intent)
            }
        })
        recyclerView.adapter = movieAdapter

        val movieRepository = (application as MovieApplication).movieRepository
        val movieViewModel = ViewModelProvider(
            this, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return MovieViewModel(movieRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            })[MovieViewModel::class.java]

        // --- BAGIAN INI YANG BERUBAH UNTUK FLOWS ---
        lifecycleScope.launch {
            // repeatOnLifecycle memastikan flow hanya dikumpulkan saat Activity dalam keadaan STARTED
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                
                // Launch coroutine terpisah untuk mengumpulkan movies
                launch {
                    movieViewModel.popularMovies.collect { popularMovies ->
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
                        movieAdapter.addMovies(
                            popularMovies
                                .filter { it.releaseDate?.startsWith(currentYear) == true }
                                .sortedByDescending { it.popularity }
                        )
                    }
                }

                // Launch coroutine terpisah untuk mengumpulkan error
                launch {
                    movieViewModel.error.collect { error ->
                        if (error.isNotEmpty()) {
                            Snackbar.make(recyclerView, error, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}