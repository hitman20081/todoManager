package github.hitman20081.todomanager;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Task {

    public enum Priority {
        HIGH, MEDIUM, LOW; // Enum for task priority

        @JsonCreator
        public static Priority fromString(String key) {
            try {
                return key == null ? null : Priority.valueOf(key.toUpperCase());
            } catch (IllegalArgumentException e) {
                return MEDIUM; // Default to MEDIUM if the string doesn't match any enum values
            }
        }
    }

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("priority")
    private Priority priority; // Change to Priority enum

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

    // New constructor to handle 'completed' field
     public Task(String name, String description, Priority priority, LocalDate dueDate, boolean completed) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Task name cannot be null or empty");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Task priority cannot be null");
        }
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.completed = completed;
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

    public Priority getPriority() {
        return priority; // Return Priority enum
    }

    public void setPriority(Priority priority) {
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
        if (comment != null && !comment.trim().isEmpty()) {
            comments.add(comment); // Add a new comment to the list
        } else {
            System.out.println("Comment cannot be empty.");
        }
    }

    public void removeComment(String comment) {
        comments.remove(comment);
    }

    public void clearComments() {
        comments.clear();
    }

    @Override
    public String toString() {
        return name + " (" + priority + ")" +
                (completed ? " [Completed]" : "") +
                (dueDate != null ? " [Due: " + dueDate + "]" : "") +
                (!comments.isEmpty() ? " [Comments: " + String.join("; ", comments) + "]" : ""); // Display comments
    }
}
