# 日记本 App

一个温暖纸张质感的 Android 日记应用，支持图文记录、AI 自动标签分析和每周总结。

## 功能特性

### 核心功能
- **图文并茂** - 支持文字、图片、视频记录
- **时间线浏览** - 按时间顺序展示所有日记
- **关键词搜索** - 快速找到想看的日记
- **每周总结** - 智能生成本周精选回顾
- **本地存储** - 所有数据保存在本地，保护隐私

### 智能功能
- **AI 自动标签** - 离线分析内容自动打标签
- **主题分类** - 系统预设 + 用户自定义主题
- **心情记录** - 1-5 分心情评分
- **每周精选** - 手动标记或自动推荐精选日记

### 分享导出
- **海报生成** - 将日记生成精美图片分享
- **数据导出** - 支持 JSON/TEXT 格式导出备份
- **主题分享** - 可选择性分享日记内容

## 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **数据库**: Room + SQLite
- **架构**: MVVM + Repository Pattern
- **依赖注入**: 手动实现（无 DI 框架）
- **图片加载**: Coil
- **视频播放**: ExoPlayer

## 项目结构

```
app/src/main/java/com/example/diaryapp/
├── data/                          # 数据层
│   ├── database/                  # 数据库相关
│   │   ├── entities/              # 数据实体
│   │   │   ├── DiaryEntry.kt      # 日记实体
│   │   │   └── Theme.kt           # 主题实体
│   │   ├── dao/                   # 数据访问对象
│   │   │   ├── DiaryDao.kt
│   │   │   └── ThemeDao.kt
│   │   ├── converters/            # 类型转换器
│   │   │   └── Converters.kt
│   │   └── DiaryDatabase.kt       # 数据库类
│   └── repository/                # 数据仓库
│       └── DiaryRepository.kt
├── ui/                            # UI 层
│   ├── screens/                   # 屏幕
│   │   ├── HomeScreen.kt          # 主页
│   │   ├── SearchScreen.kt        # 搜索页
│   │   ├── EditorScreen.kt        # 编辑器页
│   │   ├── WeeklySummaryScreen.kt # 每周总结页
│   │   └── ThemeManageScreen.kt   # 主题管理页
│   ├── components/                # UI 组件
│   │   ├── RichTextEditor.kt      # 富文本编辑器
│   │   └── DiaryComponents.kt     # 日记相关组件
│   └── theme/                     # 主题
│       ├── Color.kt               # 颜色定义
│       ├── Type.kt                # 字体样式
│       └── Theme.kt               # 主题配置
├── service/                       # 服务层
│   ├── TagAnalyzerService.kt      # 标签分析服务
│   └── WeeklySummaryService.kt    # 每周总结服务
├── util/                          # 工具类
│   └── ExportUtil.kt              # 导出工具
├── MainActivity.kt                # 主 Activity
└── MainViewModel.kt               # 主 ViewModel
```

## 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34
- Gradle 8.2
- Kotlin 1.9.20

## 构建项目

### 使用 Android Studio

1. 打开 Android Studio
2. 选择 `File > Open`
3. 选择项目根目录 `DiaryApp`
4. 等待 Gradle 同步完成
5. 点击 Run 按钮或按 `Shift + F10` 运行

### 使用命令行

```bash
# 在项目根目录执行
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug
```

## 配置说明

### 应用图标

请参考 `README_ICONS.md` 添加应用图标资源。

### 数据库

数据库会在应用首次启动时自动创建，系统预设主题也会自动初始化。

## 开发说明

### 添加新功能

1. **数据层**: 在 `data/database/entities` 添加实体类
2. **DAO**: 在 `data/database/dao` 添加数据访问方法
3. **Repository**: 在 `data/repository` 添加业务逻辑
4. **ViewModel**: 在 `MainViewModel.kt` 添加 UI 状态和方法
5. **UI**: 在 `ui/screens` 添加新的屏幕组件

### UI 主题

项目使用温暖纸张质感配色，可在 `ui/theme/Color.kt` 中修改颜色定义。

## 注意事项

1. **权限**: 应用需要读写外部存储权限来处理图片和导出文件
2. **Android 版本**: 最低支持 Android 8.0 (API 26)
3. **图片**: 图片和视频使用 URI 引用，删除文件会导致显示失败

## 许可证

本项目仅供学习交流使用。

## 作者

Created with Claude Code
