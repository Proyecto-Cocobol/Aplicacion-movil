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

import java.util.ArrayList;
import java.util.Locale;

public class Bitacora extends ActividadBase
        implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, TextWatcher {

    ListView lista;
    Spinner filTema, filOrden;
    EditText buscar;
    ArrayList<Long> ids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_bitacora, R.id.menu_lateral_calendario);

        lista = prepararLista(R.layout.cabecera_bitacora);
        filTema = cabecera.findViewById(R.id.fil_tema);
        filOrden = cabecera.findViewById(R.id.fil_orden);
        buscar = cabecera.findViewById(R.id.buscar);

        filTema.setOnItemSelectedListener(this);
        filOrden.setOnItemSelectedListener(this);
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
        String tema = textoSpinner(filTema);
        String orden = filOrden.getSelectedItemPosition() == 1 ? "ASC" : "DESC";
        String texto = buscar.getText().toString().trim().toLowerCase(Locale.ROOT);

        ArrayList<String[]> datos = new ArrayList<>();
        ids.clear();
        Cursor c = bd.consultar("SELECT id, fecha, tema, alumno, detalle FROM bitacora ORDER BY "
                + Util.ordenFecha("fecha") + " " + orden + ", id " + orden);
        while (c.moveToNext()) {
            if (!tema.equals("Todos") && !tema.equals(c.getString(2))) continue;
            String todo = (c.getString(2) + " " + Util.nulo(c.getString(3)) + " " + Util.nulo(c.getString(4)))
                    .toLowerCase(Locale.ROOT);
            if (!texto.isEmpty() && !todo.contains(texto)) continue;

            String sub = Util.diaCorto(c.getString(1)) + " · " + c.getString(1);
            if (!Util.vacio(c.getString(3))) sub += "\nAlumno: " + c.getString(3);
            ids.add(c.getLong(0));
            datos.add(new String[]{c.getString(2).substring(0, 1), "Tema: " + c.getString(2), sub, "",
                    Util.guion(c.getString(4))});
        }
        c.close();

        mostrarEnLista(lista, R.layout.item_bitacora,
                new int[]{R.id.txt_avatar, R.id.txt_titulo, R.id.txt_subtitulo, R.id.txt_estado, R.id.txt_c1}, datos);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.boton_agregar) {
            abrir(FormBitacora.class);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> padre, View vista, int posicion, long id) {
        int i = posicion - lista.getHeaderViewsCount();
        if (i >= 0 && i < ids.size()) {
            abrir(FormBitacora.class, "id", ids.get(i));
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
