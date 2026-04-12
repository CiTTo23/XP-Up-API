package com.david.xpup.backend.service;

import com.david.xpup.generated.model.AuthLoginRequest;
import com.david.xpup.generated.model.AuthLoginResponse;
import com.david.xpup.generated.model.AuthRegisterRequest;
import com.david.xpup.generated.model.AuthRegisterResponse;
import com.david.xpup.generated.model.MessageResponse;

public interface AuthService {

    AuthRegisterResponse registerUser(AuthRegisterRequest request);

    AuthLoginResponse loginUser(AuthLoginRequest request);

    MessageResponse logoutUser();
}