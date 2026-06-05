package com.kggzs.cn.mt;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;
import bin.mt.plugin.api.ui.PluginView;

import java.io.InputStream;

public class MyPreference implements PluginPreference {
    @Override
    public void onBuild(PluginContext context, Builder builder) {
        builder.title("{plugin_name}")
               .subtitle("{plugin_author}");

        builder.addText("{plugin_description}")
               .summary("{plugin_description}");

        builder.addText("{plugin_website}")
               .summary("www.kggzs.cn")
               .onClick((ui, preference) -> {
                   context.setClipboardText("www.kggzs.cn");
                   context.showToast("{copy_success_clipboard}");
               });

        builder.addText("{plugin_source}")
               .summary("https://github.com/kggzs/MT_Plugin")
               .onClick((ui, preference) -> {
                   context.setClipboardText("https://github.com/kggzs/MT_Plugin");
                   context.showToast("{copy_success_clipboard}");
               });

        builder.addText("{support_author}")
               .summary("{support_author_summary}")
               .onClick((ui, preference) -> {
                   showSupportDialog(ui, context);
               });

        builder.addHeader("{ai_config_group}");

        builder.addText("{ai_chat_title}")
               .summary("{ai_chat_settings_summary}")
               .onClick((ui, preference) -> {
                   AIChatMenu.showDialog(ui, context);
               });

        builder.addText("{mcp_service_config}")
               .summary("{mcp_service_summary}")
               .onClick((ui, preference) -> {
                   MCPServiceMenu.showMainDialog(ui, context);
               });

        builder.addText("{api_config}")
               .summary("{api_config_summary}")
               .onClick((ui, preference) -> {
                   PreferenceApiDialog.showApiConfigDialog(ui, context);
               });

        builder.addText("{ai_capability_config}")
               .summary("{ai_capability_config_summary}")
               .onClick((ui, preference) -> {
                   PreferenceSkillDialog.showAiCapabilityDialog(ui, context);
               });

        builder.addHeader("{time_config_group}");

        builder.addText("{time_format_config}")
               .summary("{time_format_config_summary}")
               .onClick((ui, preference) -> {
                   PreferenceTimeFormatDialog.showTimeFormatConfigDialog(ui, context);
               });

        builder.addText("{reset_config}")
               .summary("{reset_config_summary}")
               .onClick((ui, preference) -> {
                   ui.buildDialog()
                       .setTitle("{confirm_reset_title}")
                       .setMessage("{confirm_reset_message}")
                       .setPositiveButton("{confirm_reset_positive}", (dialog, which) -> {
                           com.kggzs.cn.mt.util.AIHelper.resetToDefault(context);
                           context.showToast("{config_reset_success}");
                           dialog.dismiss();
                       })
                       .setNegativeButton("{confirm_reset_negative}", null)
                       .show();
               });

        builder.addHeader("{features_group}");

        builder.addText("{features_title}")
               .summary("{features_summary}");

        builder.addText("{encode_decode_function}")
               .summary("{encode_decode_usage}");

        builder.addText("{base64_encode}")
               .summary("{base64_encode_usage}");

        builder.addText("{hex_encode}")
               .summary("{hex_encode_usage}");

        builder.addText("{unicode_encode}")
               .summary("{unicode_encode_usage}");

        builder.addText("{rot13_encode}")
               .summary("{rot13_encode_usage}");

        builder.addText("{binary_encode}")
               .summary("{binary_encode_usage}");

        builder.addText("{hash_function}")
               .summary("{hash_usage}");

        builder.addText("{timestamp_function}")
               .summary("{timestamp_usage}");

        builder.addText("{ai_code_analysis}")
               .summary("{ai_code_analysis_usage}");

        builder.addText("{ai_quick_analysis}")
               .summary("{ai_quick_analysis_usage}");
    }

    /**
     * 显示支持作者对话框
     */
    private void showSupportDialog(bin.mt.plugin.api.ui.PluginUI ui, PluginContext context) {
        int padding = ui.dp2px(16);
        int smallMargin = ui.dp2px(8);

        PluginView view = ui.buildVerticalLayout()
            .addTextView().text("{support_message}").textSize(14).widthMatchParent().marginBottom(smallMargin)
            .addButton("alipay_btn").text("{alipay}").widthMatchParent().marginBottom(smallMargin)
            .addButton("wechat_btn").text("{wechat_pay}").widthMatchParent()
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        bin.mt.plugin.api.ui.dialog.PluginDialog dialog = ui.buildDialog()
            .setTitle("{support_author_title}")
            .setView(view)
            .setNegativeButton("{close}", null)
            .show();

        view.requireViewById("alipay_btn").setOnClickListener(v -> {
            showPaymentImage(ui, context, "zfb.jpg", "{alipay}");
        });

        view.requireViewById("wechat_btn").setOnClickListener(v -> {
            showPaymentImage(ui, context, "wx.jpg", "{wechat_pay}");
        });
    }

    /**
     * 显示支付二维码图片
     */
    private void showPaymentImage(bin.mt.plugin.api.ui.PluginUI ui, PluginContext context, String imageName, String title) {
        try {
            InputStream is = context.getAssetsAsStream(imageName);
            if (is == null) {
                context.showToast("{load_image_failed}");
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();

            if (bitmap == null) {
                context.showToast("{load_image_failed}");
                return;
            }

            int maxWidth = ui.dp2px(280);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            if (width > maxWidth) {
                float scale = (float) maxWidth / width;
                width = maxWidth;
                height = (int) (height * scale);
            }

            int padding = ui.dp2px(16);
            int smallMargin = ui.dp2px(12);
            PluginView view = ui.buildVerticalLayout()
                .addImageView("qr_image")
                    .image(bitmap)
                    .width(width)
                    .height(height)
                    .layoutGravity(android.view.Gravity.CENTER)
                    .marginBottom(smallMargin)
                .addTextView()
                    .text("{screenshot_tip}")
                    .textSize(16)
                    .textColor(0xFF666666)
                    .widthMatchParent()
                    .layoutGravity(android.view.Gravity.CENTER)
                .paddingHorizontal(padding)
                .paddingVertical(padding)
                .build();

            ui.buildDialog()
                .setTitle(title)
                .setView(view)
                .setNegativeButton("{close}", null)
                .show();

        } catch (Exception e) {
            context.showToast("{load_image_failed}: " + e.getMessage());
        }
    }
}