package com.rojas.disco.core.common.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

/**
 * Argumento tipado de una ruta.
 *
 * El nombre se declara una sola vez y se usa tanto para armar el patron de
 * navegacion como para leer el valor desde el back stack.
 */
data class ArgumentoRuta(
    val nombre: String,
    val tipo: NavType<*>
) {
    val definicion: NamedNavArgument
        get() = navArgument(nombre) { type = tipo }
}

/**
 * Todas las rutas de la aplicacion centralizadas en una sealed class.
 *
 * Cada destino conoce su patron, sus argumentos y como construir la ruta
 * concreta. Asi no quedan strings de rutas repartidos por las pantallas.
 */
sealed class Destino(
    val base: String,
    val argumentos: List<ArgumentoRuta> = emptyList()
) {
    /** Patron que espera NavHost, por ejemplo "session/{claseId}". */
    val patron: String = buildString {
        append(base)
        argumentos.forEach { argumento ->
            append("/{").append(argumento.nombre).append("}")
        }
    }

    /** Definiciones para registrar el destino en el NavHost. */
    val definicionesArgumentos: List<NamedNavArgument>
        get() = argumentos.map { it.definicion }

    /** Construye la ruta concreta respetando el orden de [argumentos]. */
    fun construirRuta(vararg valores: Any): String {
        require(valores.size == argumentos.size) {
            "El destino '$base' espera ${argumentos.size} argumento(s) y recibio ${valores.size}"
        }
        return argumentos
            .mapIndexed { indice, argumento -> argumento.nombre to valores[indice] }
            .fold(patron) { ruta, (nombre, valor) ->
                ruta.replace("{$nombre}", valor.toString())
            }
    }

    /** Indica si [rutaActual] corresponde a este destino o a una ruta hija. */
    fun coincideCon(rutaActual: String?): Boolean =
        rutaActual != null && (rutaActual == base || rutaActual.startsWith("$base/"))

    /** Lee un argumento Long por posicion (0 = primer argumento declarado). */
    fun NavBackStackEntry.long(indice: Int = 0): Long {
        val argumento = argumentos.getOrNull(indice)
            ?: error("El destino '$base' no tiene un argumento en la posicion $indice")
        return arguments?.getLong(argumento.nombre) ?: 0L
    }

    // ---------------------------------------------------------------------
    // Destinos de la aplicacion
    // ---------------------------------------------------------------------

    data object Login : Destino("login")

    data object InicioProfesor : Destino(
        base = "teacher_home",
        argumentos = listOf(ArgumentoRuta("profesorId", NavType.LongType))
    ) {
        fun crearRuta(profesorId: Long) = construirRuta(profesorId)

        fun profesorId(entry: NavBackStackEntry) = entry.long()
    }

    data object InicioEstudiante : Destino(
        base = "student_home",
        argumentos = listOf(ArgumentoRuta("estudianteId", NavType.LongType))
    ) {
        fun crearRuta(estudianteId: Long) = construirRuta(estudianteId)

        fun estudianteId(entry: NavBackStackEntry) = entry.long()
    }

    data object CuestionarioEstudiante : Destino(
        base = "student_questionnaire",
        argumentos = listOf(
            ArgumentoRuta("claseId", NavType.LongType),
            ArgumentoRuta("estudianteId", NavType.LongType)
        )
    ) {
        fun crearRuta(claseId: Long, estudianteId: Long) = construirRuta(claseId, estudianteId)

        fun claseId(entry: NavBackStackEntry) = entry.long(0)

        fun estudianteId(entry: NavBackStackEntry) = entry.long(1)
    }

    data object Sesion : Destino(
        base = "session",
        argumentos = listOf(ArgumentoRuta("claseId", NavType.LongType))
    ) {
        fun crearRuta(claseId: Long) = construirRuta(claseId)

        fun claseId(entry: NavBackStackEntry) = entry.long()
    }

    data object DetalleSesion : Destino(
        base = "session_detail",
        argumentos = listOf(ArgumentoRuta("claseId", NavType.LongType))
    ) {
        fun crearRuta(claseId: Long) = construirRuta(claseId)

        fun claseId(entry: NavBackStackEntry) = entry.long()
    }

    data object EditarSesion : Destino(
        base = "session_edit",
        argumentos = listOf(ArgumentoRuta("claseId", NavType.LongType))
    ) {
        fun crearRuta(claseId: Long) = construirRuta(claseId)

        fun claseId(entry: NavBackStackEntry) = entry.long()
    }

    data object Perfil : Destino(
        base = "profile",
        argumentos = listOf(ArgumentoRuta("usuarioId", NavType.LongType))
    ) {
        fun crearRuta(usuarioId: Long) = construirRuta(usuarioId)

        fun usuarioId(entry: NavBackStackEntry) = entry.long()
    }
}

/** Navega a [destino] construyendo su ruta con los valores indicados. */
fun NavController.navegarA(destino: Destino, vararg valores: Any) {
    navigate(destino.construirRuta(*valores))
}

/** Navegacion entre pestanas: conserva y restaura el estado de cada una. */
fun NavController.navegarAPestana(destino: Destino, vararg valores: Any) {
    navigate(destino.construirRuta(*valores)) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Registra un [Destino] evitando repetir patron y argumentos en el NavHost. */
fun NavGraphBuilder.composableDestino(
    destino: Destino,
    contenido: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = destino.patron,
        arguments = destino.definicionesArgumentos,
        content = contenido
    )
}
