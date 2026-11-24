package com.parkeasy.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender sender;
    private final String from;

    public MailService(JavaMailSender sender, @Value("${parkeasy.mail.from:}") String from) {
        this.sender = sender;
        this.from = from;
    }

    public MailResult sendHtml(String to, String subject, String html) {
        if (from == null || from.isBlank()) {
            return MailResult.failed("SMTP non configuré : renseigne MAIL_USERNAME et MAIL_PASSWORD.");
        }
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from, "ParkEasy");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            sender.send(message);
            return MailResult.ok();
        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            log.error("Envoi de l'email à {} impossible : {}", to, e.getMessage());
            return MailResult.failed(explain(e));
        }
    }

    private String explain(Exception e) {
        String text = String.valueOf(e.getMessage());
        if (text.contains("535") || text.toLowerCase().contains("authentication")) {
            return "Gmail a refusé l'authentification : vérifie l'adresse et le mot de passe d'application.";
        }
        return "Le serveur SMTP est injoignable ou a refusé l'envoi.";
    }
}