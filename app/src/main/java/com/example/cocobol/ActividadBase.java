package com.example.cocobol;

import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Pantalla base del administrador: barra superior magenta, menú lateral y utilidades.
 * Todas las pantallas del panel heredan de esta clase.
 * Las subclases que tengan botones sobrescriben onClick y llaman primero a super.onClick(v).
 */
public class ActividadBase extends AppCompatActivity
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawer;
    protected BaseDatos bd;
    /** Alumno que se está consultando en las 4 pestañas de información del alumno. */
    protected long idAlumno = -1;
    /** Encabezado del ListView (filtros, botones) en las pantallas de lista. */
    protected View cabecera;
    private FilaAdapter adaptadorLista;
    private int itemMenu;

    /** Carga el layout y deja listo el menú lateral. "itemMenu" es la opción que se marca como activa. */
    protected void iniciarPantalla(int layout, int itemMenu) {
        EdgeToEdge.enable(this);
        setContentView(layout);
        this.itemMenu = itemMenu;
        bd = BaseDatos.obtener(this);

        // Solo el ADMINISTRADOR puede entrar al panel (igual que AuthFilter en la web)
        if (!"ADMINISTRADOR".equals(Sesion.rol(this))) {
            cerrarSesion();
        }

        drawer = findViewById(R.id.drawer_layout);
        final View barra = findViewById(R.id.barra_superior);
        final int dp8 = (int) (8 * getResources().getDisplayMetrics().density);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets b = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                barra.setPadding(dp8, dp8 + b.top, dp8, dp8);
                v.setPadding(b.left, 0, b.right, b.bottom);
                return insets;
            }
        });

        findViewById(R.id.boton_menu).setOnClickListener(this);

        NavigationView menu = findViewById(R.id.nav_menu_admin);
        menu.setCheckedItem(itemMenu);
        menu.setNavigationItemSelectedListener(this);

        // El botón "atrás" cierra primero el menú lateral si está abierto
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawers();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    /** Agrega el encabezado (filtros y botones) arriba del ListView y regresa la lista. */
    protected ListView prepararLista(int layoutCabecera) {
        ListView lista = findViewById(R.id.lista);
        cabecera = getLayoutInflater().inflate(layoutCabecera, lista, false);
        lista.addHeaderView(cabecera, null, false);
        return lista;
    }

    /**
     * Muestra los renglones en el ListView. El adaptador se crea una sola vez y después solo se cambian
     * los datos; así el campo de búsqueda del encabezado no pierde el foco mientras se escribe.
     * "botones" son los ids de botones de la fila que deben responder (se atienden en onClick).
     */
    protected void mostrarEnLista(ListView lista, int layoutFila, int[] ids, ArrayList<String[]> datos, int... botones) {
        if (adaptadorLista == null) {
            adaptadorLista = new FilaAdapter(this, layoutFila, ids, new ArrayList<String[]>());
            adaptadorLista.setOyente(this, botones);
            lista.setAdapter(adaptadorLista);
        }
        adaptadorLista.cambiarDatos(datos);
    }

    // ================================================================== Clics

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.boton_menu) {
            drawer.openDrawer(GravityCompat.START);
        } else if (id == R.id.boton_regresar) {
            finish();
        } else if (id == R.id.tab_personal) {
            irATab(Alumno_personal.class);
        } else if (id == R.id.tab_deportiva) {
            irATab(Alumno_deportiva.class);
        } else if (id == R.id.tab_financiera) {
            irATab(Alumno_financiera.class);
        } else if (id == R.id.tab_administrativa) {
            irATab(Alumno_administrativa.class);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        drawer.closeDrawers();

        if (id == R.id.menu_lateral_contrasena) {
            mostrarDialogoContrasena();
        } else if (id == R.id.menu_lateral_cerrar) {
            cerrarSesion();
        } else if (id == R.id.menu_lateral_inicio) {
            irAModulo(Inicio_principal_admin.class);
        } else if (id == R.id.menu_lateral_alumnos) {
            irAModulo(Alumnos.class);
        } else if (id == R.id.menu_lateral_personal) {
            irAModulo(Personal.class);
        } else if (id == R.id.menu_lateral_finanzas) {
            irAModulo(Finanzas.class);
        } else if (id == R.id.menu_lateral_equipos) {
            irAModulo(Equipos.class);
        } else if (id == R.id.menu_lateral_calendario) {
            irAModulo(Calendario.class);
        } else if (id == R.id.menu_lateral_documentos) {
            irAModulo(Documentos.class);
        } else if (id == R.id.menu_lateral_uniformes) {
            irAModulo(Uniformes.class);
        } else if (id == R.id.menu_lateral_cuentas) {
            irAModulo(Cuentas.class);
        }
        return true;
    }

    // ================================================================== Navegación

    /** Navegación del menú: regresa al inicio y desde ahí abre el módulo elegido. */
    protected void irAModulo(Class<?> destino) {
        Intent inicio = new Intent(this, Inicio_principal_admin.class);
        inicio.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(inicio);
        if (destino != Inicio_principal_admin.class) {
            startActivity(new Intent(this, destino));
        }
    }

    /** Abre otra pantalla encima de la actual (se puede volver con "atrás"). */
    protected void abrir(Class<?> destino) {
        startActivity(new Intent(this, destino));
    }

    /** Abre otra pantalla mandándole un dato (por ejemplo el id del registro a editar). */
    protected void abrir(Class<?> destino, String extra, long valor) {
        Intent intent = new Intent(this, destino);
        intent.putExtra(extra, valor);
        startActivity(intent);
    }

    protected void toast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    protected void cerrarSesion() {
        Sesion.cerrar(this);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ================================================================== Pestañas del alumno

    /** Botón regresar, pestañas y datos del encabezado de las 4 pantallas del alumno. */
    protected boolean configurarTabsAlumno(View raiz) {
        idAlumno = getIntent().getLongExtra("id_alumno", -1);
        raiz.findViewById(R.id.boton_regresar).setOnClickListener(this);
        raiz.findViewById(R.id.tab_personal).setOnClickListener(this);
        raiz.findViewById(R.id.tab_deportiva).setOnClickListener(this);
        raiz.findViewById(R.id.tab_financiera).setOnClickListener(this);
        raiz.findViewById(R.id.tab_administrativa).setOnClickListener(this);
        return llenarEncabezadoAlumno(raiz, idAlumno);
    }

    private void irATab(Class<?> destino) {
        if (!destino.equals(getClass())) {
            Intent intent = new Intent(this, destino);
            intent.putExtra("id_alumno", idAlumno);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }
    }

    /** Llena avatar, nombre y "Alumno · 16 años · Categoría X". Regresa false si el alumno no existe. */
    protected boolean llenarEncabezadoAlumno(View raiz, long id) {
        Cursor c = bd.consultar("SELECT a.nombre, a.paterno, a.materno, a.fecha_nac, e.categoria, a.numero "
                + "FROM alumno a LEFT JOIN equipo e ON e.id = a.equipo_id WHERE a.id = ?", String.valueOf(id));
        if (!c.moveToFirst()) {
            c.close();
            return false;
        }
        String categoria = c.isNull(4) ? "Sin categoría" : "Categoría " + c.getString(4);
        String numero = c.isNull(5) ? "" : " · #" + c.getInt(5);
        ((TextView) raiz.findViewById(R.id.txt_alumno_avatar)).setText(Util.iniciales(c.getString(0), c.getString(1)));
        ((TextView) raiz.findViewById(R.id.txt_alumno_nombre)).setText(
                Util.nombreCompleto(c.getString(0), c.getString(1), c.getString(2)));
        ((TextView) raiz.findViewById(R.id.txt_alumno_info)).setText(
                "Alumno · " + Util.edadTexto(c.getString(3)) + numero + " · " + categoria);
        c.close();
        return true;
    }

    // ================================================================== Formularios

    protected String texto(EditText campo) {
        return Util.texto(campo);
    }

    protected void ponerTexto(int idVista, String texto) {
        ((TextView) findViewById(idVista)).setText(texto);
    }

    /** Marca el campo con error, muestra el mensaje abajo del formulario y regresa false. */
    protected boolean error(EditText campo, String mensaje) {
        if (campo != null) {
            campo.setError(mensaje);
            campo.requestFocus();
        }
        TextView txt = findViewById(R.id.form_mensaje);
        if (txt != null) {
            txt.setText(mensaje);
        }
        toast(mensaje);
        return false;
    }

    /** Para los campos de fecha: abre un calendario y escribe la fecha en formato dd/mm/aaaa. */
    protected void elegirFecha(final EditText campo) {
        Calendar c = Util.fechaValida(texto(campo)) ? Util.aCalendario(texto(campo)) : Calendar.getInstance();
        DatePickerDialog dialogo = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker vista, int anio, int mes, int dia) {
                Calendar elegido = Calendar.getInstance();
                elegido.set(anio, mes, dia);
                campo.setText(Util.formatear(elegido));
                campo.setError(null);
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialogo.show();
    }

    /** Para los campos de hora: abre un reloj y escribe la hora en formato hh:mm. */
    protected void elegirHora(final EditText campo) {
        int hora = 17;
        int minuto = 0;
        if (Util.horaValida(texto(campo))) {
            hora = Integer.parseInt(texto(campo).substring(0, 2));
            minuto = Integer.parseInt(texto(campo).substring(3));
        }
        TimePickerDialog dialogo = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker vista, int h, int m) {
                campo.setText(String.format(Locale.US, "%02d:%02d", h, m));
            }
        }, hora, minuto, true);
        dialogo.setButton(DialogInterface.BUTTON_NEUTRAL, "Sin hora", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int boton) {
                campo.setText("");
            }
        });
        dialogo.show();
    }

    /** Pregunta antes de eliminar. Si el usuario acepta se llama a alConfirmar(accion). */
    protected void confirmar(String mensaje, final int accion) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar")
                .setMessage(mensaje)
                .setPositiveButton("Sí, eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int boton) {
                        alConfirmar(accion);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /** Las pantallas que usan confirmar() sobrescriben este método. */
    protected void alConfirmar(int accion) {
    }

    /** Muestra el botón Eliminar y cambia el título cuando el formulario se abre para editar. */
    protected void modoEditar(String titulo) {
        ponerTexto(R.id.form_titulo, titulo);
        findViewById(R.id.form_eliminar).setVisibility(View.VISIBLE);
    }

    /** Conecta los botones comunes de los formularios (Guardar, Cancelar, Eliminar). */
    protected void botonesFormulario() {
        findViewById(R.id.form_guardar).setOnClickListener(this);
        findViewById(R.id.form_cancelar).setOnClickListener(this);
        findViewById(R.id.form_eliminar).setOnClickListener(this);
    }

    // ================================================================== Spinners

    protected void llenarSpinner(Spinner spinner, ArrayList<String> opciones) {
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opciones);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adaptador);
    }

    /**
     * Llena un Spinner con el resultado de una consulta que regresa (id, texto).
     * Si "primera" no es null se agrega al inicio con id -1 (por ejemplo "Sin asignar" o "Todos").
     */
    protected void cargarOpciones(Spinner spinner, ArrayList<Long> ids, String primera, String sql, String... args) {
        ArrayList<String> textos = new ArrayList<>();
        ids.clear();
        if (primera != null) {
            textos.add(primera);
            ids.add(-1L);
        }
        Cursor c = bd.consultar(sql, args);
        while (c.moveToNext()) {
            ids.add(c.getLong(0));
            textos.add(c.getString(1));
        }
        c.close();
        llenarSpinner(spinner, textos);
    }

    protected long idSeleccionado(Spinner spinner, ArrayList<Long> ids) {
        int pos = spinner.getSelectedItemPosition();
        if (pos < 0 || pos >= ids.size()) {
            return -1;
        }
        return ids.get(pos);
    }

    protected void seleccionarId(Spinner spinner, ArrayList<Long> ids, long id) {
        int pos = ids.indexOf(id);
        if (pos >= 0) {
            spinner.setSelection(pos);
        }
    }

    protected String textoSpinner(Spinner spinner) {
        Object o = spinner.getSelectedItem();
        return o == null ? "" : o.toString();
    }

    protected void seleccionarTexto(Spinner spinner, String texto) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(texto)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    // ================================================================== Documentos

    /** Abre el archivo de un documento con la app que tenga el teléfono (visor de PDF, galería, etc.). */
    protected void verArchivo(String uri, String nombreArchivo) {
        if (Util.vacio(uri)) {
            toast("\"" + Util.nulo(nombreArchivo) + "\" es un dato de ejemplo: no hay archivo guardado en el teléfono.");
            return;
        }
        Uri direccion = Uri.parse(uri);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(direccion, getContentResolver().getType(direccion));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            toast("No hay una aplicación para abrir este archivo.");
        } catch (SecurityException e) {
            toast("Ya no hay permiso para abrir el archivo. Vuelve a cargarlo.");
        }
    }

    // ================================================================== Contraseña

    /** Ventana "Cambiar contraseña" de la cuenta que inició sesión. */
    protected void mostrarDialogoContrasena() {
        View vista = getLayoutInflater().inflate(R.layout.dialog_cambiar_contrasena, null);
        final EditText actual = vista.findViewById(R.id.dlg_actual);
        final EditText nueva = vista.findViewById(R.id.dlg_nueva);
        final EditText confirmar = vista.findViewById(R.id.dlg_confirmar);
        final TextView mensaje = vista.findViewById(R.id.dlg_mensaje);

        final AlertDialog dialogo = new AlertDialog.Builder(this)
                .setView(vista)
                .setPositiveButton("Actualizar contraseña", null)
                .setNegativeButton("Cancelar", null)
                .create();
        dialogo.show();

        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String a = actual.getText().toString();
                String n = nueva.getText().toString();
                String c = confirmar.getText().toString();
                long idCuenta = Sesion.id(ActividadBase.this);
                BaseDatos base = BaseDatos.obtener(ActividadBase.this);
                if (a.isEmpty() || n.isEmpty() || c.isEmpty()) {
                    mensaje.setText("Completa todos los campos.");
                } else if (!base.contrasenaCorrecta(idCuenta, a)) {
                    mensaje.setText("La contraseña actual no es correcta.");
                } else if (n.length() < 10) {
                    mensaje.setText("La nueva contraseña debe tener al menos 10 caracteres.");
                } else if (!n.equals(c)) {
                    mensaje.setText("La confirmación no coincide.");
                } else if (n.equals(a)) {
                    mensaje.setText("La nueva contraseña debe ser diferente a la actual.");
                } else {
                    base.cambiarContrasena(idCuenta, n);
                    toast("Contraseña actualizada.");
                    dialogo.dismiss();
                }
            }
        });
    }
}
