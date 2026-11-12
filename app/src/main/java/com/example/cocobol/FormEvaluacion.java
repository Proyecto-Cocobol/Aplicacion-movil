package com.example.cocobol;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

/** Agregar / editar / eliminar evaluación. Recibe "id" para editar o "id_alumno" y "area" para una nueva. */
public class FormEvaluacion extends ActividadBase {

    EditText aspecto, calificacion, fecha, observaciones;
    Spinner alumno, area;
    ArrayList<Long> idsAlumno = new ArrayList<>();
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.form_evaluacion, R.id.menu_lateral_equipos);

        aspecto = findViewById(R.id.f_aspecto);
        calificacion = findViewById(R.id.f_calificacion);
        fecha = findViewById(R.id.f_fecha);
        observaciones = findViewById(R.id.f_observaciones);
        alumno = findViewById(R.id.f_alumno);
        area = findViewById(R.id.f_area);

        cargarOpciones(alumno, idsAlumno, null, "SELECT id, nombre || ' ' || paterno FROM alumno ORDER BY nombre, paterno");
        fecha.setOnClickListener(this);
        botonesFormulario();

        id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            modoEditar("Editar evaluación");
            cargar();
        } else {
            seleccionarId(alumno, idsAlumno, getIntent().getLongExtra("id_alumno", -1));
            String a = getIntent().getStringExtra("area");
            if (!Util.vacio(a)) seleccionarTexto(area, a);
            fecha.setText(Util.hoy());
        }
    }

    void cargar() {
        Cursor c = bd.consultar("SELECT alumno_id, area, aspecto, calificacion, observaciones, fecha FROM evaluacion "
                + "WHERE id = ?", String.valueOf(id));
        if (!c.moveToFirst()) {
            c.close();
            toast("La evaluación ya no existe.");
            finish();
            return;
        }
        seleccionarId(alumno, idsAlumno, c.getLong(0));
        seleccionarTexto(area, c.getString(1));
        aspecto.setText(c.getString(2));
        calificacion.setText(String.valueOf(c.getInt(3)));
        observaciones.setText(c.getString(4));
        fecha.setText(c.getString(5));
        c.close();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int vid = v.getId();
        if (vid == R.id.f_fecha) {
            elegirFecha(fecha);
        } else if (vid == R.id.form_guardar) {
            guardar();
        } else if (vid == R.id.form_cancelar) {
            finish();
        } else if (vid == R.id.form_eliminar) {
            confirmar("¿Eliminar esta evaluación?", 1);
        }
    }

    @Override
    protected void alConfirmar(int accion) {
        bd.eliminar("evaluacion", id);
        toast("Evaluación eliminada.");
        finish();
    }

    void guardar() {
        long idAl = idSeleccionado(alumno, idsAlumno);
        if (idAl == -1) {
            error(null, "Primero registra un alumno.");
            return;
        }
        if (texto(aspecto).length() < 2) {
            error(aspecto, "Escribe el aspecto evaluado (por ejemplo Pase).");
            return;
        }
        int cal;
        try {
            cal = Integer.parseInt(texto(calificacion));
        } catch (NumberFormatException e) {
            cal = -1;
        }
        if (cal < 1 || cal > 10) {
            error(calificacion, "La calificación debe ser un número del 1 al 10.");
            return;
        }
        if (!Util.fechaValida(texto(fecha))) {
            error(fecha, "Elige la fecha.");
            return;
        }
        if (Util.esFutura(texto(fecha))) {
            error(fecha, "La fecha de la evaluación no puede ser futura.");
            return;
        }

        ContentValues v = BaseDatos.cv("alumno_id", idAl, "area", textoSpinner(area), "aspecto", texto(aspecto),
                "calificacion", cal, "observaciones", texto(observaciones), "fecha", texto(fecha));
        if (id == -1) {
            bd.insertar("evaluacion", v);
            toast("Evaluación guardada.");
        } else {
            bd.actualizar("evaluacion", v, id);
            toast("Cambios guardados.");
        }
        finish();
    }
}
