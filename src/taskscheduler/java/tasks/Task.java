package taskscheduler.java.tasks;

import taskscheduler.java.other.Duration;
import taskscheduler.java.exceptions.TaskException;
import taskscheduler.java.other.TaskPriority;

import java.util.Set;

public interface Task {
    public String getId();
    public void execute() throws TaskException;
    public boolean isCompleted();
    public Duration getEstimatedDuration();
    public TaskPriority getPriority();
    public Set<String> getDependencies();
    public long getTimeout();
    public void setTimeout(long timeout);
}
