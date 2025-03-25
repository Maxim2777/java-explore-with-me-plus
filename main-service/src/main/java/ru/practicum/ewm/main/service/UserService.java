package ru.practicum.ewm.main.service;

import ru.practicum.ewm.main.dto.AdminUserParam;
import ru.practicum.ewm.main.dto.NewUserRequest;
import ru.practicum.ewm.main.dto.UserDto;
import ru.practicum.ewm.main.dto.UserShortDto;

import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest request);

    List<UserDto> getUsers(AdminUserParam param);

    void deleteUser(Long userId);

    UserShortDto getShortById(Long userId);
}

