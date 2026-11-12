package com.example.cocobol;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Locale;

public class Cuentas extends ActividadBase implements AdapterView.OnItemClickListener, TextWatcher {

    ListView lista;
    EditText buscar;
    ArrayList<Long> ids = new ArrayList<>();
    ArrayList<String> usuarios = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_cuentas, R.id.menu_lateral_cuentas);

        lista = prepararLista(R.layout.cabecera_cuentas);
        buscar = cabecera.findViewById(R.id.buscar);
        buscar.addTextChangedListener(this);
        cabecera.findViewById(R.id.boton_crear_cuenta).setOnClickListener(this);
        lista.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    void cargar() {
        String texto = buscar.getText().toString().trim().toLowerCase(Locale.ROOT);
        ArrayList<String[]> datos = new ArrayList<>();
        ids.clear();
        usuarios.clear();
        Cursor c = bd.consultar("SELECT id, nombre, paterno, materno, usuario, correo, rol, activo FROM cuenta "
                + "ORDER BY nombre, paterno");
        while (c.moveToNext()) {
            String nombre = Util.nombreCompleto(c.getString(1), c.getString(2), c.getString(3));
            String todo = (nombre + " " + c.getString(4) + " " + Util.nulo(c.getString(5))).toLowerCase(Locale.ROOT);
            if (!texto.isEmpty() && !todo.contains(texto)) continue;

            ids.add(c.getLong(0));
            usuarios.add(c.getString(4));
            datos.add(new String[]{Util.iniciales(c.getString(1), c.getString(2)), nombre,
                    Util.rolLegible(c.getString(6)), c.getInt(7) == 1 ? "ACTIVO" : "INACTIVO",
                    c.getString(4), Util.guion(c.getString(5)), c.getString(6)});
        }
        c.close();

        mostrarEnLista(lista, R.layout.item_cuentas,
                new int[]{R.id.txt_avatar, R.id.txt_titulo, R.id.txt_subtitulo, R.id.txt_estado,
                        R.id.txt_c1, R.id.txt_c2, R.id.txt_c3}, datos, R.id.btn_editar, R.id.btn_reset);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.boton_crear_cuenta) {
            abrir(Registro.class);
        } else if (id == R.id.btn_editar) {
            abrir(Registro.class, "id", ids.get((Integer) v.getTag()));
        } else if (id == R.id.btn_reset) {
            int i = (Integer) v.getTag();
            restablecer(ids.get(i), usuarios.get(i));
        }
    }

    /** Ventana "Restablecer contraseña": el administrador asigna una nueva sin conocer la anterior. */
    void restablecer(final long idCuenta, String usuario) {
        View vista = getLayoutInflater().inflate(R.layout.dialog_restablecer, null);
        final EditText nueva = vista.findViewById(R.id.dlg_nueva);
        final EditText confirmar = vista.findViewById(R.id.dlg_confirmar);
        final TextView mensaje = vista.findViewById(R.id.dlg_mensaje);
        ((TextView) vista.findViewById(R.id.dlg_info)).setText("Cuenta: " + usuario + ". La contraseña anterior no "
                + "se puede consultar. Define una nueva contraseña para esta cuenta.");

        final AlertDialog dialogo = new AlertDialog.Builder(this)
                .setView(vista)
                .setPositiveButton("Restablecer", null)
                .setNegativeButton("Cancelar", null)
                .create();
        dialogo.show();
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String n = nueva.getText().toString();
                if (n.length() < 10) {
                    mensaje.setText("La contraseña debe tener al menos 10 caracteres.");
                } else if (!n.equals(confirmar.getText().toString())) {
                    mensaje.setText("La confirmación no coincide.");
                } else {
                    bd.cambiarContrasena(idCuenta, n);
                    toast("Contraseña restablecida.");
                    dialogo.dismiss();
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> padre, View vista, int posicion, long id) {
        int i = posicion - lista.getHeaderViewsCount();
        if (i >= 0 && i < ids.size()) {
            abrir(Registro.class, "id", ids.get(i));
        }
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
