import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Scanner;

// 控制标志类
class WeaverSettings {
    public static boolean showError = true;
    public static boolean showPath = false;
    public static boolean randomWords = false;
}

interface LetterStatusStrategy {
    List<WeaverModel.LetterStatus> calculateStatus(String guess, String target);
}

class DefaultLetterStatusStrategy implements LetterStatusStrategy {
    @Override
    public List<WeaverModel.LetterStatus> calculateStatus(String guess, String target) {
        List<WeaverModel.LetterStatus> statusList = new ArrayList<>();
        for (int i = 0; i < guess.length(); i++) {
            char c = guess.charAt(i);
            if (target.charAt(i) == c) {
                statusList.add(WeaverModel.LetterStatus.CORRECT);
            } else if (target.indexOf(c) != -1) {
                statusList.add(WeaverModel.LetterStatus.PRESENT);
            } else {
                statusList.add(WeaverModel.LetterStatus.ABSENT);
            }
        }
        return statusList;
    }
}

interface WeaverModelFactory {
    WeaverModel createModel(Set<String> wordSet);
}

class RandomWeaverModelFactory implements WeaverModelFactory {
    public WeaverModel createModel(Set<String> wordSet) {
        String[] wordsArray = wordSet.toArray(new String[0]);
        Random rand = new Random();
        String newStart = wordsArray[rand.nextInt(wordsArray.length)];
        String newTarget;
        do {
            newTarget = wordsArray[rand.nextInt(wordsArray.length)];
        } while (newStart.equals(newTarget));
        return new WeaverModel(newStart, newTarget, wordSet, new DefaultLetterStatusStrategy());
    }
}

class WeaverModel {
    public enum LetterStatus { CORRECT, PRESENT, ABSENT }

    private String startWord;
    private String targetWord;
    private String currentWord;
    private int steps;
    private Set<String> wordSet;
    private Set<Character> usedLetters = new HashSet<>();
    private List<LetterStatus> lastGuessStatus;
    private LetterStatusStrategy statusStrategy;

    public WeaverModel(String start, String target, Set<String> words, LetterStatusStrategy strategy) {
        this.startWord = start.toUpperCase();
        this.targetWord = target.toUpperCase();
        this.currentWord = this.startWord;
        this.wordSet = words;
        this.statusStrategy = strategy;
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
        lastGuessStatus = statusStrategy.calculateStatus(word.toUpperCase(), targetWord);
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

    public List<String> findAnyPath(String start, String target) {
        Queue<List<String>> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.offer(Arrays.asList(start));
        visited.add(start);

        while (!queue.isEmpty()) {
            List<String> path = queue.poll();
            String last = path.get(path.size() - 1);
            if (last.equals(target)) return path;

            for (String word : wordSet) {
                if (!visited.contains(word) && isOneLetterDifference(last, word)) {
                    List<String> newPath = new ArrayList<>(path);
                    newPath.add(word);
                    queue.offer(newPath);
                    visited.add(word);
                }
            }
        }
        return null;
    }

    public String getStartWord() { return startWord; }
    public String getCurrentWord() { return currentWord; }
    public String getTargetWord() { return targetWord; }
    public int getSteps() { return steps; }
    public Set<Character> getUsedLetters() { return usedLetters; }
    public List<LetterStatus> getLastGuessStatus() { return lastGuessStatus; }
    public Set<String> getWordSet() { return wordSet; }
}

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
    private JButton backspaceButton;
    private JLabel wordPairLabel;
    public JCheckBox showErrorCheckbox;
    public JCheckBox showPathCheckbox;
    public JCheckBox randomWordsCheckbox;
    private KeyListener keyboardListener;
    private WeaverController controller;

    public WeaverView() {
        initializeUI();
    }

    public void setController(WeaverController controller) {
        this.controller = controller;
        setupKeyboardListener();
    }

    private void setupKeyboardListener() {
        KeyListener keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                
                // 处理特殊键
                if (keyCode == KeyEvent.VK_BACK_SPACE) {
                    backspaceButton.doClick();
                    return;
                }
                
                // 处理Enter键
                if (keyCode == KeyEvent.VK_ENTER) {
                    controller.handleEnterKey();
                    return;
                }
                
                // 处理字母e
                if (keyCode == KeyEvent.VK_E) {
                    letterButtons['E' - 'A'].doClick();
                    return;
                }
                
                // 处理其他字母键
                if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
                    int index = keyCode - KeyEvent.VK_A;
                    if (letterButtons[index] != null) {
                        letterButtons[index].doClick();
                    }
                }
            }
        };
        
        frame.addKeyListener(keyListener);
        keyboardPanel.addKeyListener(keyListener);
        inputDisplayLabel.addKeyListener(keyListener);
        wordPairLabel.addKeyListener(keyListener);
        
        // 确保所有组件都能接收键盘事件
        frame.setFocusable(true);
        keyboardPanel.setFocusable(true);
        inputDisplayLabel.setFocusable(true);
        wordPairLabel.setFocusable(true);
    }

    private void initializeUI() {
        frame = new JFrame("Weaver Game");
        frame.setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new GridLayout(3, 1));
        statusLabel = new JLabel("开始游戏!", SwingConstants.CENTER);
        inputDisplayLabel = new JLabel("", SwingConstants.CENTER);
        inputDisplayLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(statusLabel);
        topPanel.add(inputDisplayLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        gameGridPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        JScrollPane scrollPane = new JScrollPane(gameGridPanel);
        frame.add(scrollPane, BorderLayout.CENTER);

        initializeKeyboard();
        frame.add(keyboardPanel, BorderLayout.SOUTH);

        JPanel controlPanel = new JPanel(new GridLayout(2, 1));
        resetButton = new JButton("重置游戏");
        resetButton.setEnabled(false);
        newGameButton = new JButton("新游戏");
        controlPanel.add(resetButton);
        controlPanel.add(newGameButton);
        frame.add(controlPanel, BorderLayout.EAST);

        JPanel settingsPanel = new JPanel(new GridLayout(3, 1));
        showErrorCheckbox = new JCheckBox("显示错误提示", WeaverSettings.showError);
        showPathCheckbox = new JCheckBox("显示路径信息", WeaverSettings.showPath);
        randomWordsCheckbox = new JCheckBox("随机词");

        settingsPanel.add(showErrorCheckbox);
        settingsPanel.add(showPathCheckbox);
        settingsPanel.add(randomWordsCheckbox);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(controlPanel, BorderLayout.NORTH);
        rightPanel.add(settingsPanel, BorderLayout.SOUTH);

        frame.add(rightPanel, BorderLayout.EAST);

        wordPairLabel = new JLabel("", SwingConstants.CENTER);
        wordPairLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(wordPairLabel);

        frame.setPreferredSize(new Dimension(600, 700));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 确保所有面板都能接收键盘事件
        gameGridPanel.setFocusable(true);
        keyboardPanel.setFocusable(true);
        frame.setFocusable(true);
        frame.requestFocusInWindow();
    }

    private void initializeKeyboard() {
        String[] keys = {
                "Q","W","E","R","T","Y","U","I","O","P",
                "A","S","D","F","G","H","J","K","L",
                "Z","X","C","V","B","N","M","⌫"
        };
        keyboardPanel = new JPanel(new GridLayout(4, 9, 5, 5));
        for (String key : keys) {
            JButton btn = new JButton(key);
            btn.setBackground(Color.DARK_GRAY);
            btn.setForeground(Color.WHITE);
            if (key.equals("⌫")) {
                backspaceButton = btn;
            } else {
                letterButtons[key.charAt(0) - 'A'] = btn;
            }
            keyboardPanel.add(btn);
        }
    }

    public void addGuessRow(String guess, List<WeaverModel.LetterStatus> statuses) {
        JPanel rowPanel = new JPanel(new GridLayout(1, 4));
        for (int i = 0; i < guess.length(); i++) {
            JLabel label = new JLabel(String.valueOf(guess.charAt(i)), SwingConstants.CENTER);
            label.setOpaque(true);
            switch (statuses.get(i)) {
                case CORRECT:
                    label.setBackground(Color.GREEN);
                    break;
                case PRESENT:
                    label.setBackground(Color.YELLOW);
                    break;
                case ABSENT:
                    label.setBackground(Color.GRAY);
                    break;
            }
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            rowPanel.add(label);
        }
        gameGridPanel.add(rowPanel);
        gameGridPanel.revalidate();
    }

    public void showWinMessage() {
        JOptionPane.showMessageDialog(frame, "恭喜你赢了！步数: " + model.getSteps());
    }

    public void updateStatus(int steps, boolean isWin) {
        statusLabel.setText(isWin ? "胜利！步数: " + steps : "步数: " + steps);
    }

    public void resetGameView() {
        gameGridPanel.removeAll();
        inputDisplayLabel.setText("");
        statusLabel.setText("开始新游戏!");
        frame.revalidate();
        frame.repaint();
    }

    public void updateWordPair(String start, String target) {
        wordPairLabel.setText(start + "   →   " + target);
    }

    public JButton[] getLetterButtons() { return letterButtons; }
    public JButton getResetButton() { return resetButton; }
    public JButton getNewGameButton() { return newGameButton; }
    public JButton getBackspaceButton() { return backspaceButton; }
    public JFrame getFrame() { return frame; }
    public void show() { frame.setVisible(true); }
}

class WeaverController {
    private WeaverModel model;
    private WeaverView view;
    private String currentInput = "";

    public WeaverController(WeaverModel model, WeaverView view) {
        this.model = model;
        this.view = view;
        view.setController(this);
        setupListeners();
        updateInputDisplay();
        view.updateWordPair(model.getStartWord(), model.getTargetWord());
    }

    private void showHintPath() {
        List<String> path = model.findAnyPath(model.getStartWord(), model.getTargetWord());
        if (path != null) {
            StringBuilder message = new StringBuilder("提示路径：\n");
            for (String step : path) {
                message.append(step).append(" → ");
            }
            message.setLength(message.length() - 3); // 移除最后的箭头
            JOptionPane.showMessageDialog(view.getFrame(), message.toString());
        } else {
            JOptionPane.showMessageDialog(view.getFrame(), "未能找到有效路径！");
        }
    }

    private void setupListeners() {
        // 虚拟键盘点击
        for (JButton btn : view.getLetterButtons()) {
            if (btn != null) {
                btn.addActionListener(e -> handleLetterInput(btn.getText()));
            }
        }

        // 退格键按钮
        view.getBackspaceButton().addActionListener(e -> handleBackspace());

        // 控制按钮
        view.getResetButton().addActionListener(e -> handleReset());
        view.getNewGameButton().addActionListener(e -> handleNewGame());

        // 设置复选框
        view.showErrorCheckbox.addItemListener(e -> WeaverSettings.showError = view.showErrorCheckbox.isSelected());
        view.showPathCheckbox.addItemListener(e -> {
            WeaverSettings.showPath = view.showPathCheckbox.isSelected();
            if (WeaverSettings.showPath) {
                showHintPath();  // 勾选时立即显示路径
            }
        });
        view.randomWordsCheckbox.addItemListener(e -> WeaverSettings.randomWords = view.randomWordsCheckbox.isSelected());
    }

    private void handleLetterInput(String letter) {
        if (currentInput.length() < 4) {
            currentInput += letter.toUpperCase();
            updateInputDisplay();
        }
        if (currentInput.length() == 4) {
            handleEnter();
        }
    }

    private void handleBackspace() {
        if (!currentInput.isEmpty()) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            updateInputDisplay();
        }
    }

    private void updateInputDisplay() {
        String displayText = currentInput;
        while (displayText.length() < 4) {
            displayText += "_";
        }
        view.inputDisplayLabel.setText("<html><font color='gray'>" + displayText + "</font></html>");
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
        if (words.size() < 2) {
            JOptionPane.showMessageDialog(view.getFrame(), "单词列表不足！");
            return;
        }
        WeaverModelFactory factory = new RandomWeaverModelFactory();
        model = WeaverSettings.randomWords
                ? factory.createModel(words)
                : new WeaverModel("WEST", "EAST", words, new DefaultLetterStatusStrategy());
        view.model = model;
        view.resetGameView();
        view.getResetButton().setEnabled(false);
        updateInputDisplay();
        view.updateWordPair(model.getStartWord(), model.getTargetWord());
    }

    public void handleEnterKey() {
        if (currentInput.length() == 4) {
            handleEnter();
        }
    }

    private void handleEnter() {
        if (currentInput.length() == 4) {
            boolean valid = model.tryWord(currentInput);
            if (valid) {
                if (model.getSteps() == 1) view.getResetButton().setEnabled(true);
                checkGameState();
            } else {
                if (WeaverSettings.showError) {
                    JOptionPane.showMessageDialog(view.getFrame(), "无效的单词或无法转换！");
                }
            }
            currentInput = "";
            updateInputDisplay();
        }
    }
}

public class WeaverGUI {
    public static void main(String[] args) {
        Set<String> words = loadWordList("dictionary.txt");
        SwingUtilities.invokeLater(() -> {
            WeaverModel model = WeaverSettings.randomWords
                    ? new RandomWeaverModelFactory().createModel(words)
                    : new WeaverModel("WEST", "EAST", words, new DefaultLetterStatusStrategy());
            WeaverView view = new WeaverView();
            view.model = model;
            new WeaverController(model, view);
            view.show();

            if (WeaverSettings.showPath) {
                System.out.println("[DEBUG] 从 " + model.getStartWord() + " 到 " + model.getTargetWord());
            }
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

abstract class WeaverGameTemplate {
    public final void playGame() {
        init();
        while (!isWin()) {
            String input = getInput();
            processInput(input);
            showFeedback();
        }
        showWin();
    }
    protected abstract void init();
    protected abstract String getInput();
    protected abstract void processInput(String input);
    protected abstract void showFeedback();
    protected abstract boolean isWin();
    protected abstract void showWin();
}

class WeaverGUIGame extends WeaverGameTemplate {
    private WeaverModel model;
    private WeaverView view;
    private String latestInput = "";

    @Override
    protected void init() {
        // Implementation
    }

    @Override
    protected String getInput() {
        return latestInput;
    }

    @Override
    protected void processInput(String input) {
        model.tryWord(input);
    }

    @Override
    protected void showFeedback() {
        // Implementation
    }

    @Override
    protected boolean isWin() {
        return model.isWin();
    }

    @Override
    protected void showWin() {
        // Implementation
    }
} 