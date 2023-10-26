package ru.kostromin.preliminarymedexamtohrtransmitter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import ru.kostromin.preliminarymedexamtohrtransmitter.service.MedicalResultRunner;

@SpringBootApplication
@EnableFeignClients
public class PreliminaryMedExamToHrTransmitterApplication {

	public static void main(String[] args) {
		SpringApplication.run(PreliminaryMedExamToHrTransmitterApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(MedicalResultRunner medicalResultRunner, ApplicationContext context){
		return args -> medicalResultRunner.runMedicalResultTransmitter();
	}
}
