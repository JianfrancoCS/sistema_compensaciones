package com.agropay.core.attendance.web;

import com.agropay.core.attendance.model.websocket.AttendanceCountWebSocketDTO;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import io.github.springwolf.bindings.stomp.annotations.StompAsyncOperationBinding;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

/**
 * WebSocket Documentation Controller for AsyncAPI.
 * This controller is used solely for SpringWolf AsyncAPI documentation generation.
 * The actual WebSocket functionality is handled by AttendanceWebSocketEventListener.
 */
@Controller
@RequiredArgsConstructor
public class AttendanceWebSocketDocumentationController {

    /**
     * Documents the general attendance count topic for all person types.
     * Topic: /topic/subsidiary/{subsidiaryId}/count
     */
    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "/topic/subsidiary/{subsidiaryId}/count",
        description = "Real-time attendance count updates for a specific subsidiary. " +
                     "Includes both employees and external persons. " +
                     "Published automatically when attendance markings occur."
    ))
    @StompAsyncOperationBinding
    public void publishGeneralCount(AttendanceCountWebSocketDTO attendanceCount) {
        // This method is for documentation only - actual publishing handled by event listener
    }

    /**
     * Documents the employees-only attendance count topic.
     * Topic: /topic/subsidiary/{subsidiaryId}/employees/count
     */
    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "/topic/subsidiary/{subsidiaryId}/employees/count",
        description = "Real-time attendance count updates for employees only. " +
                     "Excludes external persons. " +
                     "Published automatically when employee markings occur."
    ))
    @StompAsyncOperationBinding
    public void publishEmployeesCount(AttendanceCountWebSocketDTO attendanceCount) {
        // This method is for documentation only - actual publishing handled by event listener
    }

    /**
     * Documents the externals-only attendance count topic.
     * Topic: /topic/subsidiary/{subsidiaryId}/externals/count
     */
    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "/topic/subsidiary/{subsidiaryId}/externals/count",
        description = "Real-time attendance count updates for external persons only. " +
                     "Excludes employees. " +
                     "Published automatically when external person markings occur."
    ))
    @StompAsyncOperationBinding
    public void publishExternalsCount(AttendanceCountWebSocketDTO attendanceCount) {
        // This method is for documentation only - actual publishing handled by event listener
    }
}