package com.cansuiremkanli.libmanage.service;

import com.cansuiremkanli.libmanage.data.dto.AuthenticationRequest;
import com.cansuiremkanli.libmanage.data.dto.AuthenticationResponse;
import com.cansuiremkanli.libmanage.data.dto.RegisterRequest;

public interface AuthService {
    AuthenticationResponse register(RegisterRequest request);
    AuthenticationResponse authenticate(AuthenticationRequest request);
}
