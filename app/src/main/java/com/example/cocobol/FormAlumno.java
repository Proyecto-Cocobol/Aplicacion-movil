package com.example.cocobol;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Locale;

/** Agregar / editar / eliminar alumno. Recibe "id" para editar. */
public class FormAlumno extends ActividadBase {

    EditText nombre, paterno, materno, fechaNac, curp, escuela, direccion, ingreso, numero, playera,
            partidos, goles, alergias, seguro, padre, telPadre, madre, telMadre, tutor, telTutor;
    Spinner equipo, sangre;
    ArrayList<Long> idsEquipo = new ArrayList<>();
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.form_alumno, R.id.menu_lateral_alumnos);

        nombre = findViewById(R.id.f_nombre);
        paterno = findViewById(R.id.f_paterno);
        materno = findViewById(R.id.f_materno);
        fechaNac = findViewById(R.id.f_fecha_nac);
        curp = findViewById(R.id.f_curp);
        escuela = findViewById(R.id.f_escuela);
        direccion = findViewById(R.id.f_direccion);
        ingreso = findViewById(R.id.f_ingreso);
        numero = findViewById(R.id.f_numero);
        playera = findViewById(R.id.f_playera);
        partidos = findViewById(R.id.f_partidos);
        goles = findViewById(R.id.f_goles);
        alergias = findViewById(R.id.f_alergias);
        seguro = findViewById(R.id.f_seguro);
        padre = findViewById(R.id.f_padre);
        telPadre = findViewById(R.id.f_tel_padre);
        madre = findViewById(R.id.f_madre);
        telMadre = findViewById(R.id.f_tel_madre);
        tutor = findViewById(R.id.f_tutor);
        telTutor = findViewById(R.id.f_tel_tutor);
        equipo = findViewById(R.id.f_equipo);
        sangre = findViewById(R.id.f_sangre);

        cargarOpciones(equipo, idsEquipo, "Sin categoría", "SELECT id, categoria FROM equipo ORDER BY categoria");

        fechaNac.setOnClickListener(this);
        ingreso.setOnClickListener(this);
        botonesFormulario();

        id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            modoEditar("Editar alumno");
            cargar();
        } else {
            ingreso.setText(Util.hoy());
        }
    }

    void cargar() {
        Cursor c = bd.consultar("SELECT nombre, paterno, materno, fecha_nac, curp, escuela, direccion, fecha_ingreso, "
                + "equipo_id, numero, nombre_playera, sangre, alergias, seguro, padre, tel_padre, madre, tel_madre, "
                + "tutor, tel_tutor, partidos, goles FROM alumno WHERE id = ?", String.valueOf(id));
        if (!c.moveToFirst()) {
            c.close();
            toast("El alumno ya no existe.");
            finish();
            return;
        }
        nombre.setText(c.getString(0));
        paterno.setText(c.getString(1));
        materno.setText(c.getString(2));
        fechaNac.setText(c.getString(3));
        curp.setText(c.getString(4));
        escuela.setText(c.getString(5));
        direccion.setText(c.getString(6));
        ingreso.setText(c.getString(7));
        seleccionarId(equipo, idsEquipo, c.isNull(8) ? -1 : c.getLong(8));
        numero.setText(c.isNull(9) ? "" : String.valueOf(c.getInt(9)));
        playera.setText(c.getString(10));
        seleccionarTexto(sangre, Util.nulo(c.getString(11)));
        alergias.setText(c.getString(12));
        seguro.setText(c.getString(13));
        padre.setText(c.getString(14));
        telPadre.setText(c.getString(15));
        madre.setText(c.getString(16));
        telMadre.setText(c.getString(17));
        tutor.setText(c.getString(18));
        telTutor.setText(c.getString(19));
        partidos.setText(String.valueOf(c.getInt(20)));
        goles.setText(String.valueOf(c.getInt(21)));
        c.close();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int vid = v.getId();
        if (vid == R.id.f_fecha_nac) {
            elegirFecha(fechaNac);
        } else if (vid == R.id.f_ingreso) {
            elegirFecha(ingreso);
        } else if (vid == R.id.form_guardar) {
            guardar();
        } else if (vid == R.id.form_cancelar) {
            finish();
        } else if (vid == R.id.form_eliminar) {
            confirmar("¿Eliminar a " + texto(nombre) + " " + texto(paterno) + "? También se borrarán sus pagos, "
                    + "evaluaciones, documentos y uniformes.", 1);
        }
    }

    @Override
    protected void alConfirmar(int accion) {
        String n = texto(nombre) + " " + texto(paterno);
        bd.eliminarAlumno(id);
        bd.registrarBitacora("General", n, "Alumno eliminado del sistema");
        toast("Alumno eliminado.");
        finish();
    }

    /** Valida un nombre de persona opcional u obligatorio. */
    private boolean nombreValido(EditText campo, String etiqueta, boolean obligatorio) {
        String t = texto(campo);
        if (t.isEmpty()) {
            return !obligatorio || error(campo, etiqueta + " es obligatorio.");
        }
        if (t.length() < 2 || !Util.soloLetras(t)) {
            return error(campo, etiqueta + " solo puede tener letras.");
        }
        return true;
    }

    private boolean telefonoValido(EditText campo) {
        String t = texto(campo);
        return t.isEmpty() || Util.telefonoValido(t) || error(campo, "El teléfono debe tener 10 dígitos.");
    }

    /** Número entero opcional dentro de un rango. Regresa null si está vacío; lanza error si no es válido. */
    private Integer entero(EditText campo, int min, int max, String etiqueta) {
        String t = texto(campo);
        if (t.isEmpty()) return null;
        try {
            int n = Integer.parseInt(t);
            if (n >= min && n <= max) return n;
        } catch (NumberFormatException e) {
            // se reporta abajo
        }
        error(campo, etiqueta + " debe estar entre " + min + " y " + max + ".");
        throw new IllegalArgumentException();
    }

    boolean validar() {
        if (!nombreValido(nombre, "El nombre", true)) return false;
        if (!nombreValido(paterno, "El apellido paterno", true)) return false;
        if (!nombreValido(materno, "El apellido materno", false)) return false;

        String fn = texto(fechaNac);
        if (!Util.fechaValida(fn)) return error(fechaNac, "Elige la fecha de nacimiento.");
        if (Util.esFutura(fn)) return error(fechaNac, "La fecha de nacimiento no puede ser futura.");
        int edad = Util.edad(fn);
        if (edad < 3 || edad > 25) return error(fechaNac, "La edad del alumno debe estar entre 3 y 25 años.");

        String cu = texto(curp).toUpperCase(Locale.ROOT);
        if (!cu.isEmpty() && !Util.curpValida(cu)) return error(curp, "La CURP no tiene un formato válido (18 caracteres).");
        if (!cu.isEmpty() && bd.existe("SELECT id FROM alumno WHERE curp = ? AND id <> ?", cu, String.valueOf(id))) {
            return error(curp, "Ya existe otro alumno con esa CURP.");
        }

        String fi = texto(ingreso);
        if (!Util.fechaValida(fi)) return error(ingreso, "Elige la fecha de ingreso.");
        if (Util.esFutura(fi)) return error(ingreso, "La fecha de ingreso no puede ser futura.");
        if (Util.comparar(fi, fn) <= 0) return error(ingreso, "La fecha de ingreso debe ser posterior al nacimiento.");

        if (!nombreValido(padre, "El nombre del padre", false)) return false;
        if (!nombreValido(madre, "El nombre de la madre", false)) return false;
        if (!nombreValido(tutor, "El nombre del tutor", false)) return false;
        if (!telefonoValido(telPadre) || !telefonoValido(telMadre) || !telefonoValido(telTutor)) return false;
        if (texto(telPadre).isEmpty() && texto(telMadre).isEmpty() && texto(telTutor).isEmpty()) {
            return error(telPadre, "Registra al menos un teléfono de contacto.");
        }
        return true;
    }

    void guardar() {
        if (!validar()) return;
        Integer num;
        Integer part;
        Integer gol;
        try {
            num = entero(numero, 1, 99, "El número de playera");
            part = entero(partidos, 0, 999, "Los partidos");
            gol = entero(goles, 0, 999, "Los goles");
        } catch (IllegalArgumentException e) {
            return;
        }
        long idEquipo = idSeleccionado(equipo, idsEquipo);
        if (num != null && idEquipo != -1 && bd.existe("SELECT id FROM alumno WHERE equipo_id = ? AND numero = ? "
                + "AND id <> ?", String.valueOf(idEquipo), String.valueOf(num), String.valueOf(id))) {
            error(numero, "Ese número ya lo usa otro jugador de la misma categoría.");
            return;
        }

        ContentValues v = BaseDatos.cv("nombre", texto(nombre), "paterno", texto(paterno), "materno", texto(materno),
                "fecha_nac", texto(fechaNac), "curp", texto(curp).toUpperCase(Locale.ROOT), "escuela", texto(escuela),
                "direccion", texto(direccion), "fecha_ingreso", texto(ingreso), "equipo_id", BaseDatos.idONulo(idEquipo),
                "numero", num, "nombre_playera", texto(playera), "sangre", textoSpinner(sangre),
                "alergias", texto(alergias), "seguro", texto(seguro), "padre", texto(padre),
                "tel_padre", texto(telPadre), "madre", texto(madre), "tel_madre", texto(telMadre),
                "tutor", texto(tutor), "tel_tutor", texto(telTutor), "partidos", part == null ? 0 : part,
                "goles", gol == null ? 0 : gol);

        String n = texto(nombre) + " " + texto(paterno);
        if (id == -1) {
            bd.insertar("alumno", v);
            bd.registrarBitacora("Alumno Nuevo", n, "Alumno registrado en el sistema");
            toast("Alumno registrado.");
        } else {
            bd.actualizar("alumno", v, id);
            toast("Cambios guardados.");
        }
        finish();
    }
}
