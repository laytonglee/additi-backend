package groupproject.additibackend.service;

import groupproject.additibackend.request.RegisterRequest;
import groupproject.additibackend.response.RegisterResponse;

public interface RegisterService {
    RegisterResponse registerUser(RegisterRequest request);
}
