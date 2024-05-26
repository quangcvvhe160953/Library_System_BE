package swp391.learning.application.service.Implements;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import swp391.learning.application.service.AuthenticationService;
import swp391.learning.domain.dto.common.ResponseCommon;
import swp391.learning.domain.dto.request.user.authentication.ChangePasswordRequest;
import swp391.learning.domain.dto.request.user.authentication.CreateUserRequest;
import swp391.learning.domain.dto.request.user.authentication.LogOutRequest;
import swp391.learning.domain.dto.request.user.authentication.LoginRequest;
import swp391.learning.domain.dto.response.user.authentication.ChangePasswordResponse;
import swp391.learning.domain.dto.response.user.authentication.CreateUserResponseDTO;
import swp391.learning.domain.dto.response.user.authentication.LogOutResponse;
import swp391.learning.domain.entity.User;
import swp391.learning.domain.enums.EnumTypeStatus;
import swp391.learning.domain.enums.ResponseCode;
import swp391.learning.repository.AuthenticationRepository;
import swp391.learning.security.SecurityUtils;
import swp391.learning.security.UserDetailsImpl;
import swp391.learning.security.jwt.JWTResponse;
import swp391.learning.security.jwt.JWTUtils;
import swp391.learning.utils.CommonUtils;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthenticationImpl implements AuthenticationService {
    private final AuthenticationRepository authenticationRepository;
    private final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    @Override
    public ResponseCommon<CreateUserResponseDTO> createUser(CreateUserRequest requestDTO) {
        try {
            User user = authenticationRepository.findByEmail(requestDTO.getEmail()).orElse(null);
            // Kiểm tra xem người dùng đã tồn tại và trạng thái là IN_PROCESS hay không
            if (Objects.nonNull(user) && user.getStatus() != EnumTypeStatus.IN_PROCESS) {
                return new ResponseCommon<>(ResponseCode.USER_EXIST, null);
            }
            // Nếu người dùng không tồn tại, tạo mới
            if (Objects.isNull(user)) {
                user = new User();
            }

            // Thiết lập thông tin người dùng
            user.setUsername(genUserFromEmail(requestDTO.getEmail()));
            user.setPassword(requestDTO.getPassword());
            user.setEmail(requestDTO.getEmail());
            user.setPhone(requestDTO.getPhone());
            user.setRole(requestDTO.getRole());
            user.setFullName(requestDTO.getFullName());
            user.setGender(requestDTO.getGender());
            user.setDate_of_birth(requestDTO.getDateOfBirth());
            user.setCreatedAt(LocalDateTime.now());
            User createdUser = authenticationRepository.save(user);

            // Tạo phản hồi
            CreateUserResponseDTO responseDTO = new CreateUserResponseDTO();
            responseDTO.setUsername(createdUser.getUsername());
            responseDTO.setEmail(createdUser.getEmail());
            responseDTO.setCreatedAt(createdUser.getCreatedAt());

            return new ResponseCommon<>(ResponseCode.SUCCESS, responseDTO);
        } catch (Exception e) {
            log.error("create user fail", e);
            return new ResponseCommon<>(ResponseCode.FAIL, null);
        }
    }

    @Override
    public String genUserFromEmail(String email) {
        String username = email.substring(0, email.indexOf("@"));
        Random random = new Random();
        StringBuilder randomNumber = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int digit = random.nextInt(10);
            randomNumber.append(digit);
        }
        String result = username + randomNumber.toString();
        return result;
    }

    @Override
    public ResponseCommon<JWTResponse> login(LoginRequest loginRequest) {
        try {
            Optional<User> user = authenticationRepository.findByEmail(loginRequest.getUsername());
            // if username request not found in database -> tell user
            if (user.isEmpty()) {
                return new ResponseCommon<>(ResponseCode.USER_NOT_FOUND, null);
            } // else -> check password
            else {
                JWTUtils utils = new JWTUtils();
                UserDetailsImpl userDetails = UserDetailsImpl.build(user.get());
                String accessToken = utils.generateAccessToken(userDetails);
                String refreshToken = utils.generateRefreshToken(userDetails);
                user.orElse(null).setSession_id(CommonUtils.getSessionID());
                authenticationRepository.save(user.get());
                return new ResponseCommon<>(new JWTResponse(accessToken, refreshToken, ResponseCode.SUCCESS.getMessage()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseCommon<>(ResponseCode.FAIL, null);
        }
    }

    @Override
    public ResponseCommon<LogOutResponse> logOut(LogOutRequest logOutRequest) {
        try {
            User user = authenticationRepository.findByUsername(logOutRequest.getUsername()).orElse(null);
            if(!Objects.isNull(user)){
                user.setSession_id(null);
                authenticationRepository.save(user);
                return new ResponseCommon<>(ResponseCode.SUCCESS.getCode(), "Logout successful", null);
            } else {
                return new ResponseCommon<>(ResponseCode.FAIL.getCode(), "Logout failed", null);
            }
        }catch (Exception e) {
            e.printStackTrace();
            log.error("Log out failed");
            return new ResponseCommon<>(ResponseCode.FAIL.getCode(),"Log out failed",null);
        }
    }

    @Override
    public ResponseCommon<ChangePasswordResponse> changePassword(ChangePasswordRequest changePasswordRequest) {
        try {
            String username = SecurityUtils.getUsernameAuth();
            System.out.println(username);
            User user = authenticationRepository.findByUsername(username).orElse(null);
            // if user is null -> tell error
            log.debug("change password with username {}", username);
            if (Objects.isNull(user)) {
                return new ResponseCommon<>(ResponseCode.USER_NOT_FOUND, null);
            } else {
                // if oldPassword not correct -> tell user
                if (!changePasswordRequest.getOldPassword().equals(user.getPassword())) {
                    return new ResponseCommon<>(ResponseCode.PASSWORD_INCORRECT, null);
                } else {
                    user.setPassword(changePasswordRequest.getNewPassword());
                    authenticationRepository.save(user);
                    return new ResponseCommon<>(ResponseCode.SUCCESS, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseCommon<>(new ChangePasswordResponse("Error"));
        }
    }



}