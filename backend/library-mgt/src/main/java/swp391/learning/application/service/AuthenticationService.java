package swp391.learning.application.service;

import swp391.learning.domain.dto.common.ResponseCommon;
import swp391.learning.domain.dto.request.user.authentication.ChangePasswordRequest;
import swp391.learning.domain.dto.request.user.authentication.CreateUserRequest;
import swp391.learning.domain.dto.request.user.authentication.LoginRequest;
import swp391.learning.domain.dto.response.user.authentication.ChangePasswordResponse;
import swp391.learning.domain.dto.response.user.authentication.CreateUserResponseDTO;
import swp391.learning.security.jwt.JWTResponse;

public interface AuthenticationService {
    ResponseCommon<CreateUserResponseDTO> createUser(CreateUserRequest requestDTO);

    String genUserFromEmail(String email);

    ResponseCommon<JWTResponse> login(LoginRequest loginRequest);
    ResponseCommon<ChangePasswordResponse> changePassword(ChangePasswordRequest changePasswordRequest);
}