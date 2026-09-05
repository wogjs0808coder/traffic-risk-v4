package com.trafficrisk.v4.profile;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public UserProfile upsert(@RequestBody CreateProfileRequest request) {
        return profileService.upsert(request);
    }

    @GetMapping("/{userId}")
    public UserProfile get(@PathVariable UUID userId) {
        return profileService.get(userId);
    }
}
