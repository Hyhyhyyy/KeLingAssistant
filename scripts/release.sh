#!/bin/bash

# 课灵APK发布脚本
# 用于创建GitHub Release并上传APK

set -e

# 配置
REPO_OWNER="Hyhyhyyy"
REPO_NAME="KeLingAssistant"
ANDROID_DIR="apps/android"

# 获取版本信息
VERSION_CODE=$(grep "versionCode" "$ANDROID_DIR/app/build.gradle.kts" | head -1 | sed 's/.*versionCode = //' | tr -d ' ')
VERSION_NAME=$(grep "versionName" "$ANDROID_DIR/app/build.gradle.kts" | head -1 | sed 's/.*versionName = "//' | sed 's/"$//' | tr -d ' ')

TAG_NAME="v${VERSION_NAME}"
APK_NAME="KeLing-${TAG_NAME}.apk"

echo "========================================"
echo "课灵 APK 发布脚本"
echo "========================================"
echo "版本号: ${VERSION_NAME} (code: ${VERSION_CODE})"
echo "标签名: ${TAG_NAME}"
echo "APK名: ${APK_NAME}"
echo "========================================"

# 构建release APK
echo ""
echo "步骤1: 构建 release APK..."
cd "$ANDROID_DIR"
./gradlew assembleRelease

# 复制APK到发布目录
RELEASE_DIR="../releases"
mkdir -p "$RELEASE_DIR"
cp "app/build/outputs/apk/release/app-release.apk" "$RELEASE_DIR/$APK_NAME"

cd ..

# 生成更新日志
CHANGELOG_FILE="$RELEASE_DIR/changelog.md"
cat > "$CHANGELOG_FILE" << EOF
## 课灵 ${TAG_NAME}

### 更新内容
✨ 新增数据本地持久化，退出APP数据不丢失
✨ 知识图谱支持可拖动编辑、曲线箭头连接
✨ 笔记编辑器支持字体大小、颜色、高亮、分类
✨ 个人中心头像显示用户头像
✨ 固定底部导航栏，优化退出逻辑
✨ 签到日历移至首页
✨ 移动端添加网页端链接，网页端添加移动端下载链接
✨ 优化检查更新功能，增加加载状态和错误提示
🔧 修复星球图像显示问题
🔧 修复数据不保存问题
🔧 修复底部导航栏与页面协调问题

### 版本信息
- 版本号: ${VERSION_NAME}
- 版本代码: ${VERSION_CODE}
- 最低Android版本: Android 8.0 (API 26)
- 目标Android版本: Android 14 (API 34)

### 下载
- [下载APK](https://github.com/${REPO_OWNER}/${REPO_NAME}/releases/download/${TAG_NAME}/${APK_NAME})
EOF

echo ""
echo "步骤2: Git提交和推送..."
git add -A
git commit -m "chore: release ${TAG_NAME}" || echo "No changes to commit"
git push origin main

echo ""
echo "步骤3: 创建Git标签..."
git tag -a "$TAG_NAME" -m "Release ${TAG_NAME}" || echo "Tag already exists"
git push origin "$TAG_NAME" || echo "Tag already pushed"

echo ""
echo "========================================"
echo "发布准备完成！"
echo "========================================"
echo ""
echo "APK文件: $RELEASE_DIR/$APK_NAME"
echo "更新日志: $RELEASE_DIR/changelog.md"
echo ""
echo "下一步："
echo "1. 访问 https://github.com/${REPO_OWNER}/${REPO_NAME}/releases/new"
echo "2. 选择标签: ${TAG_NAME}"
echo "3. 填写标题: 课灵 ${TAG_NAME}"
echo "4. 复制changelog.md内容作为描述"
echo "5. 上传APK文件: $RELEASE_DIR/$APK_NAME"
echo "6. 点击发布"
echo ""
echo "或使用GitHub CLI (如果已安装):"
echo "gh release create ${TAG_NAME} $RELEASE_DIR/$APK_NAME --title \"课灵 ${TAG_NAME}\" --notes-file $RELEASE_DIR/changelog.md"
echo "========================================"

# 输出下载链接
echo ""
echo "下载链接 (发布后可用):"
echo "https://github.com/${REPO_OWNER}/${REPO_NAME}/releases/download/${TAG_NAME}/${APK_NAME}"