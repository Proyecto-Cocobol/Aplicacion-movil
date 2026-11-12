package com.example.cocobol;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.Calendar;

/** Dashboard del administrador: próximas actividades y actividad reciente, tomadas de la base local. */
public class Inicio_principal_admin extends ActividadBase {

    LinearLayout contenedorEventos, contenedorBitacora;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_inicio_principal_admin, R.id.menu_lateral_inicio);

        contenedorEventos = findViewById(R.id.contenedor_eventos);
        contenedorBitacora = findViewById(R.id.contenedor_bitacora);
        findViewById(R.id.boton_contacto).setOnClickListener(this);
        findViewById(R.id.boton_ver_calendario).setOnClickListener(this);
        findViewById(R.id.boton_ver_bitacora).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Calendar hoy = Calendar.getInstance();
        ponerTexto(R.id.txt_bienvenida, "Bienvenido de nuevo, " + Sesion.nombre(this));
        ponerTexto(R.id.txt_fecha_hoy, Util.fechaLarga(hoy));
        ponerTexto(R.id.txt_mes_actual, Util.mesAnio(hoy));
        ponerTexto(R.id.txt_num_alumnos, String.valueOf((int) bd.numero("SELECT COUNT(*) FROM alumno")));
        ponerTexto(R.id.txt_por_cobrar, Util.dinero(bd.numero("SELECT SUM(monto) FROM movimiento WHERE estado <> 'Pagado'")));
        cargarEventos();
        cargarBitacora();
    }

    /** Las siguientes 3 actividades a partir de hoy. */
    void cargarEventos() {
        contenedorEventos.removeAllViews();
        String hoy = Util.clave(Util.hoy());
        Cursor c = bd.consultar("SELECT ev.id, ev.titulo, ev.tipo, ev.fecha, ev.hora, e.categoria FROM evento ev "
                + "LEFT JOIN equipo e ON e.id = ev.equipo_id WHERE " + Util.ordenFecha("ev.fecha") + " >= ? "
                + "ORDER BY " + Util.ordenFecha("ev.fecha") + ", ev.hora LIMIT 3", hoy);
        while (c.moveToNext()) {
            String fecha = c.getString(3);
            String sub = Util.diaCorto(fecha) + (Util.vacio(c.getString(4)) ? "" : " · " + c.getString(4))
                    + "\n" + (c.isNull(5) ? "Toda la academia" : "Categoría " + c.getString(5));
            agregarTarjeta(contenedorEventos, R.layout.item_evento,
                    new int[]{R.id.txt_avatar, R.id.txt_titulo, R.id.txt_subtitulo, R.id.txt_estado},
                    new String[]{fecha.substring(0, 2), c.getString(1), sub, c.getString(2)});
        }
        c.close();
        if (contenedorEventos.getChildCount() == 0) {
            agregarVacio(contenedorEventos, "No hay actividades próximas.");
        }
    }

    /** Las 2 notas más recientes de la bitácora. */
    void cargarBitacora() {
        contenedorBitacora.removeAllViews();
        Cursor c = bd.consultar("SELECT fecha, tema, alumno, detalle FROM bitacora ORDER BY "
                + Util.ordenFecha("fecha") + " DESC, id DESC LIMIT 2");
        while (c.moveToNext()) {
            String sub = c.getString(0) + (Util.vacio(c.getString(2)) ? "" : "\nAlumno: " + c.getString(2));
            agregarTarjeta(contenedorBitacora, R.layout.item_bitacora,
                    new int[]{R.id.txt_avatar, R.id.txt_titulo, R.id.txt_subtitulo, R.id.txt_estado, R.id.txt_c1},
                    new String[]{c.getString(1).substring(0, 1), "Tema: " + c.getString(1), sub, "",
                            Util.guion(c.getString(3))});
        }
        c.close();
        if (contenedorBitacora.getChildCount() == 0) {
            agregarVacio(contenedorBitacora, "Sin actividad reciente.");
        }
    }

    private void agregarTarjeta(LinearLayout contenedor, int layout, int[] ids, String[] fila) {
        View tarjeta = getLayoutInflater().inflate(layout, contenedor, false);
        tarjeta.setPadding(0, tarjeta.getPaddingTop(), 0, tarjeta.getPaddingBottom());
        FilaAdapter.llenar(this, tarjeta, ids, fila);
        contenedor.addView(tarjeta);
    }

    private void agregarVacio(LinearLayout contenedor, String texto) {
        TextView t = new TextView(this);
        t.setText(texto);
        t.setTextSize(13);
        t.setPadding(0, 8, 0, 8);
        contenedor.addView(t);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.boton_contacto) {
            new AlertDialog.Builder(this)
                    .setTitle("Academia Lobos")
                    .setMessage("Información general de la Academia Lobos.\n\nLos datos de contacto se mostrarán "
                            + "cuando la app se conecte con el servidor.")
                    .setPositiveButton("Aceptar", null)
                    .show();
        } else if (id == R.id.boton_ver_calendario) {
            abrir(Calendario.class);
        } else if (id == R.id.boton_ver_bitacora) {
            abrir(Bitacora.class);
        }
    }
}
