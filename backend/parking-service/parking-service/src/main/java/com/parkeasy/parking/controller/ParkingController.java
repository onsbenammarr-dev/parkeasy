package com.parkeasy.parking.controller;

import com.parkeasy.parking.dto.ParkingRequest;
import com.parkeasy.parking.dto.ParkingResponse;
import com.parkeasy.parking.service.ParkingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parkings")
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping
    public List<ParkingResponse> list() {
        return parkingService.findAllActive();
    }

    @GetMapping("/mine")
    public List<ParkingResponse> mine(Authentication auth) {
        return parkingService.mine(auth);
    }

    @GetMapping("/{id}")
    public ParkingResponse get(@PathVariable Long id) {
        return parkingService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParkingResponse create(Authentication auth, @Valid @RequestBody ParkingRequest request) {
        return parkingService.create(auth, request);
    }

    @PutMapping("/{id}")
    public ParkingResponse update(Authentication auth, @PathVariable Long id,
                                  @Valid @RequestBody ParkingRequest request) {
        return parkingService.update(auth, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(Authentication auth, @PathVariable Long id) {
        parkingService.deactivate(auth, id);
    }
}