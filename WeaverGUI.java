import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

// Model
class WeaverModel {
    public enum LetterStatus { CORRECT, PRESENT, ABSENT }

    private String startWord;
    private String targetWord;
    private String currentWord;
    private int steps;
    private Set<String> wordSet;
    private Set<Character> usedLetters = new HashSet<>();
    private List<LetterStatus> lastGuessStatus;

    public WeaverModel(String start, String target, Set<String> words) {
        this.startWord = start.toUpperCase();
        this.targetWord = target.toUpperCase();
        this.currentWord = this.startWord;
        this.wordSet = words;
    }

    public boolean tryWord(String word) {
        if (isValidWord(word) && isOneLetterDifference(currentWord, word)) {
            currentWord = word.toUpperCase();
            steps++;
            updateUsedLetters(word);
            calculateLetterStatus(word);
            return true;
        }
        return false;
    }

    private void calculateLetterStatus(String word) {
        lastGuessStatus = new ArrayList<>();
        String upperWord = word.toUpperCase();
        for (int i = 0; i < upperWord.length(); i++) {
            char c = upperWord.charAt(i);
            if (targetWord.charAt(i) == c) {
                lastGuessStatus.add(LetterStatus.CORRECT);
            } else if (targetWord.indexOf(c) != -1) {
                lastGuessStatus.add(LetterStatus.PRESENT);
            } else {
                lastGuessStatus.add(LetterStatus.ABSENT);
            }
        }
    }

    private void updateUsedLetters(String word) {
        for (char c : word.toCharArray()) {
            usedLetters.add(Character.toUpperCase(c));
        }
    }

    public void reset() {
        currentWord = startWord;
        steps = 0;
        usedLetters.clear();
        lastGuessStatus = null;
    }

    public boolean isWin() {
        return currentWord.equalsIgnoreCase(targetWord);
    }

    private boolean isValidWord(String word) {
        return word != null && word.length() == 4 && wordSet.contains(word.toUpperCase());
    }

    private boolean isOneLetterDifference(String a, String b) {
        if (a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) diff++;
            if (diff > 1) return false;
        }
        return diff == 1;
    }

    // Getters
    public String getCurrentWord() { return currentWord; }
    public String getTargetWord() { return targetWord; }
    public int getSteps() { return steps; }
    public Set<Character> getUsedLetters() { return usedLetters; }
    public List<LetterStatus> getLastGuessStatus() { return lastGuessStatus; }
    public Set<String> getWordSet() { return wordSet; }
}

// View
class WeaverView {
    private JFrame frame;
    private JPanel gameGridPanel;
    private JPanel keyboardPanel;
    private JButton[] letterButtons = new JButton[26];
    private JButton resetButton;
    private JButton newGameButton;
    private JLabel statusLabel;
    public JLabel inputDisplayLabel;
    public WeaverModel model;

    private JLabel startWordLabel;
    private JLabel targetWordLabel;
    // 回退按钮
    private JButton backspaceButton;

    public WeaverView() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Weaver Game");
        frame.setLayout(new BorderLayout(10, 10));

        // 状态
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        statusLabel = new JLabel("开始游戏!", SwingConstants.CENTER);
        inputDisplayLabel = new JLabel("", SwingConstants.CENTER);
        inputDisplayLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(statusLabel);
        topPanel.add(inputDisplayLabel);
        frame.add(topPanel, BorderLayout.NORTH);


        // 格子
        gameGridPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        gameGridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(gameGridPanel);
        frame.add(scrollPane, BorderLayout.CENTER);

        // 键盘
        keyboardPanel = new JPanel(new GridLayout(3, 9, 5, 5));
        initializeKeyboard();
        frame.add(keyboardPanel, BorderLayout.SOUTH);

        // 控制面板
        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        resetButton = new JButton("重置游戏");
        resetButton.setEnabled(false);
        newGameButton = new JButton("新游戏");
        controlPanel.add(resetButton);
        controlPanel.add(newGameButton);
        frame.add(controlPanel, BorderLayout.EAST);

        frame.setPreferredSize(new Dimension(600, 700));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    private void initializeKeyboard() {
        String[] keys = {
            "Q","W","E","R","T","Y","U","I","O","P",
            "A","S","D","F","G","H","J","K","L",
            "Z","X","C","V","B","N","M","⌫"  // 最后添加删除符号
        };

        keyboardPanel = new JPanel(new GridLayout(4, 9, 5, 5));
        for (String key : keys) {
            JButton btn = new JButton(key);
            btn.setBackground(Color.DARK_GRAY);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            if (key.equals("⌫")) {
                backspaceButton = btn;  // 保存删除按钮引用
            } else {
                letterButtons[key.charAt(0) - 'A'] = btn;
            }
            keyboardPanel.add(btn);
        }
    }

    public void addGuessRow(String guess, List<WeaverModel.LetterStatus> statuses) {
        JPanel rowPanel = new JPanel(new GridLayout(1, 4, 2, 2));
        for (int i = 0; i < guess.length(); i++) {
            JLabel label = new JLabel(String.valueOf(guess.charAt(i)), SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBackground(getColorForStatus(statuses.get(i)));
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            label.setPreferredSize(new Dimension(50, 50));
            rowPanel.add(label);
        }
        gameGridPanel.add(rowPanel);
        gameGridPanel.revalidate();
    }

    private Color getColorForStatus(WeaverModel.LetterStatus status) {
        switch (status) {
            case CORRECT: return Color.GREEN;
            case PRESENT: return Color.YELLOW;
            case ABSENT: return Color.GRAY;
            default: return Color.LIGHT_GRAY;
        }
    }

    public void showWinMessage() {
        JOptionPane.showMessageDialog(frame, "恭喜你赢了！步数: " + (model.getSteps()));
    }

    public void updateStatus(int steps, boolean isWin) {
        String text = isWin ? "胜利！步数: " + steps : "步数: " + steps;
        statusLabel.setText(text);
    }

    public void resetGameView() {
        gameGridPanel.removeAll();
        inputDisplayLabel.setText("");
        statusLabel.setText("开始新游戏!");
        frame.revalidate();
        frame.repaint();
    }

    public JButton[] getLetterButtons() { return letterButtons; }
    public JButton getResetButton() { return resetButton; }
    public JButton getNewGameButton() { return newGameButton; }
    public JFrame getFrame() { return frame; }
    public void show() { frame.setVisible(true); frame.requestFocus(); }
    // 添加获取删除按钮的方法
    public JButton getBackspaceButton() {
        return backspaceButton;
    }
}

// Controller
class WeaverController {
    private WeaverModel model;
    private WeaverView view;
    private String currentInput = "";

    public WeaverController(WeaverModel model, WeaverView view) {
        this.model = model;
        this.view = view;
        setupListeners();
        updateInputDisplay();
    }

    private void setupListeners() {
        // 虚拟键盘
        for (JButton btn : view.getLetterButtons()) {
            btn.addActionListener(e -> handleLetterInput(btn.getText()));
        }
        // 添加删除按钮监听
        view.getBackspaceButton().addActionListener(e -> handleBackspace());


        // 修改物理键盘监听
        view.getFrame().addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    handleBackspace();
                }
            }

            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (Character.isLetter(c)) {
                    handleLetterInput(String.valueOf(c).toUpperCase());
                }
            }
        });

        // Control buttons
        view.getResetButton().addActionListener(e -> handleReset());
        view.getNewGameButton().addActionListener(e -> handleNewGame());
    }

    private void handleLetterInput(String letter) {
        if (currentInput.length() < 4) {
            currentInput += letter.toUpperCase();
            updateInputDisplay();
        }

        if (currentInput.length() == 4) {
            boolean valid = model.tryWord(currentInput);
            if (valid) {
                if (model.getSteps() == 1) view.getResetButton().setEnabled(true);
                checkGameState();
            } else {
                JOptionPane.showMessageDialog(view.getFrame(), "无效的单词或无法转换！");
            }
            currentInput = "";
            updateInputDisplay();
        }
    }

    private void handleBackspace() {
        if (!currentInput.isEmpty()) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            updateInputDisplay();
        }
    }

    private void updateInputDisplay() {
        // 添加删除效果的显示更新
        String displayText = currentInput;
        while (displayText.length() < 4) {
            displayText += "_";  // 用下划线表示待输入位置
        }
        view.inputDisplayLabel.setText(
            "<html><font color='gray'>" + displayText + "</font></html>");
    }

    private void checkGameState() {
        view.addGuessRow(model.getCurrentWord(), model.getLastGuessStatus());
        view.updateStatus(model.getSteps(), model.isWin());
        if (model.isWin()) {
            view.showWinMessage();
        }
    }

    private void handleReset() {
        model.reset();
        view.resetGameView();
        view.getResetButton().setEnabled(false);
    }

    private void handleNewGame() {
        Set<String> words = model.getWordSet();
        String[] wordsArray = words.toArray(new String[0]);
        if (wordsArray.length < 2) {
            JOptionPane.showMessageDialog(view.getFrame(), "单词列表不足！");
            return;
        }
        Random rand = new Random();
        String newStart = wordsArray[rand.nextInt(wordsArray.length)];
        String newTarget;
        do {
            newTarget = wordsArray[rand.nextInt(wordsArray.length)];
        } while (newStart.equals(newTarget));

        model = new WeaverModel(newStart, newTarget, words);
        view.resetGameView();
        view.getResetButton().setEnabled(false);
        updateInputDisplay();
    }
}

public class WeaverGUI {
    public static void main(String[] args) {
        Set<String> words = loadWordList("words.txt");
        SwingUtilities.invokeLater(() -> {
            WeaverModel model = new WeaverModel("EAST", "WEST", words);
            WeaverView view = new WeaverView();
            view.model = model;
            new WeaverController(model, view);
            view.show();
        });
    }

    private static Set<String> loadWordList(String filename) {
        Set<String> words = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().toUpperCase();
                if (line.length() == 4) {
                    words.add(line);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "无法加载单词列表: " + e.getMessage());
        }
        return words;
    }
}
