package com.tienda.servicio;

import com.tienda.modelo.Rol;
import com.tienda.modelo.Usuario;
import com.tienda.persistencia.RepositorioUsuarios;
import com.tienda.util.GeneradorId;
import com.tienda.util.HashClave;

import java.util.Optional;

/**
 * Autenticación y registro de cuentas.
 *
 * <p>Errores diferenciados para el login ("no existe" / "bloqueado" /
 * "credenciales inválidas") y validaciones estrictas en el registro
 * (clave mínima, unicidad de nombre de usuario). El rol
 * {@code ADMINISTRADOR} no se puede autoproclamar en el registro: solo otro
 * administrador puede convertir a alguien desde la pantalla de usuarios.</p>
 */
public class ServicioAuth {

    /** Largo mínimo exigido a la clave en el registro. */
    public static final int LARGO_MINIMO_CLAVE = 4;

    private final RepositorioUsuarios repositorioUsuarios;

    public ServicioAuth(RepositorioUsuarios repositorioUsuarios) {
        this.repositorioUsuarios = repositorioUsuarios;
    }

    /**
     * Inicia sesión con usuario y clave.
     *
     * @throws ExcepcionNegocio con un mensaje claro para cada caso de error.
     */
    public Usuario iniciarSesion(String nombreUsuario, String clave) {
        if (esVacio(nombreUsuario) || esVacio(clave)) {
            throw new ExcepcionNegocio("Completa usuario y clave");
        }
        Usuario usuario = repositorioUsuarios.buscarPorNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new ExcepcionNegocio("El usuario no existe"));
        if (!usuario.isActivo()) {
            throw new ExcepcionNegocio("El usuario está bloqueado");
        }
        if (!usuario.getHashClave().equals(HashClave.calcular(clave))) {
            throw new ExcepcionNegocio("Credenciales inválidas");
        }
        return usuario;
    }

    /**
     * Registra una cuenta nueva (solo {@code CLIENTE} o {@code VENDEDOR}).
     *
     * @throws ExcepcionNegocio si falta algún campo, la clave es corta,
     *                          el correo no parece válido o el usuario ya existe.
     */
    public Usuario registrar(DatosRegistro datos) {
        if (esVacio(datos.usuario()) || esVacio(datos.clave())
                || esVacio(datos.nombre()) || esVacio(datos.correo())) {
            throw new ExcepcionNegocio("Completa todos los campos");
        }
        if (datos.clave().length() < LARGO_MINIMO_CLAVE) {
            throw new ExcepcionNegocio("La clave debe tener al menos " + LARGO_MINIMO_CLAVE + " caracteres");
        }
        if (!datos.correo().contains("@") || !datos.correo().contains(".")) {
            throw new ExcepcionNegocio("El correo no parece válido");
        }
        if (datos.rol() != Rol.CLIENTE && datos.rol() != Rol.VENDEDOR) {
            throw new ExcepcionNegocio("El rol debe ser cliente o vendedor");
        }
        if (repositorioUsuarios.buscarPorNombreUsuario(datos.usuario()).isPresent()) {
            throw new ExcepcionNegocio("Ese nombre de usuario ya está en uso");
        }

        Usuario nuevo = new Usuario(
                GeneradorId.generar("U"),
                datos.usuario().trim(),
                HashClave.calcular(datos.clave()),
                datos.nombre().trim(),
                datos.correo().trim(),
                datos.rol(),
                true);
        repositorioUsuarios.guardar(nuevo);
        return nuevo;
    }

    /** Guarda cambios de un usuario existente (ej. lista de deseos). */
    public void guardarCambios(Usuario usuario) {
        repositorioUsuarios.guardar(usuario);
    }

    public Optional<Usuario> buscarPorId(String id) {
        return repositorioUsuarios.buscarPorId(id);
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    /** Datos de entrada del registro (formulario de creación de cuenta). */
    public record DatosRegistro(String usuario, String clave, String nombre, String correo, Rol rol) {
    }
}
