package com.example.cocobol;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class Equipos extends ActividadBase implements AdapterView.OnItemClickListener {

    ListView lista;
    ArrayList<Long> ids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_equipos, R.id.menu_lateral_equipos);

        lista = prepararLista(R.layout.cabecera_equipos);
        cabecera.findViewById(R.id.boton_agregar).setOnClickListener(this);
        lista.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    void cargar() {
        ArrayList<String[]> datos = new ArrayList<>();
        ids.clear();
        Cursor c = bd.consultar("SELECT e.id, e.categoria, e.edad, p.nombre || ' ' || p.paterno, e.torneo, "
                + "(SELECT COUNT(*) FROM alumno a WHERE a.equipo_id = e.id) "
                + "FROM equipo e LEFT JOIN personal p ON p.id = e.profesor_id ORDER BY e.categoria");
        while (c.moveToNext()) {
            ids.add(c.getLong(0));
            datos.add(new String[]{"Categoría " + c.getString(1) + " (" + c.getInt(5) + " jugadores)",
                    Util.guion(c.getString(2)), c.isNull(3) ? "Sin asignar" : c.getString(3),
                    Util.guion(c.getString(4))});
        }
        c.close();

        mostrarEnLista(lista, R.layout.item_equipos,
                new int[]{R.id.txt_titulo, R.id.txt_c1, R.id.txt_c2, R.id.txt_c3}, datos, R.id.btn_administrar, R.id.btn_evaluaciones);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.boton_agregar) {
            abrir(FormEquipo.class);
        } else if (id == R.id.btn_administrar) {
            abrir(Categorias.class, "id_equipo", ids.get((Integer) v.getTag()));
        } else if (id == R.id.btn_evaluaciones) {
            abrir(Evaluaciones.class, "id_equipo", ids.get((Integer) v.getTag()));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> padre, View vista, int posicion, long id) {
        int i = posicion - lista.getHeaderViewsCount();
        if (i >= 0 && i < ids.size()) {
            abrir(FormEquipo.class, "id", ids.get(i));
        }
    }
}
