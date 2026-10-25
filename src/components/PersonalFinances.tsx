import React, { useState, useMemo } from 'react';
import { useApp } from '../context/AppContext';
import { formatCurrency, formatDate } from '../utils/currency';
import {
  Plus,
  Search,
  CheckCircle2,
  Clock,
  Trash2,
  ChevronRight,
  CreditCard,
  History,
  AlertCircle
} from 'lucide-react';
import { Debt } from '../types';

export const PersonalFinances: React.FC = () => {
  const {
    currentUser,
    debts,
    openAddDebtModal,
    openAddPaymentModal,
    openDebtDetailModal,
    deleteDebt,
    updateDebt,
    getDebtPaidAmount,
    getDebtRemainingAmount,
  } = useApp();

  const [filter, setFilter] = useState<'all' | 'pending' | 'paid'>('all');
  const [searchQuery, setSearchQuery] = useState('');

  // Personal debts only (familyGroupId === null)
  const personalDebts = useMemo(() => {
    if (!currentUser) return [];
    return debts.filter(
      (d) =>
        d.familyGroupId === null &&
        (d.assignedUserId === currentUser.id || d.createdBy === currentUser.id)
    );
  }, [debts, currentUser]);

  const filteredDebts = useMemo(() => {
    return personalDebts.filter((debt) => {
      const remaining = getDebtRemainingAmount(debt.id);
      const isPaid = remaining === 0 || debt.status === 'paid';

      if (filter === 'pending' && isPaid) return false;
      if (filter === 'paid' && !isPaid) return false;

      if (searchQuery.trim()) {
        const query = searchQuery.toLowerCase();
        const matchesName = debt.name.toLowerCase().includes(query);
        const matchesDesc = debt.description?.toLowerCase().includes(query);
        if (!matchesName && !matchesDesc) return false;
      }

      return true;
    });
  }, [personalDebts, filter, searchQuery, getDebtRemainingAmount]);

  // Aggregate stats for personal finances
  const totalPersonalDebt = personalDebts.reduce((acc, d) => acc + d.amount, 0);
  const totalPersonalPaid = personalDebts.reduce(
    (acc, d) => acc + getDebtPaidAmount(d.id),
    0
  );
  const totalPersonalRemaining = Math.max(0, totalPersonalDebt - totalPersonalPaid);

  const toggleStatus = (debt: Debt) => {
    const isCurrentlyPaid = debt.status === 'paid';
    if (isCurrentlyPaid) {
      updateDebt(debt.id, { status: 'pending' });
    } else {
      updateDebt(debt.id, { status: 'paid' });
    }
  };

  return (
    <div className="space-y-8 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Sección Superior */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-[#69042A]">
            Finanzas Personales
          </h1>
          <p className="text-sm text-[#2D1B22]/70 mt-1">
            Administra tus deudas personales, cuotas parciales y el progreso de pago individual.
          </p>
        </div>

        <button
          onClick={() => openAddDebtModal(null)}
          className="self-start sm:self-center px-4 py-2.5 text-xs font-semibold text-white bg-[#69042A] hover:bg-[#520320] active:bg-[#3E0218] rounded-xl shadow-xs transition-colors flex items-center gap-2 cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>Añadir deuda personal</span>
        </button>
      </div>

      {/* Tarjetas de Resumen Acumulado */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="p-5 rounded-2xl bg-white border border-[#E1CCD4] shadow-xs">
          <span className="text-xs font-medium text-[#2D1B22]/60 uppercase tracking-wider block">
            Total Deudas Personales
          </span>
          <div className="text-2xl font-bold font-mono tabular-nums text-[#2D1B22] mt-2">
            {formatCurrency(totalPersonalDebt)}
          </div>
          <p className="text-[11px] text-[#2D1B22]/50 mt-1">
            En {personalDebts.length} registro{personalDebts.length === 1 ? '' : 's'} personal{personalDebts.length === 1 ? '' : 'es'}
          </p>
        </div>

        <div className="p-5 rounded-2xl bg-white border border-[#E1CCD4] shadow-xs">
          <span className="text-xs font-medium text-[#2D1B22]/60 uppercase tracking-wider block">
            Total Pagado
          </span>
          <div className="text-2xl font-bold font-mono tabular-nums text-emerald-700 mt-2">
            {formatCurrency(totalPersonalPaid)}
          </div>
          <p className="text-[11px] text-[#2D1B22]/50 mt-1">
            Abonado mediante pagos registrados
          </p>
        </div>

        <div className="p-5 rounded-2xl bg-[#FAF7F8] border border-[#E1CCD4] shadow-xs">
          <span className="text-xs font-medium text-[#69042A] uppercase tracking-wider block">
            Total Pendiente
          </span>
          <div className="text-2xl font-bold font-mono tabular-nums text-[#69042A] mt-2">
            {formatCurrency(totalPersonalRemaining)}
          </div>
          <p className="text-[11px] text-[#2D1B22]/50 mt-1">
            Saldo restante por liquidar
          </p>
        </div>
      </div>

      {/* Barra de Búsqueda y Filtros */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 bg-white p-3 rounded-2xl border border-[#E1CCD4]">
        {/* Filtros segmentados */}
        <div className="flex items-center gap-1 p-1 bg-[#FAF7F8] rounded-xl border border-[#E1CCD4]/70">
          <button
            onClick={() => setFilter('all')}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors cursor-pointer ${
              filter === 'all'
                ? 'bg-white text-[#69042A] shadow-xs'
                : 'text-[#2D1B22]/60 hover:text-[#2D1B22]'
            }`}
          >
            Todas ({personalDebts.length})
          </button>
          <button
            onClick={() => setFilter('pending')}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors cursor-pointer ${
              filter === 'pending'
                ? 'bg-white text-[#69042A] shadow-xs'
                : 'text-[#2D1B22]/60 hover:text-[#2D1B22]'
            }`}
          >
            Pendientes
          </button>
          <button
            onClick={() => setFilter('paid')}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors cursor-pointer ${
              filter === 'paid'
                ? 'bg-white text-[#69042A] shadow-xs'
                : 'text-[#2D1B22]/60 hover:text-[#2D1B22]'
            }`}
          >
            Pagadas
          </button>
        </div>

        {/* Buscador */}
        <div className="relative flex-1 max-w-xs">
          <Search className="w-4 h-4 absolute left-3 top-2.5 text-[#2D1B22]/40" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Buscar deudas personales..."
            className="w-full pl-9 pr-3.5 py-1.5 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-xs text-[#2D1B22] placeholder:text-[#2D1B22]/40 focus:outline-none focus:border-[#69042A]"
          />
        </div>
      </div>

      {/* Lista de Deudas */}
      <div className="space-y-4">
        {filteredDebts.length === 0 ? (
          <div className="p-12 text-center bg-white rounded-2xl border border-[#E1CCD4] space-y-3">
            <CreditCard className="w-10 h-10 text-[#69042A]/40 mx-auto" />
            <h3 className="text-base font-bold text-[#2D1B22]">No se encontraron deudas personales</h3>
            <p className="text-xs text-[#2D1B22]/60 max-w-sm mx-auto">
              {searchQuery
                ? 'Ninguna deuda coincide con el criterio de búsqueda ingresado.'
                : 'Aún no has registrado deudas personales en esta sección.'}
            </p>
            {!searchQuery && (
              <button
                onClick={() => openAddDebtModal(null)}
                className="mt-2 px-4 py-2 text-xs font-semibold text-white bg-[#69042A] hover:bg-[#520320] rounded-xl shadow-xs cursor-pointer"
              >
                + Crear primera deuda
              </button>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {filteredDebts.map((debt) => {
              const paid = getDebtPaidAmount(debt.id);
              const remaining = getDebtRemainingAmount(debt.id);
              const isPaid = remaining === 0 || debt.status === 'paid';
              const percent = Math.min(100, Math.round((paid / debt.amount) * 100));

              return (
                <div
                  key={debt.id}
                  className="p-5 bg-white rounded-2xl border border-[#E1CCD4] hover:border-[#69042A]/50 transition-all flex flex-col justify-between space-y-4 shadow-xs"
                >
                  {/* Encabezado de la tarjeta */}
                  <div>
                    <div className="flex items-start justify-between gap-2">
                      <div className="space-y-1">
                        <div className="flex items-center gap-2">
                          <h3 className="text-base font-bold text-[#2D1B22]">{debt.name}</h3>
                          {isPaid ? (
                            <span className="text-[11px] font-semibold text-emerald-800 bg-emerald-50 px-2 py-0.5 rounded-full flex items-center gap-1">
                              <CheckCircle2 className="w-3 h-3" />
                              Pagada
                            </span>
                          ) : (
                            <span className="text-[11px] font-semibold text-[#69042A] bg-[#E1CCD4]/50 px-2 py-0.5 rounded-full flex items-center gap-1">
                              <Clock className="w-3 h-3" />
                              Pendiente
                            </span>
                          )}
                        </div>
                        {debt.description && (
                          <p className="text-xs text-[#2D1B22]/60 line-clamp-2">
                            {debt.description}
                          </p>
                        )}
                      </div>

                      <div className="flex items-center gap-1">
                        <button
                          onClick={() => deleteDebt(debt.id)}
                          title="Eliminar deuda"
                          className="p-1.5 text-[#2D1B22]/40 hover:text-rose-700 hover:bg-rose-50 rounded-lg transition-colors cursor-pointer"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </div>

                    <div className="text-[11px] text-[#2D1B22]/50 mt-1">
                      Creada: {formatDate(debt.createdAt)}
                    </div>
                  </div>

                  {/* Desglose Financiero */}
                  <div className="p-3.5 bg-[#FAF7F8] rounded-xl border border-[#E1CCD4]/60 space-y-2">
                    <div className="grid grid-cols-3 gap-2 text-xs">
                      <div>
                        <span className="text-[#2D1B22]/50 block text-[11px]">Total</span>
                        <span className="font-bold font-mono tabular-nums text-[#2D1B22]">
                          {formatCurrency(debt.amount)}
                        </span>
                      </div>
                      <div>
                        <span className="text-[#2D1B22]/50 block text-[11px]">Pagado</span>
                        <span className="font-bold font-mono tabular-nums text-emerald-700">
                          {formatCurrency(paid)}
                        </span>
                      </div>
                      <div>
                        <span className="text-[#2D1B22]/50 block text-[11px]">Pendiente</span>
                        <span className="font-bold font-mono tabular-nums text-[#69042A]">
                          {formatCurrency(remaining)}
                        </span>
                      </div>
                    </div>

                    {/* Barra de progreso de amortización */}
                    <div className="space-y-1 pt-1">
                      <div className="w-full bg-white h-2 rounded-full overflow-hidden border border-[#E1CCD4]/40">
                        <div
                          className="h-full bg-[#69042A] rounded-full transition-all duration-300"
                          style={{ width: `${percent}%` }}
                        />
                      </div>
                      <div className="flex justify-between text-[10px] text-[#2D1B22]/50">
                        <span>{percent}% Pagado</span>
                        <span>{formatCurrency(remaining)} pendiente</span>
                      </div>
                    </div>
                  </div>

                  {/* Acciones de la Tarjeta */}
                  <div className="pt-1 flex items-center justify-between gap-2 border-t border-[#FAF7F8]">
                    <button
                      onClick={() => openDebtDetailModal(debt)}
                      className="px-3 py-1.5 text-xs font-semibold text-[#2D1B22]/70 hover:text-[#69042A] hover:bg-[#FAF7F8] rounded-xl transition-colors flex items-center gap-1 cursor-pointer"
                    >
                      <History className="w-3.5 h-3.5" />
                      <span>Historial</span>
                    </button>

                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => toggleStatus(debt)}
                        className="px-2.5 py-1.5 text-[11px] font-medium text-[#2D1B22]/60 hover:text-[#2D1B22] rounded-xl transition-colors cursor-pointer"
                      >
                        {isPaid ? 'Marcar pendiente' : 'Marcar pagada'}
                      </button>

                      {!isPaid && (
                        <button
                          onClick={() => openAddPaymentModal(debt)}
                          className="px-3.5 py-1.5 text-xs font-semibold text-white bg-[#69042A] hover:bg-[#520320] active:bg-[#3E0218] rounded-xl shadow-xs transition-colors flex items-center gap-1 cursor-pointer"
                        >
                          <Plus className="w-3.5 h-3.5" />
                          <span>Añadir pago</span>
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};
