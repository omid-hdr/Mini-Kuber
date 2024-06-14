import com.google.gson.Gson;

public class Task {
    private final String taskName;
    private final String neededWorkerId;
    private Worker worker;

    public Task(String taskName, String workerId) {
        this.taskName = taskName;
        this.neededWorkerId = workerId;
        worker = null;
        DataBase.tasks.add(this);
    }

    public String getTaskName() {
        return taskName;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public String getNeededWorkerId() {
        return neededWorkerId;
    }
}
