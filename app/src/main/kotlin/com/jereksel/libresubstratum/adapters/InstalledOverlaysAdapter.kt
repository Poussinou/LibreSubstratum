package com.jereksel.libresubstratum.adapters

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotterknife.bindView
import com.jereksel.libresubstratum.R
import com.jereksel.libresubstratum.activities.installed.InstalledContract.Presenter
import com.jereksel.libresubstratum.adapters.InstalledOverlaysAdapter.ViewHolder
import com.jereksel.libresubstratum.data.InstalledOverlay
import com.jereksel.libresubstratum.data.KeyPair
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.security.Key
import java.util.*

class InstalledOverlaysAdapter(
        val apps: List<InstalledOverlay>,
        val presenter: Presenter
): RecyclerView.Adapter<ViewHolder>() {

    override fun getItemCount() = apps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (destroyed) return

        val overlay = apps[position]

        val info = presenter.getOverlayInfo(overlay.overlayId)

        holder.targetIcon.setImageDrawable(overlay.targetDrawable)
        holder.themeIcon.setImageDrawable(overlay.sourceThemeDrawable)
        holder.targetName.text = "${overlay.targetName} - ${overlay.sourceThemeName}"
        val color = if(info?.enabled == true) Color.GREEN else Color.RED
        holder.targetName.setTextColor(color)

        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = presenter.getState(position)
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            presenter.setState(position, isChecked)
        }

        holder.view.setOnClickListener {
            holder.checkbox.toggle()
        }

        holder.view.setOnLongClickListener {
            if (info != null) {
                presenter.toggleOverlay(overlay.overlayId, !info.enabled)
            }
            notifyItemChanged(position)
            true
        }

        listOf(holder.targetIcon, holder.themeIcon).forEach { it.setOnLongClickListener {
            if(!presenter.openActivity(overlay.targetId)) {
                Toast.makeText(it.context, "App cannot be opened", Toast.LENGTH_SHORT).show()
            }
            true
        }}

        listOf(
                Triple(overlay.type1a, holder.type1a, R.string.theme_type1a_list),
                Triple(overlay.type1b, holder.type1b, R.string.theme_type1b_list),
                Triple(overlay.type1c, holder.type1c, R.string.theme_type1c_list),
                Triple(overlay.type2, holder.type2, R.string.theme_type2_list),
                Triple(overlay.type3, holder.type3, R.string.theme_type3_list)
        ).forEach { (name, view, stringId) ->
            if (!name.isNullOrEmpty()) {
                val text = view.context.getString(stringId)
                view.text = Html.fromHtml("<b>$text:</b> $name")
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_installed, parent, false)
        return ViewHolder(v)
    }

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        val card: RelativeLayout by bindView(R.id.card)
        val targetIcon: ImageView by bindView(R.id.target_icon)
        val themeIcon: ImageView by bindView(R.id.theme_icon)
        val targetName: TextView by bindView(R.id.target_name)
        val checkbox: CheckBox by bindView(R.id.checkbox)
        val type1a: TextView by bindView(R.id.theme_type1a)
        val type1b: TextView by bindView(R.id.theme_type1b)
        val type1c: TextView by bindView(R.id.theme_type1c)
        val type2: TextView by bindView(R.id.theme_type2)
        val type3: TextView by bindView(R.id.theme_type3)
    }

    var destroyed = false

    fun destroy() {
        destroyed = true
    }
}