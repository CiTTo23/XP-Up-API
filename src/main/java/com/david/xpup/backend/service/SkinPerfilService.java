/***********************************************************************************************************************
*   Service para la gestión de skins de perfil en XP-Up                                                                *
*                                                                                                                      *
*   Define las operaciones principales relacionadas con las skins desbloqueables del perfil de usuario                 *
*                                                                                                                      *
*   Funcionalidades principales:                                                                                       *
*       - Obtener la lista de skins disponibles para un usuario                                                        *
*       - Indicar qué skins están desbloqueadas según el nivel del usuario                                             *
*       - Indicar qué skin está actualmente equipada                                                                   *
*       - Cambiar la skin activa del perfil de un usuario                                                              *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.service;

import com.david.xpup.generated.model.InternalProfileSkinUpdateRequest;
import com.david.xpup.generated.model.InternalUserProfileResponse;
import com.david.xpup.generated.model.InternalUserProfileSkinResponse;

import java.util.List;

public interface SkinPerfilService {

    List<InternalUserProfileSkinResponse> getUserProfileSkins(Integer userId);

    InternalUserProfileResponse updateUserProfileSkin(Integer userId, InternalProfileSkinUpdateRequest request);
}