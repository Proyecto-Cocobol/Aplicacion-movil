package com.example.cocobol;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

public class Alumno_personal extends ActividadBase {

    String nombre = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_alumno_personal, R.id.menu_lateral_alumnos);

        findViewById(R.id.boton_editar).setOnClickListener(this);
        findViewById(R.id.boton_eliminar).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!configurarTabsAlumno(findViewById(R.id.main))) {
            toast("El alumno ya no existe.");
            finish();
            return;
        }
        Cursor c = bd.consultar("SELECT nombre, paterno, materno, fecha_nac, escuela, direccion, fecha_ingreso, "
                + "curp, sangre, alergias, seguro, padre, tel_padre, madre, tel_madre, tutor, tel_tutor "
                + "FROM alumno WHERE id = ?", String.valueOf(idAlumno));
        if (c.moveToFirst()) {
            nombre = Util.nombreCompleto(c.getString(0), c.getString(1), c.getString(2));
            ponerTexto(R.id.det_nombre, nombre);
            ponerTexto(R.id.det_fecha_nac, Util.guion(c.getString(3)));
            ponerTexto(R.id.det_edad, Util.edadTexto(c.getString(3)));
            ponerTexto(R.id.det_escuela, Util.guion(c.getString(4)));
            ponerTexto(R.id.det_direccion, Util.guion(c.getString(5)));
            ponerTexto(R.id.det_ingreso, Util.guion(c.getString(6)));
            ponerTexto(R.id.det_curp, Util.guion(c.getString(7)));
            ponerTexto(R.id.det_sangre, Util.guion(c.getString(8)));
            ponerTexto(R.id.det_alergias, Util.guion(c.getString(9)));
            ponerTexto(R.id.det_seguro, Util.guion(c.getString(10)));
            ponerTexto(R.id.det_padre, Util.guion(c.getString(11)));
            ponerTexto(R.id.det_tel_padre, Util.guion(c.getString(12)));
            ponerTexto(R.id.det_madre, Util.guion(c.getString(13)));
            ponerTexto(R.id.det_tel_madre, Util.guion(c.getString(14)));
            ponerTexto(R.id.det_tutor, Util.guion(c.getString(15)));
            ponerTexto(R.id.det_tel_tutor, Util.guion(c.getString(16)));
        }
        c.close();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.boton_editar) {
            abrir(FormAlumno.class, "id", idAlumno);
        } else if (id == R.id.boton_eliminar) {
            confirmar("¿Eliminar a " + nombre + "? También se borrarán sus pagos, evaluaciones, "
                    + "documentos y uniformes.", 1);
        }
    }

    @Override
    protected void alConfirmar(int accion) {
        bd.eliminarAlumno(idAlumno);
        bd.registrarBitacora("General", nombre, "Alumno eliminado del sistema");
        toast("Alumno eliminado.");
        finish();
    }
}
