package com.parkeasy.notification.service;

import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class EmailTemplates {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

    public record ReservationMail(String fullName, String parking, String spot,
                                  LocalDateTime start, LocalDateTime end,
                                  BigDecimal price, Long reservationId, String appUrl) {
    }

    private EmailTemplates() {
    }

    private static String esc(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }

    private static String paragraph(String html) {
        return "<p style=\"margin:0 0 16px;font-size:15px;line-height:1.6;color:#3a1226;\">" + html + "</p>";
    }

    private static String heading(String text) {
        return "<h2 style=\"margin:0 0 14px;font-size:22px;color:#3a1226;\">" + text + "</h2>";
    }

    public static String welcome(String fullName, String appUrl) {
        String content = heading("Bienvenue, " + esc(fullName) + " !")
                + paragraph("Ton compte ParkEasy est prêt. Trouve une place en quelques secondes, "
                        + "réserve-la et entre dans le parking avec ton QR Code.")
                + paragraph("Avec ParkEasy, tu peux :")
                + "<ul style=\"margin:0 0 18px;padding-left:20px;font-size:15px;line-height:1.8;color:#3a1226;\">"
                + "<li>comparer les parkings selon la distance, le prix et la disponibilité ;</li>"
                + "<li>réserver une place sans risque de double réservation ;</li>"
                + "<li>suivre tes réservations et tes notifications.</li></ul>";
        return layout("Bienvenue sur ParkEasy", content, "Trouver un parking", appUrl + "/parkings");
    }

    public static String confirmed(ReservationMail m) {
        String content = heading("Réservation confirmée")
                + paragraph("Bonjour " + esc(m.fullName()) + ", ta réservation est confirmée. Voici le récapitulatif :")
                + details(m)
                + paragraph("Ton QR Code d'entrée est disponible dans « Mes réservations ».");
        return layout("Réservation confirmée", content, "Voir mes réservations", m.appUrl() + "/my-reservations");
    }

    public static String cancelled(ReservationMail m, boolean paymentFailed) {
        String intro = paymentFailed
                ? "Le paiement n'a pas abouti : la réservation a été annulée et la place libérée."
                : "Ta réservation a bien été annulée.";
        String content = heading("Réservation annulée")
                + paragraph("Bonjour " + esc(m.fullName()) + ", " + intro)
                + details(m);
        return layout("Réservation annulée", content, "Trouver un parking", m.appUrl() + "/parkings");
    }

    public static String rejected(ReservationMail m) {
        String content = heading("Réservation refusée")
                + paragraph("Bonjour " + esc(m.fullName()) + ", désolé, cette place est déjà réservée pour cet horaire. "
                        + "Veuillez choisir un autre créneau ou une autre place.")
                + details(m);
        return layout("Réservation refusée", content, "Réserver une autre place", m.appUrl() + "/parkings");
    }

    private static String details(ReservationMail m) {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] {"Parking", m.parking()});
        rows.add(new String[] {"Place", m.spot()});
        rows.add(new String[] {"Début", m.start().format(WHEN)});
        rows.add(new String[] {"Fin", m.end().format(WHEN)});
        if (m.price() != null) {
            rows.add(new String[] {"Montant", m.price().setScale(2, RoundingMode.HALF_UP).toPlainString() + " DT"});
        }
        rows.add(new String[] {"Réservation", "n°" + m.reservationId()});

        StringBuilder sb = new StringBuilder(
                "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" "
                        + "style=\"background:#fff5f9;border-radius:12px;margin:0 0 20px;\">");
        for (String[] row : rows) {
            sb.append("<tr><td style=\"padding:10px 16px;font-size:14px;color:#8a5a70;width:120px;\">")
                    .append(esc(row[0]))
                    .append("</td><td style=\"padding:10px 16px;font-size:15px;font-weight:bold;color:#3a1226;\">")
                    .append(esc(row[1]))
                    .append("</td></tr>");
        }
        return sb.append("</table>").toString();
    }

    private static String layout(String title, String content, String buttonLabel, String buttonUrl) {
        String button = buttonLabel == null ? ""
                : "<p style=\"margin:8px 0 0;\"><a href=\"" + esc(buttonUrl)
                        + "\" style=\"display:inline-block;background:#ec4899;color:#ffffff;text-decoration:none;"
                        + "padding:13px 28px;border-radius:999px;font-weight:bold;font-size:15px;\">"
                        + esc(buttonLabel) + "</a></p>";
        return """
                <!DOCTYPE html>
                <html lang="fr">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>{{TITLE}}</title></head>
                <body style="margin:0;padding:0;background:#fff5f9;font-family:Arial,Helvetica,sans-serif;">
                <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background:#fff5f9;padding:24px 12px;">
                <tr><td align="center">
                <table role="presentation" width="600" cellspacing="0" cellpadding="0" style="max-width:600px;width:100%;background:#ffffff;border-radius:18px;overflow:hidden;border:1px solid #f8d7e6;">
                <tr><td style="background:#ec4899;padding:24px 30px;">
                <table role="presentation" cellspacing="0" cellpadding="0"><tr>
                <td width="44" height="44" align="center" style="background:#ffffff;border-radius:12px;font-size:26px;font-weight:bold;color:#ec4899;">P</td>
                <td style="padding-left:12px;font-size:24px;font-weight:bold;color:#ffffff;">ParkEasy</td>
                </tr></table>
                </td></tr>
                <tr><td style="padding:30px;">{{CONTENT}}{{BUTTON}}</td></tr>
                <tr><td style="padding:18px 30px;background:#fff5f9;font-size:12px;color:#8a5a70;text-align:center;">ParkEasy · Le stationnement intelligent. Cet email est automatique, merci de ne pas y répondre.</td></tr>
                </table>
                </td></tr>
                </table>
                </body>
                </html>
                """
                .replace("{{TITLE}}", esc(title))
                .replace("{{CONTENT}}", content)
                .replace("{{BUTTON}}", button);
    }
}