package taskscheduler.java.tasks;

import taskscheduler.java.other.Duration;
import taskscheduler.java.other.TaskPriority;

public class PriorityTask extends SimpleTask {

    public PriorityTask(Duration duration, TaskPriority priority) {
        super(duration);
        setPriority(priority);
    }

    // Copy constructor for defensive copying
    public PriorityTask(PriorityTask otherPriorityTask) {
        // Defensive copying: Copy the ID, duration, and priority of the other task
        super(new Duration(otherPriorityTask.getEstimatedDuration().getDuration()));  // Deep copy the duration
        setPriority(otherPriorityTask.getPriority());  // Copy priority (safe, since it's an enum)
        setId(otherPriorityTask.getId());
    }

    @Override
    public String toString() {
        return  this.getId() + "; Duration is  " + this.getEstimatedDuration().getDuration().toString() + " ; Priority is " + this.getPriority() + "; is completed " + this.isCompleted();
    }
}
