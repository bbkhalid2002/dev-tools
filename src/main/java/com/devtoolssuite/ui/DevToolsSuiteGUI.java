package com.devtoolssuite.ui;

import com.jasypt.ui.tools.JsonViewerTool;
import com.jasypt.ui.tools.TextDiffTool;
import com.jasypt.ui.tools.JsonDiffTool;
import com.jasypt.ui.tools.TextStatsTool;
import com.jasypt.ui.tools.Base64Tool;
import com.jasypt.ui.tools.CaseConverterTool;
import com.devtoolssuite.ui.tools.DevToolsSuiteTool;
import javax.swing.*;
import java.awt.*;

/**
 * Main GUI application - Dev Tools Suite with multiple tools in tabs.
 * Includes: Encryptor, JSON Viewer, Text Diff, JSON Diff, Text Statistics, Base64, Case Converter
 */
public class DevToolsSuiteGUI extends JFrame {

    private JTabbedPane tabbedPane;

    public DevToolsSuiteGUI() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Dev Tools Suite");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Optional: set window/taskbar icon if a PNG is provided at /icons/dev_tools_suite.png
        try {
            java.net.URL iconUrl = getClass().getResource("/icons/dev_tools_suite.png");
            if (iconUrl != null) {
                Image img = Toolkit.getDefaultToolkit().getImage(iconUrl);
                if (img != null) {
                    setIconImage(img);
                }
            }
        } catch (Exception ignored) { }

        // Set size to 70% of screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * 0.7);
        int height = (int) (screenSize.height * 0.7);
        setSize(width, height);

        setLocationRelativeTo(null);

        // Create tabbed pane
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setTabPlacement(JTabbedPane.TOP);

        // Add all tools as tabs in the specified order
        tabbedPane.addTab("JSON Viewer", createTabIcon(), new JsonViewerTool(),
            "Parse and visualize JSON with tree structure");

        tabbedPane.addTab("Text Diff", createTabIcon(), new TextDiffTool(),
            "Compare two text documents line-by-line");

        tabbedPane.addTab("JSON Diff", createTabIcon(), new JsonDiffTool(),
            "Structured JSON comparison");

        tabbedPane.addTab("Text Statistics", createTabIcon(), new TextStatsTool(),
            "Analyze text for comprehensive metrics");

        tabbedPane.addTab("Jasypt Encryptor", createTabIcon(), new DevToolsSuiteTool(),
            "AES-GCM Encryption/Decryption");

        tabbedPane.addTab("Base64", createTabIcon(), new Base64Tool(),
            "Base64 Encoder/Decoder");

        tabbedPane.addTab("Case Converter", createTabIcon(), new CaseConverterTool(),
            "Convert text to 14 different case styles");

        add(tabbedPane, BorderLayout.CENTER);
    }

    private Icon createTabIcon() {
        // Create a small colored square icon for tabs (optional)
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                // Optional: draw a small icon
            }

            @Override
            public int getIconWidth() {
                return 0;
            }

            @Override
            public int getIconHeight() {
                return 0;
            }
        };
    }

    public static void main(String[] args) {
        // Set Look and Feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create and show GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            DevToolsSuiteGUI gui = new DevToolsSuiteGUI();
            gui.setVisible(true);
        });
    }
}
