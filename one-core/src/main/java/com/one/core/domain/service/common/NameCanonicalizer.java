package com.one.core.domain.service.common;


import java.text.Normalizer;

public final class NameCanonicalizer {
    private NameCanonicalizer() {}

    public static String canonical(String s) {
        if (s == null) return null;
        String noAccents = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", ""); // quita tildes
        return noAccents.toLowerCase().trim().replaceAll("\\s+", " ");
    }
}