# 应用图标说明

本项目需要在 `app/src/main/res/mipmap-*` 目录下添加应用图标资源。

## 需要的图标文件

请将以下图标文件放置到对应的目录：

### ic_launcher.png
- `mipmap-hdpi/ic_launcher.png` - 72x72 px
- `mipmap-mdpi/ic_launcher.png` - 48x48 px
- `mipmap-xhdpi/ic_launcher.png` - 96x96 px
- `mipmap-xxhdpi/ic_launcher.png` - 144x144 px
- `mipmap-xxxhdpi/ic_launcher.png` - 192x192 px

### ic_launcher_round.png (圆形图标，可选)
- `mipmap-hdpi/ic_launcher_round.png` - 72x72 px
- `mipmap-mdpi/ic_launcher_round.png` - 48x48 px
- `mipmap-xhdpi/ic_launcher_round.png` - 96x96 px
- `mipmap-xxhdpi/ic_launcher_round.png` - 144x144 px
- `mipmap-xxxhdpi/ic_launcher_round.png` - 192x192 px

## 图标设计建议

本应用使用"温暖纸张质感"主题，建议图标设计：
- 背景：暖色米白色 (#FDF6E3)
- 图案：日记本或钢笔图标
- 配色：暖橙色 (#D87C4A) 或深棕色 (#3E2723)

可以使用 Android Studio 的 Image Asset Studio 创建图标：
1. 右键点击 `res` 文件夹
2. 选择 New > Image Asset
3. 选择 Launcher Icons (Adaptive and Legacy)
4. 配置图标样式并生成
