package org.diplom;

import org.apache.commons.io.FileUtils;
import org.diplom.ui.ProgressBar;
import org.diplom.ui.Ui;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.zip.Adler32;

public class Main {

    private static final String MODEL_PATH = "resources/model.zip";
    private static final String DOWNLOADING_MSG = "Downloading model for the first time 500MB!";
    private static final String FAILED_TO_DOWNLOAD_MSG = "Failed to download model";
    private static final String URL_MODEL = "https://dl.dropboxusercontent.com/s/djmh91tk1bca4hz/model.zip?dl=0";

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {

        JFrame mainFrame = new JFrame();
        ProgressBar progressBar = new ProgressBar(mainFrame, true);
        progressBar.showProgressBar("Loading model, this make take several seconds!");
        downloadDataForFirstTime();
        Ui ui = new Ui();
        Executors.newCachedThreadPool().submit(() -> {
            try {
                ui.initUi();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                progressBar.setVisible(false);
                mainFrame.dispose();
            }
        });


    }

    private static void downloadDataForFirstTime() {
        JFrame mainFrame = new JFrame();
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                System.exit(0);
            }
        });

        ProgressBar progressBar = new ProgressBar(mainFrame, false);
        File model = new File(MODEL_PATH);

        try {
            if (!model.exists() || FileUtils.checksum(model, new Adler32()).getValue() != 3082129141L) {
                model.delete();
                progressBar.showProgressBar(DOWNLOADING_MSG);
                FileUtils.copyURLToFile(new URI(URL_MODEL).toURL(), model);
            }
        } catch (IOException | URISyntaxException exception) {
            JOptionPane.showMessageDialog(null, FAILED_TO_DOWNLOAD_MSG);
            throw new RuntimeException(FAILED_TO_DOWNLOAD_MSG);
        } finally {
            progressBar.setVisible(false);
            mainFrame.dispose();
        }
    }
}
