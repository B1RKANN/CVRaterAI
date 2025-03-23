package com.birkann.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.birkann.oauth2.OAuth2Utils;
import com.birkann.service.IEmailService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;

@Service
public class GmailService implements IEmailService {
    private static final Logger logger = LoggerFactory.getLogger(GmailService.class);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "CVRaterAI";
    
    @Autowired
    private OAuth2Utils oAuth2Utils;
    
    /**
     * Gmail API için kimlik doğrulama işlemi
     * @param accessToken OAuth2 access token
     * @return Credential nesnesi
     */
    private Credential getCredentials(String accessToken) {
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        return credential;
    }
    
    /**
     * Gmail API istemcisini oluşturur
     * @param accessToken OAuth2 access token
     * @return Gmail API istemcisi
     */
    private Gmail getGmailService(String accessToken) {
        try {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            return new Gmail.Builder(httpTransport, JSON_FACTORY, getCredentials(accessToken))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            logger.error("Gmail service oluşturulurken hata: {}", e.getMessage(), e);
            throw new RuntimeException("Gmail API bağlantısı kurulamadı", e);
        }
    }
    
    @Override
    public List<Map<String, String>> fetchEmails(String accessToken, int maxResults) {
        List<Map<String, String>> emails = new ArrayList<>();
        
        try {
            Gmail service = getGmailService(accessToken);
            
            // E-postaları listele
            ListMessagesResponse listResponse = service.users().messages().list("me").setMaxResults((long) maxResults).execute();
            List<Message> messages = listResponse.getMessages();
            
            if (messages == null || messages.isEmpty()) {
                logger.debug("E-posta bulunamadı");
                return emails;
            }
            
            // Her e-posta için özet bilgileri çek
            for (Message message : messages) {
                Message fullMessage = service.users().messages().get("me", message.getId()).execute();
                
                Map<String, String> emailInfo = new HashMap<>();
                emailInfo.put("id", fullMessage.getId());
                
                // Başlık bilgilerini al
                List<MessagePartHeader> headers = fullMessage.getPayload().getHeaders();
                for (MessagePartHeader header : headers) {
                    switch (header.getName()) {
                        case "Subject":
                            emailInfo.put("subject", header.getValue());
                            break;
                        case "From":
                            emailInfo.put("from", header.getValue());
                            break;
                        case "Date":
                            emailInfo.put("date", header.getValue());
                            break;
                    }
                }
                
                // Kısa bir özet ekle
                String snippet = fullMessage.getSnippet();
                emailInfo.put("snippet", snippet != null ? snippet : "");
                
                emails.add(emailInfo);
            }
            
            return emails;
        } catch (IOException e) {
            logger.error("E-postalar alınırken hata: {}", e.getMessage(), e);
            throw new RuntimeException("E-postalar alınamadı", e);
        }
    }
    
    @Override
    public Map<String, String> fetchEmailDetails(String accessToken, String emailId) {
        Map<String, String> emailDetails = new HashMap<>();
        
        try {
            Gmail service = getGmailService(accessToken);
            
            // E-posta detaylarını çek
            Message fullMessage = service.users().messages().get("me", emailId).execute();
            
            emailDetails.put("id", fullMessage.getId());
            
            // Başlık bilgilerini al
            List<MessagePartHeader> headers = fullMessage.getPayload().getHeaders();
            for (MessagePartHeader header : headers) {
                switch (header.getName()) {
                    case "Subject":
                        emailDetails.put("subject", header.getValue());
                        break;
                    case "From":
                        emailDetails.put("from", header.getValue());
                        break;
                    case "Date":
                        emailDetails.put("date", header.getValue());
                        break;
                    case "To":
                        emailDetails.put("to", header.getValue());
                        break;
                }
            }
            
            // İçeriği çıkar
            emailDetails.put("content", extractEmailContent(fullMessage.getPayload()));
            
            return emailDetails;
        } catch (IOException e) {
            logger.error("E-posta detayları alınırken hata: {}", e.getMessage(), e);
            throw new RuntimeException("E-posta detayları alınamadı", e);
        }
    }
    
    /**
     * E-postanın içeriğini çıkarır
     * @param messagePart E-posta mesaj parçası
     * @return Çıkarılan içerik
     */
    private String extractEmailContent(MessagePart messagePart) {
        if (messagePart.getParts() == null) {
            if ("text/plain".equals(messagePart.getMimeType())) {
                String content = messagePart.getBody().getData();
                if (content != null) {
                    return new String(Base64.getUrlDecoder().decode(content));
                }
            }
            return "";
        }
        
        StringBuilder contentBuilder = new StringBuilder();
        for (MessagePart part : messagePart.getParts()) {
            if ("text/plain".equals(part.getMimeType())) {
                String content = part.getBody().getData();
                if (content != null) {
                    contentBuilder.append(new String(Base64.getUrlDecoder().decode(content)));
                }
            } else if (part.getParts() != null) {
                contentBuilder.append(extractEmailContent(part));
            }
        }
        
        return contentBuilder.toString();
    }
} 