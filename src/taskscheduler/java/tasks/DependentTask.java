package taskscheduler.java.tasks;

import taskscheduler.java.other.Duration;
import taskscheduler.java.exceptions.TaskException;
import taskscheduler.java.other.TaskPriority;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DependentTask extends PriorityTask implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // Logger instance for this class
    private static final Logger logger = Logger.getLogger(DependentTask.class.getName());

    // Use a HashSet to store dependent task IDs
    private final Set<String> dependentTaskIds;

    // No-argument constructor required for deserialization
    public DependentTask() {
        super(new Duration(BigInteger.valueOf(3000)), TaskPriority.LOW);
        this.dependentTaskIds = new HashSet<>();
    }

    // Constructor that allows dependent tasks to be optional
    public DependentTask(Duration duration, TaskPriority priority) {
        super(duration, priority);
        this.dependentTaskIds = new HashSet<>();  // Initialize to an empty set by default
    }

    // Constructor that accepts a set of dependent task IDs
    public DependentTask(Duration duration, TaskPriority priority, Set<String> dependentTaskIds) {
        super(duration, priority);
        // Use defensive copying for the dependent task IDs
        this.dependentTaskIds = (dependentTaskIds != null) ? new HashSet<>(dependentTaskIds) : new HashSet<>();
    }

    // Constructor for defensive copying
    public DependentTask(DependentTask dependentTask) {
        super(dependentTask.getEstimatedDuration(), dependentTask.getPriority());
        // Defensive copying of the dependent task IDs
        this.dependentTaskIds = new HashSet<>(dependentTask.getDependencies());
    }

    // Add a dependent task ID
    public void addDependentTask(String dependentTaskId) {
        dependentTaskIds.add(dependentTaskId);
    }

    // Override getDependencies to return a copy of the dependent task IDs
    @Override
    public Set<String> getDependencies() {
        return new HashSet<>(dependentTaskIds);  // Return a copy to prevent modification from outside
    }

    // Execute the task if not already completed, with timeout management
    @Override
    public void execute() throws TaskException {
        if (!this.isCompleted()) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<?> future = executor.submit(() -> {
                logger.log(Level.INFO, "Task {0} started execution.", this.getId());
                simulateTaskExecution();
                logger.log(Level.INFO, "Task {0} finished execution.", this.getId());
                setCompleted(true);
            });

            try {
                // Wait for the task to complete or timeout
                future.get(this.getTimeout(), TimeUnit.MILLISECONDS);
                if (this.isCompleted()) {
                    logger.log(Level.INFO, "Task {0} completed successfully.", this.getId());
                }
            } catch (Exception e) {  // Catch all exceptions and wrap them in TaskException
                future.cancel(true);  // Cancel the task in case of any exception
                logger.log(Level.SEVERE, "Task {0} failed: {1}", new Object[]{this.getId(), e.getMessage()});
                throw new TaskException("Task execution failed: " + e.getMessage(), e);
            } finally {
                executor.shutdown();  // Cleanly shut down the executor service
                logger.log(Level.INFO, "Executor service for task {0} has been shut down.", this.getId());
            }
        }
    }


    public void simulateTaskExecution() {
        logger.log(Level.INFO, "Task execution started.");

        // Introduce a random failure with 50% chance before task execution
        if (Math.random() < 0.5) {  // 50% chance of failure
            logger.log(Level.SEVERE, "Task execution failed randomly.");
            throw new RuntimeException("Task execution failed randomly.");
        }

        // Simulate the task execution to match the actual duration
        try {
            Thread.sleep(this.getEstimatedDuration().getDuration().longValue());  // Sleep for the task's duration
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Restore interrupted status
            logger.log(Level.SEVERE, "Task execution interrupted.");
        }

        logger.log(Level.INFO, "Task execution completed successfully.");
    }


}
