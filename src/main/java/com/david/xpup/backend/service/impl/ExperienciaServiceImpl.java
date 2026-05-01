/***********************************************************************************************************************
*   Implementación del Experiencia Service del sistema XP-Up                                                           *
*                                                                                                                      *
*   Esta clase contiene la lógica asociada a la gestión de experiencia y nivel de los usuarios dentro de la          *
*   plataforma                                                                                                         *
*                                                                                                                      *
*   Metodos principales:                                                                                               *
*       - Obtener la experiencia de un usuario                                                                         *
*       - Añadir experiencia al dar like                                                                               *
*       - Añadir experiencia al comentar                                                                               *
*       - Añadir experiencia al publicar                                                                               *
*                                                                                                                      *
*   Para ello, coordina los repositorios del sistema y aplica la lógica de negocio asociada al cálculo del nivel      *
*   en función de la experiencia acumulada                                                                             *
*                                                                                                                      *
*   Importante:                                                                                                        *
*       - La experiencia solo puede ser modificada internamente por el backend                                         *
*       - Centraliza la lógica de suma de experiencia y cálculo de nivel en métodos privados reutilizables            *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.Experiencia;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.backend.exception.ResourceNotFoundException;
import com.david.xpup.backend.mapper.ExperienciaMapper;
import com.david.xpup.backend.repository.ExperienciaRepository;
import com.david.xpup.backend.repository.UsuarioRepository;
import com.david.xpup.backend.service.ExperienciaService;
import com.david.xpup.generated.model.InternalExperienceResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//Capa intermedia entre controller y repository -> lógica de negocio
@Service
public class ExperienciaServiceImpl implements ExperienciaService {

    private static final int XP_LIKE = 2;
    private static final int XP_COMMENT = 5;
    private static final int XP_POST = 10;
    private static final int XP_PER_LEVEL = 25;

    private final ExperienciaRepository experienciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ExperienciaMapper experienciaMapper;

    public ExperienciaServiceImpl(
            ExperienciaRepository experienciaRepository,
            UsuarioRepository usuarioRepository,
            ExperienciaMapper experienciaMapper
    ) {
        this.experienciaRepository = experienciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.experienciaMapper = experienciaMapper;
    }


    //Obtiene la experiencia y el nivel actual de un usuario -> GET /api/experience/{userId}
    @Override
    public InternalExperienceResponse getUserExperience(Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Experiencia experiencia = experienciaRepository.findByUsuario(usuario).orElse(null);

        return experienciaMapper.toExperienceResponse(usuario, experiencia);
    }


    //Añade experiencia al usuario por realizar un like
    @Transactional
    @Override
    public void addExperienceForLike(Usuario usuario) {
        addExperience(usuario, XP_LIKE);
    }


    //Añade experiencia al usuario por realizar un comentario
    @Transactional
    @Override
    public void addExperienceForComment(Usuario usuario) {
        addExperience(usuario, XP_COMMENT);
    }


    //Añade experiencia al usuario por crear una publicación
    @Transactional
    @Override
    public void addExperienceForPost(Usuario usuario) {
        addExperience(usuario, XP_POST);
    }


    //Suma experiencia al usuario y recalcula automáticamente su nivel
    private void addExperience(Usuario usuario, int puntos) {
        Experiencia experiencia = experienciaRepository.findByUsuario(usuario)
                .orElseGet(() -> Experiencia.builder()
                        .usuario(usuario)
                        .xpTotal(0)
                        .nivel(1)
                        .build());

        int nuevoXpTotal = experiencia.getXpTotal() + puntos;
        int nuevoNivel = calculateLevel(nuevoXpTotal);

        experiencia.setXpTotal(nuevoXpTotal);
        experiencia.setNivel(nuevoNivel);

        experienciaRepository.save(experiencia);
    }


    //Calcula el nivel del usuario en función de la experiencia total acumulada
    private int calculateLevel(int xpTotal) {
        return 1 + (xpTotal / XP_PER_LEVEL);
    }
}