package github.hitman20081.todomanager;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class TaskHistory {
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("priority")
    private Task.Priority priority;

    @JsonProperty("dueDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @JsonProperty("completedDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate completedDate;

    // Default constructor for Jackson
    public TaskHistory() {}

    // Constructor that accepts individual parameters
    public TaskHistory(String name, String description, Task.Priority priority, LocalDate dueDate, LocalDate completedDate) {
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.completedDate = completedDate;
    }

    // Constructor that accepts a Task object and a completedDate
    public TaskHistory(Task task, LocalDate completedDate) {
        this.name = task.getName();
        this.description = task.getDescription();
        this.priority = task.getPriority();
        this.dueDate = task.getDueDate();
        this.completedDate = completedDate;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Task.Priority getPriority() { return priority; }
    public void setPriority(Task.Priority priority) { this.priority = priority; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDate completedDate) { this.completedDate = completedDate; }

    @Override
    public String toString() {
        return name + " (" + (priority != null ? priority.toString() : "None") + ")" +
                (completedDate != null ? " [Completed on: " + completedDate.toString() + "]" : " [Deleted]") +
                (dueDate != null ? " [Due: " + dueDate.toString() + "]" : "");
    }
}
