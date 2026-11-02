package com.tienda.servicio;

import com.tienda.modelo.Rol;
import com.tienda.modelo.Usuario;
import com.tienda.persistencia.RepositorioUsuarios;
import com.tienda.util.HashClave;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de autenticación y registro sobre archivos JSON temporales
 * ({@code data/test-usuarios.json}), borrados después de cada prueba.
 */
class ServicioAuthTest {

    private static final Path ARCHIVO = Paths.get("data", "test-usuarios.json");

    private RepositorioUsuarios repositorio;
    private ServicioAuth servicio;

    @BeforeEach
    void preparar() throws Exception {
        Files.deleteIfExists(ARCHIVO);
        repositorio = new RepositorioUsuarios(ARCHIVO);
        servicio = new ServicioAuth(repositorio);
    }

    @AfterEach
    void limpiar() throws Exception {
        Files.deleteIfExists(ARCHIVO);
    }

    private void registrarAna() {
        servicio.registrar(new ServicioAuth.DatosRegistro(
                "ana", "secreta", "Ana Pérez", "ana@correo.com", Rol.CLIENTE));
    }

    @Test
    void iniciaSesionConCredencialesCorrectas() {
        registrarAna();
        Usuario usuario = servicio.iniciarSesion("ana", "secreta");
        assertEquals("Ana Pérez", usuario.getNombre());
        assertEquals(Rol.CLIENTE, usuario.getRol());
    }

    @Test
    void ignoraMayusculasEnElNombreDeUsuario() {
        registrarAna();
        Usuario usuario = servicio.iniciarSesion("ANA", "secreta");
        assertEquals("Ana Pérez", usuario.getNombre());
    }

    @Test
    void rechazaUsuarioInexistente() {
        ExcepcionNegocio e = assertThrows(ExcepcionNegocio.class,
                () -> servicio.iniciarSesion("fantasma", "clavex"));
        assertTrue(e.getMessage().contains("no existe"));
    }

    @Test
    void rechazaClaveIncorrecta() {
        registrarAna();
        ExcepcionNegocio e = assertThrows(ExcepcionNegocio.class,
                () -> servicio.iniciarSesion("ana", "equivocada"));
        assertTrue(e.getMessage().contains("Credenciales inválidas"));
    }

    @Test
    void rechazaUsuarioBloqueado() {
        registrarAna();
        Usuario ana = repositorio.buscarPorNombreUsuario("ana").orElseThrow();
        ana.setActivo(false);
        servicio.guardarCambios(ana);

        ExcepcionNegocio e = assertThrows(ExcepcionNegocio.class,
                () -> servicio.iniciarSesion("ana", "secreta"));
        assertTrue(e.getMessage().contains("bloqueado"));
    }

    @Test
    void noGuardaLaClaveEnTextoPlano() {
        registrarAna();
        Usuario ana = repositorio.buscarPorNombreUsuario("ana").orElseThrow();
        assertNotEquals("secreta", ana.getHashClave());
        assertEquals(HashClave.calcular("secreta"), ana.getHashClave());
    }

    @Test
    void rechazaClaveCortaEnRegistro() {
        ExcepcionNegocio e = assertThrows(ExcepcionNegocio.class,
                () -> servicio.registrar(new ServicioAuth.DatosRegistro(
                        "ana", "abc", "Ana Pérez", "ana@correo.com", Rol.CLIENTE)));
        assertTrue(e.getMessage().contains("al menos"));
    }

    @Test
    void rechazaUsuarioDuplicado() {
        registrarAna();
        ExcepcionNegocio e = assertThrows(ExcepcionNegocio.class,
                () -> servicio.registrar(new ServicioAuth.DatosRegistro(
                        "ana", "otraclave", "Otra Ana", "otra@correo.com", Rol.VENDEDOR)));
        assertTrue(e.getMessage().contains("ya está en uso"));
    }

    @Test
    void rechazaCamposIncompletos() {
        ExcepcionNegocio e = assertThrows(ExcepcionNegocio.class,
                () -> servicio.registrar(new ServicioAuth.DatosRegistro(
                        "", "secreta", "Ana Pérez", "ana@correo.com", Rol.CLIENTE)));
        assertTrue(e.getMessage().contains("Completa"));
    }

    @Test
    void rechazaRolAdministradorEnRegistro() {
        ExcepcionNegocio e = assertThrows(ExcepcionNegocio.class,
                () -> servicio.registrar(new ServicioAuth.DatosRegistro(
                        "mallory", "secreta", "Mallory", "m@correo.com", Rol.ADMINISTRADOR)));
        assertTrue(e.getMessage().contains("cliente o vendedor"));
    }
}
