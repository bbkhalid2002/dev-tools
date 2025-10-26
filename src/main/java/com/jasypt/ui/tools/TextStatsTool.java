package com.jasypt.ui.tools;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text Statistics tool - exactly replicates Python TextStatTab behavior.
 * Computes 14 metrics on text input.
 */
public class TextStatsTool extends JPanel {

	private JTextArea textArea;
	private JTable statsTable;
	private DefaultTableModel tableModel;
	private Timer updateTimer;

	private static final String[][] METRICS = {
		{"chars", "Characters"},
		{"bytes", "Size (bytes)"},
		{"words", "Words"},
		{"sentences", "Sentences"},
		{"paragraphs", "Paragraphs"},
		{"unique_words", "Unique words"},
		{"avg_word_len", "Avg. word length"},
		{"avg_sentence_len", "Avg. sentence length (words)"},
		{"upper", "Uppercase letters"},
		{"lower", "Lowercase letters"},
		{"digits", "Digits"},
		{"special", "Special characters"},
		{"whitespace", "Whitespace (space/tab/newline)"},
		{"punct", "Punctuation marks"}
	};

	private static final String PUNCTUATION = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";

	public TextStatsTool() {
		initializeUI();
	}

	private void initializeUI() {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 0, 0, 0));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerLocation(550);
		splitPane.setResizeWeight(0.6);

		// Left panel: text input
		JPanel leftPanel = new JPanel(new BorderLayout(6, 6));
		leftPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
		leftPanel.add(new JLabel("Text input"), BorderLayout.NORTH);

		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane textScroll = new JScrollPane(textArea);
		leftPanel.add(textScroll, BorderLayout.CENTER);

		// Right panel: statistics table
		JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
		rightPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
		rightPanel.add(new JLabel("Statistics"), BorderLayout.NORTH);

		String[] columns = {"Metric", "Value"};
		tableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		statsTable = new JTable(tableModel);
		statsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		// Zebra striping (alternating row colors)
		statsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (!isSelected) {
					if (row % 2 == 1) {
						c.setBackground(new Color(242, 242, 242));  // #f2f2f2 (odd - light grey)
					} else {
						c.setBackground(Color.WHITE);  // even - white
					}
				}
				return c;
			}
		});

		JScrollPane tableScroll = new JScrollPane(statsTable);
		rightPanel.add(tableScroll, BorderLayout.CENTER);

		splitPane.setLeftComponent(leftPanel);
		splitPane.setRightComponent(rightPanel);
		add(splitPane, BorderLayout.CENTER);

		// Initialize table with metrics
		for (String[] metric : METRICS) {
			tableModel.addRow(new Object[]{metric[1], "0"});
		}

		// Debounced update (200ms like Python)
		updateTimer = new Timer(200, e -> computeAndUpdate());
		updateTimer.setRepeats(false);

		// Add document listener
		textArea.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) { scheduleUpdate(); }
			public void removeUpdate(DocumentEvent e) { scheduleUpdate(); }
			public void changedUpdate(DocumentEvent e) { scheduleUpdate(); }
		});

		// Initial compute
		computeAndUpdate();
	}

	private void scheduleUpdate() {
		if (updateTimer.isRunning()) {
			updateTimer.restart();
		} else {
			updateTimer.start();
		}
	}

	private void computeAndUpdate() {
		String text = textArea.getText();

		// Basic counts
		int nChars = text.length();
		int nBytes = text.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;

		// Tokenize words
		List<String> words = tokenizeWords(text);
		int nWords = words.size();

		// Split sentences
		List<String> sentences = splitSentences(text);
		int nSentences = sentences.size();

		// Split paragraphs
		List<String> paragraphs = splitParagraphs(text);
		int nParagraphs = paragraphs.size();

		// Unique words (case-insensitive)
		Set<String> uniqueWordsSet = new HashSet<>();
		for (String w : words) {
			uniqueWordsSet.add(w.toLowerCase());
		}
		int uniqueWords = uniqueWordsSet.size();

		// Average word length
		double avgWordLen = 0.0;
		if (nWords > 0) {
			int totalLen = 0;
			for (String w : words) {
				totalLen += w.length();
			}
			avgWordLen = (double) totalLen / nWords;
		}

		// Average sentence length in words
		double avgSentenceLen = 0.0;
		if (nSentences > 0) {
			int totalWords = 0;
			for (String s : sentences) {
				totalWords += tokenizeWords(s).size();
			}
			avgSentenceLen = (double) totalWords / nSentences;
		}

		// Character classes
		int nUpper = 0, nLower = 0, nDigits = 0, nWhitespace = 0, nPunct = 0, nSpecial = 0;
		for (char c : text.toCharArray()) {
			if (Character.isUpperCase(c)) nUpper++;
			if (Character.isLowerCase(c)) nLower++;
			if (Character.isDigit(c)) nDigits++;
			if (Character.isWhitespace(c)) nWhitespace++;
			if (PUNCTUATION.indexOf(c) >= 0) nPunct++;
		}

		// Special characters: not alphanumeric, not whitespace, not punctuation
		for (char c : text.toCharArray()) {
			if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c) && PUNCTUATION.indexOf(c) < 0) {
				nSpecial++;
			}
		}

		// Update table
		Map<String, String> values = new LinkedHashMap<>();
		values.put("chars", String.valueOf(nChars));
		values.put("bytes", String.valueOf(nBytes));
		values.put("words", String.valueOf(nWords));
		values.put("sentences", String.valueOf(nSentences));
		values.put("paragraphs", String.valueOf(nParagraphs));
		values.put("unique_words", String.valueOf(uniqueWords));
		values.put("avg_word_len", String.format("%.2f", avgWordLen));
		values.put("avg_sentence_len", String.format("%.2f", avgSentenceLen));
		values.put("upper", String.valueOf(nUpper));
		values.put("lower", String.valueOf(nLower));
		values.put("digits", String.valueOf(nDigits));
		values.put("special", String.valueOf(nSpecial));
		values.put("whitespace", String.valueOf(nWhitespace));
		values.put("punct", String.valueOf(nPunct));

		for (int i = 0; i < METRICS.length; i++) {
			String key = METRICS[i][0];
			tableModel.setValueAt(values.get(key), i, 1);
		}
	}

	private List<String> tokenizeWords(String text) {
		// Words: Unicode word characters (\w = [a-zA-Z0-9_])
		List<String> words = new ArrayList<>();
		Pattern pattern = Pattern.compile("\\b\\w+\\b");
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			words.add(matcher.group());
		}
		return words;
	}

	private List<String> splitSentences(String text) {
		// Split on ., !, ? followed by whitespace
		String s = text.trim();
		if (s.isEmpty()) return new ArrayList<>();

		String[] parts = s.split("(?<=[.!?])\\s+");
		List<String> sentences = new ArrayList<>();
		for (String p : parts) {
			if (!p.isEmpty()) {
				sentences.add(p);
			}
		}
		return sentences;
	}

	private List<String> splitParagraphs(String text) {
		// Paragraphs separated by one or more blank lines
		String s = text.trim();
		if (s.isEmpty()) return new ArrayList<>();

		String[] parts = s.split("\\n\\s*\\n+");
		List<String> paragraphs = new ArrayList<>();
		for (String p : parts) {
			if (!p.trim().isEmpty()) {
				paragraphs.add(p);
			}
		}
		return paragraphs;
	}
}
