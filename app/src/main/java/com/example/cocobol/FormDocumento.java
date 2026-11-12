package com.example.cocobol;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Agregar / editar / eliminar documento de un alumno.
 * El archivo se elige del teléfono (PDF, imagen, etc.); se guarda la dirección (uri) para poder abrirlo después.
 * Recibe "id" para editar, o "id_alumno" y "tipo" para uno nuevo.
 */
public class FormDocumento extends ActividadBase {

    static final int ELEGIR_ARCHIVO = 10;

    EditText fecha;
    TextView archivo;
    Spinner alumno, tipo;
    ArrayList<Long> idsAlumno = new ArrayList<>();
    long id;
    String uri = "";
    String nombreArchivo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.form_documento, R.id.menu_lateral_documentos);

        fecha = findViewById(R.id.f_fecha);
        archivo = findViewById(R.id.f_archivo);
        alumno = findViewById(R.id.f_alumno);
        tipo = findViewById(R.id.f_tipo);
        cargarOpciones(alumno, idsAlumno, null, "SELECT id, nombre || ' ' || paterno FROM alumno ORDER BY nombre, paterno");

        fecha.setOnClickListener(this);
        findViewById(R.id.f_elegir).setOnClickListener(this);
        findViewById(R.id.f_ver).setOnClickListener(this);
        botonesFormulario();

        id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            modoEditar("Editar documento");
            cargar();
        } else {
            seleccionarId(alumno, idsAlumno, getIntent().getLongExtra("id_alumno", -1));
            String t = getIntent().getStringExtra("tipo");
            if (!Util.vacio(t)) seleccionarTexto(tipo, t);
            fecha.setText(Util.hoy());
        }
        mostrarArchivo();
        if (idsAlumno.isEmpty()) {
            error(null, "Primero registra un alumno.");
        }
    }

    void cargar() {
        Cursor c = bd.consultar("SELECT alumno_id, tipo, archivo, uri, fecha FROM documento WHERE id = ?",
                String.valueOf(id));
        if (!c.moveToFirst()) {
            c.close();
            toast("El documento ya no existe.");
            finish();
            return;
        }
        seleccionarId(alumno, idsAlumno, c.getLong(0));
        seleccionarTexto(tipo, c.getString(1));
        nombreArchivo = Util.nulo(c.getString(2));
        uri = Util.nulo(c.getString(3));
        fecha.setText(c.getString(4));
        c.close();
    }

    void mostrarArchivo() {
        archivo.setText(nombreArchivo.isEmpty() ? "Ningún archivo seleccionado" : nombreArchivo);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int vid = v.getId();
        if (vid == R.id.f_fecha) {
            elegirFecha(fecha);
        } else if (vid == R.id.f_elegir) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/pdf", "image/*"});
            startActivityForResult(intent, ELEGIR_ARCHIVO);
        } else if (vid == R.id.f_ver) {
            if (nombreArchivo.isEmpty()) {
                toast("Primero elige un archivo.");
            } else {
                verArchivo(uri, nombreArchivo);
            }
        } else if (vid == R.id.form_guardar) {
            guardar();
        } else if (vid == R.id.form_cancelar) {
            finish();
        } else if (vid == R.id.form_eliminar) {
            confirmar("¿Eliminar este documento?", 1);
        }
    }

    @Override
    protected void onActivityResult(int peticion, int resultado, Intent datos) {
        super.onActivityResult(peticion, resultado, datos);
        if (peticion != ELEGIR_ARCHIVO || resultado != RESULT_OK || datos == null || datos.getData() == null) {
            return;
        }
        Uri elegido = datos.getData();
        try {
            // Para poder abrir el archivo aunque se cierre la app
            getContentResolver().takePersistableUriPermission(elegido, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException e) {
            // Algunos proveedores no permiten guardar el permiso; se podrá abrir mientras siga disponible
        }
        uri = elegido.toString();
        nombreArchivo = "archivo";
        Cursor c = getContentResolver().query(elegido, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
        if (c != null) {
            if (c.moveToFirst() && !c.isNull(0)) {
                nombreArchivo = c.getString(0);
            }
            c.close();
        }
        mostrarArchivo();
    }

    @Override
    protected void alConfirmar(int accion) {
        bd.eliminar("documento", id);
        toast("Documento eliminado.");
        finish();
    }

    void guardar() {
        long idAl = idSeleccionado(alumno, idsAlumno);
        String t = textoSpinner(tipo);
        if (idAl == -1) {
            error(null, "Primero registra un alumno.");
            return;
        }
        if (!t.equals("Otro") && bd.existe("SELECT id FROM documento WHERE alumno_id = ? AND tipo = ? AND id <> ?",
                String.valueOf(idAl), t, String.valueOf(id))) {
            error(null, "Este alumno ya tiene " + t + ". Ábrelo desde la lista para reemplazar el archivo.");
            return;
        }
        if (!Util.fechaValida(texto(fecha))) {
            error(fecha, "Elige la fecha.");
            return;
        }
        if (Util.esFutura(texto(fecha))) {
            error(fecha, "La fecha no puede ser futura.");
            return;
        }
        if (nombreArchivo.isEmpty()) {
            error(null, "Elige el archivo del documento.");
            return;
        }

        ContentValues v = BaseDatos.cv("alumno_id", idAl, "tipo", t, "archivo", nombreArchivo, "uri", uri,
                "fecha", texto(fecha));
        if (id == -1) {
            bd.insertar("documento", v);
            bd.registrarBitacora("Documentos", bd.nombreAlumno(idAl), "Documento cargado: " + t + " (" + nombreArchivo + ")");
            toast("Documento guardado.");
        } else {
            bd.actualizar("documento", v, id);
            toast("Cambios guardados.");
        }
        finish();
    }
}
