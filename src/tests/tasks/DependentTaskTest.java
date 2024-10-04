package tests.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import taskscheduler.java.exceptions.TaskException;
import taskscheduler.java.other.Duration;
import taskscheduler.java.other.TaskPriority;
import taskscheduler.java.tasks.DependentTask;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DependentTaskTest {

    private DependentTask dependentTask;
    private Duration duration;
    private static final Logger logger = Logger.getLogger(DependentTask.class.getName());

    @BeforeEach
    public void setUp() {
        duration = Duration.ofMillis(3000);  // 3000 milliseconds duration
        dependentTask = new DependentTask(duration, TaskPriority.HIGH);  // Create DependentTask with HIGH priority
    }

    @Test
    public void testAddDependentTask() {
        dependentTask.addDependentTask("task1");
        Set<String> dependencies = dependentTask.getDependencies();
        assertTrue(dependencies.contains("task1"), "Dependent task should be added correctly.");
    }

    @Test
    public void testConstructorWithDependencies() {
        Set<String> dependencies = new HashSet<>();
        dependencies.add("task1");
        dependencies.add("task2");
        DependentTask taskWithDependencies = new DependentTask(duration, TaskPriority.MEDIUM, dependencies);

        assertEquals(2, taskWithDependencies.getDependencies().size(), "Task should have two dependencies.");
        assertTrue(taskWithDependencies.getDependencies().contains("task1"), "Dependencies should include task1.");
        assertTrue(taskWithDependencies.getDependencies().contains("task2"), "Dependencies should include task2.");
    }

    @Test
    public void testCopyConstructor() {
        dependentTask.addDependentTask("task1");
        dependentTask.addDependentTask("task2");

        DependentTask copiedTask = new DependentTask(dependentTask);  // Use copy constructor
        assertEquals(dependentTask.getDependencies(), copiedTask.getDependencies(), "Copied task should have the same dependencies.");
        assertEquals(dependentTask.getEstimatedDuration(), copiedTask.getEstimatedDuration(), "Copied task should have the same duration.");
        assertEquals(dependentTask.getPriority(), copiedTask.getPriority(), "Copied task should have the same priority.");
    }

    @Test
    public void testExecuteWithSuccess() throws TaskException {
        // Create a task with a short duration and long timeout to avoid timeout issues
        DependentTask spyTask = Mockito.spy(new DependentTask(Duration.ofMillis(50), TaskPriority.HIGH));
        spyTask.setTimeout(500);  // Set a timeout longer than the task duration to avoid TimeoutException

        // Mock simulateTaskExecution() to avoid random failures
        doNothing().when(spyTask).simulateTaskExecution();

        // Execute the task and verify it completes successfully
        spyTask.execute();
        assertTrue(spyTask.isCompleted(), "Task should be completed after successful execution.");
    }

    @Test
    public void testExecuteWithFailure() {
        DependentTask spyTask = Mockito.spy(dependentTask);

        // Mock simulateTaskExecution() to throw an exception
        doThrow(new RuntimeException("Simulated failure")).when(spyTask).simulateTaskExecution();

        TaskException exception = assertThrows(TaskException.class, spyTask::execute,
                "TaskException should be thrown if task fails.");
        assertFalse(spyTask.isCompleted(), "Task should not be marked as completed after failure.");
    }


    @Test
    public void testTimeoutHandling() throws TaskException {
        // Create a task with a duration that exceeds the timeout
        DependentTask taskWithTimeout = new DependentTask(Duration.ofMillis(100), TaskPriority.HIGH);
        taskWithTimeout.setTimeout(50);  // Set timeout shorter than the duration

        // Expect TaskException to be thrown when task execution times out
        TaskException exception = assertThrows(TaskException.class, taskWithTimeout::execute,
                "TaskException should be thrown if execution times out.");

        // Check if the exception message indicates task failure due to timeout
        assertTrue(exception.getMessage().contains("Task execution failed"),
                "Exception message should indicate task failure.");
    }
}
