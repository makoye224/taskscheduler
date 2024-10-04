package tests.servers;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import taskscheduler.java.exceptions.SchedulerFullException;
import taskscheduler.java.exceptions.ServerException;
import taskscheduler.java.exceptions.TaskException;
import taskscheduler.java.other.AlertSystem;
import taskscheduler.java.other.PerformanceMonitor;
import taskscheduler.java.other.RetryPolicy;
import taskscheduler.java.servers.Server;
import taskscheduler.java.tasks.Task;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServerTest {

    private Server server;
    private RetryPolicy retryPolicy;
    private PerformanceMonitor performanceMonitor;
    private AlertSystem alertSystem;
    private Task task;

    @BeforeEach
    public void setUp() {
        // Mock dependencies
        retryPolicy = Mockito.mock(RetryPolicy.class);
        alertSystem = Mockito.mock(AlertSystem.class);
        performanceMonitor = Mockito.mock(PerformanceMonitor.class);

        // Spy the PerformanceMonitor to ensure monitor methods are called
        performanceMonitor = Mockito.spy(new PerformanceMonitor(server, alertSystem));

        // Create a server instance with mocks
        server = new Server(retryPolicy);

        // Mock task
        task = Mockito.mock(Task.class);
        when(task.getEstimatedDuration()).thenReturn(new taskscheduler.java.other.Duration(BigInteger.valueOf(5000)));
        when(task.getPriority()).thenReturn(taskscheduler.java.other.TaskPriority.MEDIUM);
        when(task.getId()).thenReturn("task1");
    }

    @Test
    public void testAddTask() throws ServerException {
        // Add task to the server
        server.addTask(task);

        // Check if the task has been added and remaining capacity updated
        assertEquals(BigInteger.valueOf(25000), server.getRemainingCapacity());
        assertTrue(server.getTasks().contains(task));

        // Verify that monitoring and alerting was triggered
        verify(performanceMonitor).monitorAndAlert();
    }

    @Test
    public void testAddTaskWhenFull() {
        // Reduce server's capacity to simulate a full server
        server.setRemainingCapacity(BigInteger.valueOf(1000));

        // Mock task duration greater than remaining capacity
        when(task.getEstimatedDuration()).thenReturn(new taskscheduler.java.other.Duration(BigInteger.valueOf(5000)));

        // Expect an exception when adding the task
        assertThrows(SchedulerFullException.class, () -> server.addTask(task));

        // Verify no monitoring or alerting was triggered
        verify(performanceMonitor, never()).monitorAndAlert();
    }

    @Test
    public void testExecuteTasksSuccess() throws ServerException, TaskException {
        // Setup task to execute successfully
        when(task.isCompleted()).thenReturn(true);

        // Add task and execute
        server.addTask(task);
        List<Task> completedTasks = server.executeTasks();

        // Verify task execution and monitor/alert call
        assertEquals(1, completedTasks.size());
        assertTrue(completedTasks.contains(task));
        verify(performanceMonitor, times(2)).monitorAndAlert();  // Once on addTask and once on execute

        // Verify task completion updated server capacity
        assertEquals(BigInteger.valueOf(30000), server.getRemainingCapacity());
    }

    @Test
    public void testExecuteTaskFailure() throws ServerException, TaskException {
        // Setup task to fail during execution
        when(task.isCompleted()).thenReturn(false);
        doThrow(new TaskException("Simulated failure", new Throwable())).when(task).execute();

        // Add task and execute
        server.addTask(task);
        List<Task> completedTasks = server.executeTasks();

        // Verify no completed tasks
        assertEquals(0, completedTasks.size());
        assertFalse(completedTasks.contains(task));

        // Verify monitoring and alerting occurred after task failure
        verify(performanceMonitor, times(2)).monitorAndAlert();  // Once on addTask and once on execute
    }

    @Test
    public void testTaskRetries() throws ServerException, TaskException, InterruptedException {
        // Set retry policy to allow 3 retries
        when(retryPolicy.getMaxRetries()).thenReturn(3);
        when(retryPolicy.getDelay(anyInt())).thenReturn(0L);

        // Setup task to fail twice, then succeed
        when(task.isCompleted()).thenReturn(false).thenReturn(false).thenReturn(true);

        // Add task and execute
        server.addTask(task);
        List<Task> completedTasks = server.executeTasks();

        // Verify the task completed after retries
        assertEquals(1, completedTasks.size());
        verify(task, times(3)).execute();  // Should have retried 3 times

        // Verify that performance monitoring occurred after retries
        verify(performanceMonitor, times(2)).monitorAndAlert();  // Once on addTask and once on execute
    }

    @Test
    public void testGetCompletedTasks() throws ServerException, TaskException {
        // Setup task to execute successfully
        when(task.isCompleted()).thenReturn(true);

        // Add task and execute
        server.addTask(task);
        server.executeTasks();

        // Verify completed tasks are returned correctly
        List<Task> completedTasks = server.getCompletedTasks();
        assertEquals(1, completedTasks.size());
        assertTrue(completedTasks.contains(task));
    }

    @Test
    public void testGetFailedTasks() throws ServerException, TaskException {
        // Setup task to fail execution
        when(task.isCompleted()).thenReturn(false);
        doThrow(new TaskException("Simulated failure", new Throwable())).when(task).execute();

        // Add task and execute
        server.addTask(task);
        server.executeTasks();

        // Verify failed tasks are returned correctly
        List<Task> failedTasks = server.getFailedTasks();
        assertEquals(1, failedTasks.size());
        assertTrue(failedTasks.contains(task));
    }

    @Test
    public void testGetTotalLoad() throws ServerException {
        // Add task to server
        server.addTask(task);

        // Verify the total load calculation
        assertEquals(BigInteger.valueOf(5000), server.getTotalLoad());
    }
}

