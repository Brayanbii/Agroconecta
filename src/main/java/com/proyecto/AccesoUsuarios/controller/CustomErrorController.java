package com.proyecto.AccesoUsuarios.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        pw.println("Status: " + status);
        if (throwable != null) {
            throwable.printStackTrace(pw);
        } else {
            Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
            pw.println("Message: " + message);
        }
        
        try {
            Files.writeString(Paths.get("THYMELEAF_ERROR.txt"), sw.toString());
        } catch (Exception e) {}
        
        return "error"; // will fallback to the existing error page
    }
}
