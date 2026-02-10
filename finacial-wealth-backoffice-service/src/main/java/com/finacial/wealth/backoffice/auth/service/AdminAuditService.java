/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.auth.service;

/**
 *
 * @author olufemioshin
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finacial.wealth.backoffice.audit.entity.AdminAuditLog;
import com.finacial.wealth.backoffice.auth.repo.AdminAuditLogRepository;

import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AdminAuditLogRepository repo;
    private final ObjectMapper objectMapper;

    public void log(AdminAuditLog log) {
        repo.save(log);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void audit(String action,
            Long actorId,
            Long targetId,
            String ip,
            String ua,
            Map<String, Object> meta) {

        try {
            log(AdminAuditLog.builder()
                    .actorAdminId(actorId)
                    .action(action)
                    .targetType("BoAdminUser")
                    .targetId(targetId)
                    .ip(ip)
                    .userAgent(ua)
                    .metadataJson(objectMapper.writeValueAsString(meta))
                    .build());
        } catch (Exception ignored) {
            // last resort: don't break business flow because audit serialization failed
        }

    }

//      public void audit(String action, Long actorId, Long targetId, String ip, String ua, Map<String, Object> meta) {
//        try {
//            log(AdminAuditLog.builder()
//                    .actorAdminId(actorId)
//                    .action(action)
//                    .targetType("BoAdminUser")
//                    .targetId(targetId)
//                    .ip(ip)
//                    .userAgent(ua)
//                    .metadataJson(objectMapper.writeValueAsString(meta))
//                    .build());
//        } catch (Exception ignored) {
//            // last resort: don't break business flow because audit serialization failed
//        }
}
