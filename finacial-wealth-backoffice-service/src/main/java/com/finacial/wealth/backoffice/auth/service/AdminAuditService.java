/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.auth.service;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.backoffice.audit.entity.AdminAuditLog;
import com.finacial.wealth.backoffice.auth.repo.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuditService {
    private final AdminAuditLogRepository repo;

    public void log(AdminAuditLog log) {
        repo.save(log);
    }
}
