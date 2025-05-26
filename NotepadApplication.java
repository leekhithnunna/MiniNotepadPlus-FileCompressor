import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import com.sun.speech.freetts.*;

public class NotepadApplication extends JFrame {
     JTextArea textArea;
     JFileChooser fileChooser;
     File currentFile;
     Voice voice;
     Stack<String> undoStack;
     Stack<String> redoStack;
     Map<String, String> replacements;


    public NotepadApplication() {
        setTitle("Notepad");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newMenuItem = new JMenuItem("New");
        JMenuItem openMenuItem = new JMenuItem("Open");
        JMenuItem saveMenuItem = new JMenuItem("Save");
        JMenuItem saveAsMenuItem = new JMenuItem("Save As");
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        JMenuItem exitMenu2Item = new JMenuItem("Exit1");
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton deleteButton = new JButton("Delete");

        buttonPanel.add(deleteButton);
        fileMenu.add(newMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        fileMenu.add(exitMenu2Item);
        menuBar.add(fileMenu);

        JMenu operationMenu = new JMenu("Operation");
        JMenuItem undoItem = new JMenuItem("Undo");
        JMenuItem redoItem = new JMenuItem("Redo");
        JMenuItem FindAndReplaceItem = new JMenuItem("Find & Replace");
        operationMenu.add(undoItem);
        operationMenu.add(redoItem);
        operationMenu.add(FindAndReplaceItem);
        menuBar.add(operationMenu);

        JMenu ModeMenu = new JMenu("Mode");
        JMenuItem lightMode = new JMenuItem("Light");
        JMenuItem darkMode = new JMenuItem("Dark");
        ModeMenu.add(lightMode);
        ModeMenu.add(darkMode);
        menuBar.add(ModeMenu);

        JMenu speechMenu = new JMenu("Speech");
        JMenuItem speakMenuItem = new JMenuItem("Speak");
        speechMenu.add(speakMenuItem);
        menuBar.add(speechMenu);

        JMenu EditMenu = new JMenu("Edit");
        JMenuItem fontColorItem = new JMenuItem("Font Color");
        EditMenu.add(fontColorItem);

        JMenu fontStyle = new JMenu("Font style");
        JMenuItem plainFontItem = new JMenuItem("Plain");
        JMenuItem boldFontItem = new JMenuItem("Bold");
        JMenuItem italicFontItem = new JMenuItem("Italic");
        JMenuItem boldItalicFontItem = new JMenuItem("Bold Italic");
        fontStyle.add(plainFontItem);
        fontStyle.add(boldFontItem);
        fontStyle.add(italicFontItem);
        fontStyle.add(boldItalicFontItem);
        EditMenu.add(fontStyle);
        menuBar.add(EditMenu);

        JMenu Compressor = new JMenu("Compressor");
        JMenuItem compressFile = new JMenuItem("compress");
        Compressor.add(compressFile);
        menuBar.add(Compressor);

        setJMenuBar(menuBar);

        fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
        fileChooser.setFileFilter(filter);

        
  
        newMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
                currentFile = null;
                undoStack.clear();
                redoStack.clear();
                // Push initial empty state
            }
        });

        openMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(NotepadApplication.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        textArea.read(reader, null);
                        reader.close();
                        currentFile = file;
                        undoStack.clear();
                        redoStack.clear();
                        undoStack.push(textArea.getText()); // Push loaded state
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentFile != null) {
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile));
                        textArea.write(writer);
                        writer.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    saveAsMenuItem.doClick();
                }
            }
        });

        saveAsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showSaveDialog(NotepadApplication.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                        textArea.write(writer);
                        writer.close();
                        currentFile = file;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        lightMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setBackground(Color.WHITE);
                textArea.setForeground(Color.BLACK);
            }
        });

        darkMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setBackground(Color.BLACK);
                textArea.setForeground(Color.WHITE);
            }
        });
    

        speakMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = textArea.getText();
                speakText(text);
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String numCharsStr = JOptionPane.showInputDialog("Enter number of characters to delete:");
                if (numCharsStr != null) {
                    try {
                        int numChars = Integer.parseInt(numCharsStr);
                        deleteText(numChars);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid number.");
                    }
                }
            }
        });
    
        undoItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });

        redoItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                redo();
            }
        });

        FindAndReplaceItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input1 = JOptionPane.showInputDialog(null, "Enter value for input 1:");
                String input2 = JOptionPane.showInputDialog(null, "Enter value for input 2:");
                replacements.put(input1, input2);
                String text=textArea.getText();
        
                String newText = findAndReplace(text, replacements);
               textArea.setText(newText);

            
            }
        });

        fontColorItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 // Show color chooser dialog
                 Color selectedColor = JColorChooser.showDialog(null, "Choose Font Color", textArea.getForeground());
                 if (selectedColor != null) {
                     // Set the selected color as the text color
                     textArea.setForeground(selectedColor);
                 }
             }
         });


         plainFontItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeFontStyle(Font.PLAIN);
            }
        });

        boldFontItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeFontStyle(Font.BOLD);
            }
        });

        italicFontItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeFontStyle(Font.ITALIC);
            }
        });

         boldItalicFontItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeFontStyle(Font.BOLD | Font.ITALIC);
            }
        });

        
        compressFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String filePath = selectedFile.getAbsolutePath();
                    String outputFilePath = filePath + ".huffman";
                    try {
                        HuffmanCompressor.compressFile(filePath, outputFilePath);
                        JOptionPane.showMessageDialog(null, "File compressed successfully to " + outputFilePath);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Error compressing file: " + ex.getMessage());
                    }
                }
            }
        });
        

        // Add button panel to the frame
        add(buttonPanel, BorderLayout.SOUTH);

        undoStack = new Stack<>();
        redoStack = new Stack<>();


        replacements = new HashMap<>();
        // Add initial state to the undo stack

    }

    private void speakText(String text) {
        if (voice == null) {
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            voice = VoiceManager.getInstance().getVoice("kevin16");
            if (voice != null) {
                voice.allocate();
            } else {
                JOptionPane.showMessageDialog(this, "Error initializing FreeTTS.");
                return;
            }
        }

        if(text.isEmpty()){
            JOptionPane.showMessageDialog(null, "There is no text");
          voice.speak("There is no text");
          
        }
        else{
        voice.speak(text);
        }
    }

    public void deleteText(int numChars) {
        String text = textArea.getText();
        if (numChars <= text.length()) {
            undoStack.push(text);
            textArea.setText(text.substring(0, text.length() - numChars));
        } else {
            JOptionPane.showMessageDialog(null, "Cannot delete more characters than available in the text.");
        }
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(textArea.getText());
            textArea.setText(undoStack.pop());
        } else {
            JOptionPane.showMessageDialog(null, "Nothing to undo.");
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(textArea.getText());
            textArea.setText(redoStack.pop());
        } else {
            JOptionPane.showMessageDialog(null, "Nothing to redo.");
        }
    }

    //Find and replace

    public static String findAndReplace(String text, Map<String, String> replacements) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            boolean replaced = false;
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (text.startsWith(key, i)) {
                    result.append(value);
                    i += key.length();
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                result.append(text.charAt(i));
                i++;
            }
        }
        return result.toString();
    }

    private void changeFontStyle(int fontStyle) {
        Font currentFont = textArea.getFont();
        Font newFont = new Font(currentFont.getFontName(), fontStyle, currentFont.getSize());
        textArea.setFont(newFont);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                NotepadApplication notepad = new NotepadApplication();
                notepad.setVisible(true);
            }
        });
    }
}
