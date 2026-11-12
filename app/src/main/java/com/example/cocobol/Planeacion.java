package com.example.cocobol;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

/** Planeación de entrenamientos. Puede recibir "id_profesor" para mostrar solo la de ese profesor. */
public class Planeacion extends ActividadBase implements AdapterView.OnItemClickListener,
        AdapterView.OnItemSelectedListener {

    ListView lista;
    Spinner filProfesor;
    ArrayList<Long> ids = new ArrayList<>();
    ArrayList<Long> idsProfesor = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_planeacion, R.id.menu_lateral_personal);

        lista = prepararLista(R.layout.cabecera_planeacion);
        filProfesor = cabecera.findViewById(R.id.fil_profesor);
        cargarOpciones(filProfesor, idsProfesor, "Todos", "SELECT id, nombre || ' ' || paterno FROM personal "
                + "WHERE tipo = 'PROFESOR' ORDER BY nombre");
        seleccionarId(filProfesor, idsProfesor, getIntent().getLongExtra("id_profesor", -1));

        filProfesor.setOnItemSelectedListener(this);
        cabecera.findViewById(R.id.boton_agregar).setOnClickListener(this);
        lista.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    void cargar() {
        long profesor = idSeleccionado(filProfesor, idsProfesor);
        ((TextView) cabecera.findViewById(R.id.txt_subtitulo_cab)).setText(profesor == -1
                ? "Planeación de entrenamiento de todos los profesores."
                : "Planeación de entrenamiento de " + textoSpinner(filProfesor) + ".");

        ArrayList<String[]> datos = new ArrayList<>();
        ids.clear();
        String sql = "SELECT p.id, p.titulo, p.fecha, p.hora, e.categoria, pe.nombre || ' ' || pe.paterno, "
                + "p.ejercicios FROM planeacion p JOIN personal pe ON pe.id = p.personal_id "
                + "LEFT JOIN equipo e ON e.id = p.equipo_id ";
        Cursor c;
        if (profesor == -1) {
            c = bd.consultar(sql + "ORDER BY " + Util.ordenFecha("p.fecha") + ", p.hora");
        } else {
            c = bd.consultar(sql + "WHERE p.personal_id = ? ORDER BY " + Util.ordenFecha("p.fecha") + ", p.hora",
                    String.valueOf(profesor));
        }
        while (c.moveToNext()) {
            String fecha = c.getString(2);
            String sub = Util.diaCorto(fecha) + " · " + fecha;
            if (!Util.vacio(c.getString(3))) sub += " · " + c.getString(3);
            sub += "\n" + (c.isNull(4) ? "Sin categoría" : "Categoría " + c.getString(4)) + " · " + c.getString(5);

            StringBuilder ejercicios = new StringBuilder();
            int n = 1;
            for (String linea : Util.nulo(c.getString(6)).split("\n")) {
                if (!linea.trim().isEmpty()) {
                    if (ejercicios.length() > 0) ejercicios.append("\n");
                    ejercicios.append(n).append(". ").append(linea.trim());
                    n++;
                }
            }
            ids.add(c.getLong(0));
            datos.add(new String[]{c.getString(1), sub,
                    Util.comparar(fecha, Util.hoy()) >= 0 ? "Próximo" : "Realizado", ejercicios.toString()});
        }
        c.close();

        ((TextView) cabecera.findViewById(R.id.txt_total)).setText(datos.size() + " entrenamiento(s)");
        mostrarEnLista(lista, R.layout.item_planeacion,
                new int[]{R.id.txt_titulo, R.id.txt_subtitulo, R.id.txt_estado, R.id.txt_c1}, datos);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.boton_agregar) {
            Intent intent = new Intent(this, FormPlaneacion.class);
            intent.putExtra("id_profesor", idSeleccionado(filProfesor, idsProfesor));
            startActivity(intent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> padre, View vista, int posicion, long id) {
        int i = posicion - lista.getHeaderViewsCount();
        if (i >= 0 && i < ids.size()) {
            abrir(FormPlaneacion.class, "id", ids.get(i));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> padre, View vista, int posicion, long id) {
        cargar();
    }

    @Override
    public void onNothingSelected(AdapterView<?> padre) {
    }
}
