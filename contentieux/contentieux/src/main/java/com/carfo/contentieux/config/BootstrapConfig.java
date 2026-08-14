package com.carfo.contentieux.config;

import com.carfo.contentieux.model.TypeContentieux;
import com.carfo.contentieux.repository.TypeContentieuxRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BootstrapConfig {

    @Bean
    CommandLineRunner bootstrapReferentiels(TypeContentieuxRepository typeContentieuxRepository) {
        return args -> {
            if (typeContentieuxRepository.count() == 0) {
                for (TypeContentieux.Nature nature : TypeContentieux.Nature.values()) {
                    TypeContentieux tc = new TypeContentieux();
                    tc.setNature(nature);
                    typeContentieuxRepository.save(tc);
                }
            }
        };
    }
}
