package com.example.cocobol;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

/** Agregar / editar / eliminar el uniforme de un alumno. Recibe "id" para editar. */
public class FormUniforme extends ActividadBase implements AdapterView.OnItemSelectedListener {

    EditText playera, numero;
    Spinner alumno, tipo, talla;
    ArrayList<Long> idsAlumno = new ArrayList<>();
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.form_uniforme, R.id.menu_lateral_uniformes);

        playera = findViewById(R.id.f_playera);
        numero = findViewById(R.id.f_numero);
        alumno = findViewById(R.id.f_alumno);
        tipo = findViewById(R.id.f_tipo);
        talla = findViewById(R.id.f_talla);
        cargarOpciones(alumno, idsAlumno, null, "SELECT id, nombre || ' ' || paterno FROM alumno ORDER BY nombre, paterno");
        botonesFormulario();

        id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            modoEditar("Editar uniforme");
            cargar();
        } else {
            seleccionarTexto(talla, "M");
        }
        alumno.setOnItemSelectedListener(this);
        if (idsAlumno.isEmpty()) {
            error(null, "Primero registra un alumno.");
        }
    }

    void cargar() {
        Cursor c = bd.consultar("SELECT alumno_id, tipo, talla, nombre_playera, numero FROM uniforme WHERE id = ?",
                String.valueOf(id));
        if (!c.moveToFirst()) {
            c.close();
            toast("El uniforme ya no existe.");
            finish();
            return;
        }
        seleccionarId(alumno, idsAlumno, c.getLong(0));
        seleccionarTexto(tipo, c.getString(1));
        seleccionarTexto(talla, c.getString(2));
        playera.setText(c.getString(3));
        numero.setText(c.isNull(4) ? "" : String.valueOf(c.getInt(4)));
        c.close();
    }

    /** Al elegir alumno en un uniforme nuevo se proponen su nombre de playera y su número. */
    @Override
    public void onItemSelected(AdapterView<?> padre, View vista, int posicion, long idFila) {
        if (id != -1) return;
        Cursor c = bd.consultar("SELECT nombre_playera, numero FROM alumno WHERE id = ?",
                String.valueOf(idSeleccionado(alumno, idsAlumno)));
        if (c.moveToFirst()) {
            playera.setText(c.getString(0));
            numero.setText(c.isNull(1) ? "" : String.valueOf(c.getInt(1)));
        }
        c.close();
    }

    @Override
    public void onNothingSelected(AdapterView<?> padre) {
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
            confirmar("¿Eliminar este uniforme?", 1);
        }
    }

    @Override
    protected void alConfirmar(int accion) {
        bd.eliminar("uniforme", id);
        toast("Uniforme eliminado.");
        finish();
    }

    void guardar() {
        long idAl = idSeleccionado(alumno, idsAlumno);
        if (idAl == -1) {
            error(null, "Primero registra un alumno.");
            return;
        }
        String p = texto(playera);
        if (p.isEmpty() || p.length() > 12) {
            error(playera, "El nombre en playera debe tener de 1 a 12 caracteres.");
            return;
        }
        if (!Util.soloLetras(p)) {
            error(playera, "El nombre en playera solo puede tener letras.");
            return;
        }
        int n;
        try {
            n = Integer.parseInt(texto(numero));
        } catch (NumberFormatException e) {
            n = 0;
        }
        if (n < 1 || n > 99) {
            error(numero, "El número debe estar entre 1 y 99.");
            return;
        }
        if (bd.existe("SELECT id FROM uniforme WHERE alumno_id = ? AND tipo = ? AND id <> ?", String.valueOf(idAl),
                textoSpinner(tipo), String.valueOf(id))) {
            error(null, "Este alumno ya tiene un uniforme " + textoSpinner(tipo) + ". Edítalo desde la lista.");
            return;
        }

        ContentValues v = BaseDatos.cv("alumno_id", idAl, "tipo", textoSpinner(tipo), "talla", textoSpinner(talla),
                "nombre_playera", p, "numero", n);
        if (id == -1) {
            bd.insertar("uniforme", v);
            bd.registrarBitacora("Uniformes", bd.nombreAlumno(idAl), "Uniforme " + textoSpinner(tipo) + " talla "
                    + textoSpinner(talla) + " asignado");
            toast("Uniforme guardado.");
        } else {
            bd.actualizar("uniforme", v, id);
            toast("Cambios guardados.");
        }
        finish();
    }
}
