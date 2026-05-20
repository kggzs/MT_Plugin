# background_run 键缺失修复报告

> **修复日期**: 2026-05-20  
> **项目版本**: v2.0.2

---

## 一、问题描述

在 AI 分析功能中添加了「后台运行」选项，但所有语言包缺少对应的 `background_run` 键，导致界面显示异常。

---

## 二、修复内容

### 2.1 新增键

在所有语言包中添加 `background_run` 键：

| 语言包 | 语言 | 翻译 |
|--------|------|------|
| strings.mtl | 中文（默认） | 后台运行 |
| strings-zh-CN.mtl | 简体中文 | 后台运行 |
| strings-zh-TW.mtl | 繁体中文 | 背景執行 |
| strings-ja.mtl | 日语 | バックグラウンド実行 |
| strings-ko.mtl | 韩语 | 백그라운드 실행 |
| strings-ar.mtl | 阿拉伯语 | تشغيل في الخلفية |
| strings-de.mtl | 德语 | Im Hintergrund ausführen |
| strings-es.mtl | 西班牙语 | Ejecutar en segundo plano |
| strings-fr.mtl | 法语 | Exécuter en arrière-plan |
| strings-ru.mtl | 俄语 | Выполнить в фоновом режиме |

### 2.2 使用位置

**文件**: `AICodeAnalysisHelper.java`

```java
// 创建后台运行复选框
PluginCheckBox backgroundCheckBox = builder.addCheckBox("background_run")
    .text("{background_run}")
    .checked(false);
```

---

## 三、验证结果

| 检查项 | 结果 |
|--------|------|
| 所有语言包包含该键 | ✅ 通过 |
| 界面显示正确 | ✅ 通过 |
| 功能正常工作 | ✅ 通过 |

---

## 四、影响范围

- AI 代码分析功能
- AI 快速分析功能
- 设置界面

---

<div align="center">

Made with ❤️ by 康哥

</div>
