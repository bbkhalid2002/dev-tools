package com.devtoolssuite.ui.tools;

import com.devtoolssuite.ui.DevToolsSuiteEncryptor;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * AES-GCM Encryption/Decryption tool.
 */
public class DevToolsSuiteTool extends JPanel {

    private JTextField algorithmField;
    private JTextField passwordField;
    private JTextField saltField;
    private JTextArea inputTextArea;
    private JTextArea outputTextArea;

    public DevToolsSuiteTool() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel configPanel = createConfigurationPanel();
        add(configPanel, BorderLayout.NORTH);

        JPanel ioPanel = createInputOutputPanel();
        add(ioPanel, BorderLayout.CENTER);
    }

    private JPanel createConfigurationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Configuration",
            TitledBorder.LEFT,
            TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Algorithm
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("Algorithm:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        algorithmField = new JTextField("PBKDF2WithHmacSHA256");
        panel.add(algorithmField, gbc);

        // Secret Key Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        passwordField = new JTextField();
        panel.add(passwordField, gbc);

        // Secret Key Salt
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panel.add(new JLabel("Salt:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        saltField = new JTextField();
        panel.add(saltField, gbc);

        // Buttons panel
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(10, 5, 5, 5);
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton encryptButton = new JButton("Encrypt");
        encryptButton.addActionListener(e -> performEncryption());
        buttonsPanel.add(encryptButton);

        JButton decryptButton = new JButton("Decrypt");
        decryptButton.addActionListener(e -> performDecryption());
        buttonsPanel.add(decryptButton);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearFields());
        buttonsPanel.add(clearButton);

        JButton copyButton = new JButton("Copy Output");
        copyButton.addActionListener(e -> copyOutputToClipboard());
        buttonsPanel.add(copyButton);

        panel.add(buttonsPanel, gbc);
        return panel;
    }

    private JPanel createInputOutputPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Input Text",
            TitledBorder.LEFT,
            TitledBorder.TOP));
        inputTextArea = new JTextArea(8, 40);
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        inputPanel.add(new JScrollPane(inputTextArea), BorderLayout.CENTER);

        // Output panel
        JPanel outputPanel = new JPanel(new BorderLayout(5, 5));
        outputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Output Text",
            TitledBorder.LEFT,
            TitledBorder.TOP));
        outputTextArea = new JTextArea(8, 40);
        outputTextArea.setLineWrap(true);
        outputTextArea.setWrapStyleWord(true);
        outputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputTextArea.setEditable(false);
        outputTextArea.setBackground(new Color(240, 240, 240));
        outputPanel.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);

        panel.add(inputPanel);
        panel.add(outputPanel);
        return panel;
    }

    private void performEncryption() {
        try {
            String algorithm = algorithmField.getText().trim();
            String password = passwordField.getText().trim();
            String salt = saltField.getText().trim();
            String inputText = inputTextArea.getText().trim();

            if (algorithm.isEmpty() || password.isEmpty() || salt.isEmpty()) {
                showError("Please fill in all configuration fields (Algorithm, Password, Salt)");
                return;
            }
            if (inputText.isEmpty()) {
                showError("Please enter text to encrypt");
                return;
            }

            DevToolsSuiteEncryptor encryptor = new DevToolsSuiteEncryptor(algorithm, password, salt);
            String encrypted = encryptor.encrypt(inputText);
            outputTextArea.setText(encrypted);
        } catch (Exception ex) {
            String message = ex.getMessage();
            if (message != null && message.contains("algorithm")) {
                showError("Encryption Failed: Invalid algorithm\n\n" +
                    "Please use a valid algorithm like:\n" +
                    "• PBKDF2WithHmacSHA256\n" +
                    "• PBKDF2WithHmacSHA512");
            } else {
                showError("Encryption failed: " + message + "\n\n" +
                    "Please check your configuration settings.");
            }
            ex.printStackTrace();
        }
    }

    private void performDecryption() {
        try {
            String algorithm = algorithmField.getText().trim();
            String password = passwordField.getText().trim();
            String salt = saltField.getText().trim();
            String inputText = inputTextArea.getText().trim();

            if (algorithm.isEmpty() || password.isEmpty() || salt.isEmpty()) {
                showError("Please fill in all configuration fields (Algorithm, Password, Salt)");
                return;
            }
            if (inputText.isEmpty()) {
                showError("Please enter text to decrypt");
                return;
            }
            if (inputText.length() < 10) {
                showError("Input text is too short to be encrypted text.\n\n" +
                    "Make sure you're decrypting text that was previously encrypted,\n" +
                    "not plain text.");
                return;
            }

            DevToolsSuiteEncryptor encryptor = new DevToolsSuiteEncryptor(algorithm, password, salt);
            String decrypted = encryptor.decrypt(inputText);
            outputTextArea.setText(decrypted);
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage() != null && (ex.getMessage().contains("base64") || ex.getMessage().contains("byte"))) {
                showError("Decryption Failed: Invalid encrypted text format\n\n" +
                    "Possible causes:\n" +
                    "• You're trying to decrypt plain text (not encrypted text)\n" +
                    "• The encrypted text is incomplete or corrupted\n" +
                    "• Extra spaces or newlines in the encrypted text\n\n" +
                    "Solution: Make sure to paste the complete encrypted output.");
            } else {
                showError("Decryption failed: " + ex.getMessage());
            }
            ex.printStackTrace();
        } catch (Exception ex) {
            String message = ex.getMessage();
            if (message != null && message.toLowerCase().contains("bad padding")) {
                showError("Decryption Failed: Wrong password or salt\n\n" +
                    "The password and salt must match exactly\n" +
                    "what was used during encryption.");
            } else {
                showError("Decryption failed: " + message + "\n\n" +
                    "Make sure:\n" +
                    "• You're using the correct password and salt\n" +
                    "• The input is encrypted text (not plain text)");
            }
            ex.printStackTrace();
        }
    }

    private void clearFields() {
        inputTextArea.setText("");
        outputTextArea.setText("");
    }

    private void copyOutputToClipboard() {
        String outputText = outputTextArea.getText();
        if (outputText.isEmpty()) {
            showInfo("No output to copy");
            return;
        }
        StringSelection stringSelection = new StringSelection(outputText);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        showInfo("Output copied to clipboard!");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}
