package com.example.cocobol;

import android.content.Intent;
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

public class Profesores extends ActividadBase
        implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, TextWatcher {

    ListView lista;
    Spinner filEdad, filOrden, filCategoria;
    EditText buscar;
    ArrayList<Long> ids = new ArrayList<>();
    ArrayList<Long> idsCategoria = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_profesores, R.id.menu_lateral_personal);

        lista = prepararLista(R.layout.cabecera_profesores);
        filEdad = cabecera.findViewById(R.id.fil_edad);
        filOrden = cabecera.findViewById(R.id.fil_orden);
        filCategoria = cabecera.findViewById(R.id.fil_categoria);
        buscar = cabecera.findViewById(R.id.buscar);

        cargarOpciones(filCategoria, idsCategoria, "Todas", "SELECT id, categoria FROM equipo ORDER BY categoria");

        filEdad.setOnItemSelectedListener(this);
        filOrden.setOnItemSelectedListener(this);
        filCategoria.setOnItemSelectedListener(this);
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
        String orden = filOrden.getSelectedItemPosition() == 1 ? "p.id DESC" : "p.nombre, p.paterno";
        String texto = buscar.getText().toString().trim().toLowerCase(Locale.ROOT);
        String rango = textoSpinner(filEdad);
        long categoria = idSeleccionado(filCategoria, idsCategoria);

        ArrayList<String[]> datos = new ArrayList<>();
        ids.clear();
        Cursor c = bd.consultar("SELECT p.id, p.nombre, p.paterno, p.materno, p.fecha_nac, p.puesto, "
                + "(SELECT GROUP_CONCAT(categoria, ', ') FROM equipo e WHERE e.profesor_id = p.id), "
                + "(SELECT COUNT(*) FROM equipo e WHERE e.profesor_id = p.id AND e.id = ?) "
                + "FROM personal p WHERE p.tipo = 'PROFESOR' ORDER BY " + orden, String.valueOf(categoria));
        while (c.moveToNext()) {
            String nombre = Util.nombreCompleto(c.getString(1), c.getString(2), c.getString(3));
            int edad = Util.edad(c.getString(4));
            if (!texto.isEmpty() && !nombre.toLowerCase(Locale.ROOT).contains(texto)) continue;
            if (!Util.enRango(edad, rango)) continue;
            if (categoria != -1 && c.getInt(7) == 0) continue;

            ids.add(c.getLong(0));
            datos.add(new String[]{Util.iniciales(c.getString(1), c.getString(2)), nombre,
                    Util.guion(c.getString(5)), edad < 0 ? "—" : String.valueOf(edad),
                    Util.guion(c.getString(4)), Util.guion(c.getString(6))});
        }
        c.close();

        ((TextView) cabecera.findViewById(R.id.txt_total)).setText(datos.size() + " profesor(es)");
        mostrarEnLista(lista, R.layout.item_profesores,
                new int[]{R.id.txt_avatar, R.id.txt_titulo, R.id.txt_subtitulo, R.id.txt_c1, R.id.txt_c2,
                        R.id.txt_c3}, datos, R.id.btn_planeacion);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.boton_agregar) {
            Intent intent = new Intent(this, FormPersonal.class);
            intent.putExtra("tipo", "PROFESOR");
            startActivity(intent);
        } else if (id == R.id.btn_planeacion) {
            abrir(Planeacion.class, "id_profesor", ids.get((Integer) v.getTag()));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> padre, View vista, int posicion, long id) {
        int i = posicion - lista.getHeaderViewsCount();
        if (i >= 0 && i < ids.size()) {
            abrir(FormPersonal.class, "id", ids.get(i));
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
