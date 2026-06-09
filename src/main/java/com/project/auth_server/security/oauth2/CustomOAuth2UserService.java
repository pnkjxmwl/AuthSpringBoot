package com.project.auth_server.security.oauth2;

import com.project.auth_server.entity.Role;
import com.project.auth_server.entity.AuthProvider;
import com.project.auth_server.entity.User;
import com.project.auth_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email      = oAuth2User.getAttribute("email");
        String providerId = oAuth2User.getName();

        if (email == null) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("email_not_found"),
                "Email not provided by OAuth2 provider"
            );
        }

        User user = userRepository.findByEmail(email)
            .map(existing -> {
                // If user exists but registered with a different provider
                if (existing.getProvider() != AuthProvider.GOOGLE) {
                    throw new OAuth2AuthenticationException(
                        new OAuth2Error("wrong_provider"),
                        "Account already exists with " + existing.getProvider() +
                        ". Please login with your email and password."
                    );
                }
                return existing;
            })
            .orElseGet(() -> {
                User newUser = User.builder()
                    .email(email)
                    .provider(AuthProvider.GOOGLE)
                    .providerId(providerId)
                    .role(Role.ROLE_USER)
                    .build();
                log.info("New Google user registered: {}", email);
                return userRepository.save(newUser);
            });

        return new CustomOAuth2User(oAuth2User, user);
    }
}