package ru.kostromin.preliminarymedexamtohrtransmitter.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.alrosa.boss.mis.DTMedicalExamRequest;
import ru.kostromin.preliminarymedexamtohrtransmitter.configuration.FeignClientConfiguration;

@FeignClient(name = "medicalExamFeignClient",
  configuration = FeignClientConfiguration.class)
public interface MedicalExamFeignClient {

  @PostMapping
  void sendMedicalExamRequest(@RequestBody DTMedicalExamRequest mtMedicalExamRequest);
}
