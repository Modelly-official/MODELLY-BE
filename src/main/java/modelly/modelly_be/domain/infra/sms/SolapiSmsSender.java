package modelly.modelly_be.domain.infra.sms;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SolapiSmsSender implements SmsSender {

    private final DefaultMessageService messageService;

    @Value("${coolsms.from}")
    private String from;

    public SolapiSmsSender(
            @Value("${coolsms.api-key}") String apiKey,
            @Value("${coolsms.api-secret}") String apiSecret
    ) {
        this.messageService = SolapiClient.INSTANCE.createInstance(apiKey, apiSecret);
    }

    @Override
    public void send(String to, String content) {
        try {
            Message message = new Message();
            message.setFrom(from);
            message.setTo(to);
            message.setText(content);

            Object response = messageService.send(message);

        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.CODE_SEND_FAIL);
        }
    }
}
