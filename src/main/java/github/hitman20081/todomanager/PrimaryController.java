package github.hitman20081.todomanager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.fxml.FXML;
import javafx.scene.control.Label; // Import for Label
import javafx.scene.layout.HBox; // Import for HBox
import javafx.animation.PauseTransition; // Import for PauseTransition
import javafx.util.Duration; // Import for Duration
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import javafx.event.ActionEvent;

public class PrimaryController {
    @FXML
    private TextField nameInput;
    @FXML
    private TextField descriptionInput;
    @FXML
    private TextField priorityInput;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private TextField filterInput;
    @FXML
    private ListView<Task> taskListView;
    @FXML
    private ListView<TaskHistory> taskHistoryListView;
    @FXML
    private TextField commentInput;
    @FXML
    private HBox notificationArea; // Reference to the notification area
    @FXML
    private Label notificationLabel; // Reference to the notification label

    private final List<Task> tasks = new ArrayList<>();
    private final List<TaskHistory> taskHistoryList = new ArrayList<>();
    private Task selectedTask;
    private boolean isEditing = false;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private boolean ascending = true; // Track sorting direction
    private String currentSortType = "dueDate"; // Track the current sort type

    @FXML
    public void initialize() {
        loadTasks();
        loadTaskHistory();
        taskListView.getItems().clear();

        // Show the welcome notification
        notificationLabel.setText("Welcome to the Task Manager!");
        showNotification("Welcome to To-Do Manager!");
    }

    @FXML
    public void addTask() {
        String name = nameInput.getText();
        String description = descriptionInput.getText();
        String priorityString = priorityInput.getText(); // Changed to priorityString
        LocalDate dueDate = dueDatePicker.getValue();

        // Validate input
        if (dueDate == null) {
            System.out.println("Please select a valid due date.");
            return;
        }

        Task.Priority priority; // Declare priority
        try {
            priority = Task.Priority.valueOf(priorityString.toUpperCase()); // Convert input to Priority enum
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid priority value. Please use a valid priority.");
            return;
        }

        // Handle editing or creating a new task
        if (isEditing) {
            updateSelectedTask(name, description, priority, dueDate);
        } else {
            createNewTask(name, description, priority, dueDate);
        }

        clearInputs();
        taskListView.getSelectionModel().clearSelection();
        saveTasks(); // Save tasks after adding or editing
    }

    @FXML
    public void deleteTask() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            addToTaskHistory(selectedTask, null);
            tasks.remove(selectedTask);
            updateTaskListView(); // Update the ListView
            saveTasks();
            saveTaskHistory();
        }
    }

    @FXML
    public void editTask() {
        selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            populateInputFields(selectedTask);
            isEditing = true;
        }
    }

    @FXML
    public void addCommentToTask() {
        String comment = commentInput.getText();
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null && comment != null && !comment.trim().isEmpty()) {
            selectedTask.getComments().add(comment); // Assuming getComments() returns a List<String>
            commentInput.clear(); // Clear the comment input after adding
            // Optionally refresh the UI or display a message
        } else {
            System.out.println("Please select a task and enter a comment.");
        }
    }

    @FXML
    public void markTaskAsCompleted() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            addToTaskHistory(selectedTask, LocalDate.now());
            selectedTask.setCompleted(true);
            taskListView.refresh();
            saveTasks();
            saveTaskHistory();
        }
    }

    @FXML
    public void filterTasks() {
        String filter = filterInput.getText().toLowerCase();
        List<Task> filteredTasks = tasks.stream()
                .filter(task -> task.getName().toLowerCase().contains(filter) ||
                        task.getDescription().toLowerCase().contains(filter) ||
                        task.getPriority().toString().toLowerCase().contains(filter)) // Change toString() for filtering
                .collect(Collectors.toList());

        taskListView.getItems().clear();
        taskListView.getItems().addAll(filteredTasks);
    }

    @FXML
    public void toggleSortOrder() {
        ascending = !ascending; // Toggle sort order
        if (currentSortType.equals("dueDate")) {
            sortTasksByDueDate();
        } else {
            sortByPriority();
        }
    }

    @FXML
    public void switchSortType() {
        currentSortType = currentSortType.equals("dueDate") ? "priority" : "dueDate";
        toggleSortOrder(); // Apply sorting with the new sort type
    }

    public void sortTasksByDueDate() {
        tasks.sort((task1, task2) -> {
            if (task1.getDueDate() == null && task2.getDueDate() == null) {
                return 0; // Both are null, considered equal
            }
            if (task1.getDueDate() == null) {
                return ascending ? 1 : -1; // Nulls are sorted last
            }
            if (task2.getDueDate() == null) {
                return ascending ? -1 : 1; // Nulls are sorted last
            }
            return ascending ? task1.getDueDate().compareTo(task2.getDueDate())
                    : task2.getDueDate().compareTo(task1.getDueDate());
        });
        updateTaskListView(); // Refresh the ListView to show sorted tasks
    }

    public void sortByPriority() {
        tasks.sort(Comparator.comparing(Task::getPriority, Comparator.nullsLast(Comparator.naturalOrder())));
        if (!ascending) {
            // If not ascending, reverse the list
            List<Task> reversedTasks = new ArrayList<>(tasks);
            tasks.clear();
            for (int i = reversedTasks.size() - 1; i >= 0; i--) {
                tasks.add(reversedTasks.get(i));
            }
        }
        updateTaskListView(); // Update the ListView after sorting
    }

    @FXML
    public void backupTasks() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Backup Directory");
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory != null) {
            File backupFile = new File(selectedDirectory, "tasks_backup.json");
            writeTasksToFile(backupFile);
        }
    }

    @FXML
    public void restoreTasks() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Backup File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            readTasksFromFile(selectedFile);
        }
    }

    @FXML
    public void exportTasks() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Tasks");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                writer.write("Name,Description,Priority,Due Date,Completed\n");
                for (Task task : tasks) {
                    writer.write(String.format("%s,%s,%s,%s,%s\n",
                            task.getName(),
                            task.getDescription(),
                            task.getPriority(),
                            task.getDueDate() != null ? task.getDueDate().toString() : "",
                            task.isCompleted() ? "Yes" : "No"));
                }
                System.out.println("Tasks exported successfully.");
            } catch (IOException e) {
                System.err.println("Failed to export tasks: " + e.getMessage());
            }
        }
    }

    @FXML
    public void importTasks() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Tasks");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                String line;
                reader.readLine(); // Skip header
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    Task task = new Task();
                    task.setName(data[0]);
                    task.setDescription(data[1]);
                    task.setPriority(Task.Priority.valueOf(data[2].toUpperCase())); // Convert String to Priority
                    task.setDueDate(LocalDate.parse(data[3]));
                    task.setCompleted(data[4].equalsIgnoreCase("Yes"));

                    tasks.add(task);
                    taskListView.getItems().add(task);
                }
                System.out.println("Tasks imported successfully.");
            } catch (IOException e) {
                System.err.println("Failed to import tasks: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid priority value in imported data: " + e.getMessage());
            }
        }
    }

    private void createNewTask(String name, String description, Task.Priority priority, LocalDate dueDate) {
        Task newTask = new Task();
        newTask.setName(name); // Set the name
        newTask.setDescription(description); // Set the description
        newTask.setPriority(priority); // Set the priority
        newTask.setDueDate(dueDate); // Set the due date
        tasks.add(newTask);
        taskListView.getItems().add(newTask);
        showNotification("Task added successfully.");
    }

    private void updateSelectedTask(String name, String description, Task.Priority priority, LocalDate dueDate) {
        selectedTask.setName(name);
        selectedTask.setDescription(description);
        selectedTask.setPriority(priority);
        selectedTask.setDueDate(dueDate);
        taskListView.refresh();
        showNotification("Task updated successfully.");
        isEditing = false;
    }

    private void populateInputFields(Task task) {
        nameInput.setText(task.getName());
        descriptionInput.setText(task.getDescription());
        priorityInput.setText(task.getPriority().toString());
        dueDatePicker.setValue(task.getDueDate());
    }

    private void clearInputs() {
        nameInput.clear();
        descriptionInput.clear();
        priorityInput.clear();
        dueDatePicker.setValue(null);
    }

    private void showNotification(String message) {
        notificationLabel.setText(message);
        notificationArea.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(event -> notificationArea.setVisible(false));
        pause.play();
    }

    private void writeTasksToFile(File file) {
        try {
            objectMapper.writeValue(file, tasks);
            System.out.println("Tasks backed up successfully.");
        } catch (IOException e) {
            System.err.println("Failed to backup tasks: " + e.getMessage());
        }
    }

    private void readTasksFromFile(File file) {
        try {
            Task[] loadedTasks = objectMapper.readValue(file, Task[].class);
            tasks.clear();
            for (Task task : loadedTasks) {
                tasks.add(task);
                taskListView.getItems().add(task);
            }
            System.out.println("Tasks restored successfully.");
        } catch (IOException e) {
            System.err.println("Failed to restore tasks: " + e.getMessage());
        }
    }
    @FXML
    private void saveTasks() {
        // Implement the logic to save tasks to a file
        try {
            File file = new File("tasks.json"); // You may want to choose a specific path or use a dialog
            objectMapper.writeValue(file, tasks);
            System.out.println("Tasks saved successfully.");
        } catch (IOException e) {
            System.err.println("Failed to save tasks: " + e.getMessage());
        }
    }

    @FXML
    private void loadTasks() {
        File file = new File("tasks.json");
        if (file.exists()) {
            readTasksFromFile(file);
        } else {
            System.out.println("No saved tasks found.");
        }
    }

    private void loadTaskHistory() {
        // Implement the logic to load task history from a file or database if necessary.
    }

    private void addToTaskHistory(Task task, LocalDate completedDate) {
        TaskHistory taskHistory = new TaskHistory();
        taskHistoryList.add(taskHistory);
        taskHistoryListView.getItems().add(taskHistory);
    }

    private void saveTaskHistory() {
        // Implement the logic to save task history to a file or database if necessary.
    }

    private void updateTaskListView() {
        taskListView.getItems().clear();
        taskListView.getItems().addAll(tasks);
    }
}
