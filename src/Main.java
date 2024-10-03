import taskscheduler.java.*;
import taskscheduler.java.exceptions.SchedulerException;
import taskscheduler.java.exceptions.ServerException;
import taskscheduler.java.exceptions.TaskException;
import taskscheduler.java.other.Duration;
import taskscheduler.java.other.RetryPolicy;
import taskscheduler.java.servers.RemoteServer;
import taskscheduler.java.servers.Server;
import taskscheduler.java.tasks.DependentTask;
import taskscheduler.java.tasks.Task;
import taskscheduler.java.other.TaskPriority;

import java.math.BigInteger;
import java.util.concurrent.PriorityBlockingQueue;

public class Main {

    // Print tasks in the server queue and their statuses
    public static void printTasksInServer(Server server) {
        System.out.println("Tasks in Server:");
        PriorityBlockingQueue<Task> tasks = server.getTasks();
        while (!tasks.isEmpty()) {
            Task task = tasks.poll();
            System.out.println(task);
        }
        System.out.println("Completed Tasks:");
        for (Task completedTask : server.getCompletedTasks()) {
            System.out.println(completedTask.getId() + " - Completed");
        }
        System.out.println("Failed Tasks:");
        for (Task failedTask : server.getFailedTasks()) {
            System.out.println(failedTask.getId() + " - Failed");
        }
    }

    public static void main(String[] args) throws ServerException, SchedulerException, TaskException {

        // Create a retry policy for the servers (3 retries, 2-second delay, exponential backoff enabled)
        RetryPolicy retryPolicy = new RetryPolicy(2, 2, true);

        // Create local server and remote server
        Server localServer = new Server(retryPolicy);
        RemoteServer remoteServer = new RemoteServer("localhost", 12345, retryPolicy);  // Remote server on localhost:12345

        // Task with timeout of 5 seconds but will take 7 seconds to execute (expected to fail)
        DependentTask timeoutTask = new DependentTask(new Duration(BigInteger.valueOf(7000)), TaskPriority.HIGH);
        timeoutTask.setTimeout(5000);  // Timeout set to 5 seconds

        // Create a chain of 3 dependent tasks A, B, and C
        DependentTask taskA = new DependentTask(new Duration(BigInteger.valueOf(3000)), TaskPriority.HIGH); // Takes 3 seconds
        DependentTask taskB = new DependentTask(new Duration(BigInteger.valueOf(3000)), TaskPriority.MEDIUM); // Takes 7 seconds (will timeout)
        DependentTask taskC = new DependentTask(new Duration(BigInteger.valueOf(2000)), TaskPriority.LOW); // Takes 2 seconds

        // Set timeouts for the dependent tasks
        taskA.setTimeout(5000);  // Task A timeout is 5 seconds
        taskB.setTimeout(5000);  // Task B timeout is 5 seconds (will exceed)
        taskC.setTimeout(5000);  // Task C timeout is 5 seconds

        // Add dependencies: Task C depends on B, Task B depends on A
        taskB.addDependentTask(taskA.getId());
        taskC.addDependentTask(taskB.getId());

        // Create the task scheduler
        TaskScheduler scheduler = new TaskScheduler();

        // Add the local and remote servers to the scheduler
        scheduler.addServer(localServer);
        scheduler.addServer(remoteServer);

        // Schedule the tasks on both servers
        scheduler.scheduleTask(timeoutTask);  // Task that will timeout (can be on local or remote)
        scheduler.scheduleTask(taskA);  // Task A (can be on local or remote)
        scheduler.scheduleTask(taskB);  // Task B (depends on A)
        scheduler.scheduleTask(taskC);  // Task C (depends on B)

        // Execute all scheduled tasks (this will execute both on local and remote servers)
        scheduler.executeAll();

        // Print the status of each server after task execution
        PriorityBlockingQueue<Server> servers = scheduler.getServers();
        while (!servers.isEmpty()) {
            Server server = servers.poll();
            System.out.println("Server Type: " + (server instanceof RemoteServer ? "Remote" : "Local"));
            printTasksInServer(server);
            System.out.println("====================================================================");
        }
    }
}
