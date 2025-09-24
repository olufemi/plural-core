/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.rating;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.rating.model.RatingCreateRq;
import com.finacial.wealth.api.fxpeer.exchange.rating.model.RatingService;
import com.finacial.wealth.api.fxpeer.exchange.rating.model.RatingView;
import com.finacial.wealth.api.fxpeer.exchange.rating.model.SellerStats;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RatingController {

    private final RatingService service;

    public RatingController(RatingService service) {
        this.service = service;
    }

    @Operation(summary = "Submit a rating for a seller (buyer-only; order must be RELEASED)")
    @PostMapping("/ratings")
    public ResponseEntity<RatingView> rate(@RequestHeader("X-User-Id") long userId,
            @RequestBody @Valid RatingCreateRq rq) {
        return ResponseEntity.ok(service.addRating(userId, rq));
    }

    @Operation(summary = "List ratings for a seller")
    @GetMapping("/sellers/{sellerId}/ratings")
    public ResponseEntity<Page<RatingView>> list(@PathVariable long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable p = PageRequest.of(page, size);
        return ResponseEntity.ok(service.listSellerRatings(sellerId, p));
    }

    @Operation(summary = "Get seller stats (trades + average rating)")
    @GetMapping("/sellers/{sellerId}/stats")
    public ResponseEntity<SellerStats> stats(@PathVariable long sellerId) {
        return ResponseEntity.ok(service.sellerStats(sellerId));
    }
}
