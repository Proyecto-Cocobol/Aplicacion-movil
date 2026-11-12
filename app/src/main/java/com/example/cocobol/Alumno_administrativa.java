package com.example.cocobol;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class Alumno_administrativa extends ActividadBase implements AdapterView.OnItemClickListener {

    /** Documentos que todo alumno debe entregar. */
    static final String[] OBLIGATORIOS = {"CURP", "Acta de nacimiento", "Credencial"};

    ListView lista;
    ArrayList<Long> ids = new ArrayList<>();       // -1 cuando el documento falta
    ArrayList<String> tipos = new ArrayList<>();
    ArrayList<String> uris = new ArrayList<>();
    ArrayList<String> archivos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_alumno_administrativa, R.id.menu_lateral_alumnos);

        lista = prepararLista(R.layout.cabecera_alumno_administrativa);
        cabecera.findViewById(R.id.boton_agregar).setOnClickListener(this);
        lista.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!configurarTabsAlumno(cabecera)) {
            toast("El alumno ya no existe.");
            finish();
            return;
        }
        cargar();
    }

    void cargar() {
        ArrayList<String[]> datos = new ArrayList<>();
        ids.clear();
        tipos.clear();
        uris.clear();
        archivos.clear();

        // Primero los obligatorios: "Listo" si ya se cargó, "No listo" si falta
        for (String tipo : OBLIGATORIOS) {
            Cursor c = bd.consultar("SELECT id, archivo, uri FROM documento WHERE alumno_id = ? AND tipo = ? "
                    + "ORDER BY id DESC LIMIT 1", String.valueOf(idAlumno), tipo);
            if (c.moveToFirst()) {
                agregar(datos, c.getLong(0), tipo, c.getString(1), c.getString(2));
            } else {
                ids.add(-1L);
                tipos.add(tipo);
                uris.add("");
                archivos.add("");
                datos.add(new String[]{tipo, "No listo", "Cargar"});
            }
            c.close();
        }

        // Después cualquier otro documento del alumno
        Cursor c = bd.consultar("SELECT id, tipo, archivo, uri FROM documento WHERE alumno_id = ? "
                + "AND tipo NOT IN ('CURP', 'Acta de nacimiento', 'Credencial') ORDER BY id DESC",
                String.valueOf(idAlumno));
        while (c.moveToNext()) {
            agregar(datos, c.getLong(0), c.getString(1), c.getString(2), c.getString(3));
        }
        c.close();

        mostrarEnLista(lista, R.layout.item_alumno_administrativa,
                new int[]{R.id.txt_titulo, R.id.txt_estado, R.id.btn_txt_accion}, datos, R.id.btn_txt_accion);
    }

    private void agregar(ArrayList<String[]> datos, long id, String tipo, String archivo, String uri) {
        ids.add(id);
        tipos.add(tipo);
        uris.add(Util.nulo(uri));
        archivos.add(Util.nulo(archivo));
        datos.add(new String[]{tipo, "Listo", "Ver"});
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.boton_agregar) {
            abrir(FormDocumento.class, "id_alumno", idAlumno);
        } else if (id == R.id.btn_txt_accion) {
            int i = (Integer) v.getTag();
            if (ids.get(i) == -1) {
                Intent intent = new Intent(this, FormDocumento.class);
                intent.putExtra("id_alumno", idAlumno);
                intent.putExtra("tipo", tipos.get(i));
                startActivity(intent);
            } else {
                verArchivo(uris.get(i), archivos.get(i));
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> padre, View vista, int posicion, long id) {
        int i = posicion - lista.getHeaderViewsCount();
        if (i >= 0 && i < ids.size() && ids.get(i) != -1) {
            abrir(FormDocumento.class, "id", ids.get(i));
        }
    }
}
