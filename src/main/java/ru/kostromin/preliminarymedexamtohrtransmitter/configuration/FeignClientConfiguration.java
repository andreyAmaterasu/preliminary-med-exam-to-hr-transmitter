package ru.kostromin.preliminarymedexamtohrtransmitter.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jaxb.JAXBContextFactory;
import feign.soap.SOAPDecoder;
import feign.soap.SOAPEncoder;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FeignClientConfiguration {

  private static final String AUTHORIZATION_HEADER = "Authorization";

  private final BKConfiguration bkConfiguration;

  private static final JAXBContextFactory jaxbFactory = new JAXBContextFactory.Builder()
      .withMarshallerJAXBEncoding("UTF-8")
      .build();

  @Bean
  public Encoder feignEncoder() {
    return new SOAPEncoder(jaxbFactory);
  }
  @Bean
  public Decoder feignDecoder() {
    return new SOAPDecoder(jaxbFactory);
  }

  @Bean
  public RequestInterceptor smpFeignCallRequestInterceptor() {
    return template -> {
      template.target(bkConfiguration.getUrl());
      template.headers(bkConfiguration.getHeaders());

      if (bkConfiguration.getHasAuthorization()) {
        addAuthorizationHeader(template);
      }
    };
  }

  /**
   * Добавить авторизацию для исходного запроса
   * @param template - запрос
   */
  private void addAuthorizationHeader(RequestTemplate template) {
    final String authCredentials = bkConfiguration.getUser() + ":" + bkConfiguration.getPassword();
    final String encodedCredentials = Base64.getEncoder()
        .encodeToString(authCredentials.getBytes());

    template.header(AUTHORIZATION_HEADER, "Basic " + encodedCredentials);
  }
}
