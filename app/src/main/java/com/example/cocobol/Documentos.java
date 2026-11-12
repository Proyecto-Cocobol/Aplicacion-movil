package com.example.cocobol;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class Documentos extends ActividadBase
        implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, TextWatcher {

    ListView lista;
    Spinner filAlumno, filOrden, filDocumento;
    EditText buscar;
    ArrayList<Long> ids = new ArrayList<>();
    ArrayList<Long> idsAlumno = new ArrayList<>();
    ArrayList<String> uris = new ArrayList<>();
    ArrayList<String> archivos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_documentos, R.id.menu_lateral_documentos);

        lista = prepararLista(R.layout.cabecera_documentos);
        filAlumno = cabecera.findViewById(R.id.fil_alumno);
        filOrden = cabecera.findViewById(R.id.fil_orden);
        filDocumento = cabecera.findViewById(R.id.fil_documento);
        buscar = cabecera.findViewById(R.id.buscar);
        cargarOpciones(filAlumno, idsAlumno, "Todos", "SELECT id, nombre || ' ' || paterno FROM alumno "
                + "ORDER BY nombre, paterno");

        filAlumno.setOnItemSelectedListener(this);
        filOrden.setOnItemSelectedListener(this);
        filDocumento.setOnItemSelectedListener(this);
        buscar.addTextChangedListener(this);
        cabecera.findViewById(R.id.boton_agregar).setOnClickListener(this);
        lista.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    void cargar() {
        long alumno = idSeleccionado(filAlumno, idsAlumno);
        String tipo = textoSpinner(filDocumento);
        String orden = filOrden.getSelectedItemPosition() == 1 ? "ASC" : "DESC";
        String texto = buscar.getText().toString().trim().toLowerCase(Locale.ROOT);

        ArrayList<String[]> datos = new ArrayList<>();
        ids.clear();
        uris.clear();
        archivos.clear();
        Cursor c = bd.consultar("SELECT d.id, d.archivo, a.nombre || ' ' || a.paterno, d.tipo, d.fecha, d.uri, "
                + "d.alumno_id FROM documento d JOIN alumno a ON a.id = d.alumno_id ORDER BY "
                + Util.ordenFecha("d.fecha") + " " + orden + ", d.id " + orden);
        while (c.moveToNext()) {
            if (alumno != -1 && c.getLong(6) != alumno) continue;
            if (!tipo.equals("Todos") && !tipo.equals(c.getString(3))) continue;
            String todo = (Util.nulo(c.getString(1)) + " " + c.getString(2) + " " + c.getString(3)).toLowerCase(Locale.ROOT);
            if (!texto.isEmpty() && !todo.contains(texto)) continue;

            String archivo = Util.nulo(c.getString(1));
            String extension = archivo.contains(".") ? archivo.substring(archivo.lastIndexOf('.') + 1) : "DOC";
            if (extension.length() > 4) extension = extension.substring(0, 4);
            ids.add(c.getLong(0));
            uris.add(Util.nulo(c.getString(5)));
            archivos.add(archivo);
            datos.add(new String[]{extension.toUpperCase(Locale.ROOT), archivo, c.getString(2), c.getString(3),
                    Util.guion(c.getString(4))});
        }
        c.close();

        ((TextView) cabecera.findViewById(R.id.txt_total)).setText(datos.size() + " documento(s)");
        mostrarEnLista(lista, R.layout.item_documentos,
                new int[]{R.id.txt_avatar, R.id.txt_titulo, R.id.txt_subtitulo, R.id.txt_c1, R.id.txt_c2}, datos, R.id.btn_ver_doc);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.boton_agregar) {
            abrir(FormDocumento.class, "id_alumno", idSeleccionado(filAlumno, idsAlumno));
        } else if (id == R.id.btn_ver_doc) {
            int i = (Integer) v.getTag();
            verArchivo(uris.get(i), archivos.get(i));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> padre, View vista, int posicion, long id) {
        int i = posicion - lista.getHeaderViewsCount();
        if (i >= 0 && i < ids.size()) {
            abrir(FormDocumento.class, "id", ids.get(i));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> padre, View vista, int posicion, long id) {
        cargar();
    }

    @Override
    public void onNothingSelected(AdapterView<?> padre) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int inicio, int cuenta, int despues) {
    }

    @Override
    public void onTextChanged(CharSequence s, int inicio, int antes, int cuenta) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        cargar();
    }
}
