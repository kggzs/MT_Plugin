package com.example.myplugin.util;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

/**
 * 农历计算工具类
 * 支持公历转农历、干支计算、生肖计算等功能
 */
public class LunarCalendar {

    // 农历月份名称
    private static final String[] LUNAR_MONTH_NAMES = {
        "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "腊月"
    };

    // 农历日期名称
    private static final String[] LUNAR_DAY_NAMES = {
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };

    // 天干
    private static final String[] TIAN_GAN = {
        "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"
    };

    // 地支
    private static final String[] DI_ZHI = {
        "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
    };

    // 生肖
    private static final String[] ZODIAC_ANIMALS = {
        "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"
    };

    // 星期名称
    private static final String[] WEEKDAY_NAMES = {
        "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"
    };

    // 农历数据表（1900-2100年）
    // 每个元素表示该年的农历信息，高16位表示闰月，低16位表示每月天数
    private static final long[] LUNAR_INFO = {
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,
        0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252,
        0x0d520
    };

    // 基准日期：1900年1月31日为农历1900年正月初一
    private static final int BASE_YEAR = 1900;
    private static final long BASE_DATE_MILLIS = -2206425600000L; // 1900-01-31

    private int lunarYear;
    private int lunarMonth;
    private int lunarDay;
    private boolean isLeapMonth;

    public LunarCalendar(Date date) {
        convertToLunar(date);
    }

    /**
     * 将公历日期转换为农历
     */
    private void convertToLunar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // 计算从基准日期到目标日期的天数
        Calendar baseCalendar = Calendar.getInstance();
        baseCalendar.set(1900, 0, 31, 0, 0, 0);

        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(year, month - 1, day, 0, 0, 0);

        long diffDays = (targetCalendar.getTimeInMillis() - baseCalendar.getTimeInMillis()) / (24 * 60 * 60 * 1000);

        // 计算农历日期
        int lunarYearIndex = 0;
        int daysInYear;

        while (diffDays >= 0) {
            daysInYear = getLunarYearDays(lunarYearIndex);
            if (diffDays < daysInYear) {
                break;
            }
            diffDays -= daysInYear;
            lunarYearIndex++;
        }

        this.lunarYear = BASE_YEAR + lunarYearIndex;

        // 计算月份
        int leapMonth = getLeapMonth(lunarYearIndex);
        int monthDays;
        lunarMonth = 1;
        isLeapMonth = false;

        while (diffDays >= 0) {
            if (leapMonth > 0 && lunarMonth == leapMonth + 1 && !isLeapMonth) {
                // 处理闰月
                isLeapMonth = true;
                lunarMonth--;
            }

            monthDays = getLunarMonthDays(lunarYearIndex, lunarMonth, isLeapMonth);

            if (diffDays < monthDays) {
                break;
            }

            diffDays -= monthDays;

            if (isLeapMonth) {
                isLeapMonth = false;
            } else {
                lunarMonth++;
            }
        }

        this.lunarDay = (int) diffDays + 1;
    }

    /**
     * 获取农历年的总天数
     */
    private int getLunarYearDays(int yearIndex) {
        int sum = 348;
        long info = LUNAR_INFO[yearIndex];
        for (int i = 0x8000; i > 0x8; i >>= 1) {
            sum += (info & i) != 0 ? 1 : 0;
        }
        return sum + getLeapDays(yearIndex);
    }

    /**
     * 获取闰月的天数
     */
    private int getLeapDays(int yearIndex) {
        if (getLeapMonth(yearIndex) > 0) {
            return (LUNAR_INFO[yearIndex] & 0x10000) != 0 ? 30 : 29;
        }
        return 0;
    }

    /**
     * 获取闰月的月份（1-12），0表示无闰月
     */
    private int getLeapMonth(int yearIndex) {
        return (int) (LUNAR_INFO[yearIndex] & 0xf);
    }

    /**
     * 获取农历月的天数
     */
    private int getLunarMonthDays(int yearIndex, int month, boolean isLeap) {
        if (isLeap) {
            return getLeapDays(yearIndex);
        }
        return (LUNAR_INFO[yearIndex] & (0x10000 >> month)) != 0 ? 30 : 29;
    }

    /**
     * 获取农历年份的干支纪年
     */
    @NonNull
    public String getGanZhiYear() {
        int offset = lunarYear - 1900 + 36; // 1900年是庚子年，offset=36
        return TIAN_GAN[offset % 10] + DI_ZHI[offset % 12];
    }

    /**
     * 获取农历月份的干支纪月
     */
    @NonNull
    public String getGanZhiMonth() {
        // 农历年份的天干
        int yearGanIndex = (lunarYear - 1900 + 36) % 10;
        // 正月干支根据年干确定
        int monthGanIndex = (yearGanIndex * 2 + lunarMonth + 1) % 10;
        int monthZhiIndex = (lunarMonth + 1) % 12;
        return TIAN_GAN[monthGanIndex] + DI_ZHI[monthZhiIndex];
    }

    /**
     * 获取农历日的干支纪日
     */
    @NonNull
    public String getGanZhiDay() {
        // 使用基准日期计算
        Calendar baseCalendar = Calendar.getInstance();
        baseCalendar.set(1900, 0, 31, 0, 0, 0);

        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(lunarYear, 0, 1, 0, 0, 0);

        // 计算从1900-01-31到当前日期的天数
        long diffDays = (targetCalendar.getTimeInMillis() - baseCalendar.getTimeInMillis()) / (24 * 60 * 60 * 1000);

        // 加上农历月日
        for (int i = 1; i < lunarMonth; i++) {
            diffDays += getLunarMonthDays(lunarYear - BASE_YEAR, i, false);
        }
        if (isLeapMonth) {
            diffDays += getLunarMonthDays(lunarYear - BASE_YEAR, lunarMonth, false);
        }
        diffDays += lunarDay - 1;

        int offset = (int) (diffDays % 60);
        return TIAN_GAN[offset % 10] + DI_ZHI[offset % 12];
    }

    /**
     * 获取生肖
     */
    @NonNull
    public String getZodiac() {
        int offset = (lunarYear - 1900 + 12) % 12;
        return ZODIAC_ANIMALS[offset];
    }

    /**
     * 获取农历年份（中文格式）
     */
    @NonNull
    public String getLunarYearString() {
        return getGanZhiYear() + "年";
    }

    /**
     * 获取农历月份（中文格式）
     */
    @NonNull
    public String getLunarMonthString() {
        String prefix = isLeapMonth ? "闰" : "";
        return prefix + LUNAR_MONTH_NAMES[lunarMonth - 1];
    }

    /**
     * 获取农历日期（中文格式）
     */
    @NonNull
    public String getLunarDayString() {
        return LUNAR_DAY_NAMES[lunarDay - 1];
    }

    /**
     * 获取完整农历日期字符串（传统汉字格式）
     * 例如：丙午年四月初四
     */
    @NonNull
    public String getFullLunarString() {
        return getGanZhiYear() + "年" + getLunarMonthString() + getLunarDayString();
    }

    /**
     * 获取纯农历简写（日常）
     * 例如：农历四月初四
     */
    @NonNull
    public String getSimpleLunarString() {
        return "农历" + getLunarMonthString() + getLunarDayString();
    }

    /**
     * 获取干支纪日格式（黄历/命理）
     * 例如：丙午年 癸巳月 甲午日
     */
    @NonNull
    public String getGanZhiFullString() {
        return getGanZhiYear() + "年 " + getGanZhiMonth() + "月 " + getGanZhiDay() + "日";
    }

    /**
     * 获取农历+阿拉伯数字格式（系统/表单）
     * 例如：农历2026年四月初四
     */
    @NonNull
    public String getLunarWithNumberYear() {
        return "农历" + lunarYear + "年" + getLunarMonthString() + getLunarDayString();
    }

    /**
     * 获取公历年份
     */
    public int getLunarYear() {
        return lunarYear;
    }

    /**
     * 获取公历月份
     */
    public int getLunarMonth() {
        return lunarMonth;
    }

    /**
     * 获取公历日期
     */
    public int getLunarDay() {
        return lunarDay;
    }

    /**
     * 是否为闰月
     */
    public boolean isLeapMonth() {
        return isLeapMonth;
    }

    /**
     * 获取星期名称
     */
    @NonNull
    public static String getWeekday(@NonNull Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return WEEKDAY_NAMES[calendar.get(Calendar.DAY_OF_WEEK) - 1];
    }

    /**
     * 获取标准中文格式日期
     * 例如：2026年5月20日
     */
    @NonNull
    public static String getStandardChineseDate(@NonNull Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return year + "年" + month + "月" + day + "日";
    }

    /**
     * 获取带星期的标准中文格式日期
     * 例如：2026年5月20日，星期三
     */
    @NonNull
    public static String getStandardChineseDateWithWeekday(@NonNull Date date) {
        return getStandardChineseDate(date) + "，" + getWeekday(date);
    }

    /**
     * 获取短横线格式（ISO）
     * 例如：2026-05-20
     */
    @NonNull
    public static String getIsoDate(@NonNull Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    /**
     * 获取斜杠格式（常用）
     * 例如：2026/5/20
     */
    @NonNull
    public static String getSlashDate(@NonNull Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return year + "/" + month + "/" + day;
    }

    /**
     * 获取无分隔紧凑格式
     * 例如：20260520
     */
    @NonNull
    public static String getCompactDate(@NonNull Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return String.format("%04d%02d%02d", year, month, day);
    }

    /**
     * 获取带星期完整格式
     * 例如：2026年5月20日 星期三
     */
    @NonNull
    public static String getFullDateWithWeekday(@NonNull Date date) {
        return getStandardChineseDate(date) + " " + getWeekday(date);
    }

    /**
     * 获取公农历并列格式（请柬/公文）
     * 例如：2026-05-20（丙午年四月初四）
     */
    @NonNull
    public String getCombinedDate(@NonNull Date date) {
        return getIsoDate(date) + "（" + getFullLunarString() + "）";
    }
}
