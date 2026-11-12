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

/** Resumen financiero por alumno: lo pagado por concepto y el saldo pendiente. */
public class Finanzas extends ActividadBase
        implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, TextWatcher {

    ListView lista;
    Spinner filAdeudos, filCategoria;
    EditText buscar;
    ArrayList<Long> ids = new ArrayList<>();
    ArrayList<Long> idsCategoria = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_finanzas, R.id.menu_lateral_finanzas);

        lista = prepararLista(R.layout.cabecera_finanzas);
        filAdeudos = cabecera.findViewById(R.id.fil_adeudos);
        filCategoria = cabecera.findViewById(R.id.fil_categoria);
        buscar = cabecera.findViewById(R.id.buscar);
        cargarOpciones(filCategoria, idsCategoria, "Todas", "SELECT id, categoria FROM equipo ORDER BY categoria");

        filAdeudos.setOnItemSelectedListener(this);
        filCategoria.setOnItemSelectedListener(this);
        buscar.addTextChangedListener(this);
        lista.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    void cargar() {
        String texto = buscar.getText().toString().trim().toLowerCase(Locale.ROOT);
        int adeudos = filAdeudos.getSelectedItemPosition();
        long categoria = idSeleccionado(filCategoria, idsCategoria);

        ArrayList<String[]> datos = new ArrayList<>();
        ids.clear();
        double totalCobrado = 0;
        double totalAdeudo = 0;
        Cursor c = bd.consultar("SELECT a.id, a.nombre, a.paterno, a.materno, a.fecha_nac, a.equipo_id, "
                + "SUM(CASE WHEN m.estado = 'Pagado' AND m.concepto = 'Mensualidad' THEN m.monto ELSE 0 END), "
                + "SUM(CASE WHEN m.estado = 'Pagado' AND m.concepto = 'Inscripción' THEN m.monto ELSE 0 END), "
                + "SUM(CASE WHEN m.estado = 'Pagado' AND m.concepto = 'Torneo' THEN m.monto ELSE 0 END), "
                + "SUM(CASE WHEN m.estado = 'Pagado' AND m.concepto = 'Arbitraje' THEN m.monto ELSE 0 END), "
                + "SUM(CASE WHEN m.estado = 'Pagado' AND m.concepto = 'Uniforme' THEN m.monto ELSE 0 END), "
                + "SUM(CASE WHEN m.estado <> 'Pagado' THEN m.monto ELSE 0 END) "
                + "FROM alumno a LEFT JOIN movimiento m ON m.alumno_id = a.id "
                + "GROUP BY a.id ORDER BY a.nombre, a.paterno");
        while (c.moveToNext()) {
            String nombre = Util.nombreCompleto(c.getString(1), c.getString(2), c.getString(3));
            long equipo = c.isNull(5) ? -1 : c.getLong(5);
            double deuda = c.getDouble(11);
            if (!texto.isEmpty() && !nombre.toLowerCase(Locale.ROOT).contains(texto)) continue;
            if (adeudos == 1 && deuda <= 0) continue;
            if (adeudos == 2 && deuda > 0) continue;
            if (categoria != -1 && equipo != categoria) continue;

            double pagado = 0;
            for (int i = 6; i <= 10; i++) pagado += c.getDouble(i);
            totalCobrado += pagado;
            totalAdeudo += deuda;

            ids.add(c.getLong(0));
            datos.add(new String[]{Util.iniciales(c.getString(1), c.getString(2)), nombre,
                    "Nac.: " + Util.guion(c.getString(4)),
                    Util.dinero(c.getDouble(6)), Util.dinero(c.getDouble(7)), Util.dinero(c.getDouble(8)),
                    Util.dinero(c.getDouble(9)), Util.dinero(c.getDouble(10)), Util.dinero(-deuda)});
        }
        c.close();

        ponerTextoCabecera(R.id.txt_total_cobrado, Util.dinero(totalCobrado));
        ponerTextoCabecera(R.id.txt_total_adeudo, Util.dinero(totalAdeudo));

        mostrarEnLista(lista, R.layout.item_finanzas,
                new int[]{R.id.txt_avatar, R.id.txt_titulo, R.id.txt_subtitulo, R.id.txt_c1, R.id.txt_c2,
                        R.id.txt_c3, R.id.txt_c4, R.id.txt_c5, R.id.txt_c6}, datos, R.id.btn_pago, R.id.btn_ver);
    }

    private void ponerTextoCabecera(int idVista, String texto) {
        ((android.widget.TextView) cabecera.findViewById(idVista)).setText(texto);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.btn_pago) {
            abrir(FormMovimiento.class, "id_alumno", ids.get((Integer) v.getTag()));
        } else if (id == R.id.btn_ver) {
            abrir(Alumno_financiera.class, "id_alumno", ids.get((Integer) v.getTag()));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> padre, View vista, int posicion, long id) {
        int i = posicion - lista.getHeaderViewsCount();
        if (i >= 0 && i < ids.size()) {
            abrir(Alumno_financiera.class, "id_alumno", ids.get(i));
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
