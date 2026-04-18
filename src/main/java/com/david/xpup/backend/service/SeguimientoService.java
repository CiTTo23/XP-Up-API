package com.david.xpup.backend.service;

import com.david.xpup.generated.model.InternalFollowCheckResponse;
import com.david.xpup.generated.model.InternalFollowRequest;
import com.david.xpup.generated.model.InternalFollowStatsResponse;
import com.david.xpup.generated.model.MessageResponse;

public interface SeguimientoService {

    MessageResponse followUser(InternalFollowRequest request);

    MessageResponse unfollowUser(InternalFollowRequest request);

    InternalFollowCheckResponse checkUserFollow(Integer idSeguidor, Integer idSeguido);

    InternalFollowStatsResponse getFollowStats(Integer userId);
}