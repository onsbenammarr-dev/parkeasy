package com.parkeasy.notification.service;

public record MailResult(boolean sent, String error) {

    public static MailResult ok() {
        return new MailResult(true, null);
    }

    public static MailResult failed(String error) {
        return new MailResult(false, error);
    }
}