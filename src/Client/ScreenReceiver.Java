import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
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

    public static void main(String[] args) throws IOException {
        // Set the port number and IP to listen on
        int screenPort = 1234;
        int mousePort = 1235;
        String senderIP = "localhost";

        // Create a JFrame to display the remote screen
        JFrame frame = new JFrame("Remote Screen");

        // Create a socket for the ScreenPanel to use
        Socket screenSocket = new Socket(senderIP, screenPort);
        Socket mouseSocket = new Socket(senderIP, mousePort);

        ScreenPanel screenPanel = new ScreenPanel(screenSocket, mouseSocket);
        frame.getContentPane().add(screenPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);

        // Continuously receive and display screen data
        // try (ServerSocket serverSocket = new ServerSocket(port)) {
        //     while (true) {
        //         // Listen for incoming connections
        //         Socket clientSocket = serverSocket.accept();

        //         // Receive the screen data
        //         try (InputStream inputStream = clientSocket.getInputStream()) {
        //             BufferedImage screenshot = ImageIO.read(inputStream);

        //             // Display the screen data
        //             screenPanel.setScreenshot(screenshot);
        //             screenPanel.repaint();
        //         } catch (IOException ex) {
        //             System.err.println("Error receiving screen data: " + ex.getMessage());
        //         }
        //     }
        // }

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

    // A JPanel to display the remote screen
    private static class ScreenPanel extends JPanel implements MouseListener {

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
                        dos.writeDouble(relativeX);
                        dos.writeDouble(relativeY);
                        dos.writeInt(button);
                        dos.flush();
                    } catch (IOException ex) {
                        System.err.println("Error sending mouse click: " + ex.getMessage());
                    }
                }
            }
            // if (e.getButton() == MouseEvent.BUTTON1 && screenshot != null) {
            //     double panelWidth = getWidth();
            //     double panelHeight = getHeight();
            //     double relativeX = e.getX() / panelWidth;
            //     double relativeY = e.getY() / panelHeight;
            //     System.out.println("Panel Size X:" + panelWidth + " Y:" + panelHeight);
            //     System.out.println("Mouse click position on panel X:" + e.getX() + " Y:" + e.getY());
            //     System.out.println("Relative values X:" + relativeX + " Y:" + relativeY + "\n");

            //     // Send mouse click coordinates to ScreenSender
            //     try {
            //         // dos.writeInt(remoteX);
            //         // dos.writeInt(remoteY);
            //         dos.writeDouble(relativeX);
            //         dos.writeDouble(relativeY);
            //         dos.writeBoolean(true); // indicate a left mouse click
            //         dos.flush();
            //     } catch (IOException ex) {
            //         System.err.println("Error sending mouse click: " + ex.getMessage());
            //     }
            // }
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
    }
}