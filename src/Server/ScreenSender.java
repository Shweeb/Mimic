import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.ImageIO;

public class ScreenSender {

    public static final byte[] DELIMITER = new byte[]{-1, -1, -1, -1}; // Delimiter to mark the end of an image

    public static void main(String[] args) throws AWTException, IOException {
        // Set the destination IP address and port number
        String destinationIP = "localhost";
        int screenPort = 1234;
        int mousePort = 1235;

        // Create a robot to capture the screen
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

        try (ServerSocket screenServerSocket = new ServerSocket(screenPort);
            ServerSocket mouseServerSocket = new ServerSocket(mousePort)) {
            System.out.println("Server sockets created.");

            // Accept the connections
            Socket screenSocket = screenServerSocket.accept();
            System.out.println("Screen connection accepted.");

            Socket mouseSocket = mouseServerSocket.accept();
            System.out.println("Mouse connection accepted.");

            DataInputStream dis = new DataInputStream(mouseSocket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(screenSocket.getOutputStream());

            // Check for incoming mouse input data
            // Start a separate thread for handling mouse events
            Thread mouseThread = new Thread(() -> {
                while (true) {
                    try {
                        System.out.println("about to read mouse stuff");
                        int remoteX = dis.readInt();
                        int remoteY = dis.readInt();
                        boolean isLeftClick = dis.readBoolean();
                        System.out.println("Mouse input received.");

                        // Perform the mouse click on the server
                        int localX = remoteX * screenRect.width / 1920;
                        int localY = remoteY * screenRect.height / 1080;
                        robot.mouseMove(localX, localY);

                        if (isLeftClick) {
                            robot.mousePress(InputEvent.BUTTON1_MASK);
                            robot.mouseRelease(InputEvent.BUTTON1_MASK);
                            System.out.println("Mouse click performed.");
                        }
                    } catch (IOException ex) {
                        System.err.println("Error receiving mouse input: " + ex.getMessage());
                    }
                }
            });

            mouseThread.start();

            // Continuously capture and send screen data
            while (true) {
                System.out.println("1");
                // Capture the screen data
                BufferedImage screenshot = robot.createScreenCapture(screenRect);

                System.out.println("2");
                // Send the screen data over the network
                // outputStream.writeInt(screenshot.getWidth());
                // outputStream.writeInt(screenshot.getHeight());
                // for (int y = 0; y < screenshot.getHeight(); y++) {
                //     for (int x = 0; x < screenshot.getWidth(); x++) {
                //         outputStream.writeInt(screenshot.getRGB(x, y));
                //     }
                // }

                // Send the screen data over the network
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(screenshot, "jpg", byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                outputStream.writeInt(imageBytes.length);
                outputStream.write(imageBytes);
                outputStream.write(DELIMITER);
                outputStream.flush();

                System.out.println("3");
                // Sleep for 100 milliseconds before capturing the screen again
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    // Ignore interruptions
                }

                System.out.println("4");
            }
        }
    }
}