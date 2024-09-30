package com.gmail.genek530.downloader.common;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class GUIThing extends JFrame {

    private boolean wasRaised = false;

    public GUIThing() throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream("techkloc.png");

        BufferedImage image = ImageIO.read(inputStream);
        Image scaled = image.getScaledInstance(800, 500, Image.SCALE_SMOOTH);
        ImageIcon imageIcon = new ImageIcon(scaled);
        JLabel jLabel = new JLabel(imageIcon);
        this.add(jLabel);
        this.setSize(800,500);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setAlwaysOnTop(true);
        pack();
        this.setVisible(true);
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                wasRaised = true;
            }
        });
    }

    public boolean didRaise(){
        return this.wasRaised;
    }
}
