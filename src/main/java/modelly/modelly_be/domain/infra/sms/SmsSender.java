package modelly.modelly_be.domain.infra.sms;

public interface SmsSender {
    void send(String to, String content);
}