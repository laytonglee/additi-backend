package groupproject.additibackend.service.impl;

import groupproject.additibackend.model.User;
import groupproject.additibackend.repository.UserRepository;
import groupproject.additibackend.request.RegisterRequest;
import groupproject.additibackend.response.RegisterResponse;
import groupproject.additibackend.service.RegisterService;
import org.springframework.stereotype.Service;

@Service
public class RegisterServiceImpl implements RegisterService{

    private final UserRepository userRepository;

    public RegisterServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public RegisterResponse registerUser(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.setEmail());
        user

        return null;
    }
}
