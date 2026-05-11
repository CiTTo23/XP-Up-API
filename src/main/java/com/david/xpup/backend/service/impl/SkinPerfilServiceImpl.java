/***********************************************************************************************************************
 *   Implementación del SkinPerfil Service del sistema XP-Up                                                           *
 *                                                                                                                      *
 *   Esta clase contiene la lógica asociada a la consulta y selección de skins desbloqueables para el perfil de usuario *
 *                                                                                                                      *
 *   Metodos principales:                                                                                               *
 *       - Obtener las skins de perfil disponibles para un usuario                                                      *
 *       - Calcular si cada skin está desbloqueada según el nivel del usuario                                           *
 *       - Calcular qué skin está actualmente equipada                                                                  *
 *       - Cambiar la skin activa del perfil de un usuario                                                              *
 *                                                                                                                      *
 *   Para ello, coordina diferentes repositorios del sistema, principalmente usuarios, experiencia y skins de perfil    *
 *                                                                                                                      *
 *   Importante:                                                                                                        *
 *       - Las skins desbloqueadas no se almacenan en base de datos                                                     *
 *       - El desbloqueo se calcula dinámicamente mediante nivelUsuario >= nivelRequeridoSkin                           *
 *       - Solo se almacena en usuarios la skin actualmente equipada                                                    *
 *                                                                                                                      *
 ***********************************************************************************************************************/

package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.Experiencia;
import com.david.xpup.backend.entity.SkinPerfil;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.backend.exception.ResourceNotFoundException;
import com.david.xpup.backend.exception.UnauthorizedException;
import com.david.xpup.backend.mapper.SkinPerfilMapper;
import com.david.xpup.backend.repository.ExperienciaRepository;
import com.david.xpup.backend.repository.SkinPerfilRepository;
import com.david.xpup.backend.repository.UsuarioRepository;
import com.david.xpup.backend.service.SkinPerfilService;
import com.david.xpup.backend.service.UsuarioService;
import com.david.xpup.generated.model.InternalProfileSkinUpdateRequest;
import com.david.xpup.generated.model.InternalUserProfileResponse;
import com.david.xpup.generated.model.InternalUserProfileSkinResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//Capa intermedia entre controller y repository -> lógica de negocio de skins de perfil
@Service
public class SkinPerfilServiceImpl implements SkinPerfilService {

    private final UsuarioRepository usuarioRepository;
    private final ExperienciaRepository experienciaRepository;
    private final SkinPerfilRepository skinPerfilRepository;
    private final UsuarioService usuarioService;
    private final SkinPerfilMapper skinPerfilMapper;

    public SkinPerfilServiceImpl(
            UsuarioRepository usuarioRepository,
            ExperienciaRepository experienciaRepository,
            SkinPerfilRepository skinPerfilRepository,
            UsuarioService usuarioService,
            SkinPerfilMapper skinPerfilMapper
    ) {
        this.usuarioRepository = usuarioRepository;
        this.experienciaRepository = experienciaRepository;
        this.skinPerfilRepository = skinPerfilRepository;
        this.usuarioService = usuarioService;
        this.skinPerfilMapper = skinPerfilMapper;
    }

    //Obtiene las skins disponibles para un usuario indicando si están desbloqueadas y cuál está equipada
    //GET /api/users/{userId}/skins
    @Transactional(readOnly = true)
    @Override
    public List<InternalUserProfileSkinResponse> getUserProfileSkins(Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Experiencia experiencia = experienciaRepository.findByUsuario(usuario).orElse(null);
        int nivelUsuario = experiencia != null ? experiencia.getNivel() : 1;

        Integer idSkinEquipada = usuario.getSkinPerfilActiva() != null
                ? usuario.getSkinPerfilActiva().getId()
                : null;

        List<SkinPerfil> skins = skinPerfilRepository.findByActivaTrueOrderByNivelRequeridoAsc();

        return skins.stream()
                .map(skin -> skinPerfilMapper.toUserProfileSkinResponse(skin, nivelUsuario, idSkinEquipada))
                .toList();
    }

    //Actualiza la skin activa del perfil siempre que el usuario autenticado sea el propietario y tenga nivel suficiente
    //PUT /api/users/{userId}/skin
    @Transactional
    @Override
    public InternalUserProfileResponse updateUserProfileSkin(Integer userId, InternalProfileSkinUpdateRequest request) {
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        if (!usuarioAutenticado.getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to update this profile skin.");
        }

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        SkinPerfil skinSeleccionada = skinPerfilRepository.findByIdAndActivaTrue(request.getIdSkin())
                .orElseThrow(() -> new ResourceNotFoundException("Profile skin not found with id: " + request.getIdSkin()));

        Experiencia experiencia = experienciaRepository.findByUsuario(usuario).orElse(null);
        int nivelUsuario = experiencia != null ? experiencia.getNivel() : 1;

        if (nivelUsuario < skinSeleccionada.getNivelRequerido()) {
            throw new UnauthorizedException("User level is not high enough to unlock this profile skin.");
        }

        usuario.setSkinPerfilActiva(skinSeleccionada);
        usuarioRepository.save(usuario);

        return usuarioService.getUserProfile(userId);
    }

    //Obtiene el usuario autenticado actual a partir del SecurityContext de Spring Security
    private Usuario getAuthenticatedUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new UnauthorizedException("User is not authenticated.");
        }

        String nombreUsuario = authentication.getName();

        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found."));
    }
}