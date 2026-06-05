# MTKang Plugin 代码审计报告（第2版）

> **审计日期**: 2026-06-04  
> **审计版本**: v2.1.0 (versionCode: 5)  
> **审计范围**: 全量源代码、构建配置、资源文件、多语言包  
> **审计工具**: 静态代码分析 + 人工审查  
> **审计目的**: 对比第1版报告（2026-06-04 第1版），检查问题修复和代码优化情况

---

## 目录

1. [项目概况](#1-项目概况)
2. [架构评估](#2-架构评估)
3. [构建配置审计](#3-构建配置审计)
4. [Java 源码审计](#4-java-源码审计)
5. [安全审计](#5-安全审计)
6. [多语言资源审计](#6-多语言资源审计)
7. [代码质量与规范](#7-代码质量与规范)
8. [修复进展跟踪](#8-修复进展跟踪)
9. [仍存在的问题与建议](#9-仍存在的问题与建议)
10. [总结评分](#10-总结评分)

---

## 1. 项目概况

| 属性 | 值 |
|------|-----|
| 项目名称 | MTKang Plugin |
| 插件ID | `com.kggzs.cn.mt` |
| 版本号 | v2.1.0 (versionCode: 5) |
| 最低 SDK | API 21 (Android 5.0) |
| 目标 SDK | API 28 |
| 编译 SDK | API 36 |
| 开发语言 | Java 17 + Kotlin (AGP 配置) |
| 构建系统 | Gradle 8.13 + AGP 8.13.2 |
| 插件框架 | MT Plugin API (bin.mt.plugin) |

### 文件统计（当前）

| 文件类型 | 数量 | 总行数 |
|----------|------|--------|
| Java 源文件（一级包） | 11 | ~3,900 行 |
| Java 源文件（util 包） | 8 | ~3,600 行 |
| Gradle 配置 | 4 | ~60 行 |
| 语言包 (.mtl) | 10 | ~2,600 行 |
| 其他配置 | 3 | ~30 行 |

**相比第1版新增文件：** 新增 8 个 Java 文件（AIChatMenu.java、MCPServiceMenu.java、PreferenceApiDialog.java、PreferenceSkillDialog.java、PreferenceTimeFormatDialog.java、SkillManager.java、StreamParser.java、ThreadPoolManager.java）

---

## 2. 架构评估

### 2.1 整体架构（当前）

```
┌──────────────────────────────────────────────────────────────────┐
│                         MT Manager V3                             │
├──────────────────────────────────────────────────────────────────┤
│                         MT Plugin API                             │
├──────────┬──────────┬──────────┬──────────┬──────────┬───────────┤
│ 浮动菜单  │ 工具菜单  │ 设置界面  │ 对话框类  │ 工具类   │ 资源文件  │
├──────────┼──────────┼──────────┼──────────┼──────────┼───────────┤
│EncodeDec-│AIChatMenu│MyPrefer- │Preferenc-│AIHelper  │strings.*  │
│odeMenu   │AICodeAna-│ence      │eApiDialog│AIChatHelp│.mtl       │
│QuickInser│lysisTool-│          │Preferenc-│er        │icon.png   │
│tFunction │Menu      │          │eSkillDia-│MCPClient │wx.jpg     │
│AICodeAnal│MCPServic-│          │log       │SkillManag│zfb.jpg    │
│ysisFloati│eMenu     │          │Preferenc-│er        │           │
│ngMenu    │          │          │eTimeForm-│StreamPars│           │
│          │          │          │atDialog  │er        │           │
│          │          │          │          │ThreadPool│           │
│          │          │          │          │Manager   │           │
│          │          │          │          │TimeFormat│           │
│          │          │          │          │Helper    │           │
│          │          │          │          │LunarCale-│           │
│          │          │          │          │ndar      │           │
└──────────┴──────────┴──────────┴──────────┴──────────┴───────────┘
```

**评分: 8.5/10** — 相比第1版（8/10），架构质量提升，模块拆分更细，职责边界更清晰。

### 2.2 架构改进（相比第1版）

| 改进项 | 说明 |
|--------|------|
| **MyPreference 拆分** | 859 行 → 214 行，拆分为 `PreferenceApiDialog`、`PreferenceSkillDialog`、`PreferenceTimeFormatDialog` 三个独立类 |
| **线程池统一管理** | 新增 `ThreadPoolManager`，替代各处散落的 `new Thread` |
| **流式解析独立** | 新增 `StreamParser`，将思考标签分离/清理逻辑从 `AIHelper` 中拆出 |
| **Skill CRUD 统一** | 新增 `SkillManager` 工具类，消除数据层重复代码 |
| **新增 AIChatMenu** | AI 聊天功能独立为浮动菜单，`AIChatHelper` 分离对话管理逻辑 |

### 2.3 仍存在的架构问题

- **`AIHelper.java` 仍过大（1814 行）**：虽然已拆出 `StreamParser`、`ThreadPoolManager`、`SkillManager`，但自身仍集成了配置读写、请求构建、SSE 流式处理、StreamChunkParser 内部类等多重职责
- **无依赖注入**：类之间通过静态方法直接耦合，不利于单元测试
- **无接口抽象层**：`AIHelper` 和 `MCPClient` 没有定义接口

---

## 3. 构建配置审计

### 3.1 Gradle 配置

| 配置项 | 当前值 | 评价 |
|--------|--------|------|
| AGP 版本 | 8.13.2 | ✅ 最新稳定版 |
| Kotlin 版本 | 2.3.0 | ✅ 最新 |
| MT Plugin 版本 | 1.0.0-beta6 | ⚠️ Beta 版 |
| Desugar JDK | 2.1.5 | ✅ |
| Java 版本 | 17 | ✅ LTS |
| 编译 SDK | 36 | ✅ 最新 |
| 目标 SDK | 28 | ⚠️ 较旧 |
| 最小 SDK | 21 | ✅ |
| Lint 检查 | 已禁用 | ⚠️ 见下方 |
| **MCPServiceMenu 未注册** | interfaces 列表中缺失 | ⚠️ 注册了 5 个接口，`MCPServiceMenu` 文件存在但未注册到 `build.gradle` |

### 3.2 已注册接口

| 接口 | 类名 |
|------|------|
| 1 | `EncodeDecodeMenu` |
| 2 | `QuickInsertFunction` |
| 3 | `AIChatMenu` |
| 4 | `AICodeAnalysisToolMenu` |
| 5 | `AICodeAnalysisFloatingMenu` |

### 3.3 构建配置问题

1. **`MCPServiceMenu` 未注册到 `interfaces` 列表** — 文件存在于源码目录，但 `build.gradle` 中未包含该接口，因此不会被 MT 管理器加载
2. **`BUILD.md` 与当前版本脱节** — `versionCode` 仍写为 4（实际为 5），`versionName` 仍写为 v2.0.2（实际为 v2.1.0），`interfaces` 列表缺少 `AIChatMenu`，项目结构未反映新增文件
3. **Lint 完全禁用** — 所有 lint 任务被强制禁用

---

## 4. Java 源码审计

### 4.1 `AIHelper.java` — AI 核心工具类

**代码行数**: 1814 行（第1版: 2072 行，减少 258 行）  
**质量评分**: 6/10（第1版: 5/10）

**改进（相比第1版）**:

| 问题 | 第1版 | 当前状态 |
|------|-------|---------|
| 文件行数 | 2072 行 | 1814 行，仍偏大 |
| `analyzeCodeWithAI` 方法 | 340+ 行 | ~142 行，已拆分 |
| 思考标签分离逻辑 | 在 AIHelper 内 | 已拆至 `StreamParser.java` |
| 线程管理 | `new Thread` 直接创建 | 已拆至 `ThreadPoolManager.java` |
| Skill 管理 | 直接硬编码 | 已拆至 `SkillManager.java` |
| 流式解析内部类 | 一个内部类 | 结构未变 |

**仍未解决的问题**:

| 行号 | 问题 | 严重性 | 说明 |
|------|------|--------|------|
| 30 | `DEFAULT_API_KEY` 硬编码 | 🔴 严重 | `sk-MT-kggzs-API-key-1724464998` 仍硬编码在源码中 |
| 29 | `DEFAULT_AI_MODEL` 硬编码 | ⚠️ 中等 | `MT-v2` 与语言包默认值 `deepseek-v3.2` 不一致 |
| 31-32 | 默认提示词硬编码 | ⚠️ 中等 | 中文硬编码，无法国际化 |
| — | 文件仍达 1814 行 | 🔴 严重 | 仍集成了配置、HTTP 请求、SSE 解析等多重职责 |
| 1756-1813 | `analyzeCodeWithAINoUI` 重复 | ⚠️ 高 | 与 `analyzeCodeWithAI` 的 SSE 读取逻辑重复约 40-50 行 |

### 4.2 `MyPreference.java` — 设置界面

**代码行数**: 214 行（第1版: 859 行，减少 645 行）  
**质量评分**: 9/10（第1版: 6/10）

**改进（相比第1版）**:

| 问题 | 第1版 | 当前状态 |
|------|-------|---------|
| 文件行数 | 859 行 | 214 行 ✅ |
| API 配置 | 硬编码在 MyPreference | 独立为 `PreferenceApiDialog.java` ✅ |
| Skill 管理 | 硬编码在 MyPreference | 独立为 `PreferenceSkillDialog.java` ✅ |
| 时间格式配置 | 硬编码在 MyPreference | 独立为 `PreferenceTimeFormatDialog.java` ✅ |
| `showMultilineInputDialog` | 未使用 | 已移除 ✅ |
| 时间格式 UI 循环 | 手动创建 | 已委托给独立类 ✅ |

### 4.3 `MCPServiceMenu.java` — MCP 服务配置

**代码行数**: 362 行（第1版: 373 行）  
**质量评分**: 8/10（第1版: 8/10）

**改进**:
- Skill CRUD 数据操作已通过 `SkillManager` 与 `PreferenceSkillDialog` 共享，消除数据层重复
- MCP URL 校验完善（非空 + 协议前缀检查）

**仍存在的问题**:
- 与 `PreferenceSkillDialog.java` 的 UI 层 Skill 编辑器代码仍有相似重复（约 30-40 行布局代码类似），但功能有差异（后者有"应用到提示词"按钮），属于可接受的合理重复

### 4.4 `EncodeDecodeMenu.java` — 编码/解码工具

**代码行数**: 532 行（第1版: 556 行）  
**质量评分**: 7.5/10（第1版: 7/10）

**改进**:

| 问题 | 第1版 | 当前状态 |
|------|-------|---------|
| ROT13 编解码重复 | 两个完全重复的方法 | 已合并为统一 `handleRot13` ✅ |

**仍未解决的问题**:

| 行号 | 问题 | 严重性 | 建议 |
|------|------|--------|------|
| 230-232 | `copy_success` 冗余包装方法 | ⚠️ 低 | 参数 `hash` 未使用，完全可内联 |
| 469-470 | `isTimestamp` 正则缺少范围校验 | ⚠️ 中等 | 仅匹配 10-13 位数字，无合理范围限制 |
| 504-506 | catch 块中 `continue` 多余 | ⚠️ 低 | 循环末尾的 `continue` 是死代码 |
| 511-515 | `SimpleDateFormat` 重复创建 | ⚠️ 低 | 每次调用 `formatTimestamp` 都新建实例 |

### 4.5 `AICodeAnalysisHelper.java` — AI 分析辅助

**代码行数**: 497 行（第1版: 496 行）  
**质量评分**: 7/10（第1版: 7/10）

**仍存在的问题**:

| 行号 | 问题 | 严重性 | 建议 |
|------|------|--------|------|
| 52-63, 68-77 | QuickPrompts 和 Skills 加载逻辑高度重复 | ⚠️ 中等 | 应抽取为通用 `loadJsonArray` 方法 |
| 352-355 | `startBackgroundAnalysis` 有未使用参数 | ⚠️ 低 | `thinkingEdit` 和 `resultEdit` 传递了但未使用 |

### 4.6 `AIChatMenu.java` — AI 对话

**代码行数**: 229 行（第1版: 228 行）  
**质量评分**: 8.5/10（第1版: 8/10）

**改进**:

| 问题 | 第1版 | 当前状态 |
|------|-------|---------|
| `new Thread` 直接创建 | 第128行 | ✅ 已改用 `ThreadPoolManager.execute()` |

**仍存在的问题**:

| 行号 | 问题 | 严重性 | 建议 |
|------|------|--------|------|
| 88 | `boolean[] isSending` 数组 hack | ⚠️ 低 | 建议改用 `AtomicBoolean` |
| 193 | `stopActiveRequest` 捕获通用 `Exception` | ⚠️ 低 | 建议仅捕获 `IOException` |

### 4.7 `AIChatHelper.java` — AI 对话辅助

**代码行数**: 445 行（第1版: 445 行）  
**质量评分**: 7.5/10（第1版: 7/10）

**改进**:

| 问题 | 第1版 | 当前状态 |
|------|-------|---------|
| AI 返回 null 时未调用 onError | 直接 return | ✅ 已调用 `onError.onError("AI 返回空响应")` |
| 工具名称为空时静默返回 null | 静默返回 | ✅ 已添加 `Log.w` 警告日志 |

**仍存在的问题**:

| 行号 | 问题 | 严重性 | 建议 |
|------|------|--------|------|
| 165-166 | Agent 状态消息拼接在内容中 | ⚠️ 低 | 状态消息与 AI 回复内容混合 |
| 407 | `getChatHistory` 从索引 1 开始 | ⚠️ 低 | 魔法数字 1，应使用常量表示跳过系统消息 |

### 4.8 `MCPClient.java` — MCP 客户端

**代码行数**: 277 行  
**质量评分**: 9/10（第1版: 9/10）

**亮点**:
- JSON-RPC 2.0 协议实现完整
- 请求 ID 使用 UUID 生成
- 超时、错误处理、资源释放完善

**仍存在的问题**:
- `clientId` 字段定义了但未使用
- 错误流读取捕获通用 `Exception`

### 4.9 `TimeFormatHelper.java` + `LunarCalendar.java`

**质量评分**: 8/10（未变化）

**改进**: 无显著变化（这两个文件质量本身较好）

**仍存在的问题**:
- `FORMAT_EXAMPLES` 硬编码示例年份
- `getFormattedTime` 五处重载
- `getGanZhiDay` 干支日计算基于公历日期

### 4.10 新增文件概览

| 文件 | 行数 | 职责 | 质量评分 |
|------|------|------|---------|
| `PreferenceApiDialog.java` | ~60 | API 配置对话框 | 9/10 |
| `PreferenceSkillDialog.java` | ~230 | AI 能力配置（提示词/Skill/QuickPrompt） | 7/10 |
| `PreferenceTimeFormatDialog.java` | ~170 | 时间格式配置 | 8/10 |
| `SkillManager.java` | 97 | Skill/QuickPrompt CRUD 工具类 | 9/10 |
| `StreamParser.java` | ~85 | 流式响应解析（思考标签分离/清理） | 9/10 |
| `ThreadPoolManager.java` | ~40 | 线程池管理 | 9/10 |

---

## 5. 安全审计

### 5.1 API 密钥安全

| 问题 | 严重性 | 第1版状态 | 当前状态 |
|------|--------|-----------|---------|
| 默认 API Key 硬编码 | 🔴 **严重** | 存在 | ❌ **仍未移除** (`AIHelper.java:30`) |
| API Key 明文存储 | ⚠️ 中等 | SharedPreferences 明文 | ❌ 仍未加密 |
| API Key UI 屏蔽 | ✅ 良好 | 有遮蔽 | ✅ 保持 |

### 5.2 网络通信安全

| 问题 | 严重性 | 当前状态 |
|------|--------|---------|
| HTTP 明文连接风险 | ⚠️ 中等 | 仍存在 |
| 无证书校验 | ⚠️ 中等 | 仍存在 |
| MCP 默认地址 HTTP | ⚠️ 中等 | 仍为 `http://127.0.0.1:8787/mcp` |

### 5.3 整体安全评分

**安全评分: 6/10** — 与第1版持平，核心安全问题 `DEFAULT_API_KEY` 硬编码仍未解决。

---

## 6. 多语言资源审计

### 6.1 支持语言

| 语言 | 文件 | 完成度 |
|------|------|--------|
| 英文 | `strings.mtl` | ✅ 100% |
| 简体中文 | `strings-zh-CN.mtl` | ✅ 100% |
| 繁体中文 | `strings-zh-TW.mtl` | ✅ 100% |
| 日语 | `strings-ja.mtl` | ✅ 100% |
| 韩语 | `strings-ko.mtl` | ✅ 100% |
| 阿拉伯语 | `strings-ar.mtl` | ✅ 100% |
| 德语 | `strings-de.mtl` | ✅ 100% |
| 西班牙语 | `strings-es.mtl` | ✅ 100% |
| 法语 | `strings-fr.mtl` | ✅ 100% |
| 俄语 | `strings-ru.mtl` | ✅ 100% |

### 6.2 硬编码字符串问题

| 位置 | 硬编码内容 | 严重性 |
|------|-----------|--------|
| `AICodeAnalysisFloatingMenu.java:17` | `"请简要分析以下代码"` | ⚠️ 中等 |
| `AICodeAnalysisToolMenu.java:17` | `"请分析以下代码"` | ⚠️ 中等 |

这两个硬编码字符串应改为通过语言包键引用。

---

## 7. 代码质量与规范

### 7.1 代码规范遵循情况

| 规范 | 评价 |
|------|------|
| 减少 else 依赖 | ✅ 良好 |
| 嵌套 ≤ 3 层 | ⚠️ 部分方法仍有深层嵌套 |
| Javadoc 注释 | ✅ 良好，新增文件均有注释 |
| 异常处理 | ⚠️ 多处仍捕获通用 `Exception` |
| 线程安全 | ✅ 改进：统一使用 `ThreadPoolManager` |

### 7.2 代码重复情况

| 重复区域 | 状态 |
|----------|------|
| ROT13 编解码 | ✅ 已修复，合并为统一方法 |
| Skill 管理 CRUD（数据层） | ✅ 已通过 `SkillManager` 消除 |
| Skill 管理 CRUD（UI 层） | ⚠️ MCPServiceMenu 与 PreferenceSkillDialog 仍有部分相似 UI 代码 |
| API 调用逻辑（analyzeCodeWithAI vs analyzeCodeWithAINoUI） | ⚠️ 仍有 ~40 行重复的 SSE 读取逻辑 |
| QuickPrompts vs Skills 加载（AICodeAnalysisHelper） | ⚠️ 两段代码高度重复，未提取公共方法 |

### 7.3 圈复杂度热点

| 方法 | 文件 | 行数 | 复杂度评估 |
|------|------|------|-----------|
| `sendMessage`（含 Agent 多轮循环） | `AIChatHelper.java` | ~80 行 | 高 |
| `showAiCapabilityDialog` | `PreferenceSkillDialog.java` | ~200 行 | 高 |
| `analyzeCodeWithAI` | `AIHelper.java` | ~142 行 | 中高 |
| `analyzeCodeWithAINoUI` | `AIHelper.java` | ~58 行 | 中 |

---

## 8. 修复进展跟踪

### 8.1 第1版严重问题修复情况

| ID | 问题 | 严重性 | 修复状态 | 说明 |
|----|------|--------|---------|------|
| C-01 | AIHelper 2072 行 | 🔴 严重 | ⚠️ **部分修复** | 降至 1814 行，拆出 3 个工具类，但仍偏大 |
| C-02 | 默认 API Key 硬编码 | 🔴 严重 | ❌ **未修复** | 仍存在于 AIHelper.java:30 |
| C-03 | 代码重复严重 | 🔴 严重 | ⚠️ **部分修复** | ROT13 已修复，Skill CRUD 数据层已修复；UI 层和 API 调用仍有重复 |
| C-04 | MyPreference 859 行 | 🔴 严重 | ✅ **已修复** | 降至 214 行，拆分为 3 个独立对话框类 |

### 8.2 第1版重要问题修复情况

| ID | 问题 | 修复状态 | 说明 |
|----|------|---------|------|
| C-05 | 硬编码 UI 字符串 | ❌ **未修复** | 两处 `AICodeAnalysis` 类仍有中文硬编码 |
| C-06 | `new Thread` 直接创建 | ✅ **已修复** | 改用 `ThreadPoolManager` |
| C-07 | 方法参数过多 | ⚠️ **部分改善** | `analyzeCodeWithAI` 已拆分 |
| C-08 | `analyzeCodeWithAINoUI` 重复 | ❌ **未修复** | SSE 读取逻辑仍有重复 |
| C-09 | 混淆规则 `-dontoptimize` | ✅ 保留（审核要求） | — |

### 8.3 第1版优化建议跟进

| ID | 建议 | 状态 |
|----|------|------|
| O-01 | 引入 EncryptedSharedPreferences | ❌ 未实施 |
| O-02 | Lint 选择性检查 | ❌ 仍完全禁用 |
| O-03 | 统一错误处理策略 | ❌ 未实施 |
| O-04 | 添加单元测试 | ❌ 未添加 |
| O-05 | 引入日志框架 | ❌ 仍使用 `android.util.Log` |
| O-06 | 更新 BUILD.md | ❌ **未更新**，与 v2.1.0 脱节 |

---

## 9. 仍存在的问题与建议

### 9.1 严重问题（建议立即处理）

| ID | 问题 | 位置 | 建议 |
|----|------|------|------|
| C-01v2 | AIHelper 仍达 1814 行 | `AIHelper.java` | 进一步拆分：将 HTTP 请求构建 (`buildRequest`/`sendStreamRequest`) 拆为 `AIHttpClient.java`，将配置读写方法拆为 `AIPreferenceManager.java` |
| C-02v2 | 默认 API Key 硬编码 | `AIHelper.java:30` | 移除默认 Key，首次使用时引导用户自行配置 |
| C-08v2 | MCPServiceMenu 未注册 | `build.gradle` | 将 `MCPServiceMenu` 添加到 `interfaces` 列表 |
| C-09v2 | BUILD.md 与版本脱节 | `BUILD.md` | 更新 versionCode/versionName/interfaces/项目结构 |

### 9.2 重要问题（建议下个版本修复）

| ID | 问题 | 位置 | 建议 |
|----|------|------|------|
| C-03v2 | 硬编码 UI 字符串 | `AICodeAnalysisFloatingMenu.java:17`、`AICodeAnalysisToolMenu.java:17` | 使用 `{key}` 引用语言包 |
| C-04v2 | analyzeCodeWithAINoUI 与 analyzeCodeWithAI 重复 | `AIHelper.java` | 抽取公共 SSE 读取方法，两者共用同一解析逻辑 |
| C-05v2 | AICodeAnalysisHelper 中 QuickPrompts/Skills 加载重复 | `AICodeAnalysisHelper.java:52-77` | 抽取为 `loadJsonArray` 通用方法 |
| C-06v2 | `startBackgroundAnalysis` 未用参数 | `AICodeAnalysisHelper.java:352-355` | 移除 `thinkingEdit` 和 `resultEdit` 参数 |

### 9.3 优化建议

| ID | 建议 | 说明 |
|----|------|------|
| O-01v2 | 引入 EncryptedSharedPreferences | 保护 API Key 存储安全 |
| O-02v2 | `isTimestamp` 增加范围校验 | 加合理的时间戳范围判断 |
| O-03v2 | 移除 `copy_success` 冗余方法 | 直接内联 `getString` 调用 |
| O-04v2 | `SimpleDateFormat` 提取为常量 | 避免每次调用新建实例 |
| O-05v2 | `continue` 死代码移除 | EncodeDecodeMenu 第 505 行 |
| O-06v2 | 统一 Skill 编辑器 UI | 考虑将 UI 层也抽取为公共组件 |
| O-07v2 | 更新 BUILD.md | 同步最新版本和结构 |

---

## 10. 总结评分

### 综合评分: **7.6/10**（第1版: 7.3/10，↑+0.3）

| 维度 | 第1版评分 | 当前评分 | 变化 | 说明 |
|------|----------|---------|------|------|
| **架构设计** | 8/10 | 8.5/10 | ↑ +0.5 | MyPreference 拆分、新增工具类职责分明 |
| **代码质量** | 6/10 | 7/10 | ↑ +1.0 | ROT13 修复、重复代码减少、线程管理改进 |
| **安全性** | 6/10 | 6/10 | — | 核心问题 DEFAULT_API_KEY 未移除 |
| **多语言支持** | 9/10 | 8.5/10 | ↓ -0.5 | 新发现 2 处硬编码中文 |
| **功能完整度** | 9/10 | 9/10 | — | 新增 AI 聊天、MCP 服务配置 |
| **构建配置** | 8/10 | 7/10 | ↓ -1.0 | MCPServiceMenu 未注册、BUILD.md 未更新 |
| **文档** | 7/10 | 6/10 | ↓ -1.0 | BUILD.md 与代码脱节 |
| **可维护性** | 5/10 | 7/10 | ↑ +2.0 | 大文件拆分、职责分离明显改善 |

### 主要改进（相比第1版）

1. **MyPreference 成功拆分** — 859 行 → 214 行，拆出 3 个独立对话框类
2. **线程管理规范化** — 统一使用 `ThreadPoolManager` 线程池
3. **数据层重复消除** — `SkillManager` 统一 Skill/QuickPrompt 的 CRUD 操作
4. **流式解析独立** — `StreamParser` 分离思考标签处理逻辑
5. **新增功能架构清晰** — AIChatMenu、MCPServiceMenu 等新功能模块结构良好

### 主要遗留风险

1. **API Key 硬编码** — 仍未移除（第1版已指出）
2. **AIHelper 仍偏大（1814 行）** — 虽有好转但仍需继续拆分
3. **BUILD.md 与代码脱节** — 版本号、接口列表、项目结构均未同步更新
4. **MCPServiceMenu 未注册** — 代码存在但不会被 MT 管理器加载

---

## 附录

### A. 项目文件结构（当前）

```
mt-kang/
├── src/main/java/com/kggzs/cn/mt/
│   ├── AIChatMenu.java                    # AI 对话浮动菜单（新增）
│   ├── AICodeAnalysisFloatingMenu.java    # AI 快速分析浮动菜单
│   ├── AICodeAnalysisHelper.java          # AI 分析辅助类
│   ├── AICodeAnalysisToolMenu.java        # AI 代码分析工具菜单
│   ├── EncodeDecodeMenu.java              # 编码/解码浮动菜单
│   ├── MCPServiceMenu.java                # MCP 服务配置（新增）
│   ├── MyPreference.java                  # 插件设置界面
│   ├── PreferenceApiDialog.java           # API 配置对话框（新增）
│   ├── PreferenceSkillDialog.java         # AI 能力配置对话框（新增）
│   ├── PreferenceTimeFormatDialog.java    # 时间格式配置对话框（新增）
│   ├── QuickInsertFunction.java           # 快速插入时间功能
│   └── util/
│       ├── AIHelper.java                  # AI 核心工具类
│       ├── AIChatHelper.java              # AI 对话辅助（新增）
│       ├── LunarCalendar.java             # 农历计算
│       ├── MCPClient.java                 # MCP 客户端（新增）
│       ├── SkillManager.java              # Skill 管理工具类（新增）
│       ├── StreamParser.java              # 流式解析工具类（新增）
│       ├── ThreadPoolManager.java         # 线程池管理（新增）
│       └── TimeFormatHelper.java          # 时间格式工具
├── assets/
│   ├── strings.mtl                        # 英文语言包
│   ├── strings-zh-CN.mtl                  # 简体中文
│   ├── strings-zh-TW.mtl                  # 繁体中文
│   ├── strings-ja.mtl                     # 日语
│   ├── strings-ko.mtl                     # 韩语
│   ├── strings-ar.mtl                     # 阿拉伯语
│   ├── strings-de.mtl                     # 德语
│   ├── strings-es.mtl                     # 西班牙语
│   ├── strings-fr.mtl                     # 法语
│   ├── strings-ru.mtl                     # 俄语
│   ├── wx.jpg                             # 微信收款码
│   └── zfb.jpg                            # 支付宝收款码
├── resources/icon.png                     # 插件图标
├── docs/code-audit-report.md              # 本文件
├── build.gradle                           # 构建配置
├── BUILD.md                               # 编译说明
├── README.md                              # 项目说明
├── CLAUDE.md                              # AI 助手指南
├── settings.gradle                        # Gradle 设置
├── gradle.properties                      # Gradle 属性
├── proguard-rules.pro                     # 混淆规则
└── gradle/libs.versions.toml              # 版本目录
```

### B. 默认配置速查

| 配置项 | 默认值 | 代码位置 |
|--------|--------|----------|
| API 地址 | `https://api.kggzs.cn/v1` | `AIHelper.java:28` |
| AI 模型 | `MT-v2` | `AIHelper.java:29` |
| API Key | `sk-MT-kggzs-API-key-1724464998` | `AIHelper.java:30` |
| MCP 地址 | `http://127.0.0.1:8787/mcp` | `AIHelper.java:220` |
| 时间格式 | 标准中文带时间 | `TimeFormatHelper.java` |
| 全局提示词 | 深度安全分析提示词 | `AIHelper.java:31` |
| 简短提示词 | 简要分析提示词 | `AIHelper.java:32` |

### C. 修复行动项优先级

| 优先级 | 行动项 | 预估工作量 |
|--------|--------|-----------|
| P0 | 将 `MCPServiceMenu` 注册到 `build.gradle` | 5 分钟 |
| P0 | 移除 `DEFAULT_API_KEY` 硬编码 | 10 分钟 |
| P1 | 更新 `BUILD.md` 与 v2.1.0 同步 | 15 分钟 |
| P1 | 修复硬编码中文字符串 | 10 分钟 |
| P2 | 进一步拆分 `AIHelper.java` | 2-3 小时 |
| P2 | 抽取公共 SSE 读取方法 | 1 小时 |
| P2 | 抽取 `loadJsonArray` 公共方法 | 30 分钟 |
| P3 | 优化 EncodeDecodeMenu 小问题 | 30 分钟 |
| P3 | 统一 Skill 编辑器 UI | 1-2 小时 |

---

*审计完成时间: 2026-06-04 20:30 CST*  
*审计工具: 静态代码分析 + 人工审查*  
*审计版本: 第2版（与第1版对比审查）*