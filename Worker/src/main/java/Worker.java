import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class Worker {
    final DataInputStream dataInputStream;
    final DataOutputStream dataOutputStream;
    final int MAX_TASK_NUMBER;//between 2 and 5.

    public Worker(String host, int port) throws IOException {
        MAX_TASK_NUMBER = 2;
        System.out.println("Starting Worker service with MAX_TASK_NUMBER:" + MAX_TASK_NUMBER);
        Socket socket = new Socket(host, port);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeUTF(String.valueOf(MAX_TASK_NUMBER));
        System.out.println(dataInputStream.readUTF());
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    dataOutputStream.writeUTF("7");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0, 3000); // Schedule the task to run every 5 seconds
        while (true)
            if (dataInputStream.available() != 0)
                System.out.println(dataInputStream.readUTF());
    }
}
