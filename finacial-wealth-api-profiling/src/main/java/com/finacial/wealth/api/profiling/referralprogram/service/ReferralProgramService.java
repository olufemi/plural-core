package com.finacial.wealth.api.profiling.referralprogram.service;

import com.finacial.wealth.api.profiling.referralprogram.entity.ReferralProgram;
import com.finacial.wealth.api.profiling.referralprogram.entity.ReferralProgramAudit;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramProductType;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramRewardCurrencyMode;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramRewardMode;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramRewardTarget;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramStatus;
import com.finacial.wealth.api.profiling.referralprogram.model.CreateReferralProgramRequest;
import com.finacial.wealth.api.profiling.referralprogram.model.UpdateReferralProgramRequest;
import com.finacial.wealth.api.profiling.referralprogram.repo.ReferralProgramAuditRepository;
import com.finacial.wealth.api.profiling.referralprogram.repo.ReferralProgramRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReferralProgramService {

    private final ReferralProgramRepository programRepo;
    private final ReferralProgramAuditRepository auditRepo;

    public ReferralProgramService(ReferralProgramRepository programRepo,
            ReferralProgramAuditRepository auditRepo) {
        this.programRepo = programRepo;
        this.auditRepo = auditRepo;
    }

    @Transactional
    public ReferralProgram create(CreateReferralProgramRequest req, String actor) {
        ReferralProgram program = new ReferralProgram();
        applyCreate(program, req);
        program.setProgramCode(resolveProgramCode(req.getProgramCode(), program.getProductType()));
        program.setStatus(ReferralProgramStatus.DRAFT);
        program.setCreatedBy(actor);
        program.setCreatedAt(new Date());
        program.setUpdatedBy(actor);
        program.setUpdatedAt(new Date());
        ReferralProgram saved = programRepo.save(program);
        audit(saved.getId(), "CREATE", actor, "Created referral program in DRAFT");
        return saved;
    }

    @Transactional
    public ReferralProgram update(Long id, UpdateReferralProgramRequest req, String actor) {
        ReferralProgram program = get(id);
        applyUpdate(program, req);
        program.setUpdatedBy(actor);
        program.setUpdatedAt(new Date());
        ReferralProgram saved = programRepo.save(program);
        audit(saved.getId(), "UPDATE", actor, "Updated referral program");
        return saved;
    }

    @Transactional
    public ReferralProgram activate(Long id, String actor) {
        ReferralProgram program = get(id);
        validateProgram(program);
        deactivateOtherActivePrograms(program, actor);
        program.setStatus(ReferralProgramStatus.ACTIVE);
        program.setUpdatedBy(actor);
        program.setUpdatedAt(new Date());
        ReferralProgram saved = programRepo.save(program);
        audit(saved.getId(), "ACTIVATE", actor, "Activated referral program");
        return saved;
    }

    @Transactional
    public ReferralProgram pause(Long id, String actor) {
        ReferralProgram program = get(id);
        if (program.getStatus() != ReferralProgramStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE referral programs can be paused");
        }
        program.setStatus(ReferralProgramStatus.PAUSED);
        program.setUpdatedBy(actor);
        program.setUpdatedAt(new Date());
        ReferralProgram saved = programRepo.save(program);
        audit(saved.getId(), "PAUSE", actor, "Paused referral program");
        return saved;
    }

    @Transactional
    public ReferralProgram end(Long id, String actor) {
        ReferralProgram program = get(id);
        program.setStatus(ReferralProgramStatus.ENDED);
        program.setUpdatedBy(actor);
        program.setUpdatedAt(new Date());
        ReferralProgram saved = programRepo.save(program);
        audit(saved.getId(), "END", actor, "Ended referral program");
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ReferralProgram> listAll() {
        List<ReferralProgram> programs = new ArrayList<>(programRepo.findAll());
        return programs.stream()
                .sorted((a, b) -> {
                    Date right = b.getCreatedAt() == null ? new Date(0) : b.getCreatedAt();
                    Date left = a.getCreatedAt() == null ? new Date(0) : a.getCreatedAt();
                    return right.compareTo(left);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReferralProgram> listByProductType(String productType) {
        if (productType == null || productType.trim().isEmpty()) {
            return listAll();
        }
        return programRepo.findByProductTypeOrderByCreatedAtDesc(parseProductType(productType));
    }

    @Transactional(readOnly = true)
    public ReferralProgram get(Long id) {
        return programRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Referral program not found: " + id));
    }

    @Transactional(readOnly = true)
    public ReferralProgram getActive(String productType) {
        ReferralProgramProductType parsedType = parseProductType(productType);
        List<ReferralProgram> active = programRepo.findActivePrograms(parsedType, ReferralProgramStatus.ACTIVE, new Date());
        if (active.isEmpty()) {
            throw new NoSuchElementException("No active referral program for product: " + parsedType);
        }
        return active.get(0);
    }

    @Transactional(readOnly = true)
    public List<ReferralProgramAudit> auditTrail(Long programId) {
        return auditRepo.findByProgramIdOrderByEventAtAsc(programId);
    }

    private void applyCreate(ReferralProgram program, CreateReferralProgramRequest req) {
        program.setTitle(requireText(req.getTitle(), "title"));
        program.setDescription(trimToNull(req.getDescription()));
        program.setProductType(parseProductType(req.getProductType()));
        program.setRewardTarget(parseRewardTarget(req.getRewardTarget()));
        program.setRewardMode(parseRewardMode(req.getRewardMode()));
        program.setRewardValue(requirePositive(req.getRewardValue(), "rewardValue"));
        program.setRewardCurrencyMode(parseRewardCurrencyMode(req.getRewardCurrencyMode()));
        program.setFixedCurrencyCode(normalizeCurrencyCode(req.getFixedCurrencyCode()));
        program.setMinQualifyingAmount(nonNegative(req.getMinQualifyingAmount(), "minQualifyingAmount"));
        program.setMinRewardAmount(nonNegative(req.getMinRewardAmount(), "minRewardAmount"));
        program.setMaxRewardAmount(nonNegative(req.getMaxRewardAmount(), "maxRewardAmount"));
        program.setQualifyingTransactionCount(resolveQualifyingCount(req.getQualifyingTransactionCount()));
        program.setStartAt(req.getStartAt());
        program.setEndAt(req.getEndAt());
        validateProgram(program);
    }

    private void applyUpdate(ReferralProgram program, UpdateReferralProgramRequest req) {
        if (req.getTitle() != null) {
            program.setTitle(requireText(req.getTitle(), "title"));
        }
        if (req.getDescription() != null) {
            program.setDescription(trimToNull(req.getDescription()));
        }
        if (req.getProductType() != null) {
            program.setProductType(parseProductType(req.getProductType()));
        }
        if (req.getRewardTarget() != null) {
            program.setRewardTarget(parseRewardTarget(req.getRewardTarget()));
        }
        if (req.getRewardMode() != null) {
            program.setRewardMode(parseRewardMode(req.getRewardMode()));
        }
        if (req.getRewardValue() != null) {
            program.setRewardValue(requirePositive(req.getRewardValue(), "rewardValue"));
        }
        if (req.getRewardCurrencyMode() != null) {
            program.setRewardCurrencyMode(parseRewardCurrencyMode(req.getRewardCurrencyMode()));
        }
        if (req.getFixedCurrencyCode() != null) {
            program.setFixedCurrencyCode(normalizeCurrencyCode(req.getFixedCurrencyCode()));
        }
        if (req.getMinQualifyingAmount() != null) {
            program.setMinQualifyingAmount(nonNegative(req.getMinQualifyingAmount(), "minQualifyingAmount"));
        }
        if (req.getMinRewardAmount() != null) {
            program.setMinRewardAmount(nonNegative(req.getMinRewardAmount(), "minRewardAmount"));
        }
        if (req.getMaxRewardAmount() != null) {
            program.setMaxRewardAmount(nonNegative(req.getMaxRewardAmount(), "maxRewardAmount"));
        }
        if (req.getQualifyingTransactionCount() != null) {
            program.setQualifyingTransactionCount(resolveQualifyingCount(req.getQualifyingTransactionCount()));
        }
        if (req.getStartAt() != null) {
            program.setStartAt(req.getStartAt());
        }
        if (req.getEndAt() != null) {
            program.setEndAt(req.getEndAt());
        }
        validateProgram(program);
    }

    private void validateProgram(ReferralProgram program) {
        if (program.getStartAt() != null && program.getEndAt() != null
                && program.getEndAt().before(program.getStartAt())) {
            throw new IllegalArgumentException("endAt must be after startAt");
        }
        if (program.getRewardMode() == ReferralProgramRewardMode.PERCENTAGE_OF_TRANSACTION
                && program.getRewardValue().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentage rewardValue cannot exceed 100");
        }
        if (program.getRewardCurrencyMode() == ReferralProgramRewardCurrencyMode.FIXED_CURRENCY
                && isBlank(program.getFixedCurrencyCode())) {
            throw new IllegalArgumentException("fixedCurrencyCode is required for FIXED_CURRENCY rewards");
        }
        if (program.getMinRewardAmount() != null && program.getMaxRewardAmount() != null
                && program.getMaxRewardAmount().compareTo(program.getMinRewardAmount()) < 0) {
            throw new IllegalArgumentException("maxRewardAmount must be greater than or equal to minRewardAmount");
        }
    }

    private void deactivateOtherActivePrograms(ReferralProgram program, String actor) {
        List<ReferralProgram> activePrograms = programRepo.findOtherProgramsByProductAndStatus(
                program.getProductType(),
                ReferralProgramStatus.ACTIVE,
                program.getId() == null ? -1L : program.getId());
        for (ReferralProgram activeProgram : activePrograms) {
            activeProgram.setStatus(ReferralProgramStatus.PAUSED);
            activeProgram.setUpdatedBy(actor);
            activeProgram.setUpdatedAt(new Date());
            programRepo.save(activeProgram);
            audit(activeProgram.getId(), "AUTO_PAUSE", actor,
                    "Auto-paused because another program for " + activeProgram.getProductType() + " was activated");
        }
    }

    private void audit(Long programId, String eventType, String actor, String note) {
        ReferralProgramAudit audit = new ReferralProgramAudit();
        audit.setProgramId(programId);
        audit.setEventType(eventType);
        audit.setActor(actor == null || actor.trim().isEmpty() ? "UNKNOWN" : actor.trim());
        audit.setNote(note);
        audit.setEventAt(new Date());
        auditRepo.save(audit);
    }

    private String resolveProgramCode(String suppliedCode, ReferralProgramProductType productType) {
        String normalized = trimToNull(suppliedCode);
        if (normalized == null) {
            normalized = "REF-" + productType.name() + "-" + System.currentTimeMillis();
        }
        normalized = normalized.toUpperCase(Locale.ROOT);
        Optional<ReferralProgram> existing = programRepo.findByProgramCodeIgnoreCase(normalized);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("programCode already exists: " + normalized);
        }
        return normalized;
    }

    private ReferralProgramProductType parseProductType(String raw) {
        return ReferralProgramProductType.valueOf(requireText(raw, "productType").toUpperCase(Locale.ROOT));
    }

    private ReferralProgramRewardTarget parseRewardTarget(String raw) {
        return ReferralProgramRewardTarget.valueOf(requireText(raw, "rewardTarget").toUpperCase(Locale.ROOT));
    }

    private ReferralProgramRewardMode parseRewardMode(String raw) {
        return ReferralProgramRewardMode.valueOf(requireText(raw, "rewardMode").toUpperCase(Locale.ROOT));
    }

    private ReferralProgramRewardCurrencyMode parseRewardCurrencyMode(String raw) {
        return ReferralProgramRewardCurrencyMode.valueOf(requireText(raw, "rewardCurrencyMode").toUpperCase(Locale.ROOT));
    }

    private Integer resolveQualifyingCount(Integer count) {
        int value = count == null ? 1 : count;
        if (value <= 0) {
            throw new IllegalArgumentException("qualifyingTransactionCount must be greater than zero");
        }
        return value;
    }

    private BigDecimal requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
        return value;
    }

    private BigDecimal nonNegative(BigDecimal value, String fieldName) {
        if (value == null) {
            return null;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
        return value;
    }

    private String normalizeCurrencyCode(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String requireText(String value, String fieldName) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
