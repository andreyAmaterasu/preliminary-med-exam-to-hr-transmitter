package ru.kostromin.preliminarymedexamtohrtransmitter.service;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.kostromin.preliminarymedexamtohrtransmitter.util.DatesInitializer;
import ru.kostromin.preliminarymedexamtohrtransmitter.util.DatesInitializer.DatesDto;

@TestInstance(Lifecycle.PER_METHOD)
class MedicalResultRunnerTest {

  @Mock
  private DatesInitializer datesInitializer;

  @Mock
  private MedicalResultService medicalResultService;

  @InjectMocks
  private MedicalResultRunner medicalResultRunner;

  AutoCloseable mocks;

  @BeforeEach
  void setup() {
    mocks = MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("Даты проинициализированы, сервис отрабатывает без ошибок")
  void datesInitializedFindAndSendMedicalResultMethodIsCalled() {

    DatesDto datesDto = new DatesDto(LocalDateTime.MIN, LocalDateTime.MAX);

    Mockito.when(datesInitializer.getDatesDto()).thenReturn(datesDto);

    medicalResultRunner.runMedicalResultTransmitter();

    Mockito.verify(datesInitializer, Mockito.times(1))
        .getDatesDto();
    Mockito.verify(medicalResultService, Mockito.times(1))
        .findAndSendMedicalResult(datesDto);
  }

  @Test
  @DisplayName("Даты не проинициализированы, выброшено исключение в DatesInitializer")
  void datesNotInitializedFindAndSendMedicalResultMethodIsNotCalled() {

    Mockito.when(datesInitializer.getDatesDto())
        .thenThrow(new RuntimeException("Any error in DatesInitializer"));

    medicalResultRunner.runMedicalResultTransmitter();

    Mockito.verify(datesInitializer, Mockito.times(1))
        .getDatesDto();
    Mockito.verify(medicalResultService, Mockito.times(0))
        .findAndSendMedicalResult(Mockito.any());
  }

  @Test
  @DisplayName("Даты проинициализированы, выброшено исключение при работе сервиса")
  void unexpectedErrorHasOccurred() {

    Mockito.doThrow(new RuntimeException("Any error in MedicalResultService"))
        .when(medicalResultService).findAndSendMedicalResult(Mockito.any());

    medicalResultRunner.runMedicalResultTransmitter();

    Mockito.verify(datesInitializer, Mockito.times(1))
        .getDatesDto();
    Mockito.verify(medicalResultService, Mockito.times(1))
        .findAndSendMedicalResult(Mockito.any());
  }
}
