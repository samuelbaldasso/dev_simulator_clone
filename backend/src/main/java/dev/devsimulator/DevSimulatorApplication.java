package dev.devsimulator;

import dev.devsimulator.challenge.ChallengePaginationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ChallengePaginationProperties.class)
public class DevSimulatorApplication {
  public static void main(String[] args) { SpringApplication.run(DevSimulatorApplication.class, args); }
}
