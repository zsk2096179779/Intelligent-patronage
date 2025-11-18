package com.example.train_back.service.impl;

import com.example.train_back.dto.agreement.*;
import com.example.train_back.entity.AgreementTemplate;
import com.example.train_back.entity.UserAgreementSign;
import com.example.train_back.mapper.AgreementTemplateMapper;
import com.example.train_back.mapper.UserAgreementSignMapper;
import com.example.train_back.service.AgreementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


@Service
@Slf4j
public class AgreementServiceImpl implements AgreementService {

    private final AgreementTemplateMapper agreementTemplateMapper;
    private final UserAgreementSignMapper userAgreementSignMapper;

    public AgreementServiceImpl(AgreementTemplateMapper agreementTemplateMapper,
                                UserAgreementSignMapper userAgreementSignMapper) {
        this.agreementTemplateMapper = agreementTemplateMapper;
        this.userAgreementSignMapper = userAgreementSignMapper;
    }

    @Override
    public List<AgreementListItemVO> getAgreementList(String scenario, Integer currentUserId) {
        // 0) 场景码兜底 + 规整（去空格 + 小写）
        String dbScene;
        if (scenario == null || scenario.isBlank()) {
            dbScene = "subscription";   // 默认订购场景
        } else {
            dbScene = scenario.trim().toLowerCase();
        }

//        log.info("[Agreement] getAgreementList 入参 scenario={}, 规整后 dbScene={}", scenario, dbScene);

        // 1) 调用 Mapper
        List<AgreementTemplate> templates = agreementTemplateMapper.selectByScenario(dbScene);
//        log.info("[Agreement] selectByScenario 返回条数={}",
//                (templates == null ? 0 : templates.size()));

        List<AgreementListItemVO> result = new ArrayList<>();
        if (templates == null || templates.isEmpty()) {
            return result;
        }

        for (AgreementTemplate t : templates) {
            AgreementListItemVO vo = new AgreementListItemVO();
            vo.setId(t.getId());
            vo.setAgreementCode(t.getAgreementCode());
            vo.setAgreementType(t.getAgreementType());
            vo.setTitle(t.getTitle());
            vo.setVersion(t.getVersion());
            vo.setEffectiveDate(
                    t.getEffectiveDate() == null ? null : t.getEffectiveDate().toString()
            );
            vo.setContentHash(t.getContentHash());


            // 目前全部视为必签
            vo.setRequired(Boolean.TRUE);

            // 是否已签（未登录就全 false）
            boolean signed = false;
            if (currentUserId != null) {
                UserAgreementSign sign = userAgreementSignMapper
                        .selectLatestByUserAndAgreement(currentUserId, t.getId(), dbScene);
                signed = (sign != null
                        && sign.getSignStatus() != null
                        && sign.getSignStatus() == 1);
            }
            vo.setSigned(signed);

            result.add(vo);
        }

//        log.info("[Agreement] 最终返回给前端的协议条数={}", result.size());
        return result;
    }

    @Override
    public AgreementDetailVO getAgreementDetail(Integer agreementId) {
        AgreementTemplate t = agreementTemplateMapper.selectById(agreementId);
        if (t == null) {
            // 这里可以抛你自己的业务异常，比如 BizException(1004, "协议不存在")
            throw new RuntimeException("协议不存在");
        }

        // 协议详情被查看次数 +1（可选）
        agreementTemplateMapper.increaseViewCount(agreementId);

        AgreementDetailVO vo = new AgreementDetailVO();
        vo.setId(t.getId());
        vo.setAgreementCode(t.getAgreementCode());
        vo.setTitle(t.getTitle());
        vo.setContent(t.getContent());
        vo.setContentHash(t.getContentHash());
        vo.setVersion(t.getVersion());
        vo.setEffectiveDate(t.getEffectiveDate() == null ? null : t.getEffectiveDate().toString());
        return vo;
    }

    @Override
    public AgreementSignResultVO signAgreement(AgreementSignRequest request,
                                               Integer currentUserId,
                                               String ipAddress,
                                               String deviceInfo) {
        if (currentUserId == null) {
            throw new RuntimeException("未登录，无法签署协议");
        }

        // 1) 基本参数校验
        if (request.getAgreementId() == null || !StringUtils.hasText(request.getScenario())) {
            throw new RuntimeException("参数不完整");
        }

        AgreementTemplate t = agreementTemplateMapper.selectById(request.getAgreementId());
        if (t == null) {
            throw new RuntimeException("协议不存在");
        }

        // 2) 内容一致性校验：防止前端传错
        if (!t.getAgreementCode().equals(request.getAgreementCode())
                || !t.getVersion().equals(request.getAgreementVersion())
                || !t.getContentHash().equals(request.getContentHash())) {
            throw new RuntimeException("协议版本或内容校验失败");
        }

        // 3) 生成签署记录
        String signNo = generateSignNo();
        LocalDateTime now = LocalDateTime.now();

        UserAgreementSign sign = new UserAgreementSign();
        sign.setUserId(currentUserId);
        sign.setAgreementId(t.getId());
        sign.setAgreementCode(t.getAgreementCode());
        sign.setAgreementVersion(t.getVersion());
        sign.setSignNo(signNo);
        sign.setSignStatus(1);
        sign.setSignScenario(request.getScenario());
        sign.setRelatedOrderNo(request.getRelatedOrderNo());
        sign.setIpAddress(ipAddress);
        sign.setDeviceInfo(deviceInfo);
        sign.setSignedAt(now);
        sign.setContentHash(t.getContentHash());
        sign.setSignatureData(request.getSignatureData());

        userAgreementSignMapper.insert(sign);

        // 4) 协议签署次数 +1（统计用）
        agreementTemplateMapper.increaseSignCount(t.getId());

        // 5) 封装返回
        AgreementSignResultVO vo = new AgreementSignResultVO();
        vo.setSignNo(signNo);
        vo.setSignedAt(now);
        vo.setAgreementCode(t.getAgreementCode());
        vo.setAgreementVersion(t.getVersion());
        return vo;
    }

    @Override
    public AgreementSignBatchResultVO signAgreementBatch(AgreementSignBatchRequest request,
                                                         Integer currentUserId,
                                                         String ipAddress,
                                                         String deviceInfo) {
//        log.info("[Agreement] signBatch 入参 userId={}, ip={}, scenario={}, relatedOrderNo={}, agreements={}",
//                currentUserId, ipAddress,
//                request != null ? request.getScenario() : null,
//                request != null ? request.getRelatedOrderNo() : null,
//                request != null ? request.getAgreements() : null);

        if (currentUserId == null) {
            throw new RuntimeException("未登录，无法签署协议");
        }
        if (request == null || CollectionUtils.isEmpty(request.getAgreements())) {
            throw new RuntimeException("无协议可签署");
        }

        List<AgreementSignBatchResultVO.SignRecord> records = new ArrayList<>();

        for (AgreementSignBatchRequest.AgreementItem item : request.getAgreements()) {
            AgreementSignRequest singleReq = new AgreementSignRequest();
            singleReq.setAgreementId(item.getAgreementId());
            singleReq.setAgreementCode(item.getAgreementCode());
            singleReq.setAgreementVersion(item.getAgreementVersion());
            singleReq.setContentHash(item.getContentHash());
            singleReq.setScenario(request.getScenario());
            singleReq.setRelatedOrderNo(request.getRelatedOrderNo());
            singleReq.setSignatureData(item.getSignatureData());

            AgreementSignResultVO one = signAgreement(singleReq, currentUserId, ipAddress, deviceInfo);

            AgreementSignBatchResultVO.SignRecord r = new AgreementSignBatchResultVO.SignRecord();
            r.setSignNo(one.getSignNo());
            r.setAgreementCode(one.getAgreementCode());
            r.setAgreementVersion(one.getAgreementVersion());
            r.setSignedAt(one.getSignedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            records.add(r);

//            log.info("[Agreement] 即将签署单个协议: id={}, code={}, ver={}",
//                    item.getAgreementId(), item.getAgreementCode(), item.getAgreementVersion());

        }

        AgreementSignBatchResultVO result = new AgreementSignBatchResultVO();
        result.setSignedCount(records.size());
        result.setSignRecords(records);
        return result;
    }

    private String generateSignNo() {
        LocalDateTime now = LocalDateTime.now();
        String timePart = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "SIGN" + timePart + rand;
    }
}
