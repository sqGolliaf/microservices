package ru.sg.user.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sg.user.event.enums.EmailStatus;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailEvent implements Serializable {

    private Long id;
    private String email;
    private EmailStatus status;
}
