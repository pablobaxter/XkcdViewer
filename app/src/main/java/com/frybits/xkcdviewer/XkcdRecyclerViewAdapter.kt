package com.frybits.xkcdviewer

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.frybits.repository.XkcdRepository
import com.frybits.repository.models.XkcdComic
import com.frybits.xkcdviewer.databinding.ViewComicBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class XkcdRecyclerViewAdapter(private val scope: CoroutineScope, private val xkcdRepository: XkcdRepository) : PagingDataAdapter<XkcdComic, XkcdRecyclerViewAdapter.XkcdComicViewHolder>(ComicComparator) {

    private val jobMap = hashMapOf<Int, Job>()

    class XkcdComicViewHolder(val binding: ViewComicBinding) : RecyclerView.ViewHolder(binding.root) {

        fun setXkcdComid(xkcdComic: XkcdComic, comicImg: Bitmap?) {
            binding.titleTextView.text = xkcdComic.title
            binding.altTextView.text = xkcdComic.alt
            binding.dateTextView.text = SimpleDateFormat.getDateInstance().format(xkcdComic.date)
            binding.comicImageView.setImageBitmap(comicImg)
        }
    }

    override fun onBindViewHolder(holder: XkcdComicViewHolder, position: Int) {
        jobMap[position]?.cancel() // Cancel any pending job mapped to the current position
        jobMap[position] = scope.launch { // Launch a new job to populate the view holder and assign the job to the map with the position being the key.
            val item = getItem(position) ?: return@launch
            // The only reason we need the scope is for getting the image. If it's cached in memory, this is done quickly, otherwise the view holder won't be loaded until this completes.
            holder.setXkcdComid(item, xkcdRepository.getComicImage(item))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): XkcdComicViewHolder {
        val binding = ViewComicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return XkcdComicViewHolder(binding)
    }
}

private object ComicComparator : DiffUtil.ItemCallback<XkcdComic>() {
    override fun areItemsTheSame(oldItem: XkcdComic, newItem: XkcdComic): Boolean {
        // Id is unique.
        return oldItem.comicId == newItem.comicId
    }

    override fun areContentsTheSame(oldItem: XkcdComic, newItem: XkcdComic): Boolean {
        return oldItem == newItem
    }
}
