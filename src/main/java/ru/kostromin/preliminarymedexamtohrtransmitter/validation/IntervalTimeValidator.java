package ru.kostromin.preliminarymedexamtohrtransmitter.validation;

import java.util.stream.Stream;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import ru.kostromin.preliminarymedexamtohrtransmitter.configuration.AppConfiguration;
import ru.kostromin.preliminarymedexamtohrtransmitter.configuration.AppConfiguration.IntervalTimeStrategy;

/**
 * Валидатор для проверки установки периоды выгрузки или стратегии инициализации периода выгрузки
 */
public class IntervalTimeValidator implements ConstraintValidator<IntervalTime, AppConfiguration> {

  @Override
  public void initialize(IntervalTime constraintAnnotation) {
    // do nothing
  }

  /**
   * Метод для проверки, что установлена стратегия инициализации периода выгрузки или период выгрузки
   */
  @Override
  public boolean isValid(AppConfiguration appConfiguration,
      ConstraintValidatorContext constraintValidatorContext) {

    if (appConfiguration.getKindIntervalTime() != null) {
        return Stream.of(IntervalTimeStrategy.values()).anyMatch(
            strategy -> strategy.getCode().equals(appConfiguration.getKindIntervalTime()));
    }

    return appConfiguration.getDateFrom() != null && appConfiguration.getDateTo() != null;
  }
}
