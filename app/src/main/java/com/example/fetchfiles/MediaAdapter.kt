package com.example.fetchfiles

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MediaAdapter(private val mediaFiles: List<Uri>, private val context: Context) :
    RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val audioView: ImageView = itemView.findViewById(R.id.audioView)
        val videoView: ImageView = itemView.findViewById(R.id.videoView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mediaFiles.size
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val uri = mediaFiles[position]
        val mimeType = context.contentResolver.getType(uri) ?: ""

        when {
            mimeType.startsWith("image/") -> {
                holder.imageView.visibility = View.VISIBLE
                holder.audioView.visibility = View.GONE
                holder.videoView.visibility = View.GONE

                Glide.with(context)
                    .load(uri)
                    .into(holder.imageView)

            }

            mimeType.startsWith("audio/") -> {
                holder.imageView.visibility = View.GONE
                holder.audioView.visibility = View.VISIBLE
                holder.videoView.visibility = View.GONE
                holder.audioView.setImageResource(R.drawable.baseline_audiotrack_24)

            }

            mimeType.startsWith("video/") -> {
                holder.imageView.visibility = View.GONE
                holder.audioView.visibility = View.GONE
                holder.videoView.visibility = View.VISIBLE
                holder.videoView.setImageResource(R.drawable.baseline_music_video_24)

            }

            else -> {
                holder.imageView.visibility = View.GONE
                holder.audioView.visibility = View.GONE
                holder.videoView.visibility = View.GONE
            }
        }
    }
}