import java.util.Set;
import java.util.HashSet;
import java.util.List;

public class WeaverTest {
    private Set<String> testWords;
    private WeaverModel model;

    public static void main(String[] args) {
        WeaverTest test = new WeaverTest();
        test.setUp();
        test.runAllTests();
    }

    private void setUp() {
        // 创建测试用的单词集合
        testWords = new HashSet<>();
        testWords.add("WEST");
        testWords.add("EAST");
        testWords.add("TEST");
        testWords.add("REST");
        testWords.add("NEST");
        testWords.add("PEST");
        testWords.add("BEST");
        testWords.add("CAST");
        testWords.add("COST");
        testWords.add("LOST");
        testWords.add("MOST");
        testWords.add("POST");
        testWords.add("ROST");
        testWords.add("TOST");
        testWords.add("WOST");
        testWords.add("YOST");
        testWords.add("ZOST");

        // 初始化模型
        model = new WeaverModel("WEST", "EAST", testWords, new DefaultLetterStatusStrategy());
    }

    private void runAllTests() {
        System.out.println("开始运行Weaver游戏测试...\n");

        testModelInitialization();
        testValidWordTransition();
        testInvalidWordTransition();
        testMultipleLetterDifference();
        testWinCondition();
        testReset();
        testLetterStatus();
        testFindPath();
        testUsedLetters();

        System.out.println("\n所有测试完成！");
    }

    private void testModelInitialization() {
        System.out.println("测试模型初始化...");
        boolean passed = true;
        
        if (!"WEST".equals(model.getStartWord())) {
            System.out.println("错误: 起始单词应该是 WEST");
            passed = false;
        }
        if (!"EAST".equals(model.getTargetWord())) {
            System.out.println("错误: 目标单词应该是 EAST");
            passed = false;
        }
        if (!"WEST".equals(model.getCurrentWord())) {
            System.out.println("错误: 当前单词应该是 WEST");
            passed = false;
        }
        if (model.getSteps() != 0) {
            System.out.println("错误: 初始步数应该是 0");
            passed = false;
        }
        if (model.isWin()) {
            System.out.println("错误: 初始状态不应该是胜利");
            passed = false;
        }

        System.out.println(passed ? "通过" : "失败");
    }

    private void testValidWordTransition() {
        System.out.println("\n测试有效单词转换...");
        boolean passed = true;

        if (!model.tryWord("TEST")) {
            System.out.println("错误: 应该可以转换到 TEST");
            passed = false;
        }
        if (!"TEST".equals(model.getCurrentWord())) {
            System.out.println("错误: 当前单词应该是 TEST");
            passed = false;
        }
        if (model.getSteps() != 1) {
            System.out.println("错误: 步数应该是 1");
            passed = false;
        }
        if (model.isWin()) {
            System.out.println("错误: 不应该已经胜利");
            passed = false;
        }

        System.out.println(passed ? "通过" : "失败");
    }

    private void testInvalidWordTransition() {
        System.out.println("\n测试无效单词转换...");
        boolean passed = true;

        if (model.tryWord("INVALID")) {
            System.out.println("错误: 不应该接受无效单词");
            passed = false;
        }
        if (!"WEST".equals(model.getCurrentWord())) {
            System.out.println("错误: 当前单词应该保持为 WEST");
            passed = false;
        }
        if (model.getSteps() != 0) {
            System.out.println("错误: 步数应该保持为 0");
            passed = false;
        }

        System.out.println(passed ? "通过" : "失败");
    }

    private void testMultipleLetterDifference() {
        System.out.println("\n测试多字母差异...");
        boolean passed = true;

        if (model.tryWord("CAST")) {
            System.out.println("错误: 不应该接受多字母差异的单词");
            passed = false;
        }
        if (!"WEST".equals(model.getCurrentWord())) {
            System.out.println("错误: 当前单词应该保持为 WEST");
            passed = false;
        }
        if (model.getSteps() != 0) {
            System.out.println("错误: 步数应该保持为 0");
            passed = false;
        }

        System.out.println(passed ? "通过" : "失败");
    }

    private void testWinCondition() {
        System.out.println("\n测试胜利条件...");
        boolean passed = true;

        if (!model.tryWord("TEST")) {
            System.out.println("错误: 应该可以转换到 TEST");
            passed = false;
        }
        if (!model.tryWord("REST")) {
            System.out.println("错误: 应该可以转换到 REST");
            passed = false;
        }
        if (!model.tryWord("EAST")) {
            System.out.println("错误: 应该可以转换到 EAST");
            passed = false;
        }
        if (!model.isWin()) {
            System.out.println("错误: 应该已经胜利");
            passed = false;
        }
        if (model.getSteps() != 3) {
            System.out.println("错误: 步数应该是 3");
            passed = false;
        }

        System.out.println(passed ? "通过" : "失败");
    }

    private void testReset() {
        System.out.println("\n测试重置功能...");
        boolean passed = true;

        model.tryWord("TEST");
        model.reset();

        if (!"WEST".equals(model.getCurrentWord())) {
            System.out.println("错误: 重置后当前单词应该是 WEST");
            passed = false;
        }
        if (model.getSteps() != 0) {
            System.out.println("错误: 重置后步数应该是 0");
            passed = false;
        }
        if (model.isWin()) {
            System.out.println("错误: 重置后不应该是胜利状态");
            passed = false;
        }

        System.out.println(passed ? "通过" : "失败");
    }

    private void testLetterStatus() {
        System.out.println("\n测试字母状态...");
        boolean passed = true;

        model.tryWord("TEST");
        List<WeaverModel.LetterStatus> statuses = model.getLastGuessStatus();

        if (statuses == null) {
            System.out.println("错误: 状态列表不应该为null");
            passed = false;
        }
        if (statuses.size() != 4) {
            System.out.println("错误: 状态列表大小应该是 4");
            passed = false;
        }
        if (statuses.get(0) != WeaverModel.LetterStatus.CORRECT) {
            System.out.println("错误: 第一个字母(T)应该是正确的");
            passed = false;
        }
        if (statuses.get(1) != WeaverModel.LetterStatus.ABSENT) {
            System.out.println("错误: 第二个字母(E)应该是不存在的");
            passed = false;
        }
        if (statuses.get(2) != WeaverModel.LetterStatus.ABSENT) {
            System.out.println("错误: 第三个字母(S)应该是不存在的");
            passed = false;
        }
        if (statuses.get(3) != WeaverModel.LetterStatus.CORRECT) {
            System.out.println("错误: 第四个字母(T)应该是正确的");
            passed = false;
        }

        System.out.println(passed ? "通过" : "失败");
    }

    private void testFindPath() {
        System.out.println("\n测试路径查找...");
        boolean passed = true;

        List<String> path = model.findAnyPath("WEST", "EAST");

        if (path == null) {
            System.out.println("错误: 应该能找到路径");
            passed = false;
        }
        if (path.size() < 3) {
            System.out.println("错误: 路径长度应该至少为 3");
            passed = false;
        }
        if (!"WEST".equals(path.get(0))) {
            System.out.println("错误: 路径起点应该是 WEST");
            passed = false;
        }
        if (!"EAST".equals(path.get(path.size() - 1))) {
            System.out.println("错误: 路径终点应该是 EAST");
            passed = false;
        }

        // 验证路径中的每一步
        for (int i = 1; i < path.size(); i++) {
            String prev = path.get(i - 1);
            String curr = path.get(i);
            if (!isOneLetterDifference(prev, curr)) {
                System.out.println("错误: 路径中的相邻单词应该只相差一个字母");
                passed = false;
            }
            if (!testWords.contains(curr)) {
                System.out.println("错误: 路径中的单词应该在字典中");
                passed = false;
            }
        }

        System.out.println(passed ? "通过" : "失败");
    }

    private void testUsedLetters() {
        System.out.println("\n测试已使用字母...");
        boolean passed = true;

        model.tryWord("TEST");
        Set<Character> usedLetters = model.getUsedLetters();

        if (!usedLetters.contains('T')) {
            System.out.println("错误: 应该包含字母 T");
            passed = false;
        }
        if (!usedLetters.contains('E')) {
            System.out.println("错误: 应该包含字母 E");
            passed = false;
        }
        if (!usedLetters.contains('S')) {
            System.out.println("错误: 应该包含字母 S");
            passed = false;
        }

        System.out.println(passed ? "通过" : "失败");
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
} 