package com.kggzs.cn.mt.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 流式响应解析工具类
 * 负责从 AI 流式响应中分离思考过程和正式内容，以及清理思考标签
 */
public class StreamParser {

    /**
     * 检测内容是否包含思考标签
     *
     * @param content 待检测的文本内容
     * @return 是否包含思考标签
     */
    public static boolean containsThinkingTag(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        return content.contains(" thinking") ||
               content.contains("<thinking>") ||
               content.contains("<reasoning>") ||
               content.contains("[思考]") ||
               content.contains("[Thinking]") ||
               content.contains("**思考过程**");
    }

    /**
     * 从内容中分离思考过程和正式内容
     * 支持多种思考标签格式： thinking, <thinking>, <reasoning>, [思考], **思考过程** 等
     *
     * @param content 完整内容
     * @return 数组：[0]=思考过程, [1]=正式内容, [2]=是否找到结束标签("true"/"false")
     */
    public static String[] separateThinkingFromContent(String content) {
        if (content == null || content.isEmpty()) {
            return new String[]{"", "", "false"};
        }

        String thinking = "";
        String finalContent = "";
        boolean foundEndTag = false;

        // 定义思考标签的正则模式
        String[][] thinkingPatterns = {
            {" thinking", " response"},
            {"<thinking>", "</thinking>"},
            {"<reasoning>", "</reasoning>"},
            {"\\[思考\\]", "\\[/思考\\]"},
            {"\\[Thinking\\]", "\\[/Thinking\\]"},
            {"\\*\\*思考过程\\*\\*", "\\*\\*分析结果\\*\\*"}
        };

        String remainingContent = content;

        for (String[] pattern : thinkingPatterns) {
            String startPattern = pattern[0];
            String endPattern = pattern[1];

            Matcher startMatcher = Pattern.compile(startPattern).matcher(remainingContent);
            if (startMatcher.find()) {
                int startIndex = startMatcher.start();
                int startEnd = startMatcher.end();

                // 检查是否有结束标签
                Matcher endMatcher = Pattern.compile(endPattern).matcher(remainingContent);
                if (endMatcher.find(startIndex)) {
                    int endIndex = endMatcher.start();
                    int endEnd = endMatcher.end();

                    // 提取思考内容（不含标签）
                    thinking = remainingContent.substring(startEnd, endIndex).trim();

                    // 提取结束标签后的正式内容
                    finalContent = remainingContent.substring(endEnd).trim();
                    foundEndTag = true;
                } else {
                    // 结束标签还未出现，所有内容都是思考过程
                    thinking = remainingContent;
                    finalContent = "";
                    foundEndTag = false;
                }
                break;
            }
        }

        // 如果没有找到任何思考标签，所有内容都是正式内容
        if (thinking.isEmpty() && finalContent.isEmpty()) {
            finalContent = content;
        }

        return new String[]{thinking, finalContent, foundEndTag ? "true" : "false"};
    }

    /**
     * 清理内容中的思考过程标记
     * 某些模型会将思考过程混入 content 中，需要用此方法清理
     *
     * @param content AI 返回的内容
     * @return 清理后的内容
     */
    public static String cleanThinkingTags(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String result = content;

        // 移除  thinking... response 标签及其内容
        result = result.replaceAll("(?s) thinking.*? response", "");

        // 移除 <thinking>...</thinking> 标签及其内容
        result = result.replaceAll("(?s)<thinking>.*?</thinking>", "");

        // 移除 [思考]...[/思考] 标签及其内容
        result = result.replaceAll("(?s)\\[思考\\].*?\\[/思考\\]", "");

        // 移除 [Thinking]...[/Thinking] 标签及其内容
        result = result.replaceAll("(?s)\\[Thinking\\].*?\\[/Thinking\\]", "");

        // 移除 **思考过程** 开头到 **分析结果** 结尾之间的内容
        result = result.replaceAll("(?s)\\*\\*思考过程\\*\\*.*?\\*\\*分析结果\\*\\*", "**分析结果**");

        // 移除 "思考过程：" 开头到 "分析结果：" 之间的内容
        result = result.replaceAll("(?s)思考过程[：:].*?分析结果[：:]", "分析结果：");

        // 清理多余的空行
        result = result.replaceAll("\n{3,}", "\n\n");

        // 去除首尾空白
        result = result.trim();

        return result;
    }
}