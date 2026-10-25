import React, { createContext, useContext, useState, useEffect, useMemo, useCallback } from 'react';
import { User, FamilyGroup, Debt, Payment, Invitation, FinancialSummary } from '../types';
import { storage } from '../utils/storage';

interface AppContextType {
  currentUser: User | null;
  users: User[];
  familyGroups: FamilyGroup[];
  debts: Debt[];
  payments: Payment[];
  invitations: Invitation[];
  activeTab: 'dashboard' | 'personal' | 'groups' | 'invitations';
  setActiveTab: (tab: 'dashboard' | 'personal' | 'groups' | 'invitations') => void;
  
  // Auth
  login: (identifier: string, password?: string) => { success: boolean; error?: string };
  register: (data: { fullName: string; username: string; email: string; password?: string }) => { success: boolean; error?: string };
  logout: () => void;
  updateProfile: (data: { fullName: string; username: string; email: string; password?: string }) => { success: boolean; error?: string };
  switchUser: (userId: string) => void;

  // Modals
  isAddDebtModalOpen: boolean;
  addDebtInitialGroup: string | null;
  openAddDebtModal: (familyGroupId?: string | null) => void;
  closeAddDebtModal: () => void;

  isCreateGroupModalOpen: boolean;
  openCreateGroupModal: () => void;
  closeCreateGroupModal: () => void;

  isInvitationsModalOpen: boolean;
  openInvitationsModal: () => void;
  closeInvitationsModal: () => void;

  isProfileModalOpen: boolean;
  openProfileModal: () => void;
  closeProfileModal: () => void;

  isAddPaymentModalOpen: boolean;
  paymentDebtTarget: Debt | null;
  openAddPaymentModal: (debt: Debt) => void;
  closeAddPaymentModal: () => void;

  selectedDebtForDetail: Debt | null;
  openDebtDetailModal: (debt: Debt) => void;
  closeDebtDetailModal: () => void;

  // Actions
  addDebt: (data: { name: string; description?: string; amount: number; assignedUserId: string | null; familyGroupId: string | null }) => Debt;
  updateDebt: (debtId: string, updates: Partial<Debt>) => void;
  deleteDebt: (debtId: string) => void;
  addPayment: (data: { debtId: string; amount: number; date: string; description?: string }) => Payment;
  deletePayment: (paymentId: string) => void;

  createFamilyGroup: (name: string) => FamilyGroup;
  inviteUserToGroup: (familyGroupId: string, identifier: string) => { success: boolean; error?: string };
  respondToInvitation: (invitationId: string, accept: boolean) => void;

  // Helpers
  getDebtPaidAmount: (debtId: string) => number;
  getDebtRemainingAmount: (debtId: string) => number;
  getDebtPayments: (debtId: string) => Payment[];
  getSummary: () => FinancialSummary;
  getUserPendingInvitations: () => Invitation[];
  getUserFamilyGroups: () => FamilyGroup[];
  getUserName: (userId: string | null) => string;
  getUserById: (userId: string | null) => User | undefined;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [users, setUsers] = useState<User[]>(() => storage.getUsers());
  const [currentUserId, setCurrentUserId] = useState<string | null>(() => storage.getCurrentUserId());
  const [familyGroups, setFamilyGroups] = useState<FamilyGroup[]>(() => storage.getGroups());
  const [debts, setDebts] = useState<Debt[]>(() => storage.getDebts());
  const [payments, setPayments] = useState<Payment[]>(() => storage.getPayments());
  const [invitations, setInvitations] = useState<Invitation[]>(() => storage.getInvitations());
  
  const [activeTab, setActiveTab] = useState<'dashboard' | 'personal' | 'groups' | 'invitations'>('dashboard');

  // Modal states
  const [isAddDebtModalOpen, setIsAddDebtModalOpen] = useState(false);
  const [addDebtInitialGroup, setAddDebtInitialGroup] = useState<string | null>(null);

  const [isCreateGroupModalOpen, setIsCreateGroupModalOpen] = useState(false);
  const [isInvitationsModalOpen, setIsInvitationsModalOpen] = useState(false);
  const [isProfileModalOpen, setIsProfileModalOpen] = useState(false);

  const [isAddPaymentModalOpen, setIsAddPaymentModalOpen] = useState(false);
  const [paymentDebtTarget, setPaymentDebtTarget] = useState<Debt | null>(null);

  const [selectedDebtForDetail, setSelectedDebtForDetail] = useState<Debt | null>(null);

  // Sync to local storage
  useEffect(() => {
    storage.setUsers(users);
  }, [users]);

  useEffect(() => {
    storage.setCurrentUserId(currentUserId);
  }, [currentUserId]);

  useEffect(() => {
    storage.setGroups(familyGroups);
  }, [familyGroups]);

  useEffect(() => {
    storage.setDebts(debts);
  }, [debts]);

  useEffect(() => {
    storage.setPayments(payments);
  }, [payments]);

  useEffect(() => {
    storage.setInvitations(invitations);
  }, [invitations]);

  const currentUser = useMemo(() => {
    return users.find((u) => u.id === currentUserId) || null;
  }, [users, currentUserId]);

  // Métodos de autenticación
  const login = (identifier: string, password?: string) => {
    const cleanId = identifier.trim().toLowerCase();
    const user = users.find(
      (u) => u.email.toLowerCase() === cleanId || u.username.toLowerCase() === cleanId
    );
    if (!user) {
      return { success: false, error: 'Usuario no encontrado. Por favor verifica tu usuario o correo.' };
    }
    if (password && user.password && user.password !== password) {
      return { success: false, error: 'Contraseña incorrecta.' };
    }
    setCurrentUserId(user.id);
    return { success: true };
  };

  const register = (data: { fullName: string; username: string; email: string; password?: string }) => {
    const trimmedUsername = data.username.trim().toLowerCase();
    const trimmedEmail = data.email.trim().toLowerCase();
    
    if (!data.fullName.trim()) return { success: false, error: 'El nombre completo es obligatorio.' };
    if (!trimmedUsername) return { success: false, error: 'El nombre de usuario es obligatorio.' };
    if (!trimmedEmail) return { success: false, error: 'El correo electrónico es obligatorio.' };
    if (!data.password || data.password.length < 6) {
      return { success: false, error: 'La contraseña debe tener al menos 6 caracteres.' };
    }

    if (users.some((u) => u.username.toLowerCase() === trimmedUsername)) {
      return { success: false, error: 'Este nombre de usuario ya está registrado.' };
    }
    if (users.some((u) => u.email.toLowerCase() === trimmedEmail)) {
      return { success: false, error: 'Ya existe una cuenta con este correo electrónico.' };
    }

    const colors = ['#69042A', '#00897B', '#1E88E5', '#8E24AA', '#D81B60', '#3949AB'];
    const randomColor = colors[Math.floor(Math.random() * colors.length)];

    const newUser: User = {
      id: `user-${Date.now()}`,
      fullName: data.fullName.trim(),
      username: trimmedUsername,
      email: trimmedEmail,
      password: data.password,
      avatarColor: randomColor,
      createdAt: new Date().toISOString(),
    };

    setUsers((prev) => [...prev, newUser]);
    setCurrentUserId(newUser.id);
    return { success: true };
  };

  const logout = () => {
    setCurrentUserId(null);
  };

  const updateProfile = (data: { fullName: string; username: string; email: string; password?: string }) => {
    if (!currentUser) return { success: false, error: 'No autenticado' };
    const trimmedUsername = data.username.trim().toLowerCase();
    const trimmedEmail = data.email.trim().toLowerCase();

    if (!data.fullName.trim()) return { success: false, error: 'El nombre completo no puede estar vacío.' };
    if (!trimmedUsername) return { success: false, error: 'El nombre de usuario no puede estar vacío.' };
    if (!trimmedEmail) return { success: false, error: 'El correo electrónico no puede estar vacío.' };

    const usernameConflict = users.some(
      (u) => u.id !== currentUser.id && u.username.toLowerCase() === trimmedUsername
    );
    if (usernameConflict) return { success: false, error: 'El nombre de usuario ya está en uso por otra persona.' };

    const emailConflict = users.some(
      (u) => u.id !== currentUser.id && u.email.toLowerCase() === trimmedEmail
    );
    if (emailConflict) return { success: false, error: 'El correo electrónico ya está en uso por otra persona.' };

    setUsers((prev) =>
      prev.map((u) => {
        if (u.id === currentUser.id) {
          return {
            ...u,
            fullName: data.fullName.trim(),
            username: trimmedUsername,
            email: trimmedEmail,
            password: data.password || u.password,
          };
        }
        return u;
      })
    );
    return { success: true };
  };

  const switchUser = (userId: string) => {
    setCurrentUserId(userId);
  };

  // Modals triggers
  const openAddDebtModal = (familyGroupId: string | null = null) => {
    setAddDebtInitialGroup(familyGroupId);
    setIsAddDebtModalOpen(true);
  };
  const closeAddDebtModal = () => {
    setIsAddDebtModalOpen(false);
    setAddDebtInitialGroup(null);
  };

  const openCreateGroupModal = () => setIsCreateGroupModalOpen(true);
  const closeCreateGroupModal = () => setIsCreateGroupModalOpen(false);

  const openInvitationsModal = () => setIsInvitationsModalOpen(true);
  const closeInvitationsModal = () => setIsInvitationsModalOpen(false);

  const openProfileModal = () => setIsProfileModalOpen(true);
  const closeProfileModal = () => setIsProfileModalOpen(false);

  const openAddPaymentModal = (debt: Debt) => {
    setPaymentDebtTarget(debt);
    setIsAddPaymentModalOpen(true);
  };
  const closeAddPaymentModal = () => {
    setIsAddPaymentModalOpen(false);
    setPaymentDebtTarget(null);
  };

  const openDebtDetailModal = (debt: Debt) => {
    setSelectedDebtForDetail(debt);
  };
  const closeDebtDetailModal = () => {
    setSelectedDebtForDetail(null);
  };

  // Helpers
  const getDebtPayments = useCallback(
    (debtId: string) => {
      return payments.filter((p) => p.debtId === debtId).sort(
        (a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()
      );
    },
    [payments]
  );

  const getDebtPaidAmount = useCallback(
    (debtId: string) => {
      return payments
        .filter((p) => p.debtId === debtId)
        .reduce((sum, p) => sum + p.amount, 0);
    },
    [payments]
  );

  const getDebtRemainingAmount = useCallback(
    (debtId: string) => {
      const debt = debts.find((d) => d.id === debtId);
      if (!debt) return 0;
      const paid = getDebtPaidAmount(debtId);
      return Math.max(0, debt.amount - paid);
    },
    [debts, getDebtPaidAmount]
  );

  // Debts operations
  const addDebt = (data: {
    name: string;
    description?: string;
    amount: number;
    assignedUserId: string | null;
    familyGroupId: string | null;
  }) => {
    if (!currentUser) throw new Error('Not authenticated');

    const newDebt: Debt = {
      id: `debt-${Date.now()}-${Math.random().toString(36).substr(2, 5)}`,
      name: data.name.trim(),
      description: data.description?.trim(),
      amount: Number(data.amount),
      assignedUserId: data.assignedUserId,
      familyGroupId: data.familyGroupId,
      status: 'pending',
      createdAt: new Date().toISOString(),
      createdBy: currentUser.id,
    };

    setDebts((prev) => [newDebt, ...prev]);
    return newDebt;
  };

  const updateDebt = (debtId: string, updates: Partial<Debt>) => {
    setDebts((prev) =>
      prev.map((d) => (d.id === debtId ? { ...d, ...updates } : d))
    );
  };

  const deleteDebt = (debtId: string) => {
    setDebts((prev) => prev.filter((d) => d.id !== debtId));
    setPayments((prev) => prev.filter((p) => p.debtId !== debtId));
    if (selectedDebtForDetail?.id === debtId) {
      setSelectedDebtForDetail(null);
    }
  };

  // Payments operations
  const addPayment = (data: {
    debtId: string;
    amount: number;
    date: string;
    description?: string;
  }) => {
    if (!currentUser) throw new Error('Not authenticated');

    const newPayment: Payment = {
      id: `pay-${Date.now()}-${Math.random().toString(36).substr(2, 5)}`,
      debtId: data.debtId,
      userId: currentUser.id,
      amount: Number(data.amount),
      date: data.date,
      description: data.description?.trim(),
      createdAt: new Date().toISOString(),
    };

    const targetDebt = debts.find((d) => d.id === data.debtId);
    if (targetDebt) {
      const currentPaid = getDebtPaidAmount(targetDebt.id);
      const newTotalPaid = currentPaid + newPayment.amount;
      if (newTotalPaid >= targetDebt.amount) {
        updateDebt(targetDebt.id, { status: 'paid' });
      }
    }

    setPayments((prev) => [newPayment, ...prev]);
    return newPayment;
  };

  const deletePayment = (paymentId: string) => {
    const pay = payments.find((p) => p.id === paymentId);
    if (!pay) return;
    
    const remainingPayments = payments.filter((p) => p.id !== paymentId);
    setPayments(remainingPayments);

    // Recheck debt status
    const targetDebt = debts.find((d) => d.id === pay.debtId);
    if (targetDebt) {
      const newPaid = remainingPayments
        .filter((p) => p.debtId === targetDebt.id)
        .reduce((sum, p) => sum + p.amount, 0);
      if (newPaid < targetDebt.amount) {
        updateDebt(targetDebt.id, { status: 'pending' });
      }
    }
  };

  // Family Group operations
  const createFamilyGroup = (name: string) => {
    if (!currentUser) throw new Error('Not authenticated');

    const newGroup: FamilyGroup = {
      id: `group-${Date.now()}-${Math.random().toString(36).substr(2, 5)}`,
      name: name.trim(),
      creatorId: currentUser.id,
      memberIds: [currentUser.id],
      createdAt: new Date().toISOString(),
    };

    setFamilyGroups((prev) => [...prev, newGroup]);
    return newGroup;
  };

  const inviteUserToGroup = (familyGroupId: string, identifier: string) => {
    if (!currentUser) return { success: false, error: 'No autenticado' };

    const group = familyGroups.find((g) => g.id === familyGroupId);
    if (!group) return { success: false, error: 'Grupo familiar no encontrado.' };

    const clean = identifier.trim().toLowerCase();
    const targetUser = users.find(
      (u) => u.email.toLowerCase() === clean || u.username.toLowerCase() === clean
    );

    if (!targetUser) {
      return { success: false, error: `No se encontró ningún usuario con nombre de usuario o correo "${identifier}".` };
    }

    if (group.memberIds.includes(targetUser.id)) {
      return { success: false, error: `${targetUser.fullName} ya es miembro de este grupo.` };
    }

    // Comprueba si ya existe una invitación pendiente para este usuario
    const alreadyInvited = invitations.some(
      (inv) =>
        inv.familyGroupId === familyGroupId &&
        inv.invitedUserId === targetUser.id &&
        inv.status === 'pending'
    );
    if (alreadyInvited) {
      return { success: false, error: `Ya se ha enviado una invitación pendiente a ${targetUser.fullName}.` };
    }

    const newInvitation: Invitation = {
      id: `inv-${Date.now()}-${Math.random().toString(36).substr(2, 5)}`,
      familyGroupId,
      invitedUserId: targetUser.id,
      invitedByUserId: currentUser.id,
      status: 'pending',
      createdAt: new Date().toISOString(),
    };

    setInvitations((prev) => [...prev, newInvitation]);
    return { success: true };
  };

  const respondToInvitation = (invitationId: string, accept: boolean) => {
    const invite = invitations.find((i) => i.id === invitationId);
    if (!invite) return;

    if (accept) {
      // Add user to family group members
      setFamilyGroups((prev) =>
        prev.map((g) => {
          if (g.id === invite.familyGroupId) {
            const currentMembers = g.memberIds || [];
            if (!currentMembers.includes(invite.invitedUserId)) {
              return { ...g, memberIds: [...currentMembers, invite.invitedUserId] };
            }
          }
          return g;
        })
      );
    }

    setInvitations((prev) =>
      prev.map((i) =>
        i.id === invitationId ? { ...i, status: accept ? 'accepted' : 'declined' } : i
      )
    );
  };

  // Group helpers
  const getUserFamilyGroups = useCallback(() => {
    if (!currentUser) return [];
    return familyGroups.filter((g) => g.memberIds.includes(currentUser.id));
  }, [currentUser, familyGroups]);

  const getUserPendingInvitations = useCallback(() => {
    if (!currentUser) return [];
    return invitations.filter(
      (inv) => inv.invitedUserId === currentUser.id && inv.status === 'pending'
    );
  }, [currentUser, invitations]);

  const getUserName = useCallback(
    (userId: string | null) => {
      if (!userId) return 'Toda la familia';
      const u = users.find((user) => user.id === userId);
      return u ? u.fullName : 'Usuario desconocido';
    },
    [users]
  );

  const getUserById = useCallback(
    (userId: string | null) => {
      if (!userId) return undefined;
      return users.find((user) => user.id === userId);
    },
    [users]
  );

  // Financial Summary calculation
  const getSummary = useCallback((): FinancialSummary => {
    if (!currentUser) {
      return {
        pendingDebtsCount: 0,
        paidDebtsCount: 0,
        personalPaymentsTotal: 0,
        familyDebtsPendingTotal: 0,
        totalPendingAmount: 0,
        totalPaidAmount: 0,
      };
    }

    // 1. User's personal debts (familyGroupId === null && (assignedUserId === currentUser.id || createdBy === currentUser.id))
    const personalDebts = debts.filter(
      (d) =>
        d.familyGroupId === null &&
        (d.assignedUserId === currentUser.id || d.createdBy === currentUser.id)
    );

    // 2. Family debts the user is involved in (user is a member of the group)
    const userGroupIds = familyGroups
      .filter((g) => g.memberIds.includes(currentUser.id))
      .map((g) => g.id);

    const familyDebts = debts.filter(
      (d) => d.familyGroupId !== null && userGroupIds.includes(d.familyGroupId)
    );

    // Debts assigned to user or personal
    const allRelevantDebts = debts.filter((d) => {
      if (d.familyGroupId === null) {
        return d.assignedUserId === currentUser.id || d.createdBy === currentUser.id;
      }
      return userGroupIds.includes(d.familyGroupId);
    });

    let pendingDebtsCount = 0;
    let paidDebtsCount = 0;
    let totalPendingAmount = 0;
    let totalPaidAmount = 0;

    allRelevantDebts.forEach((debt) => {
      const paid = getDebtPaidAmount(debt.id);
      const remaining = Math.max(0, debt.amount - paid);
      if (remaining === 0 || debt.status === 'paid') {
        paidDebtsCount += 1;
        totalPaidAmount += debt.amount;
      } else {
        pendingDebtsCount += 1;
        totalPendingAmount += remaining;
        totalPaidAmount += paid;
      }
    });

    // Personal payments made by the current user
    const personalPaymentsTotal = payments
      .filter((p) => p.userId === currentUser.id)
      .reduce((sum, p) => sum + p.amount, 0);

    // Family debts pending total
    const familyDebtsPendingTotal = familyDebts.reduce((sum, d) => {
      const remaining = getDebtRemainingAmount(d.id);
      return sum + remaining;
    }, 0);

    return {
      pendingDebtsCount,
      paidDebtsCount,
      personalPaymentsTotal,
      familyDebtsPendingTotal,
      totalPendingAmount,
      totalPaidAmount,
    };
  }, [currentUser, debts, familyGroups, payments, getDebtPaidAmount, getDebtRemainingAmount]);

  return (
    <AppContext.Provider
      value={{
        currentUser,
        users,
        familyGroups,
        debts,
        payments,
        invitations,
        activeTab,
        setActiveTab,

        login,
        register,
        logout,
        updateProfile,
        switchUser,

        isAddDebtModalOpen,
        addDebtInitialGroup,
        openAddDebtModal,
        closeAddDebtModal,

        isCreateGroupModalOpen,
        openCreateGroupModal,
        closeCreateGroupModal,

        isInvitationsModalOpen,
        openInvitationsModal,
        closeInvitationsModal,

        isProfileModalOpen,
        openProfileModal,
        closeProfileModal,

        isAddPaymentModalOpen,
        paymentDebtTarget,
        openAddPaymentModal,
        closeAddPaymentModal,

        selectedDebtForDetail,
        openDebtDetailModal,
        closeDebtDetailModal,

        addDebt,
        updateDebt,
        deleteDebt,
        addPayment,
        deletePayment,

        createFamilyGroup,
        inviteUserToGroup,
        respondToInvitation,

        getDebtPaidAmount,
        getDebtRemainingAmount,
        getDebtPayments,
        getSummary,
        getUserPendingInvitations,
        getUserFamilyGroups,
        getUserName,
        getUserById,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) throw new Error('useApp must be used within an AppProvider');
  return context;
};
