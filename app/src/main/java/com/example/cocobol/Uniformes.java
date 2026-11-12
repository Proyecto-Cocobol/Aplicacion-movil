package com.example.cocobol;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

/** Pedidos a fábrica (tarjetas de arriba) y uniformes asignados a cada alumno (lista). */
public class Uniformes extends ActividadBase implements AdapterView.OnItemClickListener {

    ListView lista;
    LinearLayout contenedorPedidos;
    ArrayList<Long> ids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_uniformes, R.id.menu_lateral_uniformes);

        lista = prepararLista(R.layout.cabecera_uniformes);
        contenedorPedidos = cabecera.findViewById(R.id.contenedor_pedidos);
        cabecera.findViewById(R.id.boton_agregar).setOnClickListener(this);
        cabecera.findViewById(R.id.boton_agregar_pedido).setOnClickListener(this);
        lista.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPedidos();
        cargar();
    }

    void cargarPedidos() {
        contenedorPedidos.removeAllViews();
        Cursor c = bd.consultar("SELECT id, tipo, cantidad, fecha, fabrica, estado FROM pedido ORDER BY "
                + Util.ordenFecha("fecha") + " DESC");
        while (c.moveToNext()) {
            View tarjeta = getLayoutInflater().inflate(R.layout.item_pedido, contenedorPedidos, false);
            FilaAdapter.llenar(this, tarjeta,
                    new int[]{R.id.txt_titulo, R.id.txt_subtitulo, R.id.txt_estado, R.id.txt_c1, R.id.txt_c2, R.id.txt_c3},
                    new String[]{"Pedido " + c.getString(1), "Toca para editar", c.getString(5),
                            String.valueOf(c.getInt(2)), Util.guion(c.getString(3)), Util.guion(c.getString(4))});
            tarjeta.setTag(c.getLong(0));
            tarjeta.setOnClickListener(this);
            contenedorPedidos.addView(tarjeta);
        }
        c.close();
        if (contenedorPedidos.getChildCount() == 0) {
            View vacio = getLayoutInflater().inflate(R.layout.item_pedido, contenedorPedidos, false);
            FilaAdapter.llenar(this, vacio, new int[]{R.id.txt_titulo, R.id.txt_subtitulo, R.id.txt_estado},
                    new String[]{"Sin pedidos", "Agrega uno con \"+ Pedido\"", ""});
            contenedorPedidos.addView(vacio);
        }
    }

    void cargar() {
        ArrayList<String[]> datos = new ArrayList<>();
        ids.clear();
        Cursor c = bd.consultar("SELECT u.id, u.tipo, u.talla, u.nombre_playera, u.numero, a.nombre || ' ' || a.paterno "
                + "FROM uniforme u JOIN alumno a ON a.id = u.alumno_id ORDER BY a.nombre, u.tipo");
        while (c.moveToNext()) {
            ids.add(c.getLong(0));
            datos.add(new String[]{c.getString(1), c.getString(2), Util.guion(c.getString(3)),
                    c.isNull(4) ? "—" : String.valueOf(c.getInt(4)), c.getString(5)});
        }
        c.close();
        mostrarEnLista(lista, R.layout.item_uniformes,
                new int[]{R.id.txt_titulo, R.id.txt_c1, R.id.txt_c2, R.id.txt_c3, R.id.txt_c4}, datos);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.boton_agregar) {
            abrir(FormUniforme.class);
        } else if (id == R.id.boton_agregar_pedido) {
            abrir(FormPedido.class);
        } else if (id == R.id.tarjeta_pedido && v.getTag() != null) {
            abrir(FormPedido.class, "id", (Long) v.getTag());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> padre, View vista, int posicion, long id) {
        int i = posicion - lista.getHeaderViewsCount();
        if (i >= 0 && i < ids.size()) {
            abrir(FormUniforme.class, "id", ids.get(i));
        }
    }
}
