package com.david.xpup.backend.controller;

import com.david.xpup.backend.exception.UnauthorizedException;
import com.david.xpup.backend.service.ExperienciaService;
import com.david.xpup.generated.api.ExperienciaApi;
import com.david.xpup.generated.model.InternalExperienceRequest;
import com.david.xpup.generated.model.InternalExperienceResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExperienciaController implements ExperienciaApi {

    private final ExperienciaService experienciaService;

    public ExperienciaController(ExperienciaService experienciaService) {
        this.experienciaService = experienciaService;
    }

    @Override
    public ResponseEntity<InternalExperienceResponse> getUserExperience(Integer userId) {
        return ResponseEntity.ok(experienciaService.getUserExperience(userId));
    }

}