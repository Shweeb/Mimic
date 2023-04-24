import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ScreenReceiver {

    public static void main(String[] args) throws IOException {
        // Set the port number to listen on
        int port = 1234;

        // Create a JFrame to display the remote screen
        JFrame frame = new JFrame("Remote Screen");

        // Create a socket for the ScreenPanel to use
        Socket socket = new Socket("localhost", port);

        ScreenPanel screenPanel = new ScreenPanel(socket);
        frame.getContentPane().add(screenPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);

        // Continuously receive and display screen data
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                // Listen for incoming connections
                Socket clientSocket = serverSocket.accept();

                // Receive the screen data
                try (InputStream inputStream = clientSocket.getInputStream()) {
                    BufferedImage screenshot = ImageIO.read(inputStream);

                    // Display the screen data
                    screenPanel.setScreenshot(screenshot);
                    screenPanel.repaint();
                } catch (IOException ex) {
                    System.err.println("Error receiving screen data: " + ex.getMessage());
                }
            }
        }
    }

    // A JPanel to display the remote screen
    private static class ScreenPanel extends JPanel implements MouseListener {

        private BufferedImage screenshot;
        private Socket socket;
        private DataOutputStream dos;

        public ScreenPanel(Socket socket) {
            this.socket = socket;
            try {
                dos = new DataOutputStream(socket.getOutputStream());
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
            }
        }

        public void setScreenshot(BufferedImage screenshot) {
            this.screenshot = screenshot;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                int remoteX = e.getX() * screenshot.getWidth() / getWidth();
                int remoteY = e.getY() * screenshot.getHeight() / getHeight();

                // Send mouse click coordinates to ScreenSender
                try {
                    dos.writeInt(remoteX);
                    dos.writeInt(remoteY);
                    dos.writeBoolean(true); // indicate a left mouse click
                    dos.flush();
                } catch (IOException ex) {
                    System.err.println("Error sending mouse click: " + ex.getMessage());
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
    }
}