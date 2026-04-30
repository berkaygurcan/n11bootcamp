package com.n11bootcamp.user_service.service;

import java.util.ArrayList;
import java.util.List;


import com.n11bootcamp.user_service.entity.ShoppingCart;
import com.n11bootcamp.user_service.entity.User;
import com.n11bootcamp.user_service.exception.BadRequestException;
import com.n11bootcamp.user_service.exception.ResourceNotFoundException;
import com.n11bootcamp.user_service.repository.UserRepository;
import com.n11bootcamp.user_service.request.LoginRequest;
import com.n11bootcamp.user_service.request.SignupRequest;
import com.n11bootcamp.user_service.request.UpdateUserRequest;
import com.n11bootcamp.user_service.response.JwtResponse;
import com.n11bootcamp.user_service.response.MessageResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    // ?? JWT Authentication i�in gerekli config de�erleri application.yml�den okunuyor
    @Value("${jwt.issuer_uri}")   String jwtIssuerUri;
    @Value("${jwt.client_id}")    String jwtClientId;
    @Value("${jwt.client_secret}")String jwtClientSecret;
    @Value("${jwt.grant_type}")   String jwtGrantType;
    @Value("${jwt.scope}")        String jwtScope;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;
    // ?? Di�er microservice�lerle haberle�mek i�in (ShoppingCartService vs.)

    /**
     * ?? Kullan�c� giri�ini do�rular, Identity Provider�dan JWT al�r.
     */
    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        // if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
        //     return ResponseEntity.badRequest().body(new MessageResponse("User credentials are not valid"));
        // }

        // ?? Apache HttpClient ile Token Endpoint�e POST iste�i at
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(jwtIssuerUri.trim());

        // Form-Data parametreleri (OAuth2 Resource Owner Password Credentials Flow)
        List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", jwtGrantType.trim()));
        params.add(new BasicNameValuePair("client_id", jwtClientId.trim()));
        params.add(new BasicNameValuePair("client_secret", jwtClientSecret.trim()));
        params.add(new BasicNameValuePair("username", loginRequest.getUsername().trim()));
        params.add(new BasicNameValuePair("password", loginRequest.getPassword().trim()));
        // params.add(new BasicNameValuePair("scope", jwtScope)); // Opsiyonel

        String accessToken = "";
        try {
            // Request body�ye parametreleri ekle
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            // Request g�nder
            HttpResponse response = httpClient.execute(httpPost);

            // Response body�yi al
            String responseBody = EntityUtils.toString(response.getEntity());
            log.debug("KEYCLOAK_TOKEN_RESPONSE_RECEIVED username={}", loginRequest.getUsername());

            // Access Token�� JSON�dan ��kar
            accessToken = extractAccessToken(responseBody);

        } catch (Exception e) {
            throw new RuntimeException("Authentication service error");
        }

        // ? Kullan�c�ya JWT token ile birlikte d�n
        return ResponseEntity.ok(new JwtResponse(accessToken, user.getId(), user.getUsername(), user.getEmail(),user.getRole()));
    }

    /**
     * ?? JSON response�tan "access_token" de�erini ��kar�r.
     */
    private static String extractAccessToken(String jsonResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            return rootNode.path("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Token response could not be parsed");
        }
    }

    /**
     * ?? Yeni kullan�c� kayd�
     */
    public ResponseEntity<?> registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        PasswordEncoder encoder = new BCryptPasswordEncoder();

        // ?? Yeni kullan�c�y� olu�tur ve kaydet
        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                "Customer"
        );

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    /**
     * ? Kullan�c�y� sil.
     * - �nce ShoppingCartService �a�r�l�r (kullan�c�n�n sepeti varsa silinir).
     * - Sonra User DB�den silinir.
     */
    public ResponseEntity<?> deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        try {
            ShoppingCart shoppingCart = restTemplate.getForObject(
                    "http://SHOPPING-CART-SERVICE/api/shopping-cart/by-name/" + user.getUsername(),
                    ShoppingCart.class);

            if (shoppingCart != null) {
                restTemplate.delete("http://SHOPPING-CART-SERVICE/api/shopping-cart/" + shoppingCart.getId());
            }
        } catch (Exception e) {
            // Cart yoksa veya cart-service cevap vermezse user silme akışı devam eder.
        }

        userRepository.delete(user);

        return ResponseEntity.ok(new MessageResponse("User account deleted successfully!"));
    }

    /**
     * ?? Kullan�c� bilgilerini g�ncelle
     */
    public ResponseEntity<?> updateUser(Long userId, UpdateUserRequest updateUserRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        if (updateUserRequest.getPassword() != null && !updateUserRequest.getPassword().isEmpty()) {
            PasswordEncoder encoder = new BCryptPasswordEncoder();
            user.setPassword(encoder.encode(updateUserRequest.getPassword()));
        }

        if (updateUserRequest.getEmail() != null && !updateUserRequest.getEmail().isEmpty()) {
            if (userRepository.existsByEmail(updateUserRequest.getEmail())) {
                throw new BadRequestException("Email is already in use!");
            }
            user.setEmail(updateUserRequest.getEmail());
        }

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User account updated successfully!"));
    }
}

