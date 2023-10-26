package ru.kostromin.preliminarymedexamtohrtransmitter.configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import ru.kostromin.preliminarymedexamtohrtransmitter.validation.IntervalTime;

@Configuration
@Data
@ConfigurationProperties(prefix = "app")
@IntervalTime
@Validated
public class AppConfiguration {

  /**
   * Дата начала периода выгрузки
   */
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate dateFrom;

  /**
   * Дата окончания периода выгрузки
   */
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate dateTo;

  /**
   * Настройка для обозначения стратегии инициализации дат периода выгрузки
   */
  private Integer kindIntervalTime;

  /**
   * Формат даты, в котором необходимо отправлять даты в запросе
   */
  private String dateTimePattern;

  /**
   * {@link Enum} содержащий статусы переданных случаев
   */
  private Map<String, String> requestStatuses;

  /**
   * Дата запуска сервиса, используется в стратегии 3, если ранее случаи не были переданы или отправлены неуспешно
   */
  private LocalDateTime startUpDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

  @Getter
  public enum RequestStatus {

    INSERT("ins"),
    UPDATE("upd");

    private final String statusName;

    RequestStatus(String statusName){
      this.statusName = statusName;
    }
  }

  @Getter
  public enum IntervalTimeStrategy {

    THIS_DAY(1),
    YESTERDAY(2),
    LAST_SUCCESSFUL_SEND(3);

    private final Integer code;

    IntervalTimeStrategy(Integer code){
      this.code = code;
    }
  }
}
