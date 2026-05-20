# MTKang Plugin 代码优化文档

> **项目名称**: MT 管理器 V3 AI 智能编程助手插件  
> **文档版本**: v2.0  
> **更新日期**: 2026-05-20  
> **技术栈**: Java 17 | Min SDK 21 | Gradle 8.13

---

## 目录

- [一、优化状态总览](#一优化状态总览)
- [二、已完成的优化](#二已完成的优化)
- [三、待优化项](#三待优化项)
- [四、代码规范检查清单](#四代码规范检查清单)

---

## 一、优化状态总览

| 优先级 | 已完成 | 待处理 | 说明 |
|--------|--------|--------|------|
| 🔴 严重 | 2 | 0 | 全部完成 |
| 🟡 中等 | 5 | 2 | 主要优化已完成 |
| 🟢 低 | 3 | 2 | 长期优化项 |

---

## 二、已完成的优化

### 2.1 ✅ API 配置本地化

**文件**: `src/main/java/com/example/myplugin/util/AIHelper.java`

**优化内容**:
- 移除云端密钥获取逻辑
- 改为本地默认配置
- 支持用户自定义 API 地址、模型、密钥
- 在设置界面提供可视化配置管理

**当前实现**:
```java
private static final String DEFAULT_API_URL = "https://api.kggzs.cn/v1";
private static final String DEFAULT_MODEL_NAME = "MT-v1";
private static final String DEFAULT_API_KEY = "sk-MT-kggzs-API-key";
```

---

### 2.2 ✅ AI 分析辅助类封装

**文件**: `src/main/java/com/example/myplugin/AICodeAnalysisHelper.java`

**优化内容**:
- 封装 AI 分析的公共逻辑
- 消除 `AICodeAnalysisToolMenu` 和 `AICodeAnalysisFloatingMenu` 的重复代码
- 统一提示词输入对话框
- 统一分析流程控制
- 统一结果显示

**收益**: 减少约 200 行重复代码，后续维护只需修改辅助类。

---

### 2.3 ✅ 多语言支持

**优化内容**:
- 支持10种语言界面
- 语言包文件完整
- 自动跟随系统语言切换

**支持的语言**:
- 中文（默认）、简体中文、繁体中文
- 日语、韩语、阿拉伯语
- 德语、西班牙语、法语、俄语

---

### 2.4 ✅ 快速提示词功能

**文件**: `AIHelper.java`, `AICodeAnalysisHelper.java`

**优化内容**:
- 支持配置最多10条快速提示词
- 在分析对话框中一键追加
- 支持添加、编辑、删除操作

---

### 2.5 ✅ 自定义 Skill 功能

**文件**: `AIHelper.java`, `AICodeAnalysisHelper.java`

**优化内容**:
- 支持添加自定义 Skill
- Skill 可追加到提示词末尾
- 支持多选 Skill

---

### 2.6 ✅ 后台运行模式

**文件**: `AICodeAnalysisHelper.java`

**优化内容**:
- AI 分析支持后台运行
- 完成后弹出结果通知
- 不阻塞当前界面操作

---

### 2.7 ✅ 时间格式多样化

**文件**: `TimeFormatHelper.java`, `LunarCalendar.java`

**优化内容**:
- 支持多种公历格式
- 支持多种农历格式
- 支持干支纪日
- 支持公农历并列显示

---

### 2.8 ✅ Markdown 清理

**文件**: `AICodeAnalysisHelper.java`

**优化内容**:
- 清理 Markdown 格式标记
- 提升纯文本阅读体验
- 保留内容结构

---

### 2.9 ✅ Handler 优化

**文件**: `AIHelper.java`

**优化内容**:
```java
private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

public static void runOnMainThread(@NonNull Runnable action) {
    MAIN_HANDLER.post(action);
}
```

**收益**: 避免频繁创建 Handler 对象，减少 GC 压力。

---

### 2.10 ✅ 赞助功能

**文件**: `MyPreference.java`

**优化内容**:
- 设置界面支持赞助二维码
- 微信收款码
- 支付宝收款码

---

## 三、待优化项

### 3.1 🟡 线程池替代直接创建线程

**当前状态**: 使用 `new Thread()` 创建线程

**优化方案**:
```java
private static final ExecutorService ANALYSIS_EXECUTOR = 
    Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("ai-analysis-thread");
        thread.setDaemon(true);
        return thread;
    });
```

**收益**:
- 控制并发数量
- 减少线程创建开销
- 更好的线程生命周期管理

---

### 3.2 🟡 升级 targetSdkVersion

**当前配置**: `targetSdk = 28` (Android 9.0)

**建议**: 升级到 `targetSdk = 33` 或更高

**注意事项**:
1. 需要适配 Android 10+ 的存储权限变更
2. 需要适配 Android 12+ 的通知权限
3. 充分测试所有功能

---

### 3.3 🟢 输入验证增强

**优化内容**:
- 为解码方法添加输入验证
- 验证十六进制格式
- 验证 Base64 格式
- 提供友好的错误提示

---

### 3.4 🟢 Lint 检查启用

**当前状态**: 完全禁用 Lint

**优化方案**:
```gradle
android {
    lintOptions {
        disable 'ExpiredTargetSdkVersion'
        abortOnError false
        warningsAsErrors false
    }
}
```

---

## 四、代码规范检查清单

### 4.1 命名规范 ✅
- [x] 类名使用 PascalCase
- [x] 方法名和变量名使用 camelCase
- [x] 常量使用 UPPER_SNAKE_CASE

### 4.2 代码质量 ✅
- [x] 无硬编码的敏感信息
- [x] 无重复代码（已封装辅助类）
- [x] 方法长度合理

### 4.3 国际化 ✅
- [x] 所有用户可见字符串使用 `{key}` 格式
- [x] 语言包键值对完整
- [x] 支持10种语言

### 4.4 资源管理 ✅
- [x] 使用 try-with-resources 管理资源
- [x] Handler 使用静态常量缓存

### 4.5 并发 🟡
- [ ] 使用线程池（待优化）
- [x] 主线程操作使用 `runOnMainThread()`

---

## 附录：版本更新记录

| 版本 | 日期 | 优化内容 |
|------|------|----------|
| v2.0.2 | 2026-05-20 | 多语言支持、后台运行、快速提示词、自定义 Skill |
| v2.0.1 | 2026-04-09 | AI 配置本地化、设置界面优化 |
| v2.0 | 2026-04-01 | 初始版本，编码/解码、AI 分析等功能 |

---

<div align="center">

Made with ❤️ by 康哥

</div>
