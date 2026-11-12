package com.example.cocobol;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

/** Agregar / editar / eliminar equipo (categoría). Recibe "id" para editar. */
public class FormEquipo extends ActividadBase {

    EditText categoria, edad, torneo;
    Spinner profesor;
    ArrayList<Long> idsProfesor = new ArrayList<>();
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.form_equipo, R.id.menu_lateral_equipos);

        categoria = findViewById(R.id.f_categoria);
        edad = findViewById(R.id.f_edad);
        torneo = findViewById(R.id.f_torneo);
        profesor = findViewById(R.id.f_profesor);
        cargarOpciones(profesor, idsProfesor, "Sin asignar", "SELECT id, nombre || ' ' || paterno FROM personal "
                + "WHERE tipo = 'PROFESOR' ORDER BY nombre");
        botonesFormulario();

        id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            modoEditar("Editar equipo");
            cargar();
        }
    }

    void cargar() {
        Cursor c = bd.consultar("SELECT categoria, edad, profesor_id, torneo FROM equipo WHERE id = ?",
                String.valueOf(id));
        if (!c.moveToFirst()) {
            c.close();
            toast("El equipo ya no existe.");
            finish();
            return;
        }
        categoria.setText(c.getString(0));
        edad.setText(c.getString(1));
        seleccionarId(profesor, idsProfesor, c.isNull(2) ? -1 : c.getLong(2));
        torneo.setText(c.getString(3));
        c.close();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int vid = v.getId();
        if (vid == R.id.form_guardar) {
            guardar();
        } else if (vid == R.id.form_cancelar) {
            finish();
        } else if (vid == R.id.form_eliminar) {
            confirmar("¿Eliminar la categoría " + texto(categoria) + "? Sus jugadores quedarán sin categoría.", 1);
        }
    }

    @Override
    protected void alConfirmar(int accion) {
        bd.eliminarEquipo(id);
        bd.registrarBitacora("Equipos", "", "Categoría " + texto(categoria) + " eliminada");
        toast("Equipo eliminado.");
        finish();
    }

    void guardar() {
        String cat = texto(categoria);
        String ed = texto(edad).replace(" ", "");
        if (cat.length() < 2) {
            error(categoria, "Escribe el nombre de la categoría (por ejemplo 2009-2012).");
            return;
        }
        if (bd.existe("SELECT id FROM equipo WHERE categoria = ? COLLATE NOCASE AND id <> ?", cat, String.valueOf(id))) {
            error(categoria, "Ya existe una categoría con ese nombre.");
            return;
        }
        if (!ed.matches("\\d{1,2}-\\d{1,2}")) {
            error(edad, "El rango de edad debe tener el formato 13-16.");
            return;
        }
        String[] partes = ed.split("-");
        if (Integer.parseInt(partes[0]) > Integer.parseInt(partes[1])) {
            error(edad, "La edad inicial no puede ser mayor que la final.");
            return;
        }

        ContentValues v = BaseDatos.cv("categoria", cat, "edad", ed,
                "profesor_id", BaseDatos.idONulo(idSeleccionado(profesor, idsProfesor)), "torneo", texto(torneo));
        if (id == -1) {
            bd.insertar("equipo", v);
            bd.registrarBitacora("Equipos", "", "Nueva categoría " + cat);
            toast("Equipo guardado.");
        } else {
            bd.actualizar("equipo", v, id);
            toast("Cambios guardados.");
        }
        finish();
    }
}
