package com.badrul.ecommercepoc.service;

import com.badrul.ecommercepoc.exception.WebhookParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.model.event.CallbackRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
@Service
public class LineService {

    public static final String CHANNEL_SECRET = "f2d3fbe16672c7718c416c15de5ecde1";
    public static final String SIGNATURE_HEADER_NAME = "X-Line-Signature";

    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;


    public void handleLineWebhookRequest() {
        log.info("***Class: LineChatBotController***");
        log.info("***Function: handleLineWebhookRequest 開始***");
        try {
            // Get Request Body
            final byte[] requestPayload = StreamUtils.copyToByteArray(request.getInputStream());

            final CallbackRequest callbackRequest = objectMapper.readValue(requestPayload, CallbackRequest.class);
            log.info("body:" + callbackRequest);
            log.info("*** *** ***");

            String botUserId = callbackRequest.getDestination();

            if (!StringUtils.hasText(botUserId)) return;

            // Validate signature of the Webhook request
            if (!validateSignature(requestPayload)) {
                log.error("LINE: Signature does not match!");
                throw new WebhookParseException("LINE: Signature does not match!");
            }

        } catch (Exception e) {
            log.error("*** handleLineWebhookRequest***", e);
        } finally {
            log.info("***Function: handleLineWebhookRequest 終了***");
        }
    }

    /**
     * LINE: Validate the Signature
     *
     * @param requestPayload
     * @return
     * @throws WebhookParseException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private boolean validateSignature(byte[] requestPayload) throws WebhookParseException,
            NoSuchAlgorithmException, InvalidKeyException {

        String requestHeader = request.getHeader(SIGNATURE_HEADER_NAME);

        // validate signature
        if (requestHeader == null || requestHeader.isEmpty()) {
            log.error("Missing '" + "X-Line-Signature' header");
            throw new WebhookParseException("Missing '" + SIGNATURE_HEADER_NAME + "X-Line-Signature' header");
        }

        SecretKeySpec key = new SecretKeySpec(CHANNEL_SECRET.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        String signature = Base64.getEncoder().encodeToString(mac.doFinal(requestPayload));

        if (request.getHeader(SIGNATURE_HEADER_NAME).equals(signature)) {
            log.info("Signature matches");
            return true;
        } else {
            log.info("Signature does not matches");
            return false;
        }
    }


}
