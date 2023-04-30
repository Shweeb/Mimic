import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class Main {
    private static ScreenSender screenSender;
    private static AtomicBoolean stopFlag = new AtomicBoolean(false); // For managing thread activity

    public static void main(String[] args) {
        JFrame frame = new JFrame("Screen Sender");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        // Color custom = new Color(173, 216, 230);
        // frame.getContentPane().setBackground(Color.GRAY);
        frame.getContentPane().setBackground(Color.RED); // Maybe get this working

        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel);

        frame.setVisible(true);
    }

    private static void placeComponents(JPanel panel) {
        panel.setLayout(null);

        // Connect Button
        JButton connectButton = new JButton("Connect");
        connectButton.setBounds(10, 10, 120, 25);
        panel.add(connectButton);

        // Disconnect Button
        JButton disconnectButton = new JButton("Disconnect");
        disconnectButton.setBounds(140, 10, 120, 25);
        disconnectButton.setEnabled(false);
        panel.add(disconnectButton);

        // IP Address Label and Input
        JLabel ipLabel = new JLabel("Destination IP:");
        ipLabel.setBounds(10, 50, 120, 25);
        panel.add(ipLabel);

        JTextField ipInput = new JTextField("localhost", 20);
        ipInput.setBounds(130, 50, 160, 25);
        panel.add(ipInput);

        // Screen Port Label and Input
        JLabel screenPortLabel = new JLabel("Screen Port:");
        screenPortLabel.setBounds(10, 90, 120, 25);
        panel.add(screenPortLabel);

        JTextField screenPortInput = new JTextField("1234", 20);
        screenPortInput.setBounds(130, 90, 160, 25);
        panel.add(screenPortInput);

        // Mouse Port Label and Input
        JLabel mousePortLabel = new JLabel("Mouse Port:");
        mousePortLabel.setBounds(10, 130, 120, 25);
        panel.add(mousePortLabel);

        JTextField mousePortInput = new JTextField("1235", 20);
        mousePortInput.setBounds(130, 130, 160, 25);
        panel.add(mousePortInput);

        // Checkbox for Mouse Port
        JCheckBox useScreenPortPlusOneCheckbox = new JCheckBox("Use Screen Port + 1");
        useScreenPortPlusOneCheckbox.setBounds(130, 160, 200, 30);
        panel.add(useScreenPortPlusOneCheckbox);

        // Console Output
        JTextArea consoleOutput = new JTextArea();
        consoleOutput.setEditable(false);
        consoleOutput.setBounds(10, 200, 465, 350);
        JScrollPane scrollPane = new JScrollPane(consoleOutput);
        scrollPane.setBounds(10, 200, 465, 350);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane);

        // Redirect console output to the JTextArea
        OutputStream outputStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                consoleOutput.append(String.valueOf((char) b));
            }
        };
        PrintStream printStream = new PrintStream(outputStream);
        System.setOut(printStream);
        System.setErr(printStream);

        // Connect Button Listener
        connectButton.addActionListener(e -> {
            String destinationIP = ipInput.getText().trim();
            String screenPortText = screenPortInput.getText().trim();
            String mousePortText = mousePortInput.getText().trim();

            if (destinationIP.isEmpty() || screenPortText.isEmpty() || (!useScreenPortPlusOneCheckbox.isSelected() && mousePortText.isEmpty())) {
                System.err.println("Error: Please fill in all required fields.");
                return;
            }

            int screenPort = Integer.parseInt(screenPortText);
            int mousePort = useScreenPortPlusOneCheckbox.isSelected() ? screenPort + 1 : Integer.parseInt(mousePortText);

            // // Update the mouse port if the checkbox is selected
            // if (useScreenPortPlusOneCheckbox.isSelected()) {
            //     mousePort = screenPort + 1;
            // } else {
            //     mousePort = Integer.parseInt(mousePortText);
            // }

            // Create a new thread to handle the connection
            Thread connectionThread = new Thread(() -> {
                try {
                    System.out.println("IP: " + destinationIP + "\nScreen Port: " + screenPort + "\nMouse Port: " + mousePort);
                    screenSender = new ScreenSender(destinationIP, screenPort, mousePort);

                    while (!stopFlag.get()) {
                        screenSender.start();
                    }
                } catch (Exception ex) {
                    // System.err.println("Failed to start the ScreenSender: " + ex.getMessage());
                    System.err.println("Server Closed: " + ex.getMessage());
                    SwingUtilities.invokeLater(() -> disconnectButton.doClick());
                }
            });

            // Start the connection thread
            connectionThread.start();

            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
        });

        // Disconnect Button Listener
        disconnectButton.addActionListener(e -> {
            stopFlag.set(true); // Close the Screen Sender

            if (screenSender != null) {
                screenSender.stop();
                screenSender = null;
            }
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
        });

        // Mouse Port Checkbox Listener
        useScreenPortPlusOneCheckbox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                mousePortInput.setEnabled(false);
                // int screenPort = Integer.parseInt(screenPortInput.getText());
                // mousePortInput.setText(String.valueOf(screenPort + 1));
            } else {
                mousePortInput.setEnabled(true);
            }
        });
    }
}
