package com.badrul.ecommercepoc.service;

import com.linecorp.bot.client.LineMessagingClient;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
@Service
public class LineChatBotService {

    /**
     * Send Reservation Confirmation Message To Line User
     *
     * @param customerId
     * @param message
     */
    public Boolean sendMessageToLineUser(Integer customerId, String message) {
//        Customers customer = customersService.getCustomerById(customerId);
//        if (!ObjectUtils.isEmpty(customer) && customer.getLineUserId() != null) {
//            try {
//                LineConfig config = lineConfigRepository.
//                        findByBotUserIdAndIsDeleted(customer.getLineBotUserId(), false);
//                if (ObjectUtils.isEmpty(config)) {
//                    return null;
//                }
//                final LineMessagingClient client = LineMessagingClient
//                        .builder(config.getChannelAccessToken())
//                        .build();
//                return sendPushTextMessage(client, message, customer.getLineUserId()).getRequestId() != null;
//            } catch (Exception e) {
//                log.error("Send Message To Line User exception!", e);
//                return false;
//            }
//        }
        return false;
    }
}
