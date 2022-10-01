package com.cogon_k.TwitterLaTeXBot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import twitter4j.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class WebhookHandler implements HttpHandler {
    public static String SECRET = "";
    public static boolean IS_CRC_CHECK = false;
    public static Twitter twitter = null;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("POST".equals(method)) {
            try (InputStream input = exchange.getRequestBody()) {
                String body = new String(input.readAllBytes());
//                System.out.println(body);
                JsonNode json = new ObjectMapper().readTree(body);
                if (json.has("tweet_create_events")) {
                    JsonNode event = json.get("tweet_create_events").get(0);
                    if (!"1575333303347204096".equals(event.get("in_reply_to_user_id_str").asText())) return;
                    Relationship relation = twitter.showFriendship(1575333303347204096L, event.get("user").get("id_str").asLong());
                    if (!(relation.isTargetFollowedBySource() && relation.isTargetFollowingSource())) return;
                    try (Connection connection = DriverManager.getConnection("jdbc:sqlite:tweets.db")) {
                        connection.setAutoCommit(false);
                        try (PreparedStatement statement = connection.prepareStatement("insert into tweets(tweet_id, user_id, tweet) values(?, ?, ?)")) {
                            statement.setString(1, event.get("id_str").asText());
                            statement.setString(2, event.get("user").get("id_str").asText());
                            statement.setString(3, event.get("text").asText());

                            statement.execute();
                        }
                        connection.commit();
                    }
                    catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
//                    System.out.println(Generater.generatePDF(Parser.parse(event.get("text").asText()), true));
                    String uuid = Generater.generatePDF(Parser.parse(event.get("text").asText()), true);
                    StatusUpdate update = new StatusUpdate("@" + event.get("user").get("screen_name").asText() + " \n変換しました！");
                    update.setMedia(new File(uuid + "-image-1.png"));
                    update.setInReplyToStatusId(event.get("id_str").asLong());
                    Status status = twitter.updateStatus(update);
                }
            }
            catch (TwitterException e) {
                throw new RuntimeException(e);
            }
        }
        else if (IS_CRC_CHECK && "GET".equals(method)) {
            byte[] hmacSha256 = null;
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET.getBytes(), "HmacSHA256");
                mac.init(secretKeySpec);
                Map<String, String> query = queryToMap(exchange.getRequestURI().getQuery());
                hmacSha256 = mac.doFinal(query.get("crc_token").getBytes());

                byte[] responseBytes = ("{\"response_token\": \"sha256=" + Base64.getEncoder().encodeToString(hmacSha256) + "\"}").getBytes();
                System.out.println(new String(responseBytes));
                try (OutputStream output = exchange.getResponseBody()) {
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    output.write(responseBytes);
                }
                return;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        exchange.sendResponseHeaders(204, -1);
    }

    public Map<String, String> queryToMap(String query) {
        if(query == null) {
            return null;
        }

        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            }
            else {
                result.put(entry[0], "");
            }
        }

        return result;
    }
}
