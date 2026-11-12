package com.example.cocobol;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

/** Agregar / editar / eliminar evento del calendario. Recibe "id" para editar o "fecha" para uno nuevo. */
public class FormEvento extends ActividadBase {

    EditText titulo, fecha, hora, lugar;
    Spinner tipo, equipo;
    ArrayList<Long> idsEquipo = new ArrayList<>();
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.form_evento, R.id.menu_lateral_calendario);

        titulo = findViewById(R.id.f_titulo);
        fecha = findViewById(R.id.f_fecha);
        hora = findViewById(R.id.f_hora);
        lugar = findViewById(R.id.f_lugar);
        tipo = findViewById(R.id.f_tipo);
        equipo = findViewById(R.id.f_equipo);
        cargarOpciones(equipo, idsEquipo, "Toda la academia", "SELECT id, categoria FROM equipo ORDER BY categoria");

        fecha.setOnClickListener(this);
        hora.setOnClickListener(this);
        botonesFormulario();

        id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            modoEditar("Editar evento");
            cargar();
        } else {
            String f = getIntent().getStringExtra("fecha");
            fecha.setText(Util.fechaValida(f) ? f : Util.hoy());
        }
    }

    void cargar() {
        Cursor c = bd.consultar("SELECT titulo, tipo, fecha, hora, equipo_id, lugar FROM evento WHERE id = ?",
                String.valueOf(id));
        if (!c.moveToFirst()) {
            c.close();
            toast("El evento ya no existe.");
            finish();
            return;
        }
        titulo.setText(c.getString(0));
        seleccionarTexto(tipo, c.getString(1));
        fecha.setText(c.getString(2));
        hora.setText(c.getString(3));
        seleccionarId(equipo, idsEquipo, c.isNull(4) ? -1 : c.getLong(4));
        lugar.setText(c.getString(5));
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
            confirmar("¿Eliminar el evento \"" + texto(titulo) + "\"?", 1);
        }
    }

    @Override
    protected void alConfirmar(int accion) {
        bd.eliminar("evento", id);
        toast("Evento eliminado.");
        finish();
    }

    void guardar() {
        if (texto(titulo).length() < 3) {
            error(titulo, "Escribe un título de al menos 3 caracteres.");
            return;
        }
        if (!Util.fechaValida(texto(fecha))) {
            error(fecha, "Elige la fecha del evento.");
            return;
        }
        if (!texto(hora).isEmpty() && !Util.horaValida(texto(hora))) {
            error(hora, "La hora debe tener el formato hh:mm.");
            return;
        }
        long eq = idSeleccionado(equipo, idsEquipo);
        // No se permiten dos eventos de la misma categoría a la misma fecha y hora
        if (!texto(hora).isEmpty() && bd.existe("SELECT id FROM evento WHERE fecha = ? AND hora = ? "
                        + "AND IFNULL(equipo_id, -1) = ? AND id <> ?", texto(fecha), texto(hora), String.valueOf(eq),
                String.valueOf(id))) {
            error(hora, "Ya hay un evento para esa categoría en la misma fecha y hora.");
            return;
        }

        ContentValues v = BaseDatos.cv("titulo", texto(titulo), "tipo", textoSpinner(tipo), "fecha", texto(fecha),
                "hora", texto(hora), "equipo_id", BaseDatos.idONulo(eq), "lugar", texto(lugar));
        if (id == -1) {
            bd.insertar("evento", v);
            toast("Evento guardado.");
        } else {
            bd.actualizar("evento", v, id);
            toast("Cambios guardados.");
        }
        finish();
    }
}
