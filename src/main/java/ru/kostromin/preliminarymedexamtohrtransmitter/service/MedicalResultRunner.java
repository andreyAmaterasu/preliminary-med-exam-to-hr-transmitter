package ru.kostromin.preliminarymedexamtohrtransmitter.service;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.kostromin.preliminarymedexamtohrtransmitter.util.DatesInitializer;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalResultRunner {

  private final DatesInitializer datesInitializer;

  private final MedicalResultService medicalResultService;

  public void runMedicalResultTransmitter() {
    log.info("- Service started");
    Try.run(() -> medicalResultService.findAndSendMedicalResult(datesInitializer.getDatesDto()))
        .onFailure(e -> log.error("Произошла непредвиденная ошибка при работе сервиса: ", e));
    log.info("- Service ended");
  }
}
