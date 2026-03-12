package ru.sg.back.repo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import ru.sg.models.Onto;

import java.io.IOException;

@Repository
public class OntoRepository {

    @Bean
    public Onto dataOnto(@Value("${file.onto.url}") Resource resource) throws IOException {
        return new Onto(resource.getInputStream());
    }
}
