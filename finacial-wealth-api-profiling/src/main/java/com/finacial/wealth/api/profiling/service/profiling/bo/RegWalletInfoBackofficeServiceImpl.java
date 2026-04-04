/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.service.profiling.bo;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.domain.RegWalletInfo;
import com.finacial.wealth.api.profiling.repo.RegWalletInfoBackOfficeRepository;
import com.finacial.wealth.api.profiling.repo.RegWalletInfoRepository;
import com.finacial.wealth.api.profiling.repo.UserBlockAuditRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegWalletInfoBackofficeServiceImpl implements RegWalletInfoBackofficeServiceInter {

    private static final String BLOCKED = "0";
    private static final String ACTIVE = "1";

    private final RegWalletInfoBackOfficeRepository regWalletInfoRepository;
    private final UserBlockAuditRepository userBlockAuditRepository;
    private final RegWalletInfoBackofficeMapper mapper;

    @Override
    public Page<RegWalletInfoBackofficeResponse> getAll(Pageable pageable) {
        return regWalletInfoRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    public RegWalletInfoBackofficeResponse getById(Long id) {
        RegWalletInfo entity = regWalletInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer record not found for id: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    public RegWalletInfoBackofficeResponse getByCustomerId(String customerId) {
        RegWalletInfo entity = regWalletInfoRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer record not found for customerId: " + customerId));
        return mapper.toResponse(entity);
    }

    @Override
    public RegWalletInfoBackofficeResponse getByUuid(String uuid) {
        RegWalletInfo entity = regWalletInfoRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Customer record not found for uuid: " + uuid));
        return mapper.toResponse(entity);
    }

    @Override
    public Page<RegWalletInfoBackofficeResponse> filter(
            String keyword,
            String customerId,
            String email,
            String phoneNumber,
            String accountNumber,
            String isUserBlocked,
            Pageable pageable
    ) {
        return regWalletInfoRepository.findAll(
                RegWalletInfoSpecification.filter(
                        keyword,
                        customerId,
                        email,
                        phoneNumber,
                        accountNumber,
                        isUserBlocked
                ),
                pageable
        ).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public RegWalletInfoBackofficeResponse blockUser(Long id, BlockUserRequest request) {
        RegWalletInfo entity = regWalletInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer record not found for id: " + id));

        String previousStatus = normalizeStatus(entity.getIsUserBlocked());
        entity.setIsUserBlocked(BLOCKED);
        RegWalletInfo saved = regWalletInfoRepository.save(entity);

        saveAudit(saved, previousStatus, BLOCKED, "BLOCK", request);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public RegWalletInfoBackofficeResponse unblockUser(Long id, BlockUserRequest request) {
        RegWalletInfo entity = regWalletInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer record not found for id: " + id));

        String previousStatus = normalizeStatus(entity.getIsUserBlocked());
        entity.setIsUserBlocked(ACTIVE);
        RegWalletInfo saved = regWalletInfoRepository.save(entity);

        saveAudit(saved, previousStatus, ACTIVE, "UNBLOCK", request);

        return mapper.toResponse(saved);
    }

    private void saveAudit(RegWalletInfo entity,
            String previousStatus,
            String newStatus,
            String action,
            BlockUserRequest request) {

        UserBlockAudit audit = new UserBlockAudit();
        audit.setRegWalletInfoId(entity.getId());
        audit.setCustomerId(entity.getCustomerId());
        audit.setPreviousStatus(previousStatus);
        audit.setNewStatus(newStatus);
        audit.setAction(action);
        audit.setPerformedBy(request != null ? request.getPerformedBy() : null);
        audit.setReason(request != null ? request.getReason() : null);
        audit.setActionDate(Instant.now());

        userBlockAuditRepository.save(audit);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return ACTIVE;
        }
        return status;
    }
}
