package ru.sg.repo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import ru.sg.model.Onto;

import java.io.IOException;

@Slf4j
@Repository
public class OntoRepository {

    @Bean
    public Onto dataOnto(@Value("${app.onto.url:classpath:data/main.ont}") Resource resource) throws IOException {
        log.info("Loading ontology from: {}", resource.getFilename());

        try {
            Onto onto = new Onto(resource.getInputStream());
            log.info("Ontology loaded successfully: {}", onto);
            return onto;
        } catch (IOException e) {
            log.error("Failed to load ontology from: {}", resource.getFilename(), e);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("Invalid ontology format: {}", e.getMessage(), e);
            throw e;
        }
    }
}
