package ru.practicum.ewm.main.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventAdminRequest {
    private String title;
    private String annotation;
    private String description;
    private Long category;
    private LocationDto location;
    private String eventDate;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String stateAction; // "PUBLISH_EVENT", "REJECT_EVENT"
}