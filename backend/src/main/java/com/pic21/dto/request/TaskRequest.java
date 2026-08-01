/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.dto.request.TaskRequest
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.Size
 */
package com.pic21.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class TaskRequest {
    @NotBlank(message="El t\u00edtulo de la tarea es obligatorio")
    @Size(max=200, message="El t\u00edtulo no puede superar los 200 caracteres")
    private @NotBlank(message="El t\u00edtulo de la tarea es obligatorio") @Size(max=200, message="El t\u00edtulo no puede superar los 200 caracteres") String title;
    @Size(max=2000, message="La descripci\u00f3n no puede superar los 2000 caracteres")
    private @Size(max=2000, message="La descripci\u00f3n no puede superar los 2000 caracteres") String description;
    @Size(max=500, message="El link no puede superar los 500 caracteres")
    private @Size(max=500, message="El link no puede superar los 500 caracteres") String link;
    private List<String> links;
    private String questionsJson;

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getLink() {
        return this.link;
    }

    public List<String> getLinks() {
        return this.links;
    }

    public String getQuestionsJson() {
        return this.questionsJson;
    }
}

