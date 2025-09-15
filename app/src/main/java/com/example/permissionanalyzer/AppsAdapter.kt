package com.example.permissionanalyzer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppsAdapter(
    private val apps: List<AppScanResult>,
    private val onAppClick: (AppScanResult) -> Unit
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.bind(app, onAppClick)
    }

    override fun getItemCount(): Int = apps.size

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAppIcon: ImageView = itemView.findViewById(R.id.ivAppIcon)
        private val tvAppName: TextView = itemView.findViewById(R.id.tvAppName)
        private val tvPackageName: TextView = itemView.findViewById(R.id.tvPackageName)
        private val tvPermissionCount: TextView = itemView.findViewById(R.id.tvPermissionCount)

        fun bind(app: AppScanResult, onAppClick: (AppScanResult) -> Unit) {
            // Use actual app icon if available, otherwise use default
            if (app.appIcon != null) {
                ivAppIcon.setImageDrawable(app.appIcon)
            } else {
                ivAppIcon.setImageResource(R.mipmap.ic_launcher)
            }

            tvAppName.text = app.appName
            tvPackageName.text = app.packageName
            tvPermissionCount.text = "${app.permissions.size} permissions"

            itemView.setOnClickListener {
                onAppClick(app)
            }
        }
    }
}