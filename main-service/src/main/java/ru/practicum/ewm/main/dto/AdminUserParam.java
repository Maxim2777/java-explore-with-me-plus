package ru.practicum.ewm.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserParam {
    private List<Long> ids;
    private int from = 0;
    private int size = 10;
}