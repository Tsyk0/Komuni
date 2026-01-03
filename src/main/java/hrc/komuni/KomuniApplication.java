package hrc.komuni;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@MapperScan("hrc.komuni.mapper")
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class KomuniApplication {

    public static void main(String[] args) {
        SpringApplication.run(KomuniApplication.class, args);
    }

}
