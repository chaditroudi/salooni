package com.glowzi.identity.application;

import com.glowzi.identity.application.result.UserProfileResult;
import com.glowzi.identity.domain.User;
import com.glowzi.identity.domain.UserRepository;
import com.glowzi.identity.domain.vo.Phone;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: retrieve the currently authenticated user's profile.
 *
 * Flow:
 * 1. Receive the username (phone) extracted from the JWT by the controller
 * 2. Look up the user in the local DB by phone
 * 3. Return profile data
 *
 * This endpoint is used by the frontend to display the user's profile page.
 */
@Service
public class GetCurrentUserUseCase {

    private final UserRepository userRepository;

    public GetCurrentUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResult execute(String username) {
        Phone phone = new Phone(username);

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found in local DB: " + username));

        return new UserProfileResult(
                user.getId(),
                user.getFullName().value(),
                user.getPhone().value(),
                user.getRole().name(),
                user.getPreferredLanguage()
        );
    }
}
