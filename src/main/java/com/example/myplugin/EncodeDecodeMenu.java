package com.example.myplugin;

import android.graphics.drawable.Drawable;
import android.util.Base64;
import bin.mt.plugin.api.editor.TextEditor;
import bin.mt.plugin.api.editor.TextEditorFloatingMenu;
import bin.mt.plugin.api.editor.BaseTextEditorFloatingMenu;
import bin.mt.plugin.api.drawable.MaterialIcons;
import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.PluginEditText;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class EncodeDecodeMenu extends BaseTextEditorFloatingMenu {
    private PluginEditText inputEditText;
    private TextEditor currentEditor;
    private String originalText;

    @Override
    public String name() {
        return "{encode_decode_function}";
    }

    @Override
    public Drawable icon() {
        return MaterialIcons.get("code");
    }

    @Override
    public boolean checkVisible(TextEditor editor) {
        return true;
    }

    @Override
    public void onMenuClick(PluginUI pluginUI, TextEditor editor) {
        currentEditor = editor;
        showEncodeDecodeUI(pluginUI, editor);
    }

    private void showEncodeDecodeUI(PluginUI pluginUI, TextEditor editor) {
        int padding = pluginUI.dp2px(12);
        int buttonHeight = pluginUI.dp2px(36);
        int smallMargin = pluginUI.dp2px(4);
        int mediumMargin = pluginUI.dp2px(8);
        int largeMargin = pluginUI.dp2px(16);

        PluginView view = pluginUI.buildVerticalLayout()
            .addEditText("input_text").hint("{select_text_hint}").widthMatchParent().height(pluginUI.dp2px(80)).marginBottom(mediumMargin)
            .addHorizontalLayout("action_row").children(row -> row
                .addButton("undo_btn").text("{undo}").height(buttonHeight).marginRight(smallMargin).onClick(v -> handleUndo(pluginUI))
                .addButton("replace_btn").text("{replace}").height(buttonHeight).onClick(v -> handleReplace(pluginUI))
            ).marginBottom(largeMargin)
            .addTextView("section_label1").text("转换工具").textSize(14).textColor(pluginUI.colorText()).marginBottom(smallMargin)
            .addHorizontalLayout("row1").children(row -> row
                .addButton("timestamp_btn").text("{timestamp_function}").height(buttonHeight).marginRight(smallMargin).onClick(v -> handleTimestamp(pluginUI))
                .addButton("hash_btn").text("{hash_function}").height(buttonHeight).onClick(v -> handleHash(pluginUI))
            ).marginBottom(mediumMargin)
            .addTextView("section_label2").text("编码/解码").textSize(14).textColor(pluginUI.colorText()).marginBottom(smallMargin)
            .addHorizontalLayout("row2").children(row -> row
                .addButton("base64_encode_btn").text("Base64编码").height(buttonHeight).marginRight(smallMargin).onClick(v -> handleBase64Encode(pluginUI))
                .addButton("base64_decode_btn").text("Base64解码").height(buttonHeight).onClick(v -> handleBase64Decode(pluginUI))
            ).marginBottom(smallMargin)
            .addHorizontalLayout("row3").children(row -> row
                .addButton("hex_encode_btn").text("Hex编码").height(buttonHeight).marginRight(smallMargin).onClick(v -> handleHexEncode(pluginUI))
                .addButton("hex_decode_btn").text("Hex解码").height(buttonHeight).onClick(v -> handleHexDecode(pluginUI))
            ).marginBottom(smallMargin)
            .addHorizontalLayout("row4").children(row -> row
                .addButton("unicode_encode_btn").text("Unicode编码").height(buttonHeight).marginRight(smallMargin).onClick(v -> handleUnicodeEncode(pluginUI))
                .addButton("unicode_decode_btn").text("Unicode解码").height(buttonHeight).onClick(v -> handleUnicodeDecode(pluginUI))
            ).marginBottom(smallMargin)
            .addHorizontalLayout("row5").children(row -> row
                .addButton("url_encode_btn").text("URL编码").height(buttonHeight).marginRight(smallMargin).onClick(v -> handleUrlEncode(pluginUI))
                .addButton("url_decode_btn").text("URL解码").height(buttonHeight).onClick(v -> handleUrlDecode(pluginUI))
            ).marginBottom(smallMargin)
            .addHorizontalLayout("row6").children(row -> row
                .addButton("rot13_encode_btn").text("ROT13编码").height(buttonHeight).marginRight(smallMargin).onClick(v -> handleRot13Encode(pluginUI))
                .addButton("rot13_decode_btn").text("ROT13解码").height(buttonHeight).onClick(v -> handleRot13Decode(pluginUI))
            ).marginBottom(smallMargin)
            .addHorizontalLayout("row7").children(row -> row
                .addButton("binary_encode_btn").text("二进制编码").height(buttonHeight).marginRight(smallMargin).onClick(v -> handleBinaryEncode(pluginUI))
                .addButton("binary_decode_btn").text("二进制解码").height(buttonHeight).onClick(v -> handleBinaryDecode(pluginUI))
            ).marginBottom(mediumMargin)
            .paddingHorizontal(padding)
            .paddingVertical(padding)
            .build();

        inputEditText = view.requireViewById("input_text");
        
        if (editor.hasTextSelected()) {
            int start = editor.getSelectionStart();
            int end = editor.getSelectionEnd();
            String selectedText = editor.subText(start, end);
            inputEditText.setText(selectedText);
            originalText = selectedText;
        } else {
            originalText = "";
        }

        pluginUI.buildDialog()
            .setTitle("{encode_decode_function}")
            .setView(view)
            .setNegativeButton("{close}", null)
            .show();
    }

    private String getInputText() {
        if (inputEditText != null) {
            String text = inputEditText.getText().toString().trim();
            if (text.isEmpty() && currentEditor != null && currentEditor.hasTextSelected()) {
                int start = currentEditor.getSelectionStart();
                int end = currentEditor.getSelectionEnd();
                text = currentEditor.subText(start, end);
            }
            return text;
        }
        return "";
    }

    private void handleUndo(PluginUI pluginUI) {
        if (inputEditText != null && originalText != null) {
            inputEditText.setText(originalText);
            pluginUI.showToast("{undo_success}");
        }
    }

    private void handleReplace(PluginUI pluginUI) {
        String currentText = "";
        if (inputEditText != null) {
            currentText = inputEditText.getText().toString();
        }
        
        if (currentText.isEmpty()) {
            pluginUI.showToast("{select_text_hint}");
            return;
        }
        
        if (currentEditor != null) {
            if (currentEditor.hasTextSelected()) {
                int start = currentEditor.getSelectionStart();
                int end = currentEditor.getSelectionEnd();
                currentEditor.replaceText(start, end, currentText);
            } else {
                int insertPos = currentEditor.getSelectionEnd();
                currentEditor.insertText(insertPos, currentText);
            }
            pluginUI.showToast("{replace_success}");
        }
    }

    private void replaceSelectedText(String result) {
        if (inputEditText != null && !inputEditText.getText().toString().trim().isEmpty()) {
            inputEditText.setText(result);
            return;
        }
        
        if (currentEditor != null) {
            if (currentEditor.hasTextSelected()) {
                int start = currentEditor.getSelectionStart();
                int end = currentEditor.getSelectionEnd();
                currentEditor.replaceText(start, end, result);
            } else {
                int insertPos = currentEditor.getSelectionEnd();
                currentEditor.insertText(insertPos, result);
            }
        }
    }

    private void handleTimestamp(PluginUI pluginUI) {
        String selected = getInputText();
        if (selected.isEmpty()) {
            pluginUI.showToast("{select_text_hint}");
            return;
        }

        try {
            String result;
            if (isTimestamp(selected)) {
                long timestamp = Long.parseLong(selected);
                if (timestamp > 10000000000000L) {
                    timestamp = timestamp / 1000;
                }
                result = formatTimestamp(timestamp);
                pluginUI.showToast("{timestamp_to_date_success}");
            } else if (isDateTime(selected)) {
                long timestamp = parseDateTimeToTimestamp(selected);
                result = String.valueOf(timestamp);
                pluginUI.showToast("{date_to_timestamp_success}");
            } else {
                pluginUI.showToast("{invalid_timestamp_or_date}");
                return;
            }
            replaceSelectedText(result);
        } catch (Exception e) {
            pluginUI.showToast("{convert_error}");
        }
    }

    private void handleHash(PluginUI pluginUI) {
        String selected = getInputText();
        if (selected.isEmpty()) {
            pluginUI.showToast("{select_text_hint}");
            return;
        }

        String[] hashTypes = {"MD5", "SHA-256", "SHA-512"};
        int[] selection = {0};

        pluginUI.buildDialog()
            .setTitle("{hash_type}")
            .setSingleChoiceItems(hashTypes, 0, (dialog, which) -> selection[0] = which)
            .setPositiveButton("{copy}", (dialog, which) -> {
                String hash = calculateHash(selected, hashTypes[selection[0]]);
                pluginUI.getContext().setClipboardText(hash);
                pluginUI.showToast(hashTypes[selection[0]] + ": " + copy_success(pluginUI, hash));
            })
            .show();
    }

    private String copy_success(PluginUI pluginUI, String hash) {
        return pluginUI.getContext().getString("{copy_success}");
    }

    private void handleBase64Encode(PluginUI pluginUI) {
        String selected = getInputText();
        if (selected.isEmpty()) {
            pluginUI.showToast("{select_text_hint}");
            return;
        }

        try {
            String result = Base64.encodeToString(selected.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
            replaceSelectedText(result);
            pluginUI.showToast("{encode_success}");
        } catch (Exception e) {
            pluginUI.showToast("{encode_decode_error}: " + e.getMessage());
        }
    }

    private void handleBase64Decode(PluginUI pluginUI) {
        String selected = getInputText();
        if (selected.isEmpty()) {
            pluginUI.showToast("{select_text_hint}");
            return;
        }

        try {
            byte[] decoded = Base64.decode(selected, Base64.DEFAULT);
            String result = new String(decoded, StandardCharsets.UTF_8);
            replaceSelectedText(result);
            pluginUI.showToast("{decode_success}");
        } catch (Exception e) {
            pluginUI.showToast("{decode_error}: " + e.getMessage());
        }
    }

    private void handleHexEncode(PluginUI pluginUI) {
        String selected = getInputText();
        if (selected.isEmpty()) {
            pluginUI.showToast("{select_text_hint}");
            return;
        }

        try {
            StringBuilder hex = new StringBuilder();
            for (byte b : selected.getBytes(StandardCharsets.UTF_8)) {
                hex.append(String.format("%02X", b));
            }
            replaceSelectedText(hex.toString());
            pluginUI.showToast("{encode_success}");
        } catch (Exception e) {
            pluginUI.showToast("{encode_decode_error}: " + e.getMessage());
        }
    }

    private void handleHexDecode(PluginUI pluginUI) {
        String selected = getInputText();
        if (selected.isEmpty()) {
            pluginUI.showToast("{select_text_hint}");
            return;
        }

        try {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < selected.length(); i += 2) {
                String str = selected.substring(i, i + 2);
                result.append((char) Integer.parseInt(str, 16));
            }
            replaceSelectedText(result.toString());
            pluginUI.showToast("{decode_success}");
        } catch (Exception e) {
            pluginUI.showToast("{decode_error}: " + e.getMessage());
        }
    }

    private void handleUnicodeEncode(PluginUI pluginUI) {
        String selected = getInputText();
        if (selected.isEmpty()) {
            pluginUI.showToast("{select_text_hint}");
            return;
        }

        try {
            StringBuilder unicode = new StringBuilder();
            for (char c : selected.toCharArray()) {
                unicode.append("\\u").append(String.format("%04X", (int) c));
            }
            replaceSelectedText(unicode.toString());
            pluginUI.showToast("{encode_success}");
        } catch (Exception e) {
            pluginUI.showToast("{encode_decode_error}: " + e.getMessage());
        }
    }

    private void handleUnicodeDecode(PluginUI pluginUI) {
        String selected = getInputText();
        if (selected.isEmpty()) {
            pluginUI.showToast("{select_text_hint}");
            return;
        }

        try {
            StringBuilder result = new StringBuilder();
            String[] parts = selected.split("\\\\u");
            for (String part : parts) {
                if (part.isEmpty()) {
                    continue;
                }
                try {
                    result.append((char) Integer.parseInt(part, 16));
                } catch (Exception e) {
                    result.append(part);
                }
            }
            replaceSelectedText(result.toString());
            pluginUI.showToast("{decode_success}");
        } catch (Exception e) {
            pluginUI.showToast("{decode_error}: " + e.getMessage());
        }
    }

    private void handleUrlEncode(PluginUI pluginUI) {
        String selected = getInputText();
        if (selected.isEmpty()) {
            pluginUI.showToast("{select_text_hint}");
            return;
        }

        try {
            String result = java.net.URLEncoder.encode(selected, StandardCharsets.UTF_8.name());
            replaceSelectedText(result);
            pluginUI.showToast("{encode_success}");
        } catch (Exception e) {
            pluginUI.showToast("{encode_decode_error}: " + e.getMessage());
        }
    }

    private void handleUrlDecode(PluginUI pluginUI) {
        String selected = getInputText();
        if (selected.isEmpty()) {
            pluginUI.showToast("{select_text_hint}");
            return;
        }

        try {
            String result = java.net.URLDecoder.decode(selected, StandardCharsets.UTF_8.name());
            replaceSelectedText(result);
            pluginUI.showToast("{decode_success}");
        } catch (Exception e) {
            pluginUI.showToast("{decode_error}: " + e.getMessage());
        }
    }

    private void handleRot13Encode(PluginUI pluginUI) {
        String selected = getInputText();
        if (selected.isEmpty()) {
            pluginUI.showToast("{select_text_hint}");
            return;
        }

        try {
            StringBuilder result = new StringBuilder();
            for (char c : selected.toCharArray()) {
                if (c >= 'a' && c <= 'z') {
                    c = (char) ('a' + (c - 'a' + 13) % 26);
                } else if (c >= 'A' && c <= 'Z') {
                    c = (char) ('A' + (c - 'A' + 13) % 26);
                }
                result.append(c);
            }
            replaceSelectedText(result.toString());
            pluginUI.showToast("{encode_success}");
        } catch (Exception e) {
            pluginUI.showToast("{encode_decode_error}: " + e.getMessage());
        }
    }

    private void handleRot13Decode(PluginUI pluginUI) {
        String selected = getInputText();
        if (selected.isEmpty()) {
            pluginUI.showToast("{select_text_hint}");
            return;
        }

        try {
            StringBuilder result = new StringBuilder();
            for (char c : selected.toCharArray()) {
                if (c >= 'a' && c <= 'z') {
                    c = (char) ('a' + (c - 'a' + 13) % 26);
                } else if (c >= 'A' && c <= 'Z') {
                    c = (char) ('A' + (c - 'A' + 13) % 26);
                }
                result.append(c);
            }
            replaceSelectedText(result.toString());
            pluginUI.showToast("{decode_success}");
        } catch (Exception e) {
            pluginUI.showToast("{encode_decode_error}: " + e.getMessage());
        }
    }

    private void handleBinaryEncode(PluginUI pluginUI) {
        String selected = getInputText();
        if (selected.isEmpty()) {
            pluginUI.showToast("{select_text_hint}");
            return;
        }

        try {
            StringBuilder binary = new StringBuilder();
            byte[] bytes = selected.getBytes(StandardCharsets.UTF_8);
            for (byte b : bytes) {
                binary.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
            }
            replaceSelectedText(binary.toString());
            pluginUI.showToast("{encode_success}");
        } catch (Exception e) {
            pluginUI.showToast("{encode_decode_error}: " + e.getMessage());
        }
    }

    private void handleBinaryDecode(PluginUI pluginUI) {
        String selected = getInputText();
        if (selected.isEmpty()) {
            pluginUI.showToast("{select_text_hint}");
            return;
        }

        try {
            String binary = selected.replaceAll("\\s+", "");
            
            if (binary.isEmpty()) {
                pluginUI.showToast("{decode_error}: Empty input");
                return;
            }
            
            if (!binary.matches("[01]+")) {
                pluginUI.showToast("{decode_error}: Invalid binary format");
                return;
            }
            
            if (binary.length() % 8 != 0) {
                pluginUI.showToast("{decode_error}: Binary length must be multiple of 8");
                return;
            }
            
            byte[] bytes = new byte[binary.length() / 8];
            for (int i = 0; i < binary.length(); i += 8) {
                String byteStr = binary.substring(i, i + 8);
                bytes[i / 8] = (byte) Integer.parseInt(byteStr, 2);
            }
            
            String result = new String(bytes, StandardCharsets.UTF_8);
            replaceSelectedText(result);
            pluginUI.showToast("{decode_success}");
        } catch (NumberFormatException e) {
            pluginUI.showToast("{decode_error}: Invalid binary format");
        } catch (Exception e) {
            pluginUI.showToast("{decode_error}: " + e.getMessage());
        }
    }

    private boolean isTimestamp(String text) {
        return Pattern.matches("^\\d+$", text.trim());
    }

    private boolean isDateTime(String text) {
        String[] patterns = {
            "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}",
            "\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}",
            "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}",
            "\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}",
            "\\d{4}-\\d{2}-\\d{2}",
            "\\d{4}/\\d{2}/\\d{2}"
        };
        for (String pattern : patterns) {
            if (Pattern.matches(pattern, text.trim())) {
                return true;
            }
        }
        return false;
    }

    private long parseDateTimeToTimestamp(String text) throws ParseException {
        String[] formats = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy/MM/dd HH:mm",
            "yyyy-MM-dd",
            "yyyy/MM/dd"
        };
        
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                return sdf.parse(text.trim()).getTime() / 1000;
            } catch (ParseException e) {
                continue;
            }
        }
        throw new ParseException("Unable to parse date", 0);
    }

    private String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }

    private String calculateHash(String text, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "{hash_error}";
        }
    }
}
