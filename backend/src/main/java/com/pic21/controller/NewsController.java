/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.controller.NewsController
 *  com.pic21.domain.NewsReaction$ReactionType
 *  com.pic21.dto.request.NewsRequest
 *  com.pic21.dto.response.NewsResponse
 *  com.pic21.service.NewsService
 *  jakarta.validation.Valid
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.security.core.annotation.AuthenticationPrincipal
 *  org.springframework.security.core.userdetails.UserDetails
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package com.pic21.controller;

import com.pic21.domain.NewsReaction;
import com.pic21.dto.request.NewsRequest;
import com.pic21.dto.response.NewsResponse;
import com.pic21.service.NewsService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/news"})
public class NewsController {
    private final NewsService newsService;

    @GetMapping
    public ResponseEntity<List<NewsResponse>> getAll(@AuthenticationPrincipal UserDetails me) {
        return ResponseEntity.ok((Object)this.newsService.findAll(me.getUsername()));
    }

    @GetMapping(value={"/{id}"})
    public ResponseEntity<NewsResponse> getById(@PathVariable Long id, @AuthenticationPrincipal UserDetails me) {
        return ResponseEntity.ok((Object)this.newsService.findById(id, me.getUsername()));
    }

    @PostMapping
    @PreAuthorize(value="hasRole('R04_ADMIN')")
    public ResponseEntity<NewsResponse> create(@Valid @RequestBody NewsRequest request, @AuthenticationPrincipal UserDetails me) {
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CREATED).body((Object)this.newsService.create(request, me.getUsername()));
    }

    @PutMapping(value={"/{id}"})
    @PreAuthorize(value="hasRole('R04_ADMIN')")
    public ResponseEntity<NewsResponse> update(@PathVariable Long id, @Valid @RequestBody NewsRequest request, @AuthenticationPrincipal UserDetails me) {
        return ResponseEntity.ok((Object)this.newsService.update(id, request, me.getUsername()));
    }

    @DeleteMapping(value={"/{id}"})
    @PreAuthorize(value="hasRole('R04_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        this.newsService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value={"/preview"})
    @PreAuthorize(value="hasRole('R04_ADMIN')")
    public ResponseEntity<Map<String, String>> preview(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL es requerida"));
        }
        return ResponseEntity.ok((Object)this.newsService.fetchOpenGraphPreview(url));
    }

    @PostMapping(value={"/{id}/like"})
    public ResponseEntity<NewsResponse> like(@PathVariable Long id, @AuthenticationPrincipal UserDetails me) {
        return ResponseEntity.ok((Object)this.newsService.toggleReaction(id, me.getUsername(), NewsReaction.ReactionType.LIKE));
    }

    @PostMapping(value={"/{id}/dislike"})
    public ResponseEntity<NewsResponse> dislike(@PathVariable Long id, @AuthenticationPrincipal UserDetails me) {
        return ResponseEntity.ok((Object)this.newsService.toggleReaction(id, me.getUsername(), NewsReaction.ReactionType.DISLIKE));
    }

    @DeleteMapping(value={"/{id}/reaction"})
    public ResponseEntity<NewsResponse> removeReaction(@PathVariable Long id, @AuthenticationPrincipal UserDetails me) {
        return ResponseEntity.ok((Object)this.newsService.removeReaction(id, me.getUsername()));
    }

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }
}

