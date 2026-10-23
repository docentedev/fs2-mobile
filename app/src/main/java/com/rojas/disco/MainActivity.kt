package com.rojas.disco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rojas.disco.ui.navigation.Destino
import com.rojas.disco.ui.navigation.composableDestino
import com.rojas.disco.ui.navigation.navegarA
import com.rojas.disco.ui.navigation.navegarAPestana
import com.rojas.disco.ui.screens.LoginScreen
import com.rojas.disco.ui.screens.ProfileScreen
import com.rojas.disco.ui.screens.SessionDetailScreen
import com.rojas.disco.ui.screens.SessionEditScreen
import com.rojas.disco.ui.screens.SessionScreen
import com.rojas.disco.ui.screens.StudentHomeScreen
import com.rojas.disco.ui.screens.StudentQuestionnaireScreen
import com.rojas.disco.ui.screens.TeacherHomeScreen
import com.rojas.disco.ui.theme.DiscoTheme
import com.rojas.disco.ui.viewmodels.SesionRestaurada
import com.rojas.disco.ui.viewmodels.SesionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DiscoTheme {
                AppNavegacion()
            }
        }
    }
}

private data class ItemBarraNavegacion(
    val etiqueta: String,
    val icono: ImageVector,
    val seleccionado: Boolean,
    val onClick: () -> Unit
)

@Composable
fun AppNavegacion() {
    val sesionViewModel: SesionViewModel = hiltViewModel()
    val sesion by sesionViewModel.sesion.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = androidx.compose.ui.platform.LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(sesion) {
        if (sesion is SesionRestaurada.ConSesion && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val destinoInicial = when (val estado = sesion) {
        is SesionRestaurada.ConSesion -> {
            if (estado.esEstudiante) {
                Destino.InicioEstudiante.crearRuta(estado.usuarioId)
            } else {
                Destino.InicioProfesor.crearRuta(estado.usuarioId)
            }
        }
        else -> Destino.Login.patron
    }

    when (sesion) {
        is SesionRestaurada.Verificando -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        else -> {
            AppNavegacionInterna(
                navController = navController,
                currentRoute = currentRoute,
                destinoInicial = destinoInicial
            )
        }
    }
}

@Composable
private fun AppNavegacionInterna(
    navController: androidx.navigation.NavHostController,
    currentRoute: String?,
    destinoInicial: String
) {
    var currentUsuarioId by remember { mutableStateOf<Long?>(null) }
    var isEstudianteUser by remember { mutableStateOf(false) }

    val enInicio = Destino.InicioProfesor.coincideCon(currentRoute) ||
            Destino.InicioEstudiante.coincideCon(currentRoute)
    val enPerfil = Destino.Perfil.coincideCon(currentRoute)
    val showBottomBar = enInicio || enPerfil

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val usuarioId = currentUsuarioId
            if (showBottomBar && usuarioId != null) {
                NavigationBar {
                    listOf(
                        ItemBarraNavegacion(
                            etiqueta = "Clases",
                            icono = Icons.Default.Home,
                            seleccionado = enInicio,
                            onClick = {
                                val destino = if (isEstudianteUser) {
                                    Destino.InicioEstudiante
                                } else {
                                    Destino.InicioProfesor
                                }
                                navController.navegarAPestana(destino, usuarioId)
                            }
                        ),
                        ItemBarraNavegacion(
                            etiqueta = "Perfil",
                            icono = Icons.Default.Person,
                            seleccionado = enPerfil,
                            onClick = {
                                navController.navegarAPestana(Destino.Perfil, usuarioId)
                            }
                        )
                    ).forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(imageVector = item.icono, contentDescription = item.etiqueta) },
                            label = { Text(item.etiqueta) },
                            selected = item.seleccionado,
                            onClick = item.onClick
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = destinoInicial,
            modifier = Modifier.padding(innerPadding)
        ) {
            composableDestino(Destino.Login) {
                LoginScreen(
                    onLoginProfesor = { profesorId ->
                        currentUsuarioId = profesorId
                        isEstudianteUser = false
                        navController.navigate(Destino.InicioProfesor.crearRuta(profesorId)) {
                            popUpTo(Destino.Login.patron) { inclusive = true }
                        }
                    },
                    onLoginEstudiante = { estudianteId ->
                        currentUsuarioId = estudianteId
                        isEstudianteUser = true
                        navController.navigate(Destino.InicioEstudiante.crearRuta(estudianteId)) {
                            popUpTo(Destino.Login.patron) { inclusive = true }
                        }
                    }
                )
            }

            composableDestino(Destino.InicioProfesor) { backStackEntry ->
                val profesorId = Destino.InicioProfesor.profesorId(backStackEntry)
                currentUsuarioId = profesorId
                isEstudianteUser = false

                TeacherHomeScreen(
                    onNavigateToSession = { claseId ->
                        navController.navegarA(Destino.Sesion, claseId)
                    }
                )
            }

            composableDestino(Destino.InicioEstudiante) { backStackEntry ->
                val estudianteId = Destino.InicioEstudiante.estudianteId(backStackEntry)
                currentUsuarioId = estudianteId
                isEstudianteUser = true

                StudentHomeScreen(
                    onNavigateToQuestionnaire = { claseId ->
                        navController.navegarA(
                            Destino.CuestionarioEstudiante,
                            claseId,
                            estudianteId
                        )
                    }
                )
            }

            composableDestino(Destino.CuestionarioEstudiante) { backStackEntry ->
                val estudianteId = Destino.CuestionarioEstudiante.estudianteId(backStackEntry)
                currentUsuarioId = estudianteId

                StudentQuestionnaireScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composableDestino(Destino.Sesion) { backStackEntry ->
                val claseId = Destino.Sesion.claseId(backStackEntry)

                SessionScreen(
                    onNavigateToDetail = {
                        navController.navegarA(Destino.DetalleSesion, claseId)
                    },
                    onNavigateToEdit = {
                        navController.navegarA(Destino.EditarSesion, claseId)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composableDestino(Destino.DetalleSesion) {
                SessionDetailScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composableDestino(Destino.EditarSesion) {
                SessionEditScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composableDestino(Destino.Perfil) { backStackEntry ->
                val usuarioId = Destino.Perfil.usuarioId(backStackEntry)
                currentUsuarioId = usuarioId

                ProfileScreen(
                    onLogout = {
                        currentUsuarioId = null
                        navController.navigate(Destino.Login.patron) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
