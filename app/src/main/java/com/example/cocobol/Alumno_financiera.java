package com.example.cocobol;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class Alumno_financiera extends ActividadBase implements AdapterView.OnItemClickListener {

    ListView lista;
    ArrayList<Long> ids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_alumno_financiera, R.id.menu_lateral_alumnos);

        lista = prepararLista(R.layout.cabecera_alumno_financiera);
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
        double pagado = 0;
        double porCobrar = 0;
        Cursor c = bd.consultar("SELECT id, concepto, periodo, estado, monto, fecha FROM movimiento "
                + "WHERE alumno_id = ? ORDER BY " + Util.ordenFecha("fecha") + " DESC, id DESC",
                String.valueOf(idAlumno));
        while (c.moveToNext()) {
            double monto = c.getDouble(4);
            if (c.getString(3).equals("Pagado")) {
                pagado += monto;
            } else {
                porCobrar += monto;
            }
            ids.add(c.getLong(0));
            datos.add(new String[]{c.getString(1), Util.nulo(c.getString(2)) + " · " + Util.guion(c.getString(5)),
                    c.getString(3), Util.dinero(monto)});
        }
        c.close();

        TextView saldo = cabecera.findViewById(R.id.txt_saldo);
        saldo.setText(Util.dinero(-porCobrar));
        saldo.setTextColor(ContextCompat.getColor(this, porCobrar > 0 ? R.color.rojo : R.color.negro));
        ((TextView) cabecera.findViewById(R.id.txt_saldo_detalle)).setText(
                "Pagado: " + Util.dinero(pagado) + " · Por cobrar: " + Util.dinero(porCobrar));

        mostrarEnLista(lista, R.layout.item_alumno_financiera,
                new int[]{R.id.txt_titulo, R.id.txt_subtitulo, R.id.txt_estado, R.id.txt_c1}, datos);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.boton_agregar) {
            abrir(FormMovimiento.class, "id_alumno", idAlumno);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> padre, View vista, int posicion, long id) {
        int i = posicion - lista.getHeaderViewsCount();
        if (i >= 0 && i < ids.size()) {
            Intent intent = new Intent(this, FormMovimiento.class);
            intent.putExtra("id", ids.get(i));
            startActivity(intent);
        }
    }
}
