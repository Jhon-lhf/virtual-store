package com.tienda.persistencia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tienda.servicio.ExcepcionNegocio;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Almacenamiento genérico de listas de objetos en archivos JSON (UTF-8,
 * pretty-print). Es el único punto del sistema que toca Gson y el disco.
 *
 * <p>Todas las rutas son relativas al directorio del proyecto
 * (ej. {@code data/usuarios.json}); nunca absolutas.</p>
 *
 * @param <T> tipo de entidad que persiste el archivo.
 */
public class AlmacenJson<T> {

    private final Path archivo;
    private final Type tipoLista;
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    /**
     * @param archivo ruta relativa del archivo JSON (ej. {@code data/usuarios.json}).
     * @param token   token de tipo {@code TypeToken<List<T>>} para deserializar la lista.
     */
    public AlmacenJson(Path archivo, TypeToken<List<T>> token) {
        this.archivo = archivo;
        this.tipoLista = token.getType();
    }

    /** Lee todas las entidades; devuelve lista vacía si el archivo no existe. */
    public synchronized List<T> leerTodos() {
        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }
        try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            List<T> datos = gson.fromJson(lector, tipoLista);
            return datos != null ? datos : new ArrayList<>();
        } catch (IOException e) {
            throw new ExcepcionNegocio("No se pudo leer el archivo " + archivo, e);
        }
    }

    /** Reemplaza el contenido completo del archivo con la lista dada. */
    public synchronized void guardarTodos(List<T> datos) {
        try {
            if (archivo.getParent() != null) {
                Files.createDirectories(archivo.getParent());
            }
            try (Writer escritor = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
                gson.toJson(datos, tipoLista, escritor);
            }
        } catch (IOException e) {
            throw new ExcepcionNegocio("No se pudo guardar el archivo " + archivo, e);
        }
    }
}
