package com.example.cocobol;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Crear / editar / eliminar cuenta de acceso (mismas reglas que el formulario de la web).
 * Recibe "id" para editar.
 */
public class Registro extends ActividadBase implements AdapterView.OnItemSelectedListener {

    EditText nombre, paterno, materno, correo, telefono, usuario, boleta, puesto, password, confirmarPass;
    Spinner rol, parentesco;
    CheckBox activa;
    long id;
    String rolOriginal = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarPantalla(R.layout.activity_registro, R.id.menu_lateral_cuentas);

        nombre = findViewById(R.id.f_nombre);
        paterno = findViewById(R.id.f_paterno);
        materno = findViewById(R.id.f_materno);
        correo = findViewById(R.id.f_correo);
        telefono = findViewById(R.id.f_telefono);
        usuario = findViewById(R.id.f_usuario);
        boleta = findViewById(R.id.f_boleta);
        puesto = findViewById(R.id.f_puesto);
        password = findViewById(R.id.f_password);
        confirmarPass = findViewById(R.id.f_confirmar);
        rol = findViewById(R.id.f_rol);
        parentesco = findViewById(R.id.f_parentesco);
        activa = findViewById(R.id.f_activa);

        rol.setOnItemSelectedListener(this);
        botonesFormulario();

        id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            modoEditar("Editar cuenta");
            ponerTexto(R.id.form_subtitulo, "La contraseña se cambia con \"Restablecer contraseña\" en la lista.");
            findViewById(R.id.cont_password).setVisibility(View.GONE);
            cargar();
        } else {
            activa.setChecked(true);
            rol.setSelection(Util.posicion(Util.ROLES, "ALUMNO"));
        }
        mostrarCamposDelRol();
    }

    void cargar() {
        Cursor c = bd.consultar("SELECT nombre, paterno, materno, correo, telefono, usuario, rol, boleta, puesto, "
                + "parentesco, activo FROM cuenta WHERE id = ?", String.valueOf(id));
        if (!c.moveToFirst()) {
            c.close();
            toast("La cuenta ya no existe.");
            finish();
            return;
        }
        nombre.setText(c.getString(0));
        paterno.setText(c.getString(1));
        materno.setText(c.getString(2));
        correo.setText(c.getString(3));
        telefono.setText(c.getString(4));
        usuario.setText(c.getString(5));
        rolOriginal = c.getString(6);
        rol.setSelection(Util.posicion(Util.ROLES, rolOriginal));
        boleta.setText(c.getString(7));
        puesto.setText(c.getString(8));
        parentesco.setSelection(Util.posicion(Util.PARENTESCOS, Util.nulo(c.getString(9))));
        activa.setChecked(c.getInt(10) == 1);
        c.close();
    }

    String rolElegido() {
        int pos = rol.getSelectedItemPosition();
        return pos < 0 ? "ALUMNO" : Util.ROLES[pos];
    }

    /** Boleta solo para alumno, puesto solo para profesor, parentesco solo para responsable. */
    void mostrarCamposDelRol() {
        String r = rolElegido();
        findViewById(R.id.cont_boleta).setVisibility(r.equals("ALUMNO") ? View.VISIBLE : View.GONE);
        findViewById(R.id.cont_puesto).setVisibility(r.equals("PROFESOR") ? View.VISIBLE : View.GONE);
        findViewById(R.id.cont_parentesco).setVisibility(r.equals("RESPONSABLE") ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemSelected(AdapterView<?> padre, View vista, int posicion, long idFila) {
        mostrarCamposDelRol();
    }

    @Override
    public void onNothingSelected(AdapterView<?> padre) {
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int vid = v.getId();
        if (vid == R.id.form_guardar) {
            guardar();
        } else if (vid == R.id.form_cancelar) {
            finish();
        } else if (vid == R.id.form_eliminar) {
            if (id == Sesion.id(this)) {
                error(null, "No puedes eliminar la cuenta con la que iniciaste sesión.");
            } else if (rolOriginal.equals("ADMINISTRADOR") && otrosAdminsActivos() == 0) {
                error(null, "No se puede eliminar el único administrador activo.");
            } else {
                confirmar("¿Eliminar la cuenta " + texto(usuario) + "? Esta acción no se puede deshacer.", 1);
            }
        }
    }

    @Override
    protected void alConfirmar(int accion) {
        bd.eliminar("cuenta", id);
        bd.registrarBitacora("Cuentas", "", "Cuenta eliminada: " + texto(usuario));
        toast("Cuenta eliminada.");
        finish();
    }

    int otrosAdminsActivos() {
        return (int) bd.numero("SELECT COUNT(*) FROM cuenta WHERE rol = 'ADMINISTRADOR' AND activo = 1 AND id <> ?",
                String.valueOf(id));
    }

    boolean validar() {
        String n = texto(nombre);
        String p = texto(paterno);
        String m = texto(materno);
        String u = texto(usuario);
        String co = texto(correo);
        String t = texto(telefono);
        String r = rolElegido();

        if (n.isEmpty() || p.isEmpty() || u.isEmpty()) {
            return error(n.isEmpty() ? nombre : (p.isEmpty() ? paterno : usuario), "Completa los campos obligatorios.");
        }
        if (!Util.soloLetras(n)) return error(nombre, "El nombre solo puede tener letras.");
        if (!Util.soloLetras(p)) return error(paterno, "El apellido solo puede tener letras.");
        if (!m.isEmpty() && !Util.soloLetras(m)) return error(materno, "El apellido solo puede tener letras.");
        if (!co.isEmpty() && !Util.correoValido(co)) return error(correo, "El correo no es válido.");
        if (!co.isEmpty() && bd.existe("SELECT id FROM cuenta WHERE correo = ? COLLATE NOCASE AND id <> ?", co,
                String.valueOf(id))) {
            return error(correo, "Ese correo ya está registrado en otra cuenta.");
        }
        if (!t.isEmpty() && !Util.telefonoValido(t)) return error(telefono, "El teléfono debe tener 10 dígitos.");

        if (!u.matches("^[A-Za-z0-9._-]{3,30}$")) {
            return error(usuario, "El usuario debe tener de 3 a 30 caracteres, sin espacios ni acentos.");
        }
        if (bd.existe("SELECT id FROM cuenta WHERE usuario = ? COLLATE NOCASE AND id <> ?", u, String.valueOf(id))) {
            return error(usuario, "Ese nombre de usuario ya existe.");
        }

        if (r.equals("ALUMNO")) {
            String b = texto(boleta);
            if (!b.matches("\\d{6,12}")) return error(boleta, "La boleta debe tener de 6 a 12 dígitos.");
            if (bd.existe("SELECT id FROM cuenta WHERE boleta = ? AND id <> ?", b, String.valueOf(id))) {
                return error(boleta, "Esa boleta ya está registrada.");
            }
        }
        if (r.equals("PROFESOR") && texto(puesto).isEmpty()) return error(puesto, "Escribe el puesto del profesor.");

        if (id == -1) {
            if (password.getText().toString().length() < 10) {
                return error(password, "La contraseña debe tener al menos 10 caracteres.");
            }
            if (!password.getText().toString().equals(confirmarPass.getText().toString())) {
                return error(confirmarPass, "La confirmación no coincide.");
            }
        } else {
            boolean esYo = id == Sesion.id(this);
            if (esYo && !activa.isChecked()) return error(null, "No puedes desactivar tu propia cuenta.");
            if (esYo && !r.equals("ADMINISTRADOR")) return error(null, "No puedes quitarte el rol de administrador.");
            boolean dejaDeSerAdmin = rolOriginal.equals("ADMINISTRADOR") && (!r.equals("ADMINISTRADOR") || !activa.isChecked());
            if (dejaDeSerAdmin && otrosAdminsActivos() == 0) {
                return error(null, "Debe quedar al menos un administrador activo.");
            }
        }
        return true;
    }

    void guardar() {
        if (!validar()) return;
        String r = rolElegido();
        String b = r.equals("ALUMNO") ? texto(boleta) : "";
        String pu = r.equals("PROFESOR") ? texto(puesto) : "";
        String pa = r.equals("RESPONSABLE") ? Util.PARENTESCOS[Math.max(0, parentesco.getSelectedItemPosition())] : "";

        if (id == -1) {
            BaseDatos.insertarCuenta(bd.getWritableDatabase(), texto(nombre), texto(paterno), texto(materno),
                    texto(correo), texto(telefono), texto(usuario), password.getText().toString(), r, b, pu, pa,
                    activa.isChecked());
            bd.registrarBitacora("Cuentas", "", "Cuenta creada: " + texto(usuario) + " (" + Util.rolLegible(r) + ")");
            toast("Cuenta creada.");
        } else {
            ContentValues v = BaseDatos.cv("nombre", texto(nombre), "paterno", texto(paterno), "materno", texto(materno),
                    "correo", texto(correo), "telefono", texto(telefono), "usuario", texto(usuario), "rol", r,
                    "boleta", b, "puesto", pu, "parentesco", pa, "activo", activa.isChecked() ? 1 : 0);
            bd.actualizar("cuenta", v, id);
            if (id == Sesion.id(this)) {
                Sesion.iniciar(this, id, texto(nombre) + " " + texto(paterno), r);
            }
            toast("Cambios guardados.");
        }
        finish();
    }
}
