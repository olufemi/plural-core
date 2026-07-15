package com.finacial.wealth.backoffice.notification.controller;

import com.finacial.wealth.backoffice.notification.dto.BackofficeNotificationResponse;
import com.finacial.wealth.backoffice.notification.service.BackofficeNotificationService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/backoffice/notifications", "/bo/backoffice/notifications"})
@RequiredArgsConstructor
public class BackofficeNotificationController {

    private final BackofficeNotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('notification.view','ROLE_SUPER_ADMIN')")
    public Page<BackofficeNotificationResponse> list(
            @RequestAttribute("boAdminUserId") Long adminUserId,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return notificationService.list(adminUserId, unreadOnly, page, size);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyAuthority('notification.view','ROLE_SUPER_ADMIN')")
    public Map<String, Object> unreadCount(@RequestAttribute("boAdminUserId") Long adminUserId) {
        return notificationService.unreadCount(adminUserId);
    }

    @PostMapping("/{notificationId}/read")
    @PreAuthorize("hasAnyAuthority('notification.view','ROLE_SUPER_ADMIN')")
    public BackofficeNotificationResponse markRead(
            @PathVariable Long notificationId,
            @RequestAttribute("boAdminUserId") Long adminUserId) {
        return notificationService.markRead(notificationId, adminUserId);
    }

    @PostMapping("/read-all")
    @PreAuthorize("hasAnyAuthority('notification.view','ROLE_SUPER_ADMIN')")
    public Map<String, Object> markAllRead(@RequestAttribute("boAdminUserId") Long adminUserId) {
        return notificationService.markAllRead(adminUserId);
    }
}
