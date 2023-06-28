package de.rki.covpass.commonapp.information

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.ibm.health.common.android.utils.BaseRecyclerViewAdapter
import com.ibm.health.common.android.utils.BindingViewHolder
import com.ibm.health.common.android.utils.getString
import de.rki.covpass.commonapp.databinding.SettingItemBinding
import de.rki.covpass.sdk.utils.formatDateTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

public data class SettingItem(
    @StringRes val title: Int,
    val date: Instant?,
    @StringRes val staticDate: Int? = null,
)

@SuppressLint("NotifyDataSetChanged")
public class SettingsAdapter(
    parent: Fragment,
) : BaseRecyclerViewAdapter<SettingsAdapter.SettingsViewHolder>(parent) {

    private lateinit var items: List<SettingItem>

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SettingsViewHolder = SettingsViewHolder(parent)

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    public fun updateList(items: List<SettingItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    public inner class SettingsViewHolder(parent: ViewGroup) :
        BindingViewHolder<SettingItemBinding>(parent, SettingItemBinding::inflate) {
        public fun bind(item: SettingItem) {
            binding.title.text = getString(item.title)
            binding.date.text = item.date?.let {
                LocalDateTime.ofInstant(item.date, ZoneId.systemDefault()).formatDateTime()
            } ?: "N/A"
            if (item.staticDate != null) {
                binding.date.text = getString(item.staticDate)
            }
        }
    }
}
