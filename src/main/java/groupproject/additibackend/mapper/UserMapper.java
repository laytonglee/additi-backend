package groupproject.additibackend.mapper;

import groupproject.additibackend.model.Role;
import groupproject.additibackend.model.User;
import groupproject.additibackend.request.UserRequest;
import groupproject.additibackend.response.UserResponse;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public UserResponse toResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getRealUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setPhoto(user.getPhoto());
        dto.setBio(user.getBio());
        dto.setEnable(user.isEnable());

        if (user.getRoles() != null) {
            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
            dto.setRoles(roleNames);
        }

        return dto;
    }

    public User toEntity(UserRequest requestDTO) {
        if (requestDTO == null) {
            return null;
        }
        User user = new User();
        user.setUsername(requestDTO.getUsername());
        user.setEmail(requestDTO.getEmail());
        user.setPhoneNumber(requestDTO.getPhoneNumber());
        user.setAddress(requestDTO.getAddress());
        user.setBio(requestDTO.getBio());
        user.setEnable(true);

        return user;
    }
}
