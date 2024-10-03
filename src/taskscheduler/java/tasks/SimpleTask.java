package taskscheduler.java.tasks;

import taskscheduler.java.other.Duration;
import taskscheduler.java.exceptions.TaskException;
import taskscheduler.java.other.TaskPriority;

import java.util.Set;
import java.util.UUID;

// SimpleTask implements Task with immutable properties and a completed status.
public class SimpleTask implements Task {
    private String id;  // Task identifier
    private final Duration estimatedDuration;  // Task duration
    private boolean completed;  // Completion status
    private TaskPriority priority = TaskPriority.LOW;
    private long timeout;

    // Constructor to set task ID and duration
    public SimpleTask(Duration duration) {
        this.id = UUID.randomUUID().toString();
        this.estimatedDuration = duration;
        this.completed = false;
    }

    // Copy constructor for defensive copying
    public SimpleTask(SimpleTask other) {
        // Copy the immutable field directly
        this.id = other.id;  // Since 'id' is immutable, we can directly copy it

        // Copy the immutable estimated duration (assuming Duration is immutable)
        this.estimatedDuration = other.estimatedDuration;

        // Copy mutable fields deeply
        this.completed = other.completed;  // Primitive type, direct copy
        this.priority = other.priority;  // Enum type, direct copy is safe as Enums are immutable
    }

    // Returns the task's ID
    @Override
    public String getId() {
        return id;
    }

    // Execute the task if not already completed
    @Override
    public void execute() throws TaskException {
        if (!completed) {
            // Pretend to process the task for its duration.
            for(int i = 0; i < 10; i++){
                i++;
            }
            setCompleted(true);
        }
    }

    // Returns true if the task is completed
    @Override
    public boolean isCompleted() {
        return completed;
    }

    // Returns the estimated task duration
    @Override
    public Duration getEstimatedDuration() {
        return estimatedDuration;
    }

    @Override
    public TaskPriority getPriority() {
        return priority;
    }

    @Override
    public Set<String> getDependencies() {
        return Set.of();
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    // Set task completion status
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    //set the ID
    public void setId(String id) {
        this.id = id;
    }
}
