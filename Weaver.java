import java.io.*;
import java.util.*;

class GameConfig {
    public boolean showError = true;
    public boolean showPath = true;
    public boolean randomWords = false;
}

interface FeedbackStrategy {
    String generateFeedback(String input, String target);
}

class ColorFeedbackStrategy implements FeedbackStrategy {
    public static final String GREEN = "\u001B[32m";
    public static final String GRAY  = "\u001B[90m";
    public static final String RESET = "\u001B[0m";

    @Override
    public String generateFeedback(String input, String target) {
        StringBuilder feedback = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == target.charAt(i)) {
                feedback.append(GREEN).append(c).append(RESET);
            } else if (target.indexOf(c) == -1) {
                feedback.append(GRAY).append(c).append(RESET);
            } else {
                feedback.append(c);
            }
        }
        return feedback.toString();
    }
}

abstract class WordGame {
    protected String startWord;
    protected String targetWord;
    protected Set<String> wordSet;
    protected int steps;
    protected GameConfig config;

    public WordGame(String start, String target, Set<String> wordSet, GameConfig config) {
        this.startWord = start.toUpperCase();
        this.targetWord = target.toUpperCase();
        this.wordSet = wordSet;
        this.steps = 0;
        this.config = config;
    }

    public final void play() {
        if (!validateWords()) {
            System.err.println("非法的起始词或目标词！");
            return;
        }

        if (startWord.equals(targetWord)) {
            System.out.println("起始词和目标词相同，游戏胜利！步数：0");
            return;
        }

        if (config.showPath) {
            List<String> path = findAnyPath(startWord, targetWord);
            if (path != null) {
                System.out.println("（调试）起始词到目标词的路径： " + String.join(" -> ", path));
            } else {
                System.out.println("（调试）找不到从起始词到目标词的路径。");
            }
        }

        gameLoop();
    }

    protected abstract boolean validateWords();
    protected abstract void gameLoop();

    protected boolean isOneLetterDifference(String a, String b) {
        if (a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) diff++;
            if (diff > 1) return false;
        }
        return diff == 1;
    }

    // 非最短路径搜索，只找一条合法路径即可
    protected List<String> findAnyPath(String start, String target) {
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
}

class WeaverGame extends WordGame {
    private FeedbackStrategy feedbackStrategy;

    public WeaverGame(String start, String target, Set<String> wordSet, FeedbackStrategy strategy, GameConfig config) {
        super(start, target, wordSet, config);
        this.feedbackStrategy = strategy;
    }

    @Override
    protected boolean validateWords() {
        return startWord.length() == 4 && targetWord.length() == 4 &&
                wordSet.contains(startWord) && wordSet.contains(targetWord);
    }

    @Override
    protected void gameLoop() {
        Scanner scanner = new Scanner(System.in);
        String currentWord = startWord;

        while (true) {
            System.out.println("\n当前词: " + currentWord);
            System.out.println("目标词: " + targetWord);
            System.out.print("请输入下一步的单词 (四字母): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals(targetWord)) {
                if (isOneLetterDifference(currentWord, input)) {
                    steps++;
                    System.out.println("反馈: " + feedbackStrategy.generateFeedback(input, targetWord));
                    System.out.println("恭喜！你成功了，步数: " + steps);
                    break;
                } else if (config.showError) {
                    System.out.println("错误：必须与当前词仅差一个字母。");
                }
                continue;
            }

            if (input.length() != 4 || !wordSet.contains(input) || !isOneLetterDifference(currentWord, input)) {
                if (config.showError) {
                    System.out.println("错误：无效输入或不是仅差一个字母的单词。");
                }
                continue;
            }

            steps++;
            System.out.println("反馈: " + feedbackStrategy.generateFeedback(input, targetWord));
            currentWord = input;
        }

        scanner.close();
    }
}

class WordListLoader {
    public static Set<String> loadWordSet(String filename) {
        Set<String> wordSet = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().toUpperCase();
                if (line.length() == 4) {
                    wordSet.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("无法读取单词列表文件: " + e.getMessage());
            System.exit(1);
        }
        return wordSet;
    }
}

public class Weaver {
    public static void main(String[] args) {
        GameConfig config = new GameConfig();

        // ✅ 在这里配置标志
        config.showError = true;
        config.showPath = true;
        config.randomWords = false;

        Set<String> wordSet = WordListLoader.loadWordSet("dictionary.txt");

        String startWord = "EAST";
        String targetWord = "WEST";

        if (config.randomWords) {
            List<String> list = new ArrayList<>(wordSet);
            Random rand = new Random();
            do {
                startWord = list.get(rand.nextInt(list.size()));
                targetWord = list.get(rand.nextInt(list.size()));
            } while (startWord.equals(targetWord) || !isPathExists(startWord, targetWord, wordSet));
        }

        FeedbackStrategy strategy = new ColorFeedbackStrategy();
        WordGame game = new WeaverGame(startWord, targetWord, wordSet, strategy, config);
        game.play();
    }

    // 检查是否存在路径（避免陷入死局）
    private static boolean isPathExists(String start, String target, Set<String> wordSet) {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(target)) return true;

            for (String word : wordSet) {
                if (!visited.contains(word) && isOneLetterDiff(word, current)) {
                    queue.add(word);
                    visited.add(word);
                }
            }
        }
        return false;
    }

    private static boolean isOneLetterDiff(String a, String b) {
        if (a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) diff++;
            if (diff > 1) return false;
        }
        return diff == 1;
    }
}
