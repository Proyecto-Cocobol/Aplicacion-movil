package com.example.cocobol;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

/**
 * Agregar / editar / eliminar profesor o personal administrativo.
 * Recibe "id" para editar, o "tipo" (PROFESOR / ADMINISTRATIVO) para uno nuevo.
 */
public class FormPersonal extends ActividadBase {

    EditText nombre, paterno, materno, fechaNac, telefono, correo;
    Spinner puesto, equipo;
    ArrayList<Long> idsEquipo = new ArrayList<>();
    long id;
    String tipo;
    long equipoOriginal = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.form_personal, R.id.menu_lateral_personal);

        nombre = findViewById(R.id.f_nombre);
        paterno = findViewById(R.id.f_paterno);
        materno = findViewById(R.id.f_materno);
        fechaNac = findViewById(R.id.f_fecha_nac);
        telefono = findViewById(R.id.f_telefono);
        correo = findViewById(R.id.f_correo);
        puesto = findViewById(R.id.f_puesto);
        equipo = findViewById(R.id.f_equipo);

        fechaNac.setOnClickListener(this);
        botonesFormulario();

        id = getIntent().getLongExtra("id", -1);
        tipo = getIntent().getStringExtra("tipo");
        if (id != -1) {
            tipo = bd.valor("SELECT tipo FROM personal WHERE id = ?", String.valueOf(id));
        }
        if (!"ADMINISTRATIVO".equals(tipo)) {
            tipo = "PROFESOR";
        }
        prepararTipo();

        if (id != -1) {
            modoEditar(tipo.equals("PROFESOR") ? "Editar profesor" : "Editar personal");
            cargar();
        }
    }

    /** Los profesores tienen categoría a cargo; el personal administrativo no. */
    void prepararTipo() {
        int arreglo = tipo.equals("PROFESOR") ? R.array.a_puesto_prof : R.array.a_puesto_admin;
        ArrayAdapter<CharSequence> adaptador = ArrayAdapter.createFromResource(this, arreglo,
                android.R.layout.simple_spinner_item);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        puesto.setAdapter(adaptador);

        if (tipo.equals("PROFESOR")) {
            cargarOpciones(equipo, idsEquipo, "Sin categoría", "SELECT id, categoria FROM equipo ORDER BY categoria");
        } else {
            findViewById(R.id.cont_equipo).setVisibility(View.GONE);
            ponerTexto(R.id.form_titulo, "Nuevo personal administrativo");
        }
    }

    void cargar() {
        Cursor c = bd.consultar("SELECT nombre, paterno, materno, fecha_nac, telefono, correo, puesto "
                + "FROM personal WHERE id = ?", String.valueOf(id));
        if (!c.moveToFirst()) {
            c.close();
            toast("El registro ya no existe.");
            finish();
            return;
        }
        nombre.setText(c.getString(0));
        paterno.setText(c.getString(1));
        materno.setText(c.getString(2));
        fechaNac.setText(c.getString(3));
        telefono.setText(c.getString(4));
        correo.setText(c.getString(5));
        seleccionarTexto(puesto, Util.nulo(c.getString(6)));
        c.close();

        if (tipo.equals("PROFESOR")) {
            String eq = bd.valor("SELECT id FROM equipo WHERE profesor_id = ? ORDER BY id LIMIT 1", String.valueOf(id));
            equipoOriginal = eq.isEmpty() ? -1 : Long.parseLong(eq);
            seleccionarId(equipo, idsEquipo, equipoOriginal);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int vid = v.getId();
        if (vid == R.id.f_fecha_nac) {
            elegirFecha(fechaNac);
        } else if (vid == R.id.form_guardar) {
            guardar();
        } else if (vid == R.id.form_cancelar) {
            finish();
        } else if (vid == R.id.form_eliminar) {
            confirmar("¿Eliminar a " + texto(nombre) + " " + texto(paterno) + "?"
                    + (tipo.equals("PROFESOR") ? " Sus categorías quedarán sin profesor y se borrará su planeación." : ""), 1);
        }
    }

    @Override
    protected void alConfirmar(int accion) {
        bd.eliminarPersonal(id);
        toast("Registro eliminado.");
        finish();
    }

    boolean validar() {
        String n = texto(nombre);
        String p = texto(paterno);
        String m = texto(materno);
        if (n.isEmpty()) return error(nombre, "El nombre es obligatorio.");
        if (!Util.soloLetras(n)) return error(nombre, "El nombre solo puede tener letras.");
        if (p.isEmpty()) return error(paterno, "El apellido paterno es obligatorio.");
        if (!Util.soloLetras(p)) return error(paterno, "El apellido solo puede tener letras.");
        if (!m.isEmpty() && !Util.soloLetras(m)) return error(materno, "El apellido solo puede tener letras.");

        String fn = texto(fechaNac);
        if (!Util.fechaValida(fn)) return error(fechaNac, "Elige la fecha de nacimiento.");
        int edad = Util.edad(fn);
        if (edad < 18 || edad > 80) return error(fechaNac, "La edad debe estar entre 18 y 80 años.");

        String t = texto(telefono);
        if (!t.isEmpty() && !Util.telefonoValido(t)) return error(telefono, "El teléfono debe tener 10 dígitos.");
        String co = texto(correo);
        if (!co.isEmpty() && !Util.correoValido(co)) return error(correo, "El correo no es válido.");
        return true;
    }

    void guardar() {
        if (!validar()) return;
        ContentValues v = BaseDatos.cv("tipo", tipo, "nombre", texto(nombre), "paterno", texto(paterno),
                "materno", texto(materno), "fecha_nac", texto(fechaNac), "telefono", texto(telefono),
                "correo", texto(correo), "puesto", textoSpinner(puesto));
        long idGuardado = id;
        if (id == -1) {
            idGuardado = bd.insertar("personal", v);
            toast("Registro guardado.");
        } else {
            bd.actualizar("personal", v, id);
            toast("Cambios guardados.");
        }

        // Asignar la categoría a cargo (un equipo tiene un solo profesor)
        if (tipo.equals("PROFESOR")) {
            long nuevo = idSeleccionado(equipo, idsEquipo);
            if (nuevo != equipoOriginal) {
                if (equipoOriginal != -1) {
                    bd.actualizar("equipo", BaseDatos.cv("profesor_id", null), equipoOriginal);
                }
                if (nuevo != -1) {
                    bd.actualizar("equipo", BaseDatos.cv("profesor_id", idGuardado), nuevo);
                }
            }
        }
        finish();
    }
}
