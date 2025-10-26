package com.jasypt.ui;

import com.jasypt.ui.tools.*;
import javax.swing.*;
import java.awt.*;

/**
 * Main GUI application - Dev Tools Suite with multiple tools in tabs.
 * Includes: Jasypt Encryptor, JSON Viewer, Text Diff, JSON Diff, Text Statistics, Base64, Case Converter
 */
public class JasyptGUI extends JFrame {

	private JTabbedPane tabbedPane;

	public JasyptGUI() {
		initializeUI();
	}

	private void initializeUI() {
		setTitle("Dev Tools Suite");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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

		tabbedPane.addTab("Jasypt Encryptor", createTabIcon(), new JasyptTool(),
			"Jasypt AES-GCM Encryption/Decryption");

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
			JasyptGUI gui = new JasyptGUI();
			gui.setVisible(true);
		});
	}
}
