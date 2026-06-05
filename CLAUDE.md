# CLAUDE.md - MTKang Plugin 项目指南

> 本文件为 AI 助手（如 Claude）提供项目上下文和开发指南

---

## 项目概述

**MTKang Plugin** 是一款专为 MT 管理器 V3 设计的 AI 智能编程助手插件。

### 核心信息

| 项目属性 | 值 |
|---------|-----|
| 插件ID | `com.kggzs.cn.mt` |
| 版本号 | v2.1.0 (versionCode: 5) |
| 最低SDK | API 21 (Android 5.0) |
| 目标SDK | API 28 |
| 编译SDK | API 36 |
| 开发语言 | Java 17 |
| 构建工具 | Gradle 8.13, AGP 8.13.2 |

### 核心功能

1. **AI 对话** - 与 AI 直接聊天，支持多轮对话和 MCP 工具调用（Agent 模式）
2. **MCP 服务配置** - 支持 MCP (Model Context Protocol) 协议，配置外部工具服务
3. **AI 代码分析** - 基于 AI 模型的智能代码分析
4. **编码/解码工具** - Base64、Hex、Unicode、URL、ROT13、二进制
5. **哈希计算** - MD5、SHA-256、SHA-512
6. **时间戳转换** - 时间戳与日期时间双向转换
7. **快速插入时间** - 支持公历、农历、干支等多种格式

---

## 项目结构

```
mt-kang/
├── src/main/
│   ├── java/com/kggzs/cn/mt/
│   │   ├── EncodeDecodeMenu.java              # 编码/解码浮动菜单
│   │   ├── AIChatMenu.java                    # AI 对话浮动菜单（新增）
│   │   ├── AICodeAnalysisToolMenu.java        # AI 代码分析工具菜单
│   │   ├── AICodeAnalysisFloatingMenu.java    # AI 快速分析浮动菜单
│   │   ├── AICodeAnalysisHelper.java          # AI 分析辅助类
│   │   ├── MCPServiceMenu.java                # MCP 服务配置菜单（新增）
│   │   ├── QuickInsertFunction.java           # 快速插入时间功能
│   │   ├── MyPreference.java                  # 插件偏好设置
│   │   └── util/
│   │       ├── AIHelper.java                  # AI 工具类（核心网络逻辑）
│   │       ├── AIChatHelper.java              # AI 对话辅助类（新增）
│   │       ├── MCPClient.java                 # MCP 客户端（新增）
│   │       ├── SkillManager.java              # Skill 管理工具类
│   │       ├── StreamParser.java              # 流式解析工具类
│   │       ├── ThreadPoolManager.java         # 线程池管理
│   │       ├── TimeFormatHelper.java          # 时间格式配置工具
│   │       └── LunarCalendar.java             # 农历计算工具
│   ├── assets/
│   │   ├── strings.mtl                        # 默认语言资源（英文）
│   │   ├── strings-zh-CN.mtl                  # 简体中文
│   │   ├── strings-zh-TW.mtl                  # 繁体中文
│   │   ├── strings-ja.mtl                     # 日语
│   │   ├── strings-ko.mtl                     # 韩语
│   │   ├── strings-ar.mtl                     # 阿拉伯语
│   │   ├── strings-de.mtl                     # 德语
│   │   ├── strings-es.mtl                     # 西班牙语
│   │   ├── strings-fr.mtl                     # 法语
│   │   ├── strings-ru.mtl                     # 俄语
│   │   ├── wx.jpg                             # 微信收款码
│   │   └── zfb.jpg                            # 支付宝收款码
│   ├── resources/
│   │   └── icon.png                           # 插件图标
│   └── AndroidManifest.xml
├── docs/                                      # 文档目录
├── gradle/
│   └── libs.versions.toml                     # 版本目录
├── build.gradle                               # 项目构建配置
├── settings.gradle                            # Gradle 设置
├── gradle.properties                          # Gradle 属性
├── proguard-rules.pro                         # 混淆规则
├── BUILD.md                                   # 编译说明
├── README.md                                  # 项目说明文档
└── CLAUDE.md                                  # 本文件
```

---

## 开发环境配置

### 环境要求

- **Android Studio**: Hedgehog (2023.1.1) 或更新
- **Java**: JDK 17
- **Gradle**: 8.13
- **Android Gradle Plugin**: 8.13.2

### 构建命令

```bash
# 调试构建
./gradlew assembleDebug

# 发布构建
./gradlew packageReleaseMtp

# 输出位置
# build/outputs/mt-plugin/
```

---

## 代码规范

### Java 代码规范

1. **减少 else 依赖** - 优先使用卫语句（提前 return/break）
2. **避免嵌套过深** - 所有代码嵌套层数 ≤ 3 层
3. **注释规范** - 使用 Javadoc 风格，中文描述
4. **语法糖控制** - 禁用过度链式调用，保留通用无歧义的语法糖

### UI 设计规范

- **黄金分割** - 所有 UI 布局遵循 1:0.618 黄金分割比例
- **窗口尺寸** - 如窗口宽 800px 时，主内容区 ≈ 494px，侧边栏 ≈ 306px

### 编码规范

- **字符集** - 所有代码默认 UTF-8 编码
- **Windows 适配** - 文件权限、路径处理、终端输出编码

---

## 多语言支持

### 支持的语言

| 语言 | 文件名 | 语言代码 |
|------|--------|----------|
| 英文（默认） | strings.mtl | - |
| 简体中文 | strings-zh-CN.mtl | zh-CN |
| 繁体中文 | strings-zh-TW.mtl | zh-TW |
| 日语 | strings-ja.mtl | ja |
| 韩语 | strings-ko.mtl | ko |
| 阿拉伯语 | strings-ar.mtl | ar |
| 德语 | strings-de.mtl | de |
| 西班牙语 | strings-es.mtl | es |
| 法语 | strings-fr.mtl | fr |
| 俄语 | strings-ru.mtl | ru |

### 语言包格式

```
key: value
```

### 语言包键分类

#### 插件信息
- `plugin_name` - 插件名称
- `plugin_description` - 插件描述
- `plugin_author` - 作者名称
- `plugin_website` - 官方网站
- `plugin_source` - 开源地址

#### 赞助相关
- `support_author` - 支持作者
- `support_author_summary` - 支持作者摘要
- `support_author_title` - 支持作者标题
- `support_message` - 支持消息
- `alipay` - 支付宝
- `wechat_pay` - 微信支付

#### 编码/解码
- `encode_decode_function` - 编码/解码功能
- `base64_encode` / `base64_decode` - Base64 编解码
- `hex_encode` / `hex_decode` - Hex 编解码
- `unicode_encode` / `unicode_decode` - Unicode 编解码
- `url_encode` / `url_decode` - URL 编解码
- `rot13_encode` / `rot13_decode` - ROT13 编解码
- `binary_encode` / `binary_decode` - 二进制编解码

#### 哈希计算
- `hash_function` - 哈希功能
- `hash_type` - 哈希类型
- `hash_error` - 哈希错误

#### 时间戳转换
- `timestamp_function` - 时间戳功能
- `timestamp_input` - 时间戳输入
- `timestamp_output` - 时间戳输出
- `convert` - 转换

#### AI 分析
- `ai_code_analysis` - AI 代码分析
- `ai_quick_analysis` - AI 快速分析
- `ai_capability_config` - AI 能力配置
- `global_analysis_prompt` - 全局分析提示词
- `short_analysis_prompt` - 简短分析提示词
- `quick_prompts` - 快速提示词
- `background_run` - 后台运行

#### 设置
- `api_config` - API 配置
- `api_address` - API 地址
- `model_name` - 模型名称
- `api_key` - API 密钥
- `reset_config` - 重置配置

#### 通用
- `ok` / `cancel` / `save` / `copy` / `replace` / `undo`
- `success` / `error` / `processing`
- `confirm` / `close`

---

## API 配置

### 默认配置

| 配置项 | 默认值 |
|--------|--------|
| API 地址 | `https://api.kggzs.cn/v1` |
| 模型名称 | `deepseek-v3.2` |
| API 密钥 | `sk-xxxxxxxxxxxxxxxxxxxxxxxx` |

### API 端点

- **聊天补全**: `POST /chat/completions`
- **流式输出**: 支持 SSE (Server-Sent Events)

---

## 插件接口

### 已注册接口

```gradle
interfaces = [
    "com.kggzs.cn.mt.EncodeDecodeMenu",           // 编码/解码浮动菜单
    "com.kggzs.cn.mt.QuickInsertFunction",        // 快速插入时间
    "com.kggzs.cn.mt.AIChatMenu",                 // AI 对话浮动菜单（新增）
    "com.kggzs.cn.mt.AICodeAnalysisToolMenu",     // AI 代码分析工具菜单
    "com.kggzs.cn.mt.AICodeAnalysisFloatingMenu"  // AI 快速分析浮动菜单
]
```

### 接口类型

- **浮动菜单** - 选中文本后显示的菜单项
- **工具菜单** - 编辑器顶部工具栏菜单项
- **MCP 服务** - MCP 协议配置和管理（集成在设置界面）

---

## 时间格式

### 支持的格式标记

| 标记 | 说明 | 示例 |
|------|------|------|
| yyyy | 4位年份 | 2026 |
| yy | 2位年份 | 26 |
| MM | 补0月份 | 05 |
| M | 月份 | 5 |
| N | 农历月份 | 四月 |
| dd | 补0日期 | 20 |
| d | 日期 | 20 |
| e | 农历日期 | 初四 |
| E | 星期 | 周三 |
| a | 时段 | 上午/下午 |
| aa | 精确时段 | 傍晚/凌晨 |
| HH | 24时补0 | 19 |
| H | 24时 | 19 |
| mm | 补0分钟 | 08 |
| ss | 补0秒数 | 55 |
| l | 时辰 | 酉 |

### 预设格式

**公历格式**:
- 标准中文：`yyyy年M月d日`
- ISO格式：`yyyy-MM-dd`
- 斜杠格式：`yyyy/M/d`
- 紧凑格式：`yyyyMd`
- 带星期：`yyyy年M月d日 E`

**农历格式**:
- 传统汉字：干支年 + 农历月日
- 农历简写：`农历Ne`
- 干支纪日：干支年 + 干支月 + 干支日
- 公农历并列：`yyyy-MM-dd（干支年Ne）`

---

## 开发指南

### 添加新功能

1. 在 `src/main/java/com/kggzs/cn/mt/` 创建新的 Java 类
2. 实现对应的 MT 插件接口
3. 在 `build.gradle` 的 `interfaces` 中注册
4. 在所有语言包中添加对应的键值

### 添加新语言

1. 复制 `strings.mtl` 为 `strings-xx.mtl`（xx 为语言代码）
2. 翻译所有键值
3. 在 README.md 中更新语言支持列表

### 修改 API 配置

1. 修改 `AIHelper.java` 中的默认值
2. 同步更新语言包中的 `api_url_hint`、`model_name_hint`、`api_key_hint`

---

## 常见问题

### Q: 构建失败？

检查：
1. Java 版本是否为 17
2. Gradle 版本是否为 8.13
3. Android SDK 是否已安装

### Q: 语言包键缺失？

运行语言包检查脚本，确保所有语言包的键与 `strings-zh-CN.mtl` 一致。

### Q: API 调用失败？

检查：
1. 网络连接是否正常
2. API 地址是否正确
3. API 密钥是否有效

---

## 更新日志

### v2.1.0 (当前版本)

- 🤖 新增 AI 对话功能，支持多轮对话和 MCP 工具调用
- 🔌 新增 MCP 服务配置，支持 MCP 协议
- 🔧 MCP Agent 模式，AI 自动调用外部工具
- 🔧 新增 AIChatMenu、MCPServiceMenu 接口
- 🔧 新增 AIChatHelper、MCPClient 工具类

### v2.0.3

- ✨ 时间插入新增三种显示模式
- ✨ 时间插入新增自定义格式功能
- 🔧 优化时间格式设置界面

### v2.0.2

- 🌐 支持10种语言
- 🔧 AI 分析支持后台运行模式
- 🔧 新增快速提示词功能
- 🔧 新增自定义 Skill 功能

---

## 联系方式

- **作者**: 康哥
- **网站**: [www.kggzs.cn](http://www.kggzs.cn)
- **GitHub**: [https://github.com/kggzs/MT_Plugin](https://github.com/kggzs/MT_Plugin)

---

<div align="center">

Made with ❤️ by 康哥

</div>
