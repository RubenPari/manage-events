package com.rubenpari.manageevents.controllers;

import com.rubenpari.manageevents.config.ResponseObject;
import com.rubenpari.manageevents.utils.Status;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping(path = "/auth")
public class AuthController {
    @Value("${google.clientId}")
    private String clientId;

    @Value("${google.clientSecret}")
    private String clientSecret;

    @Value("${google.redirectUri}")
    private String redirectUri;

    @GetMapping(value = "/login")
    public String login(HttpSession session) {
        String state = UUID.randomUUID().toString();

        session.setAttribute("state", state);

        String url = "https://accounts.google.com/o/oauth2/auth?" +
                "response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=https://www.googleapis.com/auth/calendar" +
                "&state=" + state;

        return "redirect:" + url;
    }

    @GetMapping(value = "/callback")
    public ResponseObject callback(HttpSession session,
                                   @RequestParam("code") String code,
                                   @RequestParam("state") String state) throws IOException {
        String sessionState = (String) session.getAttribute("state");

        if (sessionState == null || !sessionState.equals(state)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid state parameter");
        }

        session.removeAttribute("state");

        URL url = new URL("https://accounts.google.com/o/oauth2/token");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        String requestBody = "grant_type=" + URLEncoder.encode("authorization_code", StandardCharsets.UTF_8)
                + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        // execute request
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(requestBody.getBytes(StandardCharsets.UTF_8));
        outputStream.close();

        int responseCode = connection.getResponseCode();

        if (responseCode != 200) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid code");
        }

        // get "access_token" value from response body
        String responseBody = connection.getResponseMessage();
        String[] responseParts = responseBody.split("&");

        String accessToken = responseParts[0].split("=")[1];

        session.setAttribute("AccessToken", accessToken);

        return new ResponseObject("Successfully login", Status.OK);
    }

    @GetMapping(value = "/logout")
    public ResponseObject logout(HttpSession session) {
        session.removeAttribute("AccessToken");

        return new ResponseObject("Successfully logout", Status.OK);
    }
}
