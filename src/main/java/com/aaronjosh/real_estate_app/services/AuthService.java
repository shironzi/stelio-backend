/*
 * Service layer responsible for handling user authentication and registration.
 */

package com.aaronjosh.real_estate_app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aaronjosh.real_estate_app.dto.auth.LoginResDto;
import com.aaronjosh.real_estate_app.dto.auth.RegisterReqDto;
import com.aaronjosh.real_estate_app.exceptions.EmailAlreadyExistsException;
import com.aaronjosh.real_estate_app.exceptions.PasswordNotMatchException;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.repositories.UserRepository;
import com.aaronjosh.real_estate_app.security.JwtService;

import jakarta.servlet.http.HttpServletRequest;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    /*
     * Registers a new user.
     * - Validates email uniqueness.
     * - Validates password confirmation.
     * - Hashes password before saving.
     */
    public UserEntity register(RegisterReqDto user) {
        // checking if the email is already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        if (!user.getPassword().equals(user.getConfirmPassword())) {
            throw new PasswordNotMatchException("Passwords do not match");
        }

        // creating user entity
        UserEntity newUser = new UserEntity();
        newUser.setEmail(user.getEmail());
        newUser.setFirstname(user.getFirstname());
        newUser.setLastname(user.getLastname());

        // hashing the password with bcrypt
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(newUser);
    }

    /*
     * Handles user Login and generate a JWT Token.
     */

    @Transactional(readOnly = true)
    public LoginResDto login(String email, String password) {

        // checks if email exists
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        // checks password if valid
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        String jwtToken = jwtService.generateToken(user);

        LoginResDto loginData = new LoginResDto();

        loginData.setName(user.getFullName());
        loginData.setEmail(user.getEmail());
        loginData.setRole(user.getRole());
        loginData.setToken(jwtToken);

        return loginData;

    }

    public void logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            jwtService.revokeToken(token);
        }
    }
}
