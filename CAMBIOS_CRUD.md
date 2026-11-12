# Cambios: CRUD con validaciones (app Android Cocobol)

## Qué se hizo
- **Base de datos local (SQLite)** en `BaseDatos.java`: 12 tablas basadas en `academia_lobos_v1.sql` (simplificadas). Todavía NO se conecta con MySQL/Azure.
  La primera vez que se abre la app se cargan los mismos datos de ejemplo de la web.
- **Login con cuentas reales**: usuario o boleta + contraseña (SHA-256 con sal), revisa si la cuenta está activa y manda a cada rol a su pantalla.
  - Cuentas de prueba: `admin / admin`, `profesor / profesor`, `alumno / alumno` (o boleta `2025000001`).
- **CRUD completo con validaciones** (agregar, editar, eliminar con confirmación):

| Módulo | Pantalla de lista | Formulario |
|---|---|---|
| Alumnos (+4 pestañas) | Alumnos | FormAlumno |
| Profesores / Administrativo | Profesores, Administrativo | FormPersonal |
| Planeación | Planeacion | FormPlaneacion |
| Finanzas / Info. financiera | Finanzas, Alumno_financiera | FormMovimiento |
| Equipos / Categorías | Equipos, Categorias | FormEquipo (+ agregar/quitar jugadores) |
| Evaluaciones | Evaluaciones (pestañas por área) | FormEvaluacion |
| Calendario | Calendario (mes real, flechas ‹ ›) | FormEvento |
| Bitácora | Bitacora | FormBitacora (también se llena sola) |
| Documentos / Info. administrativa | Documentos, Alumno_administrativa | FormDocumento (elige PDF/imagen del teléfono) |
| Uniformes | Uniformes (pedidos + uniformes) | FormPedido, FormUniforme |
| Cuentas | Cuentas | Registro (+ restablecer contraseña) |

- Búsqueda y filtros funcionando en las listas.
- Cambiar contraseña (menú lateral) valida la contraseña actual.
- Se cambiaron todas las lambdas por `implements View.OnClickListener` / clases anónimas.
- Se quitó la dependencia `constraintlayout` de Gradle.

## Reglas del equipo
- Java, una Activity por vista, OnClickListener, Intent, findViewById. Sin Kotlin, Fragments, MVVM ni ViewBinding.
- XML en LinearLayout (DrawerLayout aprobado para el menú; ScrollView para desplazar). Listas con ListView.

## Sin probar
Ver la lista en el mensaje de entrega. No se compiló con Gradle ni se probó en emulador.
