package com.example.gallery.controller;

import java.security.Principal;

import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.gallery.dto.ApiResponse;
import com.example.gallery.model.FriendRequest;
import com.example.gallery.repository.UserRepository;
import com.example.gallery.service.FriendRequestService;

@RestController
@RequestMapping("/api/friends")
public class FriendRequestController {

    private final FriendRequestService friendRequestService;
    private final UserRepository userRepo;

    public FriendRequestController(FriendRequestService friendRequestService, UserRepository userRepo) {
        this.friendRequestService = friendRequestService;
        this.userRepo = userRepo;
    }

    // send request
    @PostMapping("/send/{toUsername}")
    public ResponseEntity<ApiResponse<FriendRequest>> sendRequest(
            Principal principal,
            @PathVariable String toUsername
    ) {
        FriendRequest request = friendRequestService.sendRequest(principal.getName(), toUsername);
        return ResponseEntity.ok(new ApiResponse<>("Friend request sent", request));
    }

    @PutMapping("/respond/{requestId}")
    public ResponseEntity<ApiResponse<FriendRequest>> respond(
            @PathVariable Long requestId,
            @RequestParam boolean accept
    ) {
        FriendRequest request = friendRequestService.respondToRequest(requestId, accept);
        return ResponseEntity.ok(new ApiResponse<>("Request " + (accept ? "accepted" : "rejected"), request));
    }

    // list pending
    @GetMapping("/pending")
    public ResponseEntity<?> pending(Principal principal) {
        User user = (User) userRepo.findByUsername(principal.getName()).orElseThrow();
        return ResponseEntity.ok(friendRequestService.getPendingRequests(user));
    }
}
