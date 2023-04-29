import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class Main {
    private static ScreenReceiver screenReceiver;
    private static AtomicBoolean stopFlag = new AtomicBoolean(false); // For managing thread activity

    public static void main(String[] args) {
        JFrame frame = new JFrame("Screen Receiver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel);

        frame.setVisible(true);
    }

    // private void createAndShowGUI() {
    //     JFrame frame = new JFrame("Screen Receiver");
    //     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //     frame.setSize(800, 600);

    //     JPanel panel = new JPanel();
    //     frame.add(panel);;
    //     placeComponents(panel);

    //     frame.setVisible(true);
    // }

    private static void placeComponents(JPanel panel) {
        panel.setLayout(null);

        // Sender IP label and input
        JLabel senderIPLabel = new JLabel("Sender IP:");
        senderIPLabel.setBounds(10, 20, 80, 25);
        panel.add(senderIPLabel);

        JTextField senderIPInput = new JTextField("localhost", 20);
        senderIPInput.setBounds(100, 20, 200, 25);
        panel.add(senderIPInput);

        // Screen Port label and input
        JLabel screenPortLabel = new JLabel("Screen Port:");
        screenPortLabel.setBounds(10, 60, 80, 25);
        panel.add(screenPortLabel);

        JTextField screenPortInput = new JTextField("1234", 20);
        screenPortInput.setBounds(100, 60, 200, 25);
        panel.add(screenPortInput);

        // Mouse Port label and input
        JLabel mousePortLabel = new JLabel("Mouse Port:");
        mousePortLabel.setBounds(10, 100, 80, 25);
        panel.add(mousePortLabel);

        JTextField mousePortInput = new JTextField("1235", 20);
        mousePortInput.setBounds(100, 100, 200, 25);
        panel.add(mousePortInput);

        // Use Screen Port +1 Checkbox
        JCheckBox useScreenPortPlusOneCheckbox = new JCheckBox("Use Screen Port +1");
        useScreenPortPlusOneCheckbox.setBounds(10, 140, 200, 25);
        panel.add(useScreenPortPlusOneCheckbox);

        // Connect and Disconnect buttons
        JButton connectButton = new JButton("Connect");
        connectButton.setBounds(10, 180, 100, 25);
        panel.add(connectButton);

        JButton disconnectButton = new JButton("Disconnect");
        disconnectButton.setBounds(120, 180, 100, 25);
        disconnectButton.setEnabled(false);
        panel.add(disconnectButton);

        // Console output
        JTextArea consoleOutput = new JTextArea();
        consoleOutput.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(consoleOutput);
        scrollPane.setBounds(10, 220, 760, 330);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane);

        // Redirect console output to the JTextArea
        OutputStream outputStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                consoleOutput.append(String.valueOf((char) b));
            }
        };

        // Redirect System.out and System.err to the consoleOutput
        // PrintStream printStream = new PrintStream(new CustomOutputStream(consoleOutput));
        PrintStream printStream = new PrintStream(outputStream);
        System.setOut(printStream);
        System.setErr(printStream);

        // Connect button action
        connectButton.addActionListener(e -> {
            String senderIP = senderIPInput.getText().trim();
            String screenPortText = screenPortInput.getText().trim();
            String mousePortText = mousePortInput.getText().trim();

            if (senderIP.isEmpty() || screenPortText.isEmpty() || (!useScreenPortPlusOneCheckbox.isSelected() && mousePortText.isEmpty())) {
                consoleOutput.append("Error: All required fields must be filled in.\n");
                System.out.println("EMPTY INPUT ERROR HANDLING");
                return;
            }

            int screenPort = Integer.parseInt(screenPortText);
            int mousePort = useScreenPortPlusOneCheckbox.isSelected() ? screenPort + 1 : Integer.parseInt(mousePortText);


            // Create a new thread to handle the connection
            Thread connectionThread = new Thread(() -> {
                try {
                    System.out.println("IP: " + senderIP + "\nScreen Port: " + screenPort + "\nMouse Port: " + mousePort);
                    screenReceiver = new ScreenReceiver(senderIP, screenPort, mousePort);

                    while (!stopFlag.get()) {
                        screenReceiver.start();
                    }
                } catch (IOException ex) {
                    consoleOutput.append("Error connecting: " + ex.getMessage() + "\n");
                    SwingUtilities.invokeLater(() -> disconnectButton.doClick());
                }
            });

            // Start the connection thread
            connectionThread.start();

            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
        });

        // Disconnect button action
        disconnectButton.addActionListener(e -> {
            stopFlag.set(true); // Close the Screen Sender

            if (screenReceiver != null) {
                screenReceiver.stop();
                screenReceiver = null;
            }

            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
        });

        useScreenPortPlusOneCheckbox.addActionListener(e -> {
            if (useScreenPortPlusOneCheckbox.isSelected()) {
                mousePortInput.setEnabled(false);
            } else {
                mousePortInput.setEnabled(true);
            }
        });
    }

    // public static class CustomOutputStream extends OutputStream {
    //     private JTextArea textArea;

    //     public CustomOutputStream(JTextArea textArea) {
    //         this.textArea = textArea;
    //     }

    //     @Override
    //     public void write(int b) throws IOException {
    //         textArea.append(String.valueOf((char) b));
    //         textArea.setCaretPosition(textArea.getDocument().getLength());
    //     }
    // }
}