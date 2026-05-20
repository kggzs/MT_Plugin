# MTKang Plugin 编译说明文档

## 项目简介

MTKang Plugin 是一个 MT 管理器 V3 的 AI 智能编程助手插件，使用 Java 17 和 Gradle 构建。

## 环境要求

| 环境 | 版本 |
|------|------|
| Java | 17 (LTS) |
| Gradle | 8.13 |
| Android SDK | Min SDK 21, Target SDK 28, Compile SDK 36 |
| MT 管理器 | 2.26.3+ (需要 VIP 权限) |

## 编译步骤

### 1. 进入项目目录

```powershell
cd e:\www\mt-kang
```

### 2. 执行编译命令

```powershell
.\gradlew.bat packageReleaseMtp --no-daemon
```

### 3. 编译输出

编译成功后，输出文件位于：

```
build/outputs/mt-plugin/com.kggzs.cn.mt.mtp
```

## 常用 Gradle 命令

| 命令 | 说明 |
|------|------|
| `.\gradlew.bat packageReleaseMtp` | 打包 Release 版本的 MTP 插件文件 |
| `.\gradlew.bat clean` | 清理构建输出 |
| `.\gradlew.bat build` | 完整构建项目 |
| `.\gradlew.bat assembleDebug` | 构建调试版本 |

## 编译配置说明

### build.gradle 关键配置

```gradle
android {
    namespace = 'com.kggzs.cn.mt'
    compileSdk = 36
    defaultConfig {
        targetSdk = 28
        minSdk = 21
    }
}

mtPlugin {
    pluginID = "com.kggzs.cn.mt"
    versionCode = 4
    versionName = "v2.0.2"
    name = "{plugin_name}"
    description = "{plugin_description}"
    mainPreference = "com.example.myplugin.MyPreference"
    interfaces = [
        "com.example.myplugin.EncodeDecodeMenu",
        "com.example.myplugin.QuickInsertFunction",
        "com.example.myplugin.AICodeAnalysisToolMenu",
        "com.example.myplugin.AICodeAnalysisFloatingMenu"
    ]
}
```

### libs.versions.toml 版本目录

```toml
[versions]
agp = "8.13.2"
kotlin = "2.3.0"
mt-plugin = "1.0.0-beta6"
desugarJdkLibs = "2.1.5"

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
mt-plugin = { id = "bin.mt.plugin", version.ref = "mt-plugin" }
```

## 验证编译结果

### 检查输出文件

```powershell
Get-ChildItem -Path E:\www\mt-kang\build\outputs\mt-plugin -Filter *.mtp
```

### 预期输出

```
Name                Length LastWriteTime
----                ------ -------------
com.kggzs.cn.mt.mtp  47822 2026/5/20 23:00:19
```

## 安装插件

1. 将生成的 `.mtp` 文件复制到 Android 设备
2. 在 MT 管理器中打开该文件
3. 按照提示完成安装（需要 MT 管理器 VIP 权限）

## 常见问题

### Q1: 编译失败，提示 Java 版本不匹配

**A**: 确保系统安装了 Java 17，并设置 `JAVA_HOME` 环境变量指向 Java 17 的安装目录。

### Q2: 找不到 gradlew.bat

**A**: 确保在项目根目录 `e:\www\mt-kang` 下执行命令。

### Q3: 编译成功但找不到输出文件

**A**: 输出文件可能被 .gitignore 排除，使用以下命令查找：

```powershell
Get-ChildItem -Path E:\www\mt-kang -Recurse -Filter *.mtp -ErrorAction SilentlyContinue
```

### Q4: Gradle 同步失败

**A**: 
1. 检查网络连接，确保可以访问 Maven 仓库
2. 清理 Gradle 缓存：`.\gradlew.bat clean`
3. 删除 `.gradle` 文件夹后重新同步

## 项目结构

```
mt-kang/
├── src/main/
│   ├── java/com/example/myplugin/    # Java 源代码
│   │   ├── EncodeDecodeMenu.java     # 编码/解码菜单
│   │   ├── AICodeAnalysisToolMenu.java # AI 分析工具菜单
│   │   ├── AICodeAnalysisFloatingMenu.java # AI 分析浮动菜单
│   │   ├── AICodeAnalysisHelper.java # AI 分析辅助类
│   │   ├── QuickInsertFunction.java  # 快速插入时间
│   │   ├── MyPreference.java         # 设置界面
│   │   └── util/
│   │       ├── AIHelper.java         # AI 工具类
│   │       ├── TimeFormatHelper.java # 时间格式工具
│   │       └── LunarCalendar.java    # 农历计算
│   ├── assets/                       # 语言包和资源
│   │   ├── strings.mtl               # 默认语言
│   │   ├── strings-zh-CN.mtl         # 简体中文
│   │   └── ...                       # 其他语言
│   ├── resources/                    # 图标资源
│   └── AndroidManifest.xml           # 清单文件
├── docs/                             # 文档目录
├── gradle/
│   └── libs.versions.toml            # 版本目录
├── build.gradle                      # 构建配置
├── settings.gradle                   # Gradle 设置
├── gradle.properties                 # Gradle 属性
├── proguard-rules.pro                # 混淆规则
├── BUILD.md                          # 本文件
└── README.md                         # 项目说明
```

## 更新日志

| 日期 | 版本 | 说明 |
|------|------|------|
| 2026-05-20 | v2.0.2 | 更新编译文档，添加新功能说明 |
| 2026-04-09 | v2.0 | 初始编译文档 |

---

**注意**: 编译前请确保已安装 Java 17 并正确配置环境变量。
