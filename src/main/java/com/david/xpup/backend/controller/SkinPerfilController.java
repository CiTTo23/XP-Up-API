/***********************************************************************************************************************
*   Controller REST para la gestión de skins de perfil del sistema XP-Up                                              *
*                                                                                                                      *
*   Expone los endpoints relacionados con la consulta y selección de skins desbloqueables para el perfil de usuario    *
*                                                                                                                      *
*   Endpoints principales:                                                                                             *
*       - GET /api/users/{userId}/skins                                                                                *
*       - PUT /api/users/{userId}/skin                                                                                 *
*                                                                                                                      *
*   Responsabilidades principales:                                                                                     *
*       - Recibir peticiones HTTP relacionadas con skins de perfil                                                     *
*       - Delegar la lógica de negocio en SkinPerfilService                                                            *
*       - Devolver DTOs definidos en el API Contract                                                                   *
*                                                                                                                      *
*   Importante:                                                                                                        *
*       - El desbloqueo de skins se calcula en la capa service según el nivel del usuario                              *
*       - El controller no contiene lógica de negocio                                                                  *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.controller;

import com.david.xpup.backend.service.SkinPerfilService;
import com.david.xpup.generated.model.InternalProfileSkinUpdateRequest;
import com.david.xpup.generated.model.InternalUserProfileResponse;
import com.david.xpup.generated.model.InternalUserProfileSkinResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//Controlador REST para endpoints de skins de perfil
@RestController
@RequestMapping("/api/users")
public class SkinPerfilController {

    private final SkinPerfilService skinPerfilService;

    public SkinPerfilController(SkinPerfilService skinPerfilService) {
        this.skinPerfilService = skinPerfilService;
    }

    //Obtiene todas las skins disponibles para un usuario, indicando cuáles están desbloqueadas y cuál está equipada
    //GET /api/users/{userId}/skins
    @GetMapping("/{userId}/skins")
    public ResponseEntity<List<InternalUserProfileSkinResponse>> getUserProfileSkins(
            @PathVariable Integer userId
    ) {
        List<InternalUserProfileSkinResponse> response = skinPerfilService.getUserProfileSkins(userId);
        return ResponseEntity.ok(response);
    }

    //Actualiza la skin activa del perfil de un usuario
    //PUT /api/users/{userId}/skin
    @PutMapping("/{userId}/skin")
    public ResponseEntity<InternalUserProfileResponse> updateUserProfileSkin(
            @PathVariable Integer userId,
            @Valid @RequestBody InternalProfileSkinUpdateRequest request
    ) {
        InternalUserProfileResponse response = skinPerfilService.updateUserProfileSkin(userId, request);
        return ResponseEntity.ok(response);
    }
}