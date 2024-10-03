package com.example.demo.controller;

import com.example.demo.model.Earthquake;
import com.example.demo.service.EarthquakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/earthquake")
public class EarthquakeController {

    @Autowired
    private EarthquakeService earthquakeService;

    @PostMapping("/import-data")
    public ResponseEntity<String> importData(@RequestParam String filePath) {
        try {
            earthquakeService.parseAndSaveFileFromPath(filePath);
            return ResponseEntity.ok("Data imported successfully from " + filePath);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/{eventID}")
    public ResponseEntity<Earthquake> getEventById(@PathVariable String eventID) {
        Earthquake event = earthquakeService.getEventById(eventID);
        if (event != null) {
            return ResponseEntity.ok(event);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
