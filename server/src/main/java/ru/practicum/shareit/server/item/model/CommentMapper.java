package ru.practicum.shareit.server.item.model;

import ru.practicum.shareit.server.item.model.dto.CommentRequestDto;
import ru.practicum.shareit.server.item.model.dto.CommentResponseDto;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;

public class CommentMapper {

    public static Comment toComment(CommentRequestDto commentRequestDto, long commentId, LocalDateTime created,
                                    Item item, User author) {
        return new Comment(
                commentId,
                commentRequestDto.getText(),
                created,
                item,
                author
        );
    }

    public static CommentResponseDto toCommentResponseDto(Comment comment) {
        return new CommentResponseDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }
}
