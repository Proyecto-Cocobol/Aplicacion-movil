package com.example.cocobol;

import android.content.Context;
import android.content.SharedPreferences;

/** Guarda en el teléfono quién inició sesión (id, nombre y rol). */
public class Sesion {

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("sesion", Context.MODE_PRIVATE);
    }

    public static void iniciar(Context c, long id, String nombre, String rol) {
        prefs(c).edit().putLong("id", id).putString("nombre", nombre).putString("rol", rol).apply();
    }

    public static long id(Context c) {
        return prefs(c).getLong("id", -1);
    }

    public static String nombre(Context c) {
        return prefs(c).getString("nombre", "");
    }

    public static String rol(Context c) {
        return prefs(c).getString("rol", "");
    }

    public static void cerrar(Context c) {
        prefs(c).edit().clear().apply();
    }
}
