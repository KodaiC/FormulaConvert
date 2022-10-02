package com.cogon_k.TwitterLaTeXBot;

import com.sun.net.httpserver.HttpServer;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

public class TwitterLaTeXBot {
    public static void main(String[] args) throws IOException, TwitterException {
        WebhookHandler.twitter = new TwitterFactory(new ConfigurationBuilder().build()).getInstance();

        Properties prop = new Properties();
        String HOSTNAME = "";
        try(FileInputStream reader = new FileInputStream("settings.properties")) {
            prop.load(reader);
            WebhookHandler.SECRET = prop.getProperty("secret");
            WebhookHandler.IS_CRC_CHECK = Boolean.parseBoolean(prop.getProperty("is_crc_check"));
            Generater.DO_PRINT_STDOUT = Boolean.parseBoolean(prop.getProperty("do_print_stdout"));
            HOSTNAME = prop.getProperty("hostname");
        }
        catch (FileNotFoundException e) {
            try(FileOutputStream writer = new FileOutputStream("settings.properties")) {
                prop.setProperty("secret", "");
                prop.setProperty("is_crc_check", "false");
                prop.setProperty("hostname", "");
                prop.setProperty("do_print_stdout", "");
                prop.store(writer, null);
                writer.flush();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(HOSTNAME, 8080), -1);
            server.createContext("/webhook", new WebhookHandler());
            server.setExecutor(null);
            server.start();

            System.out.println("Server Started!");

            Thread.currentThread().join();
            server.stop(0);
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
