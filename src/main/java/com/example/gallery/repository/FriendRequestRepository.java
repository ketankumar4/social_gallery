package com.example.gallery.repository;

import com.example.gallery.model.FriendRequest;
import com.example.gallery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByReceiverAndStatus(org.apache.catalina.User user, FriendRequest.Status status);
    List<FriendRequest> findBySenderAndStatus(User sender, FriendRequest.Status status);
    List<FriendRequest> findBySenderAndReceiverAndStatus(User sender, User receiver, FriendRequest.Status status);
}
