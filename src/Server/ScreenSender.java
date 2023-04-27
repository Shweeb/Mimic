import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;
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
                        // Get the default toolkit
                        Toolkit toolkit = Toolkit.getDefaultToolkit();

                        // Get the screen size as a dimension object
                        Dimension screenSize = toolkit.getScreenSize();

                        // Get the screen width and height from the dimension object
                        int screenWidth = screenSize.width;
                        int screenHeight = screenSize.height;

                        // Get the relative mouse position from the panel
                        double  relativeX = dis.readDouble();
                        double  relativeY = dis.readDouble();
                        // int button = dis.readInt();
                        boolean isLeftClick = dis.readBoolean();

                        // Scale the numbers to deal with decimals
                        int scaledX = (int) (relativeX * 10000);
                        int scaledY = (int) (relativeY * 10000);

                        int localX = (int) (scaledX * screenWidth / 10000.0);
                        int localY = (int) (scaledY * screenHeight / 10000.0);

                        // Perform the mouse click on the server
                        robot.mouseMove(localX, localY);
                        System.out.println("Screen Size X:" + screenWidth + " Y:" + screenHeight);
                        System.out.println("Receieved Relative values X:" + relativeX + " Y:" + relativeY);
                        System.out.println("Mouse click position on screen X:" + localX + " Y:" + localY + "\n");

                        // if (button == MouseEvent.BUTTON1 || button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
                        //     int mask;
                        //     switch (button) {
                        //         case MouseEvent.BUTTON1: // Is left click
                        //             mask = InputEvent.BUTTON1_MASK;
                        //             break;
                        //         case MouseEvent.BUTTON2: // Is middle click
                        //             mask = InputEvent.BUTTON2_MASK;
                        //             break;
                        //         case MouseEvent.BUTTON3: // Is right click
                        //             mask = InputEvent.BUTTON3_MASK;
                        //             break;
                        //         default:
                        //             mask = 0;
                        //             break;
                        //     }

                        //     // Perform mouse action
                        //     robot.mousePress(mask);
                        //     robot.mouseRelease(mask);
                        // }

                        if (isLeftClick) {
                            robot.mousePress(InputEvent.BUTTON1_MASK);
                            robot.mouseRelease(InputEvent.BUTTON1_MASK);
                        }

                    } catch (IOException ex) {
                        System.err.println("Error receiving mouse input: " + ex.getMessage());
                        break;
                    }
                }
            });

            mouseThread.start();

            // Continuously capture and send screen data
            while (true) {
                // Capture the screen data
                BufferedImage screenshot = robot.createScreenCapture(screenRect);

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

                // Sleep for 100 milliseconds before capturing the screen again
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    // Ignore interruptions
                }
            }
        }
    }
}