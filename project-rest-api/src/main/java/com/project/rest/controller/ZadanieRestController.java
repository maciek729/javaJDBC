package com.project.rest.controller;

import java.net.URI;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.project.model.Projekt;
import com.project.model.Zadanie;
import com.project.service.ZadanieService;

@RestController
@RequestMapping("/api")
public class ZadanieRestController {

    private final ZadanieService zadanieService;

    @Autowired
    public ZadanieRestController(ZadanieService zadanieService) {
        this.zadanieService = zadanieService;
    }

    @GetMapping("/zadania/{zadanieId}")
    public ResponseEntity<Zadanie> getZadanie(@PathVariable("zadanieId") Integer zadanieId) {
        return ResponseEntity.of(zadanieService.getZadanie(zadanieId));
    }

    @PostMapping("/zadania")
    public ResponseEntity<Void> createZadanie(@Valid @RequestBody Zadanie zadanie) {
        Zadanie createdZadanie = zadanieService.setZadanie(zadanie);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{zadanieId}")
                .buildAndExpand(createdZadanie.getZadanieId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/zadania/{zadanieId}")
    public ResponseEntity<Void> updateZadanie(@Valid @RequestBody Zadanie updatedZadanie, @PathVariable("zadanieId") Integer zadanieId) {
        // Retrieve the existing Projekt from the database
        java.util.Optional<Zadanie> existingZadanieOptional = zadanieService.getZadanie(zadanieId);
        
        // Check if the Projekt exists
        if (existingZadanieOptional.isPresent()) {
            Zadanie existingZadanie = existingZadanieOptional.get();
            
            // Update fields of the existing Projekt with the data from the updatedProjekt object
            existingZadanie.setNazwa(updatedZadanie.getNazwa());
            existingZadanie.setOpis(updatedZadanie.getOpis());
            existingZadanie.setKolejnosc(updatedZadanie.getKolejnosc());
            existingZadanie.setProjekt(updatedZadanie.getProjekt());
            // Example: Update the name
            
            // Save the updated Projekt back to the database
            zadanieService.setZadanie(existingZadanie);
            
            // Return ResponseEntity with status OK (200)
            return ResponseEntity.ok().build();
        } else {
            // If the Projekt with the given ID does not exist, return ResponseEntity with status NOT_FOUND (404)
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/zadania/{zadanieId}")
    public ResponseEntity<Void> deleteZadanie(@PathVariable("zadanieId") Integer zadanieId) {
        return zadanieService.getZadanie(zadanieId)
                .map(z -> {
                    zadanieService.deleteZadanie(zadanieId);
                    return new ResponseEntity<Void>(HttpStatus.OK);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/zadania")
    public Page<Zadanie> getZadania(Pageable pageable) {
        return zadanieService.getZadania(pageable);
    }

    @GetMapping(value = "/zadania", params = "projektId")
    public Page<Zadanie> getZadaniaByProjekt(@RequestParam(name="nazwa")Integer projektId, Pageable pageable) {
        return zadanieService.getZadaniaProjektu(projektId, pageable);
    }
}
