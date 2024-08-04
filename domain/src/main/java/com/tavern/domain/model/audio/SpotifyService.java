package com.tavern.domain.model.audio;

import org.json.JSONObject;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class SpotifyService {
    private static final XLogger logger = XLoggerFactory.getXLogger(SpotifyService.class);

    public final String URI_CHECK_STRING = "spotify";

    private static final String API_PLAYLIST_STRING = "https://api.spotify.com/v1/playlists/{id}/tracks";
    private static final String API_TRACK_STRING = "https://api.spotify.com/v1/tracks/{id}";
    private static final String API_CLIENT_CREDENTIALS_TOKEN_STRING = "https://accounts.spotify.com/api/token";
    private static final String TRACK_STRING = "(.*)track[/:]";
    private static final String REGEX_ANY_CHAR_QUANTIFIER = "(.*)";
    private static final String PLAYLIST_STRING = "(.*)playlist[/:]";
    private static final String HEADER_AUTHORIZATION_KEY = "Authorization";
    private static final String HEADER_CONTENT_TYPE_KEY = "Content-Type";
    private static final String HEADER_CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";
    private static final String BODY_KEY_GRANT_TYPE = "grant_type";
    private static final String RESPONSE_KEY_ACCESS_TOKEN = "access_token";
    private static final String RESPONSE_KEY_TRACK_TITLE = "name";
    private static final String BODY_VALUE_CLIENT_CREDENTIALS = "client_credentials";
    private static final String PREFIX_BASIC_TOKEN = "Basic ";
    private static final String PREFIX_BEARER_TOKEN = "Bearer ";
    private static final Integer TIMEOUT_DURATION = 3;

    private final String client_secret;
    private final String client_id;
    private String token;

    public SpotifyService(String client_id, String client_secret) {
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.token = getAuthorizationToken();
    }

    private String getAuthorizationToken() {
        String encodedClientIdSecret = Base64.getEncoder().encodeToString((client_id + ":" + client_secret).getBytes());

        try {
            HttpResponse<String> response;
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(API_CLIENT_CREDENTIALS_TOKEN_STRING)).header(HEADER_AUTHORIZATION_KEY, PREFIX_BASIC_TOKEN + encodedClientIdSecret).header(HEADER_CONTENT_TYPE_KEY, HEADER_CONTENT_TYPE_VALUE).timeout(Duration.ofSeconds(TIMEOUT_DURATION)).POST(HttpRequest.BodyPublishers.ofString(BODY_KEY_GRANT_TYPE + "=" + BODY_VALUE_CLIENT_CREDENTIALS)).build();

            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonObject = new JSONObject(response.body());
            return jsonObject.getString("access_token");
        } catch (Exception e) {
            logger.error("There was an error retrieving the access token from spotify.");
            return null;
        }
    }

    public Optional<List<Song>> getListOfSongsFromURL(final String spotifyUrl) {
        if (spotifyUrl.contains(URI_CHECK_STRING)) {
            List<Song> songTitleList = new ArrayList<Song>();
            String splitSpotifyUrl = "";
            boolean isTrack = false;
            boolean isPlaylist = false;
            HttpResponse<String> response;

            if (null == this.token) {
                this.token = getAuthorizationToken();
            }

            try {
                if (spotifyUrl.matches(TRACK_STRING + REGEX_ANY_CHAR_QUANTIFIER)) {
                    isTrack = true;
                    splitSpotifyUrl = spotifyUrl.split(TRACK_STRING)[1];
                } else if (spotifyUrl.matches(PLAYLIST_STRING + REGEX_ANY_CHAR_QUANTIFIER)) {
                    isPlaylist = true;
                    splitSpotifyUrl = spotifyUrl.split(PLAYLIST_STRING)[1];
                } else {
                    logger.debug("SpotifyURL did not contain track or playlist.");
                }
            } catch (NullPointerException e) {
                logger.error("Unable to split spotifyUrl: " + spotifyUrl);
            }

            if (!splitSpotifyUrl.isEmpty()) {
                HttpClient httpClient = HttpClient.newHttpClient();

                try {
                    if (isTrack) {
                        URI apiURI = new URI(API_TRACK_STRING.replaceFirst("(\\{id})", splitSpotifyUrl));
                        HttpRequest request = HttpRequest.newBuilder().uri(apiURI).header(HEADER_AUTHORIZATION_KEY, PREFIX_BEARER_TOKEN + token).timeout(Duration.ofSeconds(TIMEOUT_DURATION)).GET().build();

                        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                        JSONObject responseJSON = new JSONObject(response.body());
                        StringBuilder songTitle = new StringBuilder()
                            .append(responseJSON.getString(RESPONSE_KEY_TRACK_TITLE));
                        responseJSON.getJSONArray("artists").forEach(artist -> {
                            String artistName = ((JSONObject) artist).getString("name");
                            songTitle.append(" ")
                                .append(artistName);
                        });

                        songTitleList.add(new Song(new SongId(songTitle.toString()), new URI("")));
                    } else if (isPlaylist) {
                        URI apiURI = new URI(API_PLAYLIST_STRING.replaceFirst("(\\{id})", splitSpotifyUrl));
                        HttpRequest request = HttpRequest.newBuilder()
                            .uri(apiURI).header(HEADER_AUTHORIZATION_KEY, PREFIX_BEARER_TOKEN + token)
                            .timeout(Duration.ofSeconds(TIMEOUT_DURATION))
                            .GET()
                            .build();

                        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        JSONObject responseJSON = new JSONObject(response.body());
                        for (Object items : responseJSON.getJSONArray("items")) {
                            JSONObject track = ((JSONObject) items).getJSONObject("track");
                            StringBuilder songTitle = new StringBuilder().append(track.getString("name"));
                            track.getJSONArray("artists").forEach(artist -> {
                                songTitle.append(" ").append(((JSONObject) artist).get("name"));
                            });
                            songTitleList.add(new Song(new SongId(songTitle.toString()), new URI("")));
                        }
                    }
                } catch (URISyntaxException ex) {
                    logger.error("Invalid URI, failed to query Spotify", ex);
                    return Optional.empty();
                } catch (IOException | InterruptedException ex) {
                    logger.error("Failed to request song(s) on Spotify", ex);
                    return Optional.empty();
                }
            }

            if (!songTitleList.isEmpty()) {
                return Optional.of(songTitleList);
            }
        }

        return Optional.empty();
    }

    public String getClientId() {
        return client_id;
    }

    public String getClientSecret() {
        return client_secret;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
