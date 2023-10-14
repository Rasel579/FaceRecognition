package org.diplom.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ImagePanel extends JPanel {
    public static final int DEFAULT_WIDTH = 400;
    public static final int DEFAULT_HEIGHT = 400;
    private Image image;
    private InputStream imageStream;

    public ImagePanel() throws IOException {
        showDefault();
    }

    public void paintComponent(Graphics g){
        g.drawImage(image, 0,0, null);
    }

    public void showDefault() throws IOException {
        String showDefaultImage = getDefaultImage();
        imageStream = Files.newInputStream(Paths.get("resources/" + showDefaultImage));

    }
    public void setImage(InputStream imageStream) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(imageStream);
      Image scaledImageInstance = bufferedImage.getScaledInstance(DEFAULT_WIDTH, DEFAULT_HEIGHT, Image.SCALE_DEFAULT);
      setImage(scaledImageInstance);
      this.imageStream = imageStream;
    }

    public void setImage(Image image){
        this.image = image;
        Dimension size = new Dimension(image.getWidth(null), image.getHeight(null));
        setPreferredSize(size);
        setMaximumSize(size);
        setMinimumSize(size);
        setSize(size);
        setLayout(null);
        repaint();
        updateUI();
    }

    private String getDefaultImage(){
        return  "/placeholder.gif";
    }
}
