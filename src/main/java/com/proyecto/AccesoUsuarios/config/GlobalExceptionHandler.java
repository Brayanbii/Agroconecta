package com.proyecto.AccesoUsuarios.config;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 404 - Recurso no encontrado (ej: productoRepo.findById().orElseThrow())
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoSuchElementException ex, Model model) {
        model.addAttribute("codigo", "404");
        model.addAttribute("titulo", "Recurso no encontrado");
        model.addAttribute("mensaje", "El elemento que buscas no existe o fue eliminado.");
        model.addAttribute("icono", "fas fa-search");
        return "error";
    }

    // 404 - Página no encontrada
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handlePageNotFound(NoHandlerFoundException ex, Model model) {
        model.addAttribute("codigo", "404");
        model.addAttribute("titulo", "Página no encontrada");
        model.addAttribute("mensaje", "La página que buscas no existe. Verifica la URL e intenta de nuevo.");
        model.addAttribute("icono", "fas fa-map-signs");
        return "error";
    }

    // 403 - Acceso denegado
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        model.addAttribute("codigo", "403");
        model.addAttribute("titulo", "Acceso denegado");
        model.addAttribute("mensaje", "No tienes permisos para acceder a este recurso.");
        model.addAttribute("icono", "fas fa-lock");
        return "error";
    }

    // 500 - Error genérico (cualquier excepción no capturada)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericError(Exception ex, Model model) {
        model.addAttribute("codigo", "500");
        model.addAttribute("titulo", "Error interno del servidor");
        model.addAttribute("mensaje", "Ocurrió un error inesperado. Por favor, intenta de nuevo más tarde.");
        model.addAttribute("icono", "fas fa-exclamation-triangle");
        // Log para depuración
        ex.printStackTrace();
        return "error";
    }
}
