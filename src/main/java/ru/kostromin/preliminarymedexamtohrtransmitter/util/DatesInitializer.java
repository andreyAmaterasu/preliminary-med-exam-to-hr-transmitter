package ru.kostromin.preliminarymedexamtohrtransmitter.util;

import static ru.kostromin.preliminarymedexamtohrtransmitter.configuration.AppConfiguration.IntervalTimeStrategy.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kostromin.preliminarymedexamtohrtransmitter.configuration.AppConfiguration;
import ru.kostromin.preliminarymedexamtohrtransmitter.data.repository.HstMedResultSendingRepository;

/**
 * Инициализатор дат периода выгрузки
 */
@Component
@Slf4j
public class DatesInitializer {

  @Getter
  public final DatesDto datesDto;

  /**
   * Инициализация периода выгрузки по указанной в конфиге стратегии.
   * Стратегия инициализации периода выгрузки следующая:
   * IntervalTimeStrategy:
   * - THIS_DAY: dateFrom = начало текущего дня, dateTo = дата срабатывания job;
   * - YESTERDAY: dateFrom = начало вчерашнего дня, dateTo = конец вчерашнего дня;
   * - LAST_SUCCESSFUL_SEND: dateFrom = дата последнего успешно отправленного случая, dateTo = дата срабатывания job.
   *      Если случаи ранее не были отправлены или отправлены неуспешно, то dateFrom = дата запуска сервиса;
   * - null: dateFrom/dateTo из конфига.
   */
  @Autowired
  public DatesInitializer(
      AppConfiguration appConfiguration,
      HstMedResultSendingRepository resultSendingRepository) {

    if (appConfiguration.getKindIntervalTime() != null) {
      Map<Integer, Supplier<DatesDto>> strategies = Map.of(
          THIS_DAY.getCode(),
          () -> new DatesDto(LocalDateTime.of(LocalDate.now(), LocalTime.MIN), LocalDateTime.now()),
          YESTERDAY.getCode(),
          () -> new DatesDto(LocalDateTime.of(LocalDate.now(), LocalTime.MIN).minusDays(1),
              LocalDateTime.of(LocalDate.now(), LocalTime.MAX).minusDays(1)),
          LAST_SUCCESSFUL_SEND.getCode(),
          () -> new DatesDto(resultSendingRepository.findLastDateChangeBySendingStatusIsTrue()
              .orElseGet(appConfiguration::getStartUpDateTime), LocalDateTime.now()));
      datesDto = strategies.get(appConfiguration.getKindIntervalTime()).get();
    } else {
      datesDto = new DatesDto(LocalDateTime.of(appConfiguration.getDateFrom(), LocalTime.MIN),
          LocalDateTime.of(appConfiguration.getDateTo(), LocalTime.MAX));
    }
  }

  /**
   * Представление периода выгрузки
   *
   * @param dateFrom дата начала периода выгрузки
   * @param dateTo дата окончания периода выгрузки
   */
  public record DatesDto(LocalDateTime dateFrom, LocalDateTime dateTo) {

  }
}
