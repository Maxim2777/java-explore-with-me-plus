package ru.practicum.ewm.main.dto.params;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilationParamsPublic {
    private Boolean pinned;
    private int from = 0;
    private int size = 10;
}
