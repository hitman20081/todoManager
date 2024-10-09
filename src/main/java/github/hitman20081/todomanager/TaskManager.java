package github.hitman20081.todomanager;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TaskManager {
    private static final String FILE_PATH = "C://Users/matth/Desktop/Code/Backuptasks.json"; // Update with your actual path

    // Method to read tasks from the JSON file
    public List<Task> readTasksFromFile() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // Read the JSON file into a List of Task objects
        return objectMapper.readValue(new File(FILE_PATH),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Task.class));
    }
}
