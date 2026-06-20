package com.agroconectago.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroconectago.app.data.model.DeliveryProfileUpdateRequest
import com.agroconectago.app.data.model.DeliveryRegisterRequest
import com.agroconectago.app.data.model.DeliveryUsuarioInfo
import com.agroconectago.app.data.repository.DeliveryAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DeliveryAuthViewModel : ViewModel() {

    private val authRepo = DeliveryAuthRepository()

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando

    private val _errorLogin = MutableStateFlow<String?>(null)
    val errorLogin: StateFlow<String?> = _errorLogin

    private val _usuarioLogueado = MutableStateFlow<DeliveryUsuarioInfo?>(null)
    val usuarioLogueado: StateFlow<DeliveryUsuarioInfo?> = _usuarioLogueado

    private val _registroExitoso = MutableStateFlow(false)
    val registroExitoso: StateFlow<Boolean> = _registroExitoso

    private val _mensajeRegistro = MutableStateFlow<String?>(null)
    val mensajeRegistro: StateFlow<String?> = _mensajeRegistro

    private val _perfilActualizado = MutableStateFlow(false)
    val perfilActualizado: StateFlow<Boolean> = _perfilActualizado

    private val _mensajePerfil = MutableStateFlow<String?>(null)
    val mensajePerfil: StateFlow<String?> = _mensajePerfil

    private val _emailExiste = MutableStateFlow(false)
    val emailExiste: StateFlow<Boolean> = _emailExiste

    private val _usernameExiste = MutableStateFlow(false)
    val usernameExiste: StateFlow<Boolean> = _usernameExiste

    fun iniciarSesion(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorLogin.value = "Ingresa tu correo y contrasena"
            return
        }
        _estaCargando.value = true
        _errorLogin.value = null
        viewModelScope.launch {
            try {
                val response = authRepo.login(email, password)
                if (response.success && response.user != null) {
                    if (response.user.rol != "REPARTIDOR") {
                        _errorLogin.value = "Esta cuenta no es de repartidor. Usa la app AgroConecta."
                        _estaCargando.value = false
                        return@launch
                    }
                    _usuarioLogueado.value = response.user
                } else {
                    _errorLogin.value = response.message ?: "Credenciales invalidas"
                }
            } catch (e: Exception) {
                _errorLogin.value = "Error de conexion. Verifica que el servidor este activo."
            } finally {
                _estaCargando.value = false
            }
        }
    }

    fun registrarRepartidor(request: DeliveryRegisterRequest) {
        _estaCargando.value = true
        _mensajeRegistro.value = null
        _registroExitoso.value = false
        viewModelScope.launch {
            try {
                val response = authRepo.register(request)
                if (response.success) {
                    _registroExitoso.value = true
                    _mensajeRegistro.value = "Registro exitoso. Completa tu perfil para empezar."
                    response.user?.let { user -> _usuarioLogueado.value = user }
                } else {
                    _mensajeRegistro.value = response.message ?: "Error al registrar. Intenta de nuevo."
                }
            } catch (e: Exception) {
                _mensajeRegistro.value = "Error de conexion. Verifica que el servidor este activo."
            } finally {
                _estaCargando.value = false
            }
        }
    }

    fun actualizarPerfil(request: DeliveryProfileUpdateRequest) {
        _estaCargando.value = true
        _mensajePerfil.value = null
        _perfilActualizado.value = false
        viewModelScope.launch {
            try {
                val response = authRepo.updateProfile(request)
                if (response.success) {
                    _perfilActualizado.value = true
                    _mensajePerfil.value = "Perfil completado exitosamente"
                } else {
                    _mensajePerfil.value = response.message ?: "Error al guardar perfil"
                }
            } catch (e: Exception) {
                _mensajePerfil.value = "Error de conexion"
            } finally {
                _estaCargando.value = false
            }
        }
    }

    fun comprobarEmail(email: String) {
        viewModelScope.launch {
            try { _emailExiste.value = authRepo.checkEmail(email) } catch (_: Exception) { }
        }
    }

    fun comprobarUsername(username: String) {
        viewModelScope.launch {
            try { _usernameExiste.value = authRepo.checkUsername(username) } catch (_: Exception) { }
        }
    }

    fun setUsuarioInicial(usuario: DeliveryUsuarioInfo) {
        _usuarioLogueado.value = usuario
    }

    fun limpiarEstados() {
        _errorLogin.value = null
        _estaCargando.value = false
        _usuarioLogueado.value = null
        _registroExitoso.value = false
        _mensajeRegistro.value = null
        _perfilActualizado.value = false
        _mensajePerfil.value = null
    }

    fun limpiarMensajeRegistro() {
        _mensajeRegistro.value = null
    }

    fun limpiarMensajePerfil() {
        _mensajePerfil.value = null
        _perfilActualizado.value = false
    }
}
