# 编译 APK 指南

## 方法一：GitHub Actions 自动构建（推荐，无需本地环境）

1. 将本工程推送到 GitHub 仓库
2. 进入仓库页面 → Actions → "Build APK" → Run workflow
3. 约 5-10 分钟后，Actions 页面下方会生成 `TPlus_JFFZ-debug` artifact，下载即可获得 APK

## 方法二：Android Studio 本地构建

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更新版本
- JDK 17
- Android SDK API 34

### 步骤
1. Android Studio → `File → New → Import Project` → 选择本工程文件夹
2. 等待 Gradle Sync 完成（自动下载依赖）
3. 顶部工具栏选择 `Build → Build Bundle(s) / APK(s) → Build APK(s)`
4. 编译完成后，右下角弹出提示 → 点击 `locate` 即可找到 APK 文件
   - 默认路径：`app/build/outputs/apk/debug/app-debug.apk`

### 签名发布 APK
如果需要正式发布（非调试版）：
1. `Build → Generate Signed Bundle / APK`
2. 选择 APK → 创建或选择密钥库 (keystore)
3. 选择 release 变体 → Finish
4. 产物位于 `app/build/outputs/apk/release/app-release.apk`

## 方法三：命令行构建（需要 Android SDK）

```bash
# 1. 设置 ANDROID_HOME 环境变量
export ANDROID_HOME=/path/to/android-sdk

# 2. 编译 Debug APK
./gradlew assembleDebug

# 3. 编译 Release APK（需要配置签名）
./gradlew assembleRelease
```

产物位置：
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release-unsigned.apk`

## 常见问题

**Q: Gradle Sync 失败，提示 "Could not resolve ..."**
A: 检查网络连接，确保能访问 `https://repo1.maven.org` 和 `https://dl.google.com`。如有代理，在 `gradle.properties` 中添加代理配置。

**Q: Build 报错 "Could not find android-sdk"**
A: Android Studio 会自动配置 SDK 路径。如使用命令行，确保 `ANDROID_HOME` 环境变量指向正确的 SDK 目录。

**Q: 如何修改应用名称或图标？**
A: 
- 名称：修改 `app/src/main/res/values/strings.xml` 中的 `app_name`
- 图标：替换 `app/src/main/res/mipmap-xxx` 目录下的 `ic_launcher.png` 文件

**Q: APK 安装后无法连接 T+ 服务器？**
A: 首次启动后进入"服务器设置"页面，配置正确的 T+ 服务器 IP 地址和端口。确保手机与服务器在同一网络，或服务器有公网访问权限。
