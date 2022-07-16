package de.clinc8686.texture.imagequilting;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class optionsPanel extends JPanel implements ActionListener {
    JButton startBtn;
    JButton clearBtn;
    outputPanel outputPanel;
    inputPanel inputPanel;
    JSlider patchHeight, patchWidth, endImageHeightSlider, endImageWidthSlider, overlapSlider;
    JTextArea info;

    public optionsPanel(inputPanel ip, outputPanel ouP) throws IOException {
        super();
        inputPanel = ip;
        outputPanel = ouP;
        this.setBorder(BorderFactory.createTitledBorder("Options"));

        startBtn = new JButton("Start image quilting");
        startBtn.addActionListener(this);
        this.add(startBtn);

        clearBtn = new JButton("Clear");
        clearBtn.addActionListener(this);
        this.add(clearBtn);

        this.setLayout(new GridLayout(8,2));
        defineSlider();

        info = new JTextArea();
        info.setText("Infopanel:");
        info.setFont(info.getFont().deriveFont(13f));
        this.add(info);
    }

    private void defineSlider() throws IOException {
        BufferedImage inputImage = ImageIO.read(new File(System.getProperty("user.dir")+"\\src\\de\\clinc8686\\texture\\imagequilting\\texture_input.jpg"));
        int height = inputImage.getHeight();
        int width = inputImage.getWidth();
        patchHeight = createSlider(2, height/2, 32, (height/2)/12, "patch height: ");
        patchWidth = createSlider(2, width/2, 32, (width/2)/12, "patch width: ");
        endImageHeightSlider = createSlider(4, 512, 192, (512/12), "end image height: ");
        endImageWidthSlider = createSlider(4, 512, 192, (512/12), "end image width: ");
        overlapSlider = createSlider(1, height/2, 4, (height/2)/12, "overlap size: ");
    }

    private JSlider createSlider(int minimum, int maximum, int value, int tickSpace, String text) {
        JTextField tf = new JTextField(0);
        tf.setText(text);
        tf.setFont(tf.getFont().deriveFont(20f));
        this.add(tf);

        JSlider slider = new JSlider();
        slider.setMinimum(minimum);
        slider.setMaximum(maximum);
        slider.setValue(value);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setMajorTickSpacing(tickSpace);
        this.add(slider);
        return slider;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == startBtn){
            try {
                if (overlapSlider.getValue() >= patchHeight.getValue()/4 || overlapSlider.getValue() >= patchWidth.getValue()/4) {
                    throw new ArrayIndexOutOfBoundsException("");
                }
                ImageQuilting iq = new ImageQuilting(patchHeight.getValue(), patchWidth.getValue(), endImageHeightSlider.getValue(), endImageWidthSlider.getValue(), overlapSlider.getValue());
                inputPanel.printImage(iq.inputImage);
                outputPanel.printImage(iq.endImage);
            } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
                info.append("\npatch or overlap size too big");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (e.getSource() == clearBtn) {
            outputPanel.removeAll();
            outputPanel.validate();
            outputPanel.repaint();
            info.setText("Infopanel:");
        }
    }
}
