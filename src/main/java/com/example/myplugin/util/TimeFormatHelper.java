package com.example.myplugin.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

import bin.mt.plugin.api.PluginContext;

/**
 * 时间格式配置工具类
 * 管理时间插入格式的偏好设置
 */
public class TimeFormatHelper {

    // SharedPreferences 键名
    private static final String PREF_TIME_FORMAT = "time_format_type";

    // 时间格式类型常量
    public static final int FORMAT_STANDARD_CHINESE = 0;      // 标准中文格式：2026年5月20日
    public static final int FORMAT_ISO = 1;                   // 短横线格式（ISO）：2026-05-20
    public static final int FORMAT_SLASH = 2;                 // 斜杠格式（常用）：2026/5/20
    public static final int FORMAT_COMPACT = 3;               // 无分隔紧凑格式：20260520
    public static final int FORMAT_WITH_WEEKDAY = 4;          // 带星期完整格式：2026年5月20日 星期三
    public static final int FORMAT_LUNAR_TRADITIONAL = 5;     // 传统汉字格式（正式）：丙午年四月初四
    public static final int FORMAT_LUNAR_SIMPLE = 6;          // 纯农历简写（日常）：农历四月初四
    public static final int FORMAT_LUNAR_GANZHI = 7;          // 干支纪日格式（黄历/命理）：丙午年 癸巳月 甲午日
    public static final int FORMAT_LUNAR_WITH_NUMBER = 8;     // 农历+阿拉伯数字（系统/表单）：农历2026年四月初四
    public static final int FORMAT_COMBINED = 9;              // 公农历并列（请柬/公文）：2026-05-20（丙午年四月初四）

    // 格式名称数组（用于显示）
    public static final String[] FORMAT_NAMES = {
        "标准中文格式",
        "短横线格式（ISO）",
        "斜杠格式（常用）",
        "无分隔紧凑格式",
        "带星期完整格式",
        "传统汉字格式（农历）",
        "纯农历简写",
        "干支纪日格式",
        "农历+阿拉伯数字",
        "公农历并列格式"
    };

    // 格式示例数组
    public static final String[] FORMAT_EXAMPLES = {
        "2026年5月20日",
        "2026-05-20",
        "2026/5/20",
        "20260520",
        "2026年5月20日 星期三",
        "丙午年四月初四",
        "农历四月初四",
        "丙午年 癸巳月 甲午日",
        "农历2026年四月初四",
        "2026-05-20（丙午年四月初四）"
    };

    /**
     * 获取当前设置的时间格式类型
     *
     * @param context 插件上下文
     * @return 时间格式类型代码
     */
    public static int getTimeFormatType(@NonNull PluginContext context) {
        return context.getPreferences().getInt(PREF_TIME_FORMAT, FORMAT_STANDARD_CHINESE);
    }

    /**
     * 保存时间格式类型设置
     *
     * @param context 插件上下文
     * @param formatType 时间格式类型代码
     */
    public static void setTimeFormatType(@NonNull PluginContext context, int formatType) {
        context.getPreferences().edit().putInt(PREF_TIME_FORMAT, formatType).apply();
    }

    /**
     * 根据格式类型获取格式化后的时间字符串
     *
     * @param formatType 时间格式类型代码
     * @param date 日期对象
     * @return 格式化后的时间字符串
     */
    @NonNull
    public static String getFormattedTime(int formatType, @NonNull Date date) {
        LunarCalendar lunarCalendar = new LunarCalendar(date);

        switch (formatType) {
            case FORMAT_STANDARD_CHINESE:
                return LunarCalendar.getStandardChineseDate(date);

            case FORMAT_ISO:
                return LunarCalendar.getIsoDate(date);

            case FORMAT_SLASH:
                return LunarCalendar.getSlashDate(date);

            case FORMAT_COMPACT:
                return LunarCalendar.getCompactDate(date);

            case FORMAT_WITH_WEEKDAY:
                return LunarCalendar.getFullDateWithWeekday(date);

            case FORMAT_LUNAR_TRADITIONAL:
                return lunarCalendar.getFullLunarString();

            case FORMAT_LUNAR_SIMPLE:
                return lunarCalendar.getSimpleLunarString();

            case FORMAT_LUNAR_GANZHI:
                return lunarCalendar.getGanZhiFullString();

            case FORMAT_LUNAR_WITH_NUMBER:
                return lunarCalendar.getLunarWithNumberYear();

            case FORMAT_COMBINED:
                return lunarCalendar.getCombinedDate(date);

            default:
                return LunarCalendar.getStandardChineseDate(date);
        }
    }

    /**
     * 获取当前设置的时间格式名称
     *
     * @param context 插件上下文
     * @return 格式名称
     */
    @NonNull
    public static String getCurrentFormatName(@NonNull PluginContext context) {
        int type = getTimeFormatType(context);
        if (type >= 0 && type < FORMAT_NAMES.length) {
            return FORMAT_NAMES[type];
        }
        return FORMAT_NAMES[0];
    }

    /**
     * 获取格式类型的示例字符串
     *
     * @param formatType 时间格式类型代码
     * @return 示例字符串
     */
    @NonNull
    public static String getFormatExample(int formatType) {
        if (formatType >= 0 && formatType < FORMAT_EXAMPLES.length) {
            return FORMAT_EXAMPLES[formatType];
        }
        return FORMAT_EXAMPLES[0];
    }

    /**
     * 获取所有格式选项（用于选择对话框）
     *
     * @return 格式选项数组，每项包含名称和示例
     */
    @NonNull
    public static String[] getFormatOptions() {
        String[] options = new String[FORMAT_NAMES.length];
        for (int i = 0; i < FORMAT_NAMES.length; i++) {
            options[i] = FORMAT_NAMES[i] + " - " + FORMAT_EXAMPLES[i];
        }
        return options;
    }
}
