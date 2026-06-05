package com.kggzs.cn.mt;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.ui.PluginButton;
import bin.mt.plugin.api.ui.PluginEditText;
import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.dialog.PluginDialog;

import com.kggzs.cn.mt.util.TimeFormatHelper;

import java.util.Date;

/**
 * 时间格式配置对话框
 * 管理时间插入的格式和显示模式设置
 */
public class PreferenceTimeFormatDialog {

    /**
     * 显示时间格式配置对话框
     *
     * @param ui      MT 插件 UI 接口
     * @param context MT 插件上下文
     */
    public static void showTimeFormatConfigDialog(PluginUI ui, PluginContext context) {
        int currentFormat = TimeFormatHelper.getTimeFormatType(context);
        int currentMode = TimeFormatHelper.getTimeMode(context);

        int padding = ui.dp2px(16);
        int smallMargin = ui.dp2px(8);
        int sectionMargin = ui.dp2px(16);

        String currentPreview = TimeFormatHelper.getFormattedTime(context, new Date());
        boolean isCustom = (currentMode == TimeFormatHelper.MODE_CUSTOM);
        String timeSuffix = (currentMode == TimeFormatHelper.MODE_DATE_TIME) ? " 19:29:55" : "";

        // 构建视图（使用 var 以便在循环中添加格式按钮）
        var builder = ui.buildVerticalLayout()
            .addTextView("current_preview").text(context.getString("{current_format_prefix}") + currentPreview)
                .textSize(16).textColor(0xFF4CAF50).marginBottom(sectionMargin)
            .addTextView().text("{display_mode_section}").textSize(14).textColor(0xFF666666).marginBottom(smallMargin)
            .addButton("mode_date_only").text((currentMode == TimeFormatHelper.MODE_DATE_ONLY ? "✓ " : "") + context.getString("{mode_date_only_label}")).widthMatchParent().marginBottom(smallMargin)
            .addButton("mode_date_time").text((currentMode == TimeFormatHelper.MODE_DATE_TIME ? "✓ " : "") + context.getString("{mode_date_time_label}")).widthMatchParent().marginBottom(smallMargin)
            .addButton("mode_custom").text((currentMode == TimeFormatHelper.MODE_CUSTOM ? "✓ " : "") + context.getString("{mode_custom_label}")).widthMatchParent().marginBottom(sectionMargin)
            .addTextView().text("{gregorian_format_section}").textSize(14).textColor(0xFF666666).marginBottom(smallMargin);

        // 使用循环动态生成 10 个格式按钮
        for (int i = 0; i <= 9; i++) {
            String formatText = (i + 1) + ". " + TimeFormatHelper.FORMAT_NAMES[i] + " - " + TimeFormatHelper.FORMAT_EXAMPLES[i] + timeSuffix;
            if (currentFormat == i && !isCustom) {
                formatText = "✓ " + formatText;
            }
            // 公历格式和农历格式之间添加节间距
            int bottomMargin = (i == 4) ? sectionMargin : ((i == 9) ? 0 : smallMargin);
            builder.addButton("format_" + i).text(formatText).widthMatchParent().marginBottom(bottomMargin);

            // 在公历格式 (i=4) 之后插入农历格式节标题
            if (i == 4) {
                builder.addTextView().text("{lunar_format_section}").textSize(14).textColor(0xFF666666).marginBottom(smallMargin);
            }
        }

        PluginView view = builder
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        PluginDialog dialog = ui.buildDialog()
            .setTitle("{time_format_config}")
            .setView(view)
            .setNegativeButton("{close}", null)
            .show();

        // 绑定模式按钮事件
        PluginButton modeDateOnlyBtn = view.requireViewById("mode_date_only");
        PluginButton modeDateTimeBtn = view.requireViewById("mode_date_time");
        PluginButton modeCustomBtn = view.requireViewById("mode_custom");

        modeDateOnlyBtn.setOnClickListener(v -> {
            TimeFormatHelper.setTimeMode(context, TimeFormatHelper.MODE_DATE_ONLY);
            String preview = TimeFormatHelper.getFormattedTime(context, new Date());
            context.showToast("{switched_to_prefix}" + preview);
            dialog.dismiss();
            showTimeFormatConfigDialog(ui, context);
        });

        modeDateTimeBtn.setOnClickListener(v -> {
            TimeFormatHelper.setTimeMode(context, TimeFormatHelper.MODE_DATE_TIME);
            String preview = TimeFormatHelper.getFormattedTime(context, new Date());
            context.showToast("{switched_to_prefix}" + preview);
            dialog.dismiss();
            showTimeFormatConfigDialog(ui, context);
        });

        modeCustomBtn.setOnClickListener(v -> {
            dialog.dismiss();
            showCustomFormatEditorDialog(ui, context);
        });

        // 使用循环绑定预设格式按钮点击事件
        for (int i = 0; i <= 9; i++) {
            PluginButton btn = view.requireViewById("format_" + i);
            final int formatIndex = i;
            final PluginDialog finalDialog = dialog;
            btn.setOnClickListener(v -> {
                int mode = TimeFormatHelper.getTimeMode(context);
                if (mode == TimeFormatHelper.MODE_CUSTOM) {
                    TimeFormatHelper.setTimeMode(context, TimeFormatHelper.MODE_DATE_TIME);
                }
                TimeFormatHelper.setTimeFormatType(context, formatIndex);
                String preview = TimeFormatHelper.getFormattedTime(context, new Date());
                context.showToast("{switched_to_prefix}" + preview);
                finalDialog.dismiss();
            });
        }
    }

    /**
     * 显示自定义格式编辑对话框
     *
     * @param ui      MT 插件 UI 接口
     * @param context MT 插件上下文
     */
    private static void showCustomFormatEditorDialog(PluginUI ui, PluginContext context) {
        int padding = ui.dp2px(16);
        int smallMargin = ui.dp2px(8);

        String currentFormat = TimeFormatHelper.getCustomFormatString(context);
        String preview = TimeFormatHelper.parseCustomFormat(currentFormat, new Date());

        String formatHelp = "【年份】yyyy-4位(2026) yy-2位(26)\n" +
            "【月份】MM-补0(05) M-不补0(5) N-农历(四月)\n" +
            "【日期】dd-补0(20) d-不补0(20) e-农历(初四)\n" +
            "【星期】E-周几(周三)\n" +
            "【时段】a-上午/下午 aa-精确(傍晚/凌晨)\n" +
            "【小时】HH-24时补0(19) H-24时(19)\n" +
            "       hh-12时补0(07) h-12时(7)\n" +
            "【分钟】mm-补0(08) m-不补0(8)\n" +
            "【秒数】ss-补0(55) s-不补0(55)\n" +
            "【时辰】l-地支(酉)";

        PluginView view = ui.buildVerticalLayout()
            .addTextView().text("{custom_format_preview}").textSize(14).textColor(0xFF666666).marginBottom(smallMargin)
            .addTextView("format_preview").text(preview).textSize(16).textColor(0xFF4CAF50).marginBottom(smallMargin)
            .addTextView().text("{custom_format_input}").textSize(14).textColor(0xFF666666).marginTop(smallMargin).marginBottom(smallMargin)
            .addEditBox("format_input").text(currentFormat).minLines(2).maxLines(2).widthMatchParent().marginBottom(smallMargin)
                .softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD)
            .addTextView().text("【格式说明】").textSize(13).textColor(0xFF333333).marginTop(smallMargin).marginBottom(smallMargin)
            .addTextView().text(formatHelp).textSize(12).textColor(0xFF666666)
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        PluginEditText formatInput = view.requireViewById("format_input");

        ui.buildDialog()
            .setTitle("{custom_format_title}")
            .setView(view)
            .setPositiveButton("{save}", (d, which) -> {
                String format = formatInput.getText().toString().trim();
                if (format.isEmpty()) {
                    format = TimeFormatHelper.DEFAULT_CUSTOM_FORMAT;
                }
                TimeFormatHelper.setCustomFormatString(context, format);
                TimeFormatHelper.setTimeMode(context, TimeFormatHelper.MODE_CUSTOM);
                String finalPreview = TimeFormatHelper.parseCustomFormat(format, new Date());
                context.showToast("已保存: " + finalPreview);
            })
            .setNegativeButton("{cancel}", null)
            .setNeutralButton("{reset_default}", (d, which) -> {
                TimeFormatHelper.setCustomFormatString(context, TimeFormatHelper.DEFAULT_CUSTOM_FORMAT);
                TimeFormatHelper.setTimeMode(context, TimeFormatHelper.MODE_CUSTOM);
                context.showToast("{reset_to_default_format}");
            })
            .show();
    }
}