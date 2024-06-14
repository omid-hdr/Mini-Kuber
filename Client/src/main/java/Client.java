import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {


    final DataInputStream dataInputStream;
    final DataOutputStream dataOutputStream;


    public Client(String host, int port) throws IOException {
        System.out.println("Starting Client service...");
        Scanner scanner = new Scanner(System.in);
        Socket socket = new Socket(host, port);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeUTF("client");
        System.out.println(dataInputStream.readUTF());
        NotificationReceiver notificationReceiver = new NotificationReceiver(dataInputStream);
        notificationReceiver.start();
        while (true) {
            dataOutputStream.writeUTF(scanner.nextLine());
        }
    }

}
