package main;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class EditorForm extends JFrame {
    private JPanel mainPanel;
    private JTable mainTable;
    private JTextField pathField;
    private JButton selectButton;
    private JButton resetChangesButton;
    private JButton autoButton;
    private JButton saveButton;

    public EditorForm(String title) {
        super(title);

        pathField.setText("Z:\\Phone Music");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        selectButton.addActionListener(e -> selectDirectory());
        pathField.addActionListener(e -> openPath());
        saveButton.addActionListener(e -> ((Mp3TableDataModel)mainTable.getModel()).saveAll());
        resetChangesButton.addActionListener(e -> ((Mp3TableDataModel)mainTable.getModel()).resetAll());
        autoButton.addActionListener(e -> ((Mp3TableDataModel)mainTable.getModel()).autoFill());
    }

    private void selectDirectory() {
        JFileChooser fc = new JFileChooser();
        try {
            fc.setCurrentDirectory(new File(pathField.getText()));
        } catch (Exception e) {
            fc.setCurrentDirectory(new File("Z:\\Phone Music"));
        }
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            pathField.setText(fc.getSelectedFile().getAbsolutePath());
            openPath();
        }
    }

    private void openPath() {
        mainTable.setModel(new Mp3TableDataModel(pathField.getText()));
    }

    public static void main(String[] args) throws IOException {
        EditorForm frame = new EditorForm("Thingamabober");
        frame.setVisible(true);
    }
}
