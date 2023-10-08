import org.apache.commons.io.FileUtils;
import ui.ProgressBar;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.zip.Adler32;

public class Main {

    private static final String MODEL_PATH = "resources/model.zip";
    private static final String DOWNLOADING_MSG = "Downloading model for the first time 500MB!";
    private static final String FAILED_TO_DOWNLOAD_MSG = "Failed to download model";
    private static final String URL_MODEL = "https://dl.dropboxusercontent.com/s/djmh91tk1bca4hz/RunEpoch_class_2_soft_10_32_1800.zip?dl=0";


    public static void main(String[] args) {
        System.out.println("Hello world!");
    }

    private static void downloadDataForFirstTime() throws IOException {
        JFrame mainFrame = new JFrame();
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                System.exit(0);
            }
        });

        ProgressBar progressBar = new ProgressBar(mainFrame, false);
        File model = new File(MODEL_PATH);

        if (!model.exists() || FileUtils.checksum(model, new Adler32()).getValue() != 3082129141L) {
            model.delete();
            progressBar.showProgressBar(DOWNLOADING_MSG);
            URL modelUrl = URI.create(URL_MODEL).toURL();

            try {
                FileUtils.copyURLToFile(modelUrl, model);
            } catch (IOException exception) {
                JOptionPane.showMessageDialog(null, FAILED_TO_DOWNLOAD_MSG);
                throw new RuntimeException(FAILED_TO_DOWNLOAD_MSG);
            } finally {
                progressBar.setVisible(false);
                mainFrame.dispose();
            }

        }
    }
}