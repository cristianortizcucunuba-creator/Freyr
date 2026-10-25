/**
 * Representa a un usuario registrado en la plataforma Freyr.
 */
export interface User {
  /** Identificador único del usuario (ej. 'user-juan') */
  id: string;
  /** Nombre completo del usuario */
  fullName: string;
  /** Nombre de usuario único para autenticación y menciones */
  username: string;
  /** Correo electrónico del usuario */
  email: string;
  /** Contraseña en texto plano para el entorno de demostración */
  password?: string;
  /** Color hexadecimal asignado al avatar visual del usuario */
  avatarColor: string;
  /** Marca temporal ISO de creación de la cuenta */
  createdAt: string;
}

/**
 * Representa un grupo familiar para compartir finanzas y deudas colectivas.
 */
export interface FamilyGroup {
  /** Identificador único del grupo */
  id: string;
  /** Nombre descriptivo del grupo familiar (ej. 'Familia Pérez González') */
  name: string;
  /** Identificador del usuario que creó y administra el grupo */
  creatorId: string;
  /** Lista de identificadores de los usuarios pertenecientes al grupo */
  memberIds: string[];
  /** Marca temporal ISO de creación del grupo */
  createdAt: string;
}

/**
 * Representa una obligación financiera o deuda, ya sea de carácter personal o familiar.
 */
export interface Debt {
  /** Identificador único de la deuda */
  id: string;
  /** Título o concepto principal de la deuda (ej. 'Portátil de trabajo') */
  name: string;
  /** Descripción detallada o notas adicionales opcionales */
  description?: string;
  /** Importe total monetario de la deuda */
  amount: number;
  /**
   * Identificador del usuario asignado como responsable.
   * Si es `null`, la deuda pertenece a la familia entera en partes iguales.
   * Si `familyGroupId` es `null`, corresponde al propietario de la deuda personal.
   */
  assignedUserId: string | null;
  /**
   * Identificador del grupo familiar al que está asociada la deuda.
   * Un valor `null` indica que es una deuda puramente personal.
   */
  familyGroupId: string | null;
  /** Estado de liquidación de la deuda: 'pending' (pendiente) o 'paid' (pagada) */
  status: 'pending' | 'paid';
  /** Marca temporal ISO de creación del registro */
  createdAt: string;
  /** Identificador del usuario que registró la deuda */
  createdBy: string;
}

/**
 * Representa un pago parcial o abono aplicado a una deuda específica.
 */
export interface Payment {
  /** Identificador único del pago */
  id: string;
  /** Identificador de la deuda a la cual se abona este importe */
  debtId: string;
  /** Identificador del usuario que realizó el desembolso */
  userId: string;
  /** Importe monetario abonado */
  amount: number;
  /** Fecha en formato YYYY-MM-DD en la que se efectuó el pago */
  date: string;
  /** Nota o descripción opcional del abono */
  description?: string;
  /** Marca temporal ISO de registro del pago en el sistema */
  createdAt: string;
}

/**
 * Representa una invitación enviada a un usuario para unirse a un grupo familiar.
 */
export interface Invitation {
  /** Identificador único de la invitación */
  id: string;
  /** Identificador del grupo familiar destino */
  familyGroupId: string;
  /** Identificador del usuario al que se le envió la invitación */
  invitedUserId: string;
  /** Identificador del usuario remitente de la invitación */
  invitedByUserId: string;
  /** Estado actual de la invitación: 'pending', 'accepted' o 'declined' */
  status: 'pending' | 'accepted' | 'declined';
  /** Marca temporal ISO de creación de la invitación */
  createdAt: string;
}

/**
 * Métricas agregadas y resumen financiero del usuario activo.
 */
export interface FinancialSummary {
  /** Cantidad de deudas pendientes asociadas al usuario */
  pendingDebtsCount: number;
  /** Cantidad de deudas completamente saldadas */
  paidDebtsCount: number;
  /** Suma acumulada de los abonos personales realizados por el usuario */
  personalPaymentsTotal: number;
  /** Suma acumulada del saldo pendiente en deudas familiares vinculadas */
  familyDebtsPendingTotal: number;
  /** Monto total monetario actualmente pendiente de liquidación */
  totalPendingAmount: number;
  /** Monto total monetario acumulado que ya ha sido pagado */
  totalPaidAmount: number;
}
