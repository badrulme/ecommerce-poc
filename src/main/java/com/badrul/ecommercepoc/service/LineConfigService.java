package com.badrul.ecommercepoc.service;


import com.badrul.ecommercepoc.entity.LineConfigEntity;
import com.badrul.ecommercepoc.model.LineConfigCreateModel;
import com.badrul.ecommercepoc.repository.LineConfigRepository;
import com.linecorp.bot.client.LineBlobClient;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.model.richmenu.RichMenu;
import com.linecorp.bot.model.richmenu.RichMenuArea;
import com.linecorp.bot.model.richmenu.RichMenuBounds;
import com.linecorp.bot.model.richmenu.RichMenuIdResponse;
import com.linecorp.bot.model.richmenu.RichMenuSize;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.util.FileUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.Collections.singletonList;

/**
 * Line Config Service
 *
 * @author Badrul
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class LineConfigService {
    private static final String LINE_BOT_INFO_URL = "https://api.line.me/v2/bot/info";
    private static final String LINE_WEBHOOK_INFO_URL = "https://api.line.me/v2/bot/channel/webhook/endpoint";
    private static final String LINE_USER_PROFILE_INFO_URL = "https://api.line.me/v2/bot/profile/";

    private final LineConfigRepository repository;
    private final RestTemplate restTemplate;


    /**
     * Create a Line Config
     *
     * @param lineConfigModel
     * @return LineConfigEntity
     * @author Badrul
     */
    public LineConfigEntity registerLineConfig(LineConfigCreateModel lineConfigModel) {

        LineConfigEntity newLineConfig = new LineConfigEntity();

        newLineConfig.setBotBasicId(lineConfigModel.getBotBasicId());
        newLineConfig.setChannelName(lineConfigModel.getChannelName());
        newLineConfig.setChannelSecret(lineConfigModel.getChannelSecret());
        newLineConfig.setChannelAccessToken(lineConfigModel.getChannelAccessToken());
        newLineConfig.setBotUserId(lineConfigModel.getBotUserId());
        newLineConfig.setDeleted(false);

        newLineConfig = repository.save(newLineConfig);

        // Create and Set common Rich menu into new registered channel
        if (createAndSetCommonRichMenu(lineConfigModel.getChannelAccessToken()) == null) {
            repository.delete(newLineConfig);
            return null;
        } else {
            return newLineConfig;
        }
    }

    /**
     * Get LineConfigEntity by botUserId
     *
     * @param botUserId
     * @return LineConfigEntity
     * @author Badrul
     */
    public LineConfigEntity findByBotUserId(String botUserId) {
        if (botUserId == null)
            return null;
        return repository.findByBotUserIdAndIsDeleted(botUserId, false);
    }

    /**
     * Get LineConfigEntity by id
     *
     * @param id
     * @return LineConfigEntity
     * @author Badrul
     */
    public LineConfigEntity getLineConfigById(@NotNull Long id) {
        return repository.findByIdAndIsDeleted(id, false).orElse(null);
    }

    /**
     * Delete a LineConfigEntity by id
     *
     * @param id
     * @author Badrul
     */
    public LineConfigEntity deleteLineConfig(Long id) {
        Optional<LineConfigEntity> lineConfig = repository.findById(id);
        if (lineConfig.isEmpty()) {
            return null;
        }
        LineConfigEntity deleteLineConfig = lineConfig.get();
        deleteLineConfig.setDeleted(true);
        return repository.saveAndFlush(deleteLineConfig);
    }


    /**
     * Create and Set common Rich menu
     *
     * @param token
     * @return String
     */
    private String createAndSetCommonRichMenu(String token) {

        try {
            final LineMessagingClient client = LineMessagingClient
                    .builder(token)
                    .build();

            // *** Create Rich Menu object === Start ***
            RichMenuArea richMenuArea = new RichMenuArea(
                    new RichMenuBounds(0, 0, 2500, 781),
                    new PostbackAction(null, "startNewReservation"));

            RichMenu richMenu = RichMenu.builder()
                    .chatBarText("Tap to open")
                    .name("Rich menu")
                    .selected(true)
                    .areas(singletonList(richMenuArea))
                    .size(new RichMenuSize(2500, 843))
                    .build();

            CompletableFuture<RichMenuIdResponse> richMenuIdResponse = client.createRichMenu(richMenu);

            String richMenuId = richMenuIdResponse.get().getRichMenuId();

            if (richMenuId != null && !richMenuId.trim().isEmpty() && richMenuId.startsWith("richmenu-")) {
                // Upload rich menu image
                ClassPathResource cpr = new ClassPathResource("line/richmenu/Rich-Menu.png");

                LineBlobClient lineBlobClient = LineBlobClient.builder(token).buildBlobClient();

                CompletableFuture<BotApiResponse> botApiResponse = lineBlobClient.setRichMenuImage(richMenuId,
                        "image/jpeg", FileUtil.readAsByteArray(cpr.getInputStream()));
                if (botApiResponse.get().getRequestId() != null) {
                    // Set default rich menu into the channel
                    client.setDefaultRichMenu(richMenuId);
                    return richMenuId;
                }
            }
        } catch (IOException e) {
            log.error("*** Rich menu file not found exception ***", e);
            return null;
        } catch (Exception e) {
            log.error("*** Rich menu creation failed exception ***", e);
            return null;
        }
        return null;
        // *** Create Rich Menu object === end ***
    }

    /**
     * Get Line Bot Info By Channel Access Token
     *
     * @param channelAccessToken
     * @return Map
     */
    public Map<String, Object> getLineBotInfoByChannelAccessToken(String channelAccessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + channelAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        Map<String, Object> response = restTemplate.exchange(LINE_BOT_INFO_URL,
                HttpMethod.GET, entity, Map.class).getBody();
        return response;
    }

    /**
     * Get Line Webhook Info By Channel Access Token
     *
     * @param channelAccessToken
     * @return Map
     */
    public Map<String, Object> getLineWebhookInfoByChannelAccessToken(String channelAccessToken) {
        Map<String, Object> response;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.add("Authorization", "Bearer " + channelAccessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            response = restTemplate.exchange(LINE_WEBHOOK_INFO_URL,
                    HttpMethod.GET, entity, Map.class).getBody();
        } catch (Exception e) {
            log.error("*** Line Webhook Info By Channel Access Token exception ***", e);
            throw new NullPointerException("Webhook endpoint not found:: Line Webhook Info By Channel Access Token exception!");
        }
        return response;
    }

    public UserProfileResponse getUserProfile(String channelAccessToken, String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + channelAccessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);



        return restTemplate.exchange(LINE_USER_PROFILE_INFO_URL + userId,
                HttpMethod.GET, entity, UserProfileResponse.class).getBody();

    }
}
