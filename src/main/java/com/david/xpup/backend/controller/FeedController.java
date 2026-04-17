package com.david.xpup.backend.controller;

import com.david.xpup.backend.service.FeedService;
import com.david.xpup.generated.api.FeedApi;
import com.david.xpup.generated.model.InternalPagedPostResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeedController implements FeedApi {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @Override
    public ResponseEntity<InternalPagedPostResponse> getFeed(
            String orden,
            Integer page,
            Integer size,
            String nombreJuego
    ) {
        return ResponseEntity.ok(feedService.getFeed(orden, page, size, nombreJuego));
    }

    @Override
    public ResponseEntity<InternalPagedPostResponse> getFollowingFeed(
            Integer userId,
            String orden,
            Integer page,
            Integer size
    ) {
        return ResponseEntity.ok(feedService.getFollowingFeed(userId, orden, page, size));
    }
}