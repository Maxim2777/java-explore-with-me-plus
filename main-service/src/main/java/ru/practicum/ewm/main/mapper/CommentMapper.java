package ru.practicum.ewm.main.mapper;

import ru.practicum.ewm.main.dto.CommentDto;
import ru.practicum.ewm.main.dto.NewCommentDto;
import ru.practicum.ewm.main.model.Comment;

import java.time.LocalDateTime;

public class CommentMapper {

        public static CommentDto toDto(Comment comment) {
            return CommentDto.builder()
                    .id(comment.getId())
                    .authorId(comment.getAuthorId())
                    .eventId(comment.getEventId())
                    .text(comment.getText())
                    .createdOn(comment.getCreatedOn())
                    .build();
        }

        public static Comment toEntity(NewCommentDto dto, Long userId) {
            return Comment.builder()
                    .authorId(userId)
                    .eventId(dto.getEventId())
                    .text(dto.getText())
                    .createdOn(LocalDateTime.now())
                    .build();
        }
}