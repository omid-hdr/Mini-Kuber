import java.io.IOException;
import java.util.ArrayList;

public class Worker {
    private static int workersSize = 1;
    ArrayList<Task> tasks;
    private final Connection connection;
    private final String name;
    private boolean active;
    private final int MAX_TASK_NUMBER;

    public Worker(Connection connection, int MAX_TASK_NUMBER) {
        this.connection = connection;
        this.name = "worker" + workersSize++;
        this.MAX_TASK_NUMBER = MAX_TASK_NUMBER;
        this.active = true;
        tasks = new ArrayList<>();
        DataBase.workers.add(this);
        letKnow("worker with id:" + name + " and MAX_TASK_NUMBER:" + MAX_TASK_NUMBER);
    }

    public synchronized double usage() {
        return (float)(tasks.size()) / MAX_TASK_NUMBER;
    }

    public synchronized void letKnow(String info) {
        try {
            connection.dataOutputStream.writeUTF(info);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public String workerDetail() {
        String status = " ";
        if (!active)
            status = " unSchedulable";
        return " name: " + name + " port: " + connection.socket.getPort() +
                " id: " + connection.socket.getInetAddress() + status + ".\n";
    }

    public String deActive() {
        this.active = false;
        StringBuilder output = new StringBuilder();
        for (Task task : tasks) {
            output.append(task.getTaskName()).append(" ");
            task.setWorker(null);
        }
        tasks.clear();
        return output.toString();
    }

    public void active() {
        this.active = true;
    }

    public void dead() {
        for (Task task : tasks)
            task.setWorker(null);
        tasks.clear();
        DataBase.workers.remove(this);
    }


}
