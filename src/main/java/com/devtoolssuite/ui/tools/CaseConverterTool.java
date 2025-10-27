package com.devtoolssuite.ui.tools;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CaseConverterTool extends JPanel {

    private JTextArea inputArea;
    private Map<String, JTextField> outputFields;

    private static final String[][] CASE_STYLES = {
        {"lowercase", "Lowercase"},
        {"uppercase", "Uppercase"},
        {"camelcase", "CamelCase"},
        {"capitalcase", "Capital Case"},
        {"constantcase", "CONSTANT_CASE"},
        {"dotcase", "dot.case"},
        {"headercase", "Header-Case"},
        {"nocase", "No Case"},
        {"paramcase", "param-case"},
        {"pascalcase", "PascalCase"},
        {"pathcase", "path/case"},
        {"sentencecase", "Sentence case"},
        {"snakecase", "snake_case"},
        {"mockingcase", "mOcKiNg CaSe"}
    };

    public CaseConverterTool() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, 0, 0));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);
        splitPane.setResizeWeight(0.5);

        JPanel leftPanel = new JPanel(new BorderLayout(6, 6));
        leftPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
        leftPanel.add(new JLabel("Input"), BorderLayout.NORTH);

        inputArea = new JTextArea();
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane inputScroll = new JScrollPane(inputArea);
        leftPanel.add(inputScroll, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
        rightPanel.setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel outputsPanel = new JPanel();
        outputsPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 0, 3, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        outputFields = new LinkedHashMap<>();

        for (int i = 0; i < CASE_STYLES.length; i++) {
            String key = CASE_STYLES[i][0];
            String label = CASE_STYLES[i][1];

            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            outputsPanel.add(new JLabel(label), gbc);

            gbc.gridx = 1; gbc.weightx = 1.0;
            JTextField field = new JTextField();
            field.setEditable(false);
            field.setFont(new Font("Monospaced", Font.PLAIN, 12));
            outputsPanel.add(field, gbc);

            outputFields.put(key, field);
        }

        JScrollPane outputScroll = new JScrollPane(outputsPanel);
        outputScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        outputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rightPanel.add(outputScroll, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);

        inputArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateAll(); }
            public void removeUpdate(DocumentEvent e) { updateAll(); }
            public void changedUpdate(DocumentEvent e) { updateAll(); }
        });

        updateAll();
    }

    private void updateAll() {
        String raw = inputArea.getText();
        Map<String, String> values = new LinkedHashMap<>();
        values.put("lowercase", toLowerCase(raw));
        values.put("uppercase", toUpperCase(raw));
        values.put("camelcase", toCamelCase(raw));
        values.put("capitalcase", toCapitalCase(raw));
        values.put("constantcase", toConstantCase(raw));
        values.put("dotcase", toDotCase(raw));
        values.put("headercase", toHeaderCase(raw));
        values.put("nocase", toNoCase(raw));
        values.put("paramcase", toParamCase(raw));
        values.put("pascalcase", toPascalCase(raw));
        values.put("pathcase", toPathCase(raw));
        values.put("sentencecase", toSentenceCase(raw));
        values.put("snakecase", toSnakeCase(raw));
        values.put("mockingcase", toMockingCase(raw));

        for (Map.Entry<String, JTextField> entry : outputFields.entrySet()) {
            entry.getValue().setText(values.get(entry.getKey()));
        }
    }

    private List<String> extractWords(String s) {
        String[] parts = s.split("[^A-Za-z0-9]+");
        List<String> words = new ArrayList<>();
        for (String p : parts) {
            if (!p.isEmpty()) {
                words.add(p);
            }
        }
        return words;
    }

    private String toLowerCase(String s) { return s.toLowerCase(); }
    private String toUpperCase(String s) { return s.toUpperCase(); }

    private String toCamelCase(String s) {
        List<String> words = extractWords(s);
        if (words.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(words.get(0).toLowerCase());
        for (int i = 1; i < words.size(); i++) sb.append(capitalize(words.get(i)));
        return sb.toString();
    }

    private String toPascalCase(String s) {
        List<String> words = extractWords(s);
        StringBuilder sb = new StringBuilder();
        for (String word : words) sb.append(capitalize(word));
        return sb.toString();
    }

    private String toSnakeCase(String s) {
        List<String> words = extractWords(s);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) sb.append("_");
            sb.append(words.get(i).toLowerCase());
        }
        return sb.toString();
    }

    private String toParamCase(String s) {
        List<String> words = extractWords(s);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) sb.append("-");
            sb.append(words.get(i).toLowerCase());
        }
        return sb.toString();
    }

    private String toConstantCase(String s) {
        List<String> words = extractWords(s);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) sb.append("_");
            sb.append(words.get(i).toUpperCase());
        }
        return sb.toString();
    }

    private String toDotCase(String s) {
        List<String> words = extractWords(s);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) sb.append(".");
            sb.append(words.get(i).toLowerCase());
        }
        return sb.toString();
    }

    private String toHeaderCase(String s) {
        List<String> words = extractWords(s);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) sb.append("-");
            sb.append(capitalize(words.get(i)));
        }
        return sb.toString();
    }

    private String toPathCase(String s) {
        List<String> words = extractWords(s);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) sb.append("/");
            sb.append(words.get(i).toLowerCase());
        }
        return sb.toString();
    }

    private String toSentenceCase(String s) {
        List<String> words = extractWords(s);
        if (words.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(words.get(i).toLowerCase());
        }
        String result = sb.toString();
        if (result.length() > 0) return Character.toUpperCase(result.charAt(0)) + result.substring(1);
        return result;
    }

    private String toCapitalCase(String s) {
        List<String> words = extractWords(s);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(capitalize(words.get(i)));
        }
        return sb.toString();
    }

    private String toNoCase(String s) { return String.join(" ", extractWords(s)); }

    private String toMockingCase(String s) {
        StringBuilder sb = new StringBuilder();
        boolean upper = true;
        for (char ch : s.toCharArray()) {
            if (Character.isLetter(ch)) {
                sb.append(upper ? Character.toUpperCase(ch) : Character.toLowerCase(ch));
                upper = !upper;
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private String capitalize(String s) {
        if (s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
