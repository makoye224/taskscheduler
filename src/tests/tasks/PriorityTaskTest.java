package tests.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskscheduler.java.exceptions.TaskException;
import taskscheduler.java.other.Duration;
import taskscheduler.java.other.TaskPriority;
import taskscheduler.java.tasks.PriorityTask;

import static org.junit.jupiter.api.Assertions.*;

class PriorityTaskTest {

    private PriorityTask priorityTask;
    private Duration duration;

    @BeforeEach
    public void setUp() {
        duration = Duration.ofMillis(2000);  // 2000 milliseconds duration
        priorityTask = new PriorityTask(duration, TaskPriority.HIGH);  // Create a priority task with HIGH priority
    }

    @Test
    public void testConstructor() {
        assertNotNull(priorityTask.getId(), "PriorityTask ID should not be null.");
        assertEquals(duration, priorityTask.getEstimatedDuration(), "Duration should be set correctly.");
        assertEquals(TaskPriority.HIGH, priorityTask.getPriority(), "Priority should be set to HIGH.");
    }

    @Test
    public void testCopyConstructor() {
        PriorityTask copiedTask = new PriorityTask(priorityTask);  // Use copy constructor
        assertEquals(priorityTask.getId(), copiedTask.getId(), "Copied task should have the same ID.");
        assertEquals(priorityTask.getEstimatedDuration(), copiedTask.getEstimatedDuration(), "Copied task should have the same duration.");
        assertEquals(priorityTask.getPriority(), copiedTask.getPriority(), "Copied task should have the same priority.");
        assertFalse(copiedTask.isCompleted(), "Copied task should have the same completion status (not completed by default).");
    }

    @Test
    public void testToString() {
        String expectedString = priorityTask.getId() + "; Duration is "
                + duration.getDuration().toString() + " ; Priority is " + TaskPriority.HIGH
                + "; is completed " + priorityTask.isCompleted();
        assertEquals(expectedString, priorityTask.toString(), "The toString method should return the correct string representation.");
    }

    @Test
    public void testSetPriority() {
        priorityTask.setPriority(TaskPriority.LOW);
        assertEquals(TaskPriority.LOW, priorityTask.getPriority(), "Priority should be set to LOW.");
    }

    @Test
    public void testSetTimeout() {
        long timeout = 5000L;
        priorityTask.setTimeout(timeout);
        assertEquals(timeout, priorityTask.getTimeout(), "Task timeout should be set correctly.");
    }

    @Test
    public void testExecute() throws Exception, TaskException {
        assertFalse(priorityTask.isCompleted(), "PriorityTask should not be completed initially.");
        priorityTask.execute();
        assertTrue(priorityTask.isCompleted(), "PriorityTask should be completed after execution.");
    }
}
