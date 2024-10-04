package tests.tasks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import taskscheduler.java.exceptions.TaskException;
import taskscheduler.java.other.Duration;
import taskscheduler.java.other.TaskPriority;
import taskscheduler.java.tasks.SimpleTask;
import static org.junit.jupiter.api.Assertions.*;

class SimpleTaskTest {
    private SimpleTask task;
    private Duration duration;

    @BeforeEach
    public void setUp() {
        duration = Duration.ofMillis(1000);  // 1000 milliseconds duration
        task = new SimpleTask(duration);
    }

    @Test
    public void testGetId() {
        assertNotNull(task.getId(), "Task ID should not be null.");
        assertEquals(36, task.getId().length(), "Task ID should have the correct UUID format.");
    }

    @Test
    public void testGetDuration() {
        assertEquals(duration, task.getEstimatedDuration(), "Duration should be returned correctly.");
    }

    @Test
    public void testExecute() throws TaskException {
        assertFalse(task.isCompleted(), "Task should initially be not completed.");
        task.execute();
        assertTrue(task.isCompleted(), "Task should be completed after execution.");
    }

    @Test
    public void testExecute_Idempotent() throws TaskException {
        task.execute();
        assertTrue(task.isCompleted(), "Task should be completed after first execution.");
        task.setCompleted(false);  // Resetting task completion status
        task.execute();
        assertTrue(task.isCompleted(), "Task should be completed again after second execution.");
    }

    @Test
    public void testIsCompleted() {
        assertFalse(task.isCompleted(), "Newly created task should not be completed.");
        task.setCompleted(true);
        assertTrue(task.isCompleted(), "Task should be completed after setting completed to true.");
    }

    @Test
    public void testGetEstimatedDuration() {
        assertEquals(duration, task.getEstimatedDuration(), "The estimated duration should be returned correctly.");
    }

    @Test
    public void testSetPriority() {
        task.setPriority(TaskPriority.HIGH);
        assertEquals(TaskPriority.HIGH, task.getPriority(), "Task priority should be set to HIGH.");
    }

    @Test
    public void testSetTimeout() {
        long timeout = 5000L;
        task.setTimeout(timeout);
        assertEquals(timeout, task.getTimeout(), "Task timeout should be set correctly.");
    }
}
