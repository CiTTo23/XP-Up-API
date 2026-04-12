package com.david.xpup.backend.service;

import com.david.xpup.generated.model.InternalPostSummaryResponse;
import com.david.xpup.generated.model.InternalUserProfileResponse;
import com.david.xpup.generated.model.InternalUserSummaryResponse;
import com.david.xpup.generated.model.InternalUserUpdateRequest;

import java.util.List;

public interface UsuarioService {

    InternalUserProfileResponse getUserProfile(Integer userId);

    InternalUserProfileResponse updateUserProfile(Integer userId, InternalUserUpdateRequest request);

    List<InternalPostSummaryResponse> getUserPosts(Integer userId);

    List<InternalPostSummaryResponse> getUserLikedPosts(Integer userId);

    List<InternalPostSummaryResponse> getUserSavedPosts(Integer userId);

    List<InternalUserSummaryResponse> getUserFollowing(Integer userId);

    List<InternalUserSummaryResponse> getUserFollowers(Integer userId);
}