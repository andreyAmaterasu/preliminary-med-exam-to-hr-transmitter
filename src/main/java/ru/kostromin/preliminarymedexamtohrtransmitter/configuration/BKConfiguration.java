package ru.kostromin.preliminarymedexamtohrtransmitter.configuration;

import java.util.Collection;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация сервиса БК
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "app.services.bk")
public class BKConfiguration {

  /**
   * url сервиса для отправки
   */
  private String url;

  /**
   * Дополнительные headers для отправоляемого запроса
   */
  private Map<String, Collection<String>> headers;

  /**
   * Флаг - присутствует ли авторизация
   */
  private Boolean hasAuthorization = false;

  /**
   * Пользователь авторизации
   */
  private String user;

  /**
   * Пароль авторизации
   */
  private String password;
}
