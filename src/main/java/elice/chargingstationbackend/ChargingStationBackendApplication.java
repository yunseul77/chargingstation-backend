package elice.chargingstationbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ChargingStationBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChargingStationBackendApplication.class, args);
    }

}
