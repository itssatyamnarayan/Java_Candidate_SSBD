package com.example.demo.service;

import com.example.demo.model.Earthquake;
import com.example.demo.repository.EarthquakeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class EarthquakeService {

    @Autowired
    private EarthquakeRepository repository;
    // Formatter for datetime with microseconds and timezone
    private static final DateTimeFormatter DATE_TIME_FORMATTER_MICROSECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSXXXXX");

    // Formatter for datetime without microseconds
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXXXX");

    // Parse and save the file from a given file path
    public void parseAndSaveFileFromPath(String filePath) throws IOException {
        File file = new File(filePath);

        // Check if the file exists
        if (!file.exists()) {
            throw new IOException("File not found at location: " + filePath);
        }

        List<Earthquake> events = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip the header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] fields = line.split(","); // Assuming CSV (comma-separated values)

                if (fields.length == 0 || fields[0] == null || fields[0].isEmpty()) {
                    System.err.println("Skipping row due to missing eventID: " + line);
                    continue;  // Skip rows without eventID
                }

                Earthquake event = new Earthquake();


                event.setEventID(fields[0]); // Set eventID

                // Parse datetime with fallback for microseconds and without
                if (fields.length > 1) {
                    try {
                        event.setDatetime(OffsetDateTime.parse(fields[1], DATE_TIME_FORMATTER_MICROSECONDS));
                    } catch (DateTimeParseException e1) {
                        // Fallback to non-microseconds format
                        try {
                            event.setDatetime(OffsetDateTime.parse(fields[1], DATE_TIME_FORMATTER));
                        } catch (DateTimeParseException e2) {
                            System.err.println("Invalid datetime format: " + fields[1]);
                            continue;  // Skip this record if both formats fail
                        }
                    }
                }
                if (fields.length > 2) event.setLatitude(parseDouble(fields[2]));
                if (fields.length > 3) event.setLongitude(parseDouble(fields[3]));
                if (fields.length > 4) event.setMagnitude(parseDouble(fields[4]));
                if (fields.length > 5) event.setMagType(fields[5]);
                if (fields.length > 6) event.setDepth(parseDouble(fields[6]));
                if (fields.length > 7) event.setPhasecount(parseInteger(fields[7]));
                if (fields.length > 8) event.setAzimuthGap(parseDouble(fields[8]));
                if (fields.length > 9) event.setLocation(fields[9]);
                // Continue for all other fields

                // Optionally set agency if it exists
                if (fields.length > 10) {
                    event.setAgency(fields[10]);
                }

                // Optionally parse FM-related fields if they exist
                // Optionally parse FM-related fields with valid checks
                if (fields.length > 11 && isValidDate(fields[11])) {
                    try {
                        event.setDatetimeFM(OffsetDateTime.parse(fields[11], DATE_TIME_FORMATTER_MICROSECONDS));
                    } catch (DateTimeParseException e1) {
                        try {
                            event.setDatetimeFM(OffsetDateTime.parse(fields[11], DATE_TIME_FORMATTER));
                        } catch (DateTimeParseException e2) {
                            System.err.println("Invalid FM datetime format: " + fields[11]);
                            continue;  // Skip this record if both formats fail
                        }
                    }
                }

                try {
                        if (fields.length > 12) event.setLatFM(parseDouble(fields[12]));
                        if (fields.length > 13) event.setLonFM(parseDouble(fields[13]));
                        if (fields.length > 14) event.setMagFM(parseDouble(fields[14]));
                        if (fields.length > 15) event.setMagTypeFM(fields[15]);
                        if (fields.length > 16) event.setDepthFM(parseDouble(fields[16]));
                        if (fields.length > 17) event.setPhasecountFM(parseInteger(fields[17]));
                        if (fields.length > 18) event.setAzgapFM(parseDouble(fields[18]));
                } catch (DateTimeParseException e) {
                        System.err.println("Invalid FM datetime format: " + fields[11]);
                }


                events.add(event);

                // Save batch to the database
                if (events.size() == 1000) {  // Save in batches of 1000
                    repository.saveAll(events);
                    events.clear();  // Clear the list after saving the batch
                }
            }

            // Save any remaining events
            if (!events.isEmpty()) {
                repository.saveAll(events);
            }
        }
    }

    // Helper methods for parsing
    private Double parseDouble(String value) {
        try {
            return value.isEmpty() ? null : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        try {
            return value.isEmpty() ? null : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Method to check if the date string is valid
    private boolean isValidDate(String dateStr) {
        return dateStr != null && dateStr.matches("\\d{4}-\\d{2}-\\d{2}.*");
    }

    // Retrieve event by eventID
    public Earthquake getEventById(String eventID) {
        return repository.findById(eventID).orElse(null);
    }
}
