import { User, FamilyGroup, Debt, Payment, Invitation } from '../types';

const STORAGE_KEYS = {
  USERS: 'komorebi_users_v1',
  CURRENT_USER_ID: 'komorebi_current_user_id_v1',
  GROUPS: 'komorebi_groups_v1',
  DEBTS: 'komorebi_debts_v1',
  PAYMENTS: 'komorebi_payments_v1',
  INVITATIONS: 'komorebi_invitations_v1',
};

const INITIAL_USERS: User[] = [
  {
    id: 'user-juan',
    fullName: 'Juan Pérez',
    username: 'juan',
    email: 'juan@example.com',
    password: 'password123',
    avatarColor: '#69042A',
    createdAt: '2026-01-15T10:00:00Z',
  },
  {
    id: 'user-maria',
    fullName: 'María González',
    username: 'maria',
    email: 'maria@example.com',
    password: 'password123',
    avatarColor: '#9C27B0',
    createdAt: '2026-01-16T11:00:00Z',
  },
  {
    id: 'user-carlos',
    fullName: 'Carlos Rodríguez',
    username: 'carlos',
    email: 'carlos@example.com',
    password: 'password123',
    avatarColor: '#00897B',
    createdAt: '2026-02-01T09:30:00Z',
  },
];

const INITIAL_GROUPS: FamilyGroup[] = [
  {
    id: 'group-perez-gonzalez',
    name: 'Familia Pérez González',
    creatorId: 'user-juan',
    memberIds: ['user-juan', 'user-maria'],
    createdAt: '2026-01-20T14:00:00Z',
  },
];

const INITIAL_DEBTS: Debt[] = [
  {
    id: 'debt-laptop',
    name: 'Portátil de trabajo',
    description: 'Financiación mensual de equipo de trabajo y estudio',
    amount: 3000000,
    assignedUserId: 'user-juan',
    familyGroupId: null, // Deuda personal de Juan
    status: 'pending',
    createdAt: '2026-02-10T12:00:00Z',
    createdBy: 'user-juan',
  },
  {
    id: 'debt-internet',
    name: 'Factura de Internet',
    description: 'Servicio mensual de fibra óptica de alta velocidad',
    amount: 120000,
    assignedUserId: 'user-juan',
    familyGroupId: 'group-perez-gonzalez', // Deuda familiar asignada a Juan
    status: 'pending',
    createdAt: '2026-02-15T08:30:00Z',
    createdBy: 'user-juan',
  },
  {
    id: 'debt-groceries',
    name: 'Mercado quincenal',
    description: 'Compras del supermercado y despensa general',
    amount: 350000,
    assignedUserId: null, // Deuda colectiva de toda la familia
    familyGroupId: 'group-perez-gonzalez',
    status: 'pending',
    createdAt: '2026-02-18T16:00:00Z',
    createdBy: 'user-maria',
  },
  {
    id: 'debt-gym',
    name: 'Membresía del gimnasio',
    description: 'Acceso anual a instalaciones de entrenamiento (liquidada)',
    amount: 360000,
    assignedUserId: 'user-juan',
    familyGroupId: null, // Deuda personal
    status: 'paid',
    createdAt: '2026-01-10T10:00:00Z',
    createdBy: 'user-juan',
  },
];

const INITIAL_PAYMENTS: Payment[] = [
  {
    id: 'pay-laptop-1',
    debtId: 'debt-laptop',
    userId: 'user-juan',
    amount: 1000000,
    date: '2026-02-15',
    description: 'Depósito inicial y primera cuota',
    createdAt: '2026-02-15T14:00:00Z',
  },
  {
    id: 'pay-internet-1',
    debtId: 'debt-internet',
    userId: 'user-juan',
    amount: 60000,
    date: '2026-02-20',
    description: 'Primer abono del 50%',
    createdAt: '2026-02-20T09:00:00Z',
  },
  {
    id: 'pay-groceries-1',
    debtId: 'debt-groceries',
    userId: 'user-maria',
    amount: 150000,
    date: '2026-02-22',
    description: 'Abono de frutas, verduras y carnes',
    createdAt: '2026-02-22T17:00:00Z',
  },
  {
    id: 'pay-gym-1',
    debtId: 'debt-gym',
    userId: 'user-juan',
    amount: 360000,
    date: '2026-01-12',
    description: 'Pago total anticipado con descuento',
    createdAt: '2026-01-12T11:00:00Z',
  },
];

const INITIAL_INVITATIONS: Invitation[] = [
  {
    id: 'inv-carlos-1',
    familyGroupId: 'group-perez-gonzalez',
    invitedUserId: 'user-carlos',
    invitedByUserId: 'user-juan',
    status: 'pending',
    createdAt: '2026-02-24T10:00:00Z',
  },
];

/**
 * Capa de abstracción para persistencia en el almacenamiento local (localStorage).
 * Proporciona métodos de lectura y escritura para usuarios, deudas, abonos, grupos e invitaciones.
 */
export const storage = {
  /** Obtiene la lista de usuarios registrados o inicializa con los predeterminados */
  getUsers: (): User[] => {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.USERS);
      if (!data) {
        localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(INITIAL_USERS));
        return INITIAL_USERS;
      }
      return JSON.parse(data);
    } catch {
      return INITIAL_USERS;
    }
  },

  /** Guarda la lista de usuarios en almacenamiento local */
  setUsers: (users: User[]) => {
    localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(users));
  },

  /** Obtiene el identificador del usuario autenticado actualmente */
  getCurrentUserId: (): string | null => {
    return localStorage.getItem(STORAGE_KEYS.CURRENT_USER_ID) || 'user-juan';
  },

  /** Actualiza o borra el usuario con sesión activa */
  setCurrentUserId: (id: string | null) => {
    if (id) {
      localStorage.setItem(STORAGE_KEYS.CURRENT_USER_ID, id);
    } else {
      localStorage.removeItem(STORAGE_KEYS.CURRENT_USER_ID);
    }
  },

  /** Obtiene los grupos familiares existentes o inicializa con los predeterminados */
  getGroups: (): FamilyGroup[] => {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.GROUPS);
      if (!data) {
        localStorage.setItem(STORAGE_KEYS.GROUPS, JSON.stringify(INITIAL_GROUPS));
        return INITIAL_GROUPS;
      }
      return JSON.parse(data);
    } catch {
      return INITIAL_GROUPS;
    }
  },

  /** Persiste los grupos familiares en almacenamiento local */
  setGroups: (groups: FamilyGroup[]) => {
    localStorage.setItem(STORAGE_KEYS.GROUPS, JSON.stringify(groups));
  },

  /** Obtiene todas las deudas registradas */
  getDebts: (): Debt[] => {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.DEBTS);
      if (!data) {
        localStorage.setItem(STORAGE_KEYS.DEBTS, JSON.stringify(INITIAL_DEBTS));
        return INITIAL_DEBTS;
      }
      return JSON.parse(data);
    } catch {
      return INITIAL_DEBTS;
    }
  },

  /** Persiste las deudas en almacenamiento local */
  setDebts: (debts: Debt[]) => {
    localStorage.setItem(STORAGE_KEYS.DEBTS, JSON.stringify(debts));
  },

  /** Obtiene el historial completo de abonos y pagos */
  getPayments: (): Payment[] => {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.PAYMENTS);
      if (!data) {
        localStorage.setItem(STORAGE_KEYS.PAYMENTS, JSON.stringify(INITIAL_PAYMENTS));
        return INITIAL_PAYMENTS;
      }
      return JSON.parse(data);
    } catch {
      return INITIAL_PAYMENTS;
    }
  },

  /** Persiste los pagos en almacenamiento local */
  setPayments: (payments: Payment[]) => {
    localStorage.setItem(STORAGE_KEYS.PAYMENTS, JSON.stringify(payments));
  },

  /** Obtiene todas las invitaciones de grupos familiares */
  getInvitations: (): Invitation[] => {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.INVITATIONS);
      if (!data) {
        localStorage.setItem(STORAGE_KEYS.INVITATIONS, JSON.stringify(INITIAL_INVITATIONS));
        return INITIAL_INVITATIONS;
      }
      return JSON.parse(data);
    } catch {
      return INITIAL_INVITATIONS;
    }
  },

  /** Persiste las invitaciones en almacenamiento local */
  setInvitations: (invitations: Invitation[]) => {
    localStorage.setItem(STORAGE_KEYS.INVITATIONS, JSON.stringify(invitations));
  },

  /** Restablece todos los datos de la aplicación a su estado de demostración inicial */
  resetToDefault: () => {
    localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(INITIAL_USERS));
    localStorage.setItem(STORAGE_KEYS.GROUPS, JSON.stringify(INITIAL_GROUPS));
    localStorage.setItem(STORAGE_KEYS.DEBTS, JSON.stringify(INITIAL_DEBTS));
    localStorage.setItem(STORAGE_KEYS.PAYMENTS, JSON.stringify(INITIAL_PAYMENTS));
    localStorage.setItem(STORAGE_KEYS.INVITATIONS, JSON.stringify(INITIAL_INVITATIONS));
    localStorage.setItem(STORAGE_KEYS.CURRENT_USER_ID, 'user-juan');
  },
};

