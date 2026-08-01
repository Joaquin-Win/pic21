/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.News
 *  com.pic21.domain.NewsReaction
 *  com.pic21.domain.NewsReaction$ReactionType
 *  com.pic21.domain.Usuario
 *  com.pic21.dto.request.NewsRequest
 *  com.pic21.dto.response.NewsResponse
 *  com.pic21.exception.ResourceNotFoundException
 *  com.pic21.repository.NewsReactionRepository
 *  com.pic21.repository.NewsRepository
 *  com.pic21.repository.UsuarioRepository
 *  com.pic21.service.NewsService
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package com.pic21.service;

import com.pic21.domain.News;
import com.pic21.domain.NewsReaction;
import com.pic21.domain.Usuario;
import com.pic21.dto.request.NewsRequest;
import com.pic21.dto.response.NewsResponse;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.NewsReactionRepository;
import com.pic21.repository.NewsRepository;
import com.pic21.repository.UsuarioRepository;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NewsService {
    private static final Logger log = LoggerFactory.getLogger(NewsService.class);
    private final NewsRepository newsRepository;
    private final NewsReactionRepository newsReactionRepository;
    private final UsuarioRepository usuarioRepository;
    private static final Pattern META_OG_PATTERN = Pattern.compile("<meta[^>]*property=[\"']og:([^\"']+)[\"'][^>]*content=[\"']([^\"']*)[\"'][^>]*/?>", 2);
    private static final Pattern META_OG_REVERSE = Pattern.compile("<meta[^>]*content=[\"']([^\"']*)[\"'][^>]*property=[\"']og:([^\"']+)[\"'][^>]*/?>", 2);
    private static final Pattern META_NAME_PATTERN = Pattern.compile("<meta[^>]*name=[\"']([^\"']+)[\"'][^>]*content=[\"']([^\"']*)[\"'][^>]*/?>", 2);
    private static final Pattern TITLE_PATTERN = Pattern.compile("<title[^>]*>([^<]+)</title>", 2);

    @Transactional(readOnly=true)
    public List<NewsResponse> findAll(String username) {
        Long userId = this.findUsuarioOrThrow(username).getId();
        return this.newsRepository.findAllByOrderByCreatedAtDesc().stream().map(n -> this.mapToResponse(n, userId)).collect(Collectors.toList());
    }

    @Transactional(readOnly=true)
    public NewsResponse findById(Long id, String username) {
        Long userId = this.findUsuarioOrThrow(username).getId();
        return this.mapToResponse(this.findOrThrow(id), userId);
    }

    @Transactional
    public NewsResponse create(NewsRequest request, String username) {
        Usuario creator = this.findUsuarioOrThrow(username);
        News news = News.builder().title(request.getTitle()).description(request.getDescription()).imageUrl(request.getImageUrl()).sourceUrl(request.getSourceUrl()).publishedAt(request.getPublishedAt() != null ? request.getPublishedAt() : LocalDateTime.now()).createdBy(creator).active(true).build();
        News saved = (News)this.newsRepository.save(news);
        log.info("Noticia creada: id={}, t\u00edtulo='{}', por='{}'", new Object[]{saved.getId(), saved.getTitle(), username});
        return this.mapToResponse(saved, creator.getId());
    }

    @Transactional
    public NewsResponse update(Long id, NewsRequest request, String username) {
        News news = this.findOrThrow(id);
        Long userId = this.findUsuarioOrThrow(username).getId();
        news.setTitle(request.getTitle());
        news.setDescription(request.getDescription());
        news.setImageUrl(request.getImageUrl());
        news.setSourceUrl(request.getSourceUrl());
        if (request.getPublishedAt() != null) {
            news.setPublishedAt(request.getPublishedAt());
        }
        log.info("Noticia actualizada: id={}", id);
        return this.mapToResponse((News)this.newsRepository.save(news), userId);
    }

    @Transactional
    public void delete(Long id) {
        News news = this.findOrThrow(id);
        this.newsRepository.delete(news);
        log.info("Noticia eliminada: id={}, t\u00edtulo='{}'", id, news.getTitle());
    }

    @Transactional
    public NewsResponse toggleReaction(Long newsId, String username, NewsReaction.ReactionType type) {
        News news = this.findOrThrow(newsId);
        Usuario user = this.findUsuarioOrThrow(username);
        Long userId = user.getId();
        Optional<NewsReaction> existing = this.newsReactionRepository.findByNewsIdAndUsuarioId(newsId, userId);
        if (existing.isPresent()) {
            NewsReaction reaction = (NewsReaction)existing.get();
            if (reaction.getReactionType() == type) {
                this.newsReactionRepository.delete(reaction);
                log.info("Reacci\u00f3n eliminada: news={}, user='{}', type={}", new Object[]{newsId, username, type});
            } else {
                reaction.setReactionType(type);
                this.newsReactionRepository.save(reaction);
                log.info("Reacci\u00f3n cambiada: news={}, user='{}', type={}", new Object[]{newsId, username, type});
            }
        } else {
            NewsReaction reaction = NewsReaction.builder().news(news).usuario(user).reactionType(type).build();
            this.newsReactionRepository.save(reaction);
            log.info("Reacci\u00f3n agregada: news={}, user='{}', type={}", new Object[]{newsId, username, type});
        }
        return this.mapToResponse(news, userId);
    }

    @Transactional
    public NewsResponse removeReaction(Long newsId, String username) {
        Usuario user = this.findUsuarioOrThrow(username);
        News news = this.findOrThrow(newsId);
        this.newsReactionRepository.deleteByNewsIdAndUsuarioId(newsId, user.getId());
        log.info("Reacci\u00f3n eliminada: news={}, user='{}'", newsId, username);
        return this.mapToResponse(news, user.getId());
    }

    public Map<String, String> fetchOpenGraphPreview(String url) {
        HashMap<String, String> result = new HashMap<String, String>();
        result.put("url", url);
        try {
            String ogImage;
            String ogDesc;
            String content;
            String ogTitle;
            String redirect;
            HttpURLConnection conn = (HttpURLConnection)URI.create(url).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; PIC21Bot/1.0; +https://pic21.fly.dev)");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml");
            conn.setInstanceFollowRedirects(true);
            int status = conn.getResponseCode();
            if (status >= 300 && status < 400 && (redirect = conn.getHeaderField("Location")) != null) {
                conn = (HttpURLConnection)URI.create(redirect).toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; PIC21Bot/1.0; +https://pic21.fly.dev)");
                conn.setInstanceFollowRedirects(true);
            }
            StringBuilder html = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));){
                String line;
                for (int linesRead = 0; (line = reader.readLine()) != null && linesRead < 200; ++linesRead) {
                    html.append(line).append("\n");
                    if (!line.contains("</head>")) continue;
                    break;
                }
            }
            if ((ogTitle = this.extractMetaContent(content = html.toString(), "og:title")) == null) {
                ogTitle = this.extractHtmlTitle(content);
            }
            if (ogTitle != null) {
                result.put("title", ogTitle);
            }
            if ((ogDesc = this.extractMetaContent(content, "og:description")) == null) {
                ogDesc = this.extractMetaContent(content, "description");
            }
            if (ogDesc != null) {
                result.put("description", ogDesc);
            }
            if ((ogImage = this.extractMetaContent(content, "og:image")) != null) {
                result.put("image", ogImage);
            }
            log.info("OG preview para '{}': title={}, hasImage={}", new Object[]{url, result.containsKey("title"), result.containsKey("image")});
        }
        catch (Exception e) {
            log.warn("Error al obtener OG preview de '{}': {}", url, e.getMessage());
            result.put("error", "No se pudo obtener la vista previa: " + e.getMessage());
        }
        return result;
    }

    private String extractMetaContent(String html, String property) {
        if (property.startsWith("og:")) {
            String ogProp = property.substring(3);
            Matcher m = META_OG_PATTERN.matcher(html);
            while (m.find()) {
                if (!m.group(1).equalsIgnoreCase(ogProp)) continue;
                return this.decodeHtml(m.group(2));
            }
            Matcher m2 = META_OG_REVERSE.matcher(html);
            while (m2.find()) {
                if (!m2.group(2).equalsIgnoreCase(ogProp)) continue;
                return this.decodeHtml(m2.group(1));
            }
        } else {
            Matcher m = META_NAME_PATTERN.matcher(html);
            while (m.find()) {
                if (!m.group(1).equalsIgnoreCase(property)) continue;
                return this.decodeHtml(m.group(2));
            }
        }
        return null;
    }

    private String extractHtmlTitle(String html) {
        Matcher m = TITLE_PATTERN.matcher(html);
        return m.find() ? this.decodeHtml(m.group(1).trim()) : null;
    }

    private String decodeHtml(String s) {
        if (s == null) {
            return null;
        }
        return s.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'").replace("&#x27;", "'").replace("&apos;", "'").trim();
    }

    private News findOrThrow(Long id) {
        return (News)this.newsRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Noticia", id));
    }

    private Usuario findUsuarioOrThrow(String username) {
        return (Usuario)this.usuarioRepository.findByUsernameIgnoreCase(username).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private NewsResponse mapToResponse(News n, Long currentUserId) {
        Optional reaction;
        long likes = this.newsReactionRepository.countByNewsIdAndReactionType(n.getId(), NewsReaction.ReactionType.LIKE);
        long dislikes = this.newsReactionRepository.countByNewsIdAndReactionType(n.getId(), NewsReaction.ReactionType.DISLIKE);
        String userReaction = null;
        if (currentUserId != null && (reaction = this.newsReactionRepository.findByNewsIdAndUsuarioId(n.getId(), currentUserId)).isPresent()) {
            userReaction = ((NewsReaction)reaction.get()).getReactionType().name();
        }
        return NewsResponse.builder().id(n.getId()).title(n.getTitle()).description(n.getDescription()).imageUrl(n.getImageUrl()).sourceUrl(n.getSourceUrl()).publishedAt(n.getPublishedAt()).createdAt(n.getCreatedAt()).createdByUsername(n.getCreatedBy().getUsername()).active(n.isActive()).likes(likes).dislikes(dislikes).userReaction(userReaction).build();
    }

    public NewsService(NewsRepository newsRepository, NewsReactionRepository newsReactionRepository, UsuarioRepository usuarioRepository) {
        this.newsRepository = newsRepository;
        this.newsReactionRepository = newsReactionRepository;
        this.usuarioRepository = usuarioRepository;
    }
}

