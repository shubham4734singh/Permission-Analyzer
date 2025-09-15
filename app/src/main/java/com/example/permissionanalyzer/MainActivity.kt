package com.example.permissionanalyzer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import org.json.JSONObject
import android.os.Build

class MainActivity : AppCompatActivity() {
    // ==========================================
    // Class Variables and Initialization
    // ==========================================
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var scanAnalyzer: ScanAnalyzer

    // UI Components
    private lateinit var btnUpload: Button
    private lateinit var btnSelectApp: Button
    private lateinit var profileCard: CardView


    // ==========================================
    // Activity Result Handlers
    // ==========================================
    private val apkPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                analyzeAPKFile(uri)
            }
        }
    }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openAPKPicker()
        } else {
            Toast.makeText(
                this,
                getString(R.string.storage_permission_denied),
                Toast.LENGTH_LONG
            ).show()
            openAPKPicker()
        }
    }

    // ==========================================
    // Activity Lifecycle Methods
    // ==========================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        initializeComponents()
        setupViews()
    }

    // ==========================================
    // Initialization Methods
    // ==========================================
    private fun initializeComponents() {
        permissionHelper = PermissionHelper(this)
        scanAnalyzer = ScanAnalyzer(permissionHelper)

        // Initialize UI components
        btnUpload = findViewById(R.id.btnUploadAPK)
        btnSelectApp = findViewById(R.id.btnSelectApp)
        profileCard = findViewById(R.id.profileCard)
    }

    private fun setupViews() {
        btnUpload.setOnClickListener { handleAPKUpload() }
        btnSelectApp.setOnClickListener {
            val intent = Intent(this, AppsListActivity::class.java)
            startActivity(intent)
        }
        profileCard.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }



    private fun analyzeSelectedApp(packageName: String) {
        lifecycleScope.launch {
            try {
                btnSelectApp.text = getString(R.string.analyzing_progress)
                btnSelectApp.isEnabled = false

                val result = withContext(Dispatchers.IO) {
                    scanAnalyzer.analyzeSingleApp(packageManager, packageName)
                }

                withContext(Dispatchers.Main) {
                    if (result != null) {
                        Toast.makeText(this@MainActivity, getString(R.string.app_analysis_complete), Toast.LENGTH_SHORT).show()
                        // Open details activity
                        val intent = Intent(this@MainActivity, AppDetailsActivity::class.java).apply {
                            putExtra("packageName", packageName)
                            putExtra("appName", result.appResults.firstOrNull()?.appName ?: "Unknown")
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MainActivity, getString(R.string.app_analysis_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, getString(R.string.app_analysis_error, e.message), Toast.LENGTH_LONG).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    btnSelectApp.text = getString(R.string.select_installed_app)
                    btnSelectApp.isEnabled = true
                }
            }
        }
    }

    private fun handleAPKUpload() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                openAPKPicker()
            }
            else -> {
                storagePermissionLauncher.launch(permission)
            }
        }
    }

    private fun openAPKPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/vnd.android.package-archive"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        try {
            apkPickerLauncher.launch(Intent.createChooser(intent, getString(R.string.select_apk_file)))
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.no_file_manager), Toast.LENGTH_SHORT).show()
        }
    }

    private fun analyzeAPKFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                btnUpload.text = getString(R.string.analyzing_progress)
                btnUpload.isEnabled = false

                val result = withContext(Dispatchers.IO) {
                    scanAnalyzer.analyzeAPKFromUri(this@MainActivity, uri)
                }

                withContext(Dispatchers.Main) {
                    if (result != null) {
                        Toast.makeText(this@MainActivity, getString(R.string.apk_analysis_complete), Toast.LENGTH_SHORT).show()
                        // Open details activity for APK
                        val permissionStrings = result.appResults.firstOrNull()?.permissions?.map { it.permission } ?: emptyList()
                        val intent = Intent(this@MainActivity, AppDetailsActivity::class.java).apply {
                            putExtra("packageName", result.appResults.firstOrNull()?.packageName ?: "unknown")
                            putExtra("appName", result.appResults.firstOrNull()?.appName ?: "APK File")
                            putStringArrayListExtra("permissions", ArrayList(permissionStrings))
                            putExtra("isApk", true)
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MainActivity, getString(R.string.apk_analysis_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, getString(R.string.apk_analysis_error, e.message), Toast.LENGTH_LONG).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    btnUpload.text = getString(R.string.upload_apk_button)
                    btnUpload.isEnabled = true
                }
            }
        }
    }

}

// ==========================================
// Supporting Classes
// ==========================================
// (Include all the supporting classes from the original file here)
// Enhanced ScanAnalyzer with APK support and comparison features
class ScanAnalyzer(private val permissionHelper: PermissionHelper) {

    suspend fun analyzeAPKFromUri(context: Context, uri: Uri): ScanResult? {
        return try {
            val tempFile = File.createTempFile("temp_apk", ".apk", context.cacheDir)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            val pm = context.packageManager
            val packageInfo = pm.getPackageArchiveInfo(tempFile.absolutePath, PackageManager.GET_PERMISSIONS)

            packageInfo?.let { pkgInfo ->
                val appInfo = ApplicationInfo().apply {
                    packageName = pkgInfo.packageName ?: "unknown"
                    // Set a temporary label for APK
                }

                val appResult = analyzePackageInfo(pkgInfo, "APK File", pm)
                ScanResult(
                    appResults = listOf(appResult),
                    totalApps = 1,
                    totalPermissions = appResult.permissions.size,
                    riskScore = 0,
                    highRiskApps = 0,
                    scanType = "APK"
                )
            } ?: run {
                tempFile.delete()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun analyzePackageInfo(pkgInfo: PackageInfo, appName: String, pm: PackageManager): AppScanResult {
        val permissions = pkgInfo.requestedPermissions?.map { perm ->
            permissionHelper.getPermissionInfo(perm)
        } ?: emptyList()

        return AppScanResult(
            appName = appName,
            packageName = pkgInfo.packageName ?: "unknown",
            permissions = permissions,
            riskLevel = "unknown",
            appRiskScore = 0
        )
    }

    fun analyzeSingleApp(pm: PackageManager, packageName: String): ScanResult? {
        return try {
            val appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val appResult = analyzePackageInfo(pkgInfo, appInfo.loadLabel(pm).toString(), pm)

            ScanResult(
                appResults = listOf(appResult),
                totalApps = 1,
                totalPermissions = appResult.permissions.size,
                riskScore = 0,
                highRiskApps = 0,
                scanType = "Single App"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}


// Enhanced PermissionHelper with more comprehensive permission database
class PermissionHelper(private val context: Context) {
    private val permissionMap = mutableMapOf<String, PermissionData>()
    private var isInitialized = false

    init {
        loadPermissionMap()
    }

    private fun loadPermissionMap() {
        try {
            val resourceId = context.resources.getIdentifier("permission_mapping", "raw", context.packageName)

            if (resourceId != 0) {
                context.resources.openRawResource(resourceId).use { stream ->
                    val jsonString = stream.bufferedReader().use { it.readText() }
                    parsePermissionJson(jsonString)
                }
            } else {
                loadComprehensivePermissions()
            }

            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
            loadComprehensivePermissions()
            isInitialized = true
        }
    }

    private fun parsePermissionJson(jsonString: String) {
        val json = JSONObject(jsonString)
        json.keys().forEach { key ->
            try {
                val data = json.getJSONObject(key)
                permissionMap[key] = PermissionData(
                    text = data.getString("text"),
                    risk = data.getString("risk"),
                    icon = data.getString("icon"),
                    explanation = data.optString("explanation", ""),
                    category = data.optString("category", "other"),
                    permission = key
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadComprehensivePermissions() {
        val permissions = mapOf(
            // High Risk - Privacy Critical
            "android.permission.CAMERA" to PermissionData(
                "Camera Access", "high", "📷", "Can take photos, record videos, and access camera", "privacy", "android.permission.CAMERA"
            ),
            "android.permission.RECORD_AUDIO" to PermissionData(
                "Microphone Access", "high", "🎤", "Can record audio and listen to surroundings", "privacy", "android.permission.RECORD_AUDIO"
            ),
            "android.permission.ACCESS_FINE_LOCATION" to PermissionData(
                "Precise Location", "high", "📍", "Can access exact GPS location", "location", "android.permission.ACCESS_FINE_LOCATION"
            ),
            "android.permission.READ_SMS" to PermissionData(
                "Read SMS", "high", "💬", "Can read all text messages", "communication", "android.permission.READ_SMS"
            ),
            "android.permission.READ_CALL_LOG" to PermissionData(
                "Call History", "high", "📞", "Can read call history and logs", "communication", "android.permission.READ_CALL_LOG"
            ),
            "android.permission.READ_CONTACTS" to PermissionData(
                "Read Contacts", "high", "👥", "Can access all contact information", "personal", "android.permission.READ_CONTACTS"
            ),
            "android.permission.GET_ACCOUNTS" to PermissionData(
                "Account Access", "high", "👤", "Can access account information", "personal", "android.permission.GET_ACCOUNTS"
            ),
            "android.permission.READ_CALENDAR" to PermissionData(
                "Calendar Access", "high", "📅", "Can read calendar events and details", "personal", "android.permission.READ_CALENDAR"
            ),

            // Medium Risk - Sensitive Operations
            "android.permission.ACCESS_COARSE_LOCATION" to PermissionData(
                "Approximate Location", "medium", "📍", "Can access general location area", "location", "android.permission.ACCESS_COARSE_LOCATION"
            ),
            "android.permission.SEND_SMS" to PermissionData(
                "Send SMS", "medium", "📤", "Can send text messages", "communication", "android.permission.SEND_SMS"
            ),
            "android.permission.CALL_PHONE" to PermissionData(
                "Make Calls", "medium", "📞", "Can make phone calls", "communication", "android.permission.CALL_PHONE"
            ),
            "android.permission.WRITE_CONTACTS" to PermissionData(
                "Modify Contacts", "medium", "✏️", "Can add or modify contacts", "personal", "android.permission.WRITE_CONTACTS"
            ),
            "android.permission.WRITE_CALENDAR" to PermissionData(
                "Modify Calendar", "medium", "📝", "Can add or modify calendar events", "personal", "android.permission.WRITE_CALENDAR"
            ),
            "android.permission.READ_PHONE_STATE" to PermissionData(
                "Phone Information", "medium", "📱", "Can read phone state and identity", "device", "android.permission.READ_PHONE_STATE"
            ),
            "android.permission.WRITE_EXTERNAL_STORAGE" to PermissionData(
                "Storage Write", "medium", "💾", "Can write files to storage", "storage", "android.permission.WRITE_EXTERNAL_STORAGE"
            ),
            "android.permission.BODY_SENSORS" to PermissionData(
                "Body Sensors", "medium", "❤️", "Can access heart rate and other sensors", "sensors", "android.permission.BODY_SENSORS"
            ),

            // Low Risk - Basic Operations
            "android.permission.INTERNET" to PermissionData(
                "Internet Access", "low", "🌐", "Can connect to internet", "network", "android.permission.INTERNET"
            ),
            "android.permission.ACCESS_NETWORK_STATE" to PermissionData(
                "Network State", "low", "📶", "Can view network connection info", "network", "android.permission.ACCESS_NETWORK_STATE"
            ),
            "android.permission.READ_EXTERNAL_STORAGE" to PermissionData(
                "Storage Read", "low", "📂", "Can read files from storage", "storage", "android.permission.READ_EXTERNAL_STORAGE"
            ),
            "android.permission.VIBRATE" to PermissionData(
                "Vibration", "low", "📳", "Can control device vibration", "device", "android.permission.VIBRATE"
            ),
            "android.permission.WAKE_LOCK" to PermissionData(
                "Keep Awake", "low", "⏰", "Can prevent device from sleeping", "device", "android.permission.WAKE_LOCK"
            ),
            "android.permission.ACCESS_WIFI_STATE" to PermissionData(
                "WiFi Information", "low", "📡", "Can view WiFi connection state", "network", "android.permission.ACCESS_WIFI_STATE"
            ),
            "android.permission.CHANGE_WIFI_STATE" to PermissionData(
                "WiFi Control", "low", "📡", "Can enable/disable WiFi", "network", "android.permission.CHANGE_WIFI_STATE"
            ),
            "android.permission.BLUETOOTH" to PermissionData(
                "Bluetooth", "low", "🔵", "Can connect to Bluetooth devices", "device", "android.permission.BLUETOOTH"
            ),
            "android.permission.BLUETOOTH_ADMIN" to PermissionData(
                "Bluetooth Admin", "medium", "🔵", "Can discover and pair Bluetooth devices", "device", "android.permission.BLUETOOTH_ADMIN"
            ),
            "android.permission.NFC" to PermissionData(
                "NFC Access", "low", "📲", "Can use Near Field Communication", "device", "android.permission.NFC"
            ),

            // System Level - Minimal Risk
            "android.permission.RECEIVE_BOOT_COMPLETED" to PermissionData(
                "Auto Start", "low", "🚀", "Can start when device boots", "system", "android.permission.RECEIVE_BOOT_COMPLETED"
            ),
            "android.permission.FOREGROUND_SERVICE" to PermissionData(
                "Background Service", "low", "⚙️", "Can run services in background", "system", "android.permission.FOREGROUND_SERVICE"
            ),
            "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" to PermissionData(
                "Battery Optimization", "low", "🔋", "Can request battery optimization bypass", "system", "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"
            )
        )

        permissionMap.putAll(permissions)
    }

    fun getPermissionInfo(permission: String): PermissionData {
        return permissionMap[permission] ?: createUnknownPermission(permission)
    }

    private fun createUnknownPermission(permission: String): PermissionData {
        val simpleName = permission.substringAfterLast('.')
            .replace('_', ' ')
            .split(' ')
            .joinToString(" ") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }

        return PermissionData(
            text = simpleName,
            risk = "unknown",
            icon = "❓",
            explanation = "Unknown permission",
            category = "unknown",
            permission = permission
        )
    }

    fun getPermissionsByCategory(): Map<String, List<PermissionData>> {
        return permissionMap.values.groupBy { it.category }
    }

    fun getPermissionStats(): PermissionStats {
        val all = permissionMap.values
        return PermissionStats(
            total = all.size,
            highRisk = all.count { it.risk == "high" },
            mediumRisk = all.count { it.risk == "medium" },
            lowRisk = all.count { it.risk == "low" }
        )
    }

    data class PermissionData(
        val text: String,
        val risk: String,
        val icon: String,
        val explanation: String = "",
        val category: String = "other",
        val permission: String = ""
    )

    data class PermissionStats(
        val total: Int,
        val highRisk: Int,
        val mediumRisk: Int,
        val lowRisk: Int
    )
}

// Enhanced data classes
data class ScanResult(
    val appResults: List<AppScanResult>,
    val totalApps: Int,
    val totalPermissions: Int,
    val riskScore: Int,
    val highRiskApps: Int,
    val scanType: String = "App Scan"
)

data class AppScanResult(
    val appName: String,
    val packageName: String,
    val permissions: List<PermissionHelper.PermissionData>,
    val riskLevel: String,
    val appRiskScore: Int,
    val appIcon: android.graphics.drawable.Drawable? = null
)


object AppUtils {
    fun isSystemApp(appInfo: ApplicationInfo): Boolean {
        return appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    fun getAppInstallTime(packageManager: PackageManager, packageName: String): Long? {
        return try {
            packageManager.getPackageInfo(packageName, 0).firstInstallTime
        } catch (e: Exception) {
            null
        }
    }

    fun formatPermissionCount(count: Int): String {
        return when {
            count == 0 -> "No permissions"
            count == 1 -> "1 permission"
            count < 10 -> "$count permissions"
            else -> "10+ permissions"
        }
    }

    fun getAppSize(packageManager: PackageManager, packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val sourceDir = File(appInfo.sourceDir)
            val sizeInMB = sourceDir.length() / (1024 * 1024)
            "${sizeInMB}MB"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun isAppRecentlyUpdated(packageManager: PackageManager, packageName: String): Boolean {
        return try {
            val pkgInfo = packageManager.getPackageInfo(packageName, 0)
            val daysSinceUpdate = (System.currentTimeMillis() - pkgInfo.lastUpdateTime) / (1000 * 60 * 60 * 24)
            daysSinceUpdate < 7 // Updated within last 7 days
        } catch (e: Exception) {
            false
        }
    }
}