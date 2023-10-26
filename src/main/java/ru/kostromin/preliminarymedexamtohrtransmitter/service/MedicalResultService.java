package ru.kostromin.preliminarymedexamtohrtransmitter.service;

import io.vavr.control.Try;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.alrosa.boss.mis.DTMedicalExamRequest;
import ru.alrosa.boss.mis.DTMedicalExamRequest.Row;
import ru.alrosa.boss.mis.ObjectFactory;
import ru.kostromin.preliminarymedexamtohrtransmitter.client.MedicalExamFeignClient;
import ru.kostromin.preliminarymedexamtohrtransmitter.configuration.AppConfiguration;
import ru.kostromin.preliminarymedexamtohrtransmitter.configuration.AppConfiguration.RequestStatus;
import ru.kostromin.preliminarymedexamtohrtransmitter.data.entity.HstMedResultSending;
import ru.kostromin.preliminarymedexamtohrtransmitter.data.entity.VHstBKResult;
import ru.kostromin.preliminarymedexamtohrtransmitter.data.repository.HstMedResultSendingRepository;
import ru.kostromin.preliminarymedexamtohrtransmitter.data.repository.VHstBKResultRepository;
import ru.kostromin.preliminarymedexamtohrtransmitter.util.DatesInitializer.DatesDto;

/**
 * Сервис для загрузки данных о случаях и формирования запроса для отправки
 */
@Slf4j
@Service
public class MedicalResultService {

  private final VHstBKResultRepository bkResultRepository;

  private final HstMedResultSendingRepository resultSendingRepository;

  private final MedicalExamFeignClient medicalExamClient;

  private final DateTimeFormatter formatter;

  private static final ObjectFactory objectFactory = new ObjectFactory();

  public MedicalResultService(
      VHstBKResultRepository bkResultRepository,
      HstMedResultSendingRepository resultSendingRepository,
      MedicalExamFeignClient medicalExamClient,
      AppConfiguration appConfiguration) {

    this.bkResultRepository = bkResultRepository;
    this.resultSendingRepository = resultSendingRepository;
    this.medicalExamClient = medicalExamClient;
    this.formatter = DateTimeFormatter.ofPattern(appConfiguration.getDateTimePattern());
  }

  /**
   * Метод для поиска случаев и передачи сформированного запроса в клиент для последующей отправки
   *
   * @param datesDto {@link DatesDto} представление периода выгрузки случаев
   */
  public void findAndSendMedicalResult(DatesDto datesDto) {

    log.info("Поиск случаев ПРМО...");
    List<VHstBKResult> bkResults = bkResultRepository.findAllByDateCreateBetweenAndSendingStatusFalse(
        datesDto.dateFrom(), datesDto.dateTo());
    if (!CollectionUtils.isEmpty(bkResults)) {
      log.info("Найдено записей: {}", bkResults.size());
      log.info("Создание и отправка запроса...");
      DTMedicalExamRequest request = createRequest(bkResults);
      final boolean sendingResultStatus = Try.run(() -> medicalExamClient.sendMedicalExamRequest(request))
          .fold(e -> {
            log.error("Произошла ошибка во время отправки запроса: {}", e.getMessage());
            return false;
          }, result -> true);
      List<HstMedResultSending> sendingResults = bkResults.stream().map(result ->
          createResult(
              request.getMessageID(),
              result,
              sendingResultStatus)).toList();
      resultSendingRepository.saveAll(sendingResults);
      log.info("Сформирован результат и сохранен в базу данных: {}", sendingResults);
    } else {
      log.info("Не найдено ни одного случая ПРМО, запрос не был отправлен");
    }
  }

  /**
   * Метод для создания запроса на основе загруженных случаев
   *
   * @param bkResults {@link List} представлений {@link VHstBKResult} содержащий выгруженные случаи
   * @return {@link DTMedicalExamRequest} представление запроса
   */
  private DTMedicalExamRequest createRequest(List<VHstBKResult> bkResults) {
    DTMedicalExamRequest request = objectFactory.createDTMedicalExamRequest();
    request.setMessageID(UUID.randomUUID().toString());
    bkResults.forEach(bkResult -> {
      Row row = objectFactory.createDTMedicalExamRequestRow();
      row.setFromd(bkResult.getDateBegin().format(formatter));
      row.setTod(bkResult.getDateEnd().format(formatter));
      row.setPid(String.valueOf(bkResult.getPid()));
      row.setDescription(String.valueOf(bkResult.getResult()));

      resultSendingRepository.findFirstByTapGuidAndPid(bkResult.getTapGuid(), bkResult.getPid())
          .ifPresentOrElse(
              rs -> row.setStatus(RequestStatus.UPDATE.getStatusName()),
              () -> row.setStatus(RequestStatus.INSERT.getStatusName()));
      request.getRow().add(row);
    });
    return request;
  }

  /**
   * Метод для создания представления результата отправки случая
   *
   * @param messageId идентификатор пакета
   * @param result {@link VHstBKResult} представление выгруженного случая
   * @param sendingStatus статус отправки случая
   * @return {@link HstMedResultSending} представление результата передачи случая
   */
  private HstMedResultSending createResult(String messageId, VHstBKResult result, boolean sendingStatus) {
    HstMedResultSending resultSending = resultSendingRepository
        .findFirstByTapGuidAndPid(result.getTapGuid(), result.getPid())
        .orElse(HstMedResultSending.builder()
            .tapGuid(result.getTapGuid())
            .pid(result.getPid())
            .build());
    resultSending.setMessageId(messageId);
    resultSending.setDateChange(LocalDateTime.now());
    resultSending.setResult(result.getResult());
    resultSending.setSendingStatus(sendingStatus);
    return resultSending;
  }
}
