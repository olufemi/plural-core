/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.campaign.service;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.campaign.ennum.CampaignStatus;
import com.finacial.wealth.api.profiling.campaign.ennum.MediaKind;
import com.finacial.wealth.api.profiling.campaign.entity.Campaign;
import com.finacial.wealth.api.profiling.campaign.entity.CampaignAudit;
import com.finacial.wealth.api.profiling.campaign.entity.CampaignMediaItem;
import com.finacial.wealth.api.profiling.campaign.model.CampaignMediaItemRequest;
import com.finacial.wealth.api.profiling.campaign.model.CreateCampaignRequest;
import com.finacial.wealth.api.profiling.campaign.model.UpdateCampaignRequest;
import com.finacial.wealth.api.profiling.campaign.repo.CampaignAuditRepository;
import com.finacial.wealth.api.profiling.campaign.repo.CampaignMediaItemRepository;
import com.finacial.wealth.api.profiling.campaign.repo.CampaignRepository;
import com.finacial.wealth.api.profiling.storage.firebase.FirebaseStorageService;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepo;
    private final CampaignMediaItemRepository itemRepo;
    private final CampaignAuditRepository auditRepo;
    private final FirebaseStorageService firebaseStorageService;

    public CampaignService(CampaignRepository campaignRepo,
            CampaignMediaItemRepository itemRepo,
            CampaignAuditRepository auditRepo,
            FirebaseStorageService firebaseStorageService) {
        this.campaignRepo = campaignRepo;
        this.itemRepo = itemRepo;
        this.auditRepo = auditRepo;
        this.firebaseStorageService = firebaseStorageService;
    }

    private MediaKind determineMediaKind(String contentType, String objectName) {

        // 1) Prefer contentType if provided
        if (contentType != null) {
            String c = contentType.toLowerCase();

            if (c.startsWith("image/")) {
                if (c.contains("gif")) {
                    return MediaKind.GIF;
                }
                return MediaKind.IMAGE;
            }
            if (c.startsWith("video/")) {
                return MediaKind.VIDEO;
            }
            if (c.contains("pdf")) {
                return MediaKind.PDF;
            }
            if (c.contains("powerpoint") || c.contains("presentation")) {
                return MediaKind.SLIDE;
            }
        }

        // 2) Fallback: infer from filename/objectName extension
        if (objectName != null) {
            String f = objectName.toLowerCase();

            if (f.endsWith(".gif")) {
                return MediaKind.GIF;
            }
            if (f.endsWith(".jpg") || f.endsWith(".jpeg") || f.endsWith(".png") || f.endsWith(".webp")) {
                return MediaKind.IMAGE;
            }
            if (f.endsWith(".mp4") || f.endsWith(".mov") || f.endsWith(".mkv") || f.endsWith(".webm")) {
                return MediaKind.VIDEO;
            }
            if (f.endsWith(".pdf")) {
                return MediaKind.PDF;
            }
            if (f.endsWith(".ppt") || f.endsWith(".pptx")) {
                return MediaKind.SLIDE;
            }
        }

        return MediaKind.OTHER;
    }

    @Transactional
    public Campaign create(CreateCampaignRequest req, String actor) {
        
        System.out.println("CreateCampaignRequest reeq ::::: %S " + new
        Gson().toJson(req));

        validateCampaignBasics(req.embeddedLink, req.startAt, req.endAt);
        validateItems(req.items);

        // Firebase existence checks
        for (CampaignMediaItemRequest it : req.items) {
            if (!firebaseStorageService.exists(it.objectName)) {
                throw new IllegalArgumentException("Media not found in Firebase bucket: " + it.objectName);
            }
        }

        Campaign c = new Campaign();
        c.setTitle(req.title);
        c.setDescription(req.description);
        c.setEmbeddedLink(req.embeddedLink);
        c.setStartAt(req.startAt);
        c.setEndAt(req.endAt);
        c.setRotationSeconds(req.rotationSeconds == null ? 6 : req.rotationSeconds);
        c.setDisplayMode(resolveDisplayMode(req.displayMode, req.items));
        c.setMediaKind(determineMediaKind(req.getMediaContentType(), req.getMediaObjectName()));
        c.setStatus(CampaignStatus.PENDING_APPROVAL);
        c.setCreatedBy(actor);
        c.setCreatedAt(new Date());
        c.setMediaContentType(req.getMediaContentType());
        c.setMediaObjectName(req.getMediaObjectName());
        c.setStartAt(new Date());
        c.setEndAt(new Date());
        c.setApprovedBy(actor);
         // c.setMediaSignedUrl(req.getMediaSignedUrl());

        Campaign saved = campaignRepo.save(c);

        // save items
        persistItems(saved.getId(), req.items);

        audit(saved.getId(), "CREATE", actor, "Created with " + req.items.size() + " media items; submitted for approval");
        return saved;
    }

    @Transactional
    public Campaign update(Long id, UpdateCampaignRequest req, String actor) {
        Campaign c = getOrThrow(id);

        if (req.title != null) {
            c.setTitle(req.title);
        }
        if (req.description != null) {
            c.setDescription(req.description);
        }
        if (req.embeddedLink != null) {
            c.setEmbeddedLink(req.embeddedLink);
        }
        if (req.startAt != null) {
            c.setStartAt(req.startAt);
        }
        if (req.endAt != null) {
            c.setEndAt(req.endAt);
        }
        if (req.rotationSeconds != null) {
            c.setRotationSeconds(req.rotationSeconds);
        }
        if (req.displayMode != null) {
            c.setDisplayMode(req.displayMode);
        }

        validateCampaignBasics(c.getEmbeddedLink(), c.getStartAt(), c.getEndAt());

        // Replace items if provided
        if (req.items != null) {
            validateItems(req.items);

            for (CampaignMediaItemRequest it : req.items) {
                if (!firebaseStorageService.exists(it.objectName)) {
                    throw new IllegalArgumentException("Media not found in Firebase bucket: " + it.objectName);
                }
            }

            itemRepo.deleteByCampaignId(c.getId());
            persistItems(c.getId(), req.items);

            c.setDisplayMode(resolveDisplayMode(c.getDisplayMode(), req.items));
        }

        c.setUpdatedBy(actor);
        c.setUpdatedAt(new Date());

        // Any update requires re-approval
        c.setStatus(CampaignStatus.PENDING_APPROVAL);
        c.setApprovedBy(null);
        c.setApprovedAt(null);

        Campaign saved = campaignRepo.save(c);
        audit(saved.getId(), "UPDATE", actor, "Updated campaign; items=" + (req.items == null ? "unchanged" : req.items.size()) + "; moved to PENDING_APPROVAL");
        return saved;
    }

    @Transactional
    public Campaign approve(Long id, String approver, String note) {
        Campaign c = getOrThrow(id);
        if (c.getStatus() != CampaignStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only PENDING_APPROVAL campaigns can be approved");
        }
        c.setStatus(CampaignStatus.ACTIVE);
        c.setApprovedBy(approver);
        c.setApprovedAt(new Date());
        c.setUpdatedBy(approver);
        c.setUpdatedAt(new Date());

        Campaign saved = campaignRepo.save(c);
        audit(saved.getId(), "APPROVE", approver, (note == null ? "Approved" : note));
        return saved;
    }

    @Transactional
    public Campaign stop(Long id, String actor) {
        Campaign c = getOrThrow(id);
        if (c.getStatus() != CampaignStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE campaigns can be stopped");
        }
        c.setStatus(CampaignStatus.STOPPED);
        c.setUpdatedBy(actor);
        c.setUpdatedAt(new Date());
        Campaign saved = campaignRepo.save(c);
        audit(saved.getId(), "STOP", actor, "Stopped campaign (immediate)");
        return saved;
    }

    @Transactional
    public Campaign cancel(Long id, String actor) {
        Campaign c = getOrThrow(id);
        c.setStatus(CampaignStatus.CANCELLED);
        c.setUpdatedBy(actor);
        c.setUpdatedAt(new Date());
        Campaign saved = campaignRepo.save(c);
        audit(saved.getId(), "CANCEL", actor, "Cancelled campaign");
        return saved;
    }

    @Transactional
    public Campaign restart(Long id, String actor) {
        Campaign c = getOrThrow(id);

        if (!(c.getStatus() == CampaignStatus.STOPPED
                || c.getStatus() == CampaignStatus.CANCELLED
                || c.getStatus() == CampaignStatus.COMPLETED)) {
            throw new IllegalStateException("Only STOPPED, CANCELLED or COMPLETED campaigns can be restarted");
        }

        // block restart if another campaign is ACTIVE (lock)
        List<Campaign> actives = campaignRepo.lockActiveCampaigns();
        if (actives != null && !actives.isEmpty()) {
            throw new IllegalStateException("Restart blocked: another campaign is currently ACTIVE");
        }

        validateCampaignBasics(c.getEmbeddedLink(), c.getStartAt(), c.getEndAt());

        c.setStatus(CampaignStatus.PENDING_APPROVAL);
        c.setApprovedBy(null);
        c.setApprovedAt(null);
        c.setUpdatedBy(actor);
        c.setUpdatedAt(new Date());

        Campaign saved = campaignRepo.save(c);
        audit(saved.getId(), "RESTART", actor, "Restart requested; moved to PENDING_APPROVAL");
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Campaign> listAll() {
        return campaignRepo.findAllNewestFirst();
    }

    @Transactional(readOnly = true)
    public Campaign get(Long id) {
        return getOrThrow(id);
    }

    @Transactional(readOnly = true)
    public List<CampaignMediaItem> getItems(Long campaignId) {
        return itemRepo.findByCampaignIdOrderByOrderNoAsc(campaignId);
    }

    // ----------------- helpers -----------------
    private void persistItems(Long campaignId, List<CampaignMediaItemRequest> items) {
        for (CampaignMediaItemRequest it : items) {
            CampaignMediaItem e = new CampaignMediaItem();
            e.setCampaignId(campaignId);
            e.setOrderNo(it.orderNo);
            e.setObjectName(it.objectName);
            e.setContentType(it.contentType);
            e.setEmbeddedLink(it.embeddedLink);
            e.setMediaKind(MediaKindResolver.resolve(it.contentType, it.objectName));
            itemRepo.save(e);
        }
    }

    private void validateCampaignBasics(String embeddedLink, Date startAt, Date endAt) {
        CampaignValidator.validateUrl(embeddedLink);
        CampaignValidator.validateDates(startAt, endAt);
    }

    private void validateItems(List<CampaignMediaItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Campaign must have at least one media item");
        }
        Set<Integer> orders = new HashSet<Integer>();
        for (CampaignMediaItemRequest it : items) {
            if (it == null) {
                throw new IllegalArgumentException("Media item cannot be null");
            }
            if (it.orderNo == null) {
                throw new IllegalArgumentException("orderNo is required");
            }
            if (!orders.add(it.orderNo)) {
                throw new IllegalArgumentException("Duplicate orderNo: " + it.orderNo);
            }
            if (it.objectName == null || it.objectName.trim().isEmpty()) {
                throw new IllegalArgumentException("objectName is required");
            }
        }
    }

    private String resolveDisplayMode(String requested, List<CampaignMediaItemRequest> items) {
        if (requested != null && !requested.trim().isEmpty()) {
            return requested.trim().toUpperCase();
        }
        return (items != null && items.size() > 1) ? "CAROUSEL" : "SINGLE";
    }

    private Campaign getOrThrow(Long id) {
        Campaign c = campaignRepo.findById(id).orElse(null);
        if (c == null) {
            throw new NoSuchElementException("Campaign not found: " + id);
        }
        return c;
    }

    private void audit(Long campaignId, String action, String actor, String note) {
        CampaignAudit a = new CampaignAudit();
        a.setCampaignId(campaignId);
        a.setAction(action);
        a.setActor(actor);
        a.setEventAt(new Date());
        a.setNote(note);
        auditRepo.save(a);
    }

    @Transactional(readOnly = true)
    public List<CampaignAudit> auditTrail(Long campaignId) {
        return auditRepo.findByCampaignIdOrderByEventAtAsc(campaignId);
    }
}
