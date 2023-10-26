package ru.kostromin.preliminarymedexamtohrtransmitter.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.kostromin.preliminarymedexamtohrtransmitter.configuration.AppConfiguration;
import ru.kostromin.preliminarymedexamtohrtransmitter.data.repository.HstMedResultSendingRepository;
import ru.kostromin.preliminarymedexamtohrtransmitter.util.DatesInitializer.DatesDto;

@TestInstance(Lifecycle.PER_METHOD)
class DatesInitializerTest {

  @Mock
  private AppConfiguration appConfiguration;

  @Mock
  private HstMedResultSendingRepository resultSendingRepository;

  private DatesInitializer datesInitializer;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    Mockito.when(appConfiguration.getDateFrom()).thenReturn(LocalDate.MIN);
    Mockito.when(appConfiguration.getDateTo()).thenReturn(LocalDate.MAX);
  }

  @Test
  @DisplayName("Свойство KindIntervalTime = 1."
      + "DateFrom = начало текущего дня, DateTo = дата срабатывания job.")
  void kindIntervalTime1DateFromCurrentDateDateToJobStarted() {
    Mockito.when(appConfiguration.getKindIntervalTime()).thenReturn(1);

    datesInitializer = new DatesInitializer(appConfiguration, resultSendingRepository);
    DatesDto datesDto = datesInitializer.getDatesDto();
    LocalDateTime dateFrom = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

    Assertions.assertNotNull(datesDto);
    Assertions.assertNotNull(datesDto.dateTo());
    Assertions.assertNotNull(datesDto.dateFrom());
    Assertions.assertEquals(datesDto.dateFrom(), dateFrom);
    Mockito.verify(appConfiguration, Mockito.times(0))
        .getDateFrom();
    Mockito.verify(appConfiguration, Mockito.times(0))
        .getDateTo();
  }

  @Test
  @DisplayName("Свойство KindIntervalTime = 2."
      + "DateFrom = начало вчерашнего дня, DateTo = конец вчерашнего дня.")
  void kindIntervalTime2DateFromStartYesterdayDateToEndYesterday() {
    Mockito.when(appConfiguration.getKindIntervalTime()).thenReturn(2);

    datesInitializer = new DatesInitializer(appConfiguration, resultSendingRepository);
    DatesDto datesDto = datesInitializer.getDatesDto();
    LocalDateTime dateFrom = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).minusDays(1);
    LocalDateTime dateTo = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).minusDays(1);

    Assertions.assertNotNull(datesDto);
    Assertions.assertNotNull(datesDto.dateTo());
    Assertions.assertNotNull(datesDto.dateFrom());
    Assertions.assertEquals(datesDto.dateFrom(), dateFrom);
    Assertions.assertEquals(datesDto.dateTo(), dateTo);
    Mockito.verify(appConfiguration, Mockito.times(0))
        .getDateFrom();
    Mockito.verify(appConfiguration, Mockito.times(0))
        .getDateTo();
  }

  @Test
  @DisplayName("Свойство KindIntervalTime = 3."
      + "DateFrom = дата последней успешной отправки случаев (в таблице hst_MedResultSending есть записи с sendingStatus = true), "
      + "DateTo = дата срабатывания job.")
  void kindIntervalTime3DateFromSuccessfulDataSendingRecordsExistsDateToJobStarted() {
    Mockito.when(appConfiguration.getKindIntervalTime()).thenReturn(3);
    Mockito.when(resultSendingRepository.findLastDateChangeBySendingStatusIsTrue())
        .thenReturn(Optional.of(LocalDateTime.MIN));

    datesInitializer = new DatesInitializer(appConfiguration, resultSendingRepository);
    DatesDto datesDto = datesInitializer.getDatesDto();
    LocalDateTime dateFrom = LocalDateTime.MIN;

    Assertions.assertNotNull(datesDto);
    Assertions.assertNotNull(datesDto.dateTo());
    Assertions.assertNotNull(datesDto.dateFrom());
    Assertions.assertEquals(datesDto.dateFrom(), dateFrom);
    Mockito.verify(appConfiguration, Mockito.times(0))
        .getDateFrom();
    Mockito.verify(appConfiguration, Mockito.times(0))
        .getDateTo();
  }

  @Test
  @DisplayName("Свойство KindIntervalTime = 3."
      + "DateFrom = дата запуска сервиса (в таблице hst_MedResultSending нет записей с sendingStatus = true), "
      + "DateTo = дата срабатывания job.")
  void kindIntervalTime3DateFromStartedServiceDateDateToJobStarted() {
    Mockito.when(appConfiguration.getKindIntervalTime()).thenReturn(3);
    Mockito.when(resultSendingRepository.findLastDateChangeBySendingStatusIsTrue())
        .thenReturn(Optional.empty());
    Mockito.when(appConfiguration.getStartUpDateTime()).thenReturn(LocalDateTime.MAX);

    datesInitializer = new DatesInitializer(appConfiguration, resultSendingRepository);
    DatesDto datesDto = datesInitializer.getDatesDto();
    LocalDateTime dateFrom = LocalDateTime.MAX;

    Assertions.assertNotNull(datesDto);
    Assertions.assertNotNull(datesDto.dateTo());
    Assertions.assertNotNull(datesDto.dateFrom());
    Assertions.assertEquals(datesDto.dateFrom(), dateFrom);
    Mockito.verify(appConfiguration, Mockito.times(0))
        .getDateFrom();
    Mockito.verify(appConfiguration, Mockito.times(0))
        .getDateTo();
  }

  @Test
  @DisplayName("Свойство KindIntervalTime = null."
      + "DateFrom/DateTo из конфига.")
  void dateFromDateToFromConfig() {
    Mockito.when(appConfiguration.getKindIntervalTime()).thenReturn(null);

    datesInitializer = new DatesInitializer(appConfiguration, resultSendingRepository);
    DatesDto datesDto = datesInitializer.getDatesDto();
    LocalDateTime dateFrom = LocalDateTime.MIN;
    LocalDateTime dateTo = LocalDateTime.MAX;

    Assertions.assertNotNull(datesDto);
    Assertions.assertNotNull(datesDto.dateTo());
    Assertions.assertNotNull(datesDto.dateFrom());
    Assertions.assertEquals(datesDto.dateFrom(), dateFrom);
    Assertions.assertEquals(datesDto.dateTo(), dateTo);
    Mockito.verify(appConfiguration, Mockito.times(1))
        .getDateFrom();
    Mockito.verify(appConfiguration, Mockito.times(1))
        .getDateTo();
  }
}
