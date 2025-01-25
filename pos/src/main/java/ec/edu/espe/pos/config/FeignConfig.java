package ec.edu.espe.pos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import java.util.ArrayList;
import java.util.List;
import feign.codec.Decoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;

@Configuration
public class FeignConfig {
    
    @Bean
    public Decoder feignDecoder() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new StringHttpMessageConverter());
        return new ResponseEntityDecoder(new SpringDecoder(() -> new HttpMessageConverters(converters)));
    }
} 