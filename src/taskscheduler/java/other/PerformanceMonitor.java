package taskscheduler.java.other;

import taskscheduler.java.servers.Server;
import taskscheduler.java.tasks.Task;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.Logger;

public class PerformanceMonitor {
    private final Server server;  // Monitor a single server's performance
    private static final Logger logger = Logger.getLogger(PerformanceMonitor.class.getName());
    private final AlertSystem alertSystem;  // Use AlertSystem for sending alerts

    // Define threshold values for triggering alerts
    private static final double FAILURE_RATE_THRESHOLD = 0.10;  // 10% failure rate threshold
    private static final int TASK_QUEUE_THRESHOLD = 10;  // Task queue size threshold
    private static final BigInteger TOTAL_LOAD_THRESHOLD = BigInteger.valueOf(20000);  // Load threshold in milliseconds

    public PerformanceMonitor(Server server, AlertSystem alertSystem) {
        this.server = server;
        this.alertSystem = alertSystem;
    }

    // Calculate the average execution time for completed tasks
    public double getAverageExecutionTime() {
        List<Task> completedTasks = server.getCompletedTasks();
        return completedTasks.stream()
                .mapToLong(task -> task.getEstimatedDuration().getDuration().longValue())
                .average()
                .orElse(0);
    }

    // Calculate the success rate (ratio of completed tasks to total tasks)
    public double getSuccessRate() {
        int totalTasks = server.getCompletedTasks().size() + server.getFailedTasks().size();
        return totalTasks > 0 ? (double) server.getCompletedTasks().size() / totalTasks : 0;
    }

    // Calculate the failure rate (ratio of failed tasks to total tasks)
    public double getFailureRate() {
        int totalTasks = server.getCompletedTasks().size() + server.getFailedTasks().size();
        return totalTasks > 0 ? (double) server.getFailedTasks().size() / totalTasks : 0;
    }

    // Calculate the total load processed by the server (sum of durations of all tasks)
    public BigInteger getTotalLoad() {
        return server.getCompletedTasks().stream()
                .map(task -> task.getEstimatedDuration().getDuration())
                .reduce(BigInteger.ZERO, BigInteger::add);
    }

    // Monitor system performance and trigger alerts
    public void monitorAndAlert() {
        // Check if the task queue size exceeds the threshold
        int taskQueueSize = server.getTasks().size();
        alertSystem.checkForHighTaskQueue(taskQueueSize, TASK_QUEUE_THRESHOLD);

        // Check if the failure rate exceeds the threshold
        double failureRate = getFailureRate();
        alertSystem.checkForHighFailureRate(failureRate, FAILURE_RATE_THRESHOLD);

        // Check if the total load exceeds the threshold
        BigInteger totalLoad = getTotalLoad();
        alertSystem.checkForHighTotalLoad(totalLoad.longValue(), TOTAL_LOAD_THRESHOLD.longValue());
    }

    // Print all the collected statistics
    public void printStatistics() {
        System.out.println("===== Server Performance Statistics =====");
        System.out.println("Average Execution Time: " + getAverageExecutionTime() + " ms");
        System.out.println("Success Rate: " + (getSuccessRate() * 100) + "%");
        System.out.println("Failure Rate: " + (getFailureRate() * 100) + "%");
        System.out.println("Total Load Processed: " + getTotalLoad() + " ms");
    }
}
