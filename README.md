# Freyr — Sistema de Gestión Financiera Personal y Familiar

**Freyr** es una plataforma moderna y minimalista para el control y la gestión integral de finanzas personales y familiares. Cuenta con dos implementaciones sincronizadas bajo los mismos principios de diseño y modelo de dominio:

1. **Cliente Web SPA**: Desarrollado con **React 19**, **TypeScript**, **Vite** y **Tailwind CSS**.
2. **Aplicación Móvil Nativa**: Desarrollada para **Android** utilizando **Java 17 puro**, **Material3**, **Room Database (SQLite)** y layouts **XML**.

---

## Paleta de Colores de Marca
- **Color Primario:** `#69042A` (Burdeos elegante / vino tinto profundo)
- **Color Secundario / Fondo de realce:** `#E1CCD4` (Rosa suave pastel)
- **Fondo General:** `#FDFBF7` / `#FAFAFA`
- **Estados:** Verde esmeralda (`#10B981` / `#2E7D32`) para completado/pagado y Ámbar (`#F59E0B` / `#ED6C02`) para pendiente.

---

## Arquitectura del Proyecto

El repositorio está estructurado para permitir tanto el desarrollo web moderno como la compilación nativa en Android:

```
Freyr/
├── index.html                         # Punto de entrada HTML (SPA en español)
├── package.json                       # Dependencias y scripts de la aplicación Web
├── vite.config.ts                     # Configuración de compilación Vite y Tailwind
├── tsconfig.json                      # Configuración de TypeScript
├── metadata.json                      # Metadatos del applet en español
├── src/                               # Código Fuente Web (React 19 + TypeScript)
│   ├── main.tsx                       # Montaje de la aplicación React
│   ├── App.tsx                        # Componente raíz y enrutamiento por pestañas
│   ├── index.css                      # Estilos globales con Tailwind CSS
│   ├── types/
│   │   └── index.ts                   # Interfaces TypeScript y tipos del modelo
│   ├── utils/
│   │   ├── currency.ts                # Formateo monetario en español (es-ES)
│   │   └── storage.ts                 # Persistencia en LocalStorage y datos semilla
│   ├── context/
│   │   └── AppContext.tsx             # Estado global, validaciones y operaciones CRUD
│   └── components/
│       ├── Navbar.tsx                 # Barra superior y selector de usuario
│       ├── Dashboard.tsx              # Resumen financiero, métricas y accesos rápidos
│       ├── PersonalFinances.tsx       # Listado y filtros de deudas individuales
│       ├── FamilyGroups.tsx           # Gestión de grupos familiares y deudas colectivas
│       ├── InvitationsView.tsx        # Bandeja de invitaciones a grupos
│       ├── InvitationsModal.tsx       # Diálogo modal de invitaciones
│       ├── AddDebtModal.tsx           # Creación de deudas personales o familiares
│       ├── AddPaymentModal.tsx        # Registro de abonos y amortizaciones
│       ├── DebtDetailModal.tsx        # Historial de pagos y progreso de amortización
│       ├── CreateGroupModal.tsx       # Creación de nuevos grupos familiares
│       ├── ProfileModal.tsx           # Edición de perfil de usuario
│       └── AuthView.tsx               # Inicio de sesión y registro de usuarios
│
├── settings.gradle                    # Configuración de proyectos y repositorios Gradle
├── build.gradle                       # Configuración raíz del build de Gradle
├── gradle.properties                  # Propiedades de la JVM y compatibilidad AndroidX
├── gradlew / gradlew.bat              # Wrappers ejecutables de Gradle
├── gradle/wrapper/                    # Binarios del wrapper de Gradle
└── app/                               # Módulo de Aplicación Android Nativa (Java 17)
    ├── build.gradle                   # Configuración del módulo, dependencias y procesadores
    ├── proguard-rules.pro             # Reglas de ofuscación y optimización Proguard
    └── src/main/
        ├── AndroidManifest.xml        # Manifiesto de Android con Activities registradas
        ├── res/
        │   ├── layout/                # Layouts XML enlazados mediante ViewBinding
        │   │   ├── activity_main.xml
        │   │   ├── activity_login.xml
        │   │   ├── activity_register.xml
        │   │   ├── activity_add_debt.xml
        │   │   ├── activity_debt_detail.xml
        │   │   ├── activity_add_payment.xml
        │   │   ├── activity_create_family_group.xml
        │   │   ├── activity_family_group_detail.xml
        │   │   ├── activity_edit_profile.xml
        │   │   ├── fragment_dashboard.xml
        │   │   ├── fragment_personal_finances.xml
        │   │   ├── fragment_family_groups.xml
        │   │   ├── fragment_invitations.xml
        │   │   ├── item_debt.xml
        │   │   ├── item_payment.xml
        │   │   ├── item_family_group.xml
        │   │   └── item_invitation.xml
        │   ├── values/
        │   │   ├── strings.xml        # Catálogo centralizado de cadenas en español
        │   │   ├── colors.xml         # Colores del tema (freyr_primary, freyr_secondary)
        │   │   └── themes.xml         # Tema Material3 adaptado
        │   ├── menu/
        │   │   └── bottom_nav_menu.xml# Menú de navegación inferior (Inicio, Personal, Familia, Invitaciones)
        │   └── drawable/              # Recursos gráficos vectoriales, formas y bordes
        └── java/com/freyr/app/
            ├── FreyrApplication.java  # Clase Application para inicialización del repositorio
            ├── data/
            │   ├── model/             # Entidades de datos (User, FamilyGroup, Debt, Payment, Invitation)
            │   ├── local/             # Capa SQLite Room (DAOs, Converters, FreyrDatabase)
            │   └── repository/        # Repositorio thread-safe con LiveData reactivo
            └── ui/                    # Capa de presentación (Activities, Fragments, Adapters)
```

---

## Resolución de Archivos Generados (`build/`)

### ¿Por qué aparecen advertencias sobre archivos generados?
En el flujo de desarrollo de Android y Gradle, herramientas como:
1. **ViewBinding** (`buildFeatures { viewBinding true }`): Lee los archivos XML en `app/src/main/res/layout/` y genera clases Java en `app/build/generated/data_binding_base_class_source_out/` (ej. `ActivityAddDebtBinding.java`).
2. **Room Compiler** (`androidx.room:room-compiler:2.6.1`): Procesa las anotaciones `@Database`, `@Dao` y `@Entity` y genera implementaciones en `app/build/generated/ap_generated_sources/` (ej. `FreyrDatabase_Impl.java`, `UserDao_Impl.java`).

Ambas herramientas inyectan comentarios de advertencia en sus cabeceras:
> *"Generated source files should not be edited. The changes will be lost when sources are regenerated."*  
> *"Files under the 'build' folder are generated and should not be edited."*

### Solución aplicada desde el origen:
- **Cero edición manual en `build/`**: Ningún archivo bajo carpetas generadas o compiladas fue editado de forma manual.
- **Modificación en las fuentes canónicas**:
  - Todos los textos de la interfaz se extrajeron a `app/src/main/res/values/strings.xml`.
  - Los layouts en `app/src/main/res/layout/*.xml` se actualizaron para referenciar `@string/...`.
  - Los datos semilla de Room se configuraron en español dentro de `FreyrDatabase.java`.
- **Configuración estricta de `.gitignore`**:
  - Se añadieron directivas completas para excluir `build/`, `**/build/`, `.gradle/`, `.idea/` y `dist/`.
  - Cuando se ejecuta `./gradlew build` o `npm run build`, los archivos generados se reconstruyen limpiamente a partir de las fuentes en español sin riesgo de sobrescritura accidental.

---

## Instrucciones de Compilación y Ejecución

### 1. Aplicación Web (React + TypeScript)
```bash
# Instalar dependencias
npm install

# Iniciar servidor de desarrollo en http://localhost:3000
npm run dev

# Compilar para producción (genera bundle optimizado en /dist)
npm run build

# Validar tipado y sintaxis (Linter)
npm run lint
```

### 2. Aplicación Móvil (Android Studio)
1. Abrir **Android Studio** (Hedgehog, Iguana, Ladybug, Meerkat o superior).
2. Seleccionar **Open** y elegir el directorio raíz del proyecto (`Freyr/`).
3. El IDE sincronizará las dependencias mediante Gradle de forma automática.
4. Seleccionar un emulador Android (API 24 o superior) o un dispositivo físico con depuración USB habilitada.
5. Hacer clic en **Run 'app'** (`Shift + F10`).

---

## Cuentas de Demostración Preconfiguradas

Tanto en la versión Web como en Android se incluyen tres usuarios de prueba para verificar la colaboración y los grupos familiares:

| Nombre Completo | Nombre de Usuario | Correo Electrónico | Contraseña | Rol de Prueba |
| :--- | :--- | :--- | :--- | :--- |
| **Juan Pérez** | `juan` | `juan@example.com` | `123456` | Administrador / Creador familiar |
| **María González** | `maria` | `maria@example.com` | `123456` | Miembro familiar |
| **Carlos Rodríguez** | `carlos` | `carlos@example.com` | `123456` | Usuario con deudas independientes |

---

## Módulos y Funcionalidades

1. **Panel de Control (Dashboard)**:
   - Resumen financiero completo: Deuda Total Histórica, Total Pagado y Saldo Pendiente.
   - Indicadores de deudas pendientes, deudas liquidadas, pagos registrados y compromisos compartidos.
   - Acceso directo a las 4 acciones principales: Editar Perfil, Añadir Deuda, Crear Grupo Familiar y Aceptar Invitaciones.
2. **Finanzas Personales**:
   - Registro de gastos y compromisos financieros privados.
   - Filtro interactivo por estado: Todas, Pendientes y Pagadas.
   - Seguimiento visual de amortización mediante barras de progreso en tiempo real.
3. **Grupos Familiares**:
   - Creación de grupos colaborativos con múltiples miembros.
   - Asignación de deudas a miembros específicos o al colectivo general.
   - Visualización de totales grupales, abonos acumulados y saldos pendientes.
4. **Sistema de Invitaciones**:
   - Envío de invitaciones a usuarios registrados por nombre de usuario.
   - Bandeja interactiva con opciones de aceptación o rechazo inmediato.
5. **Abonos y Amortizaciones**:
   - Registro de pagos parciales o totales con selector de fecha y nota explicativa.
   - Recálculo automático de saldos y transición a estado **Pagado** al liquidar la totalidad.
   - Opción para revertir o eliminar abonos erróneos con actualización inmediata de saldos.
