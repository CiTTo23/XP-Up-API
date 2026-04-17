package com.david.xpup.backend.service;

import com.david.xpup.generated.model.InternalPagedPostResponse;

public interface FeedService {

    InternalPagedPostResponse getFeed(String orden, Integer page, Integer size, String nombreJuego);

    InternalPagedPostResponse getFollowingFeed(Integer userId, String orden, Integer page, Integer size);
}