package com.itg.template.ui.component.language.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.itg.template.R
import com.itg.template.databinding.ItemLanguageBinding
import com.itg.template.ui.component.language.data.LanguageModel

class LanguageAdapter(
    private val onItemLanguageClick: (LanguageModel) -> Unit
) : ListAdapter<LanguageModel, LanguageAdapter.LanguageViewHolder>(
    LanguageDiffCallback()
) {

    inner class LanguageViewHolder(
        private val binding: ItemLanguageBinding
    ) : ViewHolder(binding.root) {

        fun bind(item: LanguageModel) {
            binding.tvLanguageName.text = item.name
            binding.tvLanguageNative.text = item.nativeName
            binding.ivFlag.setImageResource(item.imageResId)
            toggleSelected(item.selected)

            binding.root.setOnClickListener {
                onItemLanguageClick(item)
            }
        }

        fun toggleSelected(isSelected: Boolean) {
            if (binding.rbLanguageSelected.isChecked != isSelected) {
                binding.rbLanguageSelected.isChecked = isSelected
            }
            binding.item.strokeColor = ContextCompat.getColor(
                binding.root.context,
                if (isSelected) R.color.color_brand else R.color.color_stroke_soft
            )
            val density = binding.root.resources.displayMetrics.density
            binding.item.cardElevation = if (isSelected) 8f * density else 0f
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val shadow = ContextCompat.getColor(binding.root.context, R.color.color_brand)
                binding.item.outlineSpotShadowColor = shadow
                binding.item.outlineAmbientShadowColor = shadow
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: LanguageViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            payloads.forEach {
                if (it !is String) return@forEach
                when (it) {
                    "payload_item_selected" -> {
                        holder.toggleSelected(getItem(position).selected)
                    }
                }
            }
        }
    }
}

class LanguageDiffCallback : DiffUtil.ItemCallback<LanguageModel>() {

    override fun areItemsTheSame(oldItem: LanguageModel, newItem: LanguageModel): Boolean {
        return oldItem.iso == newItem.iso
    }

    override fun areContentsTheSame(oldItem: LanguageModel, newItem: LanguageModel): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: LanguageModel, newItem: LanguageModel): Any? {
        return if (oldItem.selected != newItem.selected) {
            "payload_item_selected"
        } else {
            null
        }
    }
}