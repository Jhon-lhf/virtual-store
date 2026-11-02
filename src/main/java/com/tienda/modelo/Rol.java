package com.tienda.modelo;

/**
 * Roles que puede tener un usuario del sistema.
 *
 * <p>Cada rol determina qué pestañas ve el usuario en la aplicación y qué
 * acciones puede ejecutar sobre los servicios.</p>
 */
public enum Rol {
    /** Compra: busca en el catálogo, gestiona carrito, deseos y ve sus pedidos. */
    CLIENTE,
    /** Vende: publica productos, gestiona su stock y despacha pedidos. */
    VENDEDOR,
    /** Administra: gestiona usuarios, catálogo global, categorías y métricas. */
    ADMINISTRADOR
}
