package com.financial.wealth.api.transactions.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.wealth.api.transactions.domain.GroupSavingsData;
import com.financial.wealth.api.transactions.models.AddMembersModels;
import com.financial.wealth.api.transactions.models.ApiResponseModel;
import com.financial.wealth.api.transactions.repo.GroupSavingsDataRepo;
import com.financial.wealth.api.transactions.services.grp.sav.fulfil.GroupContribution;
import com.financial.wealth.api.transactions.services.grp.sav.fulfil.GroupContributionRepo;
import com.financial.wealth.api.transactions.services.grp.sav.fulfil.GroupPayout;
import com.financial.wealth.api.transactions.services.grp.sav.fulfil.GroupPayoutRepo;
import com.financial.wealth.api.transactions.services.grp.sav.fulfil.GroupSavingsCycle;
import com.financial.wealth.api.transactions.services.grp.sav.fulfil.GroupSavingsCycleRepo;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupSavingsAdminService {

    private static final ZoneId AFRICA_LAGOS = ZoneId.of("Africa/Lagos");
    private static final DateTimeFormatter DAILY_LABEL = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);
    private static final DateTimeFormatter WEEKLY_LABEL = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
    private static final DateTimeFormatter MONTHLY_LABEL = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
    private static final TypeReference<List<AddMembersModels>> MEMBER_LIST_TYPE = new TypeReference<List<AddMembersModels>>() {
    };

    private final GroupSavingsDataRepo groupSavingsDataRepo;
    private final GroupSavingsCycleRepo groupSavingsCycleRepo;
    private final GroupContributionRepo groupContributionRepo;
    private final GroupPayoutRepo groupPayoutRepo;
    private final ObjectMapper objectMapper;

    public ApiResponseModel listGroups(String status, String search, Integer page, Integer size) {
        int pageNumber = Math.max(page == null ? 0 : page, 0);
        int pageSize = Math.max(size == null ? 20 : Math.min(size, 100), 1);
        String normalizedStatus = normalizeFilter(status);
        String normalizedSearch = normalizeFilter(search);

        List<GroupSavingsData> filtered = groupSavingsDataRepo.findAll().stream()
                .filter(group -> normalizedStatus == null || normalizedGroupStatus(group).equals(normalizedStatus))
                .filter(group -> normalizedSearch == null || groupMatchesSearch(group, normalizedSearch))
                .sorted(Comparator.comparing(GroupSavingsData::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        int fromIndex = Math.min(pageNumber * pageSize, filtered.size());
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());
        List<Map<String, Object>> rows = filtered.subList(fromIndex, toIndex).stream()
                .map(this::toGroupManagementRow)
                .collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("content", rows);
        data.put("page", pageNumber);
        data.put("size", pageSize);
        data.put("totalElements", filtered.size());
        data.put("totalPages", (int) Math.ceil(filtered.size() / (double) pageSize));
        data.put("statusOptions", java.util.Arrays.asList("ALL", "INITIATED", "CREATED", "ACTIVE", "IN_PROGRESS", "COMPLETED", "CLOSED"));

        return success("Group savings groups fetched successfully.", data);
    }

    public ApiResponseModel getGroup(Long groupId) {
        return groupSavingsDataRepo.findById(groupId)
                .map(group -> success("Group savings group fetched successfully.", toGroupDetail(group)))
                .orElseGet(() -> error(404, "Group savings group not found."));
    }

    public ApiResponseModel closeGroup(Long groupId) {
        return groupSavingsDataRepo.findById(groupId)
                .map(group -> {
                    if ("CLOSED".equals(normalizedGroupStatus(group))) {
                        return success("Group savings group is already closed.", toGroupDetail(group));
                    }
                    group.setIsTrnsactionDeleted("1");
                    group.setIsTrnsactionDeletedDesc("Closed");
                    group.setLastModifiedDate(Instant.now());
                    GroupSavingsData saved = groupSavingsDataRepo.save(group);
                    return success("Group savings group closed successfully.", toGroupDetail(saved));
                })
                .orElseGet(() -> error(404, "Group savings group not found."));
    }

    public ApiResponseModel getContributionPayoutMonitoring(String period, LocalDate fromDate, LocalDate toDate, Long groupId) {
        PeriodBucket bucket = PeriodBucket.from(period);
        LocalDate end = toDate != null ? toDate : LocalDate.now(AFRICA_LAGOS);
        LocalDate start = fromDate != null ? fromDate : bucket.defaultStart(end);

        List<GroupSavingsData> groups = getGroups(groupId);
        Map<Long, GroupSavingsData> groupMap = groups.stream().collect(Collectors.toMap(GroupSavingsData::getId, group -> group));
        Set<Long> groupIds = groupMap.keySet();

        List<GroupContribution> settledContributions = groupContributionRepo.findAll().stream()
                .filter(contribution -> groupIds.contains(contribution.getGroupId()))
                .filter(contribution -> contribution.getStatus() == GroupContribution.Status.SETTLED)
                .filter(contribution -> withinRange(contribution.getCreatedAt(), start, end))
                .sorted(Comparator.comparing(GroupContribution::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        List<GroupPayout> settledPayouts = groupPayoutRepo.findAll().stream()
                .filter(payout -> groupIds.contains(payout.getGroupId()))
                .filter(payout -> payout.getStatus() == GroupPayout.Status.SETTLED)
                .filter(payout -> withinRange(payout.getCreatedAt(), start, end))
                .sorted(Comparator.comparing(GroupPayout::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        List<GroupSavingsCycle> cycles = groupSavingsCycleRepo.findAll().stream()
                .filter(cycle -> groupIds.contains(cycle.getGroupId()))
                .collect(Collectors.toList());

        BigDecimal totalContributions = sumAmounts(settledContributions.stream().map(GroupContribution::getAmount).collect(Collectors.toList()));
        BigDecimal totalPayouts = sumAmounts(settledPayouts.stream().map(GroupPayout::getAmount).collect(Collectors.toList()));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalContributions", totalContributions);
        summary.put("totalPayouts", totalPayouts);
        summary.put("netFlow", totalContributions.subtract(totalPayouts));
        summary.put("activeGroups", groups.stream().filter(this::isActiveGroup).count());
        summary.put("lastUpdatedAt", Instant.now().toString());

        Map<String, BucketAccumulator> contributionBuckets = initBuckets(bucket, start, end);
        Map<String, BucketAccumulator> payoutBuckets = initBuckets(bucket, start, end);
        settledContributions.forEach(contribution -> {
            String key = bucket.keyFor(contribution.getCreatedAt().atZone(AFRICA_LAGOS).toLocalDate());
            BucketAccumulator accumulator = contributionBuckets.get(key);
            if (accumulator != null) {
                accumulator.add(contribution.getAmount());
            }
        });
        settledPayouts.forEach(payout -> {
            String key = bucket.keyFor(payout.getCreatedAt().atZone(AFRICA_LAGOS).toLocalDate());
            BucketAccumulator accumulator = payoutBuckets.get(key);
            if (accumulator != null) {
                accumulator.add(payout.getAmount());
            }
        });

        List<Map<String, Object>> trend = new ArrayList<>();
        for (String key : contributionBuckets.keySet()) {
            BucketAccumulator contribution = contributionBuckets.get(key);
            BucketAccumulator payout = payoutBuckets.get(key);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("key", key);
            row.put("label", contribution.label());
            row.put("periodStart", contribution.start().toString());
            row.put("periodEnd", contribution.end().toString());
            row.put("contributionAmount", contribution.amount());
            row.put("payoutAmount", payout.amount());
            row.put("contributionCount", contribution.count());
            row.put("payoutCount", payout.count());
            trend.add(row);
        }

        List<Map<String, Object>> alerts = buildMonitoringAlerts(groupMap, cycles, groupContributionRepo.findAll(), groupPayoutRepo.findAll());

        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("period", bucket.name());
        filters.put("fromDate", start.toString());
        filters.put("toDate", end.toString());
        filters.put("groupId", groupId);
        data.put("filters", filters);
        data.put("summary", summary);
        data.put("trend", trend);
        data.put("alerts", alerts);
        data.put("groupOptions", toGroupOptions(groups));
        data.put("periodOptions", java.util.Arrays.asList("DAILY", "WEEKLY", "MONTHLY"));

        return success("Contribution and payout monitoring fetched successfully.", data);
    }

    public ApiResponseModel getSlotAssignmentTracking(Long groupId, String status) {
        LocalDate today = LocalDate.now(AFRICA_LAGOS);
        List<GroupSavingsData> groups = getGroups(groupId);
        Map<Long, GroupSavingsData> groupMap = groups.stream().collect(Collectors.toMap(GroupSavingsData::getId, group -> group));
        Set<Long> groupIds = groupMap.keySet();

        List<GroupSavingsCycle> cycles = groupSavingsCycleRepo.findAll().stream()
                .filter(cycle -> groupIds.contains(cycle.getGroupId()))
                .sorted(Comparator.comparing(GroupSavingsCycle::getPayoutDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(GroupSavingsCycle::getGroupId)
                        .thenComparing(GroupSavingsCycle::getCycleNumber))
                .collect(Collectors.toList());

        Map<Long, List<AddMembersModels>> memberMap = groups.stream().collect(Collectors.toMap(GroupSavingsData::getId, this::readMembers));

        List<Map<String, Object>> slotSchedule = new ArrayList<>();
        for (GroupSavingsCycle cycle : cycles) {
            GroupSavingsData group = groupMap.get(cycle.getGroupId());
            AddMembersModels member = resolveMemberForSlot(memberMap.getOrDefault(cycle.getGroupId(), java.util.Collections.<AddMembersModels>emptyList()), cycle.getCycleNumber());
            String normalizedStatus = normalizeSlotStatus(cycle, today);
            if (status != null && !status.trim().isEmpty() && !normalizedStatus.equalsIgnoreCase(status)) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("groupId", cycle.getGroupId());
            row.put("groupName", group != null ? group.getGroupSavingName() : null);
            row.put("slotNumber", cycle.getCycleNumber());
            row.put("memberName", member != null ? member.getMemberName() : "Unassigned");
            row.put("memberWalletId", member != null ? member.getMemberId() : null);
            row.put("payoutDate", cycle.getPayoutDate() != null ? cycle.getPayoutDate().toString() : null);
            row.put("status", normalizedStatus);
            row.put("cycleStatus", cycle.getStatus().name());
            row.put("reference", "GS-" + cycle.getGroupId() + "-SLOT-" + cycle.getCycleNumber());
            slotSchedule.add(row);
        }

        List<Map<String, Object>> payoutHistory = groupPayoutRepo.findAll().stream()
                .filter(payout -> groupIds.contains(payout.getGroupId()))
                .sorted(Comparator.comparing(GroupPayout::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(payout -> toPayoutHistoryRow(payout, groupMap, memberMap))
                .collect(Collectors.toList());

        List<Map<String, Object>> alerts = buildSlotAlerts(groupMap, cycles, memberMap, groupContributionRepo.findAll(), groupPayoutRepo.findAll(), today);

        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("groupId", groupId);
        filters.put("status", status);
        data.put("filters", filters);
        data.put("slotSchedule", slotSchedule);
        data.put("payoutHistory", payoutHistory);
        data.put("alerts", alerts);
        data.put("groupOptions", toGroupOptions(groups));
        data.put("statusOptions", java.util.Arrays.asList("UPCOMING", "IN_PROGRESS", "MISSED", "COMPLETED"));

        return success("Slot assignment and tracking fetched successfully.", data);
    }


    public ApiResponseModel getGroupCycleHealth(Long groupId) {
        return groupSavingsDataRepo.findById(groupId)
                .map(group -> {
                    List<AddMembersModels> members = readMembers(group);
                    List<GroupSavingsCycle> cycles = groupSavingsCycleRepo.findByGroupIdOrderByCycleNumberAsc(groupId);
                    List<Map<String, Object>> rows = cycles.stream()
                            .map(cycle -> toCycleHealthRow(cycle, group, members))
                            .collect(Collectors.toList());

                    long attentionCount = rows.stream()
                            .filter(row -> "ATTENTION".equals(row.get("healthStatus")) || "EXPIRED".equals(row.get("healthStatus")))
                            .count();
                    long settledCount = rows.stream()
                            .filter(row -> "COMPLETED".equals(row.get("healthStatus")))
                            .count();

                    Map<String, Object> summary = new LinkedHashMap<>();
                    summary.put("groupId", group.getId());
                    summary.put("groupName", group.getGroupSavingName());
                    summary.put("expectedMembers", members.size());
                    summary.put("cycleCount", rows.size());
                    summary.put("completedCycles", settledCount);
                    summary.put("cyclesNeedingAttention", attentionCount);
                    summary.put("lastUpdatedAt", Instant.now().toString());

                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("summary", summary);
                    data.put("cycles", rows);
                    data.put("actionHints", java.util.Arrays.asList(
                            "Use retry-failed only after validating the failed debit/credit did not already settle downstream.",
                            "If schedulerEligible=false, operations must extend the cycle window or handle settlement manually before expecting the scheduler to pick it up."
                    ));
                    return success("Group savings cycle health fetched successfully.", data);
                })
                .orElseGet(() -> error(404, "Group savings group not found."));
    }

    public ApiResponseModel getCycleHealth(Long cycleId) {
        return groupSavingsCycleRepo.findById(cycleId)
                .map(cycle -> {
                    GroupSavingsData group = groupSavingsDataRepo.findById(cycle.getGroupId()).orElse(null);
                    List<AddMembersModels> members = readMembers(group);
                    return success("Group savings cycle health fetched successfully.", toCycleHealthRow(cycle, group, members));
                })
                .orElseGet(() -> error(404, "Group savings cycle not found."));
    }

    @Transactional
    public ApiResponseModel retryFailedCycle(Long cycleId) {
        GroupSavingsCycle cycle = groupSavingsCycleRepo.findById(cycleId).orElse(null);
        if (cycle == null) {
            return error(404, "Group savings cycle not found.");
        }
        GroupSavingsData group = groupSavingsDataRepo.findById(cycle.getGroupId()).orElse(null);
        if (group == null) {
            return error(404, "Group savings group not found.");
        }

        Instant now = Instant.now();
        List<GroupContribution> contributions = groupContributionRepo.lockByGroupIdAndCycleNumber(cycle.getGroupId(), cycle.getCycleNumber());
        int requeuedContributions = 0;
        for (GroupContribution contribution : contributions) {
            if (contribution.getStatus() == GroupContribution.Status.FAILED) {
                contribution.setStatus(GroupContribution.Status.PENDING);
                contribution.setProviderRef(null);
                contribution.setLastUpdatedAt(now);
                requeuedContributions++;
            }
        }
        if (requeuedContributions > 0) {
            groupContributionRepo.saveAll(contributions);
        }

        GroupPayout payout = groupPayoutRepo.lockByGroupIdAndCycleNumber(cycle.getGroupId(), cycle.getCycleNumber()).orElse(null);
        boolean requeuedPayout = false;
        if (payout != null && payout.getStatus() == GroupPayout.Status.FAILED) {
            payout.setStatus(GroupPayout.Status.PENDING);
            payout.setProviderRef(null);
            payout.setLastUpdatedAt(now);
            groupPayoutRepo.save(payout);
            requeuedPayout = true;
        }

        long settled = contributions.stream()
                .filter(contribution -> contribution.getStatus() == GroupContribution.Status.SETTLED)
                .count();
        int expectedMembers = readMembers(group).size();
        if (cycle.getStatus() == GroupSavingsCycle.CycleStatus.EXPIRED || cycle.getStatus() == GroupSavingsCycle.CycleStatus.PAID) {
            cycle.setStatus(settled >= expectedMembers && expectedMembers > 0
                    ? GroupSavingsCycle.CycleStatus.AWAITING_PAYOUT
                    : GroupSavingsCycle.CycleStatus.IN_PROGRESS);
            cycle.setLastUpdatedAt(now);
            groupSavingsCycleRepo.save(cycle);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cycle", toCycleHealthRow(cycle, group, readMembers(group)));
        data.put("requeuedContributions", requeuedContributions);
        data.put("requeuedPayout", requeuedPayout);
        data.put("schedulerEligible", schedulerEligible(cycle));
        data.put("note", "Failed records were reset to PENDING. Confirm downstream ledger state before retrying to avoid duplicate debit or credit.");
        return success("Group savings failed cycle records requeued successfully.", data);
    }

    private ApiResponseModel success(String description, Object data) {
        ApiResponseModel response = new ApiResponseModel();
        response.setStatusCode(200);
        response.setDescription(description);
        response.setData(data);
        return response;
    }

    private ApiResponseModel error(int statusCode, String description) {
        ApiResponseModel response = new ApiResponseModel();
        response.setStatusCode(statusCode);
        response.setDescription(description);
        return response;
    }

    private Map<String, Object> toGroupManagementRow(GroupSavingsData group) {
        List<AddMembersModels> members = readMembers(group);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("groupId", group.getId());
        row.put("groupName", group.getGroupSavingName());
        row.put("inviteCode", group.getInviteCode());
        row.put("transactionId", group.getTransactionId());
        row.put("transactionIdLink", group.getTransactionIdLink());
        row.put("ownerName", ownerName(members, group));
        row.put("ownerEmailAddress", group.getEmailAddress());
        row.put("ownerPhoneNumber", group.getPhoneNumber());
        row.put("ownerWalletId", group.getWalletId());
        row.put("members", members.size());
        row.put("configuredMembers", group.getNumberOfMembers());
        row.put("cycle", cycleLabel(group));
        row.put("nextPayout", nextPayoutDate(group.getId()));
        row.put("volume", group.getGroupSavingFinalAmount() != null ? group.getGroupSavingFinalAmount() : group.getGroupSavingAmount());
        row.put("status", normalizedGroupStatus(group));
        row.put("transactionStatus", group.getTransactionStatus());
        row.put("transactionStatusDesc", group.getTransactionStatusDesc());
        row.put("createdDate", group.getCreatedDate() != null ? group.getCreatedDate().toString() : null);
        row.put("lastModifiedDate", group.getLastModifiedDate() != null ? group.getLastModifiedDate().toString() : null);
        return row;
    }

    private Map<String, Object> toGroupDetail(GroupSavingsData group) {
        Map<String, Object> detail = toGroupManagementRow(group);
        detail.put("description", group.getGroupSavingDescription());
        detail.put("allowPublicToJoin", group.getAllowPublicToJoin());
        detail.put("adminPayOutSlot", group.getAdminPayOutSlot());
        detail.put("availablePayOutSlot", group.getAvailablePayOutSlot());
        detail.put("contributionDate", group.getContributionDate());
        detail.put("contributionWindowEnd", group.getContributionWindowEnd());
        detail.put("payoutDate", group.getPayoutDate());
        detail.put("payoutPolicy", group.getPayoutPolicy() != null ? group.getPayoutPolicy().name() : null);
        detail.put("contributionFrequency", group.getContributionFrequency() != null ? group.getContributionFrequency().name() : null);
        detail.put("membersList", readMembers(group));
        detail.put("cycles", groupSavingsCycleRepo.findAll().stream()
                .filter(cycle -> Objects.equals(cycle.getGroupId(), group.getId()))
                .sorted(Comparator.comparing(GroupSavingsCycle::getCycleNumber))
                .map(this::toCycleRow)
                .collect(Collectors.toList()));
        return detail;
    }


    private Map<String, Object> toCycleHealthRow(GroupSavingsCycle cycle, GroupSavingsData group, List<AddMembersModels> members) {
        List<GroupContribution> contributions = groupContributionRepo.findByGroupIdAndCycleNumber(cycle.getGroupId(), cycle.getCycleNumber());
        GroupPayout payout = groupPayoutRepo.findByGroupIdAndCycleNumber(cycle.getGroupId(), cycle.getCycleNumber()).orElse(null);

        long settled = countContributions(contributions, GroupContribution.Status.SETTLED);
        long pending = countContributions(contributions, GroupContribution.Status.PENDING);
        long processing = countContributions(contributions, GroupContribution.Status.PROCESSING);
        long failed = countContributions(contributions, GroupContribution.Status.FAILED);
        BigDecimal settledAmount = sumAmounts(contributions.stream()
                .filter(contribution -> contribution.getStatus() == GroupContribution.Status.SETTLED)
                .map(GroupContribution::getAmount)
                .collect(Collectors.toList()));

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("cycleId", cycle.getId());
        row.put("groupId", cycle.getGroupId());
        row.put("groupName", groupName(group));
        row.put("cycleNumber", cycle.getCycleNumber());
        row.put("contributionDate", cycle.getContributionDate() != null ? cycle.getContributionDate().toString() : null);
        row.put("contributionWindowEnd", cycle.getContributionWindowEnd() != null ? cycle.getContributionWindowEnd().toString() : null);
        row.put("payoutDate", cycle.getPayoutDate() != null ? cycle.getPayoutDate().toString() : null);
        row.put("cycleStatus", cycle.getStatus() != null ? cycle.getStatus().name() : null);
        row.put("expectedContributionCount", members.size());
        row.put("actualContributionCount", contributions.size());
        row.put("settledContributionCount", settled);
        row.put("pendingContributionCount", pending);
        row.put("processingContributionCount", processing);
        row.put("failedContributionCount", failed);
        row.put("settledContributionAmount", settledAmount);
        row.put("schedulerEligible", schedulerEligible(cycle));
        row.put("payout", toPayoutHealthRow(payout));
        row.put("healthStatus", cycleHealthStatus(cycle, members.size(), settled, failed, payout));
        row.put("recommendedAction", cycleRecommendedAction(cycle, members.size(), settled, failed, payout));
        row.put("contributions", contributions.stream()
                .sorted(Comparator.comparing(GroupContribution::getId))
                .map(this::toContributionHealthRow)
                .collect(Collectors.toList()));
        return row;
    }

    private long countContributions(List<GroupContribution> contributions, GroupContribution.Status status) {
        return contributions.stream().filter(contribution -> contribution.getStatus() == status).count();
    }

    private Map<String, Object> toContributionHealthRow(GroupContribution contribution) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("contributionId", contribution.getId());
        row.put("memberWalletId", contribution.getMemberWalletId());
        row.put("amount", contribution.getAmount());
        row.put("status", contribution.getStatus() != null ? contribution.getStatus().name() : null);
        row.put("idempotencyRef", contribution.getIdempotencyRef());
        row.put("providerRef", contribution.getProviderRef());
        row.put("createdAt", contribution.getCreatedAt() != null ? contribution.getCreatedAt().toString() : null);
        row.put("lastUpdatedAt", contribution.getLastUpdatedAt() != null ? contribution.getLastUpdatedAt().toString() : null);
        return row;
    }

    private Map<String, Object> toPayoutHealthRow(GroupPayout payout) {
        if (payout == null) {
            return null;
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("payoutId", payout.getId());
        row.put("receiverWalletId", payout.getReceiverWalletId());
        row.put("amount", payout.getAmount());
        row.put("status", payout.getStatus() != null ? payout.getStatus().name() : null);
        row.put("idempotencyRef", payout.getIdempotencyRef());
        row.put("providerRef", payout.getProviderRef());
        row.put("createdAt", payout.getCreatedAt() != null ? payout.getCreatedAt().toString() : null);
        row.put("lastUpdatedAt", payout.getLastUpdatedAt() != null ? payout.getLastUpdatedAt().toString() : null);
        return row;
    }

    private boolean schedulerEligible(GroupSavingsCycle cycle) {
        LocalDate today = LocalDate.now(AFRICA_LAGOS);
        return cycle.getContributionDate() != null
                && cycle.getContributionWindowEnd() != null
                && !today.isBefore(cycle.getContributionDate())
                && !today.isAfter(cycle.getContributionWindowEnd());
    }

    private String cycleHealthStatus(GroupSavingsCycle cycle, int expectedMembers, long settled, long failed, GroupPayout payout) {
        if (failed > 0 || (payout != null && payout.getStatus() == GroupPayout.Status.FAILED)) {
            return "ATTENTION";
        }
        if (cycle.getStatus() == GroupSavingsCycle.CycleStatus.EXPIRED) {
            return "EXPIRED";
        }
        if (cycle.getStatus() == GroupSavingsCycle.CycleStatus.PAID || (payout != null && payout.getStatus() == GroupPayout.Status.SETTLED)) {
            return "COMPLETED";
        }
        if (expectedMembers > 0 && settled < expectedMembers) {
            return "WAITING_CONTRIBUTIONS";
        }
        return "WAITING_PAYOUT";
    }

    private String cycleRecommendedAction(GroupSavingsCycle cycle, int expectedMembers, long settled, long failed, GroupPayout payout) {
        if (failed > 0 || (payout != null && payout.getStatus() == GroupPayout.Status.FAILED)) {
            return "Review downstream ledger state, then call retry-failed if debit/credit did not settle.";
        }
        if (cycle.getStatus() == GroupSavingsCycle.CycleStatus.EXPIRED) {
            return "Review expired cycle and decide whether to extend window or close manually.";
        }
        if (expectedMembers > 0 && settled < expectedMembers) {
            return "Monitor pending member contributions.";
        }
        if (payout == null || payout.getStatus() != GroupPayout.Status.SETTLED) {
            return "Monitor payout settlement.";
        }
        return "No action required.";
    }

    private Map<String, Object> toCycleRow(GroupSavingsCycle cycle) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("cycleNumber", cycle.getCycleNumber());
        row.put("contributionDate", cycle.getContributionDate() != null ? cycle.getContributionDate().toString() : null);
        row.put("contributionWindowEnd", cycle.getContributionWindowEnd() != null ? cycle.getContributionWindowEnd().toString() : null);
        row.put("payoutDate", cycle.getPayoutDate() != null ? cycle.getPayoutDate().toString() : null);
        row.put("status", cycle.getStatus() != null ? cycle.getStatus().name() : null);
        return row;
    }

    private boolean groupMatchesSearch(GroupSavingsData group, String normalizedSearch) {
        String haystack = String.join(" ",
                safe(group.getGroupSavingName()),
                safe(group.getInviteCode()),
                safe(group.getTransactionId()),
                safe(group.getTransactionIdLink()),
                safe(group.getEmailAddress()),
                safe(group.getPhoneNumber()),
                safe(group.getWalletId())
        ).toLowerCase(Locale.ENGLISH);
        return haystack.contains(normalizedSearch.toLowerCase(Locale.ENGLISH));
    }

    private String normalizedGroupStatus(GroupSavingsData group) {
        if (group == null) {
            return "UNKNOWN";
        }
        if ("1".equals(safe(group.getIsTrnsactionDeleted())) || "CLOSED".equalsIgnoreCase(safe(group.getIsTrnsactionDeletedDesc()))) {
            return "CLOSED";
        }
        String desc = safe(group.getTransactionStatusDesc()).toUpperCase(Locale.ENGLISH);
        if (desc.contains("PAUSE")) {
            return "PAUSED";
        }
        if (desc.contains("COMPLETE")) {
            return "COMPLETED";
        }
        String status = safe(group.getTransactionStatus());
        switch (status) {
            case "0":
                return "INITIATED";
            case "2":
                return "CREATED";
            case "3":
                return "ACTIVE";
            case "4":
                return "IN_PROGRESS";
            default:
                return desc.isEmpty() ? "UNKNOWN" : desc;
        }
    }

    private String ownerName(List<AddMembersModels> members, GroupSavingsData group) {
        return members.stream()
                .filter(member -> safe(member.getMemberEmailAddress()).equalsIgnoreCase(safe(group.getEmailAddress())))
                .map(AddMembersModels::getMemberName)
                .filter(name -> !safe(name).isEmpty())
                .findFirst()
                .orElse(safe(group.getEmailAddress()).isEmpty() ? null : group.getEmailAddress());
    }

    private String cycleLabel(GroupSavingsData group) {
        if (group.getContributionFrequency() == null) {
            return null;
        }
        String raw = group.getContributionFrequency().name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
        return raw.substring(0, 1).toUpperCase(Locale.ENGLISH) + raw.substring(1);
    }

    private String nextPayoutDate(Long groupId) {
        LocalDate today = LocalDate.now(AFRICA_LAGOS);
        return groupSavingsCycleRepo.findAll().stream()
                .filter(cycle -> Objects.equals(cycle.getGroupId(), groupId))
                .filter(cycle -> cycle.getPayoutDate() != null && !cycle.getPayoutDate().isBefore(today))
                .sorted(Comparator.comparing(GroupSavingsCycle::getPayoutDate))
                .map(cycle -> cycle.getPayoutDate().toString())
                .findFirst()
                .orElse(null);
    }

    private String normalizeFilter(String value) {
        String safe = safe(value);
        if (safe.isEmpty() || "ALL".equalsIgnoreCase(safe)) {
            return null;
        }
        return safe.toUpperCase(Locale.ENGLISH);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private List<GroupSavingsData> getGroups(Long groupId) {
        return groupSavingsDataRepo.findAll().stream()
                .filter(group -> groupId == null || Objects.equals(group.getId(), groupId))
                .sorted(Comparator.comparing(GroupSavingsData::getId).reversed())
                .collect(Collectors.toList());
    }

    private boolean withinRange(Instant instant, LocalDate start, LocalDate end) {
        if (instant == null) {
            return false;
        }
        LocalDate localDate = instant.atZone(AFRICA_LAGOS).toLocalDate();
        return !localDate.isBefore(start) && !localDate.isAfter(end);
    }

    private BigDecimal sumAmounts(Collection<BigDecimal> amounts) {
        return amounts.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Map<String, Object>> toGroupOptions(List<GroupSavingsData> groups) {
        return groups.stream().map(group -> {
            Map<String, Object> option = new LinkedHashMap<>();
            option.put("groupId", group.getId());
            option.put("groupName", group.getGroupSavingName());
            option.put("inviteCode", group.getInviteCode());
            option.put("transactionId", group.getTransactionId());
            option.put("status", group.getTransactionStatus());
            return option;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildMonitoringAlerts(
            Map<Long, GroupSavingsData> groupMap,
            List<GroupSavingsCycle> cycles,
            List<GroupContribution> contributions,
            List<GroupPayout> payouts
    ) {
        LocalDate today = LocalDate.now(AFRICA_LAGOS);
        List<Map<String, Object>> alerts = new ArrayList<>();

        payouts.stream()
                .filter(payout -> payout.getStatus() == GroupPayout.Status.FAILED || payout.getStatus() == GroupPayout.Status.PROCESSING)
                .sorted(Comparator.comparing(GroupPayout::getLastUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .forEach(payout -> alerts.add(alert(
                        payout.getStatus() == GroupPayout.Status.FAILED ? "HIGH" : "MEDIUM",
                        "PAYOUT",
                        payout.getStatus() == GroupPayout.Status.FAILED ? "FAILED" : "DELAYED",
                        "TX-" + payout.getGroupId() + "-" + payout.getCycleNumber(),
                        payout.getStatus() == GroupPayout.Status.FAILED
                                ? "Payout TX-" + payout.getGroupId() + "-" + payout.getCycleNumber() + " failed - retrigger required."
                                : "Payout TX-" + payout.getGroupId() + "-" + payout.getCycleNumber() + " is still processing.",
                        payout.getLastUpdatedAt() != null ? payout.getLastUpdatedAt() : payout.getCreatedAt(),
                        groupMap.get(payout.getGroupId())
                )));

        contributions.stream()
                .filter(contribution -> contribution.getStatus() == GroupContribution.Status.FAILED || contribution.getStatus() == GroupContribution.Status.PENDING)
                .sorted(Comparator.comparing(GroupContribution::getLastUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .forEach(contribution -> alerts.add(alert(
                        contribution.getStatus() == GroupContribution.Status.FAILED ? "HIGH" : "MEDIUM",
                        "CONTRIBUTION",
                        contribution.getStatus() == GroupContribution.Status.FAILED ? "FAILED" : "DELAYED",
                        contribution.getIdempotencyRef(),
                        contribution.getStatus() == GroupContribution.Status.FAILED
                                ? "Contribution " + contribution.getIdempotencyRef() + " failed and needs intervention."
                                : "Contribution " + contribution.getIdempotencyRef() + " is still pending.",
                        contribution.getLastUpdatedAt() != null ? contribution.getLastUpdatedAt() : contribution.getCreatedAt(),
                        groupMap.get(contribution.getGroupId())
                )));

        cycles.stream()
                .filter(cycle -> cycle.getStatus() == GroupSavingsCycle.CycleStatus.EXPIRED || (cycle.getPayoutDate() != null && !cycle.getPayoutDate().isBefore(today) && !cycle.getPayoutDate().isAfter(today.plusDays(3))))
                .sorted(Comparator.comparing(GroupSavingsCycle::getPayoutDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(5)
                .forEach(cycle -> alerts.add(alert(
                        cycle.getStatus() == GroupSavingsCycle.CycleStatus.EXPIRED ? "HIGH" : "LOW",
                        "CYCLE",
                        cycle.getStatus() == GroupSavingsCycle.CycleStatus.EXPIRED ? "MISSED" : "UPCOMING",
                        "GS-" + cycle.getGroupId() + "-" + cycle.getCycleNumber(),
                        cycle.getStatus() == GroupSavingsCycle.CycleStatus.EXPIRED
                                ? "Cycle " + cycle.getCycleNumber() + " for " + groupName(groupMap.get(cycle.getGroupId())) + " missed its contribution window."
                                : "Upcoming payout for cycle " + cycle.getCycleNumber() + " on " + cycle.getPayoutDate() + ".",
                        atStartOfDay(cycle.getPayoutDate() != null ? cycle.getPayoutDate() : cycle.getContributionWindowEnd()),
                        groupMap.get(cycle.getGroupId())
                )));

        return alerts.stream()
                .sorted(Comparator.comparing((Map<String, Object> alert) -> String.valueOf(alert.get("eventAt")), Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildSlotAlerts(
            Map<Long, GroupSavingsData> groupMap,
            List<GroupSavingsCycle> cycles,
            Map<Long, List<AddMembersModels>> memberMap,
            List<GroupContribution> contributions,
            List<GroupPayout> payouts,
            LocalDate today
    ) {
        List<Map<String, Object>> alerts = new ArrayList<>();

        cycles.stream()
                .filter(cycle -> cycle.getStatus() == GroupSavingsCycle.CycleStatus.EXPIRED)
                .sorted(Comparator.comparing(GroupSavingsCycle::getContributionWindowEnd, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .forEach(cycle -> {
                    AddMembersModels member = resolveMemberForSlot(memberMap.getOrDefault(cycle.getGroupId(), java.util.Collections.<AddMembersModels>emptyList()), cycle.getCycleNumber());
                    alerts.add(alert(
                            "HIGH",
                            "SLOT",
                            "MISSED",
                            "GS-" + cycle.getGroupId() + "-SLOT-" + cycle.getCycleNumber(),
                            "Slot " + cycle.getCycleNumber() + " missed contribution. Notify member and reschedule.",
                            atStartOfDay(cycle.getContributionWindowEnd()),
                            groupMap.get(cycle.getGroupId()),
                            member != null ? member.getMemberName() : null
                    ));
                });

        cycles.stream()
                .filter(cycle -> cycle.getPayoutDate() != null && !cycle.getPayoutDate().isBefore(today) && !cycle.getPayoutDate().isAfter(today.plusDays(3)))
                .sorted(Comparator.comparing(GroupSavingsCycle::getPayoutDate))
                .limit(5)
                .forEach(cycle -> {
                    AddMembersModels member = resolveMemberForSlot(memberMap.getOrDefault(cycle.getGroupId(), java.util.Collections.<AddMembersModels>emptyList()), cycle.getCycleNumber());
                    long daysAway = today.until(cycle.getPayoutDate()).getDays();
                    alerts.add(alert(
                            "LOW",
                            "PAYOUT",
                            "UPCOMING",
                            "GS-" + cycle.getGroupId() + "-SLOT-" + cycle.getCycleNumber(),
                            "Upcoming payout for Slot " + cycle.getCycleNumber() + " in " + daysAway + " day" + (daysAway == 1 ? "" : "s") + ".",
                            atStartOfDay(cycle.getPayoutDate()),
                            groupMap.get(cycle.getGroupId()),
                            member != null ? member.getMemberName() : null
                    ));
                });

        payouts.stream()
                .filter(payout -> payout.getStatus() == GroupPayout.Status.FAILED)
                .sorted(Comparator.comparing(GroupPayout::getLastUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .forEach(payout -> {
                    AddMembersModels member = resolveMemberForSlot(memberMap.getOrDefault(payout.getGroupId(), java.util.Collections.<AddMembersModels>emptyList()), payout.getCycleNumber());
                    alerts.add(alert(
                            "HIGH",
                            "PAYOUT",
                            "FAILED",
                            payout.getIdempotencyRef(),
                            "Payout for Slot " + payout.getCycleNumber() + " failed and should be retried.",
                            payout.getLastUpdatedAt() != null ? payout.getLastUpdatedAt() : payout.getCreatedAt(),
                            groupMap.get(payout.getGroupId()),
                            member != null ? member.getMemberName() : null
                    ));
                });

        contributions.stream()
                .filter(contribution -> contribution.getStatus() == GroupContribution.Status.FAILED)
                .sorted(Comparator.comparing(GroupContribution::getLastUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .forEach(contribution -> alerts.add(alert(
                        "HIGH",
                        "CONTRIBUTION",
                        "FAILED",
                        contribution.getIdempotencyRef(),
                        "Contribution " + contribution.getIdempotencyRef() + " failed. Follow up with the member.",
                        contribution.getLastUpdatedAt() != null ? contribution.getLastUpdatedAt() : contribution.getCreatedAt(),
                        groupMap.get(contribution.getGroupId()),
                        contribution.getMemberWalletId()
                )));

        return alerts.stream()
                .sorted(Comparator.comparing((Map<String, Object> alert) -> String.valueOf(alert.get("eventAt")), Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toPayoutHistoryRow(
            GroupPayout payout,
            Map<Long, GroupSavingsData> groupMap,
            Map<Long, List<AddMembersModels>> memberMap
    ) {
        AddMembersModels member = resolveMemberForSlot(memberMap.getOrDefault(payout.getGroupId(), java.util.Collections.<AddMembersModels>emptyList()), payout.getCycleNumber());
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("groupId", payout.getGroupId());
        row.put("groupName", groupName(groupMap.get(payout.getGroupId())));
        row.put("memberName", member != null ? member.getMemberName() : "Unassigned");
        row.put("slotNumber", payout.getCycleNumber());
        row.put("amount", payout.getAmount());
        row.put("date", payout.getLastUpdatedAt() != null ? payout.getLastUpdatedAt().toString() : payout.getCreatedAt().toString());
        row.put("status", normalizePayoutStatus(payout.getStatus()));
        row.put("reference", payout.getIdempotencyRef());
        return row;
    }

    private Map<String, Object> alert(
            String level,
            String category,
            String status,
            String reference,
            String message,
            Instant eventAt,
            GroupSavingsData group
    ) {
        return alert(level, category, status, reference, message, eventAt, group, null);
    }

    private Map<String, Object> alert(
            String level,
            String category,
            String status,
            String reference,
            String message,
            Instant eventAt,
            GroupSavingsData group,
            String memberName
    ) {
        Map<String, Object> alert = new LinkedHashMap<>();
        alert.put("level", level);
        alert.put("category", category);
        alert.put("status", status);
        alert.put("reference", reference);
        alert.put("message", message);
        alert.put("eventAt", eventAt != null ? eventAt.toString() : null);
        alert.put("groupId", group != null ? group.getId() : null);
        alert.put("groupName", groupName(group));
        alert.put("memberName", memberName);
        return alert;
    }

    private Instant atStartOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay(AFRICA_LAGOS).toInstant();
    }

    private List<AddMembersModels> readMembers(GroupSavingsData group) {
        try {
            if (group == null || group.getAddedMembersModels() == null || group.getAddedMembersModels().trim().isEmpty()) {
                return java.util.Collections.emptyList();
            }
            return objectMapper.readValue(group.getAddedMembersModels(), MEMBER_LIST_TYPE);
        } catch (Exception ex) {
            return java.util.Collections.emptyList();
        }
    }

    private AddMembersModels resolveMemberForSlot(List<AddMembersModels> members, Integer slot) {
        if (slot == null) {
            return null;
        }
        return members.stream().filter(member -> member.getSlot() == slot).findFirst().orElse(null);
    }

    private boolean isActiveGroup(GroupSavingsData group) {
        if (group == null || group.getTransactionStatus() == null) {
            return false;
        }
        String status = group.getTransactionStatus().toUpperCase(Locale.ENGLISH);
        return !status.contains("DELETE") && !status.contains("CANCEL");
    }

    private String normalizeSlotStatus(GroupSavingsCycle cycle, LocalDate today) {
        if (cycle.getStatus() == GroupSavingsCycle.CycleStatus.PAID) {
            return "COMPLETED";
        }
        if (cycle.getStatus() == GroupSavingsCycle.CycleStatus.EXPIRED) {
            return "MISSED";
        }
        if (cycle.getStatus() == GroupSavingsCycle.CycleStatus.IN_PROGRESS || cycle.getStatus() == GroupSavingsCycle.CycleStatus.AWAITING_PAYOUT) {
            return "IN_PROGRESS";
        }
        if (cycle.getPayoutDate() != null && !cycle.getPayoutDate().isBefore(today)) {
            return "UPCOMING";
        }
        return "UPCOMING";
    }

    private String normalizePayoutStatus(GroupPayout.Status status) {
        if (status == null) {
            return "UNKNOWN";
        }
        switch (status) {
            case SETTLED:
                return "PAID";
            case FAILED:
                return "FAILED";
            case PROCESSING:
                return "PROCESSING";
            case PENDING:
                return "PENDING";
            default:
                return "UNKNOWN";
        }
    }

    private String groupName(GroupSavingsData group) {
        return group != null ? group.getGroupSavingName() : null;
    }

    private Map<String, BucketAccumulator> initBuckets(PeriodBucket bucket, LocalDate start, LocalDate end) {
        Map<String, BucketAccumulator> buckets = new LinkedHashMap<>();
        LocalDate cursor = bucket.alignStart(start);
        while (!cursor.isAfter(end)) {
            LocalDate periodEnd = bucket.periodEnd(cursor);
            BucketAccumulator accumulator = new BucketAccumulator(cursor, periodEnd, bucket.label(cursor));
            buckets.put(bucket.keyFor(cursor), accumulator);
            cursor = bucket.next(cursor);
        }
        return buckets;
    }

    private enum PeriodBucket {
        DAILY,
        WEEKLY,
        MONTHLY;

        static PeriodBucket from(String value) {
            if (value == null || value.trim().isEmpty()) {
                return DAILY;
            }
            try {
                return PeriodBucket.valueOf(value.trim().toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException ex) {
                return DAILY;
            }
        }

        LocalDate defaultStart(LocalDate end) {
            switch (this) {
                case DAILY:
                    return end.minusDays(6);
                case WEEKLY:
                    return end.minusWeeks(7).with(java.time.DayOfWeek.MONDAY);
                case MONTHLY:
                    return end.minusMonths(5).withDayOfMonth(1);
                default:
                    return end.minusDays(6);
            }
        }

        LocalDate alignStart(LocalDate date) {
            switch (this) {
                case DAILY:
                    return date;
                case WEEKLY:
                    return date.with(java.time.DayOfWeek.MONDAY);
                case MONTHLY:
                    return date.withDayOfMonth(1);
                default:
                    return date;
            }
        }

        LocalDate periodEnd(LocalDate start) {
            switch (this) {
                case DAILY:
                    return start;
                case WEEKLY:
                    return start.plusDays(6);
                case MONTHLY:
                    return start.with(TemporalAdjusters.lastDayOfMonth());
                default:
                    return start;
            }
        }

        LocalDate next(LocalDate start) {
            switch (this) {
                case DAILY:
                    return start.plusDays(1);
                case WEEKLY:
                    return start.plusWeeks(1);
                case MONTHLY:
                    return start.plusMonths(1).withDayOfMonth(1);
                default:
                    return start.plusDays(1);
            }
        }

        String keyFor(LocalDate date) {
            LocalDate aligned = alignStart(date);
            return aligned.toString();
        }

        String label(LocalDate date) {
            LocalDate aligned = alignStart(date);
            switch (this) {
                case DAILY:
                    return aligned.format(DAILY_LABEL);
                case WEEKLY:
                    return aligned.format(WEEKLY_LABEL);
                case MONTHLY:
                    return aligned.format(MONTHLY_LABEL);
                default:
                    return aligned.toString();
            }
        }
    }

    private static final class BucketAccumulator {

        private final LocalDate start;
        private final LocalDate end;
        private final String label;
        private BigDecimal amount = BigDecimal.ZERO;
        private int count = 0;

        private BucketAccumulator(LocalDate start, LocalDate end, String label) {
            this.start = start;
            this.end = end;
            this.label = label;
        }

        private void add(BigDecimal value) {
            if (value != null) {
                amount = amount.add(value);
            }
            count++;
        }

        private LocalDate start() {
            return start;
        }

        private LocalDate end() {
            return end;
        }

        private String label() {
            return label;
        }

        private BigDecimal amount() {
            return amount;
        }

        private int count() {
            return count;
        }
    }
}
