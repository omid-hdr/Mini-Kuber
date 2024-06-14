import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;

public class Connection extends Thread {
    final DataInputStream dataInputStream;
    final DataOutputStream dataOutputStream;
    Socket socket;
    private Worker worker;
    private boolean isAlive;
    private boolean live;

    public Connection(Socket socket) throws IOException {
        System.out.println("New connection form: " + socket.getInetAddress() + ":" + socket.getPort());
        this.socket = socket;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        isAlive = true;
        this.live = true;
    }

    @Override
    public synchronized void run() {
        try {
            Type type = get_intro();
            if (type == Type.WORKER) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (isAlive)
                            isAlive = false;
                        else {
                            worker.dead();
                            live = false;
                            System.out.println(worker.getName() + " dead!");
                            timer.cancel();
                        }
                    }
                }, 0, 4000); // Schedule the task to run every 4 seconds
                while (live)
                    if (dataInputStream.available() != 0)
                        isAlive = true;
            } else {
                Timer timer1 = new Timer();
                timer1.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        DataBase.schedule();
                    }
                }, 0, 500); // Schedule the task to run every half seconds
                while (true) handle();
            }
        } catch (IOException e) {
            System.out.println("Connection " + socket.getInetAddress() + ":" + socket.getPort() + " lost!");
        }
    }

    private synchronized void handle() throws IOException {
        if (dataInputStream.available() != 0) {
            String command = dataInputStream.readUTF();
            Matcher matcher;
            if ((matcher = Command.getMatcher(command, Command.CREATE_TASK)) != null)
                dataOutputStream.writeUTF(DataBase.createTask(matcher.group("taskName"), matcher.group("workerName")).toString());
            else if (Command.getMatcher(command, Command.TASKS) != null)
                dataOutputStream.writeUTF(DataBase.getTasks());
            else if (Command.getMatcher(command, Command.WORKERS) != null)
                dataOutputStream.writeUTF(DataBase.getWorkers());
            else if ((matcher = Command.getMatcher(command, Command.DELETE_TASK)) != null)
                dataOutputStream.writeUTF(DataBase.remove(matcher.group("taskName")).toString());
            else if ((matcher = Command.getMatcher(command, Command.CORDON_NODE)) != null)
                dataOutputStream.writeUTF(DataBase.deActive(matcher.group("workerName")));
            else if ((matcher = Command.getMatcher(command, Command.UN_CORDON_NODE)) != null)
                dataOutputStream.writeUTF(DataBase.active(matcher.group("workerName")));
            else
                dataOutputStream.writeUTF("wrong command!");
        }
    }


    private Type get_intro() throws IOException {
        String intro = dataInputStream.readUTF();
        if (!intro.equals("client")) {
            worker = new Worker(this, Integer.parseInt(intro));
            return Type.WORKER;
        } else {
            dataOutputStream.writeUTF("7: You are registered as a client.");
            DataBase.setOutputToClient(dataOutputStream);
            return Type.CLIENT;
        }
    }
}