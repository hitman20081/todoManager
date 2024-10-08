package github.hitman20081.todomanager;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Task {
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("completed")
    private boolean completed;

    @JsonProperty("dueDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @JsonProperty("comments")
    private List<String> comments = new ArrayList<>(); // Initialize comments list

    // Default constructor for Jackson
    public Task() {
    }

    public Task(String name, String description, String priority, LocalDate dueDate) {
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.completed = false;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    // Getters and Setters for comments
    public List<String> getComments() {
        return comments;
    }

    public void addComment(String comment) {
        comments.add(comment); // Add a new comment to the list
    }

    @Override
    public String toString() {
        return name + " (" + priority + ")" + (completed ? " [Completed]" : "") +
                (dueDate != null ? " [Due: " + dueDate + "]" : "") +
                (!comments.isEmpty() ? " [Comments: " + String.join(", ", comments) + "]" : ""); // Display comments
    }
}
