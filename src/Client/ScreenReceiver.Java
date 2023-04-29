import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Point;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ScreenReceiver {

    public static final byte[] DELIMITER = new byte[]{-1, -1, -1, -1}; // Delimiter to mark the end of an image

    private String senderIP;
    private int screenPort;
    private int mousePort;
    private ScreenPanel screenPanel;
    private JFrame frame;

    // Add instance variables for sockets and streams
    private Socket screenSocket;
    private Socket mouseSocket;
    // private Thread screenReceiverThread;

    public ScreenReceiver(String senderIP, int screenPort, int mousePort) {
        // Set the port number and IP to listen on
        this.senderIP = senderIP;
        this.screenPort = screenPort;
        this.mousePort = mousePort;
    }

    public void start() throws IOException {

        // Create a JFrame to display the remote screen
        frame = new JFrame("Remote Screen");

        // Create a socket for the ScreenPanel to use
        screenSocket = new Socket(senderIP, screenPort);
        mouseSocket = new Socket(senderIP, mousePort);

        screenPanel = new ScreenPanel(screenSocket, mouseSocket);
        frame.getContentPane().add(screenPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);

        try (DataInputStream inputStream = new DataInputStream(screenSocket.getInputStream())) {
            while (true) {
                int imageSize = inputStream.readInt();
                byte[] imageBytes = new byte[imageSize];
                inputStream.readFully(imageBytes);

                // Read and discard delimiter
                byte[] receivedDelimiter = new byte[DELIMITER.length];
                inputStream.readFully(receivedDelimiter);

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
                BufferedImage screenshot = ImageIO.read(byteArrayInputStream);

                screenPanel.setScreenshot(screenshot);
                screenPanel.repaint();
            }
        }
    }

    public void stop() {
        try {
            if (screenSocket != null) {
                screenSocket.close();
            }
            if (mouseSocket != null) {
                mouseSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing sockets: " + e.getMessage());
        }
    }

    // A JPanel to display the remote screen
    private static class ScreenPanel extends JPanel implements MouseListener, KeyListener {

        private BufferedImage screenshot;
        private Socket screenSocket;
        private Socket mouseSocket;
        private DataOutputStream dos;

        public ScreenPanel(Socket screenSocket, Socket mouseSocket) {
            this.screenSocket = screenSocket;
            this.mouseSocket = mouseSocket;
            try {
                dos = new DataOutputStream(mouseSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            addMouseListener(this);
            setFocusable(true); // Makes it so the panel can actually take keyboard inputs
            requestFocusInWindow();
            addKeyListener(this);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(1920, 1080);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (screenshot != null) {
                g.drawImage(screenshot, 0, 0, null);
            } else {
                g.drawString("Waiting for image...", 10, 20);
            }
        }

        public void setScreenshot(BufferedImage screenshot) {
            // this.screenshot = screenshot;
            // Scale the screenshot to fit the panel size
            Image scaledImage = screenshot.getScaledInstance(getWidth(), getHeight(), Image.SCALE_FAST);
            BufferedImage scaledScreenshot = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledScreenshot.createGraphics();
            g2d.drawImage(scaledImage, 0, 0, null);
            g2d.dispose();

            this.screenshot = scaledScreenshot;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (screenshot != null) {
                double panelWidth = getWidth();
                double panelHeight = getHeight();
                double relativeX = e.getX() / panelWidth;
                double relativeY = e.getY() / panelHeight;

                int button = e.getButton();

                // System.out.println("Panel Size X:" + panelWidth + " Y:" + panelHeight);
                // System.out.println("Mouse click position on panel X:" + e.getX() + " Y:" + e.getY());
                // System.out.println("Relative values X:" + relativeX + " Y:" + relativeY + "\n");
                if (button == MouseEvent.BUTTON1 || button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
                    // Send mouse click coordinates to ScreenSender
                    try {
                        dos.writeBoolean(true); // Indicate that this is a mouse event
                        dos.writeDouble(relativeX);
                        dos.writeDouble(relativeY);
                        dos.writeInt(button);
                        dos.flush();
                    } catch (IOException ex) {
                        System.err.println("Error sending mouse click: " + ex.getMessage());
                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            // add your code here
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // add your code here
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // add your code here
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // add your code here
        }

        @Override
        public void keyPressed(KeyEvent e) {
            sendKeyEvent(0, e.getKeyCode(), '\0');
        }

        @Override
        public void keyReleased(KeyEvent e) {
            sendKeyEvent(1, e.getKeyCode(), '\0');
        }

        @Override
        public void keyTyped(KeyEvent e) {
            sendKeyEvent(2, 0, e.getKeyChar());
        }

        private void sendKeyEvent(int eventType, int keyCode, char keyChar) {
            try {
                dos.writeBoolean(false); // Indicate that this is a keyboard event
                dos.writeInt(eventType);

                if (eventType == 2) { // KeyTyped event
                    dos.writeChar(keyChar);
                    System.out.println("Key Typed: " + keyChar);
                } else {
                    dos.writeInt(keyCode);
                    if (eventType == 0) {
                        System.out.println("Key Pressed: " + keyCode);
                    } else if (eventType == 1) {
                        System.out.println("Key Released: " + keyCode);
                    }
                }

                dos.flush();
            } catch (IOException ex) {
                System.err.println("Error sending keyboard event: " + ex.getMessage());
            }
        }
    }
}