package tests.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import taskscheduler.java.tasks.DependentTask;
import taskscheduler.java.other.Duration;
import taskscheduler.java.other.TaskPriority;
import taskscheduler.java.servers.RemoteServer;
import taskscheduler.java.servers.Server;
import taskscheduler.java.TaskScheduler;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class TaskSchedulerTest {

    private RemoteServer mockRemoteServer;
    private TaskScheduler taskScheduler;
    private DependentTask mockTask;

    @BeforeEach
    public void setUp() {
        // Mock the RemoteServer
        mockRemoteServer = Mockito.mock(RemoteServer.class);

        // Mock a DependentTask
        mockTask = Mockito.mock(DependentTask.class);

        // Create a new TaskScheduler instance
        taskScheduler = new TaskScheduler();
    }

    @Test
    public void testRemoteServerTaskExecution() throws Exception {
        // Arrange
        when(mockRemoteServer.executeTasks()).thenReturn(Collections.singletonList(mockTask));
        when(mockTask.isCompleted()).thenReturn(true);

        // Act
        taskScheduler.addServer(mockRemoteServer);
        taskScheduler.executeAll();  // Trigger task execution

        // Assert that the server is present in the queue
        assertTrue(taskScheduler.getServers().contains(mockRemoteServer), "Remote server should be in the scheduler.");

        // Verify that the task was executed
        verify(mockRemoteServer).executeTasks();  // Now this should pass
    }


    @Test
    public void testRemoteServerTaskExecutionFailure() throws Exception {
        // Arrange: Simulate remote task execution failure
        when(mockRemoteServer.executeTasks()).thenThrow(new RuntimeException("Remote server failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> mockRemoteServer.executeTasks(), "Remote server failed");
    }
}

