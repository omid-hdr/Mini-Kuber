import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class DataBase {
    static Vector<Worker> workers = new Vector<>();
    static ArrayList<Task> tasks = new ArrayList<>();
    //static ArrayList<Task> fuckedTasks = new ArrayList<>();// task that it's needed worker dead!
    private static DataOutputStream outputToClient;

    public static synchronized Message createTask(String name, String workerName) {
        if (getTaskWithName(name) != null)
            return Message.TASK_EXIST;
        if (workerName != null && getTWorkerWithName(workerName) == null)
            return Message.WORKER_NOT_EXIST;
        new Task(name, workerName);
        return Message.SUCCESS;
    }

    public static synchronized void schedule() {
        for (Task task : tasks)
            if (getFreeWorker() == null)
                return;
            else if (task.getWorker() == null)
                allocate(task);
    }// tasks.removeAll(fuckedTasks);fuckedTasks.clear();


    private synchronized static void allocate(Task task) {
        if (task.getNeededWorkerId() == null)
            scheduling(getFreeWorker(), task);
        else {
            Worker worker = getTWorkerWithName(task.getNeededWorkerId());
            if (worker != null && worker.usage() < 1) scheduling(worker, task);//fuckedTasks.add(task);
        }
    }

    private synchronized static void scheduling(Worker worker, Task task) {
        worker.tasks.add(task);
        task.setWorker(worker);
        String info = "task: " + task.getTaskName() + "is scheduling on " + worker.getName();
        worker.letKnow(info);
        try {
            outputToClient.writeUTF(info);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized static Worker getFreeWorker() {
        double min = 1;
        Worker worker = null;
        for (Worker worker1 : workers) {
            if (worker1.isActive())
                if (worker1.usage() < min) {
                    worker = worker1;
                    min = worker1.usage();
                }
        }
        return worker;
    }

    public static synchronized String getTasks() {
        if (tasks.size() == 0)
            return "empty!\n";
        String output = "";
        for (Task task : tasks) {
            output += task.getTaskName();
            if (task.getWorker() == null)
                output += " Pending.\n";
            else
                output += task.getWorker().workerDetail();
        }
        return output;
    }

    public static synchronized Message remove(String taskName) {
        Task task = getTaskWithName(taskName);
        if (task == null)
            return Message.TASK_NOT_EXIST;
        Worker worker = task.getWorker();
        if (worker != null) {
            worker.tasks.remove(task);
        }
        tasks.remove(task);
        return Message.SUCCESS;
    }

    public static synchronized String getWorkers() {
        if (workers.size() == 0)
            return "empty!\n";
        String output = "";
        for (Worker worker : workers) {
            output += worker.workerDetail();
        }
        return output;
    }

    public static synchronized String deActive(String workerName) {
        Worker worker = getTWorkerWithName(workerName);
        if (worker == null)
            return Message.WORKER_NOT_EXIST.toString();
        if (!worker.isActive())
            return Message.WORKER_DEACTIVE_ALLREADY.toString();
        return worker.deActive();
    }

    public static synchronized String active(String workerName) {
        Worker worker = getTWorkerWithName(workerName);
        if (worker == null)
            return Message.WORKER_NOT_EXIST.toString();
        if (worker.isActive())
            return Message.WORKER_ACTIVE_ALLREADY.toString();
        worker.active();
        return Message.SUCCESS.toString();
    }

    public static synchronized Worker getTWorkerWithName(String workerName) {
        for (Worker worker : workers)
            if (worker.getName().equals(workerName))
                return worker;
        return null;
    }

    public static synchronized Task getTaskWithName(String name) {
        for (Task task : tasks)
            if (task.getTaskName().equals(name))
                return task;
        return null;
    }

    public static void setOutputToClient(DataOutputStream outputToClient) {
        DataBase.outputToClient = outputToClient;
    }
}
