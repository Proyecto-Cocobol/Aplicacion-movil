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

public class Administrativo extends ActividadBase
        implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, TextWatcher {

    ListView lista;
    Spinner filEdad, filOrden, filRol;
    EditText buscar;
    ArrayList<Long> ids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_administrativo, R.id.menu_lateral_personal);

        lista = prepararLista(R.layout.cabecera_administrativo);
        filEdad = cabecera.findViewById(R.id.fil_edad);
        filOrden = cabecera.findViewById(R.id.fil_orden);
        filRol = cabecera.findViewById(R.id.fil_rol);
        buscar = cabecera.findViewById(R.id.buscar);

        filEdad.setOnItemSelectedListener(this);
        filOrden.setOnItemSelectedListener(this);
        filRol.setOnItemSelectedListener(this);
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
        String orden = filOrden.getSelectedItemPosition() == 1 ? "id DESC" : "nombre, paterno";
        String texto = buscar.getText().toString().trim().toLowerCase(Locale.ROOT);
        String rango = textoSpinner(filEdad);
        String rol = textoSpinner(filRol);

        ArrayList<String[]> datos = new ArrayList<>();
        ids.clear();
        Cursor c = bd.consultar("SELECT id, nombre, paterno, materno, fecha_nac, puesto, telefono FROM personal "
                + "WHERE tipo = 'ADMINISTRATIVO' ORDER BY " + orden);
        while (c.moveToNext()) {
            String nombre = Util.nombreCompleto(c.getString(1), c.getString(2), c.getString(3));
            int edad = Util.edad(c.getString(4));
            if (!texto.isEmpty() && !nombre.toLowerCase(Locale.ROOT).contains(texto)) continue;
            if (!Util.enRango(edad, rango)) continue;
            if (!rol.equals("Todos") && !rol.equals(c.getString(5))) continue;

            ids.add(c.getLong(0));
            datos.add(new String[]{Util.iniciales(c.getString(1), c.getString(2)), nombre,
                    "Personal administrativo", edad < 0 ? "—" : String.valueOf(edad), Util.guion(c.getString(4)),
                    Util.guion(c.getString(6)), Util.guion(c.getString(5))});
        }
        c.close();

        ((TextView) cabecera.findViewById(R.id.txt_total)).setText(datos.size() + " persona(s)");
        mostrarEnLista(lista, R.layout.item_administrativo,
                new int[]{R.id.txt_avatar, R.id.txt_titulo, R.id.txt_subtitulo, R.id.txt_c1, R.id.txt_c2,
                        R.id.txt_c3, R.id.txt_c4}, datos);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.boton_agregar) {
            Intent intent = new Intent(this, FormPersonal.class);
            intent.putExtra("tipo", "ADMINISTRATIVO");
            startActivity(intent);
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
