package com.kggzs.cn.mt.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Skill/QuickPrompt 管理工具类
 * 提供 Skill 和 QuickPrompt 的增删改查公共逻辑，消除 MyPreference 和 MCPServiceMenu 间的重复代码
 */
public class SkillManager {

    private SkillManager() {
    }

    /**
     * 从 JSONArray 中提取所有条目名称列表
     *
     * @param array JSONArray，每个元素为包含 "name" 字段的 JSONObject
     * @return 名称列表
     */
    public static ArrayList<String> extractNames(JSONArray array) {
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item != null) {
                String name = item.optString("name", "");
                if (!name.isEmpty()) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    /**
     * 创建或更新 Skill/QuickPrompt 条目
     *
     * @param array  目标 JSONArray
     * @param name   名称
     * @param prompt 内容
     * @param index  索引，-1 表示新增
     * @return 更新后的 JSONArray
     */
    public static JSONObject saveSkill(JSONArray array, String name, String prompt, int index) {
        JSONObject item = new JSONObject();
        try {
            item.put("name", name);
            item.put("prompt", prompt);
            if (index >= 0 && index < array.length()) {
                array.put(index, item);
            } else {
                array.put(item);
            }
        } catch (org.json.JSONException e) {
            // ignore
        }
        return item;
    }

    /**
     * 删除 Skill/QuickPrompt 条目
     *
     * @param array 目标 JSONArray
     * @param index 要删除的索引
     */
    public static void deleteSkill(JSONArray array, int index) {
        if (index >= 0 && index < array.length()) {
            array.remove(index);
        }
    }

    /**
     * 获取指定条目的名称
     *
     * @param array 目标 JSONArray
     * @param index 索引
     * @return 名称，不存在时返回空字符串
     */
    public static String getSkillName(JSONArray array, int index) {
        JSONObject item = array.optJSONObject(index);
        return item != null ? item.optString("name", "") : "";
    }

    /**
     * 获取指定条目的内容
     *
     * @param array 目标 JSONArray
     * @param index 索引
     * @return 内容，不存在时返回空字符串
     */
    public static String getSkillPrompt(JSONArray array, int index) {
        JSONObject item = array.optJSONObject(index);
        return item != null ? item.optString("prompt", "") : "";
    }
}