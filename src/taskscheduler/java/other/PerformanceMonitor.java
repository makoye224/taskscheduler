package taskscheduler.java.other;

import taskscheduler.java.servers.Server;
import taskscheduler.java.tasks.Task;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.Logger;

public class PerformanceMonitor {
    private final Server server;  // Use the Server instance instead of a list of tasks
    private static final Logger logger = Logger.getLogger(PerformanceMonitor.class.getName());

    public PerformanceMonitor(Server server) {
        this.server = server;
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

    // Print all the collected statistics
    public void printStatistics() {
        System.out.println("===== Server Performance Statistics =====");
        System.out.println("Average Execution Time: " + getAverageExecutionTime() + " ms");
        System.out.println("Success Rate: " + (getSuccessRate() * 100) + "%");
        System.out.println("Failure Rate: " + (getFailureRate() * 100) + "%");
        System.out.println("Total Load Processed: " + getTotalLoad() + " ms");
    }
}
