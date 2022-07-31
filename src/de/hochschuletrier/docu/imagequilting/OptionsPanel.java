package de.hochschuletrier.docu.imagequilting;

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

public class OptionsPanel extends JPanel implements ActionListener, ChangeListener {
    private JButton startBtn, clearBtn, chooseBtn;
    private final OutputPanel outputPanel;
    private final InputPanel inputPanel;
    private JLabel patchSizeText, imageSizeText, overlapSliderText;
    private JSlider patchSize, imageSizeSlider, overlapSlider;
    private JTextArea info;
    private JFileChooser fileChooser;
    private String inputPath;
    private BufferedImage inputImage;

    /**
     * Constructor defines some Layout options.
     *
     * @param ip The input panel from the center.
     * @param ouP The output panel from the left.
     */
    public OptionsPanel(InputPanel ip, OutputPanel ouP) {
        super();
        inputPanel = ip;
        outputPanel = ouP;
        this.setBorder(BorderFactory.createTitledBorder("Options"));

        this.setLayout(new GridLayout(8,2));
        defineChooserTextArea();
    }

    /**
     * Create two Options on the OptionPanel.
     */
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

    /**
     * Creates the 3 options slider and 2 option buttons.
     */
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

    /**
     * Creates a JLabel with a text.
     *
     * @param text The text for the JLabel.
     * @return The JLabel with the text on it.
     */
    private JLabel createText(String text) {
        JLabel tf = new JLabel(text, JLabel.CENTER);
        tf.setFont(tf.getFont().deriveFont(20f));
        this.add(tf);
        return tf;
    }

    /**
     * Creates a slider with some defined values.
     *
     * @param minimum The minimum value of the slider.
     * @param maximum The maximum value of the slider.
     * @param value The start value of the slider.
     * @param tickSpace The distances of the marks between the maximum and the minimum.
     * @return The defined slider.
     */
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

    /**
     * Responds to the changes of the buttons on the options panel.
     *
     * @param e The given ActionEvent from a Button.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == startBtn){
            try {
                int overlap = overlapSlider.getValue();
                int endsize = imageSizeSlider.getValue();
                int patch = patchSize.getValue();
                if (overlap >= endsize || overlap >= patch) {
                    throw new IllegalOverlapException("bad overlap");
                } else if (patch >= endsize) {
                    throw new IllegalPatchException("bad patch");
                }
                ImageQuilting iq = new ImageQuilting(inputImage, patch, imageSizeSlider.getValue(), overlap);
                outputPanel.printImage(iq.endImage);
                inputPanel.printFirstBlock(iq.firstBlock);
            } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException | IOException ex) {
                info.append("\nThere is something wrong");
            } catch (IllegalOverlapException | IllegalPatchException ex) {
                info.append("\n" + ex.getMessage());
            }
        } else if (e.getSource() == clearBtn) {
            outputPanel.removeAll();
            outputPanel.validate();
            outputPanel.repaint();
            inputPanel.removeAll();
            inputPanel.printImage(inputImage);
            inputPanel.validate();
            inputPanel.repaint();
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

    /**
     * Responds to the changes of the slider at the options panel.
     *
     * @param e The given ActionEvent from a Slider.
     */
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
