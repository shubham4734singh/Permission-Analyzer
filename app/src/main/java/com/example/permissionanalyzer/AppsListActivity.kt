package com.example.permissionanalyzer

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppsListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var btnBack: Button
    private lateinit var btnUserApps: Button
    private lateinit var btnSystemApps: Button
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var scanAnalyzer: ScanAnalyzer

    private var currentAppType = "user" // "user" or "system"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_apps_list)

        initializeComponents()
        setupViews()
        updateTabButtons()
        loadAppsWithDangerousPermissions()
    }

    private fun initializeComponents() {
        permissionHelper = PermissionHelper(this)
        scanAnalyzer = ScanAnalyzer(permissionHelper)

        recyclerView = findViewById(R.id.recyclerViewApps)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        btnBack = findViewById(R.id.btnBack)
        btnUserApps = findViewById(R.id.btnUserApps)
        btnSystemApps = findViewById(R.id.btnSystemApps)
    }

    private fun setupViews() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnBack.setOnClickListener {
            finish()
        }

        btnUserApps.setOnClickListener {
            currentAppType = "user"
            updateTabButtons()
            loadAppsWithDangerousPermissions()
        }

        btnSystemApps.setOnClickListener {
            currentAppType = "system"
            updateTabButtons()
            loadAppsWithDangerousPermissions()
        }
    }

    private fun updateTabButtons() {
        if (currentAppType == "user") {
            btnUserApps.backgroundTintList = resources.getColorStateList(R.color.button_scan)
            btnUserApps.setTextColor(resources.getColor(R.color.white))
            btnSystemApps.backgroundTintList = resources.getColorStateList(R.color.surface_primary)
            btnSystemApps.setTextColor(resources.getColor(android.R.color.primary_text_light))
        } else {
            btnSystemApps.backgroundTintList = resources.getColorStateList(R.color.button_scan)
            btnSystemApps.setTextColor(resources.getColor(R.color.white))
            btnUserApps.backgroundTintList = resources.getColorStateList(R.color.surface_primary)
            btnUserApps.setTextColor(resources.getColor(android.R.color.primary_text_light))
        }
    }

    private fun loadAppsWithDangerousPermissions() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val appsWithDangerousPermissions = withContext(Dispatchers.IO) {
                    getAppsWithDangerousPermissions()
                }

                if (appsWithDangerousPermissions.isEmpty()) {
                    showEmptyState()
                } else {
                    showAppsList(appsWithDangerousPermissions)
                }
            } catch (e: Exception) {
                showEmptyState()
            }
        }
    }

    private fun getAppsWithDangerousPermissions(): List<AppScanResult> {
        val pm = packageManager
        val allApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        // Filter based on app type
        val apps = if (currentAppType == "user") {
            allApps.filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 } // User apps
        } else {
            allApps.filter { it.flags and ApplicationInfo.FLAG_SYSTEM != 0 } // System apps
        }

        // Limit to 50 apps for performance
        val limitedApps = apps.take(50)

        val results = mutableListOf<AppScanResult>()

        limitedApps.forEach { app ->
            try {
                val pkgInfo = pm.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS)
                val permissions = pkgInfo.requestedPermissions?.map { perm ->
                    permissionHelper.getPermissionInfo(perm)
                } ?: emptyList()

                // Include all apps, not just those with dangerous permissions
                val appResult = AppScanResult(
                    appName = app.loadLabel(pm).toString(),
                    packageName = app.packageName,
                    permissions = permissions,
                    riskLevel = if (permissions.any { it.risk == "high" }) "high" else "low",
                    appRiskScore = 0,
                    appIcon = app.loadIcon(pm)
                )
                results.add(appResult)
            } catch (e: Exception) {
                // Skip apps that can't be analyzed
            }
        }

        return results.sortedBy { it.appName }
    }

    private fun showAppsList(apps: List<AppScanResult>) {
        tvEmptyState.visibility = android.view.View.GONE
        recyclerView.visibility = android.view.View.VISIBLE

        val adapter = AppsAdapter(apps) { appResult ->
            // Open app details activity to show permissions first
            val intent = Intent(this, AppDetailsActivity::class.java).apply {
                putExtra("packageName", appResult.packageName)
                putExtra("appName", appResult.appName)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun showEmptyState() {
        recyclerView.visibility = android.view.View.GONE
        tvEmptyState.visibility = android.view.View.VISIBLE
        val appTypeText = if (currentAppType == "user") getString(R.string.downloaded_apps) else getString(R.string.system_apps)
        tvEmptyState.text = getString(R.string.no_apps_found, appTypeText.lowercase())
    }

    private fun openAppPermissionSettings(packageName: String) {
        try {
            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:$packageName")
                addCategory(android.content.Intent.CATEGORY_DEFAULT)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback: try to open app details
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS)
                startActivity(intent)
            } catch (e2: Exception) {
                android.widget.Toast.makeText(this, "Unable to open permission settings", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}