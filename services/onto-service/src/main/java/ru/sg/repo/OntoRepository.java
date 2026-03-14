package ru.sg.repo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import ru.sg.model.Onto;

import java.io.IOException;

@Repository
public class OntoRepository {

    @Bean
    public Onto dataOnto(@Value("${app.onto.url}") Resource resource) throws IOException {
        return new Onto(resource.getInputStream());
    }
}
