import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;
import java.util.HashSet;

public class WeaverGUITest {
    public static void main(String[] args) {
        // 创建测试用的单词集合
        Set<String> testWords = new HashSet<>();
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

        // 启动GUI测试
        SwingUtilities.invokeLater(() -> {
            // 创建模型
            WeaverModel model = new WeaverModel("WEST", "EAST", testWords, new DefaultLetterStatusStrategy());
            
            // 创建视图
            WeaverView view = new WeaverView();
            view.model = model;
            
            // 创建控制器
            WeaverController controller = new WeaverController(model, view);
            
            // 设置自动关闭对话框
            setupAutoCloseDialogs();
            
            // 显示窗口
            view.show();

            // 测试键盘输入
            testKeyboardInput(view);
        });
    }

    private static void setupAutoCloseDialogs() {
        // 创建一个计时器来检查和关闭对话框
        Timer dialogTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Window[] windows = Window.getWindows();
                for (Window window : windows) {
                    if (window instanceof JDialog) {
                        JDialog dialog = (JDialog) window;
                        if (dialog.isVisible() && 
                            dialog.getTitle() != null && 
                            dialog.getTitle().equals("Message")) {
                            // 找到对话框中的确定按钮并点击
                            for (Component comp : dialog.getContentPane().getComponents()) {
                                if (comp instanceof JOptionPane) {
                                    JOptionPane pane = (JOptionPane) comp;
                                    for (Component button : ((Container) pane.getComponent(1)).getComponents()) {
                                        if (button instanceof JButton) {
                                            JButton jButton = (JButton) button;
                                            if ("OK".equals(jButton.getText())) {
                                                jButton.doClick();
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        dialogTimer.start();
    }

    private static void testKeyboardInput(WeaverView view) {
        // 创建一个定时器来模拟键盘输入
        Timer timer = new Timer(1000, new ActionListener() {
            private int step = 0;
            private String[] testInputs = {
                "CAST",  // 无效转换
                "TEST",  // 有效转换
                "REST",  // 有效转换
                "EAST"   // 有效转换
            };

            @Override
            public void actionPerformed(ActionEvent e) {
                if (step < testInputs.length) {
                    // 模拟键盘输入
                    String word = testInputs[step];
                    System.out.println("测试输入: " + word);
                    
                    for (char c : word.toCharArray()) {
                        int keyCode = KeyEvent.VK_A + (Character.toUpperCase(c) - 'A');
                        KeyEvent keyEvent = new KeyEvent(
                            view.getFrame(),
                            KeyEvent.KEY_PRESSED,
                            System.currentTimeMillis(),
                            0,
                            keyCode,
                            c
                        );
                        view.getFrame().dispatchEvent(keyEvent);
                    }
                    
                    // 模拟按下回车键
                    KeyEvent enterEvent = new KeyEvent(
                        view.getFrame(),
                        KeyEvent.KEY_PRESSED,
                        System.currentTimeMillis(),
                        0,
                        KeyEvent.VK_ENTER,
                        KeyEvent.CHAR_UNDEFINED
                    );
                    view.getFrame().dispatchEvent(enterEvent);
                    
                    step++;
                } else {
                    ((Timer)e.getSource()).stop();
                }
            }
        });
        timer.start();
    }
} 