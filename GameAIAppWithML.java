import org.tensorflow.Tensor;
import org.tensorflow.SavedModelBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.nio.FloatBuffer;
import java.util.Random;

public class GameAIAppWithML {
    private JFrame frame;
    private JTextArea statusArea;
    private JButton startButton, stopButton;
    private boolean isRunning = false;
    private SavedModelBundle model;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameAIAppWithML::new);
    }

    public GameAIAppWithML() {
        loadModel();
        setupUI();
    }

    private void loadModel() {
        try {
            model = SavedModelBundle.load("path/to/saved_model");
            System.out.println("Model loaded successfully!");
        } catch (Exception e) {
            System.err.println("Error loading TensorFlow model: " + e.getMessage());
        }
    }

    private void setupUI() {
        frame = new JFrame("AI Game Controller with ML");
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
                new Thread(GameAIAppWithML.this::runAI).start();
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

            while (isRunning) {
                // Capture the game state
                BufferedImage screenshot = captureGameState();

                // Process the image into a tensor
                Tensor<Float> inputTensor = preprocessImage(screenshot);

                // Run inference
                float[] output = predictAction(inputTensor);

                // Choose the action based on the model's output
                int action = selectAction(output);
                executeAction(robot, action);

                statusArea.append("AI action: " + actionToKey(action) + "\n");

                // Pause to mimic real gameplay speed
                Thread.sleep(200);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private BufferedImage captureGameState() {
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        try {
            Robot robot = new Robot();
            return robot.createScreenCapture(screenRect);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Tensor<Float> preprocessImage(BufferedImage image) {
        int width = 128; // Target width for the model
        int height = 128; // Target height for the model
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();

        float[] pixelData = new float[width * height * 3];
        int index = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = resizedImage.getRGB(x, y);
                pixelData[index++] = ((rgb >> 16) & 0xFF) / 255.0f; // Red
                pixelData[index++] = ((rgb >> 8) & 0xFF) / 255.0f;  // Green
                pixelData[index++] = (rgb & 0xFF) / 255.0f;         // Blue
            }
        }

        return Tensor.create(new long[]{1, width, height, 3}, FloatBuffer.wrap(pixelData));
    }

    private float[] predictAction(Tensor<Float> inputTensor) {
        try (Tensor<Float> outputTensor = model.session().runner()
                .feed("input_tensor", inputTensor)
                .fetch("output_tensor")
                .run()
                .get(0)
                .expect(Float.class)) {
            float[] output = new float[(int) outputTensor.shape()[1]];
            outputTensor.copyTo(output);
            return output;
        }
    }

    private int selectAction(float[] output) {
        int action = 0;
        float maxValue = output[0];
        for (int i = 1; i < output.length; i++) {
            if (output[i] > maxValue) {
                maxValue = output[i];
                action = i;
            }
        }
        return action;
    }

    private void executeAction(Robot robot, int action) {
        int keyCode = switch (action) {
            case 0 -> KeyEvent.VK_W;
            case 1 -> KeyEvent.VK_A;
            case 2 -> KeyEvent.VK_S;
            case 3 -> KeyEvent.VK_D;
            default -> -1;
        };

        if (keyCode != -1) {
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
        }
    }

    private String actionToKey(int action) {
        return switch (action) {
            case 0 -> "W";
            case 1 -> "A";
            case 2 -> "S";
            case 3 -> "D";
            default -> "Unknown";
        };
    }
}
