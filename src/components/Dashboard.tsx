import React from 'react';
import { useApp } from '../context/AppContext';
import { formatCurrency, formatDate } from '../utils/currency';
import {
  UserCheck,
  PlusCircle,
  Users,
  Bell,
  Clock,
  CheckCircle2,
  DollarSign,
  ArrowUpRight,
  TrendingDown,
  CreditCard,
  ChevronRight,
  Wallet
} from 'lucide-react';

export const Dashboard: React.FC = () => {
  const {
    currentUser,
    getSummary,
    openProfileModal,
    openAddDebtModal,
    openCreateGroupModal,
    openInvitationsModal,
    getUserPendingInvitations,
    debts,
    familyGroups,
    getDebtRemainingAmount,
    getDebtPaidAmount,
    openAddPaymentModal,
    openDebtDetailModal,
    payments,
    getUserName,
    setActiveTab,
  } = useApp();

  if (!currentUser) return null;

  const summary = getSummary();
  const pendingInvitations = getUserPendingInvitations();

  // Active pending debts relevant to this user
  const userGroupIds = familyGroups
    .filter((g) => g.memberIds.includes(currentUser.id))
    .map((g) => g.id);

  const activePendingDebts = debts
    .filter((d) => {
      const isRelevant =
        (d.familyGroupId === null &&
          (d.assignedUserId === currentUser.id || d.createdBy === currentUser.id)) ||
        (d.familyGroupId !== null && userGroupIds.includes(d.familyGroupId));
      const remaining = getDebtRemainingAmount(d.id);
      return isRelevant && remaining > 0 && d.status === 'pending';
    })
    .slice(0, 4);

  // Recent payment transactions
  const recentPayments = [...payments]
    .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
    .slice(0, 4);

  return (
    <div className="space-y-8 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Encabezado de bienvenida y estado */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-[#69042A]">
            Resumen Financiero
          </h1>
          <p className="text-sm text-[#2D1B22]/70 mt-1">
            Bienvenido de nuevo, <span className="font-semibold text-[#2D1B22]">{currentUser.fullName}</span>. Este es el estado consolidado de tus finanzas.
          </p>
        </div>
      </div>

      {/* Las 4 Acciones Principales */}
      <section className="space-y-3">
        <h2 className="text-xs font-bold uppercase tracking-wider text-[#2D1B22]/60">
          Acciones Principales
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Opción 1: Editar perfil */}
          <button
            onClick={openProfileModal}
            className="group p-5 bg-white rounded-2xl border border-[#E1CCD4] hover:border-[#69042A] shadow-xs hover:shadow-md transition-all text-left flex items-start justify-between cursor-pointer"
          >
            <div className="space-y-2">
              <div className="w-10 h-10 rounded-xl bg-[#FAF7F8] group-hover:bg-[#E1CCD4]/50 text-[#69042A] flex items-center justify-center transition-colors">
                <UserCheck className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-[#2D1B22] group-hover:text-[#69042A] transition-colors">
                  1. Editar perfil
                </h3>
                <p className="text-xs text-[#2D1B22]/60 mt-0.5">
                  Actualizar datos y contraseña
                </p>
              </div>
            </div>
            <ArrowUpRight className="w-4 h-4 text-[#2D1B22]/40 group-hover:text-[#69042A] transition-colors" />
          </button>

          {/* Opción 2: Añadir deuda */}
          <button
            onClick={() => openAddDebtModal()}
            className="group p-5 bg-white rounded-2xl border border-[#E1CCD4] hover:border-[#69042A] shadow-xs hover:shadow-md transition-all text-left flex items-start justify-between cursor-pointer"
          >
            <div className="space-y-2">
              <div className="w-10 h-10 rounded-xl bg-[#69042A] text-white flex items-center justify-center transition-colors">
                <PlusCircle className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-[#2D1B22] group-hover:text-[#69042A] transition-colors">
                  2. Añadir deuda
                </h3>
                <p className="text-xs text-[#2D1B22]/60 mt-0.5">
                  Crear deuda personal o familiar
                </p>
              </div>
            </div>
            <ArrowUpRight className="w-4 h-4 text-[#2D1B22]/40 group-hover:text-[#69042A] transition-colors" />
          </button>

          {/* Opción 3: Crear grupo familiar */}
          <button
            onClick={openCreateGroupModal}
            className="group p-5 bg-white rounded-2xl border border-[#E1CCD4] hover:border-[#69042A] shadow-xs hover:shadow-md transition-all text-left flex items-start justify-between cursor-pointer"
          >
            <div className="space-y-2">
              <div className="w-10 h-10 rounded-xl bg-[#FAF7F8] group-hover:bg-[#E1CCD4]/50 text-[#69042A] flex items-center justify-center transition-colors">
                <Users className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-[#2D1B22] group-hover:text-[#69042A] transition-colors">
                  3. Crear grupo familiar
                </h3>
                <p className="text-xs text-[#2D1B22]/60 mt-0.5">
                  Distribuir gastos y finanzas conjuntas
                </p>
              </div>
            </div>
            <ArrowUpRight className="w-4 h-4 text-[#2D1B22]/40 group-hover:text-[#69042A] transition-colors" />
          </button>

          {/* Opción 4: Aceptar invitación familiar */}
          <button
            onClick={openInvitationsModal}
            className="group p-5 bg-white rounded-2xl border border-[#E1CCD4] hover:border-[#69042A] shadow-xs hover:shadow-md transition-all text-left flex items-start justify-between relative cursor-pointer"
          >
            <div className="space-y-2">
              <div className="w-10 h-10 rounded-xl bg-[#FAF7F8] group-hover:bg-[#E1CCD4]/50 text-[#69042A] flex items-center justify-center transition-colors">
                <Bell className="w-5 h-5" />
              </div>
              <div>
                <div className="flex items-center gap-1.5">
                  <h3 className="text-sm font-bold text-[#2D1B22] group-hover:text-[#69042A] transition-colors">
                    4. Aceptar invitación
                  </h3>
                  {pendingInvitations.length > 0 && (
                    <span className="w-2 h-2 rounded-full bg-[#69042A] animate-pulse" />
                  )}
                </div>
                <p className="text-xs text-[#2D1B22]/60 mt-0.5">
                  {pendingInvitations.length > 0
                    ? `${pendingInvitations.length} invitación${pendingInvitations.length > 1 ? 'es' : ''} pendiente${pendingInvitations.length > 1 ? 's' : ''}`
                    : 'Ver invitaciones recibidas'}
                </p>
              </div>
            </div>
            {pendingInvitations.length > 0 ? (
              <span className="text-xs font-bold text-white bg-[#69042A] px-2 py-0.5 rounded-full">
                {pendingInvitations.length}
              </span>
            ) : (
              <ArrowUpRight className="w-4 h-4 text-[#2D1B22]/40 group-hover:text-[#69042A] transition-colors" />
            )}
          </button>
        </div>
      </section>

      {/* Resumen Métrico Financiero */}
      <section className="space-y-3">
        <h2 className="text-xs font-bold uppercase tracking-wider text-[#2D1B22]/60">
          Resumen Financiero
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
          {/* Métrica Principal: Monto Total Pendiente */}
          <div className="lg:col-span-1 p-5 rounded-2xl bg-[#69042A] text-white shadow-sm flex flex-col justify-between">
            <div>
              <span className="text-xs font-medium text-[#E1CCD4] uppercase tracking-wider block">
                Total Pendiente
              </span>
              <div className="text-2xl font-bold font-mono tabular-nums tracking-tight mt-2">
                {formatCurrency(summary.totalPendingAmount)}
              </div>
            </div>
            <div className="text-[11px] text-[#E1CCD4]/80 mt-4 flex items-center gap-1.5 pt-3 border-t border-[#E1CCD4]/20">
              <Clock className="w-3.5 h-3.5" />
              <span>Saldo total por saldar</span>
            </div>
          </div>

          {/* Deudas Pendientes */}
          <div className="p-5 rounded-2xl bg-white border border-[#E1CCD4] shadow-xs flex flex-col justify-between">
            <div>
              <span className="text-xs font-medium text-[#2D1B22]/60 uppercase tracking-wider block">
                Deudas Pendientes
              </span>
              <div className="text-2xl font-bold font-mono tabular-nums text-[#69042A] mt-2">
                {summary.pendingDebtsCount}
              </div>
            </div>
            <div className="text-[11px] text-[#2D1B22]/50 mt-4 pt-3 border-t border-[#FAF7F8]">
              Deudas activas con saldo por pagar
            </div>
          </div>

          {/* Deudas Pagadas */}
          <div className="p-5 rounded-2xl bg-white border border-[#E1CCD4] shadow-xs flex flex-col justify-between">
            <div>
              <span className="text-xs font-medium text-[#2D1B22]/60 uppercase tracking-wider block">
                Deudas Pagadas
              </span>
              <div className="text-2xl font-bold font-mono tabular-nums text-emerald-700 mt-2">
                {summary.paidDebtsCount}
              </div>
            </div>
            <div className="text-[11px] text-[#2D1B22]/50 mt-4 pt-3 border-t border-[#FAF7F8]">
              Compromisos totalmente liquidados
            </div>
          </div>

          {/* Abonos Personales */}
          <div className="p-5 rounded-2xl bg-white border border-[#E1CCD4] shadow-xs flex flex-col justify-between">
            <div>
              <span className="text-xs font-medium text-[#2D1B22]/60 uppercase tracking-wider block">
                Abonos Personales
              </span>
              <div className="text-xl sm:text-2xl font-bold font-mono tabular-nums text-[#2D1B22] mt-2">
                {formatCurrency(summary.personalPaymentsTotal)}
              </div>
            </div>
            <div className="text-[11px] text-[#2D1B22]/50 mt-4 pt-3 border-t border-[#FAF7F8]">
              Total desembolsado por ti
            </div>
          </div>

          {/* Deudas Familiares */}
          <div className="p-5 rounded-2xl bg-white border border-[#E1CCD4] shadow-xs flex flex-col justify-between">
            <div>
              <span className="text-xs font-medium text-[#2D1B22]/60 uppercase tracking-wider block">
                Deudas Familiares
              </span>
              <div className="text-xl sm:text-2xl font-bold font-mono tabular-nums text-[#2D1B22] mt-2">
                {formatCurrency(summary.familyDebtsPendingTotal)}
              </div>
            </div>
            <div className="text-[11px] text-[#2D1B22]/50 mt-4 pt-3 border-t border-[#FAF7F8]">
              Pendiente en tus grupos familiares
            </div>
          </div>
        </div>
      </section>

      {/* Sección en Dos Columnas: Deudas Pendientes y Abonos Recientes */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Lista de deudas pendientes que requieren atención */}
        <div className="lg:col-span-2 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-bold text-[#69042A] uppercase tracking-wider">
              Deudas Pendientes de Atención
            </h2>
            <button
              onClick={() => setActiveTab('personal')}
              className="text-xs font-semibold text-[#69042A] hover:underline flex items-center gap-1 cursor-pointer"
            >
              <span>Ver todas</span>
              <ChevronRight className="w-3.5 h-3.5" />
            </button>
          </div>

          {activePendingDebts.length === 0 ? (
            <div className="p-8 text-center bg-white rounded-2xl border border-[#E1CCD4] space-y-2">
              <CheckCircle2 className="w-8 h-8 text-emerald-600 mx-auto" />
              <h3 className="text-sm font-bold text-[#2D1B22]">¡Todo al día!</h3>
              <p className="text-xs text-[#2D1B22]/60">
                No tienes deudas pendientes en este momento. Añade una deuda personal o familiar para comenzar el seguimiento.
              </p>
            </div>
          ) : (
            <div className="space-y-3">
              {activePendingDebts.map((debt) => {
                const paid = getDebtPaidAmount(debt.id);
                const remaining = getDebtRemainingAmount(debt.id);
                const percent = Math.min(100, Math.round((paid / debt.amount) * 100));

                return (
                  <div
                    key={debt.id}
                    className="p-4 sm:p-5 bg-white rounded-2xl border border-[#E1CCD4] hover:border-[#69042A]/60 transition-all space-y-3"
                  >
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                      <div>
                        <div className="flex items-center gap-2">
                          <h4 className="text-sm sm:text-base font-bold text-[#2D1B22]">
                            {debt.name}
                          </h4>
                          <span className="text-[11px] font-medium text-[#2D1B22]/60 bg-[#FAF7F8] px-2 py-0.5 rounded border border-[#E1CCD4]/60">
                            {debt.familyGroupId ? 'Familiar' : 'Personal'}
                          </span>
                        </div>
                        {debt.description && (
                          <p className="text-xs text-[#2D1B22]/60 mt-0.5">{debt.description}</p>
                        )}
                      </div>

                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => openAddPaymentModal(debt)}
                          className="px-3.5 py-1.5 text-xs font-semibold text-white bg-[#69042A] hover:bg-[#520320] active:bg-[#3E0218] rounded-xl transition-colors shadow-xs cursor-pointer"
                        >
                          + Añadir pago
                        </button>
                        <button
                          onClick={() => openDebtDetailModal(debt)}
                          className="px-3 py-1.5 text-xs font-semibold text-[#2D1B22]/70 hover:text-[#69042A] hover:bg-[#FAF7F8] rounded-xl transition-colors border border-[#E1CCD4]/70 cursor-pointer"
                        >
                          Historial
                        </button>
                      </div>
                    </div>

                    {/* Barra de progreso y montos monetarios */}
                    <div className="space-y-1.5 pt-1">
                      <div className="flex items-center justify-between text-xs">
                        <div className="flex items-center gap-3">
                          <span className="text-[#2D1B22]/50">
                            Total:{' '}
                            <strong className="font-mono tabular-nums text-[#2D1B22]">
                              {formatCurrency(debt.amount)}
                            </strong>
                          </span>
                          <span className="text-emerald-700">
                            Pagado:{' '}
                            <strong className="font-mono tabular-nums">
                              {formatCurrency(paid)}
                            </strong>
                          </span>
                        </div>
                        <span className="text-[#69042A] font-bold">
                          Pendiente:{' '}
                          <strong className="font-mono tabular-nums">
                            {formatCurrency(remaining)}
                          </strong>
                        </span>
                      </div>

                      <div className="w-full bg-[#FAF7F8] h-2 rounded-full overflow-hidden border border-[#E1CCD4]/50">
                        <div
                          className="h-full bg-[#69042A] rounded-full transition-all duration-300"
                          style={{ width: `${percent}%` }}
                        />
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Historial de abonos recientes */}
        <div className="space-y-4">
          <h2 className="text-sm font-bold text-[#69042A] uppercase tracking-wider">
            Abonos Recientes
          </h2>
          <div className="bg-white rounded-2xl border border-[#E1CCD4] p-5 space-y-4 shadow-xs">
            {recentPayments.length === 0 ? (
              <p className="text-xs text-[#2D1B22]/60 text-center py-4">
                No se han registrado abonos todavía.
              </p>
            ) : (
              <div className="space-y-3">
                {recentPayments.map((p) => {
                  const debt = debts.find((d) => d.id === p.debtId);
                  return (
                    <div
                      key={p.id}
                      className="flex items-start justify-between py-2 border-b border-[#FAF7F8] last:border-0 last:pb-0 text-xs"
                    >
                      <div className="space-y-0.5">
                        <p className="font-bold text-[#2D1B22] truncate max-w-[160px]">
                          {debt?.name || 'Abono de deuda'}
                        </p>
                        <p className="text-[11px] text-[#2D1B22]/60">
                          {getUserName(p.userId)} · {formatDate(p.date)}
                        </p>
                        {p.description && (
                          <p className="text-[10px] text-[#2D1B22]/40 italic truncate max-w-[160px]">
                            "{p.description}"
                          </p>
                        )}
                      </div>
                      <span className="font-bold font-mono tabular-nums text-emerald-800 text-sm">
                        +{formatCurrency(p.amount)}
                      </span>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
