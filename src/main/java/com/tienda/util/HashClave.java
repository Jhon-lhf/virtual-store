package com.tienda.util;

import com.tienda.servicio.ExcepcionNegocio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hash SHA-256 de contraseñas. Suficiente para el alcance académico del
 * proyecto; la migración a bcrypt (con salts por usuario) está en el roadmap.
 *
 * <p>La clave del usuario nunca se guarda en texto plano: se guarda el hash
 * y en el login se compara hash contra hash.</p>
 */
public final class HashClave {

    private HashClave() {
    }

    /** Devuelve el hash SHA-256 de {@code texto} como cadena hexadecimal. */
    public static String calcular(String texto) {
        try {
            MessageDigest digesto = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digesto.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 está garantizado en toda JVM estándar; nunca debería ocurrir.
            throw new ExcepcionNegocio("No se pudo calcular el hash de la clave", e);
        }
    }
}
