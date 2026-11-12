package com.example.cocobol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Base de datos LOCAL del teléfono (SQLite).
 * Todavía NO se conecta con MySQL/Azure: guarda aquí lo que se agrega, edita o elimina en la app.
 * Las tablas son una versión simplificada de academia_lobos_v1.sql.
 */
public class BaseDatos extends SQLiteOpenHelper {

    private static final String NOMBRE = "academia_lobos.db";
    private static final int VERSION = 1;
    private static BaseDatos instancia;

    public static synchronized BaseDatos obtener(Context contexto) {
        if (instancia == null) {
            instancia = new BaseDatos(contexto.getApplicationContext());
        }
        return instancia;
    }

    private BaseDatos(Context contexto) {
        super(contexto, NOMBRE, null, VERSION);
    }

    // ================================================================== Tablas

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE cuenta (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, "
                + "paterno TEXT NOT NULL, materno TEXT, correo TEXT, telefono TEXT, usuario TEXT NOT NULL UNIQUE, "
                + "hash TEXT NOT NULL, sal TEXT NOT NULL, rol TEXT NOT NULL, boleta TEXT, puesto TEXT, "
                + "parentesco TEXT, activo INTEGER NOT NULL DEFAULT 1)");
        db.execSQL("CREATE TABLE equipo (id INTEGER PRIMARY KEY AUTOINCREMENT, categoria TEXT NOT NULL, "
                + "edad TEXT, profesor_id INTEGER, torneo TEXT)");
        db.execSQL("CREATE TABLE alumno (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, "
                + "paterno TEXT NOT NULL, materno TEXT, fecha_nac TEXT NOT NULL, curp TEXT, escuela TEXT, "
                + "direccion TEXT, fecha_ingreso TEXT, equipo_id INTEGER, numero INTEGER, nombre_playera TEXT, "
                + "sangre TEXT, alergias TEXT, seguro TEXT, padre TEXT, tel_padre TEXT, madre TEXT, tel_madre TEXT, "
                + "tutor TEXT, tel_tutor TEXT, partidos INTEGER DEFAULT 0, goles INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE personal (id INTEGER PRIMARY KEY AUTOINCREMENT, tipo TEXT NOT NULL, "
                + "nombre TEXT NOT NULL, paterno TEXT NOT NULL, materno TEXT, fecha_nac TEXT, telefono TEXT, "
                + "correo TEXT, puesto TEXT, equipo_id INTEGER)");
        db.execSQL("CREATE TABLE planeacion (id INTEGER PRIMARY KEY AUTOINCREMENT, personal_id INTEGER NOT NULL, "
                + "equipo_id INTEGER, fecha TEXT NOT NULL, hora TEXT, titulo TEXT NOT NULL, ejercicios TEXT)");
        db.execSQL("CREATE TABLE movimiento (id INTEGER PRIMARY KEY AUTOINCREMENT, alumno_id INTEGER NOT NULL, "
                + "concepto TEXT NOT NULL, periodo TEXT, monto REAL NOT NULL, estado TEXT NOT NULL, fecha TEXT)");
        db.execSQL("CREATE TABLE evaluacion (id INTEGER PRIMARY KEY AUTOINCREMENT, alumno_id INTEGER NOT NULL, "
                + "area TEXT NOT NULL, aspecto TEXT NOT NULL, calificacion INTEGER NOT NULL, observaciones TEXT, "
                + "fecha TEXT)");
        db.execSQL("CREATE TABLE evento (id INTEGER PRIMARY KEY AUTOINCREMENT, titulo TEXT NOT NULL, "
                + "tipo TEXT NOT NULL, fecha TEXT NOT NULL, hora TEXT, equipo_id INTEGER, lugar TEXT)");
        db.execSQL("CREATE TABLE bitacora (id INTEGER PRIMARY KEY AUTOINCREMENT, fecha TEXT NOT NULL, "
                + "tema TEXT NOT NULL, alumno TEXT, detalle TEXT)");
        db.execSQL("CREATE TABLE documento (id INTEGER PRIMARY KEY AUTOINCREMENT, alumno_id INTEGER NOT NULL, "
                + "tipo TEXT NOT NULL, archivo TEXT, uri TEXT, fecha TEXT)");
        db.execSQL("CREATE TABLE pedido (id INTEGER PRIMARY KEY AUTOINCREMENT, tipo TEXT NOT NULL, "
                + "cantidad INTEGER NOT NULL, fecha TEXT, fabrica TEXT, estado TEXT)");
        db.execSQL("CREATE TABLE uniforme (id INTEGER PRIMARY KEY AUTOINCREMENT, alumno_id INTEGER NOT NULL, "
                + "tipo TEXT NOT NULL, talla TEXT NOT NULL, nombre_playera TEXT, numero INTEGER)");

        datosIniciales(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int anterior, int nueva) {
        String[] tablas = {"cuenta", "equipo", "alumno", "personal", "planeacion", "movimiento", "evaluacion",
                "evento", "bitacora", "documento", "pedido", "uniforme"};
        for (String t : tablas) {
            db.execSQL("DROP TABLE IF EXISTS " + t);
        }
        onCreate(db);
    }

    /** Datos de ejemplo (los mismos de la página web) para que la app no inicie vacía. */
    private void datosIniciales(SQLiteDatabase db) {
        insertarCuenta(db, "Administrador", "Lobos", "", "admin@correo.com", "", "admin", "admin",
                "ADMINISTRADOR", "", "", "", true);
        insertarCuenta(db, "Juan", "Pérez", "", "jperez@correo.com", "", "alumno", "alumno",
                "ALUMNO", "2025000001", "", "", true);
        insertarCuenta(db, "Pedro", "Ramírez", "", "pramirez@correo.com", "", "profesor", "profesor",
                "PROFESOR", "", "Entrenador", "", true);

        long prof = db.insert("personal", null, cv("tipo", "PROFESOR", "nombre", "Juan", "paterno", "Pérez",
                "materno", "", "fecha_nac", "10/10/1986", "telefono", "5512345678", "correo", "profe.juan@correo.com",
                "puesto", "Profesor"));
        db.insert("personal", null, cv("tipo", "ADMINISTRATIVO", "nombre", "Ana", "paterno", "Gómez",
                "materno", "Ruiz", "fecha_nac", "05/03/1985", "telefono", "5587654321", "correo", "ana@correo.com",
                "puesto", "Director"));

        long eq1 = db.insert("equipo", null, cv("categoria", "2009-2012", "edad", "13-16",
                "profesor_id", prof, "torneo", "Torneo X"));
        long eq2 = db.insert("equipo", null, cv("categoria", "2013-2016", "edad", "9-12",
                "profesor_id", null, "torneo", "Liga Municipal"));
        db.execSQL("UPDATE personal SET equipo_id = " + eq1 + " WHERE id = " + prof);

        long juan = db.insert("alumno", null, cv("nombre", "Juan", "paterno", "Pérez", "materno", "",
                "fecha_nac", "10/10/2010", "curp", "", "escuela", "Escuelita", "direccion", "Calle 14",
                "fecha_ingreso", "20/07/2025", "equipo_id", eq1, "numero", 10, "nombre_playera", "Juanito",
                "sangre", "O+", "alergias", "Ninguna", "seguro", "Ninguno", "padre", "Juan Pérez",
                "tel_padre", "5511112222", "madre", "María Pérez", "tel_madre", "5533334444", "tutor", "",
                "tel_tutor", "", "partidos", 4, "goles", 0));
        long luis = db.insert("alumno", null, cv("nombre", "Luis", "paterno", "Martínez", "materno", "",
                "fecha_nac", "12/04/2012", "curp", "", "escuela", "Primaria Benito Juárez", "direccion", "Av. Central 5",
                "fecha_ingreso", "01/08/2025", "equipo_id", eq2, "numero", 14, "nombre_playera", "Luis",
                "sangre", "A+", "alergias", "Ninguna", "seguro", "IMSS", "padre", "", "tel_padre", "",
                "madre", "Laura Martínez", "tel_madre", "5555556666", "tutor", "", "tel_tutor", "",
                "partidos", 2, "goles", 1));

        db.insert("movimiento", null, cv("alumno_id", juan, "concepto", "Mensualidad", "periodo", "Septiembre",
                "monto", 500, "estado", "Pagado", "fecha", "05/09/2025"));
        db.insert("movimiento", null, cv("alumno_id", juan, "concepto", "Mensualidad", "periodo", "Octubre",
                "monto", 500, "estado", "Adeudo", "fecha", "05/10/2025"));
        db.insert("movimiento", null, cv("alumno_id", juan, "concepto", "Torneo", "periodo", "Periodo actual",
                "monto", 100, "estado", "Pagado", "fecha", "15/09/2025"));
        db.insert("movimiento", null, cv("alumno_id", luis, "concepto", "Mensualidad", "periodo", "Septiembre",
                "monto", 500, "estado", "Pagado", "fecha", "03/09/2025"));

        db.insert("evaluacion", null, cv("alumno_id", juan, "area", "Técnico", "aspecto", "Pase",
                "calificacion", 8, "observaciones", "Buen control del balón.", "fecha", "20/09/2025"));
        db.insert("evaluacion", null, cv("alumno_id", juan, "area", "Técnico", "aspecto", "Tiro",
                "calificacion", 7, "observaciones", "Mejorar la precisión.", "fecha", "20/09/2025"));
        db.insert("evaluacion", null, cv("alumno_id", juan, "area", "Físico", "aspecto", "Resistencia",
                "calificacion", 9, "observaciones", "", "fecha", "20/09/2025"));

        db.insert("evento", null, cv("titulo", "Entrenamiento", "tipo", "Entreno", "fecha", Util.sumarDias(2),
                "hora", "17:00", "equipo_id", eq1, "lugar", "Cancha 1"));
        db.insert("evento", null, cv("titulo", "Partido amistoso", "tipo", "Partido", "fecha", Util.sumarDias(4),
                "hora", "10:00", "equipo_id", eq1, "lugar", "Unidad deportiva"));
        db.insert("evento", null, cv("titulo", "Junta de padres", "tipo", "Junta", "fecha", Util.sumarDias(7),
                "hora", "18:30", "equipo_id", null, "lugar", "Oficina"));

        db.insert("planeacion", null, cv("personal_id", prof, "equipo_id", eq1, "fecha", Util.sumarDias(2),
                "hora", "17:00", "titulo", "Entrenamiento",
                "ejercicios", "Calentamiento clásico\nRutina tren inferior\nRondo de pases"));

        db.insert("bitacora", null, cv("fecha", Util.hoy(), "tema", "Pagos", "alumno", "Juan Pérez",
                "detalle", "Concepto: Uniforme · Total: $500"));
        db.insert("bitacora", null, cv("fecha", Util.hoy(), "tema", "Alumno Nuevo", "alumno", "Luis Martínez",
                "detalle", "Solicitud: muestraLuis.pdf"));

        db.insert("documento", null, cv("alumno_id", juan, "tipo", "CURP", "archivo", "curp_juan.pdf",
                "uri", "", "fecha", "11/09/2025"));
        db.insert("documento", null, cv("alumno_id", juan, "tipo", "Acta de nacimiento", "archivo", "acta_juan.pdf",
                "uri", "", "fecha", "12/09/2025"));

        db.insert("pedido", null, cv("tipo", "Visita", "cantidad", 40, "fecha", "10/10/2025",
                "fabrica", "Fábrica Alve", "estado", "Entregado"));
        db.insert("pedido", null, cv("tipo", "Local", "cantidad", 30, "fecha", Util.hoy(),
                "fabrica", "Fábrica Zublima", "estado", "Pendiente"));

        db.insert("uniforme", null, cv("alumno_id", juan, "tipo", "Visita", "talla", "M",
                "nombre_playera", "Juanito", "numero", 10));
    }

    // ================================================================== Utilidades

    /** Arma un ContentValues a partir de pares nombre, valor. */
    public static ContentValues cv(Object... pares) {
        ContentValues v = new ContentValues();
        for (int i = 0; i < pares.length; i += 2) {
            String k = (String) pares[i];
            Object o = pares[i + 1];
            if (o == null) {
                v.putNull(k);
            } else if (o instanceof Integer) {
                v.put(k, (Integer) o);
            } else if (o instanceof Long) {
                v.put(k, (Long) o);
            } else if (o instanceof Double) {
                v.put(k, (Double) o);
            } else {
                v.put(k, o.toString());
            }
        }
        return v;
    }

    /** Convierte un id de Spinner en valor para la base: -1 se guarda como NULL. */
    public static Object idONulo(long id) {
        return id < 0 ? null : id;
    }

    public Cursor consultar(String sql, String... args) {
        return getReadableDatabase().rawQuery(sql, args);
    }

    /** Primera columna del primer renglón, o "" si no hay resultado. */
    public String valor(String sql, String... args) {
        Cursor c = consultar(sql, args);
        String r = "";
        if (c.moveToFirst() && !c.isNull(0)) {
            r = c.getString(0);
        }
        c.close();
        return r;
    }

    public double numero(String sql, String... args) {
        Cursor c = consultar(sql, args);
        double r = 0;
        if (c.moveToFirst() && !c.isNull(0)) {
            r = c.getDouble(0);
        }
        c.close();
        return r;
    }

    public boolean existe(String sql, String... args) {
        Cursor c = consultar(sql, args);
        boolean r = c.moveToFirst();
        c.close();
        return r;
    }

    public long insertar(String tabla, ContentValues valores) {
        return getWritableDatabase().insert(tabla, null, valores);
    }

    public void actualizar(String tabla, ContentValues valores, long id) {
        getWritableDatabase().update(tabla, valores, "id = ?", new String[]{String.valueOf(id)});
    }

    public void eliminar(String tabla, long id) {
        getWritableDatabase().delete(tabla, "id = ?", new String[]{String.valueOf(id)});
    }

    /** Agrega un registro automático a la bitácora. */
    public void registrarBitacora(String tema, String alumno, String detalle) {
        insertar("bitacora", cv("fecha", Util.hoy(), "tema", tema, "alumno", alumno, "detalle", detalle));
    }

    // ================================================================== Borrados en cascada

    public void eliminarAlumno(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] a = {String.valueOf(id)};
        db.beginTransaction();
        try {
            db.delete("movimiento", "alumno_id = ?", a);
            db.delete("evaluacion", "alumno_id = ?", a);
            db.delete("documento", "alumno_id = ?", a);
            db.delete("uniforme", "alumno_id = ?", a);
            db.delete("alumno", "id = ?", a);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void eliminarEquipo(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] a = {String.valueOf(id)};
        db.beginTransaction();
        try {
            db.execSQL("UPDATE alumno SET equipo_id = NULL WHERE equipo_id = ?", a);
            db.execSQL("UPDATE personal SET equipo_id = NULL WHERE equipo_id = ?", a);
            db.execSQL("UPDATE evento SET equipo_id = NULL WHERE equipo_id = ?", a);
            db.execSQL("UPDATE planeacion SET equipo_id = NULL WHERE equipo_id = ?", a);
            db.delete("equipo", "id = ?", a);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void eliminarPersonal(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] a = {String.valueOf(id)};
        db.beginTransaction();
        try {
            db.execSQL("UPDATE equipo SET profesor_id = NULL WHERE profesor_id = ?", a);
            db.delete("planeacion", "personal_id = ?", a);
            db.delete("personal", "id = ?", a);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // ================================================================== Consultas comunes

    /** Saldo del alumno: negativo si debe (suma de cargos en Adeudo o Pendiente). */
    public double saldoAlumno(long idAlumno) {
        return -numero("SELECT SUM(monto) FROM movimiento WHERE alumno_id = ? AND estado <> 'Pagado'",
                String.valueOf(idAlumno));
    }

    public String nombreAlumno(long idAlumno) {
        return valor("SELECT nombre || ' ' || paterno FROM alumno WHERE id = ?", String.valueOf(idAlumno));
    }

    // ================================================================== Cuentas

    public static long insertarCuenta(SQLiteDatabase db, String nombre, String paterno, String materno,
                                      String correo, String telefono, String usuario, String contrasena,
                                      String rol, String boleta, String puesto, String parentesco, boolean activa) {
        String sal = Util.nuevaSal();
        return db.insert("cuenta", null, cv("nombre", nombre, "paterno", paterno, "materno", materno,
                "correo", correo, "telefono", telefono, "usuario", usuario, "hash", Util.hash(sal, contrasena),
                "sal", sal, "rol", rol, "boleta", boleta, "puesto", puesto, "parentesco", parentesco,
                "activo", activa ? 1 : 0));
    }

    public void cambiarContrasena(long idCuenta, String nueva) {
        String sal = Util.nuevaSal();
        actualizar("cuenta", cv("hash", Util.hash(sal, nueva), "sal", sal), idCuenta);
    }

    /** Revisa si la contraseña corresponde a la cuenta. */
    public boolean contrasenaCorrecta(long idCuenta, String contrasena) {
        Cursor c = consultar("SELECT hash, sal FROM cuenta WHERE id = ?", String.valueOf(idCuenta));
        boolean ok = false;
        if (c.moveToFirst()) {
            ok = c.getString(0).equals(Util.hash(c.getString(1), contrasena));
        }
        c.close();
        return ok;
    }
}
