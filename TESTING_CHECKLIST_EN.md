# ✅ Android App Testing Checklist

## 🎯 Testing Objectives
Verify that all features of the Gemma 3N Multimodal Android Assistant work properly and evaluate performance and user experience.

## 📋 Basic Environment Testing

### 1. Build and Installation Testing
- [ ] **Project clone successful**
  ```bash
  git clone https://github.com/cis2042/ej-gemma-3n.git
  ```
- [ ] **Build successful**
  ```bash
  ./gradlew clean assembleDebug
  ```
- [ ] **Unit tests pass**
  ```bash
  ./gradlew test
  ```
- [ ] **APK generation successful**
  - Check: `app/build/outputs/apk/debug/app-debug.apk`
- [ ] **Installation to device successful**
  ```bash
  ./gradlew installDebug
  ```

### 2. App Launch Testing
- [ ] **App icon displays normally**
- [ ] **Splash screen normal**
- [ ] **Main interface loads successfully**
- [ ] **No crashes or ANR**
- [ ] **Reasonable startup time** (< 3 seconds)

## 📱 Core Functionality Testing

### 3. Chat Interface Testing
- [ ] **Interface layout correct**
  - Input box at bottom
  - Message list in middle
  - Attachment button visible
- [ ] **Text input functionality**
  - Can input text
  - Send button responsive
  - Messages display in chat list
- [ ] **Message display**
  - User messages on right (blue)
  - AI responses on left (gray)
  - Timestamps display correctly
- [ ] **Scrolling functionality**
  - Message list can scroll
  - New messages auto-scroll to bottom

### 4. Permission Management Testing
- [ ] **Camera permission**
  - Requests permission on first use
  - Shows prompt when permission denied
  - Can re-request permission
- [ ] **Microphone permission**
  - Voice features trigger permission request
  - Permission handling correct
- [ ] **Storage permission**
  - File upload triggers permission request
  - Permission status handled correctly
- [ ] **Settings navigation**
  - Can navigate to system settings
  - Status updates after returning to app

### 5. Multimedia Feature Testing

#### Image Features
- [ ] **Photo capture**
  - Click attachment → camera
  - Camera interface normal
  - Photo capture successful
  - Image preview correct
- [ ] **Gallery selection**
  - Click attachment → gallery
  - Can select images
  - Images load correctly
- [ ] **Image sending**
  - Can send selected images
  - Images display in chat
  - Thumbnails generate correctly

#### Audio Features
- [ ] **Recording functionality**
  - Long press send button starts recording
  - Recording interface displays
  - Recording time shows
  - Release button stops recording
- [ ] **Audio playback**
  - Recorded audio can play
  - Playback controls normal
  - Audio quality clear
- [ ] **Speech recognition**
  - Voice-to-text functionality
  - Recognition accuracy testing

#### Video Features
- [ ] **Video recording**
  - Can record videos
  - Recording interface normal
  - Video quality reasonable
- [ ] **Video selection**
  - Can select videos from gallery
  - Videos load correctly
- [ ] **Video preview**
  - Thumbnail generation
  - Can play preview

#### Document Features
- [ ] **Document selection**
  - Can select PDF files
  - Can select text files
- [ ] **Document parsing**
  - Document content extraction correct
  - Supported formats handled properly

### 6. AI Functionality Testing
- [ ] **Model loading**
  - Model loads successfully on app start
  - Reasonable loading time
  - No error messages
- [ ] **Text responses**
  - AI responds after sending text messages
  - Response content reasonable
  - Acceptable response speed
- [ ] **Multimodal understanding**
  - AI can understand image content after sending images
  - Audio-to-text functionality normal
  - Document content can be processed

### 7. Data Persistence Testing
- [ ] **Chat history saving**
  - Sent messages are saved
  - History remains after app restart
- [ ] **Database operations**
  - Message insertion normal
  - Message querying normal
  - Database upgrade normal

## 🚀 Performance Testing

### 8. Memory and Performance
- [ ] **Memory usage**
  - Normal usage < 200MB memory
  - No memory leaks during long-term use
  - Memory control during large file processing
- [ ] **CPU usage**
  - Low CPU usage when idle
  - Reasonable CPU usage during AI inference
- [ ] **Battery consumption**
  - Low battery consumption in background
  - Reasonable battery consumption during extended use
- [ ] **Storage space**
  - Reasonable app size
  - Controllable chat data growth

### 9. Response Speed
- [ ] **Interface responsiveness**
  - Click response < 100ms
  - Smooth scrolling without lag
  - Smooth animation playback
- [ ] **AI response speed**
  - Text response < 5 seconds
  - Image analysis < 10 seconds
  - Speech recognition < 3 seconds

### 10. Stability Testing
- [ ] **Long-term running**
  - No crashes after 1 hour continuous use
  - No issues after sending 100+ messages
  - No crashes after uploading multiple large files
- [ ] **Exception handling**
  - Proper handling when network disconnected
  - Prompts when storage space insufficient
  - Correct handling when permissions revoked

## 📱 Device Compatibility Testing

### 11. Different Device Testing
- [ ] **Low-end devices** (2GB RAM)
  - App runs normally
  - Acceptable performance
  - Complete functionality
- [ ] **Mid-range devices** (4GB RAM)
  - Smooth operation
  - All features normal
- [ ] **High-end devices** (8GB+ RAM)
  - Best performance
  - Hardware acceleration effective

### 12. System Version Testing
- [ ] **Android 8.0** (API 26)
  - Basic functionality normal
- [ ] **Android 10.0** (API 29)
  - All features normal
- [ ] **Android 13.0** (API 33)
  - Latest system compatibility

## 🎨 User Experience Testing

### 13. Interface and Interaction
- [ ] **Visual design**
  - Modern and beautiful interface
  - Reasonable color scheme
  - Appropriate font size
- [ ] **Interaction experience**
  - Clear operation logic
  - Timely and clear feedback
  - User-friendly error messages
- [ ] **Accessibility support**
  - TalkBack support
  - Standard-compliant contrast
  - Appropriate touch target size

### 14. Error Handling
- [ ] **Network errors**
  - Offline mode works normally
  - Clear error messages
- [ ] **File errors**
  - Prompts for unsupported file formats
  - Prompts when files too large
- [ ] **Permission errors**
  - Guides users when permissions denied
  - Provides solutions

## 📊 Test Report Template

### Test Results Summary
```
✅ Passed items: ___/___
⚠️ Items needing fixes: ___
❌ Critical issues: ___

Overall assessment:
□ Ready for release
□ Needs minor fixes
□ Needs major fixes
□ Not recommended for release
```

### Major Issues Record
```
1. Issue description:
   Reproduction steps:
   Expected result:
   Actual result:
   Severity: High/Medium/Low

2. Issue description:
   ...
```

### Performance Data
```
- App size: ___ MB
- Startup time: ___ seconds
- Memory usage: ___ MB
- AI response time: ___ seconds
- Battery consumption: ___% /hour
```

### Suggestions and Improvements
```
1. Priority fix items:
2. Performance optimization suggestions:
3. Feature enhancement suggestions:
4. User experience improvements:
```

---

**📝 Note: Please test in order, record all discovered issues, and provide a detailed test report.**
