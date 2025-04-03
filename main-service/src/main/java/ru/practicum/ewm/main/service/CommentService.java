package ru.practicum.ewm.main.service;

import ru.practicum.ewm.main.dto.CommentDto;
import ru.practicum.ewm.main.dto.NewCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(Long userId, NewCommentDto newCommentDto);

    void deleteOwnComment(Long userId, Long commentId);

    List<CommentDto> getCommentsByEvent(Long eventId, int from, int size);

    List<CommentDto> getUserComments(Long userId, int from, int size);

    void deleteByAdmin(Long commentId);
}
