package de.clinc8686.texture.imagequilting;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class optionsPanel extends JPanel implements ActionListener, ChangeListener {
    JButton startBtn, clearBtn, chooseBtn;
    outputPanel outputPanel;
    inputPanel inputPanel;
    JLabel patchSizeText, imageSizeText, overlapSliderText;
    JSlider patchSize, imageSizeSlider, overlapSlider;
    JTextArea info;
    JFileChooser fileChooser;
    String inputPath;
    BufferedImage inputImage;

    public optionsPanel(inputPanel ip, outputPanel ouP) {
        super();
        inputPanel = ip;
        outputPanel = ouP;
        this.setBorder(BorderFactory.createTitledBorder("Options"));

        this.setLayout(new GridLayout(8,2));
        defineChooserTextArea();
    }

    private void defineChooserTextArea() {
        info = new JTextArea();
        info.setText("Infopanel:");
        info.setFont(info.getFont().deriveFont(13f));
        info.setEditable(false);
        this.add(info);

        chooseBtn = new JButton("Choose input");
        chooseBtn.addActionListener(this);
        this.add(chooseBtn);

        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png"));
    }

    private void defineSlider() {
        startBtn = new JButton("Start image quilting");
        startBtn.addActionListener(this);
        this.add(startBtn);

        clearBtn = new JButton("Clear");
        clearBtn.addActionListener(this);
        this.add(clearBtn);

        try {
            inputImage = ImageIO.read(new File(inputPath));
        } catch (IOException e) {
            info.append("\n Cant open file");
            return;
        }
        int height = inputImage.getHeight();
        int width = inputImage.getWidth();
        patchSizeText = createText("patch size: " + 32 + " * " + 32);
        patchSize = createSlider(2, width/2, 32, (width/2)/12);
        imageSizeText = createText("end image size: " + 192 + " * " + 192);
        imageSizeSlider = createSlider(4, 512, 192, (512/12));
        overlapSliderText = createText("overlap size: " + 4);
        overlapSlider = createSlider(1, height/2, 4, (height/2)/12);

    }

    private JLabel createText(String text) {
        JLabel tf = new JLabel(text, JLabel.CENTER);
        tf.setFont(tf.getFont().deriveFont(20f));
        this.add(tf);
        return tf;
    }

    private JSlider createSlider(int minimum, int maximum, int value, int tickSpace) {
        JSlider slider = new JSlider();
        slider.setMinimum(minimum);
        slider.setMaximum(maximum);
        slider.setValue(value);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setMajorTickSpacing(tickSpace);
        slider.addChangeListener(this);
        this.add(slider);
        return slider;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == startBtn){
            try {
                if (overlapSlider.getValue() >= patchSize.getValue()/4) {
                    throw new ArrayIndexOutOfBoundsException("");
                }
                ImageQuilting iq = new ImageQuilting(inputImage, patchSize.getValue(), imageSizeSlider.getValue(), overlapSlider.getValue());
                outputPanel.printImage(iq.endImage);
            } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
                info.append("\npatch or overlap size too high");
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (e.getSource() == clearBtn) {
            outputPanel.removeAll();
            outputPanel.validate();
            outputPanel.repaint();
            info.setText("Infopanel:");
        } else if (e.getSource() == chooseBtn) {
            int value = fileChooser.showOpenDialog(null);
            if (value == JFileChooser.APPROVE_OPTION) {
                this.removeAll();
                inputPath = fileChooser.getSelectedFile().getAbsolutePath();
                defineSlider();
                defineChooserTextArea();
                inputPanel.printImage(inputImage);
                this.validate();
                this.repaint();
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Object source = e.getSource();
        if (patchSize.equals(source)) {
            patchSizeText.setText("patch size:  " + ((JSlider)e.getSource()).getValue() + " * " + ((JSlider)e.getSource()).getValue());
        } else if (imageSizeSlider.equals(source)) {
            imageSizeText.setText("end image size: " + ((JSlider)e.getSource()).getValue() + " * " + ((JSlider)e.getSource()).getValue());
        } else if (overlapSlider.equals(source)) {
            overlapSliderText.setText("overlap size: " + ((JSlider)e.getSource()).getValue());
        }
    }
}
