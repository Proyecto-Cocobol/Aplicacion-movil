package com.example.cocobol;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

/** Agregar / editar / eliminar una nota de la bitácora. Recibe "id" para editar. */
public class FormBitacora extends ActividadBase {

    EditText fecha, alumno, detalle;
    Spinner tema;
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.form_bitacora, R.id.menu_lateral_calendario);

        fecha = findViewById(R.id.f_fecha);
        alumno = findViewById(R.id.f_alumno);
        detalle = findViewById(R.id.f_detalle);
        tema = findViewById(R.id.f_tema);

        fecha.setOnClickListener(this);
        botonesFormulario();

        id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            modoEditar("Editar nota");
            cargar();
        } else {
            fecha.setText(Util.hoy());
            seleccionarTexto(tema, "General");
        }
    }

    void cargar() {
        Cursor c = bd.consultar("SELECT fecha, tema, alumno, detalle FROM bitacora WHERE id = ?", String.valueOf(id));
        if (!c.moveToFirst()) {
            c.close();
            toast("La nota ya no existe.");
            finish();
            return;
        }
        fecha.setText(c.getString(0));
        seleccionarTexto(tema, c.getString(1));
        alumno.setText(c.getString(2));
        detalle.setText(c.getString(3));
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
            confirmar("¿Eliminar esta nota de la bitácora?", 1);
        }
    }

    @Override
    protected void alConfirmar(int accion) {
        bd.eliminar("bitacora", id);
        toast("Nota eliminada.");
        finish();
    }

    void guardar() {
        if (!Util.fechaValida(texto(fecha))) {
            error(fecha, "Elige la fecha.");
            return;
        }
        if (!texto(alumno).isEmpty() && !Util.soloLetras(texto(alumno))) {
            error(alumno, "El nombre del alumno solo puede tener letras.");
            return;
        }
        if (texto(detalle).length() < 3) {
            error(detalle, "Escribe el detalle de la nota.");
            return;
        }
        ContentValues v = BaseDatos.cv("fecha", texto(fecha), "tema", textoSpinner(tema), "alumno", texto(alumno),
                "detalle", texto(detalle));
        if (id == -1) {
            bd.insertar("bitacora", v);
            toast("Nota guardada.");
        } else {
            bd.actualizar("bitacora", v, id);
            toast("Cambios guardados.");
        }
        finish();
    }
}
