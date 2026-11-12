package com.example.cocobol;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Evaluaciones de un jugador, separadas por área (pestañas).
 * Recibe "id_equipo" (muestra los jugadores de ese equipo) o "id_alumno" (abre directo a ese jugador).
 */
public class Evaluaciones extends ActividadBase implements AdapterView.OnItemClickListener,
        AdapterView.OnItemSelectedListener {

    static final int[] TABS = {R.id.tab_todas, R.id.tab_tecnico, R.id.tab_tactico, R.id.tab_fisico,
            R.id.tab_mental, R.id.tab_general};
    static final String[] AREAS = {"", "Técnico", "Táctico", "Físico", "Mental", "General"};

    ListView lista;
    Spinner filJugador;
    ArrayList<Long> ids = new ArrayList<>();
    ArrayList<Long> idsJugador = new ArrayList<>();
    int tabActiva = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_evaluaciones, R.id.menu_lateral_equipos);

        lista = prepararLista(R.layout.cabecera_evaluaciones);
        filJugador = cabecera.findViewById(R.id.fil_jugador);

        long idEquipo = getIntent().getLongExtra("id_equipo", -1);
        if (idEquipo != -1) {
            cargarOpciones(filJugador, idsJugador, null, "SELECT id, nombre || ' ' || paterno FROM alumno "
                    + "WHERE equipo_id = ? ORDER BY numero, nombre", String.valueOf(idEquipo));
        } else {
            cargarOpciones(filJugador, idsJugador, null, "SELECT id, nombre || ' ' || paterno FROM alumno "
                    + "ORDER BY nombre, paterno");
        }
        seleccionarId(filJugador, idsJugador, getIntent().getLongExtra("id_alumno", -1));

        for (int tab : TABS) {
            cabecera.findViewById(tab).setOnClickListener(this);
        }
        filJugador.setOnItemSelectedListener(this);
        cabecera.findViewById(R.id.boton_agregar).setOnClickListener(this);
        lista.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    void cargar() {
        // Pestaña activa en magenta, las demás en gris
        for (int i = 0; i < TABS.length; i++) {
            TextView tab = cabecera.findViewById(TABS[i]);
            boolean activa = i == tabActiva;
            tab.setTextColor(ContextCompat.getColor(this, activa ? R.color.magenta : R.color.gris));
            tab.setBackgroundResource(activa ? R.drawable.fondo_tab_activo : 0);
        }

        ArrayList<String[]> datos = new ArrayList<>();
        ids.clear();
        long jugador = idSeleccionado(filJugador, idsJugador);
        View tarjeta = cabecera.findViewById(R.id.txt_alumno_avatar);
        if (jugador == -1 || !llenarEncabezadoAlumno(cabecera, jugador)) {
            ((TextView) cabecera.findViewById(R.id.txt_alumno_nombre)).setText("Sin jugadores");
            ((TextView) cabecera.findViewById(R.id.txt_alumno_info)).setText("Agrega jugadores a esta categoría.");
            ((TextView) cabecera.findViewById(R.id.txt_promedio)).setText("");
            ((TextView) tarjeta).setText("—");
            mostrarEnLista(lista, R.layout.item_evaluaciones,
                new int[]{R.id.txt_titulo, R.id.txt_subtitulo, R.id.txt_estado, R.id.txt_c1}, datos);
            return;
        }

        String area = AREAS[tabActiva];
        Cursor c = bd.consultar("SELECT id, aspecto, area, fecha, calificacion, observaciones FROM evaluacion "
                + "WHERE alumno_id = ? AND (? = '' OR area = ?) ORDER BY " + Util.ordenFecha("fecha") + " DESC, id DESC",
                String.valueOf(jugador), area, area);
        double suma = 0;
        while (c.moveToNext()) {
            ids.add(c.getLong(0));
            suma += c.getInt(4);
            datos.add(new String[]{c.getString(1), c.getString(2) + " · " + Util.guion(c.getString(3)),
                    c.getInt(4) + "/10", Util.vacio(c.getString(5)) ? "Sin observaciones" : c.getString(5)});
        }
        c.close();

        ((TextView) cabecera.findViewById(R.id.txt_promedio)).setText(datos.isEmpty()
                ? "Evaluación pendiente"
                : String.format(Locale.US, "Promedio %s: %.1f / 10", area.isEmpty() ? "general" : area.toLowerCase(Locale.ROOT),
                suma / datos.size()));

        mostrarEnLista(lista, R.layout.item_evaluaciones,
                new int[]{R.id.txt_titulo, R.id.txt_subtitulo, R.id.txt_estado, R.id.txt_c1}, datos);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        for (int i = 0; i < TABS.length; i++) {
            if (id == TABS[i]) {
                tabActiva = i;
                cargar();
                return;
            }
        }
        if (id == R.id.boton_agregar) {
            long jugador = idSeleccionado(filJugador, idsJugador);
            if (jugador == -1) {
                toast("No hay jugadores para evaluar.");
                return;
            }
            Intent intent = new Intent(this, FormEvaluacion.class);
            intent.putExtra("id_alumno", jugador);
            intent.putExtra("area", AREAS[tabActiva]);
            startActivity(intent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> padre, View vista, int posicion, long id) {
        int i = posicion - lista.getHeaderViewsCount();
        if (i >= 0 && i < ids.size()) {
            abrir(FormEvaluacion.class, "id", ids.get(i));
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
