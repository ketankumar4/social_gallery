package com.example.gallery.service;

import com.example.gallery.model.FriendRequest;
import com.example.gallery.model.User;
import com.example.gallery.repository.FriendRequestRepository;
import com.example.gallery.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepo;
    private final UserRepository userRepo;

    public FriendRequestService(FriendRequestRepository friendRequestRepo, UserRepository userRepo) {
        this.friendRequestRepo = friendRequestRepo;
        this.userRepo = userRepo;
    }

    public FriendRequest sendRequest(String senderUsername, String receiverUsername) {
        User sender = userRepo.findByUsername(senderUsername).orElseThrow();
        User receiver = userRepo.findByUsername(receiverUsername).orElseThrow();

        // avoid duplicates
        if (!friendRequestRepo.findBySenderAndReceiverAndStatus(sender, receiver, FriendRequest.Status.PENDING).isEmpty()) {
            throw new RuntimeException("Request already sent");
        }

        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        return friendRequestRepo.save(request);
    }

    public FriendRequest respondToRequest(Long requestId, boolean accept) {
        FriendRequest request = friendRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus(accept ? FriendRequest.Status.ACCEPTED : FriendRequest.Status.REJECTED);
        return friendRequestRepo.save(request);
    }

    public List<FriendRequest> getPendingRequests(org.apache.catalina.User user) {
        return friendRequestRepo.findByReceiverAndStatus(user, FriendRequest.Status.PENDING);
    }
}
