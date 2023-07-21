package com.badrul.ecommercepoc.service;

import com.badrul.ecommercepoc.entity.CustomerEntity;
import com.badrul.ecommercepoc.entity.LineConfigEntity;
import com.badrul.ecommercepoc.entity.LineReservationEntity;
import com.badrul.ecommercepoc.entity.LineReservationItemEntity;
import com.badrul.ecommercepoc.enums.LineEventType;
import com.badrul.ecommercepoc.enums.LineProductOrderStep;
import com.badrul.ecommercepoc.enums.OrderFrom;
import com.badrul.ecommercepoc.enums.UserFrom;
import com.badrul.ecommercepoc.exception.WebhookParseException;
import com.badrul.ecommercepoc.model.CustomerRequest;
import com.badrul.ecommercepoc.model.OrderItemRequest;
import com.badrul.ecommercepoc.model.OrderRequest;
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
import com.linecorp.bot.model.message.flex.component.Separator;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.unit.FlexAlign;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize;
import com.linecorp.bot.model.message.flex.unit.FlexOffsetSize;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
@Service
public class LineService {

    public static final String CHANNEL_SECRET = ""; // set your channel secret
    public static final String SIGNATURE_HEADER_NAME = "X-Line-Signature";
    public static final String REVIEW_GOLD_STAR = "https://scdn.line-apps.com/n/channel_devcenter/img/fx/review_gold_star_28.png";
    public static final String REVIEW_GRAY_STAR = "https://scdn.line-apps.com/n/channel_devcenter/img/fx/review_gray_star_28.png";

    public static final String STORE_NAME = "ChatBot Commerce Shop";
    public static final String STORE_LOCATION = "Temporary House, L-1 Kazipur, Jashore";

    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;

    private final LineConfigRepository lineConfigRepository;
    private final LineReservationRepository lineReservationRepository;
    private final LineReservationItemRepository lineReservationItemRepository;
    private final ProductService productService;
    private final OrderService orderService;
    private final CustomerService customerService;
    private final LineConfigService lineConfigService;

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

                    LineReservationEntity lineReservation = lineReservationRepository.
                            findFirstByBotUserIdAndUserIdAndReservationCompletedFalseOrderByIdDesc(botUserId, userId);

                    if (!ObjectUtils.isEmpty(lineReservation)) {
                        MessageEvent<TextMessageContent> messageEvent = (MessageEvent<TextMessageContent>) event;
                        if (!ObjectUtils.isEmpty(messageEvent.getMessage())) {
                            String userMessage = messageEvent.getMessage().getText();

                            LineReservationItemEntity reservationItem = lineReservationItemRepository
                                    .findFirstByReservationIdOrderByIdDesc(lineReservation.getId());

                            if (!ObjectUtils.isEmpty(reservationItem) && (StringUtils.hasText(userMessage))) {

                                // Event:: Quantity
                                if (reservationItem.getLineProductOrderStep()
                                        .equals(LineProductOrderStep.PRODUCT_SELECTED)) {

                                    saveQuantity(client, messageEvent, userMessage, lineReservation);
                                }

                                // Event:: Shipping Address
                                if (reservationItem.getLineProductOrderStep()
                                        .equals(LineProductOrderStep.ORDER_QUANTITY_PROVIDED)) {

                                    saveShippingAddress(client, messageEvent, userMessage, lineReservation);

                                }

                            }

                        }
                    }
                } else if (event instanceof PostbackEvent postbackEvent) {

                    String postBackContent = postbackEvent.getPostbackContent().getData();

                    // Event:: Start to create nfew Order
                    if (StringUtils.hasText(postBackContent)) {
                        if (postBackContent.equals("startNewOrder")) {
                            createNewOrder(client, postbackEvent, botUserId);
                        }
                        // Event:: Start to create new Order
                        else if (postBackContent.startsWith("placeOrder")) {
                            placeOrder(client, postbackEvent);
                        } else if (postBackContent.startsWith("confirmOrder")) {
                            confirmOrder(client, postbackEvent);
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

    private void saveShippingAddress(LineMessagingClient client,
                                     MessageEvent<TextMessageContent> event,
                                     String userMessage,
                                     LineReservationEntity lineReservation) {

        lineReservation.setShippingAddress(userMessage);

        lineReservationRepository.save(lineReservation);

        sendReplyFlexMessage(client, event.getReplyToken(),
                prepareReceiptForLineOrder(lineReservation));

        LineReservationItemEntity itemEntity = new LineReservationItemEntity();

        itemEntity.setReservation(lineReservation);
        itemEntity.setLineMessageId(event.getMessage().getId());
        itemEntity.setLineEventType(LineEventType.MESSAGE);
        itemEntity.setLineTextMessage(userMessage);
        itemEntity.setLineProductOrderStep(LineProductOrderStep.DELIVERY_ADDRESS_PROVIDED);
        itemEntity.setCreateDate(LocalDateTime.now());
        itemEntity.setUserId(event.getSource().getUserId());

        lineReservationItemRepository.saveAndFlush(itemEntity);

    }

    private void saveQuantity(LineMessagingClient client,
                              MessageEvent<TextMessageContent> messageEvent,
                              String userMessage,
                              LineReservationEntity lineReservation) {

        if (!userMessage.trim().matches("\\d+")) {
            sendReplyTextMessage(client,
                    "Enter order quantity", messageEvent.getReplyToken());
        } else {
            sendReplyTextMessage(client,
                    "Enter shipping address", messageEvent.getReplyToken());

            LineReservationItemEntity itemEntity = new LineReservationItemEntity();
            lineReservation.setOrderQuantity(Integer.parseInt(userMessage));

            lineReservationRepository.save(lineReservation);

            itemEntity.setReservation(lineReservation);
            itemEntity.setLineMessageId(messageEvent.getMessage().getId());
            itemEntity.setLineEventType(LineEventType.MESSAGE);
            itemEntity.setLineTextMessage(userMessage);
            itemEntity.setLineProductOrderStep(LineProductOrderStep.ORDER_QUANTITY_PROVIDED);
            itemEntity.setCreateDate(LocalDateTime.now());
            itemEntity.setUserId(messageEvent.getSource().getUserId());

            lineReservationItemRepository.save(itemEntity);
        }
    }

    private void confirmOrder(LineMessagingClient client,
                              PostbackEvent postbackEvent) {
        String[] postbackItems = postbackEvent.getPostbackContent().getData().split("&");
        Long reservationId = Long.valueOf(postbackItems[1].replace("reservationId=", ""));

        // send Cart items and ask confirmation
        sendReplyTextMessage(client,
                "Thanks for placing the order with us. We will contact you soon.",
                postbackEvent.getReplyToken()
        );

        LineReservationItemEntity itemEntity = new LineReservationItemEntity();

        LineReservationEntity lineReservationEntity = lineReservationRepository.getReferenceById(reservationId);

        itemEntity.setReservation(lineReservationEntity);
        itemEntity.setLineMessageId(postbackEvent.getWebhookEventId());
        itemEntity.setLineEventType(LineEventType.POSTBACK);
        itemEntity.setLineTextMessage(postbackEvent.getPostbackContent().getData());
        itemEntity.setLineProductOrderStep(LineProductOrderStep.ORDER_CONFIRMED);
        itemEntity.setCreateDate(LocalDateTime.now());
        itemEntity.setUserId(postbackEvent.getSource().getUserId());

        lineReservationItemRepository.save(itemEntity);

        lineReservationEntity.setReservationCompleted(true);

        lineReservationRepository.save(lineReservationEntity);

        OrderRequest orderRequest = new OrderRequest();

        orderRequest.setOrderFrom(OrderFrom.LINE);
        orderRequest.setLineReservationId(reservationId);
        orderRequest.setShippingAddress(lineReservationEntity.getShippingAddress());
        orderRequest.setLineReservationId(lineReservationEntity.getId());
        orderRequest.setCustomerId(getCustomer(client, lineReservationEntity.getUserId()));
        orderRequest.setOrderAmount(
                lineReservationEntity.getProduct().getPrice().multiply(BigDecimal.valueOf(lineReservationEntity.getOrderQuantity()))
        );

        orderRequest.setOrderQuantity(lineReservationEntity.getOrderQuantity());

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .orderQuantity(lineReservationEntity.getOrderQuantity())
                .productPrice(lineReservationEntity.getProduct().getPrice())
                .productId(lineReservationEntity.getProduct().getId()).build();

        orderRequest.setItemRequests(List.of(itemRequest));

        orderService.create(orderRequest);
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

    private void placeOrder(
            LineMessagingClient client,
            PostbackEvent postbackEvent
    ) {
        String[] postbackItems = postbackEvent.getPostbackContent().getData().split("&");
        Long reservationId = Long.valueOf(postbackItems[1].replace("reservationId=", ""));
        Long productId = Long.valueOf(postbackItems[2].replace("productId=", ""));

        sendReplyTextMessage(
                client,
                "Enter order quantity",
                postbackEvent.getReplyToken()
        );

        LineReservationItemEntity itemEntity = new LineReservationItemEntity();

        LineReservationEntity lineReservationEntity = lineReservationRepository.getReferenceById(reservationId);

        lineReservationEntity.setProduct(productService.getProductEntity(productId));

        lineReservationRepository.save(lineReservationEntity);

        itemEntity.setReservation(lineReservationRepository.getReferenceById(reservationId));
        itemEntity.setReservation(lineReservationRepository.getReferenceById(reservationId));
        itemEntity.setLineMessageId(postbackEvent.getWebhookEventId());
        itemEntity.setLineEventType(LineEventType.POSTBACK);
        itemEntity.setLineTextMessage(postbackEvent.getPostbackContent().getData());
        itemEntity.setLineProductOrderStep(LineProductOrderStep.PRODUCT_SELECTED);
        itemEntity.setCreateDate(LocalDateTime.now());
        itemEntity.setUserId(postbackEvent.getSource().getUserId());

        lineReservationItemRepository.save(itemEntity);


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

    private void createNewOrder(LineMessagingClient client, PostbackEvent event, String botUserId) {
        try {
            LineReservationEntity lineReservation = new LineReservationEntity();

            lineReservation.setCreateDate(LocalDateTime.now());
            lineReservation.setBotUserId(botUserId);
            lineReservation.setReservationCompleted(false);
            lineReservation.setCode(String.valueOf(new Date().getTime()));
            lineReservation.setUserId(event.getSource().getUserId());

            Long reservationId = lineReservationRepository.saveAndFlush(lineReservation).getId();

            sendReplyFlexMessage(client, ((PostbackEvent) event).getReplyToken(),
                    prepareProductListMessage(reservationId)
            );

        } catch (Exception e) {
            log.error("PostbackEvent for create new Order. ", e);
        }
    }

    private Long getCustomer(LineMessagingClient client, String lineUserId) {
        CustomerEntity customer = customerService.getCustomerEntity(lineUserId);

        try {
            if (ObjectUtils.isEmpty(customer)) {
                CustomerRequest customerRequest = new CustomerRequest();

                UserProfileResponse userProfileResponse = client.getProfile(lineUserId).get();

                customerRequest.setName(userProfileResponse.getDisplayName());
                customerRequest.setLineUserId(userProfileResponse.getUserId());
                customerRequest.setUserFrom(UserFrom.LINE);

                customer = customerService.getCustomerEntity(customerService.create(customerRequest));
            }
            return customer.getId();
        } catch (InterruptedException | ExecutionException e) {
            throw new WebhookParseException("Line user get profile exception");
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
                                                            .label("Place Order")
                                                            .data("placeOrder" +
                                                                    "&reservationId=" + reservationId +
                                                                    "&productId=" + product.getId()
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

    private FlexMessage prepareReceiptForLineOrder(LineReservationEntity lineReservationEntity) {


        List<FlexComponent> productBoxes = new ArrayList<>();

        Text productTitle = Text.builder()
                .text("1. " + lineReservationEntity.getProduct().getTitle())
                .flex(4)
                .size(FlexFontSize.SM)
                .color("#555555")
                .build();

        Text productPrice = Text.builder()
                .text("৳ " + lineReservationEntity.getProduct().getPrice())
                .size(FlexFontSize.SM)
                .flex(2)
                .color("#111111")
                .align(FlexAlign.END)
                .build();

        productBoxes.add(Box.builder()
                .layout(FlexLayout.HORIZONTAL)
                .contents(productTitle, productPrice)
                .build());

        Text receiptText = Text.builder()
                .text("Order Summary")
                .size(FlexFontSize.SM)
                .color("#1DB446")
                .weight(Text.TextWeight.BOLD)
                .build();


        Text orderNoText = Text.builder()
                .text("#" + lineReservationEntity.getCode())
                .size(FlexFontSize.XS)
                .color("#aaaaaa")
                .weight(Text.TextWeight.BOLD)
                .build();


        Text storeName = Text.builder()
                .text(STORE_NAME)
                .margin(FlexMarginSize.SM)
                .size(FlexFontSize.Md)
                .weight(Text.TextWeight.BOLD)
                .build();

        Text storeLocation = Text.builder()
                .text(STORE_LOCATION)
                .size(FlexFontSize.XS)
                .color("#aaaaaa")
                .wrap(true)
                .build();

        Separator separatorXXL = Separator.builder().margin(FlexMarginSize.XXL).build();

        // Items count
        Text itemsText = Text.builder()
                .text("ITEMS")
                .flex(0)
                .size(FlexFontSize.SM)
                .color("#555555")
                .build();

        Text itemsCount = Text.builder()
                .text(String.valueOf(lineReservationEntity.getOrderQuantity()))
                .size(FlexFontSize.SM)
                .color("#111111")
                .align(FlexAlign.END)
                .build();

        Box itemCountBox = Box.builder().layout(FlexLayout.HORIZONTAL).contents(itemsText, itemsCount).build();

        // Total
        Text totalText = Text.builder()
                .text("TOTAL")
                .flex(0)
                .size(FlexFontSize.SM)
                .color("#555555")
                .build();

        Text totalCount = Text.builder()
                .text("৳ " + BigDecimal.valueOf(lineReservationEntity.getOrderQuantity())
                        .multiply(lineReservationEntity.getProduct().getPrice()))
                .size(FlexFontSize.SM)
                .color("#111111")
                .align(FlexAlign.END)
                .build();

        Box totalCountBox = Box.builder().layout(FlexLayout.HORIZONTAL).contents(totalText, totalCount).build();

        // Shipping
        Text shippingTitle = Text.builder()
                .text("Shipping Address")
                .size(FlexFontSize.SM)
                .color("#555555")
                .build();

        System.out.println("lineReservationEntity.getShippingAddress()");
        Text shippingAddress = Text.builder()
                .size(FlexFontSize.SM)
                .text(lineReservationEntity.getShippingAddress())
                .color("#111111")
                .wrap(true)
                .build();

        Box shippingBox = Box.builder().margin(FlexMarginSize.SM)
                .layout(FlexLayout.VERTICAL).contents(shippingTitle, shippingAddress).build();

        productBoxes.add(separatorXXL);
        productBoxes.add(itemCountBox);
        productBoxes.add(totalCountBox);

        Box itemBox = Box.builder()
                .layout(FlexLayout.VERTICAL)
                .spacing(FlexMarginSize.SM)
                .margin(FlexMarginSize.XXL)
                .contents(productBoxes)
                .build();

        FlexComponent orderConfirmButton = Button.builder()
                .height(Button.ButtonHeight.MEDIUM)
                .style(Button.ButtonStyle.PRIMARY)
                .margin(FlexMarginSize.LG)
                .action(
                        PostbackAction.builder()
                                .label("Confirm")
                                .data("confirmOrder&reservationId=" + lineReservationEntity.getId())
                                .build()
                )
                .build();

        Box bodyBox = Box.builder()
                .layout(FlexLayout.VERTICAL)
                .contents(receiptText, orderNoText, storeName, storeLocation, separatorXXL, itemBox,
                        separatorXXL, shippingBox,
                        orderConfirmButton).build();

        Bubble bubble = Bubble.builder().size(Bubble.BubbleSize.KILO).body(bodyBox).build();


        return new FlexMessage("Order Summary", bubble);
    }

}
