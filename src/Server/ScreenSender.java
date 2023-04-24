import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import javax.imageio.ImageIO;

public class ScreenSender {

    public static void main(String[] args) throws AWTException, IOException {
        // Set the destination IP address and port number
        String destinationIP = "localhost";
        int destinationPort = 1234;

        // Create a robot to capture the screen
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

        // Create a socket for receiving mouse input data from the client
        ServerSocket mouseSocket = new ServerSocket(1235);

        // Continuously capture and send screen data
        while (true) {
            // Capture the screen data
            BufferedImage screenshot = robot.createScreenCapture(screenRect);

            System.out.println("1");

            // Send the screen data over the network
            try (Socket socket = new Socket(destinationIP, destinationPort)) {
                OutputStream outputStream = socket.getOutputStream();
                ImageIO.write(screenshot, "jpg", outputStream);
            } catch (IOException ex) {
                System.err.println("Error sending screen data: " + ex.getMessage());
            }

            System.out.println("2");

            // Check for incoming mouse input data
            try {
                System.out.println("after try");
                Socket mouseClient = mouseSocket.accept(); // TODO find out why it gets stuck here
                System.out.println("Acepted Mouse Socket");
                DataInputStream inputStream = new DataInputStream(mouseClient.getInputStream());
                System.out.println("Data Input Strem Defined");
                int remoteX = inputStream.readInt();
                System.out.println("Reading numerical shit 1");
                int remoteY = inputStream.readInt();
                System.out.println("Reading numerical shit 2");
                boolean isLeftClick = inputStream.readBoolean();
                System.out.println("reading boolean type shit");

                System.out.println("after reading input");

                // Perform the mouse click on the server
                int localX = remoteX * screenRect.width / 1920;
                int localY = remoteY * screenRect.height / 1080;
                robot.mouseMove(localX, localY);
                System.out.println("After moving mouse");
                if (isLeftClick) {
                    robot.mousePress(InputEvent.BUTTON1_MASK);
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                    System.out.println("after making mouse click");
                }

                mouseClient.close(); // close the client socket
            } catch (SocketTimeoutException ex) {
                System.out.println("no connetion");
                // no connection within the timeout period, continue with the loop
            } catch (IOException ex) {
                System.out.println("no mouse input");
                System.err.println("Error receiving mouse input: " + ex.getMessage());
            }

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