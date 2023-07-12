package com.badrul.ecommercepoc.service;

import com.badrul.ecommercepoc.entity.LineConfigEntity;
import com.badrul.ecommercepoc.entity.LineReservationEntity;
import com.badrul.ecommercepoc.exception.WebhookParseException;
import com.badrul.ecommercepoc.model.ProductResponse;
import com.badrul.ecommercepoc.repository.LineConfigRepository;
import com.badrul.ecommercepoc.repository.LineReservationItemRepository;
import com.badrul.ecommercepoc.repository.LineReservationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.Button;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Icon;
import com.linecorp.bot.model.message.flex.component.Image;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.unit.FlexAlign;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize;
import com.linecorp.bot.model.message.flex.unit.FlexOffsetSize;
import com.linecorp.bot.model.response.BotApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class LineService {

    public static final String CHANNEL_SECRET = "f2d3fbe16672c7718c416c15de5ecde1";
    public static final String SIGNATURE_HEADER_NAME = "X-Line-Signature";
    public static final String REVIEW_GOLD_STAR = "https://scdn.line-apps.com/n/channel_devcenter/img/fx/review_gold_star_28.png";
    public static final String REVIEW_GRAY_STAR = "https://scdn.line-apps.com/n/channel_devcenter/img/fx/review_gray_star_28.png";

    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;

    private final LineConfigRepository lineConfigRepository;
    private final LineReservationRepository lineReservationRepository;
    private final LineReservationItemRepository lineReservationItemRepository;
    private final ProductService productService;

    public void handleLineWebhookRequest() {
        log.info("*** Class: LineChatBotController ***");
        log.info("*** Function: handleLineWebhookRequest ***");
        try {
            // Get Request Body
            final byte[] requestPayload = StreamUtils.copyToByteArray(request.getInputStream());

            final CallbackRequest callbackRequest = objectMapper.readValue(requestPayload, CallbackRequest.class);
            log.info("body:" + callbackRequest);
            log.info("*** *** ***");

            String botUserId = callbackRequest.getDestination();

            if (!StringUtils.hasText(botUserId)) return;

            LineConfigEntity lineConfig = lineConfigRepository.findByBotUserIdAndIsDeleted(botUserId, false);

            if (lineConfig == null) {
                log.error("LINE channel does not configured into the system!");
                throw new WebhookParseException("LINE channel does not configured into the system!");
            } else if (lineConfig.getBotUserId() == null || lineConfig.getBotUserId().isEmpty()) {
                log.error("LINE channel does not configured into the system!");
                throw new WebhookParseException("LINE channel does not configured into the system!");
            }

            // Validate signature of the Webhook request
            if (!validateSignature(lineConfig.getChannelSecret(), requestPayload)) {
                log.error("LINE: Signature does not match!");
                throw new WebhookParseException("LINE: Signature does not match!");
            }

            final LineMessagingClient client = LineMessagingClient
                    .builder(lineConfig.getChannelAccessToken())
                    .build();

            callbackRequest.getEvents().forEach(event -> {
                if (event instanceof MessageEvent) {
                    String userId = event.getSource().getUserId();
                    String userMessageReplyToken = ((MessageEvent<?>) event).getReplyToken();

//                    LineReservationEntity lineReservation = lineReservationRepository.
//                            findFirstByBotUserIdAndUserIdOrderByIdDesc(botUserId, userId);

//                    if (!ObjectUtils.isEmpty(lineReservation)) {
                    MessageEvent messageEvent = (MessageEvent) event;
                    if (messageEvent.getMessage() instanceof TextMessageContent) {
                        String userMessage = ((TextMessageContent) messageEvent.getMessage()).getText();
                        sendReplyTextMessage(client, userMessage, userMessageReplyToken);
                    }
//                    }
                } else if (event instanceof PostbackEvent) {
                    String postBackContent = ((PostbackEvent) event).getPostbackContent().getData();
                    // Start new Reservation event
                    if (StringUtils.hasText(postBackContent)) {
                        if (postBackContent.equals("startNewOrder")) {
                            try {

                                LineReservationEntity lineReservation = new LineReservationEntity();

                                lineReservation.setCreateDate(LocalDateTime.now());
                                lineReservation.setBotUserId(callbackRequest.getDestination());
                                lineReservation.setReservationCompleted(false);
                                lineReservation.setUserId(event.getSource().getUserId());

                                Long reservationId = lineReservationRepository.saveAndFlush(lineReservation).getId();

                                sendReplyFlexMessage(client, ((PostbackEvent) event).getReplyToken(),
                                        prepareProductListMessage(reservationId));

                            } catch (Exception e) {
                                log.error("PostbackEvent for exception. ", e);
                            }
                        } else if (postBackContent.startsWith("productAddToCart")) {
                            String[] postbackItems = postBackContent.split("&");
                            Long reservationId = Long.valueOf(postbackItems[1].replace("reservationId=", ""));
                            Long productId = Long.valueOf(postbackItems[2].replace("productId=", ""));
                        }

                    }

                }
            });
        } catch (Exception e) {
            log.error("*** handleLineWebhookRequest***", e);
        } finally {
            log.info("***Function: handleLineWebhookRequest ***");
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
    private boolean validateSignature(String channelSecret, byte[] requestPayload) throws WebhookParseException,
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


    /**
     * LINE: Send Reply Text Message
     *
     * @param client
     * @param message
     * @param replyToken
     * @return BotApiResponse
     */
    private BotApiResponse sendReplyTextMessage(LineMessagingClient client, String message, String replyToken) {
        try {
            final TextMessage textMessage = new TextMessage(message);
            final ReplyMessage replyMessage = new ReplyMessage(
                    replyToken,
                    textMessage);

            return client.replyMessage(replyMessage).get();
        } catch (Exception e) {
            throw new WebhookParseException("Exception: ");
        }

    }

    /**
     * LINE: Send Reply Flex Message
     *
     * @param client
     * @param replyToken
     * @param flexMessage
     */
    private void sendReplyFlexMessage(LineMessagingClient client,
                                      String replyToken,
                                      FlexMessage flexMessage) {

        ReplyMessage replyMessage = new ReplyMessage(replyToken, flexMessage);

        client.replyMessage(replyMessage);
    }

    private FlexMessage prepareProductListMessage(Long reservationId) {

        List<ProductResponse> products = productService.getAllProducts();
        List<Bubble> productBubbles =
                products.stream().map(product -> {

                    Text productTitle = Text.builder()
                            .text(product.getTitle())
                            .size(FlexFontSize.LG)
                            .weight(Text.TextWeight.BOLD)
                            .wrap(true)
                            .build();

                    Text productDescription = Text.builder()
                            .text(product.getDescription())
                            .weight(Text.TextWeight.REGULAR)
                            .wrap(true)
                            .offsetTop(FlexOffsetSize.SM)
                            .build();

                    Text ratingText = Text.builder()
                            .text("(" + product.getRating() + ")")
                            .size(FlexFontSize.SM)
                            .margin(FlexMarginSize.MD)
                            .build();

                    List<FlexComponent> builders = new ArrayList<>();

                    for (int i = 0; i < 5; i++) {
                        builders.add(
                                Icon.builder()
                                        .url(
                                                URI.create(i < product.getRating() ?
                                                        REVIEW_GOLD_STAR : REVIEW_GRAY_STAR
                                                ))
                                        .build()
                        );
                    }

                    builders.add(ratingText);

                    Box ratingsBox = Box.builder()
                            .layout(FlexLayout.BASELINE)
                            .margin(FlexMarginSize.SM)
                            .contents(builders)
                            .build();

                    Box priceBox = Box.builder()
                            .layout(FlexLayout.BASELINE)
                            .spacing(FlexMarginSize.SM)
                            .contents(
                                    List.of(
                                            Text.builder()
                                                    .text("Price")
                                                    .flex(2)
                                                    .size(FlexFontSize.LG)
                                                    .color("#aaaaaa")
                                                    .build(),
                                            Text.builder()
                                                    .text("৳ " + product.getPrice())
                                                    .flex(7)
                                                    .size(FlexFontSize.LG)
                                                    .color("#666666")
                                                    .weight(Text.TextWeight.BOLD)
                                                    .build()
                                    )
                            )
                            .build();

                    Box footerBox = Box.builder()
                            .layout(FlexLayout.VERTICAL)
                            .contents(
                                    Button.builder()
                                            .height(Button.ButtonHeight.MEDIUM)
                                            .style(Button.ButtonStyle.PRIMARY)
                                            .action(
                                                    PostbackAction.builder()
                                                            .label("Add to Cart")
                                                            .data("productAddToCart&" +
                                                                    "reservationId=" + reservationId +
                                                                    "productId=" + product.getId()
                                                            )
                                                            .build()
                                            )
                                            .build()
                            )
                            .spacing(FlexMarginSize.SM)
                            .build();

                    return Bubble.builder()
                            .size(Bubble.BubbleSize.KILO)
                            .hero(
                                    Image.builder()
                                            .url(URI.create(product.getProductImageUrl()))
                                            .align(FlexAlign.CENTER)
                                            .size(Image.ImageSize.FULL_WIDTH)
                                            .aspectMode(Image.ImageAspectMode.Fit)
                                            .aspectRatio("20:13")
                                            .build()
                            )
                            .body(
                                    Box.builder()
                                            .layout(FlexLayout.VERTICAL)
                                            .contents(
                                                    List.of(productTitle, ratingsBox, priceBox, productDescription)
                                            ).build()
                            )
                            .footer(footerBox)
                            .build();
                }).toList();


        Carousel carousel = Carousel.builder()
                .contents(productBubbles)
                .build();

        return new FlexMessage("Product list", carousel);
    }

}
