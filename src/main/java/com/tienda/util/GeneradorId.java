package com.tienda.util;

import java.security.SecureRandom;

/**
 * Generador de IDs con formato {@code PREFIJO-xxxxxxxx} (8 caracteres
 * hexadecimales aleatorios). Prefijos en uso: {@code U-} usuarios,
 * {@code P-} productos, {@code C-} categorías, {@code S-} solicitudes.
 */
public final class GeneradorId {

    private static final String CARACTERES_HEX = "0123456789abcdef";
    private static final int LARGO = 8;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private GeneradorId() {
    }

    /** Genera un ID nuevo con el prefijo dado (ej. {@code generar("P")} → {@code P-3f2a1b4c}). */
    public static String generar(String prefijo) {
        StringBuilder hex = new StringBuilder(LARGO);
        for (int i = 0; i < LARGO; i++) {
            hex.append(CARACTERES_HEX.charAt(ALEATORIO.nextInt(CARACTERES_HEX.length())));
        }
        return prefijo + "-" + hex;
    }
}
