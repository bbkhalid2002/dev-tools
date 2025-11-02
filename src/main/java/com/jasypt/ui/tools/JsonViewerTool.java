package com.jasypt.ui.tools;

import com.google.gson.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * JSON Viewer tool - exactly replicates Python JSONViewerFrame behavior.
 * Parses and visualizes JSON with tree structure, search, expand/collapse.
 */
public class JsonViewerTool extends JPanel {

	private JTree tree;
	private DefaultTreeModel treeModel;
	private JTextArea textArea;
	private JTextField searchField;
	private JLabel statusLabel;
	private Timer parseTimer;
	private Map<DefaultMutableTreeNode, Object> nodeValueMap;

	// Search state
	private String lastSearch = "";
	private List<TreePath> lastFoundPaths = new ArrayList<>();
	private int lastFoundIndex = -1;

	// For zebra striping
	private int rowIndex = 0;

	public JsonViewerTool() {
		initializeUI();
	}

	private void initializeUI() {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 0, 0, 0));

		// Create split pane
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setResizeWeight(0.5);
		splitPane.setDividerLocation(0.5);

		// Left panel: search + tree
		JPanel leftPanel = new JPanel(new BorderLayout(6, 6));
		leftPanel.setBorder(new EmptyBorder(6, 6, 6, 6));

		// Search row
		JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		searchRow.add(new JLabel("Search:"));
		searchField = new JTextField(20);
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					findNext();
				}
			}
		});
		searchRow.add(searchField);

		JButton expandAllBtn = new JButton("Expand All");
		expandAllBtn.addActionListener(e -> expandAll());
		searchRow.add(expandAllBtn);

		JButton collapseAllBtn = new JButton("Collapse All");
		collapseAllBtn.addActionListener(e -> collapseAll());
		searchRow.add(collapseAllBtn);

		leftPanel.add(searchRow, BorderLayout.NORTH);

		// Tree
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);

		// Tree popup menu
		JPopupMenu treeMenu = new JPopupMenu();
		JMenuItem copyItem = new JMenuItem("Copy Value");
		copyItem.addActionListener(e -> copySelectedValue());
		treeMenu.add(copyItem);

		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopup(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopup(e);
				}
			}

			private void showPopup(MouseEvent e) {
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (path != null) {
					tree.setSelectionPath(path);
					treeMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		// Ctrl+C for copy
		tree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C) {
					copySelectedValue();
					e.consume();
				}
			}
		});

		JScrollPane treeScroll = new JScrollPane(tree);
		leftPanel.add(treeScroll, BorderLayout.CENTER);

		// Right panel: text area
		JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
		rightPanel.setBorder(new EmptyBorder(6, 6, 6, 6));

		JPanel rightHeader = new JPanel(new BorderLayout());
		rightHeader.add(new JLabel("Paste text here (JSON will be auto-extracted):"), BorderLayout.WEST);
		JButton formatBtn = new JButton("Format JSON");
		formatBtn.addActionListener(e -> formatJson());
		rightHeader.add(formatBtn, BorderLayout.EAST);
		rightPanel.add(rightHeader, BorderLayout.NORTH);

		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

		// Add document listener for auto-parse with debouncing
		textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			public void insertUpdate(javax.swing.event.DocumentEvent e) { scheduleParse(); }
			public void removeUpdate(javax.swing.event.DocumentEvent e) { scheduleParse(); }
			public void changedUpdate(javax.swing.event.DocumentEvent e) { scheduleParse(); }
		});

		JScrollPane textScroll = new JScrollPane(textArea);
		rightPanel.add(textScroll, BorderLayout.CENTER);

		splitPane.setLeftComponent(leftPanel);
		splitPane.setRightComponent(rightPanel);
		add(splitPane, BorderLayout.CENTER);

		// Status bar
		statusLabel = new JLabel("Paste text to parse JSON…");
		statusLabel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
			new EmptyBorder(2, 5, 2, 5)
		));
		add(statusLabel, BorderLayout.SOUTH);

		nodeValueMap = new HashMap<>();
		parseTimer = new Timer(400, e -> parseAndRender());
		parseTimer.setRepeats(false);
	}

	private void scheduleParse() {
		if (parseTimer.isRunning()) {
			parseTimer.restart();
		} else {
			parseTimer.start();
		}
	}

	private void parseAndRender() {
		String raw = textArea.getText().trim();
		if (raw.isEmpty()) {
			setStatus("Paste text to parse JSON…");
			clearTree();
			return;
		}

		ParseResult result = extractAndLoadJson(raw);
		if (result.error != null) {
			setStatus("Parse failed: " + result.error);
			clearTree();
			return;
		}

		setStatus("Parsed JSON successfully");
		populateTree(result.data);
	}

	private ParseResult extractAndLoadJson(String text) {
		// First, try to parse the text directly with variants (handles escaped JSON)
		VariantResult directResult = tryParseVariants(text.trim());
		if (directResult.data != null) {
			return new ParseResult(directResult.data, null);
		}

		// Try to locate the first valid JSON object/array
		String candidate = extractJsonBlock(text);

		// If no block found with original text, try with unescaped quotes
		if (candidate == null) {
			String unescaped = text.replace("\\\"", "\"");
			if (!unescaped.equals(text)) {
				candidate = extractJsonBlock(unescaped);
			}
		}

		// Fallback: if no block found, try unwrapping quoted string
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

		// Try parsing with variants
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

		// Prefer earliest occurrence
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

		// Try unwrapping quoted strings up to 3 levels
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

		// Try various transformations
		while (!queue.isEmpty()) {
			String cur = queue.poll();

			// Try direct parse
			try {
				JsonElement data = JsonParser.parseString(cur);
				return new VariantResult(data, null);
			} catch (Exception e) {}

			// Try unicode escape decoding
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

			// Try collapsing double backslashes
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

			// Try unescaping quotes
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

			// Try combined: collapse + unescape
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
		// Simple unicode escape decoder - converts \\uXXXX sequences
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

	private void clearTree() {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
		root.removeAllChildren();
		treeModel.reload();
		nodeValueMap.clear();
	}

	private void populateTree(JsonElement data) {
		clearTree();
		rowIndex = 0;
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
		insertNode(root, "root", data);
		treeModel.reload();

		// Expand first level
		if (root.getChildCount() > 0) {
			tree.expandPath(new TreePath(new Object[]{root, root.getChildAt(0)}));
		}
	}

	private void insertNode(DefaultMutableTreeNode parent, String key, JsonElement value) {
		if (value.isJsonObject()) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(key + " : {...}");
			parent.add(node);
			nodeValueMap.put(node, value);
			rowIndex++;

			JsonObject obj = value.getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
				insertNode(node, entry.getKey(), entry.getValue());
			}
		} else if (value.isJsonArray()) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(key + " : [ … ]");
			parent.add(node);
			nodeValueMap.put(node, value);
			rowIndex++;

			JsonArray arr = value.getAsJsonArray();
			for (int i = 0; i < arr.size(); i++) {
				insertNode(node, "[" + i + "]", arr.get(i));
			}
		} else {
			String disp = primitiveToString(value);
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(key + " : " + disp);
			parent.add(node);
			nodeValueMap.put(node, value);
			rowIndex++;
		}
	}

	private String primitiveToString(JsonElement v) {
		if (v.isJsonNull()) return "null";
		if (v.isJsonPrimitive()) {
			JsonPrimitive p = v.getAsJsonPrimitive();
			if (p.isString()) return p.getAsString();
			if (p.isBoolean()) return String.valueOf(p.getAsBoolean());
			if (p.isNumber()) return p.getAsNumber().toString();
		}
		return v.toString();
	}

	private void formatJson() {
		String raw = textArea.getText().trim();
		if (raw.isEmpty()) {
			JOptionPane.showMessageDialog(this, "The provided JSON is invalid or empty.",
				"Format JSON", JOptionPane.ERROR_MESSAGE);
			return;
		}

		ParseResult result = extractAndLoadJson(raw);
		if (result.error != null || result.data == null) {
			JOptionPane.showMessageDialog(this,
				"The provided JSON is invalid.\n\n" +
				"Tip: If you have escaped JSON like {\\\"key\\\":\\\"value\\\"}, " +
				"it should now be auto-converted to valid JSON.",
				"Format JSON", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		String pretty = gson.toJson(result.data);
		textArea.setText(pretty);
		setStatus("Formatted JSON");
		populateTree(result.data);
	}

	private void findNext() {
		String query = searchField.getText().trim().toLowerCase();
		if (query.isEmpty()) return;

		// Build list of matching paths if query changed
		if (!query.equals(lastSearch)) {
			lastSearch = query;
			lastFoundPaths = collectMatches(query);
			lastFoundIndex = -1;
		}

		if (lastFoundPaths.isEmpty()) {
			setStatus("No matches");
			return;
		}

		lastFoundIndex = (lastFoundIndex + 1) % lastFoundPaths.size();
		TreePath path = lastFoundPaths.get(lastFoundIndex);
		tree.setSelectionPath(path);
		tree.scrollPathToVisible(path);
		setStatus("Match " + (lastFoundIndex + 1) + "/" + lastFoundPaths.size());
	}

	private List<TreePath> collectMatches(String query) {
		List<TreePath> matches = new ArrayList<>();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();

		for (int i = 0; i < root.getChildCount(); i++) {
			collectMatchesRecursive((DefaultMutableTreeNode) root.getChildAt(i),
				new TreePath(new Object[]{root, root.getChildAt(i)}), query, matches);
		}

		return matches;
	}

	private void collectMatchesRecursive(DefaultMutableTreeNode node, TreePath path,
			String query, List<TreePath> matches) {
		String text = node.getUserObject().toString().toLowerCase();
		if (text.contains(query)) {
			matches.add(path);
		}

		for (int i = 0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
			collectMatchesRecursive(child, path.pathByAddingChild(child), query, matches);
		}
	}

	private void expandAll() {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
		for (int i = 0; i < root.getChildCount(); i++) {
			expandRecursive(new TreePath(new Object[]{root, root.getChildAt(i)}), true);
		}
		setStatus("Expanded all");
	}

	private void collapseAll() {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
		for (int i = 0; i < root.getChildCount(); i++) {
			expandRecursive(new TreePath(new Object[]{root, root.getChildAt(i)}), false);
		}
		setStatus("Collapsed all");
	}

	private void expandRecursive(TreePath path, boolean expand) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

		// When collapsing, collapse children first, then parent
		// When expanding, expand parent first, then children
		if (!expand) {
			for (int i = 0; i < node.getChildCount(); i++) {
				expandRecursive(path.pathByAddingChild(node.getChildAt(i)), expand);
			}
		}

		if (expand) {
			tree.expandPath(path);
		} else {
			tree.collapsePath(path);
		}

		if (expand) {
			for (int i = 0; i < node.getChildCount(); i++) {
				expandRecursive(path.pathByAddingChild(node.getChildAt(i)), expand);
			}
		}
	}

	private void copySelectedValue() {
		TreePath path = tree.getSelectionPath();
		if (path == null) return;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		Object value = nodeValueMap.get(node);

		String text;
		if (value == null) {
			text = node.getUserObject().toString();
		} else if (value instanceof JsonObject || value instanceof JsonArray) {
			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
			text = gson.toJson(value);
		} else if (value instanceof JsonPrimitive) {
			JsonPrimitive p = (JsonPrimitive) value;
			if (p.isString()) {
				text = p.getAsString();
			} else {
				text = p.toString();
			}
		} else {
			text = value.toString();
		}

		StringSelection selection = new StringSelection(text);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, null);
		setStatus("Copied value to clipboard");
	}

	private void setStatus(String msg) {
		statusLabel.setText(msg);
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
}
