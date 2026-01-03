# VS Code Android 开发配置指南

## 一、前置条件

### 1. 安装 JDK 17
下载并安装 JDK 17：
- Oracle JDK: https://www.oracle.com/java/technologies/downloads/#java17
- 或 OpenJDK: https://adoptium.net/

### 2. 安装 Android SDK
有两种方式：

**方式 A: 安装 Android Studio**（推荐）
1. 下载 Android Studio: https://developer.android.com/studio
2. 安装后，SDK 位置通常在：
   - Windows: `C:\Users\你的用户名\AppData\Local\Android\Sdk`
   - Mac: `~/Library/Android/sdk`
   - Linux: `~/Android/Sdk`

**方式 B: 单独安装 Android SDK 命令行工具**
1. 下载 Command line tools: https://developer.android.com/studio#command-tools
2. 解压到指定目录
3. 运行 `sdkmanager` 安装必要组件

### 3. 配置环境变量
在系统环境变量中添加：
```
ANDROID_HOME=C:\Users\你的用户名\AppData\Local\Android\Sdk
JAVA_HOME=C:\Program Files\Java\jdk-17
```

并在 PATH 中添加：
```
%ANDROID_HOME%\platform-tools
%ANDROID_HOME%\tools
%JAVA_HOME%\bin
```

## 二、VS Code 插件安装

### 方式 A: 自动安装推荐插件
1. 在 VS Code 中打开项目文件夹
2. 按 `Ctrl + Shift + P` 打开命令面板
3. 输入 `Extensions: Show Recommended Extensions`
4. 安装所有推荐的插件

### 方式 B: 手动安装核心插件
在 VS Code 扩展商店搜索并安装：

| 插件名称 | 用途 |
|---------|------|
| **Language Support for Java** | Java 语言支持 |
| **Kotlin Language** | Kotlin 语言支持 |
| **Gradle for Java** | Gradle 构建 |
| **Android iOS Emulator** | 模拟器支持 |
| **Debugger for Java** | Java 调试 |
| **GitLens** | Git 增强 |

## 三、项目配置

### 1. 打开项目
```bash
# 在命令行中进入项目目录
cd G:\GITHUB\DiaryApp

# 用 VS Code 打开
code .
```

### 2. 等待依赖下载
首次打开项目，VS Code 会：
1. 检测到 Java 项目
2. 下载 Gradle 依赖（可能需要几分钟）
3. 索引项目文件

### 3. 配置 Java 和 Android SDK
打开 VS Code 设置（`Ctrl + ,`），搜索并设置：

```
Java > Java Home: C:\Program Files\Java\jdk-17
Android > CustomSdkPath: C:\Users\你的用户名\AppData\Local\Android\Sdk
```

## 四、构建和运行

### 在 VS Code 终端中构建

```bash
# 清理构建
./gradlew clean

# 构建调试版本
./gradlew assembleDebug

# 安装到连接的设备
./gradlew installDebug
```

### 使用 VS Code 任务
1. 按 `Ctrl + Shift + B` 或 `F1` -> `Tasks: Run Task`
2. 选择要运行的任务：
   - `Gradle: Build Debug` - 构建项目
   - `Gradle: Install Debug` - 安装到设备
   - `Gradle: Run Tests` - 运行测试

## 五、调试

### 方式 A: 使用 ADB 调试
1. 连接 Android 设备或启动模拟器
2. 启用 USB 调试
3. 在终端运行：
   ```bash
   adb logcat  # 查看日志
   ```

### 方式 B: 使用 Java 调试器
1. 在代码行号左侧点击设置断点
2. 按 `F5` 启动调试
3. 选择 `Debug Android App` 配置

## 六、常用命令

| 命令 | 说明 |
|------|------|
| `./gradlew assembleDebug` | 构建调试版 APK |
| `./gradlew assembleRelease` | 构建发布版 APK |
| `./gradlew installDebug` | 安装调试版到设备 |
| `./gradlew clean` | 清理构建 |
| `adb devices` | 查看连接的设备 |
| `adb install -r app-debug.apk` | 手动安装 APK |
| `adb logcat` | 查看设备日志 |

## 七、目录结构快速导航

```
DiaryApp/
├── .vscode/           # VS Code 配置
├── app/               # 应用模块
│   └── src/main/
│       ├── java/      # Kotlin 代码
│       └── res/       # 资源文件
└── build.gradle.kts   # 构建配置
```

## 八、常见问题

### 1. Gradle 下载慢
在项目根目录创建 `gradle.properties`，添加：
```properties
# 使用国内镜像
systemProp.http.proxyHost=
systemProp.http.proxyPort=
```

### 2. 找不到 Android SDK
检查环境变量 `ANDROID_HOME` 是否正确设置

### 3. Java 版本错误
确保安装的是 JDK 17，并在 VS Code 中配置正确

### 4. 模拟器启动失败
确保电脑启用了虚拟化技术（VT-x/AMD-V）

## 九、推荐的 VS Code 快捷键

| 快捷键 | 功能 |
|--------|------|
| `Ctrl + P` | 快速打开文件 |
| `Ctrl + Shift + F` | 全局搜索 |
| `F5` | 启动调试 |
| `Ctrl + Shift + B` | 运行构建任务 |
| `Ctrl + `` | 打开终端 |
| `Alt + Shift + F` | 格式化代码 |

## 十、下一步

配置完成后，你可以：
1. 开始修改代码
2. 按 `Ctrl + Shift + B` 构建项目
3. 连接手机或启动模拟器
4. 运行 `./gradlew installDebug` 安装应用

祝你开发愉快！
