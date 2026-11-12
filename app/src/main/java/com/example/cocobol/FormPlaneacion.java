package com.example.cocobol;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

/** Agregar / editar / eliminar un entrenamiento. Recibe "id" para editar o "id_profesor" para uno nuevo. */
public class FormPlaneacion extends ActividadBase {

    EditText fecha, hora, titulo, ejercicios;
    Spinner profesor, equipo;
    ArrayList<Long> idsProfesor = new ArrayList<>();
    ArrayList<Long> idsEquipo = new ArrayList<>();
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.form_planeacion, R.id.menu_lateral_personal);

        fecha = findViewById(R.id.f_fecha);
        hora = findViewById(R.id.f_hora);
        titulo = findViewById(R.id.f_titulo);
        ejercicios = findViewById(R.id.f_ejercicios);
        profesor = findViewById(R.id.f_profesor);
        equipo = findViewById(R.id.f_equipo);

        cargarOpciones(profesor, idsProfesor, null, "SELECT id, nombre || ' ' || paterno FROM personal "
                + "WHERE tipo = 'PROFESOR' ORDER BY nombre");
        cargarOpciones(equipo, idsEquipo, "Sin categoría", "SELECT id, categoria FROM equipo ORDER BY categoria");

        fecha.setOnClickListener(this);
        hora.setOnClickListener(this);
        botonesFormulario();

        id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            modoEditar("Editar entrenamiento");
            cargar();
        } else {
            long prof = getIntent().getLongExtra("id_profesor", -1);
            seleccionarId(profesor, idsProfesor, prof);
            // Si el profesor tiene categoría a cargo, se propone esa
            String eq = bd.valor("SELECT id FROM equipo WHERE profesor_id = ? LIMIT 1",
                    String.valueOf(idSeleccionado(profesor, idsProfesor)));
            if (!eq.isEmpty()) seleccionarId(equipo, idsEquipo, Long.parseLong(eq));
            fecha.setText(Util.hoy());
            titulo.setText("Entrenamiento");
        }
        if (idsProfesor.isEmpty()) {
            error(null, "Primero registra un profesor en Personal > Profesores.");
        }
    }

    void cargar() {
        Cursor c = bd.consultar("SELECT personal_id, equipo_id, fecha, hora, titulo, ejercicios FROM planeacion "
                + "WHERE id = ?", String.valueOf(id));
        if (!c.moveToFirst()) {
            c.close();
            toast("El entrenamiento ya no existe.");
            finish();
            return;
        }
        seleccionarId(profesor, idsProfesor, c.getLong(0));
        seleccionarId(equipo, idsEquipo, c.isNull(1) ? -1 : c.getLong(1));
        fecha.setText(c.getString(2));
        hora.setText(c.getString(3));
        titulo.setText(c.getString(4));
        ejercicios.setText(c.getString(5));
        c.close();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int vid = v.getId();
        if (vid == R.id.f_fecha) {
            elegirFecha(fecha);
        } else if (vid == R.id.f_hora) {
            elegirHora(hora);
        } else if (vid == R.id.form_guardar) {
            guardar();
        } else if (vid == R.id.form_cancelar) {
            finish();
        } else if (vid == R.id.form_eliminar) {
            confirmar("¿Eliminar este entrenamiento?", 1);
        }
    }

    @Override
    protected void alConfirmar(int accion) {
        bd.eliminar("planeacion", id);
        toast("Entrenamiento eliminado.");
        finish();
    }

    void guardar() {
        long prof = idSeleccionado(profesor, idsProfesor);
        if (prof == -1) {
            error(null, "Primero registra un profesor en Personal > Profesores.");
            return;
        }
        if (!Util.fechaValida(texto(fecha))) {
            error(fecha, "Elige la fecha del entrenamiento.");
            return;
        }
        if (!texto(hora).isEmpty() && !Util.horaValida(texto(hora))) {
            error(hora, "La hora debe tener el formato hh:mm.");
            return;
        }
        if (texto(titulo).length() < 3) {
            error(titulo, "Escribe un título de al menos 3 caracteres.");
            return;
        }
        int lineas = 0;
        for (String l : texto(ejercicios).split("\n")) {
            if (!l.trim().isEmpty()) lineas++;
        }
        if (lineas == 0) {
            error(ejercicios, "Escribe al menos un ejercicio.");
            return;
        }
        if (lineas > 15) {
            error(ejercicios, "Máximo 15 ejercicios por entrenamiento.");
            return;
        }

        long eq = idSeleccionado(equipo, idsEquipo);
        android.content.ContentValues v = BaseDatos.cv("personal_id", prof, "equipo_id", BaseDatos.idONulo(eq),
                "fecha", texto(fecha), "hora", texto(hora), "titulo", texto(titulo), "ejercicios", texto(ejercicios));
        if (id == -1) {
            bd.insertar("planeacion", v);
            toast("Entrenamiento guardado.");
        } else {
            bd.actualizar("planeacion", v, id);
            toast("Cambios guardados.");
        }
        finish();
    }
}
