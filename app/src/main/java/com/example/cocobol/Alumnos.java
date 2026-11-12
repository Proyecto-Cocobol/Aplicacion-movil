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

public class Alumnos extends ActividadBase
        implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, TextWatcher {

    ListView lista;
    Spinner filEdad, filOrden, filCategoria, filAdeudos;
    EditText buscar;
    ArrayList<Long> ids = new ArrayList<>();
    ArrayList<Long> idsCategoria = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_alumnos, R.id.menu_lateral_alumnos);

        lista = prepararLista(R.layout.cabecera_alumnos);
        filEdad = cabecera.findViewById(R.id.fil_edad);
        filOrden = cabecera.findViewById(R.id.fil_orden);
        filCategoria = cabecera.findViewById(R.id.fil_categoria);
        filAdeudos = cabecera.findViewById(R.id.fil_adeudos);
        buscar = cabecera.findViewById(R.id.buscar);

        cargarOpciones(filCategoria, idsCategoria, "Todas", "SELECT id, categoria FROM equipo ORDER BY categoria");

        filEdad.setOnItemSelectedListener(this);
        filOrden.setOnItemSelectedListener(this);
        filCategoria.setOnItemSelectedListener(this);
        filAdeudos.setOnItemSelectedListener(this);
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
        String orden = filOrden.getSelectedItemPosition() == 1 ? "a.id DESC" : "a.nombre, a.paterno";
        String texto = buscar.getText().toString().trim().toLowerCase(Locale.ROOT);
        String rango = textoSpinner(filEdad);
        long categoria = idSeleccionado(filCategoria, idsCategoria);
        int adeudos = filAdeudos.getSelectedItemPosition(); // 0 todos, 1 con adeudo, 2 sin adeudo

        ArrayList<String[]> datos = new ArrayList<>();
        ids.clear();
        Cursor c = bd.consultar("SELECT a.id, a.nombre, a.paterno, a.materno, a.fecha_nac, a.numero, "
                + "a.fecha_ingreso, a.equipo_id, e.categoria, "
                + "(SELECT SUM(monto) FROM movimiento m WHERE m.alumno_id = a.id AND m.estado <> 'Pagado') "
                + "FROM alumno a LEFT JOIN equipo e ON e.id = a.equipo_id ORDER BY " + orden);
        while (c.moveToNext()) {
            String nombre = Util.nombreCompleto(c.getString(1), c.getString(2), c.getString(3));
            int edad = Util.edad(c.getString(4));
            long equipo = c.isNull(7) ? -1 : c.getLong(7);
            double deuda = c.isNull(9) ? 0 : c.getDouble(9);

            if (!texto.isEmpty() && !nombre.toLowerCase(Locale.ROOT).contains(texto)) continue;
            if (!Util.enRango(edad, rango)) continue;
            if (categoria != -1 && equipo != categoria) continue;
            if (adeudos == 1 && deuda <= 0) continue;
            if (adeudos == 2 && deuda > 0) continue;

            ids.add(c.getLong(0));
            datos.add(new String[]{
                    Util.iniciales(c.getString(1), c.getString(2)),
                    nombre,
                    c.isNull(8) ? "Sin categoría" : "Categoría " + c.getString(8),
                    deuda > 0 ? Util.dinero(-deuda) : "Sin adeudo",
                    edad < 0 ? "—" : String.valueOf(edad),
                    c.getString(4),
                    c.isNull(5) ? "—" : "#" + c.getInt(5),
                    Util.guion(c.getString(6))
            });
        }
        c.close();

        ((TextView) cabecera.findViewById(R.id.txt_total)).setText(datos.size() + " alumno(s)");
        mostrarEnLista(lista, R.layout.item_alumnos,
                new int[]{R.id.txt_avatar, R.id.txt_titulo, R.id.txt_subtitulo, R.id.txt_estado,
                        R.id.txt_c1, R.id.txt_c2, R.id.txt_c3, R.id.txt_c4}, datos);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.boton_agregar) {
            abrir(FormAlumno.class);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> padre, View vista, int posicion, long id) {
        int i = posicion - lista.getHeaderViewsCount();
        if (i >= 0 && i < ids.size()) {
            abrir(Alumno_personal.class, "id_alumno", ids.get(i));
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
