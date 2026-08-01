/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.controller.SpaController
 *  org.springframework.core.io.ClassPathResource
 *  org.springframework.core.io.Resource
 *  org.springframework.http.CacheControl
 *  org.springframework.http.MediaType
 *  org.springframework.http.ResponseEntity
 *  org.springframework.http.ResponseEntity$BodyBuilder
 *  org.springframework.stereotype.Controller
 *  org.springframework.web.bind.annotation.GetMapping
 */
package com.pic21.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    private static final Resource INDEX_HTML = new ClassPathResource("static/index.html");

    @GetMapping(value={"/"})
    public ResponseEntity<Resource> serveIndex() {
        ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.ok();
        CacheControl.noCache();
        return ((ResponseEntity.BodyBuilder)((ResponseEntity.BodyBuilder)((ResponseEntity.BodyBuilder)bodyBuilder.cacheControl(CacheControl.noStore().mustRevalidate())).header("Pragma", new String[]{"no-cache"})).header("Expires", new String[]{"0"})).contentType(MediaType.TEXT_HTML).body((Object)INDEX_HTML);
    }
}

