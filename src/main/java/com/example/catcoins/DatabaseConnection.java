package com.example.catcoins;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private static final String Url = "jdbc:mysql://fixstuff.net:3306/catcoin"; //host
    private static final String User = "catcoin"; // user
    private static final String Password = "d64o99yA$"; // pass

    private DatabaseConnection() throws SQLException {
        try {
            this.connection = DriverManager.getConnection(Url, User, Password);
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to the database", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.getConnection().isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() throws SQLException {

    }

    public static class EmailConfig {
        public static boolean SendEmail(String RecipientEmail, String Content, String Subject){
            final String username = "no-reply-catcoins@fixstuff.net";
            final String password = "nv8S50&o4";
            final String host = "fixstuff.net";

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", "465");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.charset", "UTF-8");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(
                        Message.RecipientType.TO,
                        InternetAddress.parse(RecipientEmail)
                );
                message.setSubject(Subject);
                message.setText(Content);

                Transport.send(message);
                return true;
            } catch (MessagingException e) {
                e.printStackTrace();
                return false;
            }
        }
        public static boolean SendEmailAttach(String RecipientEmail, String Content, String Subject){
            final String username = "no-reply-catcoins@fixstuff.net";
            final String password = "nv8S50&o4";
            final String host = "fixstuff.net";

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", "465");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.charset", "UTF-8");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(RecipientEmail));
                message.setSubject(Subject);

                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(Content);



                message.setFrom(new InternetAddress(username));
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(new File("src/main/resources/CSV/market.csv"));
                message.setRecipients(
                        Message.RecipientType.TO,
                        InternetAddress.parse(RecipientEmail)
                );
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);
                multipart.addBodyPart(attachmentPart);
                message.setContent(multipart);
                Transport.send(message);
                return true;
            } catch (MessagingException e) {
                e.printStackTrace();
                return false;
            }catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

