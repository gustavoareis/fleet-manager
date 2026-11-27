package com.medagenda.auth;

import com.medagenda.common.error.ConflictException;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creates and removes login accounts. Used by the patient and doctor features. */
@Service
public class UserAccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createAccount(String email, String rawPassword, Role role) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Já existe uma conta com o e-mail " + normalizedEmail + ".");
        }
        return userRepository.save(new User(normalizedEmail, passwordEncoder.encode(rawPassword), role));
    }

    @Transactional
    public void deleteAccount(User user) {
        userRepository.delete(user);
    }

    /** E-mails are compared case-insensitively, so they are always stored in lower case. */
    public static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
