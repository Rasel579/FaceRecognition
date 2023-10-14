package org.diplom.ui;

import org.diplom.vgg16.PetType;
import org.diplom.vgg16.VGG16Cat;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class Ui {
    private static final double THRESHOLD_ACCURACY = 0.5;
    private JFrame mainFrame;
    private JPanel mainPanel;
    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 600;
    private ImagePanel sourceImagePanel;
    private JLabel predictionResponse;
    private VGG16Cat vgg16Cat;
    private File selectedFile;
    private JSpinner thresholdSpinner;
    private final Font sansSerifFont = new Font("SansSerif", Font.BOLD, 18);

    public Ui() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        UIManager.put("Button.font", new FontUIResource(new Font("Dialog", Font.BOLD, 18)));
        UIManager.put("ProgressBar.font", new FontUIResource(new Font("Dialog", Font.BOLD, 18)));
    }

    public void initUi() throws IOException {
        vgg16Cat = new VGG16Cat();
        vgg16Cat.loadModel();
        mainFrame = createMainFrame();

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

        JButton chooseBtn = new JButton("Choose Pet Image");
        chooseBtn.addActionListener(e -> {
            chooseFileAction();
            predictionResponse.setText("");
        });

        JButton predictBtn = new JButton("Is It Cat or Dog?");
        predictBtn.addActionListener(e -> {
            try {
                PetType type = vgg16Cat.detectCat(selectedFile, (Double) thresholdSpinner.getValue());
                if (type == PetType.CAT) {
                    predictionResponse.setText("It is Cat");
                    predictionResponse.setForeground(Color.GREEN);
                }

                if (type == PetType.DOG) {
                    predictionResponse.setText("It is DOG");
                    predictionResponse.setForeground(Color.GREEN);
                } else {
                    predictionResponse.setText("Not Sure...");
                    predictionResponse.setForeground(Color.RED);
                }

                mainPanel.updateUI();

            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });

        fillMainPanel(chooseBtn, predictBtn);
        addSignature();

        mainFrame.add(mainPanel, BorderLayout.CENTER);
        mainFrame.setVisible(true);
        mainFrame.pack();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JFrame createMainFrame() {
        JFrame mainFrame = new JFrame();
        mainFrame.setTitle("Image Recognizer");
        mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });
        ImageIcon icon = new ImageIcon("icon.png");
        mainFrame.setIconImage(icon.getImage());
        return mainFrame;
    }

    private void chooseFileAction() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(new File("resources").getAbsolutePath()));
        int action = chooser.showOpenDialog(null);
        if (action == JFileChooser.APPROVE_OPTION) {
            try {
                selectedFile = chooser.getSelectedFile();
                showSelectedImageOnPanel(Files.newInputStream(selectedFile.toPath()), sourceImagePanel);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void showSelectedImageOnPanel(InputStream selectedFile, ImagePanel imagePanel) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(selectedFile);
        imagePanel.setImage(bufferedImage);
    }

    private void fillMainPanel(JButton chooseButton, JButton predictButton) throws IOException {
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 0;
        constraints.weighty = 0;
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(THRESHOLD_ACCURACY, 0.5, 1, 0.5);
        thresholdSpinner = new JSpinner(spinnerNumberModel);
        JLabel label = new JLabel("Threshold Accuracy %");
        label.setFont(sansSerifFont);
        buttonsPanel.add(label);
        buttonsPanel.add(thresholdSpinner);
        buttonsPanel.add(chooseButton);
        buttonsPanel.add(predictButton);
        mainPanel.add(buttonsPanel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.weighty = 1;
        constraints.weightx = 1;
        sourceImagePanel = new ImagePanel();
        mainPanel.add(sourceImagePanel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.weightx = 0;
        constraints.weighty = 0;

        predictionResponse = new JLabel();
        predictionResponse.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 72));
        mainPanel.add(predictionResponse, constraints);
    }

    private void addSignature() {
        JLabel signature = new JLabel("Ruslan_Shaykh", SwingConstants.CENTER);
        signature.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 20));

        signature.setForeground(Color.BLUE);
        mainFrame.add(signature, BorderLayout.SOUTH);
    }
}
