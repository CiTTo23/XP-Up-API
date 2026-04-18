package com.david.xpup.backend.service;

import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.generated.model.InternalExperienceResponse;

public interface ExperienciaService {

    InternalExperienceResponse getUserExperience(Integer userId);

    void addExperienceForLike(Usuario usuario);

    void addExperienceForComment(Usuario usuario);

    void addExperienceForPost(Usuario usuario);
}