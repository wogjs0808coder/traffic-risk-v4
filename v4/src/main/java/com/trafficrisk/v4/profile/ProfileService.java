package com.trafficrisk.v4.profile;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProfileService {

    private final UserProfileRepository repository;

    public ProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    public UserProfile upsert(CreateProfileRequest request) {
        if (request.getUserId() != null && !request.getUserId().isBlank()) {
            UUID userId = UUID.fromString(request.getUserId());
            UserProfile existing = repository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필: " + userId));
            existing.setVehicleType(request.getVehicleType());
            existing.setAgeGroup(request.getAgeGroup());
            return repository.save(existing);
        }
        UserProfile newProfile = new UserProfile(request.getVehicleType(), request.getAgeGroup());
        return repository.save(newProfile);
    }

    public UserProfile get(UUID userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필: " + userId));
    }
}
