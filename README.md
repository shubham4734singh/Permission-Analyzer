# Permission Analyzer 📱🔐

A comprehensive Android application for analyzing app permissions, helping users understand and manage their privacy by examining what permissions installed apps and APK files request.

## 🚀 Features

### Core Functionality
- **📱 Scan Installed Apps**: Analyze permissions of all installed applications (both user and system apps)
- **📦 APK File Analysis**: Upload and analyze APK files directly from your device
- **🔍 Detailed Permission Analysis**: View comprehensive permission details with risk assessment
- **⚠️ Risk Assessment**: Categorize permissions by risk level (High, Medium, Low)
- **🌙 Dark Mode Support**: Toggle between light and dark themes
- **👤 Developer Profile**: Access developer information and contact details

### Permission Categories
- **Privacy**: Camera, Microphone, Location, SMS, Contacts, etc.
- **Communication**: Phone calls, SMS, Call logs
- **Storage**: Read/Write external storage
- **Device**: Vibration, NFC, Bluetooth, WiFi
- **System**: Background services, battery optimization
- **Network**: Internet access, network state

## 🛠️ Technical Details

### Platform & Requirements
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 36 (Android 16)
- **Compile SDK**: API 36
- **Language**: Kotlin
- **Architecture**: MVVM with Repository pattern

### Dependencies
- **AndroidX Core KTX**: Core Kotlin extensions
- **AndroidX AppCompat**: Backward compatibility
- **Material Components**: Modern UI components
- **AndroidX Activity**: Activity result APIs
- **AndroidX ConstraintLayout**: Flexible layouts
- **AndroidX Preference KTX**: Settings management

## 📱 Screenshots & UI

### Main Screen
- Clean, modern interface with gradient backgrounds
- Two primary actions: Scan installed apps and Upload APK
- Profile access in top-right corner
- Responsive design with proper spacing

### Apps List
- Tabbed interface for User Apps vs System Apps
- App icons, names, and permission counts
- Risk level indicators
- Alphabetical sorting

### App Details
- Comprehensive permission breakdown
- Risk categorization with visual indicators
- Direct access to system permission settings
- APK analysis support

### Profile Screen
- Developer information and contact details
- Social media links (GitHub, LinkedIn, Portfolio)
- Dark mode toggle
- Clean, card-based layout

## 🔧 Installation & Setup

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or higher
- Android SDK with API 36

### Build Instructions
1. Clone the repository:
```bash
git clone https://github.com/yourusername/permission-analyzer.git
cd permission-analyzer
```

2. Open in Android Studio:
```bash
# Open Android Studio and select "Open an existing Android Studio project"
# Navigate to the cloned directory and select it
```

3. Build the project:
```bash
# In Android Studio: Build > Make Project
# Or use Gradle wrapper:
./gradlew assembleDebug
```

4. Run on device/emulator:
```bash
# Connect Android device or start emulator
# In Android Studio: Run > Run 'app'
```

## 📋 Usage Guide

### Scanning Installed Apps
1. Launch the app
2. Tap "📱 Select Installed App"
3. Choose between "Downloaded Apps" (user apps) or "System Apps"
4. Browse the list and tap any app to view its permissions
5. Review permission details and risk levels

### Analyzing APK Files
1. From the main screen, tap "📁 Upload APK File"
2. Grant storage permissions if prompted
3. Select an APK file from your device storage
4. View the analysis results and permission breakdown

### Managing Permissions
1. From any app's detail view, tap "Edit Permissions"
2. This opens Android's system settings for that app
3. Modify permissions as needed

### Dark Mode
1. Tap the profile icon in the top-right
2. Use the "Dark Mode" toggle
3. Changes apply immediately across the app

## 🏗️ Project Structure

```
app/src/main/
├── java/com/example/permissionanalyzer/
│   ├── MainActivity.kt              # Main screen with app/APK selection
│   ├── AppsListActivity.kt          # Lists installed apps with permissions
│   ├── AppDetailsActivity.kt        # Shows detailed permission analysis
│   ├── ProfileActivity.kt           # Developer profile and settings
│   ├── AppsAdapter.kt               # RecyclerView adapter for apps list
│   ├── PermissionsAdapter.kt        # RecyclerView adapter for permissions
│   ├── PermissionHelper.kt          # Permission data and risk assessment
│   ├── ScanAnalyzer.kt              # Core analysis logic
│   └── AppUtils.kt                  # Utility functions
├── res/
│   ├── layout/                      # XML layout files
│   ├── values/                      # Strings, colors, themes
│   ├── drawable/                    # Icons and graphics
│   └── raw/                         # Permission mapping data
└── AndroidManifest.xml              # App configuration and permissions
```

## 🔐 Permissions Explained

### App Permissions (Required for Analysis)
- `INTERNET`: Network access for potential updates
- `ACCESS_NETWORK_STATE`: Check network connectivity
- `READ_EXTERNAL_STORAGE`: Access APK files for analysis
- `READ_MEDIA_IMAGES/VIDEO/AUDIO`: Modern storage access (API 33+)
- `QUERY_ALL_PACKAGES`: List all installed applications

### Analyzed Permissions (What the App Checks)
The app analyzes over 40 different Android permissions, categorized by risk level:

**High Risk:**
- Camera, Microphone, Location (Fine/Coarse)
- SMS, Call logs, Contacts
- Calendar access

**Medium Risk:**
- Phone state, External storage write
- SMS sending, Phone calls
- Body sensors

**Low Risk:**
- Internet, Network state, Vibration
- WiFi state, Bluetooth, NFC

## 🎨 Design System

### Colors
- **Primary**: Blue gradient for action buttons
- **Surface**: Card backgrounds with elevation
- **Text**: Primary and secondary text colors
- **Accent**: White text on colored backgrounds

### Typography
- **Headings**: 20-24sp with bold weight
- **Body**: 14-16sp regular weight
- **Captions**: 12-14sp for secondary information

### Components
- **CardView**: Elevated containers with rounded corners
- **Buttons**: Gradient backgrounds with ripple effects
- **Switches**: Material Design switches for settings
- **RecyclerView**: Efficient list rendering

## 🧪 Testing

### Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Instrumentation Tests
```bash
./gradlew connectedDebugAndroidTest
```

### Test Coverage
- Permission parsing and risk assessment
- APK file analysis
- UI component interactions
- Data persistence

## 📊 Data & Privacy

### Data Collection
- **No personal data collected**: The app only analyzes locally stored APK files and installed app permissions
- **No internet communication**: All analysis happens offline
- **No data storage**: Results are not saved or transmitted

### Privacy Features
- **Local analysis only**: Everything processed on-device
- **No tracking**: No analytics or crash reporting
- **Permission transparency**: Clear explanation of why each permission is needed

## 🤝 Contributing

### Development Setup
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Make your changes and test thoroughly
4. Commit with clear messages: `git commit -m "Add feature X"`
5. Push to your fork: `git push origin feature/your-feature`
6. Create a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Maintain consistent formatting

### Testing Requirements
- Add unit tests for new features
- Ensure all existing tests pass
- Test on multiple Android versions
- Verify APK analysis works correctly

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Android Developer Documentation**: Comprehensive API references
- **Material Design Guidelines**: UI/UX best practices
- **Kotlin Language**: Modern Android development
- **AndroidX Libraries**: Robust framework components

## 📞 Support

### Contact Information
- **Developer**: Shubham Singh
- **Email**: shubhamsingh9974525390@email.com
- **GitHub**: [shubham4734singh](https://github.com/shubham4734singh)
- **LinkedIn**: [shubham4734singh](https://www.linkedin.com/in/shubham4734singh/)

### Issue Reporting
- Use GitHub Issues for bug reports
- Include device information and Android version
- Attach APK files if analysis fails
- Provide step-by-step reproduction instructions

---

**Made with ❤️ for Android privacy and security**