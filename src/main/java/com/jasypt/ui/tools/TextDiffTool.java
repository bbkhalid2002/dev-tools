package com.jasypt.ui.tools;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Text Diff tool - exactly replicates Python TextDiffTab behavior.
 * Two-pane text comparison with line-by-line diff highlighting.
 */
public class TextDiffTool extends JPanel {

	private JTextPane leftText;
	private JTextPane rightText;
	private Timer diffTimer;

	// Highlight colors matching Python
	private static final Color REMOVED_BG = new Color(255, 236, 236);  // #ffecec
	private static final Color REMOVED_FG = new Color(164, 0, 0);      // #a40000
	private static final Color ADDED_BG = new Color(234, 255, 234);    // #eaffea
	private static final Color ADDED_FG = new Color(0, 100, 0);        // #006400

	public TextDiffTool() {
		initializeUI();
	}

	private void initializeUI() {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 0, 0, 0));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setResizeWeight(0.5);
		splitPane.setDividerLocation(0.5);

		// Left pane
		JPanel leftPanel = new JPanel(new BorderLayout(6, 6));
		leftPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
		JLabel leftLabel = new JLabel("Left (base)");
		leftPanel.add(leftLabel, BorderLayout.NORTH);

		leftText = new JTextPane();
		leftText.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane leftScroll = new JScrollPane(leftText);
		leftPanel.add(leftScroll, BorderLayout.CENTER);

		// Right pane
		JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
		rightPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
		JLabel rightLabel = new JLabel("Right (changed)");
		rightPanel.add(rightLabel, BorderLayout.NORTH);

		rightText = new JTextPane();
		rightText.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane rightScroll = new JScrollPane(rightText);
		rightPanel.add(rightScroll, BorderLayout.CENTER);

		splitPane.setLeftComponent(leftPanel);
		splitPane.setRightComponent(rightPanel);
		add(splitPane, BorderLayout.CENTER);

		// Debounced diff computation (250ms like Python)
		diffTimer = new Timer(250, e -> computeAndHighlight());
		diffTimer.setRepeats(false);

		// Add document listeners
		DocumentListener docListener = new DocumentListener() {
			public void insertUpdate(DocumentEvent e) { scheduleDiff(); }
			public void removeUpdate(DocumentEvent e) { scheduleDiff(); }
			public void changedUpdate(DocumentEvent e) { scheduleDiff(); }
		};

		leftText.getDocument().addDocumentListener(docListener);
		rightText.getDocument().addDocumentListener(docListener);

		// Sync scrolling (rough sync on mouse wheel)
		leftScroll.getVerticalScrollBar().addAdjustmentListener(e -> {
			if (!e.getValueIsAdjusting()) {
				rightScroll.getVerticalScrollBar().setValue(e.getValue());
			}
		});
	}

	private void scheduleDiff() {
		if (diffTimer.isRunning()) {
			diffTimer.restart();
		} else {
			diffTimer.start();
		}
	}

	private void computeAndHighlight() {
		String leftContent = leftText.getText();
		String rightContent = rightText.getText();

		String[] leftLines = leftContent.split("\n", -1);
		String[] rightLines = rightContent.split("\n", -1);

		// Clear previous highlights
		clearHighlights(leftText);
		clearHighlights(rightText);

		// Compute diff using simple SequenceMatcher-like algorithm
		List<DiffOp> ops = computeDiff(leftLines, rightLines);

		// Apply highlights
		int leftLine = 0;
		int rightLine = 0;

		for (DiffOp op : ops) {
			switch (op.tag) {
				case EQUAL:
					leftLine += (op.i2 - op.i1);
					rightLine += (op.j2 - op.j1);
					break;
				case DELETE:
					for (int i = op.i1; i < op.i2; i++) {
						highlightLine(leftText, leftLine, REMOVED_BG, REMOVED_FG);
						leftLine++;
					}
					break;
				case INSERT:
					for (int j = op.j1; j < op.j2; j++) {
						highlightLine(rightText, rightLine, ADDED_BG, ADDED_FG);
						rightLine++;
					}
					break;
				case REPLACE:
					for (int i = op.i1; i < op.i2; i++) {
						highlightLine(leftText, leftLine, REMOVED_BG, REMOVED_FG);
						leftLine++;
					}
					for (int j = op.j1; j < op.j2; j++) {
						highlightLine(rightText, rightLine, ADDED_BG, ADDED_FG);
						rightLine++;
					}
					break;
			}
		}
	}

	private void clearHighlights(JTextPane textPane) {
		StyledDocument doc = textPane.getStyledDocument();
		Style defaultStyle = textPane.getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setBackground(defaultStyle, Color.WHITE);
		StyleConstants.setForeground(defaultStyle, Color.BLACK);
		doc.setCharacterAttributes(0, doc.getLength(), defaultStyle, true);
	}

	private void highlightLine(JTextPane textPane, int lineNumber, Color bg, Color fg) {
		try {
			StyledDocument doc = textPane.getStyledDocument();
			Element root = doc.getDefaultRootElement();
			if (lineNumber >= root.getElementCount()) return;

			Element line = root.getElement(lineNumber);
			int start = line.getStartOffset();
			int end = line.getEndOffset();

			Style style = textPane.addStyle("highlight", null);
			StyleConstants.setBackground(style, bg);
			StyleConstants.setForeground(style, fg);
			doc.setCharacterAttributes(start, end - start, style, false);
		} catch (Exception e) {
			// Ignore
		}
	}

	// Simple diff algorithm (similar to Python's difflib.SequenceMatcher)
	private List<DiffOp> computeDiff(String[] a, String[] b) {
		List<DiffOp> result = new ArrayList<>();
		int[][] dp = new int[a.length + 1][b.length + 1];

		// Build LCS table
		for (int i = 1; i <= a.length; i++) {
			for (int j = 1; j <= b.length; j++) {
				if (a[i-1].equals(b[j-1])) {
					dp[i][j] = dp[i-1][j-1] + 1;
				} else {
					dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
				}
			}
		}

		// Backtrack to find operations
		int i = a.length, j = b.length;
		List<DiffOp> ops = new ArrayList<>();

		while (i > 0 || j > 0) {
			if (i > 0 && j > 0 && a[i-1].equals(b[j-1])) {
				// Find start of equal block
				int i2 = i, j2 = j;
				while (i > 0 && j > 0 && a[i-1].equals(b[j-1])) {
					i--; j--;
				}
				ops.add(new DiffOp(OpTag.EQUAL, i, i2, j, j2));
			} else if (j > 0 && (i == 0 || dp[i][j-1] >= dp[i-1][j])) {
				// Find start of insert block
				int j2 = j;
				while (j > 0 && (i == 0 || dp[i][j-1] >= dp[i-1][j])) {
					j--;
					if (i > 0 && j > 0 && a[i-1].equals(b[j-1])) break;
				}
				ops.add(new DiffOp(OpTag.INSERT, i, i, j, j2));
			} else if (i > 0) {
				// Find start of delete block
				int i2 = i;
				while (i > 0 && (j == 0 || dp[i-1][j] >= dp[i][j-1])) {
					i--;
					if (i > 0 && j > 0 && a[i-1].equals(b[j-1])) break;
				}
				ops.add(new DiffOp(OpTag.DELETE, i, i2, j, j));
			}
		}

		Collections.reverse(ops);

		// Merge adjacent operations and convert delete+insert to replace
		List<DiffOp> merged = new ArrayList<>();
		for (int k = 0; k < ops.size(); k++) {
			DiffOp op = ops.get(k);
			if (k < ops.size() - 1) {
				DiffOp next = ops.get(k + 1);
				if (op.tag == OpTag.DELETE && next.tag == OpTag.INSERT) {
					merged.add(new DiffOp(OpTag.REPLACE, op.i1, op.i2, next.j1, next.j2));
					k++; // Skip next
					continue;
				}
			}
			merged.add(op);
		}

		return merged;
	}

	// Helper classes
	private enum OpTag { EQUAL, DELETE, INSERT, REPLACE }

	private static class DiffOp {
		OpTag tag;
		int i1, i2, j1, j2;

		DiffOp(OpTag tag, int i1, int i2, int j1, int j2) {
			this.tag = tag;
			this.i1 = i1;
			this.i2 = i2;
			this.j1 = j1;
			this.j2 = j2;
		}
	}
}
