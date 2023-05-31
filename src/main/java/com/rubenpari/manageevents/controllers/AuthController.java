package com.rubenpari.manageevents.controllers;

import com.rubenpari.manageevents.config.ResponseObject;
import com.rubenpari.manageevents.utils.Status;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Slf4j
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
    public RedirectView login(HttpSession session) {
        log.info("Called login");

        String state = UUID.randomUUID().toString();
        log.info("generate state: " + state);

        session.setAttribute("state", state);

        String url = "https://accounts.google.com/o/oauth2/auth?" +
                "response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=https://www.googleapis.com/auth/calendar" +
                "&state=" + state;

        log.info("Redirecting to: " + url);
        return new RedirectView(url);
    }

    /**
     * TODO: doesn't work
     */
    @GetMapping(value = "/callback")
    public ResponseObject callback(HttpSession session,
                                   @RequestParam("code") String code,
                                   @RequestParam("state") String state) throws IOException, InterruptedException, URISyntaxException {
        log.info("Called callback");
        log.info("code: " + code);
        log.info("state from request: " + state);

        String sessionState = (String) session.getAttribute("state");
        log.info("state from session: " + sessionState);

        if (sessionState == null || !sessionState.equals(state)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid state parameter");
        }

        session.removeAttribute("state");

        String url = "https://accounts.google.com/o/oauth2/token";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(url).toURI())
                .POST(HttpRequest.BodyPublishers.ofString(
                        "code=" + code +
                                "&client_id=" + clientId +
                                "&client_secret=" + clientSecret +
                                "&redirect_uri=" + redirectUri +
                                "&grant_type=authorization_code"
                ))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
        log.info("Sending request to: " + url + " to get access token");

        // send request
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        // check if status code is 200
        if (response.statusCode() != 200) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid code");
        }
        log.info("Response for access token successfully received");

        // get "access_token" value from response body
        String accessToken = response.body().split("\"")[3];

        session.setAttribute("AccessToken", accessToken);
        log.info("Access token saved in session: " + accessToken);

        return new ResponseObject("Successfully login", Status.OK);
    }

    @GetMapping(value = "/logout")
    public ResponseObject logout(HttpSession session) {
        session.removeAttribute("AccessToken");

        return new ResponseObject("Successfully logout", Status.OK);
    }
}
