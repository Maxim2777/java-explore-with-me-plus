package ru.practicum.ewm.main.dto.params;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventParamsAdmin {

    private List<Long> users; // список id пользователей, чьи события нужно найти

    private List<String> states; // список состояний в которых находятся искомые события

    private List<Long> categories; // список id категорий в которых будет вестись поиск

    private String rangeStart; // дата и время не раньше которых должно произойти событие

    private String rangeEnd; // дата и время не позже которых должно произойти событие

    @PositiveOrZero
    private Integer from = 0; // количество событий, которые нужно пропустить // Default value : 0

    @Positive
    private Integer size = 10; // количество событий в наборе // Default value : 10
}