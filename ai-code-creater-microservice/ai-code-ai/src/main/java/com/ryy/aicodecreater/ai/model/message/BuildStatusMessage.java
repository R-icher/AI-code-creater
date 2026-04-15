package com.ryy.aicodecreater.ai.model.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BuildStatusMessage extends StreamMessage {

    /**
     * 构建阶段：
     * BUILD_START / INSTALL_START / INSTALL_SUCCESS / INSTALL_FAIL
     * BUILD_STEP_START / BUILD_STEP_SUCCESS / BUILD_STEP_FAIL
     * BUILD_SUCCESS / BUILD_FAIL
     */
    private String stage;

    /**
     * 展示消息
     */
    private String message;

    /**
     * 预览地址
     */
    private String previewUrl;

    /**
     * 错误信息
     */
    private String errorMessage;

    public BuildStatusMessage() {
        super(StreamMessageTypeEnum.BUILD_STATUS.getValue());
    }

    public BuildStatusMessage(String stage, String message) {
        super(StreamMessageTypeEnum.BUILD_STATUS.getValue());
        this.stage = stage;
        this.message = message;
    }
}