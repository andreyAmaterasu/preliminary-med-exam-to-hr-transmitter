package ru.kostromin.preliminarymedexamtohrtransmitter.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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

@TestInstance(Lifecycle.PER_METHOD)
class MedicalResultServiceTest {

  @Mock
  private VHstBKResultRepository bkResultRepository;

  @Mock
  private HstMedResultSendingRepository resultSendingRepository;

  @Mock
  private MedicalExamFeignClient client;

  @Mock
  private AppConfiguration appConfiguration;

  private MedicalResultService service;

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private static final ObjectFactory objectFactory = new ObjectFactory();

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    Mockito.when(appConfiguration.getDateTimePattern()).thenReturn("yyyy-MM-dd HH:mm");
    service = new MedicalResultService(bkResultRepository, resultSendingRepository, client, appConfiguration);
  }

  @Test
  @DisplayName("Данные из БД получены."
      + "В таблице hst_MedResultSending нет записей (MessageId = 1, Status = ins)"
      + "Запрос сформирован и отправлен."
      + "Результат сохранен в БД.")
  void dataReceivedFromDatabaseRecordNotExistRequestGeneratedAndSentResultSavedInDatabase() {

    DatesDto datesDtoMock = new DatesDto(LocalDateTime.MIN, LocalDateTime.MAX);
    Mockito.when(bkResultRepository.findAllByDateCreateBetweenAndSendingStatusFalse(Mockito.any(), Mockito.any()))
        .thenReturn(getBKResultsMock());

    service.findAndSendMedicalResult(datesDtoMock);

    ArgumentCaptor<DTMedicalExamRequest> requestCaptor = ArgumentCaptor.forClass(DTMedicalExamRequest.class);
    Mockito.verify(client, Mockito.times(1))
        .sendMedicalExamRequest(requestCaptor.capture());
    Assertions.assertNotNull(requestCaptor.getValue());
    DTMedicalExamRequest actualRequest = requestCaptor.getValue();
    DTMedicalExamRequest requstMock = getMedicalExamRequestMock(actualRequest.getMessageID());

    Assertions.assertEquals(requstMock.getMessageID(), actualRequest.getMessageID());
    Assertions.assertEquals(requstMock.getRow().size(), actualRequest.getRow().size());
    Assertions.assertEquals(requstMock.getRow().get(0).getPid(), actualRequest.getRow().get(0).getPid());
    Assertions.assertEquals(requstMock.getRow().get(0).getDescription(), actualRequest.getRow().get(0).getDescription());
    Assertions.assertEquals(requstMock.getRow().get(0).getFromd(), actualRequest.getRow().get(0).getFromd());
    Assertions.assertEquals(requstMock.getRow().get(0).getTod(), actualRequest.getRow().get(0).getTod());
    Assertions.assertEquals(requstMock.getRow().get(0).getStatus(), actualRequest.getRow().get(0).getStatus());
    Assertions.assertEquals("ins", actualRequest.getRow().get(0).getStatus());

    Mockito.verify(client, Mockito.times(1))
        .sendMedicalExamRequest(Mockito.any());
  }

  @Test
  @DisplayName("Данные из БД получены."
      + "В таблице hst_MedResultSending нет записей"
      + "Запрос сформирован и отправлен."
      + "Результат сохранен в БД.")
  void ataReceivedFromDatabaseRecordNotExistRequestGeneratedAndSentResultSavedInDatabase() {

    DatesDto datesDtoMock = new DatesDto(LocalDateTime.MIN, LocalDateTime.MAX);
    Mockito.when(bkResultRepository.findAllByDateCreateBetweenAndSendingStatusFalse(Mockito.any(), Mockito.any()))
        .thenReturn(getBKResultsMock());
    Mockito.doThrow(new RuntimeException("Any sending request error"))
        .when(client).sendMedicalExamRequest(Mockito.any());

    service.findAndSendMedicalResult(datesDtoMock);

    ArgumentCaptor<DTMedicalExamRequest> requestCaptor = ArgumentCaptor.forClass(DTMedicalExamRequest.class);
    Mockito.verify(client, Mockito.times(1))
        .sendMedicalExamRequest(requestCaptor.capture());
    Assertions.assertNotNull(requestCaptor.getValue());
    DTMedicalExamRequest actualRequest = requestCaptor.getValue();
    DTMedicalExamRequest requstMock = getMedicalExamRequestMock(actualRequest.getMessageID());

    Assertions.assertEquals(requstMock.getMessageID(), actualRequest.getMessageID());
    Assertions.assertEquals(requstMock.getRow().size(), actualRequest.getRow().size());
    Assertions.assertEquals(requstMock.getRow().get(0).getPid(), actualRequest.getRow().get(0).getPid());
    Assertions.assertEquals(requstMock.getRow().get(0).getDescription(), actualRequest.getRow().get(0).getDescription());
    Assertions.assertEquals(requstMock.getRow().get(0).getFromd(), actualRequest.getRow().get(0).getFromd());
    Assertions.assertEquals(requstMock.getRow().get(0).getTod(), actualRequest.getRow().get(0).getTod());
    Assertions.assertEquals(requstMock.getRow().get(0).getStatus(), actualRequest.getRow().get(0).getStatus());
    Assertions.assertEquals("ins", actualRequest.getRow().get(0).getStatus());

    Mockito.verify(client, Mockito.times(1))
        .sendMedicalExamRequest(Mockito.any());
  }

  @Test
  @DisplayName("Данные из БД получены."
      + "В таблице hst_MedResultSending есть запись по TAPGUID и PID"
      + "Запрос сформирован и отправлен."
      + "Результат сохранен в БД.")
  void dataReceivedFromDatabaseRecordExistRequestGeneratedAndSentResultSavedInDatabase() {

    DatesDto datesDtoMock = new DatesDto(LocalDateTime.MIN, LocalDateTime.MAX);
    Mockito.when(bkResultRepository.findAllByDateCreateBetweenAndSendingStatusFalse(Mockito.any(), Mockito.any()))
        .thenReturn(getBKResultsMock());
    final UUID messageId = UUID.randomUUID();
    Mockito.when(resultSendingRepository.findFirstByTapGuidAndPid(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(resultSendingOptionalMock(messageId,true));

    service.findAndSendMedicalResult(datesDtoMock);

    ArgumentCaptor<DTMedicalExamRequest> requestCaptor = ArgumentCaptor.forClass(DTMedicalExamRequest.class);
    Mockito.verify(client, Mockito.times(1))
        .sendMedicalExamRequest(requestCaptor.capture());
    Assertions.assertNotNull(requestCaptor.getValue());
    DTMedicalExamRequest actualRequest = requestCaptor.getValue();
    DTMedicalExamRequest requstMock = getMedicalExamRequestMock(actualRequest.getMessageID());

    Assertions.assertEquals(requstMock.getMessageID(), actualRequest.getMessageID());
    Assertions.assertEquals(requstMock.getRow().size(), actualRequest.getRow().size());
    Assertions.assertEquals(requstMock.getRow().get(0).getPid(), actualRequest.getRow().get(0).getPid());
    Assertions.assertEquals(requstMock.getRow().get(0).getDescription(), actualRequest.getRow().get(0).getDescription());
    Assertions.assertEquals(requstMock.getRow().get(0).getFromd(), actualRequest.getRow().get(0).getFromd());
    Assertions.assertEquals(requstMock.getRow().get(0).getTod(), actualRequest.getRow().get(0).getTod());
    Assertions.assertEquals(requstMock.getRow().get(0).getStatus(), actualRequest.getRow().get(0).getStatus());
    Assertions.assertEquals("upd", actualRequest.getRow().get(0).getStatus());

    Mockito.verify(client, Mockito.times(1))
        .sendMedicalExamRequest(Mockito.any());
  }

  @Test
  @DisplayName("Данные из БД не получены."
      + "Запрос не отправлен."
      + "В БД ничего не сохранено.")
  void dataNotReceivedFromDatabaseRequestNotSentResultNotSavedInDatabase() {

    DatesDto datesDtoMock = new DatesDto(LocalDateTime.MIN, LocalDateTime.MAX);
    Mockito.when(bkResultRepository.findAllByDateCreateBetweenAndSendingStatusFalse(Mockito.any(), Mockito.any()))
        .thenReturn(new ArrayList<>());

    service.findAndSendMedicalResult(datesDtoMock);

    Mockito.verify(client, Mockito.times(0))
        .sendMedicalExamRequest(Mockito.any());
    Mockito.verify(resultSendingRepository, Mockito.times(0))
        .saveAll(Mockito.any());
    Mockito.verify(resultSendingRepository, Mockito.times(0))
        .findFirstByTapGuidAndPid(Mockito.anyString(), Mockito.anyInt());
  }

  private List<VHstBKResult> getBKResultsMock() {

    VHstBKResult result = new VHstBKResult();
    result.setTapGuid("TAPGUID");
    result.setPid(1);
    result.setDateBegin(LocalDateTime.of(LocalDate.of(2023, 1, 1), LocalTime.MIN));
    result.setDateEnd(LocalDateTime.of(LocalDate.of(2023, 1, 2), LocalTime.MIN));
    result.setDateCreate(LocalDateTime.of(LocalDate.of(2023, 1, 3), LocalTime.MIN));
    result.setResult(2);
    return List.of(result);
  }

  private DTMedicalExamRequest getMedicalExamRequestMock(String messageID) {
    DTMedicalExamRequest request = objectFactory.createDTMedicalExamRequest();
    request.setMessageID(messageID);

    getBKResultsMock().forEach(bkResult -> {
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

  private Optional<HstMedResultSending> resultSendingOptionalMock(UUID messageId, Boolean sendingStatus) {
    return Optional.of(HstMedResultSending.builder()
        .medResultSendingId(1L)
        .messageId(messageId.toString())
        .result(2)
        .tapGuid("TAPGUID")
        .pid(1)
        .dateChange(LocalDateTime.MIN)
        .sendingStatus(sendingStatus).build());
  }
}
