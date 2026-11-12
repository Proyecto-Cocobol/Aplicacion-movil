package com.example.cocobol;

import android.util.Patterns;
import android.widget.EditText;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Funciones de apoyo: fechas, validaciones, formato de dinero y cifrado de contraseñas.
 * Todas las fechas de la app se guardan como texto con formato dd/MM/yyyy.
 */
public class Util {

    /** Mismo orden que el arreglo f_rol de arrays.xml. */
    public static final String[] ROLES = {"ADMINISTRADOR", "PROFESOR", "RESPONSABLE", "ALUMNO"};
    /** Mismo orden que el arreglo f_parentesco de arrays.xml. */
    public static final String[] PARENTESCOS = {"TUTOR", "PADRE", "MADRE"};

    public static final String[] DIAS = {"Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
    public static final String[] MESES = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
            "agosto", "septiembre", "octubre", "noviembre", "diciembre"};

    /** Expresión SQL para ordenar una columna de fecha dd/MM/yyyy (queda como yyyyMMdd). */
    public static String ordenFecha(String columna) {
        return "(substr(" + columna + ",7,4)||substr(" + columna + ",4,2)||substr(" + columna + ",1,2))";
    }

    // ------------------------------------------------------------------ Texto

    public static String texto(EditText campo) {
        return campo.getText().toString().trim();
    }

    public static boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String nulo(String s) {
        return s == null ? "" : s;
    }

    /** Muestra un guion cuando el dato no se capturó. */
    public static String guion(String s) {
        return vacio(s) ? "—" : s;
    }

    public static String nombreCompleto(String nombre, String paterno, String materno) {
        String n = nulo(nombre) + " " + nulo(paterno);
        if (!vacio(materno)) {
            n = n + " " + materno;
        }
        return n.trim();
    }

    public static String iniciales(String nombre, String apellido) {
        String i = "";
        if (!vacio(nombre)) i = i + nombre.trim().charAt(0);
        if (!vacio(apellido)) i = i + apellido.trim().charAt(0);
        return i.toUpperCase(Locale.ROOT);
    }

    public static String capitalizar(String s) {
        if (vacio(s)) return "";
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }

    // ------------------------------------------------------------------ Validaciones

    public static boolean correoValido(String correo) {
        return Patterns.EMAIL_ADDRESS.matcher(correo).matches();
    }

    public static boolean telefonoValido(String telefono) {
        return telefono.matches("\\d{10}");
    }

    public static boolean curpValida(String curp) {
        return curp.toUpperCase(Locale.ROOT).matches("^[A-Z]{4}\\d{6}[HMX][A-Z]{5}[A-Z0-9]\\d$");
    }

    public static boolean horaValida(String hora) {
        return hora.matches("^([01]\\d|2[0-3]):[0-5]\\d$");
    }

    public static boolean soloLetras(String s) {
        return s.matches("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ' .-]+$");
    }

    // ------------------------------------------------------------------ Fechas

    private static SimpleDateFormat formato() {
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        f.setLenient(false);
        return f;
    }

    public static boolean fechaValida(String fecha) {
        if (fecha == null || !fecha.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return false;
        }
        try {
            formato().parse(fecha);
            int anio = Integer.parseInt(fecha.substring(6));
            return anio >= 1900 && anio <= 2100;
        } catch (ParseException e) {
            return false;
        }
    }

    public static Calendar aCalendario(String fecha) {
        Calendar c = Calendar.getInstance();
        try {
            Date d = formato().parse(fecha);
            if (d != null) c.setTime(d);
        } catch (ParseException e) {
            // Si la fecha no es válida se regresa la fecha de hoy
        }
        return c;
    }

    public static String formatear(Calendar c) {
        return formato().format(c.getTime());
    }

    public static String hoy() {
        return formatear(Calendar.getInstance());
    }

    public static String sumarDias(int dias) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, dias);
        return formatear(c);
    }

    /** Compara dos fechas dd/MM/yyyy. Negativo si a es antes que b. */
    public static int comparar(String a, String b) {
        return clave(a).compareTo(clave(b));
    }

    /** Convierte dd/MM/yyyy en yyyyMMdd para poder comparar. */
    public static String clave(String fecha) {
        if (fecha == null || fecha.length() != 10) return "";
        return fecha.substring(6) + fecha.substring(3, 5) + fecha.substring(0, 2);
    }

    public static boolean esFutura(String fecha) {
        return comparar(fecha, hoy()) > 0;
    }

    /** Edad en años cumplidos, o -1 si la fecha no es válida. */
    public static int edad(String fechaNac) {
        if (!fechaValida(fechaNac)) return -1;
        Calendar nac = aCalendario(fechaNac);
        Calendar hoy = Calendar.getInstance();
        int edad = hoy.get(Calendar.YEAR) - nac.get(Calendar.YEAR);
        int mesHoy = hoy.get(Calendar.MONTH);
        int mesNac = nac.get(Calendar.MONTH);
        if (mesHoy < mesNac || (mesHoy == mesNac && hoy.get(Calendar.DAY_OF_MONTH) < nac.get(Calendar.DAY_OF_MONTH))) {
            edad--;
        }
        return edad;
    }

    public static String edadTexto(String fechaNac) {
        int e = edad(fechaNac);
        return e < 0 ? "—" : e + " años";
    }

    /** Revisa si la edad cae en un rango del filtro: "Todos", "6 - 10" o "46+". */
    public static boolean enRango(int edad, String rango) {
        if (rango == null || rango.equals("Todos") || rango.equals("Todas")) return true;
        if (edad < 0) return false;
        String r = rango.replace(" ", "");
        if (r.endsWith("+")) {
            return edad >= Integer.parseInt(r.substring(0, r.length() - 1));
        }
        String[] partes = r.split("-");
        return edad >= Integer.parseInt(partes[0]) && edad <= Integer.parseInt(partes[1]);
    }

    /** Ejemplo: "Lunes 9 de noviembre de 2026". */
    public static String fechaLarga(Calendar c) {
        return DIAS[c.get(Calendar.DAY_OF_WEEK) - 1] + " " + c.get(Calendar.DAY_OF_MONTH) + " de "
                + MESES[c.get(Calendar.MONTH)] + " de " + c.get(Calendar.YEAR);
    }

    /** Ejemplo: "Noviembre 2026". */
    public static String mesAnio(Calendar c) {
        return capitalizar(MESES[c.get(Calendar.MONTH)]) + " " + c.get(Calendar.YEAR);
    }

    /** Ejemplo: "Miércoles 23". */
    public static String diaCorto(String fecha) {
        Calendar c = aCalendario(fecha);
        return DIAS[c.get(Calendar.DAY_OF_WEEK) - 1] + " " + c.get(Calendar.DAY_OF_MONTH);
    }

    // ------------------------------------------------------------------ Dinero

    public static String dinero(double monto) {
        String s = String.format(Locale.US, "$%,.2f", Math.abs(monto));
        if (s.endsWith(".00")) s = s.substring(0, s.length() - 3);
        return monto < 0 ? "-" + s : s;
    }

    // ------------------------------------------------------------------ Roles

    public static String rolLegible(String rol) {
        if ("ADMINISTRADOR".equals(rol)) return "Administrador";
        if ("PROFESOR".equals(rol)) return "Profesor";
        if ("RESPONSABLE".equals(rol)) return "Padre/Madre/Tutor";
        if ("ALUMNO".equals(rol)) return "Alumno";
        return nulo(rol);
    }

    public static int posicion(String[] arreglo, String valor) {
        for (int i = 0; i < arreglo.length; i++) {
            if (arreglo[i].equals(valor)) return i;
        }
        return 0;
    }

    // ------------------------------------------------------------------ Contraseñas

    public static String nuevaSal() {
        byte[] b = new byte[16];
        new SecureRandom().nextBytes(b);
        return hex(b);
    }

    /** SHA-256 de sal + contraseña. La contraseña nunca se guarda en texto plano. */
    public static String hash(String sal, String contrasena) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return hex(md.digest((sal + contrasena).getBytes("UTF-8")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format(Locale.ROOT, "%02x", b));
        }
        return sb.toString();
    }
}
