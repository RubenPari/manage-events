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
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Collections;

@RestController
@RequestMapping(path = "/auth")
public class AuthController {
    private final Dotenv env;
    private final AuthorizationCodeFlow authorizationCodeFlow;

    public AuthController() {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        this.env = Dotenv.load();

        this.authorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, env.get("CLIENT_ID"), env.get("CLIENT_SECRET"), Collections.singletonList(env.get("SCOPES"))).build();
    }

    @GetMapping(value = "/login")
    public String login() {
        AuthorizationCodeRequestUrl authorizationUrl = authorizationCodeFlow.newAuthorizationUrl().setRedirectUri(this.env.get("REDIRECT_URL"));

        return "redirect:" + authorizationUrl.build();
    }

    @GetMapping(value = "/callback")
    public ResponseObject callback(HttpSession session, @RequestParam("code") String code) throws IOException {
        GoogleTokenResponse tokenResponse;

        try {
            tokenResponse = (GoogleTokenResponse) authorizationCodeFlow.newTokenRequest(code).setRedirectUri(this.env.get("REDIRECT_URI")).execute();
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
