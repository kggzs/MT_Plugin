package com.kggzs.cn.mt.util;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

import bin.mt.plugin.api.PluginContext;

/**
 * 时间格式配置工具类
 * 管理时间插入格式的偏好设置
 */
public class TimeFormatHelper {

    // SharedPreferences 键名
    private static final String PREF_TIME_FORMAT = "time_format_type";
    private static final String PREF_TIME_MODE = "time_mode";
    private static final String PREF_CUSTOM_FORMAT_STRING = "custom_format_string";

    // 时间模式常量
    public static final int MODE_DATE_ONLY = 0;     // 只显示日期（不带时分秒）
    public static final int MODE_DATE_TIME = 1;     // 显示日期+时间（带时分秒）
    public static final int MODE_CUSTOM = 2;        // 自定义格式

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
    public static final int FORMAT_CUSTOM = 10;               // 自定义格式

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
        "公农历并列格式",
        "自定义格式"
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
        "2026-05-20（丙午年四月初四）",
        "自定义"
    };

    // 默认自定义格式
    public static final String DEFAULT_CUSTOM_FORMAT = "N月e E a H:mm";

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
     * 获取时间模式
     *
     * @param context 插件上下文
     * @return 时间模式（MODE_DATE_ONLY, MODE_DATE_TIME, MODE_CUSTOM）
     */
    public static int getTimeMode(@NonNull PluginContext context) {
        return context.getPreferences().getInt(PREF_TIME_MODE, MODE_DATE_TIME);
    }

    /**
     * 设置时间模式
     *
     * @param context 插件上下文
     * @param mode 时间模式
     */
    public static void setTimeMode(@NonNull PluginContext context, int mode) {
        context.getPreferences().edit().putInt(PREF_TIME_MODE, mode).apply();
    }

    /**
     * 是否启用了自定义格式
     *
     * @param context 插件上下文
     * @return 是否启用自定义格式
     */
    public static boolean isCustomFormatEnabled(@NonNull PluginContext context) {
        return getTimeMode(context) == MODE_CUSTOM;
    }

    /**
     * 获取自定义格式字符串
     *
     * @param context 插件上下文
     * @return 自定义格式字符串
     */
    @NonNull
    public static String getCustomFormatString(@NonNull PluginContext context) {
        return context.getPreferences().getString(PREF_CUSTOM_FORMAT_STRING, DEFAULT_CUSTOM_FORMAT);
    }

    /**
     * 保存自定义格式字符串
     *
     * @param context 插件上下文
     * @param formatString 自定义格式字符串
     */
    public static void setCustomFormatString(@NonNull PluginContext context, @NonNull String formatString) {
        context.getPreferences().edit().putString(PREF_CUSTOM_FORMAT_STRING, formatString).apply();
    }

    /**
     * 根据格式类型获取格式化后的时间字符串
     *
     * @param formatType 时间格式类型代码
     * @param date 日期对象
     * @param includeTime 是否包含时间（时分秒）
     * @return 格式化后的时间字符串
     */
    @NonNull
    public static String getFormattedTime(int formatType, @NonNull Date date, boolean includeTime) {
        LunarCalendar lunarCalendar = new LunarCalendar(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String timeStr = includeTime ? " " + String.format("%02d:%02d:%02d", 
            calendar.get(Calendar.HOUR_OF_DAY), 
            calendar.get(Calendar.MINUTE), 
            calendar.get(Calendar.SECOND)) : "";

        switch (formatType) {
            case FORMAT_STANDARD_CHINESE:
                return LunarCalendar.getStandardChineseDate(date) + timeStr;

            case FORMAT_ISO:
                return LunarCalendar.getIsoDate(date) + timeStr;

            case FORMAT_SLASH:
                return LunarCalendar.getSlashDate(date) + timeStr;

            case FORMAT_COMPACT:
                return LunarCalendar.getCompactDate(date) + timeStr;

            case FORMAT_WITH_WEEKDAY:
                return LunarCalendar.getFullDateWithWeekday(date) + timeStr;

            case FORMAT_LUNAR_TRADITIONAL:
                return lunarCalendar.getFullLunarString() + timeStr;

            case FORMAT_LUNAR_SIMPLE:
                return lunarCalendar.getSimpleLunarString() + timeStr;

            case FORMAT_LUNAR_GANZHI:
                return lunarCalendar.getGanZhiFullString() + timeStr;

            case FORMAT_LUNAR_WITH_NUMBER:
                return lunarCalendar.getLunarWithNumberYear() + timeStr;

            case FORMAT_COMBINED:
                return lunarCalendar.getCombinedDate(date) + timeStr;

            default:
                return LunarCalendar.getStandardChineseDate(date) + timeStr;
        }
    }

    /**
     * 根据格式类型获取格式化后的时间字符串（默认包含时间）
     *
     * @param formatType 时间格式类型代码
     * @param date 日期对象
     * @return 格式化后的时间字符串
     */
    @NonNull
    public static String getFormattedTime(int formatType, @NonNull Date date) {
        return getFormattedTime(formatType, date, true);
    }

    /**
     * 获取格式化后的时间字符串（根据当前设置）
     *
     * @param context 插件上下文
     * @param date 日期对象
     * @return 格式化后的时间字符串
     */
    @NonNull
    public static String getFormattedTime(@NonNull PluginContext context, @NonNull Date date) {
        int mode = getTimeMode(context);
        if (mode == MODE_CUSTOM) {
            String customFormat = getCustomFormatString(context);
            return parseCustomFormat(customFormat, date);
        }
        boolean includeTime = (mode == MODE_DATE_TIME);
        return getFormattedTime(getTimeFormatType(context), date, includeTime);
    }

    /**
     * 解析自定义格式字符串
     *
     * @param format 格式字符串
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    @NonNull
    public static String parseCustomFormat(@NonNull String format, @NonNull Date date) {
        LunarCalendar lunarCalendar = new LunarCalendar(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < format.length()) {
            char c = format.charAt(i);

            if (c == 'y' || c == 'M' || c == 'd' || c == 'N' || c == 'e' ||
                c == 'E' || c == 'a' || c == 'H' || c == 'h' || c == 'm' ||
                c == 's' || c == 'l') {
                // 统计连续相同字符的数量
                int count = 1;
                while (i + count < format.length() && format.charAt(i + count) == c) {
                    count++;
                }

                String replacement = getFormatReplacement(c, count, calendar, lunarCalendar);
                result.append(replacement);
                i += count;
            } else {
                // 普通字符直接添加
                result.append(c);
                i++;
            }
        }

        return result.toString();
    }

    /**
     * 获取格式标记对应的字符串
     */
    @NonNull
    private static String getFormatReplacement(char type, int count, Calendar calendar, LunarCalendar lunarCalendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour24 = calendar.get(Calendar.HOUR_OF_DAY);
        int hour12 = calendar.get(Calendar.HOUR);
        if (hour12 == 0) hour12 = 12;
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);

        switch (type) {
            case 'y':
                if (count >= 4) {
                    return String.valueOf(year);
                } else {
                    return String.valueOf(year).substring(2);
                }

            case 'M':
                if (count >= 2) {
                    return String.format("%02d", month);
                } else {
                    return String.valueOf(month);
                }

            case 'd':
                if (count >= 2) {
                    return String.format("%02d", day);
                } else {
                    return String.valueOf(day);
                }

            case 'N':
                return lunarCalendar.getLunarMonthString();

            case 'e':
                return lunarCalendar.getLunarDayString();

            case 'E':
                return getShortWeekday(weekday);

            case 'a':
                if (count >= 2) {
                    return getDetailedPeriod(hour24);
                } else {
                    return getPeriod(hour24);
                }

            case 'H':
                if (count >= 2) {
                    return String.format("%02d", hour24);
                } else {
                    return String.valueOf(hour24);
                }

            case 'h':
                if (count >= 2) {
                    return String.format("%02d", hour12);
                } else {
                    return String.valueOf(hour12);
                }

            case 'm':
                if (count >= 2) {
                    return String.format("%02d", minute);
                } else {
                    return String.valueOf(minute);
                }

            case 's':
                if (count >= 2) {
                    return String.format("%02d", second);
                } else {
                    return String.valueOf(second);
                }

            case 'l':
                return getShiChen(hour24);

            default:
                return String.valueOf(type);
        }
    }

    /**
     * 获取简写星期
     */
    @NonNull
    private static String getShortWeekday(int weekday) {
        String[] weekdays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        return weekdays[weekday - 1];
    }

    /**
     * 获取时段（上午/下午）
     */
    @NonNull
    private static String getPeriod(int hour24) {
        return hour24 < 12 ? "上午" : "下午";
    }

    /**
     * 获取详细时段
     */
    @NonNull
    private static String getDetailedPeriod(int hour24) {
        if (hour24 >= 23 || hour24 < 1) return "深夜";
        if (hour24 >= 1 && hour24 < 5) return "凌晨";
        if (hour24 >= 5 && hour24 < 7) return "清晨";
        if (hour24 >= 7 && hour24 < 9) return "早晨";
        if (hour24 >= 9 && hour24 < 11) return "上午";
        if (hour24 >= 11 && hour24 < 13) return "中午";
        if (hour24 >= 13 && hour24 < 17) return "下午";
        if (hour24 >= 17 && hour24 < 19) return "傍晚";
        return "晚上";
    }

    /**
     * 获取时辰
     */
    @NonNull
    private static String getShiChen(int hour24) {
        String[] shiChen = {
            "子", "子", "丑", "丑", "寅", "寅", "卯", "卯",
            "辰", "辰", "巳", "巳", "午", "午", "未", "未",
            "申", "申", "酉", "酉", "戌", "戌", "亥", "亥"
        };
        return shiChen[hour24];
    }

    /**
     * 获取当前设置的时间格式名称
     *
     * @param context 插件上下文
     * @return 格式名称
     */
    @NonNull
    public static String getCurrentFormatName(@NonNull PluginContext context) {
        if (isCustomFormatEnabled(context)) {
            return "自定义格式";
        }
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
