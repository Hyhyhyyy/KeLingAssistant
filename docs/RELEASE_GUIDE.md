# 课灵 APK 发布指南

## 快速发布流程

### 方式一：手动发布（推荐）

1. **运行发布脚本**
   ```bash
   cd scripts
   ./release.bat   # Windows
   # 或
   ./release.sh    # Linux/Mac
   ```

2. **脚本会自动完成**
   - 构建release APK
   - 复制APK到 `releases/` 目录
   - 提交代码并推送到GitHub
   - 创建Git标签并推送

3. **创建GitHub Release**
   - 访问 https://github.com/Hyhyhyyy/KeLingAssistant/releases/new
   - 选择刚创建的标签
   - 填写标题和更新日志
   - 上传APK文件
   - 点击发布

### 方式二：GitHub Actions自动构建

当你推送一个版本标签时，GitHub Actions会自动构建APK并创建Release：

```bash
# 构建APK后，创建并推送标签
cd apps/android
./gradlew assembleRelease
cd ../..
git tag -a v3.0.8 -m "Release 3.0.8"
git push origin v3.0.8
```

GitHub Actions会自动：
- 构建APK
- 创建Release
- 上传APK文件

## 版本号规则

- `versionCode`: 数字，每次发布+1（build.gradle.kts中）
- `versionName`: 字符串，如 "3.0.8"（build.gradle.kts中）
- Git标签: `v{versionName}`，如 `v3.0.8`

## 下载链接

发布成功后，下载链接格式：
```
https://github.com/Hyhyhyyy/KeLingAssistant/releases/download/v3.0.8/KeLing-v3.0.8.apk
```

最新版本页面：
```
https://github.com/Hyhyhyyy/KeLingAssistant/releases/latest
```

## 自动同步机制

服务器端会自动从GitHub API获取最新版本信息：
- 每5分钟刷新一次缓存
- 自动获取版本号、下载链接、更新日志
- 无需手动更新服务器配置

## 更新日志模板

```
【课灵 X.X.X 更新内容】
✨ 新功能描述
🔧 修复问题描述
💡 优化改进描述

### 版本信息
- 版本号: X.X.X
- 版本代码: XXX
- 最低Android版本: Android 8.0 (API 26)
```

## 检查清单

发布前确认：
- [ ] 版本号已更新 (build.gradle.kts)
- [ ] 签名配置正确 (keling-release.jks)
- [ ] 测试关键功能正常
- [ ] 更新日志已编写

发布后确认：
- [ ] GitHub Release已创建
- [ ] APK已上传
- [ ] 下载链接可访问
- [ ] 应用内检查更新显示新版本