package com.jasypt.ui.tools;

import com.google.gson.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * JSON Diff tool - exactly replicates Python JSONDiffTab behavior.
 * Structured JSON comparison showing differences in a table.
 */
public class JsonDiffTool extends JPanel {

	private JTextArea leftText;
	private JTextArea rightText;
	private JTable diffTable;
	private DefaultTableModel tableModel;
	private JLabel statusLabel;

	// Colors matching Python
	private static final Color ADDED_BG = new Color(234, 255, 234);    // #eaffea
	private static final Color REMOVED_BG = new Color(255, 236, 236);  // #ffecec
	private static final Color CHANGED_BG = new Color(255, 246, 213);  // #fff6d5
	private static final Color TYPE_BG = new Color(230, 240, 255);     // #e6f0ff

	public JsonDiffTool() {
		initializeUI();
	}

	private void initializeUI() {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 0, 0, 0));

		// Toolbar
		JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
		JButton formatBtn = new JButton("Format");
		formatBtn.addActionListener(e -> formatBoth());
		toolbar.add(formatBtn);

		JButton diffBtn = new JButton("Diff");
		diffBtn.addActionListener(e -> runDiff());
		toolbar.add(diffBtn);

		statusLabel = new JLabel("Paste JSON on both sides and click Diff");
		toolbar.add(Box.createHorizontalStrut(12));
		toolbar.add(statusLabel);

		add(toolbar, BorderLayout.NORTH);

		// Split pane for JSON editors
		JSplitPane jsonPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		jsonPane.setResizeWeight(0.5);
		jsonPane.setDividerLocation(0.5);

		// Left JSON
		JPanel leftPanel = new JPanel(new BorderLayout(6, 6));
		leftPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
		leftPanel.add(new JLabel("Left JSON (base)"), BorderLayout.NORTH);
		leftText = new JTextArea();
		leftText.setFont(new Font("Monospaced", Font.PLAIN, 12));
		leftText.setLineWrap(false);
		leftText.setTabSize(2);
		JScrollPane leftScroll = new JScrollPane(leftText);
		leftPanel.add(leftScroll, BorderLayout.CENTER);

		// Right JSON
		JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
		rightPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
		rightPanel.add(new JLabel("Right JSON (changed)"), BorderLayout.NORTH);
		rightText = new JTextArea();
		rightText.setFont(new Font("Monospaced", Font.PLAIN, 12));
		rightText.setLineWrap(false);
		rightText.setTabSize(2);
		JScrollPane rightScroll = new JScrollPane(rightText);
		rightPanel.add(rightScroll, BorderLayout.CENTER);

		jsonPane.setLeftComponent(leftPanel);
		jsonPane.setRightComponent(rightPanel);

		// Results table
		String[] columns = {"Path", "Change", "Left", "Right"};
		tableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		diffTable = new JTable(tableModel);
		diffTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		diffTable.getColumnModel().getColumn(0).setPreferredWidth(300);
		diffTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		diffTable.getColumnModel().getColumn(2).setPreferredWidth(350);
		diffTable.getColumnModel().getColumn(3).setPreferredWidth(350);

		// Custom renderer for row colors
		diffTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					String change = (String) tableModel.getValueAt(row, 1);
					switch (change) {
						case "added":
							c.setBackground(ADDED_BG);
							break;
						case "removed":
							c.setBackground(REMOVED_BG);
							break;
						case "changed":
							c.setBackground(CHANGED_BG);
							break;
						case "type":
							c.setBackground(TYPE_BG);
							break;
						default:
							c.setBackground(Color.WHITE);
					}
				}
				return c;
			}
		});

		JScrollPane tableScroll = new JScrollPane(diffTable);
		tableScroll.setPreferredSize(new Dimension(0, 250));

		// Main split pane
		JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jsonPane, tableScroll);
		mainSplit.setDividerLocation(400);
		mainSplit.setResizeWeight(0.6);

		add(mainSplit, BorderLayout.CENTER);
	}

	private void formatBoth() {
		List<String> errors = new ArrayList<>();
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();

		// Format left
		formatSide(leftText, "Left", gson, errors);

		// Format right
		formatSide(rightText, "Right", gson, errors);

		if (!errors.isEmpty()) {
			statusLabel.setText("Format errors: " + String.join(" | ", errors));
		} else {
			statusLabel.setText("Formatted JSON");
		}
	}

	private void formatSide(JTextArea textArea, String which, Gson gson, List<String> errors) {
		String raw = textArea.getText().trim();
		if (raw.isEmpty()) return;

		try {
			// Use extraction logic like JSON Viewer
			ParseResult result = extractAndLoadJson(raw);
			if (result.error != null || result.data == null) {
				errors.add(which + ": " + result.error);
				return;
			}
			String formatted = gson.toJson(result.data);
			textArea.setText(formatted);
		} catch (Exception e) {
			errors.add(which + ": " + e.getMessage());
		}
	}

	private void runDiff() {
		// Clear previous results
		tableModel.setRowCount(0);

		// Parse left
		JsonElement leftObj;
		try {
			leftObj = JsonParser.parseString(leftText.getText());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Left JSON parse error: " + e.getMessage(),
				"Left JSON Error", JOptionPane.ERROR_MESSAGE);
			statusLabel.setText("Left JSON parse error");
			return;
		}

		// Parse right
		JsonElement rightObj;
		try {
			rightObj = JsonParser.parseString(rightText.getText());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Right JSON parse error: " + e.getMessage(),
				"Right JSON Error", JOptionPane.ERROR_MESSAGE);
			statusLabel.setText("Right JSON parse error");
			return;
		}

		// Compute diff
		List<DiffRow> diffs = new ArrayList<>();
		diffRecursive(leftObj, rightObj, "$", diffs);

		if (diffs.isEmpty()) {
			statusLabel.setText("No differences");
			return;
		}

		// Populate table
		for (DiffRow row : diffs) {
			tableModel.addRow(new Object[]{row.path, row.change, row.left, row.right});
		}

		statusLabel.setText(diffs.size() + " difference(s)");
	}

	private void diffRecursive(JsonElement a, JsonElement b, String path, List<DiffRow> out) {
		// Type change
		if (!a.getClass().equals(b.getClass())) {
			out.add(new DiffRow("type", path, "type", getTypeName(a), getTypeName(b)));
			return;
		}

		// Object comparison
		if (a.isJsonObject() && b.isJsonObject()) {
			JsonObject aObj = a.getAsJsonObject();
			JsonObject bObj = b.getAsJsonObject();

			Set<String> aKeys = aObj.keySet();
			Set<String> bKeys = bObj.keySet();

			// Removed keys
			Set<String> removed = new TreeSet<>(aKeys);
			removed.removeAll(bKeys);
			for (String k : removed) {
				String p = path + "." + k;
				out.add(new DiffRow("removed", p, "removed", shortValue(aObj.get(k)), ""));
			}

			// Added keys
			Set<String> added = new TreeSet<>(bKeys);
			added.removeAll(aKeys);
			for (String k : added) {
				String p = path + "." + k;
				out.add(new DiffRow("added", p, "added", "", shortValue(bObj.get(k))));
			}

			// Common keys
			Set<String> common = new TreeSet<>(aKeys);
			common.retainAll(bKeys);
			for (String k : common) {
				diffRecursive(aObj.get(k), bObj.get(k), path + "." + k, out);
			}
			return;
		}

		// Array comparison (index-wise)
		if (a.isJsonArray() && b.isJsonArray()) {
			JsonArray aArr = a.getAsJsonArray();
			JsonArray bArr = b.getAsJsonArray();

			int maxLen = Math.max(aArr.size(), bArr.size());
			for (int i = 0; i < maxLen; i++) {
				String p = path + "[" + i + "]";
				if (i >= aArr.size()) {
					out.add(new DiffRow("added", p, "added", "", shortValue(bArr.get(i))));
				} else if (i >= bArr.size()) {
					out.add(new DiffRow("removed", p, "removed", shortValue(aArr.get(i)), ""));
				} else {
					diffRecursive(aArr.get(i), bArr.get(i), p, out);
				}
			}
			return;
		}

		// Primitive comparison
		if (!a.equals(b)) {
			out.add(new DiffRow("changed", path, "changed", shortValue(a), shortValue(b)));
		}
	}

	private String shortValue(JsonElement value) {
		return shortValue(value, 160);
	}

	private String shortValue(JsonElement value, int limit) {
		String s;
		if (value == null || value.isJsonNull()) {
			s = "null";
		} else {
			Gson gson = new Gson();
			s = gson.toJson(value);
		}

		if (s.length() > limit) {
			return s.substring(0, limit) + "…";
		}
		return s;
	}

	private String getTypeName(JsonElement elem) {
		if (elem.isJsonObject()) return "object";
		if (elem.isJsonArray()) return "array";
		if (elem.isJsonNull()) return "null";
		if (elem.isJsonPrimitive()) {
			JsonPrimitive p = elem.getAsJsonPrimitive();
			if (p.isString()) return "string";
			if (p.isBoolean()) return "boolean";
			if (p.isNumber()) return "number";
		}
		return "unknown";
	}

	// JSON Extraction Logic (same as JSON Viewer)
	private ParseResult extractAndLoadJson(String text) {
		String candidate = extractJsonBlock(text);

		if (candidate == null) {
			String t = text.trim();
			if ((t.startsWith("\"") && t.endsWith("\"")) || (t.startsWith("'") && t.endsWith("'"))) {
				try {
					JsonElement elem = JsonParser.parseString(t);
					if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) {
						String unquoted = elem.getAsString();
						String innerBlock = extractJsonBlock(unquoted);
						candidate = (innerBlock != null) ? innerBlock : unquoted;
					}
				} catch (Exception e) {
					candidate = t;
				}
			} else {
				return new ParseResult(null, "No JSON object/array found");
			}
		}

		VariantResult vr = tryParseVariants(candidate);
		if (vr.data != null) {
			return new ParseResult(vr.data, null);
		}

		return new ParseResult(null, "Unable to parse JSON from provided text");
	}

	private String extractJsonBlock(String text) {
		String obj = scanForBalancedBlock(text, '{', '}');
		String arr = scanForBalancedBlock(text, '[', ']');

		if (obj == null && arr == null) return null;
		if (obj == null) return arr;
		if (arr == null) return obj;

		int oidx = text.indexOf(obj);
		int aidx = text.indexOf(arr);
		return (oidx >= 0 && (aidx < 0 || oidx < aidx)) ? obj : arr;
	}

	private String scanForBalancedBlock(String text, char openCh, char closeCh) {
		int n = text.length();
		for (int i = 0; i < n; i++) {
			if (text.charAt(i) == openCh) {
				Integer end = findMatching(text, i, openCh, closeCh);
				if (end != null) {
					return text.substring(i, end + 1);
				}
			}
		}
		return null;
	}

	private Integer findMatching(String s, int start, char openCh, char closeCh) {
		int depth = 0;
		boolean inStr = false;
		boolean esc = false;

		for (int i = start; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (inStr) {
				if (esc) {
					esc = false;
				} else if (ch == '\\') {
					esc = true;
				} else if (ch == '"') {
					inStr = false;
				}
			} else {
				if (ch == '"') {
					inStr = true;
				} else if (ch == openCh) {
					depth++;
				} else if (ch == closeCh) {
					depth--;
					if (depth == 0) {
						return i;
					}
				}
			}
		}
		return null;
	}

	private VariantResult tryParseVariants(String base) {
		Set<String> tried = new HashSet<>();
		Queue<String> queue = new LinkedList<>();

		queue.add(base);
		tried.add(base);

		String s = base;
		for (int i = 0; i < 3; i++) {
			String ts = s.trim();
			if (ts.startsWith("\"") && ts.endsWith("\"")) {
				try {
					JsonElement elem = JsonParser.parseString(ts);
					if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) {
						s = elem.getAsString();
						if (!tried.contains(s)) {
							queue.add(s);
							tried.add(s);
						}
					} else {
						return new VariantResult(elem, null);
					}
				} catch (Exception e) {
					break;
				}
			} else {
				break;
			}
		}

		while (!queue.isEmpty()) {
			String cur = queue.poll();

			try {
				JsonElement data = JsonParser.parseString(cur);
				return new VariantResult(data, null);
			} catch (Exception e) {}

			try {
				String ue = decodeUnicodeEscape(cur);
				if (!ue.equals(cur)) {
					try {
						JsonElement data = JsonParser.parseString(ue);
						return new VariantResult(data, null);
					} catch (Exception e) {
						if (!tried.contains(ue)) {
							queue.add(ue);
							tried.add(ue);
						}
					}
				}
			} catch (Exception e) {}

			String collapsed = cur.replace("\\\\", "\\");
			if (!collapsed.equals(cur)) {
				try {
					JsonElement data = JsonParser.parseString(collapsed);
					return new VariantResult(data, null);
				} catch (Exception e) {
					if (!tried.contains(collapsed)) {
						queue.add(collapsed);
						tried.add(collapsed);
					}
				}
			}

			String unq = cur.replace("\\\"", "\"");
			if (!unq.equals(cur)) {
				try {
					JsonElement data = JsonParser.parseString(unq);
					return new VariantResult(data, null);
				} catch (Exception e) {
					if (!tried.contains(unq)) {
						queue.add(unq);
						tried.add(unq);
					}
				}
			}

			String combo = cur.replace("\\\\", "\\").replace("\\\"", "\"");
			if (!combo.equals(cur)) {
				try {
					JsonElement data = JsonParser.parseString(combo);
					return new VariantResult(data, null);
				} catch (Exception e) {
					if (!tried.contains(combo)) {
						queue.add(combo);
						tried.add(combo);
					}
				}
			}
		}

		return new VariantResult(null, "Unable to parse");
	}

	private String decodeUnicodeEscape(String s) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (i < s.length()) {
			if (i < s.length() - 5 && s.charAt(i) == '\\' && s.charAt(i+1) == 'u') {
				try {
					String hex = s.substring(i+2, i+6);
					int code = Integer.parseInt(hex, 16);
					sb.append((char) code);
					i += 6;
				} catch (Exception e) {
					sb.append(s.charAt(i));
					i++;
				}
			} else {
				sb.append(s.charAt(i));
				i++;
			}
		}
		return sb.toString();
	}

	// Helper classes
	private static class ParseResult {
		JsonElement data;
		String error;

		ParseResult(JsonElement data, String error) {
			this.data = data;
			this.error = error;
		}
	}

	private static class VariantResult {
		JsonElement data;
		String error;

		VariantResult(JsonElement data, String error) {
			this.data = data;
			this.error = error;
		}
	}

	// Helper class
	private static class DiffRow {
		String tag;
		String path;
		String change;
		String left;
		String right;

		DiffRow(String tag, String path, String change, String left, String right) {
			this.tag = tag;
			this.path = path;
			this.change = change;
			this.left = left;
			this.right = right;
		}
	}
}
