package com.badrul.ecommercepoc.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LineConfigCreateModel {

    private String channelSecret;

    private String channelAccessToken;

    private String botBasicId;

    private String channelName;

    private String botUserId;

}
