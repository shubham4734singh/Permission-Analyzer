package com.example.permissionanalyzer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class PermissionsAdapter(
    private val permissions: List<PermissionHelper.PermissionData>
) : RecyclerView.Adapter<PermissionsAdapter.PermissionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_permission_detail, parent, false)
        return PermissionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        val permission = permissions[position]
        holder.bind(permission)
    }

    override fun getItemCount(): Int = permissions.size

    class PermissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPermissionName: TextView = itemView.findViewById(R.id.tvPermissionName)
        private val tvPermissionDesc: TextView = itemView.findViewById(R.id.tvPermissionDesc)
        private val tvPermissionIcon: TextView = itemView.findViewById(R.id.tvPermissionIcon)

        fun bind(permission: PermissionHelper.PermissionData) {
            tvPermissionName.text = permission.text
            tvPermissionDesc.text = permission.explanation
            tvPermissionIcon.text = permission.icon
        }
    }
}