package com.example.train_back.service;

import com.example.train_back.dto.agreement.*;

import java.util.List;

public interface AgreementService {

    List<AgreementListItemVO> getAgreementList(String scenario, Integer currentUserId);

    AgreementDetailVO getAgreementDetail(Integer agreementId);

    AgreementSignResultVO signAgreement(AgreementSignRequest request,
                                        Integer currentUserId,
                                        String ipAddress,
                                        String deviceInfo);

    AgreementSignBatchResultVO signAgreementBatch(AgreementSignBatchRequest request,
                                                  Integer currentUserId,
                                                  String ipAddress,
                                                  String deviceInfo);
}
