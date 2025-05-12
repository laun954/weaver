import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Weaver {

    public static final String GREEN = "\u001B[32m";
    public static final String GRAY = "\u001B[90m";
    public static final String RESET = "\u001B[0m";

    public static void main(String[] args) {
        // if (args.length != 2) {
        //     System.err.println("用法: java Weaver <起始词> <目标词>");
        //     System.exit(1);
        // }

        Set<String> wordSet = loadWordSet("words.txt");
        String startWord = "EAST";
        String targetWord = "WEST";

        validateWords(startWord, targetWord, wordSet);

        if (startWord.equals(targetWord)) {
            System.out.println("起始词和目标词相同，游戏胜利！步数：0");
            return;
        }

        playGame(startWord, targetWord, wordSet);
    }

    private static Set<String> loadWordSet(String filename) {
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

    private static void validateWords(String start, String target, Set<String> wordSet) {
        if (start.length() != 4 || target.length() != 4) {
            System.err.println("起始词和目标词必须为四字母。");
            System.exit(1);
        }
        if (!wordSet.contains(start)) {
            System.err.println("起始词无效。");
            System.exit(1);
        }
        if (!wordSet.contains(target)) {
            System.err.println("目标词无效。");
            System.exit(1);
        }
    }

    private static void playGame(String start, String target, Set<String> wordSet) {
        Scanner scanner = new Scanner(System.in);
        String currentWord = start;
        int steps = 0;

        while (true) {
            System.out.println("\n当前词: " + currentWord);
            System.out.println("目标词: " + target);
            System.out.print("请输入下一步的单词 (四字母): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals(target)) {
                if (isOneLetterDifference(currentWord, input)) {
                    steps++;
                    System.out.println(generateFeedback(input, target));
                    System.out.println("恭喜！你成功了，步数: " + steps);
                    break;
                } else {
                    System.out.println("错误：必须与当前词仅差一个字母。");
                    continue;
                }
            }

            if (input.length() != 4) {
                System.out.println("错误：单词必须为四字母。");
                continue;
            }

            if (!wordSet.contains(input)) {
                System.out.println("错误：无效的单词。");
                continue;
            }

            if (!isOneLetterDifference(currentWord, input)) {
                System.out.println("错误：必须与当前词仅差一个字母。");
                continue;
            }

            steps++;
            System.out.println("反馈: " + generateFeedback(input, target));
            currentWord = input;
        }
        scanner.close();
    }

    private static boolean isOneLetterDifference(String word1, String word2) {
        if (word1.length() != word2.length()) return false;
        int diff = 0;
        for (int i = 0; i < word1.length(); i++) {
            if (word1.charAt(i) != word2.charAt(i)) {
                diff++;
                if (diff > 1) return false;
            }
        }
        return diff == 1;
    }

    private static String generateFeedback(String input, String target) {
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
