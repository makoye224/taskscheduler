package taskscheduler.java.servers;

import taskscheduler.java.exceptions.TaskException;
import taskscheduler.java.tasks.Task;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoteServerTaskExecuter {

    private static final Logger logger = Logger.getLogger(RemoteServerTaskExecuter.class.getName());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            logger.log(Level.INFO, "Task Execution Server is listening on port 12345");

            while (true) {
                Socket socket = serverSocket.accept();  // Wait for client connections
                new TaskHandler(socket).start();  // Handle each connection in a new thread
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server exception: " + e.getMessage());
        }
    }
}

// Handles incoming task execution requests
class TaskHandler extends Thread {
    private final Socket socket;
    private static final Logger logger = Logger.getLogger(TaskHandler.class.getName());

    public TaskHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            // Read the task from the client (RemoteServer)
            Task task = (Task) in.readObject();
            logger.log(Level.INFO, "Received task: {0}", task.getId());

            // Execute the task
            try {
                task.execute();  // Execute the task
                out.writeObject("ACK");  // Send an acknowledgment back to the client
                out.flush();
                logger.log(Level.INFO, "Task {0} executed successfully.", task.getId());
            } catch (TaskException e) {
                logger.log(Level.SEVERE, "Task {0} execution failed: {1}", new Object[]{task.getId(), e.getMessage()});
                out.writeObject("FAILED");
                out.flush();
            }

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Exception while handling task: {0}", e.getMessage());
            try {
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("FAILED");
                out.flush();
            } catch (IOException ioException) {
                logger.log(Level.SEVERE, "Failed to send failure response: {0}", ioException.getMessage());
            }
        } finally {
            try {
                socket.close();  // Ensure socket is closed after task execution
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to close socket: {0}", e.getMessage());
            }
        }
    }
}
