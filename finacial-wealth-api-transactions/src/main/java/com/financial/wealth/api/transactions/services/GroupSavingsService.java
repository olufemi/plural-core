/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.services;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.financial.wealth.api.transactions.domain.CommissionCfg;
import com.financial.wealth.api.transactions.domain.ConfiguedMembersNumber;
import com.financial.wealth.api.transactions.domain.ConfiguredPayOutSlots;
import com.financial.wealth.api.transactions.domain.FinWealthServiceConfig;
import com.financial.wealth.api.transactions.domain.GroupSavingsData;
import com.financial.wealth.api.transactions.domain.ReceiverFailedTransInfo;
import com.financial.wealth.api.transactions.domain.RegWalletInfo;
import com.financial.wealth.api.transactions.domain.SettlementFailureLog;
import com.financial.wealth.api.transactions.models.AddMembers;
import com.financial.wealth.api.transactions.models.AddMembersModels;
import com.financial.wealth.api.transactions.models.ApiResponseModel;
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.GroupSavingConf;
import com.financial.wealth.api.transactions.models.InitiateGroupSavings;
import com.financial.wealth.api.transactions.models.ReByEmailAddress;
import com.financial.wealth.api.transactions.models.SavedGroupDetails;
import com.financial.wealth.api.transactions.repo.CommissionCfgRepo;
import com.financial.wealth.api.transactions.repo.ConfiguedMembersNumberRepo;
import com.financial.wealth.api.transactions.repo.ConfiguredPayOutSlotsRepo;
import com.financial.wealth.api.transactions.repo.FinWealthServiceConfigRepo;
import com.financial.wealth.api.transactions.repo.GroupSavingsDataRepo;
import com.financial.wealth.api.transactions.repo.ReceiverFailedTransInfoRepo;
import com.financial.wealth.api.transactions.repo.RegWalletInfoRepository;
import com.financial.wealth.api.transactions.repo.SettlementFailureLogRepo;
import com.financial.wealth.api.transactions.utils.DecodedJWTToken;
import com.financial.wealth.api.transactions.utils.GlobalMethods;
import com.financial.wealth.api.transactions.utils.UttilityMethods;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.validator.GenericValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.wealth.api.transactions.domain.GetSwapSlotDetailsResponse;
import com.financial.wealth.api.transactions.domain.SwapSlotDetails;
import com.financial.wealth.api.transactions.enumm.ContributionFrequency;
import com.financial.wealth.api.transactions.enumm.PayoutPolicy;
import com.financial.wealth.api.transactions.models.AcceptDeclineSwapSlotReq;
import com.financial.wealth.api.transactions.models.AddMembersModelsOthers;
import com.financial.wealth.api.transactions.models.AddedMembersFE;
import com.financial.wealth.api.transactions.models.CheckIfMerIdExists;
import com.financial.wealth.api.transactions.models.GetMemBerDe;
import com.financial.wealth.api.transactions.models.GroupSavingActivation;
import com.financial.wealth.api.transactions.models.GroupSavingsDataModel;
import com.financial.wealth.api.transactions.models.InitiateGroupSavingsV2;
import com.financial.wealth.api.transactions.models.JoinGroupRequest;
import com.financial.wealth.api.transactions.models.LeaveGroupRequest;
import com.financial.wealth.api.transactions.models.ReByInvitationCode;
import com.financial.wealth.api.transactions.models.SavedGroupSlotDetails;
import com.financial.wealth.api.transactions.models.SwapSlot;
import com.financial.wealth.api.transactions.models.SwapSlotReq;
import com.financial.wealth.api.transactions.models.ValidateReqReq;
import com.financial.wealth.api.transactions.repo.SwapSlotDetailsRepo;
import com.financial.wealth.api.transactions.services.grp.sav.fulfil.GroupSavingsCycle;
import com.financial.wealth.api.transactions.services.grp.sav.fulfil.GroupSavingsCycleRepo;
import com.financial.wealth.api.transactions.services.utils.ScheduleUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.text.SimpleDateFormat;

import java.util.Locale;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author olufemioshin
 */
@Service
public class GroupSavingsService {
    
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    private final GroupSavingsDataRepo groupSavingsDataRepo;
    private final UttilityMethods utilMeth;
    private final ReceiverFailedTransInfoRepo receiverFailedTransInfoRepo;
    private final SettlementFailureLogRepo settlementFailureLogRepo;
    private final Logger log = LoggerFactory.getLogger(GroupSavingsService.class);
    private final CommissionCfgRepo commissionCfgRepo;
    private final FinWealthServiceConfigRepo finWealthServiceConfigRepo;
    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
    private final RegWalletInfoRepository regWalletInfoRepository;
    @Value("${fin.wealth.otp.encrypt.key}")
    private String encryptionKey;
    private final ConfiguedMembersNumberRepo configuedMembersNumberRepo;
    private final ConfiguredPayOutSlotsRepo configuredPayOutSlotsRepo;
    private final SwapSlotDetailsRepo swapSlotDetailsRepo;
    @Value("${spring.profiles.active}")
    private String environment;
    private final GroupSavingsScheduleEngine groupSavingsScheduleEngine;
    private final GroupSavingsCycleRepo groupSavingsCycleRepo;
    
    public GroupSavingsService(GroupSavingsDataRepo groupSavingsDataRepo,
            UttilityMethods utilMeth, ReceiverFailedTransInfoRepo receiverFailedTransInfoRepo,
            SettlementFailureLogRepo settlementFailureLogRepo,
            CommissionCfgRepo commissionCfgRepo,
            FinWealthServiceConfigRepo finWealthServiceConfigRepo,
            RegWalletInfoRepository regWalletInfoRepository,
            ConfiguedMembersNumberRepo configuedMembersNumberRepo,
            ConfiguredPayOutSlotsRepo configuredPayOutSlotsRepo, SwapSlotDetailsRepo swapSlotDetailsRepo,
            GroupSavingsScheduleEngine groupSavingsScheduleEngine,
            GroupSavingsCycleRepo groupSavingsCycleRepo) {
        
        this.groupSavingsDataRepo = groupSavingsDataRepo;
        this.utilMeth = utilMeth;
        this.receiverFailedTransInfoRepo = receiverFailedTransInfoRepo;
        this.settlementFailureLogRepo = settlementFailureLogRepo;
        this.commissionCfgRepo = commissionCfgRepo;
        this.finWealthServiceConfigRepo = finWealthServiceConfigRepo;
        this.regWalletInfoRepository = regWalletInfoRepository;
        this.configuedMembersNumberRepo = configuedMembersNumberRepo;
        this.configuredPayOutSlotsRepo = configuredPayOutSlotsRepo;
        this.swapSlotDetailsRepo = swapSlotDetailsRepo;
        this.groupSavingsScheduleEngine = groupSavingsScheduleEngine;
        this.groupSavingsCycleRepo = groupSavingsCycleRepo;
    }
    
    public BaseResponse deleteGroupSaving(GroupSavingConf rq, String channel, String auth) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured, please try again.";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String emailAddress = getDecoded.emailAddress;
            GroupSavingsData updateRecord = null;
            // System.out.println("email from jwt" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + emailAddress);

            if (!rq.getEmailAddress().equals(emailAddress)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Suspected fraud!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Suspected fraud!!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            List<RegWalletInfo> senderWalletdetails = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);
            if (!senderWalletdetails.get(0).isActivation()) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Customer has not created PIN!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setDescription("Customer has not created PIN!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            String encyrptedPin = utilMeth.encyrpt(String.valueOf(rq.getPin()), encryptionKey);
            String pin = senderWalletdetails.get(0).getPersonId();
            if (!encyrptedPin.equals(pin)) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Invalid PIN!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setDescription("Invalid PIN!");
                responseModel.setStatusCode(statusCode);
                
                return responseModel;
                
            }
            
            List<GroupSavingsData> chkPendId = groupSavingsDataRepo.findByInviteCode(rq.getInvitationCodeReqId());
            boolean itsId = false;
            boolean itsIdLink = false;
            List<GroupSavingsData> chkPendIdLink = groupSavingsDataRepo.findByTransactionIdLink(rq.getInvitationCodeReqId());
            
            if (chkPendId.size() > 0) {
                
                if (chkPendId.get(0).getTransactionStatus().equals("4")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction is still active, contributions on going!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction is still active, contributions on going!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
                itsId = true;
                
                updateRecord = groupSavingsDataRepo.findByInviteCodeDe(rq.getInvitationCodeReqId());
                updateRecord.setLastModifiedDate(Instant.now());
                
                updateRecord.setIsTrnsactionDeleted("1");
                updateRecord.setIsTrnsactionDeletedDesc("Deleted");
                
            }
            
            System.out.println("itsId" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + itsId);
            
            if (chkPendIdLink.size() > 0) {
                if (chkPendIdLink.get(0).getTransactionStatus().equals("4")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction is still active, contributions on going!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction is still active, contributions on going!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                itsIdLink = true;
                
                updateRecord = groupSavingsDataRepo.findByTransactionIdLinkDe(rq.getInvitationCodeReqId());
                updateRecord.setLastModifiedDate(Instant.now());
                updateRecord.setIsTrnsactionDeleted("1");
                updateRecord.setIsTrnsactionDeletedDesc("Deleted");
                
            }
            
            System.out.println("itsIdLink" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + itsIdLink);
            
            if (itsId == false && itsIdLink == false) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "The transaction request-id is invalid!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("The transaction request-id is invalid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            groupSavingsDataRepo.save(updateRecord);
            
            responseModel.setDescription("Transaction Created.");
            responseModel.setStatusCode(200);
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
    }
    
    public ApiResponseModel configuedMembersNumberData() {
        ApiResponseModel response = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "an Error occured, please try again";
        try {
            List<ConfiguedMembersNumber> sData = configuedMembersNumberRepo.findAll();
            String sDataJson = cleanText(new Gson().toJson(sData));
            
            statusCode = 400;
            statusMessage = "Invalid Parameter";
            if (!sData.isEmpty()) {
                statusCode = 200;
                statusMessage = "Successful";
                List<ConfiguedMembersNumber> ssData = new Gson().fromJson(sDataJson, new TypeToken<List<ConfiguedMembersNumber>>() {
                }.getType());
                response.setData(ssData);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        response.setStatusCode(statusCode);
        response.setDescription(statusMessage);
        return response;
    }
    
    public int[] StringToIntArray(String[] stringArray) {
        
        System.out.println("StringToIntArray :::::::: " + stringArray);

        //  String[] stringArray = {"1", "2", "3", "4", "5"};
        int[] intArray = Arrays.stream(stringArray)
                .mapToInt(Integer::parseInt)
                .toArray();

        //System.out.println(Arrays.toString(intArray)); // Output: [1, 2, 3, 4, 5]
        System.out.println("StringToIntArray :::::::: " + Arrays.toString(intArray));
        
        return intArray;
        
    }
    
    public static String removeNumberAndConvertToString1(int[] array, int numberToRemove) {
        System.out.println("removeNumberAndConvertToString :::::::: ");
        
        System.out.println("numberToRemove :::::::: " + numberToRemove);
        System.out.println("array :::::::: " + array);
        
        return Arrays.toString(
                Arrays.stream(array)
                        .filter(n -> n != numberToRemove)
                        .toArray()
        );
        
    }
    
    public static int[] removeNumberAndConvertToString(int[] array, int numberToRemove) {
        return Arrays.stream(array)
                .filter(n -> n != numberToRemove)
                .toArray();
    }
    
    public int[] returnAraryumberOfSlots(int number) {
        
        int[] array = new int[number];
        for (int i = 0; i < number; i++) {
            array[i] = i + 1;
        }
        System.out.println("returnAraryumberOfSlots :::::::: " + array);
        
        return array;
        
    }
    
    private LocalDate parseIso(String s) {
        return LocalDate.parse(s, ISO);
    }
    
    private void validateFrequencyPayload(InitiateGroupSavingsV2 rq) {
        ContributionFrequency freq = ContributionFrequency.valueOf(rq.getContributionFrequency().toUpperCase());
        if (freq == ContributionFrequency.WEEKLY || freq == ContributionFrequency.BIWEEKLY) {
            if (rq.getContributionDayOfWeek() == null || rq.getContributionDayOfWeek() < 1 || rq.getContributionDayOfWeek() > 7) {
                throw new IllegalArgumentException("contributionDayOfWeek must be 1..7 for WEEKLY/BIWEEKLY");
            }
        } else {
            if (rq.getContributionDayOfMonth() == null || rq.getContributionDayOfMonth() < 1 || rq.getContributionDayOfMonth() > 28) {
                throw new IllegalArgumentException("contributionDayOfMonth must be 1..28 for MONTHLY/QUARTERLY");
            }
        }
        // payout policy
        PayoutPolicy.valueOf(rq.getPayoutPolicy().toUpperCase());
    }
    
    private String slotsCsv(int numberOfMembers, int reservedAdminSlot) {
        // 1..N excluding admin reserved slot
        List<String> slots = new ArrayList<>();
        for (int i = 1; i <= numberOfMembers; i++) {
            if (i != reservedAdminSlot) {
                slots.add(String.valueOf(i));
            }
        }
        return String.join(",", slots);
    }
    
    public ApiResponseModel initiateGroupSavingsV2(InitiateGroupSavingsV2 rq, String channel, String auth) {
        ApiResponseModel responseModel = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;

            // ... keep your auth + wallet checks exactly as before ...
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            BigDecimal fees = BigDecimal.ZERO;
            // BigDecimal transAmount = BigDecimal.ZERO;
            int number = rq.getSelectedSlot();
            
            List<RegWalletInfo> wallDe = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);
            if (!wallDe.get(0).isActivation()) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Customer has not created PIN!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setDescription("Customer has not created PIN!");
                responseModel.setStatusCode(60);
                
                return responseModel;
            }
            
            String transactionId = "#" + String.valueOf(GlobalMethods.generateTransactionId());
            
            String inviteCode = utilMeth.generateReferralCode("group-savings-generate-invite-code");
            
            String transactionL = utilMeth.getSETTING_KEY_G_INVITE_CODE_URL() + inviteCode;
            
            String inviteCodeLink = "<a href='transactionLink:" + transactionL + "'>" + "<b>" + transactionL + "<b>" + "</a>";

            //String payOutDateOfTheMonth = rq.getPayOutDateOfTheMonth();

            /*ApiResponseModel getIfPayDateIsCorrect = checkIfDateIsAfterRequiredWorkingDays(payOutDateOfTheMonth);

            if (getIfPayDateIsCorrect.getStatusCode() != 200) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        getIfPayDateIsCorrect.getDescription());
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription(getIfPayDateIsCorrect.getDescription());
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }*/
            //validate number of members
            String convertMems = String.valueOf(rq.getNumberOfMembers());
            
            if (!utilMeth.getSETTING_KEY_G_SAVINGS_MEM_LIST(convertMems)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Request No of Members is not reconized!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Request No of Members is not reconized!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            System.out.println(" rq.getEmailAddress()" + "  :::::::::::::::::::::   " + rq.getEmailAddress());
            System.out.println("getDecoded.emailAddress" + "  :::::::::::::::::::::   " + getDecoded.emailAddress);
            
            if (!getDecoded.emailAddress.equals(rq.getEmailAddress())) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Invalid User, Suspected Fraud!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Invalid User!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            List<FinWealthServiceConfig> getConfList = finWealthServiceConfigRepo.findByServiceTypeEnable("groupsavings");
            List<CommissionCfg> pullData = findAllByTransactionType("groupsavings");
            if (pullData.size() > 0) {
                if (pullData.isEmpty()) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "Transaction Type does not exist!");
                    settlementFailureLogRepo.save(conWall);
                    
                    responseModel.setStatusCode(400);
                    responseModel.setDescription("Transaction Type does not exist!");
                    return responseModel;
                    
                }
            } else {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Transaction Type does not exist!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setStatusCode(400);
                responseModel.setDescription("Transaction Type does not exist!");
                return responseModel;
            }
            
            if (getConfList.size() <= 0) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Service type not configured!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Service type not configured!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
                
            }
            
            if (!getConfList.get(0).isEnabled()) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Service type not configured!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Service type not configured!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
                
            }
            Optional<FinWealthServiceConfig> getKul = finWealthServiceConfigRepo.findAllByServiceType("groupsavings");
            
            List<GroupSavingsData> checkDe = groupSavingsDataRepo.findByEmailAddress(getDecoded.emailAddress);
            
            if (checkDe.size() > 0) {
                //check through to see that customer can have active transaction above configured number of active

                for (GroupSavingsData checkDeInt : checkDe) {
                    
                    String gName = checkDeInt.getGroupSavingName().toUpperCase();
                    if (checkDeInt.getTransactionStatus().equals("0")) {
                        String isDeleted = checkDeInt.getTransactionStatus() == null ? "0" : checkDeInt.getTransactionStatus();
                        
                        if (!isDeleted.equals("1")) {
                            SettlementFailureLog conWall = new SettlementFailureLog("", "",
                                    "You have an existing group( " + gName + " ) initiated but not yet created!");
                            settlementFailureLogRepo.save(conWall);
                            
                            GroupSavingsDataModel doSave = new GroupSavingsDataModel();
                            
                            doSave.setAllowPublicToJoin(checkDeInt.getAllowPublicToJoin());
                            doSave.setTransactionDate(formDate(checkDeInt.getCreatedDate()));
                            
                            doSave.setGroupSavingDescription(checkDeInt.getGroupSavingDescription());
                            doSave.setGroupSavingName(checkDeInt.getGroupSavingName());
                            doSave.setNumberOfMembers(String.valueOf(checkDeInt.getNumberOfMembers()));
                            doSave.setPayOutDateOfTheMonth(checkDeInt.getPayOutDateOfTheMonth());
                            doSave.setAdminPayOutSlot(checkDeInt.getAdminPayOutSlot());
                            doSave.setTransactionId(checkDeInt.getTransactionId());
                            doSave.setTransactionStatus(checkDeInt.getTransactionStatus());
                            doSave.setTransactionStatusDesc(checkDeInt.getTransactionStatusDesc());
                            doSave.setInviteCode(checkDeInt.getInviteCode());
                            doSave.setTransactionIdLink(checkDeInt.getTransactionIdLink());
                            doSave.setIsTrnsactionDeleted(checkDeInt.getIsTrnsactionDeleted());
                            doSave.setAddedMembersModels(checkDeInt.getAddedMembersModels());
                            doSave.setWalletId(checkDeInt.getWalletId());
                            doSave.setPhoneNumber(checkDeInt.getPhoneNumber());
                            doSave.setEmailAddress(checkDeInt.getEmailAddress());
                            doSave.setGroupSavingAmount(checkDeInt.getGroupSavingAmount());
                            doSave.setGroupSavingFinalAmount(checkDeInt.getGroupSavingFinalAmount());
                            // String[] array = checkDeInt.getAvailablePayOutSlot().split(",");
                            doSave.setAvailablePayOutSlot(checkDeInt.getAvailablePayOutSlot());
                            
                            responseModel.setData(doSave);
                            
                            responseModel.setStatusCode(90);
                            responseModel.setDescription("You have an existing group( " + gName + " ) initiated but not yet created!");
                            //return responseModel;
                            GroupSavingsData checkDeUp = groupSavingsDataRepo.findByInviteCodeDe(checkDeInt.getInviteCode());
                            checkDeUp.setIsTrnsactionDeleted("1");
                            checkDeUp.setLastModifiedDate(Instant.now());
                            groupSavingsDataRepo.save(checkDeUp);
                        }
                        
                    }
                    
                }
                
            }
            // === NEW: frequency validation ===
            rq.setContributionDayOfMonth(1);
            rq.setContributionDayOfWeek(1);
            validateFrequencyPayload(rq);
            ContributionFrequency freq = ContributionFrequency.valueOf(rq.getContributionFrequency().toUpperCase());
            if (!PayoutPolicy.isValid(rq.getPayoutPolicy().toUpperCase())) {
                // return 400/Bad Request in your controller or throw a custom exception
                throw new IllegalArgumentException(
                        "payoutPolicy must be one of: EACH_CYCLE, PERIOD_END, AFTER_ALL_CONTRIBUTIONS"
                );
            }
            PayoutPolicy payoutPolicy = PayoutPolicy.valueOf(rq.getPayoutPolicy().toUpperCase());
            //LocalDate start = parseIso(rq.getStartDate());
            //LocalDate start = parseIso(new Date().toString());

            // Back-compat: if MONTHLY and legacy payOutDateOfTheMonth provided like "dd/MM/yyyy"
            /*if (rq.getPayOutDateOfTheMonth() != null && !rq.getPayOutDateOfTheMonth().trim().isEmpty()
                    && freq == ContributionFrequency.MONTHLY) {
                ApiResponseModel payCheck = checkIfDateIsAfterRequiredWorkingDays(rq.getPayOutDateOfTheMonth());
                if (payCheck.getStatusCode() != 200) {
                    // ... your existing failure logging & return ...
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            payCheck.getDescription());
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription(payCheck.getDescription());
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
            }*/
            // Normalize start against chosen anchors
            /*Integer dow = rq.getContributionDayOfWeek();
            Integer dom = rq.getContributionDayOfMonth();
            LocalDate normalizedStart = ScheduleUtils.normalizeStart(start, freq, dow, dom);*/
            // Build cycles = numberOfMembers (slot model: one payout per cycle)
            /*int cycles = rq.getNumberOfMembers();

            List<LocalDate> contributionDates = ScheduleUtils.nextContributionDates(normalizedStart, cycles, freq);*/
            // Optional first payout override
            /* List<LocalDate> payoutDates = ScheduleUtils.payoutDates(contributionDates, payoutPolicy, freq);
            if (rq.getFirstPayoutDate() != null && !rq.getFirstPayoutDate().trim().isEmpty()) {
                payoutDates.set(0, parseIso(rq.getFirstPayoutDate()));
            }*/
            List<GroupSavingsScheduleEngine.Cycle> plan = groupSavingsScheduleEngine.buildSchedule(freq, rq.getNumberOfMembers(), payoutPolicy);

            // === Save ===
            GroupSavingsData doSave = new GroupSavingsData();
            doSave.setAllowPublicToJoin(rq.isAllowPublicToJoin() ? "1" : "0");
            BigDecimal transAmount = new BigDecimal(rq.getGroupSavingAmount());
            doSave.setGroupSavingAmount(transAmount);
            doSave.setGroupSavingFinalAmount(transAmount); // will adjust with fees below
            doSave.setCreatedDate(Instant.now());
            doSave.setGroupSavingDescription(rq.getGroupSavingDescription());
            doSave.setGroupSavingName(rq.getGroupSavingName());
            doSave.setNumberOfMembers(String.valueOf(rq.getNumberOfMembers()));
            doSave.setAdminPayOutSlot(rq.getSelectedSlot());
            doSave.setTransactionId("#" + String.valueOf(GlobalMethods.generateTransactionId()));
            doSave.setTransactionStatus("0");
            doSave.setTransactionStatusDesc("Initiated");
            doSave.setIsTrnsactionDeleted("0");
            doSave.setIsTrnsactionDeletedDesc("Not deleted");
            
            doSave.setInviteCode(inviteCode);
            doSave.setTransactionIdLink(inviteCodeLink);
            
            doSave.setWalletId(wallDe.get(0).getWalletId());
            doSave.setPhoneNumber(wallDe.get(0).getPhoneNumber());
            doSave.setEmailAddress(wallDe.get(0).getEmail());
            doSave.setCycleNumber(rq.getNumberOfMembers());
            doSave.setContributionDate(plan.get(rq.getNumberOfMembers() - 1).contributionDate.toString());
            doSave.setContributionWindowEnd(plan.get(rq.getNumberOfMembers() - 1).contributionWindowEnd.toString());
            doSave.setPayoutDate(plan.get(rq.getNumberOfMembers() - 1).payoutDate.toString());
            doSave.setPayOutDateOfTheMonth(plan.get(rq.getNumberOfMembers() - 1).payoutDate.toString());

            /*
            private int cycleNumber;
    private LocalDate contributionDate;       // due/anchor day for the cycle
    private LocalDate contributionWindowEnd;  // last acceptable day to pay for that cycle
    private LocalDate payoutDate;  
             */
            // NEW fields
            doSave.setContributionFrequency(freq);
            doSave.setPayoutPolicy(payoutPolicy);
            // doSave.setStartDate(normalizedStart);
            //doSave.setContributionDayOfWeek(dow);
            // doSave.setContributionDayOfMonth(dom);
            /*if (rq.getFirstPayoutDate() != null && !rq.getFirstPayoutDate().isEmpty()) {
                doSave.setFirstPayoutDate(parseIso(rq.getFirstPayoutDate()));
            }*/

            // Slots (excluding admin's selected slot)
            // doSave.setAvailablePayOutSlotCsv(slotsCsv(rq.getNumberOfMembers(), rq.getSelectedSlot()));
            int[] arrayT = returnAraryumberOfSlots(rq.getNumberOfMembers());
            doSave.setAvailablePayOutSlot(arrayT);
            //groupSavingsDataRepo

            boolean found = false;
            int target = rq.getSelectedSlot();
            for (int n : arrayT) {
                if (n == target) {
                    found = true;
                    break;
                }
            }
            
            if (found == true) {
                int[] arrayT2 = doSave.getAvailablePayOutSlot();
                int[] availablePayOutSlot = removeNumberAndConvertToString(arrayT2, rq.getSelectedSlot());
                doSave.setAvailablePayOutSlot(availablePayOutSlot);
                doSave.setAdminPayOutSlot(rq.getSelectedSlot());
                
            }

            // ===== Fees (same logic you already have) =====
            // getKul, pullData, compute fees -> update groupSavingFinalAmount
            // (keep your existing code; omitted here for brevity)
            for (CommissionCfg partData : pullData) {
                if (getKul.get().getServiceType().trim().equals(partData.getTransType())) {
                    
                    if (betweenTransBand(new BigDecimal(rq.getGroupSavingAmount()), new BigDecimal(partData.getAmountMin()), new BigDecimal(partData.getAmountMax())) == true) {
                        if (partData.getHasPercent().equals("1")) {

                            //compute the fees
                            //1.8% + 100 (convenience fee)
                            System.out.println(" rq.getGroupSavingAmount()" + "  :::::::::::::::::::::   " + rq.getGroupSavingAmount());
                            System.out.println(" pullData.get(0).getCommPercent()" + "  :::::::::::::::::::::   " + partData.getCommPercent());
                            //int conv = Integer.valueOf(pullData.get(0).getCommPercent().toString());

                            BigDecimal pFees = percentage(new BigDecimal(rq.getGroupSavingAmount()), partData.getCommPercent());
                            System.out.println(" pFees before added flatCharges" + "  :::::::::::::::::::::   " + pFees);
                            
                            fees = pFees.add(partData.getCharges());
                            doSave.setGroupSavingAmount(transAmount);
                            doSave.setGroupSavingFinalAmount(transAmount.add(fees));
                            
                        } else {
                            
                            doSave.setGroupSavingAmount(transAmount);
                            doSave.setGroupSavingFinalAmount(transAmount.add(fees));
                            
                        }
                    }
                }
            }
            
            groupSavingsDataRepo.save(doSave);

            // Optional: return preview schedule (first few dates) to client
            Map<String, Object> preview = new HashMap<>();
            // preview.put("contributionDates", contributionDates.stream().limit(4).map(ISO::format).collect(joining(", ")));
            //preview.put("payoutDates", payoutDates.stream().limit(4).map(ISO::format).collect(joining(", ")));

            responseModel.setDescription("Kindly confirm Group Creation.");
            responseModel.setStatusCode(200);
            responseModel.setData(doSave); // or wrap with a response model including the preview
            return responseModel;
            
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            ex.printStackTrace();
            return responseModel;
        }
    }
    
    public ApiResponseModel initiateGroupSavings(InitiateGroupSavings rq, String channel, String auth) {
        ApiResponseModel responseModel = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            BigDecimal fees = BigDecimal.ZERO;
            BigDecimal transAmount = BigDecimal.ZERO;
            int number = rq.getSelectedSlot();
            
            List<RegWalletInfo> wallDe = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);
            if (!wallDe.get(0).isActivation()) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Customer has not created PIN!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setDescription("Customer has not created PIN!");
                responseModel.setStatusCode(60);
                
                return responseModel;
            }
            
            String transactionId = "#" + String.valueOf(GlobalMethods.generateTransactionId());
            
            String inviteCode = utilMeth.generateReferralCode("group-savings-generate-invite-code");
            
            String transactionL = utilMeth.getSETTING_KEY_G_INVITE_CODE_URL() + inviteCode;
            
            String inviteCodeLink = "<a href='transactionLink:" + transactionL + "'>" + "<b>" + transactionL + "<b>" + "</a>";
            
            String payOutDateOfTheMonth = rq.getPayOutDateOfTheMonth();
            
            ApiResponseModel getIfPayDateIsCorrect = checkIfDateIsAfterRequiredWorkingDays(payOutDateOfTheMonth);
            
            if (getIfPayDateIsCorrect.getStatusCode() != 200) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        getIfPayDateIsCorrect.getDescription());
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription(getIfPayDateIsCorrect.getDescription());
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            //validate number of members
            String convertMems = String.valueOf(rq.getNumberOfMembers());
            
            if (!utilMeth.getSETTING_KEY_G_SAVINGS_MEM_LIST(convertMems)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Request No of Members is not reconized!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Request No of Members is not reconized!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            //validate number of slots
            /*String convertSlots = String.valueOf(rq.getPayOutSlot());
            if (convertSlots.equals(convertMems)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Request Pay Slot is invalid!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Request Pay Slot is invalid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }*/

 /* if (!utilMeth.getSETTING_KEY_G_SAVINGS_PAY_SLOT_LIST(convertSlots)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Request Pay Slot is not reconized!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Request Pay Slot is not reconized!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }*/
 /*if (!convertSlots.equals("1")) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Admin slot must be 1!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Admin slot must be 1!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }*/
            System.out.println(" rq.getEmailAddress()" + "  :::::::::::::::::::::   " + rq.getEmailAddress());
            System.out.println("getDecoded.emailAddress" + "  :::::::::::::::::::::   " + getDecoded.emailAddress);
            
            if (!getDecoded.emailAddress.equals(rq.getEmailAddress())) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Invalid User, Suspected Fraud!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Invalid User!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            List<FinWealthServiceConfig> getConfList = finWealthServiceConfigRepo.findByServiceTypeEnable("groupsavings");
            List<CommissionCfg> pullData = findAllByTransactionType("groupsavings");
            if (pullData.size() > 0) {
                if (pullData.isEmpty()) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "Transaction Type does not exist!");
                    settlementFailureLogRepo.save(conWall);
                    
                    responseModel.setStatusCode(400);
                    responseModel.setDescription("Transaction Type does not exist!");
                    return responseModel;
                    
                }
            } else {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Transaction Type does not exist!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setStatusCode(400);
                responseModel.setDescription("Transaction Type does not exist!");
                return responseModel;
            }
            
            if (getConfList.size() <= 0) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Service type not configured!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Service type not configured!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
                
            }
            
            if (!getConfList.get(0).isEnabled()) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Service type not configured!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Service type not configured!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
                
            }
            Optional<FinWealthServiceConfig> getKul = finWealthServiceConfigRepo.findAllByServiceType("groupsavings");
            
            List<GroupSavingsData> checkDe = groupSavingsDataRepo.findByEmailAddress(getDecoded.emailAddress);
            
            if (checkDe.size() > 0) {
                //check through to see that customer can have active transaction above configured number of active

                for (GroupSavingsData checkDeInt : checkDe) {
                    
                    String gName = checkDeInt.getGroupSavingName().toUpperCase();
                    
                    if (checkDeInt.getTransactionStatus().equals("0")) {
                        SettlementFailureLog conWall = new SettlementFailureLog("", "",
                                "You have an existing group(" + gName + ") initiated but not yet created!");
                        settlementFailureLogRepo.save(conWall);
                        
                        GroupSavingsDataModel doSave = new GroupSavingsDataModel();
                        
                        doSave.setAllowPublicToJoin(checkDeInt.getAllowPublicToJoin());
                        doSave.setTransactionDate(formDate(checkDeInt.getCreatedDate()));
                        
                        doSave.setGroupSavingDescription(checkDeInt.getGroupSavingDescription());
                        doSave.setGroupSavingName(checkDeInt.getGroupSavingName());
                        doSave.setNumberOfMembers(String.valueOf(checkDeInt.getNumberOfMembers()));
                        doSave.setPayOutDateOfTheMonth(checkDeInt.getPayOutDateOfTheMonth());
                        doSave.setAdminPayOutSlot(checkDeInt.getAdminPayOutSlot());
                        doSave.setTransactionId(checkDeInt.getTransactionId());
                        doSave.setTransactionStatus(checkDeInt.getTransactionStatus());
                        doSave.setTransactionStatusDesc(checkDeInt.getTransactionStatusDesc());
                        doSave.setInviteCode(checkDeInt.getInviteCode());
                        doSave.setTransactionIdLink(checkDeInt.getTransactionIdLink());
                        doSave.setIsTrnsactionDeleted(checkDeInt.getIsTrnsactionDeleted());
                        doSave.setAddedMembersModels(checkDeInt.getAddedMembersModels());
                        doSave.setWalletId(checkDeInt.getWalletId());
                        doSave.setPhoneNumber(checkDeInt.getPhoneNumber());
                        doSave.setEmailAddress(checkDeInt.getEmailAddress());
                        doSave.setGroupSavingAmount(checkDeInt.getGroupSavingAmount());
                        doSave.setGroupSavingFinalAmount(checkDeInt.getGroupSavingFinalAmount());
                        // String[] array = checkDeInt.getAvailablePayOutSlot().split(",");
                        doSave.setAvailablePayOutSlot(checkDeInt.getAvailablePayOutSlot());
                        
                        responseModel.setData(doSave);
                        
                        responseModel.setStatusCode(90);
                        responseModel.setDescription("You have an existing group(" + gName + ") initiated but not yet created!");
                        return responseModel;
                    }
                    
                }
                
            }
            
            GroupSavingsData doSave = new GroupSavingsData();
            String AllowPublicToJoin = "0";
            if (rq.isAllowPublicToJoin()) {
                AllowPublicToJoin = "1";
            }
            transAmount = new BigDecimal(rq.getGroupSavingAmount());
            doSave.setAllowPublicToJoin(AllowPublicToJoin);
            doSave.setGroupSavingAmount(transAmount);
            doSave.setGroupSavingFinalAmount(transAmount);
            doSave.setCreatedDate(Instant.now());
            doSave.setGroupSavingDescription(rq.getGroupSavingDescription());
            doSave.setGroupSavingName(rq.getGroupSavingName());
            doSave.setNumberOfMembers(String.valueOf(rq.getNumberOfMembers()));
            doSave.setPayOutDateOfTheMonth(payOutDateOfTheMonth);
            doSave.setAdminPayOutSlot(rq.getSelectedSlot());
            doSave.setTransactionId(transactionId);
            doSave.setTransactionStatus("0");
            doSave.setTransactionStatusDesc("Initiated");
            doSave.setInviteCode(inviteCode);
            doSave.setTransactionIdLink(inviteCodeLink);
            doSave.setIsTrnsactionDeleted("0");
            doSave.setAddedMembersModels(null);
            doSave.setWalletId(wallDe.get(0).getWalletId());
            doSave.setPhoneNumber(wallDe.get(0).getPhoneNumber());
            doSave.setEmailAddress(wallDe.get(0).getEmail());
            int[] arrayT = returnAraryumberOfSlots(rq.getNumberOfMembers());
            doSave.setAvailablePayOutSlot(arrayT);
            //groupSavingsDataRepo

            boolean found = false;
            int target = rq.getSelectedSlot();
            for (int n : arrayT) {
                if (n == target) {
                    found = true;
                    break;
                }
            }
            
            if (found == true) {
                int[] arrayT2 = doSave.getAvailablePayOutSlot();
                int[] availablePayOutSlot = removeNumberAndConvertToString(arrayT2, rq.getSelectedSlot());
                doSave.setAvailablePayOutSlot(availablePayOutSlot);
                doSave.setAdminPayOutSlot(rq.getSelectedSlot());
                
            }
            
            for (CommissionCfg partData : pullData) {
                if (getKul.get().getServiceType().trim().equals(partData.getTransType())) {
                    
                    if (betweenTransBand(new BigDecimal(rq.getGroupSavingAmount()), new BigDecimal(partData.getAmountMin()), new BigDecimal(partData.getAmountMax())) == true) {
                        if (partData.getHasPercent().equals("1")) {

                            //compute the fees
                            //1.8% + 100 (convenience fee)
                            System.out.println(" rq.getGroupSavingAmount()" + "  :::::::::::::::::::::   " + rq.getGroupSavingAmount());
                            System.out.println(" pullData.get(0).getCommPercent()" + "  :::::::::::::::::::::   " + partData.getCommPercent());
                            //int conv = Integer.valueOf(pullData.get(0).getCommPercent().toString());

                            BigDecimal pFees = percentage(new BigDecimal(rq.getGroupSavingAmount()), partData.getCommPercent());
                            System.out.println(" pFees before added flatCharges" + "  :::::::::::::::::::::   " + pFees);
                            
                            fees = pFees.add(partData.getCharges());
                            doSave.setGroupSavingAmount(transAmount);
                            doSave.setGroupSavingFinalAmount(transAmount.add(fees));
                            
                        } else {
                            
                            doSave.setGroupSavingAmount(transAmount);
                            doSave.setGroupSavingFinalAmount(transAmount.add(fees));
                            
                        }
                    }
                }
            }
            
            groupSavingsDataRepo.save(doSave);
            
            responseModel.setDescription("Kindly confirm Group Creation.");
            responseModel.setStatusCode(200);
            responseModel.setData(doSave);
            return responseModel;
            
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
    }
    
    public ApiResponseModel getUserInitiateGroupSavings(ReByEmailAddress rq, String channel, String auth) {
        ApiResponseModel responseModel = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            
            if (!rq.getEmailAddress().equals(getDecoded.emailAddress)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Suspected fraud!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Suspected fraud!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            //System.out.println("pageNation :::::::: " + "    ::::::::::::::::::::: " + returnPagenation());
            //  List<KuleanPaymentTransaction> getKulTransPage = kuleanPaymentTransactionRepo.findByWalletNoListPage(getDecoded.phoneNumber, PageRequest.of(0, pageNation));
            boolean chkGDetailsBool = true;

            /* List<GroupSavingsData> chkGDetails = groupSavingsDataRepo.findByEmailAddressOrdered(rq.getEmailAddress());
             if (chkGDetails.size() <= 0) {
                chkGDetailsBool = false;

            }*/
            //get groups from aaded to groups
            List<GroupSavingsData> getALLGROUPS = groupSavingsDataRepo.findByInviteCode(rq.getEmailAddress());
            
            if (getALLGROUPS.size() <= 0) {
                chkGDetailsBool = false;
                
            }
            if (chkGDetailsBool == false) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "There is no existing group!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("There is no existing group!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            List<SavedGroupDetails> mapAll = new ArrayList<SavedGroupDetails>();
            
            if (getALLGROUPS.size() > 0) {
                //check through to see that customer can have active transaction above configured number of active

                for (GroupSavingsData checkDeInt : getALLGROUPS) {
                    
                    String gName = checkDeInt.getGroupSavingName().toUpperCase();
                    if (!checkDeInt.getIsTrnsactionDeleted().equals("1")) {
                        if (checkDeInt.getTransactionStatus().equals("0") && checkDeInt.getTransactionStatus().equals("Initiated")) {
                            SettlementFailureLog conWall = new SettlementFailureLog("", "",
                                    "You have an existing group(" + gName + ") initiated but not yet created!");
                            settlementFailureLogRepo.save(conWall);
                            
                            GroupSavingsDataModel doSave = new GroupSavingsDataModel();
                            
                            doSave.setAllowPublicToJoin(checkDeInt.getAllowPublicToJoin());
                            doSave.setTransactionDate(formDate(checkDeInt.getCreatedDate()));
                            
                            doSave.setGroupSavingDescription(checkDeInt.getGroupSavingDescription());
                            doSave.setGroupSavingName(checkDeInt.getGroupSavingName());
                            doSave.setNumberOfMembers(String.valueOf(checkDeInt.getNumberOfMembers()));
                            doSave.setPayOutDateOfTheMonth(checkDeInt.getPayOutDateOfTheMonth());
                            doSave.setAdminPayOutSlot(checkDeInt.getAdminPayOutSlot());
                            doSave.setTransactionId(checkDeInt.getTransactionId());
                            doSave.setTransactionStatus(checkDeInt.getTransactionStatus());
                            doSave.setTransactionStatusDesc(checkDeInt.getTransactionStatusDesc());
                            doSave.setInviteCode(checkDeInt.getInviteCode());
                            doSave.setTransactionIdLink(checkDeInt.getTransactionIdLink());
                            doSave.setIsTrnsactionDeleted(checkDeInt.getIsTrnsactionDeleted());
                            doSave.setAddedMembersModels(checkDeInt.getAddedMembersModels());
                            doSave.setWalletId(checkDeInt.getWalletId());
                            doSave.setPhoneNumber(checkDeInt.getPhoneNumber());
                            doSave.setEmailAddress(checkDeInt.getEmailAddress());
                            doSave.setGroupSavingAmount(checkDeInt.getGroupSavingAmount());
                            doSave.setGroupSavingFinalAmount(checkDeInt.getGroupSavingFinalAmount());
                            // String[] array = checkDeInt.getAvailablePayOutSlot().split(",");
                            doSave.setAvailablePayOutSlot(checkDeInt.getAvailablePayOutSlot());
                            
                            responseModel.setData(doSave);
                            
                            responseModel.setStatusCode(90);
                            responseModel.setDescription("You have an existing group(" + gName + ") initiated but not yet created!");
                            return responseModel;
                        }
                    }
                    
                }
                
            }
            
            if (mapAll.isEmpty()) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Customer does not have an existing group!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Customer does not have an existing group!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            responseModel.setData(mapAll);
            responseModel.setDescription("Customer transactions pulled successfully.");
            responseModel.setStatusCode(200);
            
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
        
    }
    
    public BaseResponse confirmCreateTransaction(GroupSavingConf rq, String channel, String auth) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured, please try again.";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String emailAddress = getDecoded.emailAddress;
            GroupSavingsData updateRecord = null;
            // System.out.println("email from jwt" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + emailAddress);

            if (!rq.getEmailAddress().equals(getDecoded.emailAddress)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Suspected fraud!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Suspected fraud!!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            List<RegWalletInfo> getWallDe = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);
            if (!getWallDe.get(0).isActivation()) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Customer has not created PIN!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setDescription("Customer has not created PIN!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            String encyrptedPin = utilMeth.encyrpt(String.valueOf(rq.getPin()), encryptionKey);
            String pin = getWallDe.get(0).getPersonId();
            if (!encyrptedPin.equals(pin)) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Invalid PIN!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setDescription("Invalid PIN!");
                responseModel.setStatusCode(statusCode);
                
                return responseModel;
                
            }
            
            List<GroupSavingsData> chkPendId = groupSavingsDataRepo.findByInviteCodeAndEmailAddress(rq.getInvitationCodeReqId(), rq.getEmailAddress());
            boolean itsId = false;
            boolean itsIdLink = false;
            List<GroupSavingsData> chkPendIdLink = groupSavingsDataRepo.findByTransactionIdLinkAndEmailAddress(rq.getInvitationCodeReqId(), rq.getEmailAddress());
            AddMembersModels adM = new AddMembersModels();
            String addedMeStr = "";
            
            if (chkPendId.size() > 0) {
                
                if (chkPendId.get(0).getIsTrnsactionDeleted().equals("1")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction is deleted!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction is deleted!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
                if (chkPendId.get(0).getTransactionStatus().equals("2")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction has already being confirmed!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction has already being confirmed!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
                itsId = true;
                
                updateRecord = groupSavingsDataRepo.findByInviteCodeDe(rq.getInvitationCodeReqId());
                updateRecord.setLastModifiedDate(Instant.now());
                updateRecord.setTransactionStatus("2");
                updateRecord.setTransactionStatusDesc("Created");
                //add members

                adM.setAdminEmailAddress(emailAddress);
                adM.setIsAdmin("1");
                adM.setMemberEmailAddress(emailAddress);
                adM.setInvitationCodeReqId(rq.getInvitationCodeReqId());
                adM.setMemberId(getWallDe.get(0).getWalletId());
                adM.setMemberIdType("WalletId");
                adM.setMemberName(getWallDe.get(0).getFullName());
                adM.setMemberUserId(getWallDe.get(0).getFullName());
                adM.setMemberJoined("1");
                adM.setPayOutStatus("3");
                adM.setPayOutStatusDesc("Not yet");
                adM.setAmountToContribute(BigDecimal.ZERO);
                adM.setCurrentMonthContribution(BigDecimal.ZERO);
                adM.setCurrentMonthContributionStatusDesc("Not yet");
                adM.setCurrentMonthContributionStatusId("3");
                adM.setTotalMonthlyContribution(BigDecimal.ZERO);
                // adM.setMemberEmailAddress("");
                SwapSlot swP = new SwapSlot();
                swP.setIsSwapActive("0");
                swP.setIsSwapped("0");
                swP.setReceiverSlot(0);
                swP.setSenderSlot(0);
                adM.setSwapSlot(swP);

                /*boolean found = false;
                int target = rq.getSelectedSlot();
                for (int n : updateRecord.getAvailablePayOutSlot()) {
                    if (n == target) {
                        found = true;
                        break;
                    }
                }

                if (found == true) {
                    int[] arrayT = updateRecord.getAvailablePayOutSlot();
                    int[] availablePayOutSlot = removeNumberAndConvertToString(arrayT, rq.getSelectedSlot());
                    updateRecord.setAvailablePayOutSlot(availablePayOutSlot);

                }*/
                adM.setSlot(updateRecord.getAdminPayOutSlot());
                List<AddMembersModels> addMee = new ArrayList<>();
                addMee.add(adM);
                addedMeStr = returnStringOject(addMee);
                updateRecord.setAddedMembersModels(addedMeStr);
                
                responseModel.addData("InviteCode", chkPendId.get(0).getInviteCode());
                responseModel.addData("transactionIdLink", chkPendId.get(0).getTransactionIdLink());
                
            }
            
            System.out.println("itsId" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + itsId);
            
            if (chkPendIdLink.size() > 0) {
                if (chkPendIdLink.get(0).getIsTrnsactionDeleted().equals("1")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction is deleted!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction is deleted!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                if (chkPendIdLink.get(0).getTransactionStatus().equals("1")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction has already being confirmed!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction has already being confirmed!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
                itsIdLink = true;
                
                updateRecord = groupSavingsDataRepo.findByTransactionIdLinkDe(rq.getInvitationCodeReqId());
                updateRecord.setLastModifiedDate(Instant.now());
                updateRecord.setTransactionStatus("2");
                updateRecord.setTransactionStatusDesc("Created");
                adM.setAdminEmailAddress(emailAddress);
                adM.setIsAdmin("1");
                adM.setMemberEmailAddress(emailAddress);
                adM.setInvitationCodeReqId(rq.getInvitationCodeReqId());
                adM.setMemberId(getWallDe.get(0).getWalletId());
                adM.setMemberIdType("WalletId");
                adM.setMemberName(getWallDe.get(0).getFullName());
                adM.setMemberUserId(getWallDe.get(0).getFullName());
                adM.setMemberJoined("1");
                adM.setPayOutStatus("3");
                adM.setPayOutStatusDesc("Not yet");
                adM.setAmountToContribute(BigDecimal.ZERO);
                adM.setCurrentMonthContribution(BigDecimal.ZERO);
                adM.setCurrentMonthContributionStatusDesc("Not yet");
                adM.setCurrentMonthContributionStatusId("3");
                adM.setTotalMonthlyContribution(BigDecimal.ZERO);
                //adM.setMemberEmailAddress("");
                SwapSlot swP = new SwapSlot();
                swP.setIsSwapActive("0");
                swP.setIsSwapped("0");
                swP.setReceiverSlot(0);
                swP.setSenderSlot(0);
                adM.setSwapSlot(swP);

                /*boolean found = false;
                int target = rq.getSelectedSlot();
                for (int n : updateRecord.getAvailablePayOutSlot()) {
                    if (n == target) {
                        found = true;
                        break;
                    }
                }

                if (found == true) {
                    int[] arrayT = updateRecord.getAvailablePayOutSlot();
                    int[] availablePayOutSlot = removeNumberAndConvertToString(arrayT, rq.getSelectedSlot());
                    updateRecord.setAvailablePayOutSlot(availablePayOutSlot);

                }*/
                adM.setSlot(updateRecord.getAdminPayOutSlot());
                List<AddMembersModels> addMee = new ArrayList<>();
                addMee.add(adM);
                addedMeStr = returnStringOject(addMee);
                updateRecord.setAddedMembersModels(addedMeStr);
                
                responseModel.addData("InviteCode", chkPendIdLink.get(0).getInviteCode());
                responseModel.addData("transactionIdLink", chkPendIdLink.get(0).getTransactionIdLink());
                
            }
            
            System.out.println("itsIdLink" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + itsIdLink);
            
            if (itsId == false && itsIdLink == false) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "The transaction request-id is invalid!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("The transaction request-id is invalid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            groupSavingsDataRepo.save(updateRecord);
            
            responseModel.setDescription("Transaction Created.");
            responseModel.setStatusCode(200);
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
    }
    
    public BaseResponse activateGroup(GroupSavingActivation rq, String channel, String auth) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured, please try again.";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            GroupSavingsData updateRecord = null;
            // System.out.println("email from jwt" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + emailAddress);

            if (!rq.getEmailAddress().equals(getDecoded.emailAddress)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Suspected fraud!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Suspected fraud!!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            List<RegWalletInfo> getWallDe = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);
            if (!getWallDe.get(0).isActivation()) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Customer has not created PIN!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setDescription("Customer has not created PIN!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            String encyrptedPin = utilMeth.encyrpt(String.valueOf(rq.getPin()), encryptionKey);
            String pin = getWallDe.get(0).getPersonId();
            if (!encyrptedPin.equals(pin)) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Invalid PIN!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setDescription("Invalid PIN!");
                responseModel.setStatusCode(statusCode);
                
                return responseModel;
                
            }
            
            boolean valFormatD1 = utilMeth.isValidFormat(utilMeth.getSETTING_DATE_FORMATT(), rq.getActivationDate(), Locale.ENGLISH);
            
            if (valFormatD1 == false) {
                System.out.println("############# StartDate is invalid");
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Invalid activation date format dd/MM/yyyy!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Invalid activation date format dd/MM/yyyy!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
                
            }
            
            SimpleDateFormat formatter = new SimpleDateFormat(utilMeth.getSETTING_DATE_FORMATT());
            Date date = formatter.parse(rq.getActivationDate());
            System.out.println("Converted Date: " + date);

            //check if the activation date is after {48} hrs from now
            boolean getBool = checkIfDateIsAfterRequiredHours(date);
            
            int numbDate = Integer.parseInt(utilMeth.getSETTING_CONFIGURE_NUMB_DAYS_BEFORE_ACTIVATION());
            
            if (!getBool) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Selected date must be at least " + numbDate + " hours from now.");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Selected date must be at least " + numbDate + " hours from now.");
                responseModel.setStatusCode(statusCode);
                return responseModel;
                
            }
            
            List<GroupSavingsData> chkPendId = groupSavingsDataRepo.findByInviteCode(rq.getInvitationCodeReqId());
            boolean itsId = false;
            boolean itsIdLink = false;
            List<GroupSavingsData> chkPendIdLink = groupSavingsDataRepo.findByTransactionIdLink(rq.getInvitationCodeReqId());
            
            if (chkPendId.size() > 0) {
                
                if (!chkPendId.get(0).getEmailAddress().equals(rq.getEmailAddress())) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "Transaction owner mismatch!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("Transaction owner mismatch!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
                if (chkPendId.get(0).getIsTrnsactionDeleted().equals("1")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction is deleted!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction is deleted!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
                if (chkPendId.get(0).getTransactionStatus().equals("3")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction has already being activated!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction has already being activated!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }

                /* if (chkPendId.get(0).getTransactionStatus().equals("4")) {

                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction has already been accepted!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction has already been accepted!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }*/
                if (chkPendId.get(0).getIsTrnsactionDeleted().equals("1")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction no longer available!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction no longer available!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                itsId = true;
                
                updateRecord = groupSavingsDataRepo.findByInviteCodeDe(rq.getInvitationCodeReqId());
                updateRecord.setLastModifiedDate(Instant.now());
                updateRecord.setTransactionStatus("3");
                updateRecord.setTransactionStatusDesc("Activate");
                List<GroupSavingsScheduleEngine.Cycle> plan
                        = GroupSavingsScheduleEngine.buildSchedule(
                                updateRecord.getContributionFrequency(),
                                updateRecord.getCycleNumber(),
                                updateRecord.getPayoutPolicy()
                        );
                
                int i = 1;
                for (GroupSavingsScheduleEngine.Cycle c : plan) {
                    GroupSavingsCycle row = new GroupSavingsCycle();
                    row.setGroupId(updateRecord.getId());
                    row.setCycleNumber(i++);
                    row.setContributionDate(c.contributionDate);      // public final fields in your engine
                    row.setContributionWindowEnd(c.contributionWindowEnd);
                    row.setPayoutDate(c.payoutDate);                  // may be null for AFTER_ALL_CONTRIBUTIONS
                    row.setStatus(GroupSavingsCycle.CycleStatus.PENDING);
                    groupSavingsCycleRepo.save(row);
                }
                //add members

                responseModel.addData("InviteCode", chkPendId.get(0).getInviteCode());
                responseModel.addData("transactionIdLink", chkPendId.get(0).getTransactionIdLink());
                
            }
            
            System.out.println("itsId" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + itsId);
            
            if (chkPendIdLink.size() > 0) {
                if (!chkPendIdLink.get(0).getEmailAddress().equals(rq.getEmailAddress())) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "Transaction owner mismatch!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("Transaction owner mismatch!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                if (chkPendIdLink.get(0).getIsTrnsactionDeleted().equals("1")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction is deleted!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction is deleted!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                if (chkPendIdLink.get(0).getTransactionStatus().equals("3")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction has already being activated!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction has already being activated!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }

                /* if (chkPendIdLink.get(0).getTransactionStatus().equals("4")) {

                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction has already been accepted!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction has already been accepted!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }*/
                if (chkPendIdLink.get(0).getIsTrnsactionDeleted().equals("1")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction no longer available!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction no longer available!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                itsIdLink = true;
                
                updateRecord = groupSavingsDataRepo.findByTransactionIdLinkDe(rq.getInvitationCodeReqId());
                updateRecord.setLastModifiedDate(Instant.now());
                updateRecord.setTransactionStatus("3");
                updateRecord.setTransactionStatusDesc("Activate");
                responseModel.addData("InviteCode", chkPendIdLink.get(0).getInviteCode());
                responseModel.addData("transactionIdLink", chkPendIdLink.get(0).getTransactionIdLink());
                
                List<GroupSavingsScheduleEngine.Cycle> plan
                        = GroupSavingsScheduleEngine.buildSchedule(
                                updateRecord.getContributionFrequency(),
                                updateRecord.getCycleNumber(),
                                updateRecord.getPayoutPolicy()
                        );
                
                int i = 1;
                for (GroupSavingsScheduleEngine.Cycle c : plan) {
                    GroupSavingsCycle row = new GroupSavingsCycle();
                    row.setGroupId(updateRecord.getId());
                    row.setCycleNumber(i++);
                    row.setContributionDate(c.contributionDate);      // public final fields in your engine
                    row.setContributionWindowEnd(c.contributionWindowEnd);
                    row.setPayoutDate(c.payoutDate);                  // may be null for AFTER_ALL_CONTRIBUTIONS
                    row.setStatus(GroupSavingsCycle.CycleStatus.PENDING);
                    groupSavingsCycleRepo.save(row);
                }
                
            }
            
            System.out.println("itsIdLink" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + itsIdLink);
            
            if (itsId == false && itsIdLink == false) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "The transaction request-id is invalid!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("The transaction request-id is invalid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            groupSavingsDataRepo.save(updateRecord);
            
            responseModel.setDescription("Transaction activated.");
            responseModel.setStatusCode(200);
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
        
    }
    
    public BaseResponse getCusDetails(GetMemBerDe rq, String channel, String auth) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured, please try again.";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            Map mp = new HashMap();

            // System.out.println("email from jwt" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + emailAddress);
            if (!rq.getEmailAddress().equals(getDecoded.emailAddress)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Suspected fraud!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Suspected fraud!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            boolean isWalletId = true;
            boolean isPhonenUmber = false;
            
            if (!utilMeth.isValid10Num(rq.getMemberId().trim())) {
                isWalletId = false;
                isPhonenUmber = true;
                
            }

            /* if (!utilMeth.isValid11Num(rq.getMemberId())) {

                isPhonenUmber = false;

            }*/
            if (isWalletId == false && isPhonenUmber == false) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Invalid MemberId!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Invalid MemberId!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
                
            }
            
            if (isWalletId) {
                
                List<RegWalletInfo> wallDe = regWalletInfoRepository.findByWalletIdList(rq.getMemberId());
                if (wallDe.size() <= 0) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "MemberId does not exist!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("MemberId does not exist!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                    
                }
                
                mp.put("WalletId", rq.getMemberId());
                mp.put("Name", wallDe.get(0).getFullName());
                mp.put("userId", wallDe.get(0).getWalletId());
                
            }
            
            if (isPhonenUmber) {
                
                List<RegWalletInfo> wallDe = regWalletInfoRepository.findByPhoneNumberData(rq.getMemberId());
                if (wallDe.size() <= 0) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "MemberId does not exist!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("MemberId does not exist!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                    
                }
                
                mp.put("phoneNumber", rq.getMemberId());
                mp.put("name", wallDe.get(0).getFullName());
                mp.put("userId", wallDe.get(0).getWalletId());
                
            }
            responseModel.setDescription("Member details gotten successfully.");
            responseModel.setStatusCode(200);
            responseModel.setData(mp);
            
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
        
    }
    
    public BaseResponse addMembers(AddedMembersFE rq, String channel, String auth) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured, please try again.";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String emailAddress = getDecoded.emailAddress;
            GroupSavingsData updateRecord = null;
            AddMembersModels adM = new AddMembersModels();
            String addedMeStr;

            // System.out.println("email from jwt" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + emailAddress);
            if (!rq.getAdminEmailAddress().equals(getDecoded.emailAddress)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Suspected fraud!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Suspected fraud!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            boolean isWalletId = true;
            boolean isPhonenUmber = false;
            
            if (!utilMeth.isValid10Num(rq.getMemberId().trim())) {
                isWalletId = false;
                isPhonenUmber = true;
                
            }

            /*if (!utilMeth.isValid11Num(rq.getMemberId())) {

                isPhonenUmber = false;

            }*/
            List<RegWalletInfo> wallDeCr = regWalletInfoRepository.findByEmailsList(emailAddress);
            
            if (isWalletId == false && isPhonenUmber == false) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Invalid MemberId!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Invalid MemberId!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
                
            }
            
            if (isWalletId) {
                
                if (wallDeCr.get(0).getWalletId().equals(rq.getMemberId())) {
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "MemberId mismatch, initiator cannot add self!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("MemberId mismatch, initiator cannot add self!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
                List<RegWalletInfo> wallDe = regWalletInfoRepository.findByWalletIdList(rq.getMemberId());
                if (wallDe.size() <= 0) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "MemberId does not exist!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("MemberId does not exist!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                    
                }
                
                adM.setAdminEmailAddress(emailAddress);
                adM.setInvitationCodeReqId(rq.getInvitationCodeReqId());
                adM.setMemberId(wallDe.get(0).getWalletId());
                adM.setMemberIdType("WalletId");
                adM.setMemberName(wallDe.get(0).getFullName());
                adM.setMemberUserId(wallDe.get(0).getFullName());
                adM.setMemberEmailAddress(wallDe.get(0).getEmail());
                adM.setMemberJoined("0");
                adM.setPayOutStatus("3");
                adM.setPayOutStatusDesc("Not yet");
                adM.setAmountToContribute(BigDecimal.ZERO);
                adM.setCurrentMonthContribution(BigDecimal.ZERO);
                adM.setCurrentMonthContributionStatusDesc("Not yet");
                adM.setCurrentMonthContributionStatusId("3");
                adM.setTotalMonthlyContribution(BigDecimal.ZERO);
                
                SwapSlot swP = new SwapSlot();
                swP.setIsSwapActive("0");
                swP.setIsSwapped("0");
                swP.setReceiverSlot(0);
                swP.setSenderSlot(0);
                adM.setSwapSlot(swP);
                
            }
            
            if (isPhonenUmber) {
                
                if (wallDeCr.get(0).getPhoneNumber().equals(rq.getMemberId())) {
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "MemberId mismatch, initiator cannot add self!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("MemberId mismatch, initiator cannot add self!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
                List<RegWalletInfo> wallDe = regWalletInfoRepository.findByPhoneNumberData(rq.getMemberId());
                if (wallDe.size() <= 0) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "MemberId does not exist!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("MemberId does not exist!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                    
                }
                
                adM.setAdminEmailAddress(emailAddress);
                adM.setInvitationCodeReqId(rq.getInvitationCodeReqId());
                adM.setMemberId(wallDe.get(0).getPhoneNumber());
                adM.setMemberIdType("Phonenumber");
                adM.setMemberName(wallDe.get(0).getFullName());
                adM.setMemberUserId(wallDe.get(0).getFullName());
                adM.setMemberEmailAddress(wallDe.get(0).getEmail());
                adM.setMemberJoined("0");
                adM.setPayOutStatus("3");
                adM.setPayOutStatusDesc("Not yet");
                adM.setAmountToContribute(BigDecimal.ZERO);
                adM.setCurrentMonthContribution(BigDecimal.ZERO);
                adM.setCurrentMonthContributionStatusDesc("Not yet");
                adM.setCurrentMonthContributionStatusId("3");
                adM.setTotalMonthlyContribution(BigDecimal.ZERO);
                SwapSlot swP = new SwapSlot();
                swP.setIsSwapActive("0");
                swP.setIsSwapped("0");
                swP.setReceiverSlot(0);
                swP.setSenderSlot(0);
                adM.setSwapSlot(swP);
                
            }
            
            List<GroupSavingsData> chkPendId = groupSavingsDataRepo.findByInviteCode(rq.getInvitationCodeReqId());
            boolean itsId = false;
            boolean itsIdLink = false;
            List<GroupSavingsData> chkPendIdLink = groupSavingsDataRepo.findByTransactionIdLink(rq.getInvitationCodeReqId());
            String getAddedMemberString = null;
            String numMems = null;
            if (chkPendId.size() > 0) {

                //check if Adder is an admin
                if (!chkPendId.get(0).getEmailAddress().equals(rq.getAdminEmailAddress())) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "Transaction owner mismatch!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("Transaction owner mismatch!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
                if (chkPendId.get(0).getIsTrnsactionDeleted().equals("1")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction is deleted!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction is deleted!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
                if (chkPendId.get(0).getIsTrnsactionDeleted().equals("1")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction no longer available!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction no longer available!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                itsId = true;
                numMems = chkPendId.get(0).getNumberOfMembers();
                
                getAddedMemberString = chkPendId.get(0).getAddedMembersModels();
                updateRecord = groupSavingsDataRepo.findByInviteCodeDe(rq.getInvitationCodeReqId());
                updateRecord.setLastModifiedDate(Instant.now());
                
                responseModel.addData("InviteCode", chkPendId.get(0).getInviteCode());
                responseModel.addData("inviteCodeLink", chkPendId.get(0).getTransactionIdLink());
                
            }
            
            System.out.println("itsId" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + itsId);
            
            if (chkPendIdLink.size() > 0) {
                if (!chkPendIdLink.get(0).getEmailAddress().equals(rq.getAdminEmailAddress())) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "Transaction owner mismatch!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("Transaction owner mismatch!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                if (chkPendIdLink.get(0).getIsTrnsactionDeleted().equals("1")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction is deleted!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction is deleted!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
                if (chkPendIdLink.get(0).getIsTrnsactionDeleted().equals("1")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction no longer available!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction no longer available!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                itsIdLink = true;
                numMems = chkPendIdLink.get(0).getNumberOfMembers();
                
                getAddedMemberString = chkPendIdLink.get(0).getAddedMembersModels();
                
                updateRecord = groupSavingsDataRepo.findByInviteCodeDe(rq.getInvitationCodeReqId());
                updateRecord.setLastModifiedDate(Instant.now());
                
                responseModel.addData("InviteCode", chkPendIdLink.get(0).getInviteCode());
                responseModel.addData("inviteCodeLink", chkPendIdLink.get(0).getTransactionIdLink());
                
            }
            
            System.out.println("itsIdLink" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + itsIdLink);
            
            if (itsId == false && itsIdLink == false) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "The transaction request-id is invalid!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("The transaction request-id is invalid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            //get numbers of members'
            //System.out.println("adM ::::::::::::::::  %S  " + new Gson().toJson(adM));
            // System.out.println("getAddedMemberString ::::::::::::::::  %S  " + new Gson().toJson(getAddedMemberString));
            int numb = 0;
            ObjectMapper mapper = new ObjectMapper();
            List<AddMembersModels> mems = mapper.readValue(getAddedMemberString, new TypeReference<List<AddMembersModels>>() {
            });
            
            String memEmailAdd = adM.getMemberEmailAddress();
            for (AddMembersModels getMems : mems) {
                String gottenMemEmail = getMems.getMemberEmailAddress() == null ? "empty" : getMems.getMemberEmailAddress();
                if (gottenMemEmail.equals(memEmailAdd)) {
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "The Member alread exist in the Group, you cannot re-add");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("The Member alread exist in the Group, you cannot re-add!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                if (getMems.getMemberId() != null) {
                    numb = numb + 1;
                }
            }
            
            int convgetMaxNoOfMembers = Integer.valueOf(numMems);
            if (numb > convgetMaxNoOfMembers) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Number of Members cannot be more than " + convgetMaxNoOfMembers + ".");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Number of Members cannot be more than " + convgetMaxNoOfMembers + ".");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            List<AddMembersModels> addMee = mems;
            addMee.add(adM);
            addedMeStr = returnStringOject(addMee);
            updateRecord.setAddedMembersModels(addedMeStr);
            groupSavingsDataRepo.save(updateRecord);
            
            responseModel.setDescription("Member added sucessfully.");
            responseModel.setStatusCode(200);
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
    }
    
    private BaseResponse validateRequest(ValidateReqReq rq) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured, please try again.";
        try {
            statusCode = 400;
            
            List<GroupSavingsData> chkPendId = groupSavingsDataRepo.findByInviteCode(rq.getInvitationCodeReqId());
            boolean itsId = false;
            boolean itsIdLink = false;
            List<GroupSavingsData> chkPendIdLink = groupSavingsDataRepo.findByTransactionIdLink(rq.getInvitationCodeReqId());
            if (chkPendId.size() > 0) {

                //check if the merid and the sender is in the group details
                if (chkPendId.get(0).getIsTrnsactionDeleted().equals("1")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction is deleted!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction is deleted!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
                if (chkPendId.get(0).getIsTrnsactionDeleted().equals("1")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction no longer available!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction no longer available!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
                if (chkPendId.get(0).getIsTrnsactionDeleted().equals("4")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction contribution in progress, user can no longer join the group!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction contribution in progress, user can no longer join the group!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                itsId = true;
                
                responseModel.addData("InviteCode", chkPendId.get(0).getInviteCode());
                responseModel.addData("inviteCodeLink", chkPendId.get(0).getTransactionIdLink());
                
            }
            
            System.out.println("itsId" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + itsId);
            
            if (chkPendIdLink.size() > 0) {
                
                if (chkPendIdLink.get(0).getIsTrnsactionDeleted().equals("1")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction is deleted!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction is deleted!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
                if (chkPendIdLink.get(0).getIsTrnsactionDeleted().equals("1")) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "This transaction no longer available!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("This transaction no longer available!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                itsIdLink = true;
                
                responseModel.addData("InviteCode", chkPendIdLink.get(0).getInviteCode());
                responseModel.addData("inviteCodeLink", chkPendIdLink.get(0).getTransactionIdLink());
                
            }
            
            System.out.println("itsIdLink" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + itsIdLink);
            
            if (itsId == false && itsIdLink == false) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "The transaction request-id is invalid!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("The transaction request-id is invalid!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            responseModel.setDescription("Valid request!");
            responseModel.setStatusCode(200);
            return responseModel;
            
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
        
    }

    //check if the merid exists in the group details
    public BaseResponse checkIfMerIdExistsInTheDetails(CheckIfMerIdExists rq) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        boolean userExists = false;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            
            System.out.println("checkIfMerIdExistsInTheDetails rq ::::::::::::::::  %S  " + new Gson().toJson(rq));
            
            boolean chkGDetailsBool = true;
            List<GroupSavingsData> getALLGROUPS = groupSavingsDataRepo.findByInviteCode(rq.getInvitationCodeReqId());
            if (getALLGROUPS.size() <= 0) {
                chkGDetailsBool = false;
                
            }
            if (chkGDetailsBool == false) {
                responseModel.setDescription("Transaction does not exist");
                responseModel.setStatusCode(statusCode);
                if (environment.equals("staging")) {
                    System.out.println("responseModel ::::::::::::::::  %S  " + new Gson().toJson(responseModel));
                }
                return responseModel;
            }
            
            Map mp = new HashMap();
            
            for (GroupSavingsData getByCodeI : getALLGROUPS) {
                
                if (!getByCodeI.getIsTrnsactionDeleted().equals("1")) {
                    
                    if (getByCodeI.getAddedMembersModels() != null) {

                        //   System.out.println("getByCodeI.getAddedMembersModels() ::::::::::::::::  %S  " + new Gson().toJson(getByCodeI.getAddedMembersModels()));
                        ObjectMapper mappergetByCode = new ObjectMapper();
                        List<AddMembersModels> memsgetByCode = mappergetByCode.readValue(getByCodeI.getAddedMembersModels(), new TypeReference<List<AddMembersModels>>() {
                        });
                        for (AddMembersModels getMemsByCode : memsgetByCode) {

                            //System.out.println("getMemsByCode.getMemberId() ::::::::::::::::  %S  " + getMemsByCode.getMemberId());
                            //System.out.println("getMemsByCode.getMemberIdType() ::::::::::::::::  %S  " + getMemsByCode.getMemberIdType());
                            if (getMemsByCode.getMemberEmailAddress().equals(rq.getEmailAddress())) {
                                // if (getMemsByCode.getMemberId().equals(rq.getMemberId())) {
                                responseModel.setDescription("Transaction exists");
                                responseModel.setStatusCode(200);
                                mp.put("slotNumber", getMemsByCode.getSlot() == 0 ? "" : getMemsByCode.getSlot());
                                mp.put("memberEamil", getMemsByCode.getMemberEmailAddress() == null ? "" : getMemsByCode.getMemberEmailAddress());
                                responseModel.setData(mp);
                                
                                responseModel.setDescription("success");
                                responseModel.setStatusCode(200);
                                if (environment.equals("staging")) {
                                    System.out.println("responseModel ::::::::::::::::  %S  " + new Gson().toJson(responseModel));
                                }
                                
                                return responseModel;
                                // }
                            }
                        }
                        
                    }
                }
                
            }
            
            responseModel.setDescription("Invalid request");
            responseModel.setStatusCode(400);
            
            System.out.println("responseModel ::::::::::::::::  %S  " + new Gson().toJson(responseModel));
            
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
    }
    
    public BaseResponse checkSenderDetailss(CheckIfMerIdExists rq) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        boolean userExists = false;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            
            System.out.println("checkSenderDetailss rq ::::::::::::::::  %S  " + new Gson().toJson(rq));
            
            boolean chkGDetailsBool = true;
            List<GroupSavingsData> getALLGROUPS = groupSavingsDataRepo.findByInviteCode(rq.getInvitationCodeReqId());
            if (getALLGROUPS.size() <= 0) {
                chkGDetailsBool = false;
                
            }
            if (chkGDetailsBool == false) {
                responseModel.setDescription("Transaction does not exist");
                responseModel.setStatusCode(statusCode);
                if (environment.equals("staging")) {
                    System.out.println("responseModel ::::::::::::::::  %S  " + new Gson().toJson(responseModel));
                }
                return responseModel;
            }
            
            Map mp = new HashMap();
            
            for (GroupSavingsData getByCodeI : getALLGROUPS) {
                
                if (!getByCodeI.getIsTrnsactionDeleted().equals("1")) {
                    
                    if (getByCodeI.getAddedMembersModels() != null) {
                        
                        ObjectMapper mappergetByCode = new ObjectMapper();
                        List<AddMembersModels> memsgetByCode = mappergetByCode.readValue(getByCodeI.getAddedMembersModels(), new TypeReference<List<AddMembersModels>>() {
                        });
                        for (AddMembersModels getMemsByCode : memsgetByCode) {
                            
                            if (getMemsByCode.getMemberEmailAddress().equals(rq.getEmailAddress())) {
                                
                                responseModel.setDescription("Transaction exists");
                                responseModel.setStatusCode(200);
                                mp.put("slotNumber", getMemsByCode.getSlot());
                                mp.put("senderName", getMemsByCode.getMemberName());
                                responseModel.setData(mp);
                                responseModel.setDescription("success");
                                responseModel.setStatusCode(200);
                                if (environment.equals("staging")) {
                                    System.out.println("responseModel ::::::::::::::::  %S  " + new Gson().toJson(responseModel));
                                    
                                }
                                
                                return responseModel;
                                
                            }
                            
                        }
                        
                    }
                }
                
            }
            
            responseModel.setDescription("Invalid request");
            responseModel.setStatusCode(400);
            System.out.println("responseModel ::::::::::::::::  %S  " + new Gson().toJson(responseModel));
            
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
    }

    //get all slots and members details
    //if all slots have not been taken, slots cannot be swapped
    public ApiResponseModel getSavingsSlots(ReByInvitationCode rq, String channel, String auth) {
        ApiResponseModel responseModel = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            
            if (!rq.getEmailAddress().equals(getDecoded.emailAddress)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Suspected fraud!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Suspected fraud!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            System.out.println("pageNation :::::::: " + "    ::::::::::::::::::::: " + returnPagenation());
            boolean chkGDetailsBool = true;
            
            List<GroupSavingsData> getALLGROUPS = groupSavingsDataRepo.findByInviteCode(rq.getInvitationCodeReqId());
            if (getALLGROUPS.size() <= 0) {
                chkGDetailsBool = false;
                
            }
            if (chkGDetailsBool == false) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "There is no existing group!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("There is no existing group!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            List<SavedGroupSlotDetails> mapAll = new ArrayList<SavedGroupSlotDetails>();
            SavedGroupSlotDetails logC = new SavedGroupSlotDetails();
            
            String numMems = "0";
            int numb = 0;
            for (GroupSavingsData getByCodeI : getALLGROUPS) {
                
                if (!getByCodeI.getIsTrnsactionDeleted().equals("1")) {
                    numMems = getByCodeI.getNumberOfMembers();
                    
                    if (getByCodeI.getAddedMembersModels() != null) {

                        // for (GroupSavingsData getByCodeI : getByCode) {
                        logC.setGroupSavingDescription(getByCodeI.getGroupSavingDescription());
                        logC.setGroupSavingName(getByCodeI.getGroupSavingName());
                        logC.setInviteCode(getByCodeI.getInviteCode());
                        logC.setNumberOfMembers(getByCodeI.getNumberOfMembers());
                        logC.setAdminPayOutSlot(returnAraryumberOfSlots(getByCodeI.getAdminPayOutSlot()));
                        logC.setAvailablePayOutSlot(getByCodeI.getAvailablePayOutSlot());
                        
                        ObjectMapper mappergetByCode = new ObjectMapper();
                        List<AddMembersModels> memsgetByCode = mappergetByCode.readValue(getByCodeI.getAddedMembersModels(), new TypeReference<List<AddMembersModels>>() {
                        });
                        List<AddMembersModels> addMeegetByCode = new ArrayList<>();
                        for (AddMembersModels getMemsByCode : memsgetByCode) {
                            AddMembersModels adM = new AddMembersModels();
                            adM.setAdminEmailAddress(getMemsByCode.getAdminEmailAddress());
                            adM.setIsAdmin(getMemsByCode.getIsAdmin());
                            adM.setInvitationCodeReqId(getMemsByCode.getInvitationCodeReqId());
                            adM.setMemberId(getMemsByCode.getMemberId());
                            adM.setMemberIdType(getMemsByCode.getMemberIdType());
                            adM.setMemberName(getMemsByCode.getMemberName());
                            adM.setMemberUserId(getMemsByCode.getMemberUserId());
                            adM.setSlot(getMemsByCode.getSlot());
                            adM.setMemberJoined(getMemsByCode.getMemberJoined());
                            adM.setPayOutStatus(getMemsByCode.getPayOutStatus());
                            adM.setPayOutStatusDesc(getMemsByCode.getPayOutStatusDesc());
                            adM.setAmountToContribute(getMemsByCode.getAmountToContribute());
                            adM.setCurrentMonthContribution(getMemsByCode.getCurrentMonthContribution());
                            adM.setCurrentMonthContributionStatusDesc(getMemsByCode.getCurrentMonthContributionStatusDesc());
                            adM.setCurrentMonthContributionStatusId(getMemsByCode.getCurrentMonthContributionStatusId());
                            adM.setTotalMonthlyContribution(getMemsByCode.getTotalMonthlyContribution());
                            SwapSlot swP = new SwapSlot();
                            swP.setIsSwapActive(getMemsByCode.getSwapSlot().getIsSwapActive());
                            swP.setIsSwapped(getMemsByCode.getSwapSlot().getIsSwapped());
                            swP.setDateSwapped(getMemsByCode.getSwapSlot().getDateSwapped() == null ? "" : getMemsByCode.getSwapSlot().getDateSwapped());
                            swP.setReceiverId(getMemsByCode.getSwapSlot().getReceiverId() == null ? "" : getMemsByCode.getSwapSlot().getReceiverId());
                            swP.setReceiverSlot(getMemsByCode.getSwapSlot().getReceiverSlot() == 0 ? 0 : getMemsByCode.getSwapSlot().getReceiverSlot());
                            swP.setSenderId(getMemsByCode.getSwapSlot().getSenderId() == null ? "" : getMemsByCode.getSwapSlot().getSenderId());
                            swP.setSenderSlot(getMemsByCode.getSwapSlot().getSenderSlot() == 0 ? 0 : getMemsByCode.getSwapSlot().getSenderSlot());
                            adM.setSwapSlot(swP);
                            
                            addMeegetByCode.add(adM);
                            //numb = numb + 1;

                        }
                        
                        logC.setAddMembersModels(addMeegetByCode);

                        //logC.setTransactionId(getKul.getTransactionId());
                        logC.setTransactionStatus(getByCodeI.getTransactionStatus());
                        logC.setTransactionStatusDesc(getByCodeI.getTransactionStatusDesc());
                        
                        if (getByCodeI.getLastModifiedDate() == null) {
                            logC.setTransactionDate(formDate(getByCodeI.getCreatedDate()));
                        } else {
                            
                            logC.setTransactionDate(formDate(getByCodeI.getLastModifiedDate()));
                        }
                        
                        mapAll.add(logC);
                        
                    }
                }
                
            }
            
            if (mapAll.isEmpty()) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Customer does not have an existing group!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Customer does not have an existing group!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            int convgetMaxNoOfMembers = Integer.valueOf(numMems);
            if (numb != convgetMaxNoOfMembers) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Slot swap could only be applied if all members have joined.");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Slot swap could only be applied if all members have joined.");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            responseModel.setData(mapAll);
            responseModel.setDescription("Customer transactions pulled successfully.");
            responseModel.setStatusCode(200);
            
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
        
    }

    //swap slots
    //get available slot
    //send swap request to receiver
    //swap request could either be accepted or failed
    //if accepted update repective paired swap
    public BaseResponse sendSwapRequest(SwapSlotReq rq, String channel, String auth) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured, please try again.";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String emailAddress = getDecoded.emailAddress;

            // System.out.println("email from jwt" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + emailAddress);
            if (!rq.getSenderEmailAddress().equals(getDecoded.emailAddress)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Suspected fraud!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Suspected fraud!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            boolean isWalletId = true;
            boolean isPhonenUmber = false;
            CheckIfMerIdExists rr = new CheckIfMerIdExists();
            
            List<SwapSlotDetails> getSw = swapSlotDetailsRepo.findBySenderEmailAddressAndMemberIdAndInvitationCodeReqId(emailAddress, rq.getMemberId(), rq.getInvitationCodeReqId());
            
            if (getSw.size() > 0) {
                if (getSw.get(0).getIsSlotSwappedStatus().equals("Pending") || getSw.get(0).getIsSlotSwapped().equals("0")) {
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "You have an an awaiting Swap Slot with Receiver!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("You have an an awaiting Swap Slot with Receiver!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
                if (getSw.get(0).getIsSlotSwappedStatus().equals("Accepted")) {
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "You have an an existing Swap Slot with the Receiver!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("You have an an existing Swap Slot with the Receiver!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
            }
            
            if (!utilMeth.isValid10Num(rq.getMemberId().trim())) {
                isWalletId = false;
                isPhonenUmber = true;
                
            }
            /*if (!utilMeth.isValid11Num(rq.getMemberId())) {

                isPhonenUmber = false;

            }*/
            if (isWalletId == false && isPhonenUmber == false) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Invalid MemberId!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Invalid MemberId!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
                
            }
            
            if (isWalletId) {
                
                List<RegWalletInfo> wallDe = regWalletInfoRepository.findByWalletIdList(rq.getMemberId());
                if (wallDe.size() <= 0) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "MemberId does not exist!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("MemberId does not exist!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                    
                }
                rr.setEmailAddress(wallDe.get(0).getEmail());
                rr.setMemberId(rq.getMemberId());
                rr.setMemberIdType("WalletId");
                rr.setInvitationCodeReqId(rq.getInvitationCodeReqId());
                
            }
            
            if (isPhonenUmber) {
                
                List<RegWalletInfo> wallDe = regWalletInfoRepository.findByPhoneNumberData(rq.getMemberId());
                if (wallDe.size() <= 0) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "MemberId does not exist!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("MemberId does not exist!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                    
                }
                rr.setEmailAddress(wallDe.get(0).getEmail());
                rr.setMemberId(rq.getMemberId());
                rr.setMemberIdType("Phonenumber");
                rr.setInvitationCodeReqId(rq.getInvitationCodeReqId());
                
            }
            
            if (rq.getSenderEmailAddress().equals(rr.getEmailAddress())) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "You cannot swap with self, kindly check!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("You cannot swap with self, kindly check!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            List<RegWalletInfo> getWallDe = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);
            if (!getWallDe.get(0).isActivation()) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Customer has not created PIN!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setDescription("Customer has not created PIN!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            String encyrptedPin = utilMeth.encyrpt(String.valueOf(rq.getPin()), encryptionKey);
            String pin = getWallDe.get(0).getPersonId();
            if (!encyrptedPin.equals(pin)) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Invalid PIN!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setDescription("Invalid PIN!");
                responseModel.setStatusCode(statusCode);
                
                return responseModel;
                
            }
            
            SwapSlotDetails sw = new SwapSlotDetails();
            ValidateReqReq rqq = new ValidateReqReq();
            rqq.setAdminEmailAddress("");
            rqq.setInvitationCodeReqId(rq.getInvitationCodeReqId());
            rqq.setSenderEmailAddress(rq.getSenderEmailAddress());
            //check if mem exists

            BaseResponse checkMemberExists = this.checkIfMerIdExistsInTheDetails(rr);
            if (checkMemberExists.getStatusCode() != 200) {
                responseModel.setDescription(checkMemberExists.getDescription());
                responseModel.setStatusCode(checkMemberExists.getStatusCode());
                return responseModel;
            }
            
            int memSlotNo = (int) checkMemberExists.getData()
                    .get("slotNumber");
            
            String memEmailAdd = (String) checkMemberExists.getData()
                    .get("memberEmail");
            
            CheckIfMerIdExists rrSender = new CheckIfMerIdExists();
            rrSender.setEmailAddress(emailAddress);
            rrSender.setInvitationCodeReqId(rq.getInvitationCodeReqId());
            rrSender.setMemberId(memEmailAdd);
            rrSender.setMemberIdType(memEmailAdd);
            
            BaseResponse checkSenderExists = this.checkSenderDetailss(rrSender);
            if (checkSenderExists.getStatusCode() != 200) {
                responseModel.setDescription(checkSenderExists.getDescription());
                responseModel.setStatusCode(checkSenderExists.getStatusCode());
                return responseModel;
            }
            
            int senderSlotNo = (int) checkSenderExists.getData()
                    .get("slotNumber");
            
            String senderName = (String) checkMemberExists.getData()
                    .get("senderName");
            
            BaseResponse valRe = this.validateRequest(rqq);
            
            if (valRe.getStatusCode() != 200) {
                responseModel.setDescription(valRe.getDescription());
                responseModel.setStatusCode(valRe.getStatusCode());
                return responseModel;
            }
            
            sw.setCreatedDate(Instant.now());
            sw.setInvitationCodeReqId(rq.getInvitationCodeReqId());
            sw.setMemberId(rq.getMemberId());
            sw.setReceiverSlot(memSlotNo);
            sw.setSenderEmailAddress(emailAddress);
            sw.setSenderSlot(senderSlotNo);
            sw.setMemberType(rr.getMemberIdType());
            sw.setIsSlotSwapped("0");
            sw.setIsSlotSwappedStatus("Pending");
            sw.setReceiverEmailAddress(rr.getEmailAddress());
            sw.setSenderFullName(senderName);
            swapSlotDetailsRepo.save(sw);
            
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        responseModel.setDescription("Swap slot request sent successfully.");
        responseModel.setStatusCode(200);
        
        return responseModel;
    }
    
    public BaseResponse joinGroup(JoinGroupRequest rq, String channel, String auth) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured, please try again.";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String emailAddress = getDecoded.emailAddress;
            CheckIfMerIdExists rr = new CheckIfMerIdExists();

            // System.out.println("email from jwt" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + emailAddress);
            if (!rq.getMemberEmailAddress().equals(emailAddress)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Suspected fraud!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Suspected fraud!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            boolean isWalletId = true;
            boolean isPhonenUmber = false;
            
            if (!utilMeth.isValid10Num(rq.getMemberId().trim())) {
                isWalletId = false;
                isPhonenUmber = true;
                
            }

            /*if (!utilMeth.isValid11Num(rq.getMemberId())) {

                isPhonenUmber = false;

            }*/
            // System.out.println("isWalletId" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + isWalletId);
            if (isWalletId == false && isPhonenUmber == false) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Invalid MemberId!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Invalid MemberId!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
                
            }
            
            if (isWalletId) {
                
                List<RegWalletInfo> wallDe = regWalletInfoRepository.findByWalletIdList(rq.getMemberId());
                if (wallDe.size() <= 0) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "MemberId does not exist!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("MemberId does not exist!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                    
                }

                // rr.setEmailAddress(wallDe.get(0).getEmail());
                rr.setMemberId(rq.getMemberId());
                rr.setMemberIdType("WalletId");
            }
            
            if (isPhonenUmber) {
                
                List<RegWalletInfo> wallDe = regWalletInfoRepository.findByPhoneNumberData(rq.getMemberId());
                if (wallDe.size() <= 0) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "MemberId does not exist!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("MemberId does not exist!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                    
                }

                //rr.setEmailAddress(wallDe.get(0).getEmail());
                rr.setMemberId(rq.getMemberId());
                rr.setMemberIdType("Phonenumber");
                
            }
            
            List<RegWalletInfo> getWallDe = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);
            if (!getWallDe.get(0).isActivation()) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Customer has not created PIN!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setDescription("Customer has not created PIN!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            String encyrptedPin = utilMeth.encyrpt(String.valueOf(rq.getPin()), encryptionKey);
            String pin = getWallDe.get(0).getPersonId();
            if (!encyrptedPin.equals(pin)) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Invalid PIN!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setDescription("Invalid PIN!");
                responseModel.setStatusCode(statusCode);
                
                return responseModel;
                
            }
            
            rr.setEmailAddress(rq.getMemberEmailAddress());
            
            rr.setInvitationCodeReqId(rq.getInvitationCodeReqId());
            BaseResponse checkMemberExists = this.checkIfMerIdExistsInTheDetails(rr);
            if (checkMemberExists.getStatusCode() != 200) {
                responseModel.setDescription(checkMemberExists.getDescription());
                responseModel.setStatusCode(checkMemberExists.getStatusCode());
                return responseModel;
            }
            
            ValidateReqReq rqq = new ValidateReqReq();
            rqq.setAdminEmailAddress("");
            rqq.setInvitationCodeReqId(rq.getInvitationCodeReqId());
            rqq.setSenderEmailAddress(rq.getMemberEmailAddress());
            
            BaseResponse valRe = this.validateRequest(rqq);
            
            if (valRe.getStatusCode() != 200) {
                responseModel.setDescription(valRe.getDescription());
                responseModel.setStatusCode(valRe.getStatusCode());
                return responseModel;
            }
            
            boolean chkGDetailsBool = true;
            
            List<GroupSavingsData> getALLGROUPS = groupSavingsDataRepo.findByInviteCode(rq.getInvitationCodeReqId());
            if (getALLGROUPS.size() <= 0) {
                chkGDetailsBool = false;
                
            }
            if (chkGDetailsBool == false) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "There is no existing group!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("There is no existing group!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            GroupSavingsData getByInvite = groupSavingsDataRepo.findByInviteCodeDe(rq.getInvitationCodeReqId());
            
            for (GroupSavingsData getByCodeI : getALLGROUPS) {
                
                if (!getByCodeI.getIsTrnsactionDeleted().equals("1")) {
                    
                    if (getByCodeI.getAddedMembersModels() != null) {
                        
                        ObjectMapper mappergetByCode = new ObjectMapper();
                        List<AddMembersModels> memsgetByCode = mappergetByCode.readValue(getByCodeI.getAddedMembersModels(), new TypeReference<List<AddMembersModels>>() {
                        });
                        for (AddMembersModels getMemsByCode : memsgetByCode) {
                            
                            if (getMemsByCode.getMemberEmailAddress().equals(rq.getMemberEmailAddress())) {
                                getMemsByCode.setSlot(rq.getSelectedSlot());
                                getMemsByCode.setMemberJoined("1");
                                
                                String updatedJson = mappergetByCode.writeValueAsString(memsgetByCode);
                                getByInvite.setAddedMembersModels(updatedJson);
                                getByInvite.setLastModifiedDate(Instant.now());
                                boolean found = false;
                                int target = getMemsByCode.getSlot();
                                for (int n : getByInvite.getAvailablePayOutSlot()) {
                                    if (n == target) {
                                        found = true;
                                        break;
                                    }
                                }
                                
                                if (found == true) {
                                    System.out.println("found" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + found);
                                    
                                    int[] arrayT = getByInvite.getAvailablePayOutSlot();
                                    int[] availablePayOutSlot = removeNumberAndConvertToString(arrayT, rq.getSelectedSlot());
                                    getByInvite.setAvailablePayOutSlot(availablePayOutSlot);
                                    // Save back to DB
                                    groupSavingsDataRepo.save(getByInvite);
                                    
                                }
                                
                            }
                            
                        }
                        
                    }
                }
                
            }
            
            GroupSavingsData getagain = groupSavingsDataRepo.findByInviteCodeDe(rq.getInvitationCodeReqId());
            
            if (getagain.getAvailablePayOutSlot() != null || getagain.getAvailablePayOutSlot().length != 0) {
                System.out.println("getagain.getAvailablePayOutSlot() == null || getagain.getAvailablePayOutSlot().length == 0" + "  :::::::::::::::::::::   ");

                //set transactionStatus = 4
                //description: contributionInprogress
                // getagain.setTransactionStatus("4");
                getagain.setTransactionStatusDesc(getagain.getTransactionStatusDesc());
                getagain.setLastModifiedDate(Instant.now());
                groupSavingsDataRepo.save(getagain);
                
            }
            
            responseModel.setDescription("Member joined successfuly.");
            responseModel.setStatusCode(200);
            
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
    }
    
    public BaseResponse leaveGroup(LeaveGroupRequest rq, String channel, String auth) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occurred, please try again.";
        
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String emailAddress = getDecoded.emailAddress;
            
            if (!rq.getMemberEmailAddress().equals(emailAddress)) {
                settlementFailureLogRepo.save(new SettlementFailureLog("", "", "Suspected fraud!"));
                responseModel.setDescription("Suspected fraud!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            List<GroupSavingsData> groupList = groupSavingsDataRepo.findByInviteCode(rq.getInvitationCodeReqId());
            if (groupList.isEmpty()) {
                settlementFailureLogRepo.save(new SettlementFailureLog("", "", "Group not found!"));
                responseModel.setDescription("Group not found!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            List<RegWalletInfo> getWallDe = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);
            if (!getWallDe.get(0).isActivation()) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Customer has not created PIN!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setDescription("Customer has not created PIN!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            String encyrptedPin = utilMeth.encyrpt(String.valueOf(rq.getPin()), encryptionKey);
            String pin = getWallDe.get(0).getPersonId();
            if (!encyrptedPin.equals(pin)) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Invalid PIN!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setDescription("Invalid PIN!");
                responseModel.setStatusCode(statusCode);
                
                return responseModel;
                
            }
            
            GroupSavingsData group = groupSavingsDataRepo.findByInviteCodeDe(rq.getInvitationCodeReqId());
            if (group == null || "1".equals(group.getIsTrnsactionDeleted())) {
                responseModel.setDescription("Invalid or deleted group.");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            ObjectMapper mapper = new ObjectMapper();
            List<AddMembersModels> activeMembers = mapper.readValue(
                    group.getAddedMembersModels(), new TypeReference<List<AddMembersModels>>() {
            }
            );
            
            List<AddMembersModels> formerMembers = new ArrayList<>();
            if (group.getFormerMembersLog() != null) {
                formerMembers = mapper.readValue(
                        group.getFormerMembersLog(), new TypeReference<List<AddMembersModels>>() {
                }
                );
            }
            
            boolean removed = false;
            Integer slotToRestore = null;
            
            Iterator<AddMembersModels> iterator = activeMembers.iterator();
            while (iterator.hasNext()) {
                AddMembersModels member = iterator.next();
                if (member.getMemberEmailAddress().equals(rq.getMemberEmailAddress())) {
                    slotToRestore = member.getSlot();
                    formerMembers.add(member); // archive for reference
                    iterator.remove();
                    removed = true;
                    break;
                }
            }
            
            if (!removed) {
                responseModel.setDescription("Member not found in group.");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }

            // Restore payout slot
            if (slotToRestore != null) {
                int[] currentSlots = group.getAvailablePayOutSlot();
                int[] updatedSlots = Arrays.copyOf(currentSlots, currentSlots.length + 1);
                updatedSlots[updatedSlots.length - 1] = slotToRestore;
                group.setAvailablePayOutSlot(updatedSlots);
            }

            // Save updated member list and former member log
            group.setAddedMembersModels(mapper.writeValueAsString(activeMembers));
            group.setFormerMembersLog(mapper.writeValueAsString(formerMembers));
            group.setLastModifiedDate(Instant.now());
            
            groupSavingsDataRepo.save(group);
            
            responseModel.setDescription("Member left the group successfully.");
            responseModel.setStatusCode(200);
        } catch (Exception ex) {
            ex.printStackTrace();
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
        }
        
        return responseModel;
    }
    
    public ApiResponseModel getMemSwapSlotNotify(String auth) {
        ApiResponseModel responseModel = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            
            System.out.println("pageNation :::::::: " + "    ::::::::::::::::::::: " + returnPagenation());
            boolean chkGDetailsBool = true;
            
            List<SwapSlotDetails> getALLSwapDe = swapSlotDetailsRepo.findByReceiverEmailAddress(getDecoded.emailAddress);
            if (getALLSwapDe.size() <= 0) {
                chkGDetailsBool = false;
                
            }
            if (chkGDetailsBool == false) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "There is no awaiting Swpa notification!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("There is no awaiting swap notification!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            List<GetSwapSlotDetailsResponse> mapAll = new ArrayList<GetSwapSlotDetailsResponse>();
            GetSwapSlotDetailsResponse logC = new GetSwapSlotDetailsResponse();
            
            for (SwapSlotDetails getByCodeI : getALLSwapDe) {
                
                if (!getByCodeI.getIsSlotSwapped().equals("1")) {
                    if (getByCodeI.getIsSlotSwappedStatus().equals("Pending")) {
                        
                        logC.setInvitationCodeReqId(getByCodeI.getInvitationCodeReqId());
                        logC.setIsSlotSwappedStatus(getByCodeI.getIsSlotSwappedStatus());
                        logC.setMemberId(getByCodeI.getMemberId());
                        logC.setMemberType(getByCodeI.getMemberType());
                        logC.setNotificationDate(formDate(getByCodeI.getCreatedDate()));
                        logC.setReceiverEmailAddress(getByCodeI.getReceiverEmailAddress());
                        logC.setReceiverSlot(getByCodeI.getReceiverSlot());
                        logC.setSenderEmailAddress(getByCodeI.getSenderEmailAddress());
                        logC.setSenderFullName(getByCodeI.getSenderFullName());
                        logC.setSenderSlot(getByCodeI.getSenderSlot());
                        mapAll.add(logC);
                    }
                }
                
            }
            if (environment.equals("staging")) {
                System.out.println("getMemSwapSlotNotify::::mapAll :::::::: " + "    ::::::::::::::::::::: " + mapAll);
            }
            
            if (mapAll.isEmpty()) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "There are no awaiting notification for user!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("There are no awaiting notification for user!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            responseModel.setData(mapAll);
            responseModel.setDescription("Awaiting notification for User pulled successfully.");
            responseModel.setStatusCode(200);
            
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
        
    }
    
    public BaseResponse acceptDeclineSwap(AcceptDeclineSwapSlotReq rq, String channel, String auth) {
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured, please try again.";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            String emailAddress = getDecoded.emailAddress;

            // System.out.println("email from jwt" + "  ::::::::::::::::::::: >>>>>>>>>>>>>>>>>>  " + emailAddress);
            if (rq.getSenderEmailAddress().equals(getDecoded.emailAddress)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Suspected fraud!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Suspected fraud!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            List<SwapSlotDetails> getSw = swapSlotDetailsRepo.findBySenderEmailAddressAndMemberIdAndInvitationCodeReqId(rq.getSenderEmailAddress(), rq.getMemberId(), rq.getInvitationCodeReqId());
            if (getSw.size() > 0) {
                if (!getSw.get(0).getIsSlotSwappedStatus().equals("Pending")) {
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "You do not have awaiting Swap Slot with Sender!");
                    settlementFailureLogRepo.save(conWall);
                    responseModel.setDescription("You do not have awaiting Swap Slot with Sender!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
            } else if (getSw.size() <= 0) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "You do not have awaiting Swap Slot with Sender!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("You do not have awaiting Swap Slot with Sender!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            boolean isAccepted = true;
            
            if (rq.isAcceptOrDecline() == true) {
                List<RegWalletInfo> getWallDe = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);
                if (!getWallDe.get(0).isActivation()) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "Customer has not created PIN!");
                    settlementFailureLogRepo.save(conWall);
                    
                    responseModel.setDescription("Customer has not created PIN!");
                    responseModel.setStatusCode(statusCode);
                    return responseModel;
                }
                
                String encyrptedPin = utilMeth.encyrpt(String.valueOf(rq.getPin()), encryptionKey);
                String pin = getWallDe.get(0).getPersonId();
                if (!encyrptedPin.equals(pin)) {
                    
                    SettlementFailureLog conWall = new SettlementFailureLog("", "",
                            "Invalid PIN!");
                    settlementFailureLogRepo.save(conWall);
                    
                    responseModel.setDescription("Invalid PIN!");
                    responseModel.setStatusCode(statusCode);
                    
                    return responseModel;
                    
                }
                
            } else {
                isAccepted = false;
                
            }
            
            if (isAccepted == false) {
                //update table and return

                SwapSlotDetails getSwUp = swapSlotDetailsRepo.findBySenderEmailAddressAndMemberIdAndInvitationCodeReqIdUpdate(rq.getSenderEmailAddress(), rq.getMemberId(), rq.getInvitationCodeReqId());
                getSwUp.setIsSlotSwapped("0");
                getSwUp.setIsSlotSwappedStatus("Declined");
                getSwUp.setLastModifiedDate(Instant.now());
                swapSlotDetailsRepo.save(getSwUp);
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Customer declined Slot Swap!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setDescription("Customer declined Slot Swap!");
                responseModel.setStatusCode(statusCode);
                
                return responseModel;
                
            }

            //if accepted, swap slot
            boolean chkGDetailsBool = true;
            
            List<GroupSavingsData> getALLGROUPS = groupSavingsDataRepo.findByInviteCode(rq.getInvitationCodeReqId());
            if (getALLGROUPS.size() <= 0) {
                chkGDetailsBool = false;
                
            }
            if (chkGDetailsBool == false) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "There is no existing group!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("There is no existing group!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            GroupSavingsData getByInvite = groupSavingsDataRepo.findByInviteCodeDe(rq.getInvitationCodeReqId());
            
            for (GroupSavingsData getByCodeI : getALLGROUPS) {
                
                if (!getByCodeI.getIsTrnsactionDeleted().equals("1")) {
                    
                    if (getByCodeI.getAddedMembersModels() != null) {
                        
                        ObjectMapper mappergetByCode = new ObjectMapper();
                        List<AddMembersModels> memsgetByCode = mappergetByCode.readValue(getByCodeI.getAddedMembersModels(), new TypeReference<List<AddMembersModels>>() {
                        });
                        for (AddMembersModels getMemsByCode : memsgetByCode) {
                            
                            if (getMemsByCode.getMemberEmailAddress().equals(getSw.get(0).getReceiverEmailAddress())) {
                                getMemsByCode.setSlot(getSw.get(0).getSenderSlot());
                                
                                String updatedJson = mappergetByCode.writeValueAsString(memsgetByCode);
                                getByInvite.setAddedMembersModels(updatedJson);
                                getByInvite.setLastModifiedDate(Instant.now());

                                // Save back to DB
                                groupSavingsDataRepo.save(getByInvite);
                            }
                            
                            if (getMemsByCode.getMemberEmailAddress().equals(getSw.get(0).getSenderEmailAddress())) {
                                getMemsByCode.setSlot(getSw.get(0).getReceiverSlot());
                                String updatedJson = mappergetByCode.writeValueAsString(memsgetByCode);
                                getByInvite.setAddedMembersModels(updatedJson);
                                getByInvite.setLastModifiedDate(Instant.now());

                                // Save back to DB
                                groupSavingsDataRepo.save(getByInvite);
                            }
                            
                        }
                        
                    }
                }
                
            }
            
            SwapSlotDetails getSwUp = swapSlotDetailsRepo.findBySenderEmailAddressAndMemberIdAndInvitationCodeReqIdUpdate(rq.getSenderEmailAddress(), rq.getMemberId(), rq.getInvitationCodeReqId());
            getSwUp.setIsSlotSwapped("1");
            getSwUp.setIsSlotSwappedStatus("Accepted");
            getSwUp.setLastModifiedDate(Instant.now());
            swapSlotDetailsRepo.save(getSwUp);
            
            responseModel.setDescription("Slots have been swapped successfuly.");
            responseModel.setStatusCode(200);
            
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
    }
    
    public ApiResponseModel getAllTransactions(ReByEmailAddress rq, String channel, String auth) {
        ApiResponseModel responseModel = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            
            if (!rq.getEmailAddress().equals(getDecoded.emailAddress)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Suspected fraud!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Suspected fraud!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            if (environment.equals("staging")) {
                System.out.println("pageNation :::::::: " + "    ::::::::::::::::::::: " + returnPagenation());
            }
            //  List<KuleanPaymentTransaction> getKulTransPage = kuleanPaymentTransactionRepo.findByWalletNoListPage(getDecoded.phoneNumber, PageRequest.of(0, pageNation));
            boolean chkGDetailsBool = true;

            /* List<GroupSavingsData> chkGDetails = groupSavingsDataRepo.findByEmailAddressOrdered(rq.getEmailAddress());
             if (chkGDetails.size() <= 0) {
                chkGDetailsBool = false;

            }*/
            //get groups from aaded to groups
            List<GroupSavingsData> getALLGROUPS = groupSavingsDataRepo.findAll();
            
            if (getALLGROUPS.size() <= 0) {
                chkGDetailsBool = false;
                
            }
            if (chkGDetailsBool == false) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "There is no existing group!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("There is no existing group!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            List<SavedGroupDetails> mapAll = new ArrayList<SavedGroupDetails>();
            
            for (GroupSavingsData getAlG : getALLGROUPS) {
                
                if (!getAlG.getIsTrnsactionDeleted().equals("1")) {
                    if (!getAlG.getTransactionStatus().equals("1")) {
                        
                        String addedMemModel = getAlG.getAddedMembersModels();
                        
                        String addedMemInvCode = null;
                        
                        if (getAlG.getEmailAddress().equals(rq.getEmailAddress())) {
                            
                            SavedGroupDetails logC = new SavedGroupDetails();
                            
                            logC.setAllowPublicToJoin(getAlG.getAllowPublicToJoin());
                            logC.setGroupSavingAmount(getAlG.getGroupSavingAmount());
                            logC.setGroupSavingDescription(getAlG.getGroupSavingDescription());
                            logC.setGroupSavingFinalAmount(getAlG.getGroupSavingFinalAmount());
                            logC.setGroupSavingName(getAlG.getGroupSavingName());
                            logC.setInviteCode(getAlG.getInviteCode());
                            logC.setTransactionIdLink(getAlG.getTransactionIdLink());
                            logC.setIsTrnsactionDeleted(getAlG.getIsTrnsactionDeleted());
                            logC.setNumberOfMembers(getAlG.getNumberOfMembers());
                            logC.setPayOutDateOfTheMonth(getAlG.getPayOutDateOfTheMonth());
                            logC.setAdminPayOutSlot(returnAraryumberOfSlots(getAlG.getAdminPayOutSlot()));
                            //String[] array = getAlG.getAvailablePayOutSlot().split(",");
                            logC.setAvailablePayOutSlot(getAlG.getAvailablePayOutSlot());
                            
                            ObjectMapper mapper = new ObjectMapper();
                            if (getAlG.getAddedMembersModels() != null) {
                                List<AddMembersModels> mems = mapper.readValue(getAlG.getAddedMembersModels(), new TypeReference<List<AddMembersModels>>() {
                                });
                                List<AddMembersModels> addMee = new ArrayList<>();
                                for (AddMembersModels getMems : mems) {
                                    AddMembersModels adM = new AddMembersModels();
                                    adM.setAdminEmailAddress(getMems.getAdminEmailAddress());
                                    adM.setIsAdmin(getMems.getIsAdmin());
                                    adM.setInvitationCodeReqId(getMems.getInvitationCodeReqId() == null ? "empty" : getMems.getInvitationCodeReqId());
                                    adM.setMemberId(getMems.getMemberId());
                                    adM.setMemberEmailAddress(getMems.getMemberEmailAddress());
                                    adM.setMemberIdType(getMems.getMemberIdType());
                                    adM.setMemberName(getMems.getMemberName());
                                    adM.setMemberUserId(getMems.getMemberUserId());
                                    adM.setSlot(getMems.getSlot());
                                    adM.setMemberJoined(getMems.getMemberJoined());
                                    adM.setPayOutStatus(getMems.getPayOutStatus() == null ? "empty" : getMems.getPayOutStatus());
                                    adM.setPayOutStatusDesc(getMems.getPayOutStatusDesc() == null ? "empty" : getMems.getPayOutStatusDesc());
                                    adM.setAmountToContribute(getMems.getAmountToContribute());
                                    adM.setCurrentMonthContribution(getMems.getCurrentMonthContribution());
                                    adM.setCurrentMonthContributionStatusDesc(getMems.getCurrentMonthContributionStatusDesc() == null ? "empty" : getMems.getCurrentMonthContributionStatusDesc());
                                    adM.setCurrentMonthContributionStatusId(getMems.getCurrentMonthContributionStatusId() == null ? "empty" : getMems.getCurrentMonthContributionStatusId());
                                    adM.setTotalMonthlyContribution(getMems.getTotalMonthlyContribution());
                                    SwapSlot swP = new SwapSlot();
                                    swP.setIsSwapActive(getMems.getSwapSlot().getIsSwapActive());
                                    swP.setIsSwapped(getMems.getSwapSlot().getIsSwapped());
                                    swP.setDateSwapped(getMems.getSwapSlot().getDateSwapped() == null ? "" : getMems.getSwapSlot().getDateSwapped());
                                    swP.setReceiverId(getMems.getSwapSlot().getReceiverId() == null ? "" : getMems.getSwapSlot().getReceiverId());
                                    swP.setReceiverSlot(getMems.getSwapSlot().getReceiverSlot() == 0 ? 0 : getMems.getSwapSlot().getReceiverSlot());
                                    swP.setSenderId(getMems.getSwapSlot().getSenderId() == null ? "" : getMems.getSwapSlot().getSenderId());
                                    swP.setSenderSlot(getMems.getSwapSlot().getSenderSlot() == 0 ? 0 : getMems.getSwapSlot().getSenderSlot());
                                    adM.setSwapSlot(swP);
                                    
                                    addMee.add(adM);
                                    
                                }
                                logC.setAddMembersModels(addMee);
                            }

                            //logC.setTransactionId(getKul.getTransactionId());
                            logC.setTransactionStatus(getAlG.getTransactionStatus());
                            logC.setTransactionStatusDesc(getAlG.getTransactionStatusDesc());
                            
                            if (getAlG.getLastModifiedDate() == null) {
                                logC.setTransactionDate(formDate(getAlG.getCreatedDate()));
                            } else {
                                
                                logC.setTransactionDate(formDate(getAlG.getLastModifiedDate()));
                            }
                            
                            mapAll.add(logC);
                            
                        }
                        
                        if (getAlG.getAddedMembersModels() != null) {
                            
                            String jsonInput = getAlG.getAddedMembersModels();
                            
                            JsonElement jsonElement = JsonParser.parseString(jsonInput);
                            
                            String excludeEmail = rq.getEmailAddress(); // the email to exclude
                            boolean emailExists = false;

                            // Step 1: Check if any object has the email
                            if (jsonElement.isJsonArray()) {
                                JsonArray jsonArray = jsonElement.getAsJsonArray();
                                for (JsonElement element : jsonArray) {
                                    JsonObject member = element.getAsJsonObject();

                                    // Now safely access fields
                                    // JsonElement emailElement = obj.get("memberEmailAddress");
                                    // String email = (emailElement != null && !emailElement.isJsonNull()) ? emailElement.getAsString() : null;
                                    if (member.has("memberEmailAddress") && !member.get("memberEmailAddress").isJsonNull()) {
                                        // ... handle the rest
                                        String email = member.get("memberEmailAddress").getAsString();
                                        if (excludeEmail.equalsIgnoreCase(email)) {
                                            emailExists = true;
                                            break;
                                        }
                                    }
                                }
                                
                            } else {
                                JsonObject root = JsonParser.parseString(jsonInput).getAsJsonObject();
                                JsonArray membersArray = root.getAsJsonArray("addMembersModels");
                                
                                for (JsonElement element : membersArray) {
                                    JsonObject member = element.getAsJsonObject();
                                    if (member.has("memberEmailAddress") && !member.get("memberEmailAddress").isJsonNull()) {
                                        String email = member.get("memberEmailAddress").getAsString();
                                        if (excludeEmail.equalsIgnoreCase(email)) {
                                            emailExists = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            
                            ObjectMapper mapper = new ObjectMapper();
                            List<AddMembersModels> mems = mapper.readValue(getAlG.getAddedMembersModels(), new TypeReference<List<AddMembersModels>>() {
                            });
                            for (AddMembersModels getMems : mems) {
                                System.out.println("emailExists :::::::: " + " " + emailExists);
                                
                                if (emailExists == true) {
                                    
                                    boolean existsreqId = false;
                                    
                                    addedMemInvCode = getMems.getInvitationCodeReqId();
                                    System.out.println("addedMemInvCode :::::::: " + " " + addedMemInvCode);

                                    // }
                                    for (SavedGroupDetails item : mapAll) {
                                        if (environment.equals("staging")) {
                                            System.out.println("item.getInviteCode() :::::::: " + " " + item.getInviteCode());
                                        }
                                        
                                        if (item.getInviteCode() != null && item.getInviteCode().equals(addedMemInvCode)) {
                                            existsreqId = true;
                                            break;
                                        }
                                    }
                                    System.out.println("existsreqId :::::::: " + " " + existsreqId);
                                    
                                    if (existsreqId == true) {
                                        List<GroupSavingsData> getByCode = groupSavingsDataRepo.findByInviteCode(addedMemInvCode);
                                        
                                        if (getByCode.size() > 0) {
                                            System.out.println("getByCode EXISTS :::::::: " + " " + getByCode);
                                            
                                            for (GroupSavingsData getByCodeI : getByCode) {
                                                SavedGroupDetails logC = new SavedGroupDetails();

                                                //String[] array = getByCodeI.getAvailablePayOutSlot().split(",");
                                                logC.setAvailablePayOutSlot(getByCodeI.getAvailablePayOutSlot());
                                                ObjectMapper mappergetByCode = new ObjectMapper();
                                                List<AddMembersModels> memsgetByCode = mappergetByCode.readValue(getByCodeI.getAddedMembersModels(), new TypeReference<List<AddMembersModels>>() {
                                                });
                                                List<AddMembersModels> addMeegetByCode = new ArrayList<>();
                                                
                                                for (AddMembersModels getMemsByCode : memsgetByCode) {
                                                    System.out.println("getMemsByCode.getMemberEmailAddress() EXISTS :::::::: " + " " + getMemsByCode.getMemberEmailAddress());
                                                    
                                                    if (!getDecoded.emailAddress.equals(getMemsByCode.getMemberEmailAddress())) {
                                                        
                                                        System.out.println("getDecoded.emailAddress :::::::: " + " " + getDecoded.emailAddress);
                                                        System.out.println("getMemsByCode.getMemberEmailAddress() :::::::: " + " " + getMemsByCode.getMemberEmailAddress());
                                                        
                                                        logC.setAllowPublicToJoin(getByCodeI.getAllowPublicToJoin());
                                                        logC.setGroupSavingAmount(getByCodeI.getGroupSavingAmount());
                                                        logC.setGroupSavingDescription(getByCodeI.getGroupSavingDescription());
                                                        logC.setGroupSavingFinalAmount(getByCodeI.getGroupSavingFinalAmount());
                                                        logC.setGroupSavingName(getByCodeI.getGroupSavingName());
                                                        logC.setInviteCode(getByCodeI.getInviteCode());
                                                        logC.setTransactionIdLink(getByCodeI.getTransactionIdLink());
                                                        logC.setIsTrnsactionDeleted(getByCodeI.getIsTrnsactionDeleted());
                                                        logC.setNumberOfMembers(getByCodeI.getNumberOfMembers());
                                                        logC.setPayOutDateOfTheMonth(getByCodeI.getPayOutDateOfTheMonth());
                                                        logC.setAdminPayOutSlot(returnAraryumberOfSlots(getByCodeI.getAdminPayOutSlot()));
                                                        
                                                        AddMembersModels adM = new AddMembersModels();
                                                        adM.setInvitationCodeReqId(getMemsByCode.getInvitationCodeReqId());
                                                        adM.setMemberId(getMemsByCode.getMemberId());
                                                        //adM.setAdminEmailAddress(getMems.getAdminEmailAddress());
                                                        adM.setIsAdmin("0");
                                                        
                                                        adM.setMemberEmailAddress(getMemsByCode.getMemberEmailAddress());
                                                        adM.setMemberIdType(getMemsByCode.getMemberIdType());
                                                        adM.setMemberName(getMemsByCode.getMemberName());
                                                        adM.setMemberUserId(getMemsByCode.getMemberUserId());
                                                        adM.setSlot(getMemsByCode.getSlot());
                                                        adM.setMemberJoined(getMemsByCode.getMemberJoined());
                                                        adM.setPayOutStatus(getMemsByCode.getPayOutStatus() == null ? "empty" : getMemsByCode.getPayOutStatus());
                                                        adM.setPayOutStatusDesc(getMemsByCode.getPayOutStatusDesc() == null ? "empty" : getMemsByCode.getPayOutStatusDesc());
                                                        adM.setAmountToContribute(getMemsByCode.getAmountToContribute());
                                                        adM.setCurrentMonthContribution(getMemsByCode.getCurrentMonthContribution());
                                                        adM.setCurrentMonthContributionStatusDesc(getMemsByCode.getCurrentMonthContributionStatusDesc() == null ? "empty" : getMemsByCode.getCurrentMonthContributionStatusDesc());
                                                        adM.setCurrentMonthContributionStatusId(getMemsByCode.getCurrentMonthContributionStatusId() == null ? "empty" : getMemsByCode.getCurrentMonthContributionStatusId());
                                                        adM.setTotalMonthlyContribution(getMemsByCode.getTotalMonthlyContribution());
                                                        SwapSlot swP = new SwapSlot();
                                                        swP.setIsSwapActive(getMemsByCode.getSwapSlot().getIsSwapActive());
                                                        swP.setIsSwapped(getMemsByCode.getSwapSlot().getIsSwapped());
                                                        swP.setDateSwapped(getMemsByCode.getSwapSlot().getDateSwapped() == null ? "" : getMemsByCode.getSwapSlot().getDateSwapped());
                                                        swP.setReceiverId(getMemsByCode.getSwapSlot().getReceiverId() == null ? "" : getMemsByCode.getSwapSlot().getReceiverId());
                                                        swP.setReceiverSlot(getMemsByCode.getSwapSlot().getReceiverSlot() == 0 ? 0 : getMemsByCode.getSwapSlot().getReceiverSlot());
                                                        swP.setSenderId(getMemsByCode.getSwapSlot().getSenderId() == null ? "" : getMemsByCode.getSwapSlot().getSenderId());
                                                        swP.setSenderSlot(getMemsByCode.getSwapSlot().getSenderSlot() == 0 ? 0 : getMemsByCode.getSwapSlot().getSenderSlot());
                                                        adM.setSwapSlot(swP);
                                                        
                                                        addMeegetByCode.add(adM);
                                                    }
                                                    
                                                }
                                                
                                                logC.setAddMembersModels(addMeegetByCode);

                                                //logC.setTransactionId(getKul.getTransactionId());
                                                logC.setTransactionStatus(getByCodeI.getTransactionStatus());
                                                logC.setTransactionStatusDesc(getByCodeI.getTransactionStatusDesc());
                                                
                                                if (getByCodeI.getLastModifiedDate() == null) {
                                                    logC.setTransactionDate(formDate(getByCodeI.getCreatedDate()));
                                                } else {
                                                    
                                                    logC.setTransactionDate(formDate(getByCodeI.getLastModifiedDate()));
                                                }
                                                
                                                mapAll.add(logC);
                                            }
                                            
                                        }
                                    }
                                    
                                }
                                
                            }
                            
                        }
                    }
                }
                
            }
            
            if (mapAll.isEmpty()) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Customer does not have an existing group!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Customer does not have an existing group!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            responseModel.setData(mapAll);
            responseModel.setDescription("Customer transactions pulled successfully.");
            responseModel.setStatusCode(200);
            
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
        
    }
    
    public ApiResponseModel getAllTransactionsTEST(ReByEmailAddress rq, String channel, String auth) {
        ApiResponseModel responseModel = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            
            if (!rq.getEmailAddress().equals(getDecoded.emailAddress)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Suspected fraud!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Suspected fraud!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            System.out.println("pageNation :::::::: " + "    ::::::::::::::::::::: " + returnPagenation());
            //  List<KuleanPaymentTransaction> getKulTransPage = kuleanPaymentTransactionRepo.findByWalletNoListPage(getDecoded.phoneNumber, PageRequest.of(0, pageNation));
            boolean chkGDetailsBool = true;

            /* List<GroupSavingsData> chkGDetails = groupSavingsDataRepo.findByEmailAddressOrdered(rq.getEmailAddress());
             if (chkGDetails.size() <= 0) {
                chkGDetailsBool = false;

            }*/
            //get groups from aaded to groups
            List<GroupSavingsData> getALLGROUPS = groupSavingsDataRepo.findAll();
            
            if (getALLGROUPS.size() <= 0) {
                chkGDetailsBool = false;
                
            }
            if (chkGDetailsBool == false) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "There is no existing group!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("There is no existing group!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            List<SavedGroupDetails> mapAll = new ArrayList<SavedGroupDetails>();
            
            for (GroupSavingsData getAlG : getALLGROUPS) {
                
                if (!getAlG.getIsTrnsactionDeleted().equals("1")) {
                    if (!getAlG.getTransactionStatus().equals("1")) {
                        
                        String addedMemModel = getAlG.getAddedMembersModels();
                        
                        String addedMemInvCode = null;
                        
                        if (getAlG.getEmailAddress().equals(rq.getEmailAddress())) {
                            
                            SavedGroupDetails logC = new SavedGroupDetails();
                            
                            logC.setAllowPublicToJoin(getAlG.getAllowPublicToJoin());
                            logC.setGroupSavingAmount(getAlG.getGroupSavingAmount());
                            logC.setGroupSavingDescription(getAlG.getGroupSavingDescription());
                            logC.setGroupSavingFinalAmount(getAlG.getGroupSavingFinalAmount());
                            logC.setGroupSavingName(getAlG.getGroupSavingName());
                            logC.setInviteCode(getAlG.getInviteCode());
                            logC.setTransactionIdLink(getAlG.getTransactionIdLink());
                            logC.setIsTrnsactionDeleted(getAlG.getIsTrnsactionDeleted());
                            logC.setNumberOfMembers(getAlG.getNumberOfMembers());
                            logC.setPayOutDateOfTheMonth(getAlG.getPayOutDateOfTheMonth());
                            logC.setAdminPayOutSlot(returnAraryumberOfSlots(getAlG.getAdminPayOutSlot()));
                            //String[] array = getAlG.getAvailablePayOutSlot().split(",");
                            logC.setAvailablePayOutSlot(getAlG.getAvailablePayOutSlot());
                            
                            ObjectMapper mapper = new ObjectMapper();
                            if (getAlG.getAddedMembersModels() != null) {
                                List<AddMembersModels> mems = mapper.readValue(getAlG.getAddedMembersModels(), new TypeReference<List<AddMembersModels>>() {
                                });
                                List<AddMembersModels> addMee = new ArrayList<>();
                                for (AddMembersModels getMems : mems) {
                                    AddMembersModels adM = new AddMembersModels();
                                    adM.setAdminEmailAddress(getMems.getAdminEmailAddress());
                                    adM.setIsAdmin(getMems.getIsAdmin());
                                    adM.setInvitationCodeReqId(getMems.getInvitationCodeReqId() == null ? "empty" : getMems.getInvitationCodeReqId());
                                    adM.setMemberId(getMems.getMemberId());
                                    adM.setMemberEmailAddress(getMems.getMemberEmailAddress());
                                    adM.setMemberIdType(getMems.getMemberIdType());
                                    adM.setMemberName(getMems.getMemberName());
                                    adM.setMemberUserId(getMems.getMemberUserId());
                                    adM.setSlot(getMems.getSlot());
                                    adM.setMemberJoined(getMems.getMemberJoined());
                                    adM.setPayOutStatus(getMems.getPayOutStatus() == null ? "empty" : getMems.getPayOutStatus());
                                    adM.setPayOutStatusDesc(getMems.getPayOutStatusDesc() == null ? "empty" : getMems.getPayOutStatusDesc());
                                    adM.setAmountToContribute(getMems.getAmountToContribute());
                                    adM.setCurrentMonthContribution(getMems.getCurrentMonthContribution());
                                    adM.setCurrentMonthContributionStatusDesc(getMems.getCurrentMonthContributionStatusDesc() == null ? "empty" : getMems.getCurrentMonthContributionStatusDesc());
                                    adM.setCurrentMonthContributionStatusId(getMems.getCurrentMonthContributionStatusId() == null ? "empty" : getMems.getCurrentMonthContributionStatusId());
                                    adM.setTotalMonthlyContribution(getMems.getTotalMonthlyContribution());
                                    SwapSlot swP = new SwapSlot();
                                    swP.setIsSwapActive(getMems.getSwapSlot().getIsSwapActive());
                                    swP.setIsSwapped(getMems.getSwapSlot().getIsSwapped());
                                    swP.setDateSwapped(getMems.getSwapSlot().getDateSwapped() == null ? "" : getMems.getSwapSlot().getDateSwapped());
                                    swP.setReceiverId(getMems.getSwapSlot().getReceiverId() == null ? "" : getMems.getSwapSlot().getReceiverId());
                                    swP.setReceiverSlot(getMems.getSwapSlot().getReceiverSlot() == 0 ? 0 : getMems.getSwapSlot().getReceiverSlot());
                                    swP.setSenderId(getMems.getSwapSlot().getSenderId() == null ? "" : getMems.getSwapSlot().getSenderId());
                                    swP.setSenderSlot(getMems.getSwapSlot().getSenderSlot() == 0 ? 0 : getMems.getSwapSlot().getSenderSlot());
                                    adM.setSwapSlot(swP);
                                    
                                    addMee.add(adM);
                                    
                                }
                                logC.setAddMembersModels(addMee);
                            }

                            //logC.setTransactionId(getKul.getTransactionId());
                            logC.setTransactionStatus(getAlG.getTransactionStatus());
                            logC.setTransactionStatusDesc(getAlG.getTransactionStatusDesc());
                            
                            if (getAlG.getLastModifiedDate() == null) {
                                logC.setTransactionDate(formDate(getAlG.getCreatedDate()));
                            } else {
                                
                                logC.setTransactionDate(formDate(getAlG.getLastModifiedDate()));
                            }
                            
                            mapAll.add(logC);
                            
                        } else {
                            
                            System.out.println("getAlG.getAddedMembersModels() ::::::::::::::::  %S  " + new Gson().toJson(getAlG.getAddedMembersModels()));
                            String jsonInput = getAlG.getAddedMembersModels() == null ? "" : getAlG.getAddedMembersModels();
                            if (!jsonInput.equals("")) {
                                
                                JsonElement jsonElement = JsonParser.parseString(jsonInput);
                                
                                String excludeEmail = rq.getEmailAddress(); // the email to exclude
                                boolean emailExists = false;

                                // Step 1: Check if any object has the email
                                if (jsonElement.isJsonArray()) {
                                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                                    for (JsonElement element : jsonArray) {
                                        JsonObject member = element.getAsJsonObject();

                                        // Now safely access fields
                                        // JsonElement emailElement = obj.get("memberEmailAddress");
                                        // String email = (emailElement != null && !emailElement.isJsonNull()) ? emailElement.getAsString() : null;
                                        if (member.has("memberEmailAddress") && !member.get("memberEmailAddress").isJsonNull()) {
                                            // ... handle the rest
                                            String email = member.get("memberEmailAddress").getAsString();
                                            if (excludeEmail.equalsIgnoreCase(email)) {
                                                emailExists = true;
                                                break;
                                            }
                                        }
                                    }
                                    
                                } else {
                                    JsonObject root = JsonParser.parseString(jsonInput).getAsJsonObject();
                                    JsonArray membersArray = root.getAsJsonArray("addMembersModels");
                                    
                                    for (JsonElement element : membersArray) {
                                        JsonObject member = element.getAsJsonObject();
                                        if (member.has("memberEmailAddress") && !member.get("memberEmailAddress").isJsonNull()) {
                                            String email = member.get("memberEmailAddress").getAsString();
                                            if (excludeEmail.equalsIgnoreCase(email)) {
                                                emailExists = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                                System.out.println("emailExists :::::::: " + " " + emailExists);
                                
                                if (emailExists == true) {
                                    SavedGroupDetails logC = new SavedGroupDetails();
                                    
                                    logC.setAllowPublicToJoin(getAlG.getAllowPublicToJoin());
                                    logC.setGroupSavingAmount(getAlG.getGroupSavingAmount());
                                    logC.setGroupSavingDescription(getAlG.getGroupSavingDescription());
                                    logC.setGroupSavingFinalAmount(getAlG.getGroupSavingFinalAmount());
                                    logC.setGroupSavingName(getAlG.getGroupSavingName());
                                    logC.setInviteCode(getAlG.getInviteCode());
                                    logC.setTransactionIdLink(getAlG.getTransactionIdLink());
                                    logC.setIsTrnsactionDeleted(getAlG.getIsTrnsactionDeleted());
                                    logC.setNumberOfMembers(getAlG.getNumberOfMembers());
                                    logC.setPayOutDateOfTheMonth(getAlG.getPayOutDateOfTheMonth());
                                    logC.setAdminPayOutSlot(returnAraryumberOfSlots(getAlG.getAdminPayOutSlot()));
                                    //String[] array = getAlG.getAvailablePayOutSlot().split(",");
                                    logC.setAvailablePayOutSlot(getAlG.getAvailablePayOutSlot());
                                    
                                    ObjectMapper mapper = new ObjectMapper();
                                    if (getAlG.getAddedMembersModels() != null) {
                                        List<AddMembersModels> mems = mapper.readValue(getAlG.getAddedMembersModels(), new TypeReference<List<AddMembersModels>>() {
                                        });
                                        List<AddMembersModels> addMee = new ArrayList<>();
                                        for (AddMembersModels getMems : mems) {
                                            AddMembersModels adM = new AddMembersModels();
                                            adM.setAdminEmailAddress(getMems.getAdminEmailAddress());
                                            adM.setIsAdmin(getMems.getIsAdmin());
                                            adM.setInvitationCodeReqId(getMems.getInvitationCodeReqId() == null ? "empty" : getMems.getInvitationCodeReqId());
                                            adM.setMemberId(getMems.getMemberId());
                                            adM.setMemberEmailAddress(getMems.getMemberEmailAddress());
                                            adM.setMemberIdType(getMems.getMemberIdType());
                                            adM.setMemberName(getMems.getMemberName());
                                            adM.setMemberUserId(getMems.getMemberUserId());
                                            adM.setSlot(getMems.getSlot());
                                            adM.setMemberJoined(getMems.getMemberJoined());
                                            adM.setPayOutStatus(getMems.getPayOutStatus() == null ? "empty" : getMems.getPayOutStatus());
                                            adM.setPayOutStatusDesc(getMems.getPayOutStatusDesc() == null ? "empty" : getMems.getPayOutStatusDesc());
                                            adM.setAmountToContribute(getMems.getAmountToContribute());
                                            adM.setCurrentMonthContribution(getMems.getCurrentMonthContribution());
                                            adM.setCurrentMonthContributionStatusDesc(getMems.getCurrentMonthContributionStatusDesc() == null ? "empty" : getMems.getCurrentMonthContributionStatusDesc());
                                            adM.setCurrentMonthContributionStatusId(getMems.getCurrentMonthContributionStatusId() == null ? "empty" : getMems.getCurrentMonthContributionStatusId());
                                            adM.setTotalMonthlyContribution(getMems.getTotalMonthlyContribution());
                                            SwapSlot swP = new SwapSlot();
                                            swP.setIsSwapActive(getMems.getSwapSlot().getIsSwapActive());
                                            swP.setIsSwapped(getMems.getSwapSlot().getIsSwapped());
                                            swP.setDateSwapped(getMems.getSwapSlot().getDateSwapped() == null ? "" : getMems.getSwapSlot().getDateSwapped());
                                            swP.setReceiverId(getMems.getSwapSlot().getReceiverId() == null ? "" : getMems.getSwapSlot().getReceiverId());
                                            swP.setReceiverSlot(getMems.getSwapSlot().getReceiverSlot() == 0 ? 0 : getMems.getSwapSlot().getReceiverSlot());
                                            swP.setSenderId(getMems.getSwapSlot().getSenderId() == null ? "" : getMems.getSwapSlot().getSenderId());
                                            swP.setSenderSlot(getMems.getSwapSlot().getSenderSlot() == 0 ? 0 : getMems.getSwapSlot().getSenderSlot());
                                            adM.setSwapSlot(swP);
                                            
                                            addMee.add(adM);
                                            
                                        }
                                        logC.setAddMembersModels(addMee);
                                    }

                                    //logC.setTransactionId(getKul.getTransactionId());
                                    logC.setTransactionStatus(getAlG.getTransactionStatus());
                                    logC.setTransactionStatusDesc(getAlG.getTransactionStatusDesc());
                                    
                                    if (getAlG.getLastModifiedDate() == null) {
                                        logC.setTransactionDate(formDate(getAlG.getCreatedDate()));
                                    } else {
                                        
                                        logC.setTransactionDate(formDate(getAlG.getLastModifiedDate()));
                                    }
                                    
                                    mapAll.add(logC);
                                    
                                }
                            }
                        }

                        /* if (getAlG.getAddedMembersModels() != null) {

                            String jsonInput = getAlG.getAddedMembersModels();

                            JsonElement jsonElement = JsonParser.parseString(jsonInput);

                            String excludeEmail = rq.getEmailAddress(); // the email to exclude
                            boolean emailExists = false;

                            // Step 1: Check if any object has the email
                            if (jsonElement.isJsonArray()) {
                                JsonArray jsonArray = jsonElement.getAsJsonArray();
                                for (JsonElement element : jsonArray) {
                                    JsonObject member = element.getAsJsonObject();

                                    // Now safely access fields
                                    // JsonElement emailElement = obj.get("memberEmailAddress");
                                    // String email = (emailElement != null && !emailElement.isJsonNull()) ? emailElement.getAsString() : null;
                                    if (member.has("memberEmailAddress") && !member.get("memberEmailAddress").isJsonNull()) {
                                        // ... handle the rest
                                        String email = member.get("memberEmailAddress").getAsString();
                                        if (excludeEmail.equalsIgnoreCase(email)) {
                                            emailExists = true;
                                            break;
                                        }
                                    }
                                }

                            } else {
                                JsonObject root = JsonParser.parseString(jsonInput).getAsJsonObject();
                                JsonArray membersArray = root.getAsJsonArray("addMembersModels");

                                for (JsonElement element : membersArray) {
                                    JsonObject member = element.getAsJsonObject();
                                    if (member.has("memberEmailAddress") && !member.get("memberEmailAddress").isJsonNull()) {
                                        String email = member.get("memberEmailAddress").getAsString();
                                        if (excludeEmail.equalsIgnoreCase(email)) {
                                            emailExists = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            ObjectMapper mapper = new ObjectMapper();
                            List<AddMembersModels> mems = mapper.readValue(getAlG.getAddedMembersModels(), new TypeReference<List<AddMembersModels>>() {
                            });
                            for (AddMembersModels getMems : mems) {
                                System.out.println("emailExists :::::::: " + " " + emailExists);

                                if (emailExists == true) {

                                    boolean existsreqId = false;

                                    addedMemInvCode = getMems.getInvitationCodeReqId();
                                    System.out.println("addedMemInvCode :::::::: " + " " + addedMemInvCode);

                                    // }
                                    for (SavedGroupDetails item : mapAll) {
                                        System.out.println("item.getInviteCode() :::::::: " + " " + item.getInviteCode());

                                        if (item.getInviteCode() != null && item.getInviteCode().equals(addedMemInvCode)) {
                                            existsreqId = true;
                                            break;
                                        }
                                    }
                                    System.out.println("existsreqId :::::::: " + " " + existsreqId);

                                    if (existsreqId == true) {
                                        List<GroupSavingsData> getByCode = groupSavingsDataRepo.findByInviteCode(addedMemInvCode);

                                        if (getByCode.size() > 0) {
                                            System.out.println("getByCode EXISTS :::::::: " + " " + getByCode);

                                            for (GroupSavingsData getByCodeI : getByCode) {
                                                SavedGroupDetails logC = new SavedGroupDetails();

                                                //String[] array = getByCodeI.getAvailablePayOutSlot().split(",");
                                                logC.setAvailablePayOutSlot(getByCodeI.getAvailablePayOutSlot());
                                                ObjectMapper mappergetByCode = new ObjectMapper();
                                                List<AddMembersModels> memsgetByCode = mappergetByCode.readValue(getByCodeI.getAddedMembersModels(), new TypeReference<List<AddMembersModels>>() {
                                                });
                                                List<AddMembersModels> addMeegetByCode = new ArrayList<>();

                                                for (AddMembersModels getMemsByCode : memsgetByCode) {
                                                    System.out.println("getMemsByCode.getMemberEmailAddress() EXISTS :::::::: " + " " + getMemsByCode.getMemberEmailAddress());

                                                    if (!getDecoded.emailAddress.equals(getMemsByCode.getMemberEmailAddress())) {

                                                        System.out.println("getDecoded.emailAddress :::::::: " + " " + getDecoded.emailAddress);
                                                        System.out.println("getMemsByCode.getMemberEmailAddress() :::::::: " + " " + getMemsByCode.getMemberEmailAddress());

                                                        logC.setAllowPublicToJoin(getByCodeI.getAllowPublicToJoin());
                                                        logC.setGroupSavingAmount(getByCodeI.getGroupSavingAmount());
                                                        logC.setGroupSavingDescription(getByCodeI.getGroupSavingDescription());
                                                        logC.setGroupSavingFinalAmount(getByCodeI.getGroupSavingFinalAmount());
                                                        logC.setGroupSavingName(getByCodeI.getGroupSavingName());
                                                        logC.setInviteCode(getByCodeI.getInviteCode());
                                                        logC.setTransactionIdLink(getByCodeI.getTransactionIdLink());
                                                        logC.setIsTrnsactionDeleted(getByCodeI.getIsTrnsactionDeleted());
                                                        logC.setNumberOfMembers(getByCodeI.getNumberOfMembers());
                                                        logC.setPayOutDateOfTheMonth(getByCodeI.getPayOutDateOfTheMonth());
                                                        logC.setAdminPayOutSlot(returnAraryumberOfSlots(getByCodeI.getAdminPayOutSlot()));

                                                        AddMembersModels adM = new AddMembersModels();
                                                        adM.setInvitationCodeReqId(getMemsByCode.getInvitationCodeReqId());
                                                        adM.setMemberId(getMemsByCode.getMemberId());
                                                        //adM.setAdminEmailAddress(getMems.getAdminEmailAddress());
                                                        adM.setIsAdmin("0");

                                                        adM.setMemberEmailAddress(getMemsByCode.getMemberEmailAddress());
                                                        adM.setMemberIdType(getMemsByCode.getMemberIdType());
                                                        adM.setMemberName(getMemsByCode.getMemberName());
                                                        adM.setMemberUserId(getMemsByCode.getMemberUserId());
                                                        adM.setSlot(getMemsByCode.getSlot());
                                                        adM.setMemberJoined(getMemsByCode.getMemberJoined());
                                                        adM.setPayOutStatus(getMemsByCode.getPayOutStatus() == null ? "empty" : getMemsByCode.getPayOutStatus());
                                                        adM.setPayOutStatusDesc(getMemsByCode.getPayOutStatusDesc() == null ? "empty" : getMemsByCode.getPayOutStatusDesc());
                                                        adM.setAmountToContribute(getMemsByCode.getAmountToContribute());
                                                        adM.setCurrentMonthContribution(getMemsByCode.getCurrentMonthContribution());
                                                        adM.setCurrentMonthContributionStatusDesc(getMemsByCode.getCurrentMonthContributionStatusDesc() == null ? "empty" : getMemsByCode.getCurrentMonthContributionStatusDesc());
                                                        adM.setCurrentMonthContributionStatusId(getMemsByCode.getCurrentMonthContributionStatusId() == null ? "empty" : getMemsByCode.getCurrentMonthContributionStatusId());
                                                        adM.setTotalMonthlyContribution(getMemsByCode.getTotalMonthlyContribution());
                                                        SwapSlot swP = new SwapSlot();
                                                        swP.setIsSwapActive(getMemsByCode.getSwapSlot().getIsSwapActive());
                                                        swP.setIsSwapped(getMemsByCode.getSwapSlot().getIsSwapped());
                                                        swP.setDateSwapped(getMemsByCode.getSwapSlot().getDateSwapped() == null ? "" : getMemsByCode.getSwapSlot().getDateSwapped());
                                                        swP.setReceiverId(getMemsByCode.getSwapSlot().getReceiverId() == null ? "" : getMemsByCode.getSwapSlot().getReceiverId());
                                                        swP.setReceiverSlot(getMemsByCode.getSwapSlot().getReceiverSlot() == 0 ? 0 : getMemsByCode.getSwapSlot().getReceiverSlot());
                                                        swP.setSenderId(getMemsByCode.getSwapSlot().getSenderId() == null ? "" : getMemsByCode.getSwapSlot().getSenderId());
                                                        swP.setSenderSlot(getMemsByCode.getSwapSlot().getSenderSlot() == 0 ? 0 : getMemsByCode.getSwapSlot().getSenderSlot());
                                                        adM.setSwapSlot(swP);

                                                        addMeegetByCode.add(adM);
                                                    }

                                                }

                                                logC.setAddMembersModels(addMeegetByCode);

                                                //logC.setTransactionId(getKul.getTransactionId());
                                                logC.setTransactionStatus(getByCodeI.getTransactionStatus());
                                                logC.setTransactionStatusDesc(getByCodeI.getTransactionStatusDesc());

                                                if (getByCodeI.getLastModifiedDate() == null) {
                                                    logC.setTransactionDate(formDate(getByCodeI.getCreatedDate()));
                                                } else {

                                                    logC.setTransactionDate(formDate(getByCodeI.getLastModifiedDate()));
                                                }

                                                mapAll.add(logC);
                                            }

                                        }
                                    }

                                }

                            }

                        }*/
                    }
                }
                
            }
            
            if (mapAll.isEmpty()) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Customer does not have an existing group!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Customer does not have an existing group!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            responseModel.setData(mapAll);
            responseModel.setDescription("Customer transactions pulled successfully.");
            responseModel.setStatusCode(200);
            
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
        
    }
    
    public ApiResponseModel getUserRequestDetails(ReByEmailAddress rq, String channel, String auth) {
        ApiResponseModel responseModel = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            
            if (!rq.getEmailAddress().equals(getDecoded.emailAddress)) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Suspected fraud!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Suspected fraud!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            System.out.println("pageNation :::::::: " + "    ::::::::::::::::::::: " + returnPagenation());
            //  List<KuleanPaymentTransaction> getKulTransPage = kuleanPaymentTransactionRepo.findByWalletNoListPage(getDecoded.phoneNumber, PageRequest.of(0, pageNation));
            boolean chkGDetailsBool = true;

            /* List<GroupSavingsData> chkGDetails = groupSavingsDataRepo.findByEmailAddressOrdered(rq.getEmailAddress());
             if (chkGDetails.size() <= 0) {
                chkGDetailsBool = false;

            }*/
            //get groups from aaded to groups
            List<GroupSavingsData> getALLGROUPS = groupSavingsDataRepo.findAll();
            
            if (getALLGROUPS.size() <= 0) {
                chkGDetailsBool = false;
                
            }
            if (chkGDetailsBool == false) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "There is no existing group!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("There is no existing group!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            List<SavedGroupDetails> mapAll = new ArrayList<SavedGroupDetails>();
            
            for (GroupSavingsData getAlG : getALLGROUPS) {
                
                if (!getAlG.getIsTrnsactionDeleted().equals("1")) {
                    
                    String addedMemModel = getAlG.getAddedMembersModels();
                    
                    String addedMemInvCode = null;
                    
                    if (getAlG.getAddedMembersModels() != null) {
                        
                        ObjectMapper mapper = new ObjectMapper();
                        List<AddMembersModels> mems = mapper.readValue(addedMemModel, new TypeReference<List<AddMembersModels>>() {
                        });
                        //System.out.println("mems ::::::::::::::::  %S  " + new Gson().toJson(mems));
                        for (AddMembersModels getMems : mems) {
                            String getMemEmailAdd = getMems.getMemberEmailAddress() == null ? "empty" : getMems.getMemberEmailAddress();
                            
                            if (getMemEmailAdd.equals(rq.getEmailAddress()) && !"1".equals(getMems.getIsAdmin()) && !getMems.getMemberJoined().equals("1")) {

                                //  if (!"1".equals(getMems.getIsAdmin())) {
                                addedMemInvCode = getMems.getInvitationCodeReqId();
                                // }
                                List<GroupSavingsData> getByCode = groupSavingsDataRepo.findByInviteCode(addedMemInvCode);
                                SavedGroupDetails logC = new SavedGroupDetails();
                                
                                if (getByCode.size() > 0) {
                                    for (GroupSavingsData getByCodeI : getByCode) {
                                        
                                        logC.setAllowPublicToJoin(getByCodeI.getAllowPublicToJoin());
                                        logC.setGroupSavingAmount(getByCodeI.getGroupSavingAmount());
                                        logC.setGroupSavingDescription(getByCodeI.getGroupSavingDescription());
                                        logC.setGroupSavingFinalAmount(getByCodeI.getGroupSavingFinalAmount());
                                        logC.setGroupSavingName(getByCodeI.getGroupSavingName());
                                        logC.setInviteCode(getByCodeI.getInviteCode());
                                        logC.setTransactionIdLink(getByCodeI.getTransactionIdLink());
                                        logC.setIsTrnsactionDeleted(getByCodeI.getIsTrnsactionDeleted());
                                        logC.setNumberOfMembers(getByCodeI.getNumberOfMembers());
                                        logC.setPayOutDateOfTheMonth(getByCodeI.getPayOutDateOfTheMonth());
                                        logC.setAdminPayOutSlot(returnAraryumberOfSlots(getByCodeI.getAdminPayOutSlot()));
                                        logC.setAvailablePayOutSlot(getByCodeI.getAvailablePayOutSlot());
                                        
                                        ObjectMapper mappergetByCode = new ObjectMapper();
                                        List<AddMembersModels> memsgetByCode = mappergetByCode.readValue(getByCodeI.getAddedMembersModels(), new TypeReference<List<AddMembersModels>>() {
                                        });
                                        List<AddMembersModels> addMeegetByCode = new ArrayList<>();
                                        for (AddMembersModels getMemsByCode : memsgetByCode) {
                                            //if (getDecoded.emailAddress.equals(getMemsByCode.getMemberEmailAddress())) {
                                            AddMembersModels adM = new AddMembersModels();
                                            adM.setMemberEmailAddress(getMemsByCode.getMemberEmailAddress());
                                            adM.setInvitationCodeReqId(getMemsByCode.getInvitationCodeReqId());
                                            adM.setMemberId(getMemsByCode.getMemberId());
                                            adM.setIsAdmin("0");
                                            adM.setMemberIdType(getMemsByCode.getMemberIdType());
                                            adM.setMemberName(getMemsByCode.getMemberName());
                                            adM.setMemberUserId(getMemsByCode.getMemberUserId());
                                            adM.setSlot(getMemsByCode.getSlot());
                                            adM.setMemberJoined(getMemsByCode.getMemberJoined());
                                            adM.setPayOutStatus(getMemsByCode.getPayOutStatus());
                                            adM.setPayOutStatusDesc(getMemsByCode.getPayOutStatusDesc());
                                            adM.setAmountToContribute(getMemsByCode.getAmountToContribute());
                                            adM.setCurrentMonthContribution(getMemsByCode.getCurrentMonthContribution());
                                            adM.setCurrentMonthContributionStatusDesc(getMemsByCode.getCurrentMonthContributionStatusDesc());
                                            adM.setCurrentMonthContributionStatusId(getMemsByCode.getCurrentMonthContributionStatusId());
                                            adM.setTotalMonthlyContribution(getMemsByCode.getTotalMonthlyContribution());
                                            SwapSlot swP = new SwapSlot();
                                            swP.setIsSwapActive(getMemsByCode.getSwapSlot().getIsSwapActive());
                                            swP.setIsSwapped(getMemsByCode.getSwapSlot().getIsSwapped());
                                            swP.setDateSwapped(getMemsByCode.getSwapSlot().getDateSwapped() == null ? "" : getMemsByCode.getSwapSlot().getDateSwapped());
                                            swP.setReceiverId(getMemsByCode.getSwapSlot().getReceiverId() == null ? "" : getMemsByCode.getSwapSlot().getReceiverId());
                                            swP.setReceiverSlot(getMemsByCode.getSwapSlot().getReceiverSlot() == 0 ? 0 : getMemsByCode.getSwapSlot().getReceiverSlot());
                                            swP.setSenderId(getMemsByCode.getSwapSlot().getSenderId() == null ? "" : getMemsByCode.getSwapSlot().getSenderId());
                                            swP.setSenderSlot(getMemsByCode.getSwapSlot().getSenderSlot() == 0 ? 0 : getMemsByCode.getSwapSlot().getSenderSlot());
                                            adM.setSwapSlot(swP);
                                            
                                            addMeegetByCode.add(adM);
                                            //   }

                                        }
                                        /*String jsonInput = getByCodeI.getAddedMembersModels();
                                        JsonElement jsonElement = JsonParser.parseString(jsonInput);
                                        String excludeEmail = rq.getEmailAddress(); // the email to exclude
                                        boolean emailExists = false;

                                        List<AddMembersModelsOthers> addOtherByCode = new ArrayList<>();
                                        if (jsonElement.isJsonArray()) {
                                            JsonArray jsonArray = jsonElement.getAsJsonArray();
                                            for (JsonElement element : jsonArray) {
                                                JsonObject member = element.getAsJsonObject();

                                                // Now safely access fields
                                                // JsonElement emailElement = obj.get("memberEmailAddress");
                                                // String email = (emailElement != null && !emailElement.isJsonNull()) ? emailElement.getAsString() : null;
                                                if (member.has("memberEmailAddress") && !member.get("memberEmailAddress").isJsonNull()) {
                                                    // ... handle the rest
                                                    String email = member.get("memberEmailAddress").getAsString();
                                                    if (excludeEmail.equalsIgnoreCase(email)) {
                                                        emailExists = true;
                                                        break;
                                                    }
                                                }
                                            }

                                        } else {
                                            JsonObject root = JsonParser.parseString(jsonInput).getAsJsonObject();
                                            JsonArray membersArray = root.getAsJsonArray("addMembersModels");

                                            for (JsonElement element : membersArray) {
                                                JsonObject member = element.getAsJsonObject();
                                                if (member.has("memberEmailAddress") && !member.get("memberEmailAddress").isJsonNull()) {
                                                    String email = member.get("memberEmailAddress").getAsString();
                                                    if (excludeEmail.equalsIgnoreCase(email)) {
                                                        emailExists = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }*/

 /*for (AddMembersModels getMemsByCode : memsgetByCode) {
                                            if (emailExists) {
                                                if (!getDecoded.emailAddress.equals(getMemsByCode.getMemberEmailAddress())) {

                                                    AddMembersModelsOthers adM = new AddMembersModelsOthers();
                                                    adM.setInvitationCodeReqId(getMemsByCode.getInvitationCodeReqId());
                                                    adM.setMemberId(getMemsByCode.getMemberId());
                                                    adM.setMemberEmailAddress(getMemsByCode.getMemberEmailAddress());

                                                    adM.setMemberName(getMemsByCode.getMemberName());
                                                    adM.setSlot(getMemsByCode.getSlot());
                                                    adM.setMemberJoined(getMemsByCode.getMemberJoined());
                                                    adM.setPayOutStatus(getMemsByCode.getPayOutStatus());
                                                    adM.setPayOutStatusDesc(getMemsByCode.getPayOutStatusDesc());
                                                    adM.setAmountToContribute(getMemsByCode.getAmountToContribute());
                                                    adM.setCurrentMonthContribution(getMemsByCode.getCurrentMonthContribution());
                                                    adM.setCurrentMonthContributionStatusDesc(getMemsByCode.getCurrentMonthContributionStatusDesc());
                                                    adM.setCurrentMonthContributionStatusId(getMemsByCode.getCurrentMonthContributionStatusId());
                                                    adM.setTotalMonthlyContribution(getMemsByCode.getTotalMonthlyContribution());
                                                    SwapSlot swP = new SwapSlot();
                                                    swP.setIsSwapActive(getMemsByCode.getSwapSlot().getIsSwapActive());
                                                    swP.setIsSwapped(getMemsByCode.getSwapSlot().getIsSwapped());
                                                    swP.setDateSwapped(getMemsByCode.getSwapSlot().getDateSwapped() == null ? "" : getMemsByCode.getSwapSlot().getDateSwapped());
                                                    swP.setReceiverId(getMemsByCode.getSwapSlot().getReceiverId() == null ? "" : getMemsByCode.getSwapSlot().getReceiverId());
                                                    swP.setReceiverSlot(getMemsByCode.getSwapSlot().getReceiverSlot() == 0 ? 0 : getMemsByCode.getSwapSlot().getReceiverSlot());
                                                    swP.setSenderId(getMemsByCode.getSwapSlot().getSenderId() == null ? "" : getMemsByCode.getSwapSlot().getSenderId());
                                                    swP.setSenderSlot(getMemsByCode.getSwapSlot().getSenderSlot() == 0 ? 0 : getMemsByCode.getSwapSlot().getSenderSlot());
                                                    adM.setSwapSlot(swP);

                                                    addOtherByCode.add(adM);
                                                }
                                            }
                                        }*/
                                        logC.setAddMembersModels(addMeegetByCode);
                                        //logC.setAddMembersModelsOthers(addOtherByCode);

                                        //logC.setTransactionId(getKul.getTransactionId());
                                        logC.setTransactionStatus(getByCodeI.getTransactionStatus());
                                        logC.setTransactionStatusDesc(getByCodeI.getTransactionStatusDesc());
                                        
                                        if (getByCodeI.getLastModifiedDate() == null) {
                                            logC.setTransactionDate(formDate(getByCodeI.getCreatedDate()));
                                        } else {
                                            
                                            logC.setTransactionDate(formDate(getByCodeI.getLastModifiedDate()));
                                        }
                                        
                                    }
                                    mapAll.add(logC);
                                    
                                }
                                
                            }
                            
                        }
                        
                    }
                }
                
            }
            
            if (mapAll.isEmpty()) {
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Customer does not have an existing group!");
                settlementFailureLogRepo.save(conWall);
                responseModel.setDescription("Customer does not have an existing group!");
                responseModel.setStatusCode(statusCode);
                return responseModel;
            }
            
            responseModel.setData(mapAll);
            responseModel.setDescription("Customer transactions pulled successfully.");
            responseModel.setStatusCode(200);
            
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
        
    }
    
    public int returnPagenation() {
        
        int pageNation = Integer.parseInt(utilMeth.getSETTING_KEY_TRANS_G_SAVINGS_LIST_PAGENATION());
        
        return pageNation;
        
    }
    
    public BaseResponse validateHasPin(String channel, String auth) {
        
        BaseResponse responseModel = new BaseResponse();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            DecodedJWTToken getDecoded = DecodedJWTToken.getDecoded(auth);
            
            List<RegWalletInfo> senderWalletdetails = regWalletInfoRepository.findByPhoneNumberData(getDecoded.phoneNumber);
            if (!senderWalletdetails.get(0).isActivation()) {
                
                SettlementFailureLog conWall = new SettlementFailureLog("", "",
                        "Customer has not created PIN!");
                settlementFailureLogRepo.save(conWall);
                
                responseModel.setDescription("Customer has not created PIN!");
                responseModel.setStatusCode(60);
                
                return responseModel;
            }
            
            responseModel.setDescription("Customer has activated PIN.");
            responseModel.setStatusCode(200);
            
        } catch (Exception ex) {
            responseModel.setDescription(statusMessage);
            responseModel.setStatusCode(statusCode);
            
            ex.printStackTrace();
        }
        
        return responseModel;
    }
    
    public List<CommissionCfg> findAllByTransactionType(String transType) {
        
        List<CommissionCfg> getAllPartActiveNoti = commissionCfgRepo.findAllByTransactionType(transType);
        
        return getAllPartActiveNoti;
    }
    
    public static boolean betweenTransBand(BigDecimal i, BigDecimal minValueInclusive, BigDecimal maxValueInclusive) {
        return i.subtract(minValueInclusive).signum() >= 0 && i.subtract(maxValueInclusive).signum() <= 0;
        
    }
    
    public static BigDecimal percentage(BigDecimal base, BigDecimal pct) {
        return base.multiply(pct).divide(ONE_HUNDRED);
    }
    
    private String formDate(Instant datte) {
        
        LocalDateTime datetime = LocalDateTime.ofInstant(datte, ZoneOffset.UTC);
        String formatted = DateTimeFormatter.ofPattern("MMM dd, yyyy").format(datetime);
        // System.out.println(formatted);

        return formatted;
    }
    
    private static String cleanText(String text) {
        text = text.replaceAll("[^\\x00-\\x7F]", "");
        text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
        text = text.replaceAll("\\p{C}", "");
        return text.trim();
    }
    
    public boolean checkIfDateIsAfterRequiredHours(Date date) {
        
        boolean isValid = true;
        
        LocalDateTime submittedDateTime = date
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        
        LocalDateTime now = LocalDateTime.now();
        
        int numbDate = Integer.parseInt(utilMeth.getSETTING_CONFIGURE_NUMB_DAYS_BEFORE_ACTIVATION());

        // Check if submitted date is at least 48 hours from now
        if (Duration.between(now, submittedDateTime).toHours() < numbDate) {
            
            System.out.println("Selected date must be at least " + numbDate + " hours from now.");
            isValid = false;
            
        }
        
        return isValid;
    }
    
    public ApiResponseModel checkIfDateIsAfterRequiredWorkingDays(String day) {
        
        ApiResponseModel responseModel = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "An error occured,please try again";
        try {
            statusCode = 400;
            
            int numbWorkingDays = Integer.parseInt(utilMeth.getSETTING_KEY_TRANS_G_SAVINGS_LIST_PAYMENT_WORK_DAYS());
            
            int enteredDay = Integer.parseInt(day);
            
            if (enteredDay < 1 || enteredDay > 31) {
                
                System.out.println("Invalid day of the month: " + enteredDay);
                responseModel.setStatusCode(statusCode);
                responseModel.setDescription("Invalid day of the month: " + enteredDay + "!");
                return responseModel;
                
            }
            
            LocalDate minWorkingDay = addWorkingDays(LocalDate.now(), numbWorkingDays);
            LocalDate validDate = findNextValidDate(enteredDay, minWorkingDay);
            
            if (validDate.isBefore(minWorkingDay)) {
                
                System.out.println("The next date matching day " + enteredDay
                        + " is before 20 working days from today (" + minWorkingDay + ").");
                responseModel.setStatusCode(statusCode);
                responseModel.setDescription("The next date matching day " + enteredDay
                        + " is before 20 working days from today (" + minWorkingDay + ")." + "!");
                
            }
            
            responseModel.setStatusCode(200);
            responseModel.setDescription("Payment date is valid.");
            
        } catch (Exception ex) {
            ex.printStackTrace();
            responseModel.setStatusCode(statusCode);
            responseModel.setDescription(statusMessage);
        }
        
        return responseModel;
    }

    // Finds the next date with the entered day (e.g., 15th) after or equal to the reference date
    private LocalDate findNextValidDate(int targetDay, LocalDate referenceDate) {
        LocalDate candidate = referenceDate.withDayOfMonth(1);
        
        while (true) {
            YearMonth ym = YearMonth.of(candidate.getYear(), candidate.getMonth());
            int maxDay = ym.lengthOfMonth();
            
            if (targetDay <= maxDay) {
                LocalDate potentialDate = candidate.withDayOfMonth(targetDay);
                if (!potentialDate.isBefore(referenceDate)) {
                    return potentialDate;
                }
            }
            
            candidate = candidate.plusMonths(1);
        }
    }
    
    private LocalDate addWorkingDays(LocalDate startDate, int workingDays) {
        LocalDate date = startDate;
        int addedDays = 0;
        while (addedDays < workingDays) {
            date = date.plusDays(1);
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                addedDays++;
            }
        }
        return date;
    }
    
    public String returnStringOject(List<AddMembersModels> user) {
        
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writeValueAsString(user);
            System.out.println(json);  // Output: {"name":"Alice","age":25}
            System.out.println("added to array AddMembersModels" + " :::::::::::::::::::::   " + json);
            
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        return json;
    }
    
    public ApiResponseModel configuredPayOutSlotsData() {
        ApiResponseModel response = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "an Error occured, please try again";
        try {
            List<ConfiguredPayOutSlots> sData = configuredPayOutSlotsRepo.findAll();
            String sDataJson = cleanText(new Gson().toJson(sData));
            
            statusCode = 400;
            statusMessage = "Invalid Parameter";
            if (!sData.isEmpty()) {
                statusCode = 200;
                statusMessage = "Successful";
                List<ConfiguredPayOutSlots> ssData = new Gson().fromJson(sDataJson, new TypeToken<List<ConfiguredPayOutSlots>>() {
                }.getType());
                response.setData(ssData);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        response.setStatusCode(statusCode);
        response.setDescription(statusMessage);
        return response;
    }
    
}
