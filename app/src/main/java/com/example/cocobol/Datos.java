package com.example.cocobol;

public class Datos {
    // Servidor de la web en Azure. Todavia NO se usa: la app aun no se conecta a la base de datos.
    public static final String URL_SERVIDOR = "https://cocobol-ena8g7gnbycqdxbw.northcentralus-01.azurewebsites.net/";

    String Contrasena;
    String Correo;

    public String getContrasena() {
        return Contrasena;
    }

    public void setContrasena(String contrasena) {
        Contrasena = contrasena;
    }

    public String getCorreo() {
        return Correo;
    }

    public void setCorreo(String correo) {
        Correo = correo;
    }
}