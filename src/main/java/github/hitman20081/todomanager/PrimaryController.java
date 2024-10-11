package github.hitman20081.todomanager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.TextInputDialog;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.util.Comparator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

public class PrimaryController {

    @FXML
    private TextField taskNameField;

    @FXML
    private TextField taskDescriptionField;

    @FXML
    private TextField commentField;

    @FXML
    private ComboBox<String> sortComboBox;
    @FXML
    private ChoiceBox<String> priorityChoiceBox; // Added ChoiceBox for priority

    @FXML
    private ListView<Task> taskListView;
    @FXML
    private ListView<String> templateListView; // ListView to display template names
    @FXML
    private ListView<String> commentsListView;

    private ObservableList<String> templateNames;

    private ObservableList<Task> tasks;
    @FXML
    private TextField editCommentInput;

    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private TextArea notificationArea;


    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Ensures that dates are serialized in a human-readable format
        return mapper;
    }

    @FXML
    private void initialize() {
        // Initialize the tasks ObservableList and ListView
        tasks = FXCollections.observableArrayList();
        taskListView.setItems(tasks);
        taskListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateCommentsListView(newSelection);
        });
        // Add listener to update comments when a new task is selected
        taskListView.getSelectionModel().selectedItemProperty().addListener((obs, oldTask, newTask) -> {
            updateCommentsListView(newTask);
        });
        // Initialize sort options if needed
        sortComboBox.setItems(FXCollections.observableArrayList("Sort by Due Date", "Sort by Priority", "Sort by Name"));

        // Initialize ChoiceBox for priorities
        priorityChoiceBox.setItems(FXCollections.observableArrayList(
                "Low", "Medium Low", "Medium", "Medium High", "High"
        ));
        priorityChoiceBox.setValue("Medium"); // Set a default value if needed

        // Initialize ComboBox for sorting options
        sortComboBox.setItems(FXCollections.observableArrayList(
                "Sort by Due Date", "Sort by Priority", "Sort by Name"
        ));
        sortComboBox.setValue("Sort by Name"); // Set a default sorting option

        // Load template names from the 'templates' directory on startup
        templateNames = FXCollections.observableArrayList();
        loadTemplateNames();
        templateListView.setItems(templateNames);
    }

    @FXML
    private void addTask() {
        String name = taskNameField.getText();
        String description = taskDescriptionField.getText();
        LocalDate dueDate = dueDatePicker.getValue();
        String selectedPriority = priorityChoiceBox.getValue();
        Task.Priority priority = Task.Priority.fromString(selectedPriority);

        if (name.isEmpty() || description.isEmpty() || dueDate == null || priority == null) {
            showNotification("All fields are required to add a task.");
            return;
        }

        Task newTask = new Task(name, description, priority, dueDate, false);
        tasks.add(newTask);
        showNotification("Task added successfully.");

        // Clear fields after successful addition
        taskNameField.clear();
        taskDescriptionField.clear();
        dueDatePicker.setValue(null);
        priorityChoiceBox.setValue(null);
    }


    @FXML
    private void editTask() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            selectedTask.setName(taskNameField.getText());
            selectedTask.setDescription(taskDescriptionField.getText());
            showNotification("Task edited successfully.");
            updateTaskListView();
        } else {
            showNotification("No task selected for editing.");
        }
    }

    @FXML
    private void deleteTask() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            tasks.remove(selectedTask);
            showNotification("Task deleted successfully.");
            updateTaskListView();
        } else {
            showNotification("No task selected for deletion.");
        }
    }

    @FXML
    private void markTaskAsCompleted() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            selectedTask.setCompleted(true);
            showNotification("Task marked as completed.");
            updateTaskListView();
        } else {
            showNotification("No task selected to mark as completed.");
        }
    }

    @FXML
    private void handleAddComment() {
        String comment = commentField.getText();
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();

        if (selectedTask != null && comment != null && !comment.trim().isEmpty()) {
            selectedTask.addComment(comment);
            updateCommentsListView(selectedTask); // Refresh comments after adding
            showNotification("Comment added to task.");
            commentField.clear(); // Clear the comment field after adding
        } else {
            showNotification("Please select a task and enter a comment.");
        }
    }

    @FXML
    private void handleEditComment() {
        String newComment = editCommentInput.getText();
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        String selectedComment = commentsListView.getSelectionModel().getSelectedItem();

        if (selectedTask != null && selectedComment != null && newComment != null && !newComment.trim().isEmpty()) {
            List<String> comments = selectedTask.getComments();
            int commentIndex = comments.indexOf(selectedComment);

            if (commentIndex != -1) {
                comments.set(commentIndex, newComment);
                updateCommentsListView(selectedTask);
                showNotification("Comment edited successfully.");
                editCommentInput.clear();
            } else {
                showNotification("Selected comment could not be found.");
            }
        } else {
            showNotification("Please select a comment to edit and enter a new value.");
        }
    }
    private void updateCommentsListView(Task task) {
        if (task != null && commentsListView != null) {
            ObservableList<String> comments = FXCollections.observableArrayList(task.getComments());
            commentsListView.setItems(comments);
        }
    }


    @FXML
    private void saveTasks() {
        try {
            // Define the path to save tasks.json
            Path taskFilePath = Paths.get("src/main/resources/tasks.json");
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.writerWithDefaultPrettyPrinter().writeValue(taskFilePath.toFile(), tasks);
            showNotification("Tasks saved successfully to tasks.json.");
        } catch (IOException e) {
            showNotification("Failed to save tasks: " + e.getMessage());
        }
    }
    @FXML
    private void saveTemplate() {
        // Prompt user to enter a template name
        TextInputDialog dialog = new TextInputDialog("TemplateName");
        dialog.setTitle("Template Name");
        dialog.setHeaderText("Save current tasks as a template");
        dialog.setContentText("Please enter the template name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(templateName -> {
            if (!templateName.trim().isEmpty()) {
                saveTemplateToResources(templateName.trim());
            } else {
                showNotification("Template name cannot be empty.");
            }
        });
    }

    private void saveTemplateToResources(String templateName) {
        try {
            // Define the path for the templates folder within resources
            Path templateFolderPath = Paths.get("src/main/resources/templates/");
            if (!Files.exists(templateFolderPath)) {
                Files.createDirectories(templateFolderPath);
            }

            // Create the file path using the template name
            Path templateFilePath = templateFolderPath.resolve(templateName + ".json");

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.writerWithDefaultPrettyPrinter().writeValue(templateFilePath.toFile(), tasks);
            showNotification("Template '" + templateName + "' saved to templates folder.");
        } catch (IOException e) {
            showNotification("Failed to save template: " + e.getMessage());
        }
    }
    private void loadTemplate(String templateName) {
        try {
            File file = new File("src/main/resources/templates/" + templateName + ".json");
            ObjectMapper mapper = createObjectMapper(); // Use the configured ObjectMapper
            List<Task> loadedTasks = mapper.readValue(file, new TypeReference<List<Task>>() {});
            tasks.setAll(loadedTasks);
            showNotification("Template loaded successfully.");
        } catch (IOException e) {
            showNotification("Failed to load template: " + e.getMessage());
        }
    }


    private void saveTemplate(String templateName) {
        try {
            File file = new File("src/main/resources/templates/" + templateName + ".json");
            ObjectMapper mapper = createObjectMapper(); // Use the configured ObjectMapper
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, tasks);
            showNotification("Template saved successfully.");
        } catch (IOException e) {
            showNotification("Failed to save template: " + e.getMessage());
        }
    }




    @FXML
    private void convertToTemplate() {
        // Placeholder for loading tasks logic
        showNotification("Tasks loaded successfully.");
    }

    @FXML
    private void backupTasks() {
        try {
            // Define the path for the backup file
            File backupFile = new File("tasks_backup.json");

            // Create ObjectMapper and configure it for pretty printing and LocalDate handling
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, tasks);

            showNotification("Tasks backed up successfully to tasks_backup.json.");
        } catch (IOException e) {
            showNotification("Failed to back up tasks: " + e.getMessage());
        }
    }

    @FXML
    private void restoreBackup() {
        try {
            // Define the path for the backup file
            File backupFile = new File("tasks_backup.json");

            if (backupFile.exists()) {
                // Create ObjectMapper and configure it for LocalDate handling
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());

                // Read the JSON data and deserialize it into a list of tasks
                List<Task> restoredTasks = mapper.readValue(
                        backupFile,
                        new TypeReference<List<Task>>() {}
                );

                // Update the tasks list and refresh the ListView
                tasks.setAll(restoredTasks);
                taskListView.refresh();
                showNotification("Tasks restored successfully from tasks_backup.json.");
            } else {
                showNotification("Backup file not found. Restore operation canceled.");
            }
        } catch (IOException e) {
            showNotification("Failed to restore tasks: " + e.getMessage());
        }
    }

    // Method to import tasks
    @FXML
    private void importTasks() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Tasks");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                List<Task> importedTasks = mapper.readValue(file, new TypeReference<List<Task>>() {});
                tasks.setAll(importedTasks);
                taskListView.setItems(tasks);
                showNotification("Tasks imported successfully.");
            } else {
                showNotification("Import canceled.");
            }
        } catch (IOException e) {
            showNotification("Failed to import tasks: " + e.getMessage());
        }
    }

    // Method to export tasks
    @FXML
    private void exportTasks() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Tasks");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, tasks);
                showNotification("Tasks exported successfully.");
            } else {
                showNotification("Export canceled.");
            }
        } catch (IOException e) {
            showNotification("Failed to export tasks: " + e.getMessage());
        }
    }

    @FXML
    private void handleSortSelection() {
        String selectedOption = sortComboBox.getValue();

        if (selectedOption != null) {
            switch (selectedOption) {
                case "Sort by Due Date":
                    tasks.sort(Comparator.comparing(Task::getDueDate));
                    showNotification("Tasks sorted by due date.");
                    break;
                case "Sort by Priority":
                    tasks.sort(Comparator.comparing(Task::getPriority));
                    showNotification("Tasks sorted by priority.");
                    break;
                case "Sort by Name":
                    tasks.sort(Comparator.comparing(Task::getName));
                    showNotification("Tasks sorted by name.");
                    break;
                default:
                    showNotification("Please select a valid sort option.");
            }
            updateTaskListView();
        }
    }

    // Template section //
    private void loadTemplateNames() {
        try {
            File templateDir = new File("src/main/resources/templates");
            if (!templateDir.exists() || !templateDir.isDirectory()) {
                showNotification("Template directory does not exist.");
                return;
            }

            File[] templateFiles = templateDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (templateFiles != null) {
                templateNames.clear();
                for (File file : templateFiles) {
                    templateNames.add(file.getName().replace(".json", ""));
                }
            }
        } catch (Exception e) {
            showNotification("Failed to load template names: " + e.getMessage());
        }
    }


    @FXML
    private void loadTemplate() {
        String selectedTemplate = templateListView.getSelectionModel().getSelectedItem();
        if (selectedTemplate == null) {
            showNotification("Please select a template to load.");
            return;
        }

        File templateFile = new File("src/main/resources/templates/" + selectedTemplate + ".json");
        if (templateFile.exists()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                List<Task> loadedTasks = objectMapper.readValue(templateFile, objectMapper.getTypeFactory().constructCollectionType(List.class, Task.class));
                tasks.setAll(loadedTasks);
                showNotification("Template '" + selectedTemplate + "' loaded successfully.");
            } catch (IOException e) {
                showNotification("Failed to load template: " + e.getMessage());
            }
        } else {
            showNotification("Template file not found.");
        }
    }

    // updateTaskListView
    private void updateTaskListView() {
        // Refreshes the ListView
        taskListView.refresh();
    }

    // showNotification
    private void showNotification(String message) {
        // Displays a message in the notification area
        notificationArea.setText(message);
    }


}
