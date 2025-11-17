# Changelog

All notable changes to Floating Dock will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.2] - 2025-11-15

### Added
- **Automatic Update Checker**: New feature to check for app updates directly from GitHub releases
  - Manual update check button in settings
  - Automatic version comparison with latest GitHub release
  - Direct download of APK from GitHub releases
  - Option to open GitHub releases page in browser
- **Drag and Drop Reordering**: Dock apps can now be reordered by dragging items in the list
  - Visual drag handle indicator on each dock app item
  - Long-press to drag and reorder apps
  - Changes are automatically saved and reflected in the dock

### Changed
- **UI Layout Improvements**: 
  - "Check for updates" button moved to bottom section with other action buttons
  - Improved button organization and layout consistency

### Technical Improvements
- **Update Checker Implementation**: 
  - GitHub API integration for release checking
  - Semantic version comparison
  - DownloadManager integration for APK downloads
  - Proper permission handling for Android 8.0+ installations

## [1.0.1] - 2025-11-15

### Added
- **Auto-start App**: Configure a specific app to launch automatically when the service starts (only on first service start, not on restart)
- **Draggable Dock**: Toggle to enable/disable dock dragging. When enabled, long-press (1 second) on the dock or icons to drag it to a new position
- **Dock Behavior Options**:
  - **Hide on action**: Dock can be hidden using the "Hide Dock" system action
  - **Hide after time**: Dock automatically hides after a configurable timeout (1-60 seconds). Timer resets when icons are clicked or when dragging
- **Hide Dock System Action**: New system action to manually hide the dock
- **Hide Timeout Configuration**: Configurable timeout for auto-hide behavior (in seconds)
- **Visual Drag Indicator**: White rounded bar indicator appears when the dock is hidden, showing the exposed area
- **Negative Margins Support**: Dock can now be positioned off-screen using negative margin values (-200 to 200 dp)
- **Real-time Configuration Updates**: Configuration changes are now applied instantly without service restart, with smooth animations
- **Multi-language Support**: Added English and Spanish language support
- **Dynamic Material Symbols Loading**: Material Symbols icons are now loaded from JSON file at runtime, supporting all 4,000+ icons

### Changed
- **Separate Horizontal/Vertical Margins**: Margins are now configured separately for X and Y axes
- **Automatic Save on Dock Changes**: Adding, editing, or deleting dock apps now automatically saves and updates the service
- **Improved Position Calculation**: Better positioning logic for draggable and non-draggable modes
- **Service Restart on Draggable Toggle**: Service now restarts when draggable mode is toggled to ensure proper initialization
- **Removed "Fixed" Behavior**: "Fixed" behavior option removed (functionality merged with "Hide on action")

### Fixed
- **Position Inversion Bug**: Fixed issue where initial position was inverted (top/bottom, left/right)
- **Vertical Margin Not Working**: Fixed vertical margin not being applied correctly
- **Margin Changes on Draggable Toggle**: Fixed margins changing unexpectedly when toggling draggable mode
- **Hidden Dock Position Calculation**: Fixed dock not hiding completely off-screen when hiding to the right
- **Indicator Position**: Fixed drag indicator position calculation when switching between draggable modes
- **Click Functionality with Draggable**: Fixed icon clicks not working when draggable mode was enabled
- **NullPointerException in dockAppsEqual**: Fixed crash when comparing dock apps with null values

### Removed
- **Keyboard Detection**: Removed automatic dock hiding when keyboard opens (feature removed per user request)
- **"Fixed" Behavior Option**: Removed from dock behavior options (merged with "Hide on action")
- **"center_center" Position**: Removed from position options (migrated to "center_left" for existing users)

### Technical Improvements
- **Code Cleanup**: Removed unused methods and variables (`scheduleServiceRestart`, `settingsUpdateHandler`, `settingsUpdateRunnable`)
- **Deprecated Code Removal**: Removed deprecated `getLegacyIconNames()` method from `MaterialIconHelper`
- **BroadcastReceiver Implementation**: Added real-time configuration updates via BroadcastReceiver instead of service restarts
- **Improved Animation System**: Enhanced animations for dock hiding/showing with border radius transitions
- **Better State Management**: Improved handling of dock hidden/visible states during configuration changes

## [1.0.0] - 2025-11-14

### Initial Release

- **Floating Dock**: Access favorite applications from any screen
- **System Actions**: Control functions like Home, Back, Volume, Media playback, and more
- **Full Customization**:
  - Configurable icon size
  - Initial dock position (9 positions available)
  - Background color and transparency
  - Icon color and transparency
  - Dock border radius
  - Icon spacing
  - Icon padding
  - Margins from edges
- **Material Symbols Icons**: Over 4,000 icons available
- **Native Icons**: Option to use each application's native icon
- **Activity Selection**: For apps with multiple activities (like car launchers)
- **Auto Start**: The service starts automatically on system boot
- **Keyboard Detection**: The dock automatically hides when the keyboard opens

