package com.example.demo.repository;

import com.example.demo.model.Earthquake;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EarthquakeRepository extends JpaRepository<Earthquake, String> {

}