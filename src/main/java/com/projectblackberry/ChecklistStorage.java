package com.projectblackberry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectblackberry.model.WeeklyChecklist;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChecklistStorage {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DATA_DIR = "data/checklists";

    static {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Warning: Could not create data directory: " + e.getMessage());
        }
    }

    public static void saveChecklist(WeeklyChecklist checklist) throws IOException {
        String filename = checklist.getWeekId() + ".json";
        Path filePath = Paths.get(DATA_DIR, filename);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), checklist);
        System.out.println("Saved checklist to: " + filePath);
    }

    public static WeeklyChecklist loadChecklist(String weekId) {
        try {
            String filename = weekId + ".json";
            Path filePath = Paths.get(DATA_DIR, filename);
            File file = filePath.toFile();
            
            if (file.exists()) {
                WeeklyChecklist checklist = objectMapper.readValue(file, WeeklyChecklist.class);
                System.out.println("Loaded checklist from: " + filePath);
                return checklist;
            }
            // file doesn't exist, return null
        } catch (Exception e) {
            System.err.println("Error loading checklist " + weekId + ": " + e.getMessage());
        }
        return null;
    }

    public static boolean checklistExists(String weekId) {
        String filename = weekId + ".json";
        Path filePath = Paths.get(DATA_DIR, filename);
        return filePath.toFile().exists();
    }
}

