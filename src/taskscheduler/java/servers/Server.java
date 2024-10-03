package taskscheduler.java.servers;

import taskscheduler.java.other.RetryPolicy;
import taskscheduler.java.exceptions.SchedulerFullException;
import taskscheduler.java.exceptions.ServerException;
import taskscheduler.java.exceptions.TaskException;
import taskscheduler.java.tasks.Task;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public static final BigInteger DEFAULT_MAX_CAPACITY = BigInteger.valueOf(30000);
    private BigInteger remainingCapacity = DEFAULT_MAX_CAPACITY;

    private final RetryPolicy retryPolicy;  // Retry policy for tasks

    // PriorityQueue to hold tasks, ordered by their priority (HIGH -> MEDIUM -> LOW)
    private final PriorityBlockingQueue<Task> tasks = new PriorityBlockingQueue<>(
            11,
            (t1, t2) -> t2.getPriority().ordinal() - t1.getPriority().ordinal()
    );

    // List to track completed tasks
    private final List<Task> completedTasks = new ArrayList<>();

    // List to track failed tasks
    private final List<Task> failedTasks = new ArrayList<>();

    public Server(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;  // Set retry policy
    }

    // Method to add a task to the server's task list
    public void addTask(Task task) throws ServerException {
        Objects.requireNonNull(task, "Task cannot be null");

        if (getRemainingCapacity().intValue() - task.getEstimatedDuration().getDuration().intValue() < 0) {
            throw new SchedulerFullException("This server is full");
        }

        tasks.add(task);  // Safely adds the non-null task
        remainingCapacity = remainingCapacity.subtract(task.getEstimatedDuration().getDuration());
        logger.log(Level.INFO, "Task {0} added to server. Remaining capacity: {1}", new Object[]{task.getId(), remainingCapacity});
    }

    public BigInteger getRemainingCapacity() {
        return new BigInteger(remainingCapacity.toString());
    }

    public void setRemainingCapacity(BigInteger remainingCapacity) {
        this.remainingCapacity = remainingCapacity;
    }

    // Check if a task's dependencies are all completed
    private boolean areDependenciesCompleted(Task task) {
        Set<String> dependencies = task.getDependencies();
        for (String depId : dependencies) {
            boolean dependencyMet = completedTasks.stream().anyMatch(t -> t.getId().equals(depId));
            if (!dependencyMet) {
                logger.log(Level.WARNING, "Task {0} cannot be executed because dependency {1} is not completed.", new Object[]{task.getId(), depId});
                return false;
            }
        }
        return true;
    }

    // Method to execute a task with retries
    private boolean executeTaskWithRetries(Task task) {
        int attempts = 0;
        boolean taskCompleted = false;

        // Retry loop for task execution based on retry policy
        while (attempts < retryPolicy.getMaxRetries() && !taskCompleted) {
            try {
                task.execute();  // Attempt to execute the task
                if (task.isCompleted()) {
                    taskCompleted = true;
                    setRemainingCapacity(getRemainingCapacity().add(task.getEstimatedDuration().getDuration()));
                    logger.log(Level.INFO, "Task {0} completed successfully on attempt {1}. Remaining capacity: {2}",
                            new Object[]{task.getId(), attempts + 1, remainingCapacity});
                }
            } catch (TaskException e) {
                attempts++;
                logger.log(Level.WARNING, "Task {0} failed on attempt {1}. {2} retries remaining.",
                        new Object[]{task.getId(), attempts, retryPolicy.getMaxRetries() - attempts});

                // Delay between retries, if specified
                if (retryPolicy.getDelay(attempts) > 0) {
                    try {
                        Thread.sleep(retryPolicy.getDelay(attempts));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        return taskCompleted;
    }

    // Executes all tasks in the queue, returns a list of successfully completed tasks
    public List<Task> executeTasks() throws ServerException {
        List<Task> completedTasksThisSession = new ArrayList<>();  // List to store successfully completed tasks in this session
        List<Task> failedTasksThisSession = new ArrayList<>();  // Local list to track failed tasks within this execution session

        tasks.forEach(task -> {
            if (areDependenciesCompleted(task)) {
                if (executeTaskWithRetries(task)) {
                    completedTasksThisSession.add(task);
                } else {
                    failedTasksThisSession.add(task);
                    logger.log(Level.SEVERE, "Task {0} failed after {1} attempts.", new Object[]{task.getId(), retryPolicy.getMaxRetries()});
                }
            } else {
                failedTasksThisSession.add(task);  // Add task to failed list if dependencies are not met
                logger.log(Level.WARNING, "Task {0} skipped due to unmet dependencies.", task.getId());
            }
        });

        tasks.removeAll(completedTasksThisSession);  // Remove completed tasks from queue
        this.failedTasks.addAll(failedTasksThisSession);  // Add failed tasks to the server-level failed tasks list
        this.completedTasks.addAll(completedTasksThisSession);  // Add completed tasks to the server's completed task list
        logger.log(Level.INFO, "{0} tasks completed, {1} tasks failed.", new Object[]{completedTasksThisSession.size(), failedTasksThisSession.size()});
        return completedTasksThisSession;  // Return the list of successfully completed tasks in this session
    }

    // Retrieve all completed tasks
    public List<Task> getCompletedTasks() {
        return new ArrayList<>(completedTasks);  // Return a defensive copy of completed tasks
    }

    // Retrieve a list of tasks that failed to complete
    public List<Task> getFailedTasks() {
        return new ArrayList<>(failedTasks);  // Return a defensive copy of failed tasks
    }

    // Calculate the weighted total load of the server
    public BigInteger getTotalLoad() {
        return tasks.stream()
                .map(task -> task.getEstimatedDuration().getDuration())  // Get the task duration
                .reduce(BigInteger.ZERO, BigInteger::add);  // Sum up all the weighted loads
    }

    // Return tasks (for reference or display purposes)
    public PriorityBlockingQueue<Task> getTasks() {
        return new PriorityBlockingQueue<>(tasks);
    }

    @Override
    public String toString() {
        return tasks.toString();
    }
}
