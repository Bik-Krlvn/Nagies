package com.wNagiesEducationalCenterj_9905.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wNagiesEducationalCenterj_9905.R
import com.wNagiesEducationalCenterj_9905.common.ItemCallback
import com.wNagiesEducationalCenterj_9905.data.db.Entities.MessageEntity
import kotlinx.android.synthetic.main.list_messages.view.*

class MessageAdapter : ListAdapter<MessageEntity, MessageVH>(DiffUtils()) {
    private var itemCallback:ItemCallback<Int>? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageVH {
        return MessageVH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_messages, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MessageVH, position: Int) {
        holder.bind(getItem(position),itemCallback)
    }

    fun setItemCallback(callback: ItemCallback<Int>?){
        itemCallback = callback
    }
}

class MessageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(
        entity: MessageEntity?,
        itemCallback: ItemCallback<Int>?
    ) {
        val level = "Class: ${entity?.level}"
        val from = "From: ${entity?.sender}"
        itemView.tv_msg_date.text = level
        itemView.tv_msg_content.text = "${entity?.content}"
        itemView.tv_msg_sender.text = from
        itemView.setOnClickListener { itemCallback?.onClick(entity?.id) }
    }
}

private class DiffUtils : DiffUtil.ItemCallback<MessageEntity>() {
    override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean {
        return oldItem.level == newItem.level &&
                oldItem.content == newItem.content &&
                oldItem.sender == newItem.sender
    }
}