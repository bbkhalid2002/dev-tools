package com.devtoolssuite.ui.tools;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 Encoder/Decoder tool - exactly replicates Python Base64Tab behavior.
 * Bi-directional Base64 conversion.
 */
public class Base64Tool extends JPanel {

    private JTextArea leftText;
    private JTextArea rightText;

    public Base64Tool() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, 0, 0));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));

        JButton encodeBtn = new JButton("Encode →");
        encodeBtn.addActionListener(e -> encode());
        toolbar.add(encodeBtn);

        JButton decodeBtn = new JButton("← Decode");
        decodeBtn.addActionListener(e -> decode());
        toolbar.add(decodeBtn);

        add(toolbar, BorderLayout.NORTH);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.5);

        // Left panel: plain text
        JPanel leftPanel = new JPanel(new BorderLayout(6, 6));
        leftPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
        leftPanel.add(new JLabel("Plain text"), BorderLayout.NORTH);

        leftText = new JTextArea();
        leftText.setLineWrap(false);
        leftText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane leftScroll = new JScrollPane(leftText);
        leftPanel.add(leftScroll, BorderLayout.CENTER);

        // Right panel: Base64 text
        JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
        rightPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
        rightPanel.add(new JLabel("Base64"), BorderLayout.NORTH);

        rightText = new JTextArea();
        rightText.setLineWrap(false);
        rightText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane rightScroll = new JScrollPane(rightText);
        rightPanel.add(rightScroll, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);
    }

    private void encode() {
        try {
            String plainText = leftText.getText();
            byte[] data = plainText.getBytes(StandardCharsets.UTF_8);
            String encoded = Base64.getEncoder().encodeToString(data);
            rightText.setText(encoded);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Encode Error: " + e.getMessage(),
                "Encode Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void decode() {
        try {
            String base64Text = rightText.getText().trim();
            if (base64Text.isEmpty()) {
                leftText.setText("");
                return;
            }

            // Decode with validation (strict mode)
            byte[] data = Base64.getDecoder().decode(base64Text);
            // Decode UTF-8 with error replacement (like Python errors='replace')
            String decoded = new String(data, StandardCharsets.UTF_8);
            leftText.setText(decoded);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Decode Error: Invalid Base64 string\n" + e.getMessage(),
                "Decode Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Decode Error: " + e.getMessage(),
                "Decode Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
