package com.example.cocobol;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

/** Agregar / editar / eliminar un pedido de uniformes a fábrica. Recibe "id" para editar. */
public class FormPedido extends ActividadBase {

    EditText cantidad, fecha, fabrica;
    Spinner tipo, estado;
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.form_pedido, R.id.menu_lateral_uniformes);

        cantidad = findViewById(R.id.f_cantidad);
        fecha = findViewById(R.id.f_fecha);
        fabrica = findViewById(R.id.f_fabrica);
        tipo = findViewById(R.id.f_tipo);
        estado = findViewById(R.id.f_estado);

        fecha.setOnClickListener(this);
        botonesFormulario();

        id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            modoEditar("Editar pedido");
            cargar();
        } else {
            fecha.setText(Util.hoy());
        }
    }

    void cargar() {
        Cursor c = bd.consultar("SELECT tipo, cantidad, fecha, fabrica, estado FROM pedido WHERE id = ?",
                String.valueOf(id));
        if (!c.moveToFirst()) {
            c.close();
            toast("El pedido ya no existe.");
            finish();
            return;
        }
        seleccionarTexto(tipo, c.getString(0));
        cantidad.setText(String.valueOf(c.getInt(1)));
        fecha.setText(c.getString(2));
        fabrica.setText(c.getString(3));
        seleccionarTexto(estado, c.getString(4));
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
            confirmar("¿Eliminar este pedido?", 1);
        }
    }

    @Override
    protected void alConfirmar(int accion) {
        bd.eliminar("pedido", id);
        toast("Pedido eliminado.");
        finish();
    }

    void guardar() {
        int n;
        try {
            n = Integer.parseInt(texto(cantidad));
        } catch (NumberFormatException e) {
            n = 0;
        }
        if (n < 1 || n > 1000) {
            error(cantidad, "La cantidad debe ser entre 1 y 1000.");
            return;
        }
        if (!Util.fechaValida(texto(fecha))) {
            error(fecha, "Elige la fecha del pedido.");
            return;
        }
        if (texto(fabrica).length() < 2) {
            error(fabrica, "Escribe el nombre de la fábrica.");
            return;
        }
        ContentValues v = BaseDatos.cv("tipo", textoSpinner(tipo), "cantidad", n, "fecha", texto(fecha),
                "fabrica", texto(fabrica), "estado", textoSpinner(estado));
        if (id == -1) {
            bd.insertar("pedido", v);
            bd.registrarBitacora("Uniformes", "", "Pedido " + textoSpinner(tipo) + " de " + n + " piezas a " + texto(fabrica));
            toast("Pedido guardado.");
        } else {
            bd.actualizar("pedido", v, id);
            toast("Cambios guardados.");
        }
        finish();
    }
}
