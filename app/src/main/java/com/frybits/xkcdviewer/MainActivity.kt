package com.frybits.xkcdviewer

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.cachedIn
import com.frybits.repository.XkcdRepository
import com.frybits.xkcdviewer.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Our only scope! And the only one we need to make the calls necessary.
    // Since we are modifying UI, default dispatcher for this scope is "Main"
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Inject
    lateinit var xkcdRepository: XkcdRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        // Passing the scope of this activity to the adapter, since the adapter has to make a call to get images from a suspend function.
        val adapter = XkcdRecyclerViewAdapter(mainScope, xkcdRepository)
        binding.comicViewPager.adapter = adapter

        // Launching the flow to get all comic data!
        mainScope.launch {
            xkcdRepository.pagedFlow()
                .cachedIn(this)
                .collectLatest {
                    adapter.submitData(it) // Every emit will call "adapter.submitData()". This "collect" call is done on the main thread.
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel() // Cancel the scope, since the activity is being destroyed. Cancels all network/disk calls being made by this scope. Easy and simple cleanup.
    }
}
