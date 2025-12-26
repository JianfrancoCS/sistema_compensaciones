package com.agropay.core.hiring.model.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadUrlResponse {

    private String uploadUrl;
    private String apiKey;
    private Long timestamp;
    private String signature;
    private String folder;
}
