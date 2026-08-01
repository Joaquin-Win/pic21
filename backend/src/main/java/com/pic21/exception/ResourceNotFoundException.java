/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.exception.ResourceNotFoundException
 */
package com.pic21.exception;

public class ResourceNotFoundException
extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " con id " + id + " no encontrado");
    }
}

