import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Random;

public class GameAIApp {
    private JFrame frame;
    private JTextArea statusArea;
    private JButton startButton, stopButton;
    private boolean isRunning = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameAIApp::new);
    }

    public GameAIApp() {
        setupUI();
    }

    private void setupUI() {
        frame = new JFrame("AI Game Controller");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        statusArea = new JTextArea();
        statusArea.setEditable(false);

        JPanel controlPanel = new JPanel();
        startButton = new JButton("Start AI");
        stopButton = new JButton("Stop AI");
        stopButton.setEnabled(false);

        controlPanel.add(startButton);
        controlPanel.add(stopButton);

        startButton.addActionListener(new StartAIListener());
        stopButton.addActionListener(new StopAIListener());

        frame.add(new JScrollPane(statusArea), BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private class StartAIListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isRunning) {
                isRunning = true;
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                statusArea.append("AI started...\n");
                new Thread(GameAIApp.this::runAI).start();
            }
        }
    }

    private class StopAIListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            isRunning = false;
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            statusArea.append("AI stopped.\n");
        }
    }

    private void runAI() {
        try {
            Robot robot = new Robot();
            Random random = new Random();

            while (isRunning) {
                // Simulate key presses (e.g., WASD)
                int action = random.nextInt(4);
                switch (action) {
                    case 0 -> simulateKeyPress(robot, KeyEvent.VK_W); // Move up
                    case 1 -> simulateKeyPress(robot, KeyEvent.VK_A); // Move left
                    case 2 -> simulateKeyPress(robot, KeyEvent.VK_S); // Move down
                    case 3 -> simulateKeyPress(robot, KeyEvent.VK_D); // Move right
                }

                // Simulate learning logic here (state analysis, reward feedback)
                statusArea.append("AI action: " + (char) ('W' + action) + "\n");

                // Pause to mimic real gameplay speed
                Thread.sleep(200);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void simulateKeyPress(Robot robot, int keyCode) {
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
    }
}
