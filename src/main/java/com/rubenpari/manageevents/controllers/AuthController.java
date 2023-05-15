package com.rubenpari.manageevents.controllers;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.rubenpari.manageevents.config.ResponseObject;
import com.rubenpari.manageevents.utils.Status;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Arrays;

@RestController
@RequestMapping(path = "/auth")
public class AuthController {
    private final AuthorizationCodeFlow authorizationCodeFlow;

    private static final String CLIENT_ID = "";
    private static final String CLIENT_SECRET = "";
    private static final String[] SCOPES = {"https://www.googleapis.com/auth/calendar"};
    private static final String REDIRECT_URI = "http://localhost:8080/auth/callback";

    public AuthController() {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        this.authorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(SCOPES)).build();
    }

    @GetMapping(value = "/login")
    public String login() {
        AuthorizationCodeRequestUrl authorizationUrl = authorizationCodeFlow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI);

        return "redirect:" + authorizationUrl.build();
    }

    @GetMapping(value = "/callback")
    public ResponseObject callback(HttpSession session, @RequestParam("code") String code) throws IOException {
        GoogleTokenResponse tokenResponse;

        try {
            tokenResponse = (GoogleTokenResponse) authorizationCodeFlow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
        } catch (Error e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error to get access token");
        }

        // save accessToken in session
        session.setAttribute("accessToken", tokenResponse.getAccessToken());

        return new ResponseObject("Sucessfully login", Status.OK);
    }

    @GetMapping(value = "/logout")
    public ResponseObject logout(HttpSession session) {
        session.removeAttribute("AccessToken");

        return new ResponseObject("Sucessfully logout", Status.OK);
    }
}
