package com.agropay.core.images.model;

public class CreateImageCommand {
    private Long imageableId;
    private String imageableType;
    private String url;

    public CreateImageCommand(Long imageableId, String imageableType, String url) {
        this.imageableId = imageableId;
        this.imageableType = imageableType;
        this.url = url;
    }

    public Long getImageableId() {
        return imageableId;
    }

    public String getImageableType() {
        return imageableType;
    }

    public String getUrl() {
        return url;
    }
}
