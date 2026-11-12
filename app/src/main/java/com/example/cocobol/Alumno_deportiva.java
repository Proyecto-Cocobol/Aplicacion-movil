package com.example.cocobol;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import java.util.Locale;

public class Alumno_deportiva extends ActividadBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_alumno_deportiva, R.id.menu_lateral_alumnos);

        findViewById(R.id.boton_evaluacion).setOnClickListener(this);
        findViewById(R.id.boton_editar).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!configurarTabsAlumno(findViewById(R.id.main))) {
            toast("El alumno ya no existe.");
            finish();
            return;
        }
        String id = String.valueOf(idAlumno);
        Cursor c = bd.consultar("SELECT a.nombre, a.paterno, a.materno, a.fecha_nac, a.numero, a.nombre_playera, "
                + "e.categoria, a.partidos, a.goles FROM alumno a LEFT JOIN equipo e ON e.id = a.equipo_id "
                + "WHERE a.id = ?", id);
        if (c.moveToFirst()) {
            ponerTexto(R.id.det_nombre, Util.nombreCompleto(c.getString(0), c.getString(1), c.getString(2)));
            ponerTexto(R.id.det_fecha_nac, Util.guion(c.getString(3)));
            ponerTexto(R.id.det_edad, Util.edadTexto(c.getString(3)));
            ponerTexto(R.id.det_numero, c.isNull(4) ? "—" : "#" + c.getInt(4));
            ponerTexto(R.id.det_playera, Util.guion(c.getString(5)));
            ponerTexto(R.id.det_categoria, c.isNull(6) ? "Sin categoría" : c.getString(6));
            ponerTexto(R.id.det_partidos, String.valueOf(c.getInt(7)));
            ponerTexto(R.id.det_goles, String.valueOf(c.getInt(8)));
        }
        c.close();

        Cursor e = bd.consultar("SELECT COUNT(*), AVG(calificacion) FROM evaluacion WHERE alumno_id = ?", id);
        if (e.moveToFirst() && e.getInt(0) > 0) {
            ponerTexto(R.id.det_eval_estado, "Evaluado (" + e.getInt(0) + " aspectos)");
            ponerTexto(R.id.det_eval_promedio, String.format(Locale.US, "%.1f / 10", e.getDouble(1)));
        } else {
            ponerTexto(R.id.det_eval_estado, "Pendiente");
            ponerTexto(R.id.det_eval_promedio, "—");
        }
        e.close();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.boton_evaluacion) {
            abrir(Evaluaciones.class, "id_alumno", idAlumno);
        } else if (id == R.id.boton_editar) {
            abrir(FormAlumno.class, "id", idAlumno);
        }
    }
}
