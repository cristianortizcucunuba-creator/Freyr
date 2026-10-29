package com.freyr.app.data.local;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.freyr.app.data.model.*;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base de datos principal de la aplicación Freyr utilizando Room (SQLite).
 *
 * NOTA DE ARQUITECTURA SOBRE ARCHIVOS GENERADOS:
 * Esta clase es la FUENTE ORIGINAL que el compilador de Room (`room-compiler`) utiliza
 * para generar las implementaciones `FreyrDatabase_Impl.java` y `*Dao_Impl.java` dentro
 * del directorio `app/build/generated/`.
 * Nunca se deben modificar manualmente los archivos generados en `build/`, sino modificar
 * esta clase abstracta, las entidades en `com.freyr.app.data.model` o las interfaces DAO.
 */
@Database(
    entities = {Usuario.class, Grupofamiliar.class, Deuda.class, Pago.class, Invitacion.class},
    version = 1,
    exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class FreyrDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract FamilyGroupDao familyGroupDao();
    public abstract DebtDao debtDao();
    public abstract PaymentDao paymentDao();
    public abstract InvitationDao invitationDao();

    private static volatile FreyrDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    /**
     * Obtiene la instancia singleton de la base de datos Room.
     *
     * @param context Contexto de la aplicación Android.
     * @return Instancia única de {@link FreyrDatabase}.
     */
    public static FreyrDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FreyrDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        FreyrDatabase.class,
                        "freyr_database"
                    )
                    .addCallback(sRoomDatabaseCallback)
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback de creación de base de datos para sembrar datos iniciales en español.
     */
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                if (INSTANCE == null) return;
                UserDao userDao = INSTANCE.userDao();
                FamilyGroupDao groupDao = INSTANCE.familyGroupDao();
                DebtDao debtDao = INSTANCE.debtDao();
                PaymentDao paymentDao = INSTANCE.paymentDao();
                InvitationDao invitationDao = INSTANCE.invitationDao();

                // 1. Usuarios iniciales de prueba
                Usuario juan = new Usuario("user-juan", "Juan Pérez", "juan", "juan@ejemplo.com", "password123", "#69042A", "2026-01-15T10:00:00Z");
                Usuario maria = new Usuario("user-maria", "María González", "maria", "maria@ejemplo.com", "password123", "#9C27B0", "2026-01-16T11:00:00Z");
                Usuario carlos = new Usuario("user-carlos", "Carlos Rodríguez", "carlos", "carlos@ejemplo.com", "password123", "#00897B", "2026-02-01T09:30:00Z");
                userDao.insertUser(juan);
                userDao.insertUser(maria);
                userDao.insertUser(carlos);

                // 2. Grupo familiar demostrativo
                Grupofamiliar familyGroup = new Grupofamiliar(
                    "group-perez-gonzalez",
                    "Familia Pérez González",
                    juan.getId(),
                    Arrays.asList(juan.getId(), maria.getId()),
                    "2026-01-20T14:00:00Z"
                );
                groupDao.insertGroup(familyGroup);

                // 3. Deudas iniciales de ejemplo (Portátil 3M, Internet 120k, Mercado 350k, Gimnasio 360k)
                Deuda laptopDebt = new Deuda(
                    "debt-laptop",
                    "Computador portátil",
                    "Financiación a cuotas para trabajo y estudio",
                    3000000.0,
                    juan.getId(),
                    null, // Deuda personal
                    "pending",
                    "2026-02-10T12:00:00Z",
                    juan.getId()
                );
                Deuda internetDebt = new Deuda(
                    "debt-internet",
                    "Factura de internet fibra",
                    "Servicio mensual de internet fibra óptica hogar",
                    120000.0,
                    juan.getId(),
                    familyGroup.getId(), // Deuda familiar asignada a Juan
                    "pending",
                    "2026-02-15T08:30:00Z",
                    juan.getId()
                );
                Deuda groceriesDebt = new Deuda(
                    "debt-groceries",
                    "Mercado quincenal familiar",
                    "Víveres y despensa básica del supermercado",
                    350000.0,
                    null, // Toda la familia
                    familyGroup.getId(),
                    "pending",
                    "2026-02-18T16:00:00Z",
                    maria.getId()
                );
                Deuda gymDebt = new Deuda(
                    "debt-gym",
                    "Membresía anual de gimnasio",
                    "Plan de entrenamiento físico ya liquidado",
                    360000.0,
                    juan.getId(),
                    null,
                    "paid",
                    "2026-01-10T10:00:00Z",
                    juan.getId()
                );
                debtDao.insertDebt(laptopDebt);
                debtDao.insertDebt(internetDebt);
                debtDao.insertDebt(groceriesDebt);
                debtDao.insertDebt(gymDebt);

                // 4. Abonos y pagos iniciales
                paymentDao.insertPayment(new Pago(
                    "pay-laptop-1", laptopDebt.getId(), juan.getId(), 1000000.0, "2026-02-15", "Cuota inicial y primer abono", "2026-02-15T14:00:00Z"
                ));
                paymentDao.insertPayment(new Pago(
                    "pay-internet-1", internetDebt.getId(), juan.getId(), 60000.0, "2026-02-20", "Abono de primera mitad de la mensualidad", "2026-02-20T09:00:00Z"
                ));
                paymentDao.insertPayment(new Pago(
                    "pay-groceries-1", groceriesDebt.getId(), maria.getId(), 150000.0, "2026-02-22", "Compra víveres en el supermercado", "2026-02-22T17:00:00Z"
                ));
                paymentDao.insertPayment(new Pago(
                    "pay-gym-1", gymDebt.getId(), juan.getId(), 360000.0, "2026-01-12", "Pago total de membresía", "2026-01-12T11:00:00Z"
                ));

                // 5. Invitación para Carlos
                invitationDao.insertInvitation(new Invitacion(
                    "inv-carlos-1", familyGroup.getId(), carlos.getId(), juan.getId(), "pending", "2026-02-24T10:00:00Z"
                ));
            });
        }
    };
}

