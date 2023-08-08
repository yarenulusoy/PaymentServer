import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.ByteMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class PaymentServer {
    private static JLabel imageLabel;

    public static void main(String[] args) {
        int port = 443;
        JFrame frame = new JFrame("Payment Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        imageLabel = new JLabel();
        panel.add(imageLabel);

        JButton submitButton = new JButton("Gönder");
        panel.add(submitButton);

        frame.add(panel);
        frame.setVisible(true);

        submitButton.addActionListener(e -> {
            sendResponseToClient();
        });

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("PaymentServer is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connected to a client.");
                InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader); //get the client message

                String message = bufferedReader.readLine();

                System.out.println(message);
                processRequest(message);

                inputStreamReader.close();
                clientSocket.close();
                System.out.println("Client disconnected.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processRequest(String request) {
        try {
            JSONObject jsonObject = new JSONObject(request);
            String paymentType = jsonObject.getString("PaymentType");
            String qrContent = jsonObject.getString("Content");
            if (paymentType.equals("QRCode")) {

                generateAndDisplayQRCode(qrContent);
            } else {
                generateAmountImage(qrContent);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void generateAmountImage(String text) {
        int width = 300;
        int height = 100;

        try {
            String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "creditCard.png";
            BufferedImage resourceImage = ImageIO.read(new File(filePath));
            ;

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();

            graphics.drawImage(resourceImage, 0, 0, null);

            graphics.setColor(Color.white);
            Font font = new Font("Arial", Font.PLAIN, 16);
            graphics.setFont(font);

            FontMetrics metrics = graphics.getFontMetrics(font);
            int textWidth = metrics.stringWidth(text);
            int x = (width - textWidth) / 2;
            int y = height / 2 + metrics.getAscent();

            graphics.drawString(text, x, y);

            graphics.dispose();

            ImageIcon icon = new ImageIcon(image);
            imageLabel.setIcon(icon);

            imageLabel.setIcon(new ImageIcon(image));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateAndDisplayQRCode(String qrContent) {
        int width = 300;
        int height = 300;
        BufferedImage qrCodeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        try {
            ByteMatrix bitMatrix = new QRCodeWriter().encode(qrContent, BarcodeFormat.QR_CODE, width, height);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    qrCodeImage.setRGB(x, y, bitMatrix.get(x, y) == 0 ? Color.WHITE.getRGB() : Color.BLACK.getRGB());
                }
            }
            ImageIcon icon = new ImageIcon(qrCodeImage);
            imageLabel.setIcon(icon);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


    private static void sendResponseToClient() {
        // Yanıtı istemciye gönder
        String response = "{\"ResponseCode\":\"00\"}";

    }


}
