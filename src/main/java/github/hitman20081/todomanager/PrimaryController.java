package github.hitman20081.todomanager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PrimaryController {
    @FXML
    private TextField nameInput;
    @FXML
    private TextField descriptionInput;
    @FXML
    private TextField priorityInput;
    @FXML
    private TextField dueDateInput; // Format: YYYY-MM-DD
    @FXML
    private TextField filterInput; // Filter input field
    @FXML
    private ListView<Task> taskListView;
    @FXML
    private ListView<TaskHistory> taskHistoryListView; // Task history ListView
    @FXML
    private TextField commentInput; // Comment input field

    private final List<Task> tasks = new ArrayList<>();
    private final List<TaskHistory> taskHistoryList = new ArrayList<>();
    private Task selectedTask; // Currently selected task
    private boolean isEditing = false; // Editing mode flag

    // Initialize ObjectMapper with JavaTimeModule
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @FXML
    public void initialize() {
        loadTasks(); // Load tasks when the controller is initialized
        loadTaskHistory(); // Load task history when the controller is initialized
        taskListView.getItems().clear(); // Clear the ListView first
    }

    @FXML
    public void addTask() {
        String name = nameInput.getText();
        String description = descriptionInput.getText();
        String priority = priorityInput.getText();
        LocalDate dueDate = parseDueDate();

        if (dueDate == null) {
            return; // Exit if due date is invalid
        }

        if (isEditing) {
            updateSelectedTask(name, description, priority, dueDate);
        } else {
            createNewTask(name, description, priority, dueDate);
        }

        clearInputs();
        taskListView.getSelectionModel().clearSelection(); // Deselect current task
        saveTasks(); // Save tasks after adding or editing
    }

    @FXML
    public void deleteTask() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            addToTaskHistory(selectedTask, null); // Add to task history before deletion
            tasks.remove(selectedTask);
            taskListView.getItems().remove(selectedTask);
            saveTasks(); // Save current tasks
            saveTaskHistory(); // Save task history
        }
    }

    @FXML
    public void editTask() {
        selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            populateInputFields(selectedTask);
            isEditing = true; // Set editing mode
        }
    }

    @FXML
    public void markTaskAsCompleted() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            addToTaskHistory(selectedTask, LocalDate.now()); // Record the completion date
            selectedTask.setCompleted(true);
            taskListView.refresh(); // Refresh the ListView to show updated task
            saveTasks(); // Save current tasks
            saveTaskHistory(); // Save task history
        }
    }

    @FXML
    public void filterTasks() {
        String filter = filterInput.getText().toLowerCase();
        List<Task> filteredTasks = tasks.stream()
                .filter(task -> task.getName().toLowerCase().contains(filter) ||
                        task.getDescription().toLowerCase().contains(filter) ||
                        task.getPriority().toLowerCase().contains(filter))
                .collect(Collectors.toList());

        taskListView.getItems().clear();
        taskListView.getItems().addAll(filteredTasks);
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
                // Write header
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
                e.printStackTrace();
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
                tasks.clear(); // Clear current tasks
                taskListView.getItems().clear(); // Clear the ListView

                // Skip the header line
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        String name = parts[0];
                        String description = parts[1];
                        String priority = parts[2];
                        LocalDate dueDate = parts[3].isEmpty() ? null : LocalDate.parse(parts[3]);
                        boolean completed = "Yes".equalsIgnoreCase(parts[4]);

                        Task task = new Task(name, description, priority, dueDate);
                        task.setCompleted(completed);
                        tasks.add(task);
                        taskListView.getItems().add(task);
                    }
                }
                saveTasks(); // Optionally save imported tasks
                System.out.println("Tasks imported successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DateTimeParseException e) {
                System.out.println("Error parsing due date.");
            }
        }
    }

    // Updated loadTasks method
    @FXML
    public void loadTasks() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("tasks.json")) {
            if (inputStream != null) {
                List<Task> loadedTasks = objectMapper.readValue(inputStream, new TypeReference<List<Task>>() {});
                tasks.clear();
                tasks.addAll(loadedTasks);
                taskListView.getItems().clear();
                taskListView.getItems().addAll(tasks);
                System.out.println("Tasks loaded from resources/tasks.json");
            } else {
                System.out.println("No tasks.json file found in resources.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Updated saveTasks method
    @FXML
    public void saveTasks() {
        writeTasksToFile(new File("tasks.json")); // Write to the current working directory
    }

    @FXML
    public void saveTaskHistory() {
        writeTaskHistoryToFile(new File("task_history.json"));
    }

    @FXML
    public void loadTaskHistory() {
        readTaskHistoryFromFile(new File("task_history.json"));
    }

    @FXML
    public void sortByDueDate() {
        tasks.sort(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));
        refreshTaskListView();
    }

    @FXML
    public void sortByPriority() {
        tasks.sort(Comparator.comparing(Task::getPriority));
        refreshTaskListView();
    }

    @FXML
    public void addComment() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            String comment = commentInput.getText();
            if (!comment.isEmpty()) {
                selectedTask.getComments().add(comment);
                commentInput.clear(); // Clear input field after adding
                taskListView.refresh(); // Refresh ListView to show updated comments
            }
        }
    }

    // Utility methods
    private LocalDate parseDueDate() {
        try {
            return LocalDate.parse(dueDateInput.getText());
        } catch (DateTimeParseException e) {
            System.out.println("Invalid due date format. Please use YYYY-MM-DD.");
            return null;
        }
    }

    private void createNewTask(String name, String description, String priority, LocalDate dueDate) {
        Task newTask = new Task(name, description, priority, dueDate);
        tasks.add(newTask);
        taskListView.getItems().add(newTask);
        System.out.println("Task added: " + newTask);
    }

    private void updateSelectedTask(String name, String description, String priority, LocalDate dueDate) {
        selectedTask.setName(name);
        selectedTask.setDescription(description);
        selectedTask.setPriority(priority);
        selectedTask.setDueDate(dueDate);
        taskListView.refresh(); // Refresh ListView to show updated task
        System.out.println("Task updated: " + selectedTask);
        isEditing = false; // Reset editing mode
    }

    private void clearInputs() {
        nameInput.clear();
        descriptionInput.clear();
        priorityInput.clear();
        dueDateInput.clear();
        commentInput.clear(); // Clear comment input as well
    }

    private void populateInputFields(Task task) {
        nameInput.setText(task.getName());
        descriptionInput.setText(task.getDescription());
        priorityInput.setText(task.getPriority());
        dueDateInput.setText(task.getDueDate() != null ? task.getDueDate().toString() : "");
    }

    private void addToTaskHistory(Task task, LocalDate completionDate) {
        // Logic for adding task to history (if needed)
    }

    private void refreshTaskListView() {
        taskListView.getItems().clear();
        taskListView.getItems().addAll(tasks);
    }

    private void writeTasksToFile(File file) {
        try {
            objectMapper.writeValue(file, tasks);
            System.out.println("Tasks saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeTaskHistoryToFile(File file) {
        // Implementation for writing task history to a file
    }

    private void readTaskHistoryFromFile(File file) {
        // Implementation for reading task history from a file
    }

    private void readTasksFromFile(File file) {
        // Implementation for reading tasks from a specified file
    }
}
