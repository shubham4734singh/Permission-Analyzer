package com.example.permissionanalyzer

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

class AppDetailsActivity : AppCompatActivity() {

    private lateinit var tvAppName: TextView
    private lateinit var tvPackageName: TextView
    private lateinit var tvPermissionCount: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: Button
    private lateinit var btnEditPermissions: Button
    private lateinit var permissionHelper: PermissionHelper

    private var currentPackageName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_app_details)

        val packageName = intent.getStringExtra("packageName") ?: ""
        val appName = intent.getStringExtra("appName") ?: ""
        val isApk = intent.getBooleanExtra("isApk", false)

        if (packageName.isEmpty()) {
            finish()
            return
        }

        currentPackageName = packageName

        initializeComponents()
        setupViews(isApk)
        loadAppPermissions(packageName, appName, isApk)
    }

    private fun initializeComponents() {
        permissionHelper = PermissionHelper(this)

        tvAppName = findViewById(R.id.tvAppName)
        tvPackageName = findViewById(R.id.tvPackageName)
        tvPermissionCount = findViewById(R.id.tvPermissionCount)
        recyclerView = findViewById(R.id.recyclerViewPermissions)
        btnBack = findViewById(R.id.btnBack)
        btnEditPermissions = findViewById(R.id.btnEditPermissions)
    }

    private fun setupViews(isApk: Boolean) {
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnBack.setOnClickListener {
            finish()
        }

        if (isApk) {
            btnEditPermissions.isEnabled = false
            btnEditPermissions.alpha = 0.5f
            btnEditPermissions.setOnClickListener {
                android.widget.Toast.makeText(this, "Cannot edit permissions for APK files", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            btnEditPermissions.setOnClickListener {
                openAppPermissionSettings(currentPackageName)
            }
        }
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

    private fun loadAppPermissions(packageName: String, appName: String, isApk: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val permissions = withContext(Dispatchers.IO) {
                    if (isApk) {
                        getApkPermissions()
                    } else {
                        getAppPermissions(packageName)
                    }
                }

                tvAppName.text = appName
                tvPackageName.text = packageName
                tvPermissionCount.text = "${permissions.size} permissions"

                showPermissions(permissions)
            } catch (e: Exception) {
                tvAppName.text = "Error loading app details"
                tvPermissionCount.text = "0 permissions"
            }
        }
    }

    private fun getAppPermissions(packageName: String): List<PermissionHelper.PermissionData> {
        val pm = packageManager
        val pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)

        return pkgInfo.requestedPermissions?.map { perm ->
            permissionHelper.getPermissionInfo(perm)
        } ?: emptyList()
    }

    private fun getApkPermissions(): List<PermissionHelper.PermissionData> {
        val permissionStrings = intent.getStringArrayListExtra("permissions") ?: emptyList()
        return permissionStrings.map { perm ->
            permissionHelper.getPermissionInfo(perm)
        }
    }

    private fun showPermissions(permissions: List<PermissionHelper.PermissionData>) {
        val adapter = PermissionsAdapter(permissions)
        recyclerView.adapter = adapter
    }
}