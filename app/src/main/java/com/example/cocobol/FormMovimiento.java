package com.example.cocobol;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/** Registrar / editar / eliminar un cargo o pago. Recibe "id" para editar o "id_alumno" para uno nuevo. */
public class FormMovimiento extends ActividadBase {

    EditText periodo, monto, fecha;
    Spinner alumno, concepto, estado;
    ArrayList<Long> idsAlumno = new ArrayList<>();
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.form_movimiento, R.id.menu_lateral_finanzas);

        periodo = findViewById(R.id.f_periodo);
        monto = findViewById(R.id.f_monto);
        fecha = findViewById(R.id.f_fecha);
        alumno = findViewById(R.id.f_alumno);
        concepto = findViewById(R.id.f_concepto);
        estado = findViewById(R.id.f_estado);

        cargarOpciones(alumno, idsAlumno, null, "SELECT id, nombre || ' ' || paterno FROM alumno ORDER BY nombre, paterno");

        fecha.setOnClickListener(this);
        botonesFormulario();

        id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            modoEditar("Editar movimiento");
            cargar();
        } else {
            seleccionarId(alumno, idsAlumno, getIntent().getLongExtra("id_alumno", -1));
            fecha.setText(Util.hoy());
            periodo.setText(Util.capitalizar(Util.MESES[Calendar.getInstance().get(Calendar.MONTH)]));
        }
        if (idsAlumno.isEmpty()) {
            error(null, "Primero registra un alumno.");
        }
    }

    void cargar() {
        Cursor c = bd.consultar("SELECT alumno_id, concepto, periodo, monto, estado, fecha FROM movimiento "
                + "WHERE id = ?", String.valueOf(id));
        if (!c.moveToFirst()) {
            c.close();
            toast("El movimiento ya no existe.");
            finish();
            return;
        }
        seleccionarId(alumno, idsAlumno, c.getLong(0));
        seleccionarTexto(concepto, c.getString(1));
        periodo.setText(c.getString(2));
        monto.setText(String.format(Locale.US, "%.2f", c.getDouble(3)));
        seleccionarTexto(estado, c.getString(4));
        fecha.setText(c.getString(5));
        c.close();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int vid = v.getId();
        if (vid == R.id.f_fecha) {
            elegirFecha(fecha);
        } else if (vid == R.id.form_guardar) {
            guardar();
        } else if (vid == R.id.form_cancelar) {
            finish();
        } else if (vid == R.id.form_eliminar) {
            confirmar("¿Eliminar este movimiento?", 1);
        }
    }

    @Override
    protected void alConfirmar(int accion) {
        bd.eliminar("movimiento", id);
        toast("Movimiento eliminado.");
        finish();
    }

    void guardar() {
        long idAl = idSeleccionado(alumno, idsAlumno);
        if (idAl == -1) {
            error(null, "Primero registra un alumno.");
            return;
        }
        if (texto(periodo).isEmpty()) {
            error(periodo, "Escribe el periodo (por ejemplo, el mes).");
            return;
        }
        double cantidad;
        try {
            cantidad = Double.parseDouble(texto(monto));
        } catch (NumberFormatException e) {
            error(monto, "Escribe un monto válido.");
            return;
        }
        if (cantidad <= 0 || cantidad > 100000) {
            error(monto, "El monto debe ser mayor a 0 y menor a 100,000.");
            return;
        }
        if (!Util.fechaValida(texto(fecha))) {
            error(fecha, "Elige la fecha.");
            return;
        }

        ContentValues v = BaseDatos.cv("alumno_id", idAl, "concepto", textoSpinner(concepto),
                "periodo", texto(periodo), "monto", cantidad, "estado", textoSpinner(estado), "fecha", texto(fecha));
        if (id == -1) {
            bd.insertar("movimiento", v);
            if (textoSpinner(estado).equals("Pagado")) {
                bd.registrarBitacora("Pagos", bd.nombreAlumno(idAl),
                        "Concepto: " + textoSpinner(concepto) + " · Total: " + Util.dinero(cantidad));
            }
            toast("Movimiento registrado.");
        } else {
            bd.actualizar("movimiento", v, id);
            toast("Cambios guardados.");
        }
        finish();
    }
}
