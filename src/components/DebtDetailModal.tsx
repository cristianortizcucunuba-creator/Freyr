import React from 'react';
import { useApp } from '../context/AppContext';
import { formatCurrency, formatDate } from '../utils/currency';
import { X, Plus, Trash2, Calendar, User, Clock, CheckCircle2, AlertCircle } from 'lucide-react';

/**
 * Modal de desglose y detalle de una deuda específica.
 * Permite visualizar el total acumulado, total abonado, saldo pendiente,
 * porcentaje de avance y el historial completo de abonos registrados con opción a registrar un nuevo pago.
 */
export const DebtDetailModal: React.FC = () => {
  const {
    selectedDebtForDetail,
    closeDebtDetailModal,
    getDebtPayments,
    getDebtPaidAmount,
    getDebtRemainingAmount,
    getUserName,
    familyGroups,
    openAddPaymentModal,
    deletePayment,
  } = useApp();

  if (!selectedDebtForDetail) return null;

  const debt = selectedDebtForDetail;
  const payments = getDebtPayments(debt.id);
  const totalPaid = getDebtPaidAmount(debt.id);
  const remaining = getDebtRemainingAmount(debt.id);
  const isPaid = remaining === 0 || debt.status === 'paid';

  const group = debt.familyGroupId
    ? familyGroups.find((g) => g.id === debt.familyGroupId)
    : null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl max-w-xl w-full border border-[#E1CCD4] shadow-xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        {/* Cabecera */}
        <div className="px-6 py-4 border-b border-[#E1CCD4]/70 flex items-center justify-between bg-[#FAF7F8]">
          <div className="space-y-0.5">
            <div className="flex items-center gap-2">
              <h2 className="text-lg font-bold text-[#69042A]">{debt.name}</h2>
              {isPaid ? (
                <span className="text-[11px] font-semibold text-emerald-700 bg-emerald-50 px-2.5 py-0.5 rounded-full flex items-center gap-1">
                  <CheckCircle2 className="w-3 h-3" />
                  Pagada
                </span>
              ) : (
                <span className="text-[11px] font-semibold text-[#69042A] bg-[#E1CCD4]/60 px-2.5 py-0.5 rounded-full flex items-center gap-1">
                  <Clock className="w-3 h-3" />
                  Pendiente
                </span>
              )}
            </div>
            <p className="text-xs text-[#2D1B22]/60">
              {debt.familyGroupId ? `Deuda familiar · ${group?.name || 'Grupo familiar'}` : 'Deuda personal'}
            </p>
          </div>
          <button
            onClick={closeDebtDetailModal}
            className="p-1.5 rounded-lg text-[#2D1B22]/50 hover:text-[#69042A] hover:bg-[#E1CCD4]/30 transition-colors"
            title="Cerrar ventana"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Métricas y resumen financiero */}
        <div className="p-6 space-y-6">
          <div className="grid grid-cols-3 gap-3">
            <div className="bg-[#FAF7F8] p-3 rounded-xl border border-[#E1CCD4]/60">
              <span className="text-[11px] text-[#2D1B22]/50 font-medium block">Monto total</span>
              <span className="text-base font-bold font-mono tabular-nums text-[#2D1B22]">
                {formatCurrency(debt.amount)}
              </span>
            </div>

            <div className="bg-[#FAF7F8] p-3 rounded-xl border border-[#E1CCD4]/60">
              <span className="text-[11px] text-[#2D1B22]/50 font-medium block">Total abonado</span>
              <span className="text-base font-bold font-mono tabular-nums text-emerald-700">
                {formatCurrency(totalPaid)}
              </span>
            </div>

            <div className="bg-[#FAF7F8] p-3 rounded-xl border border-[#E1CCD4]/60">
              <span className="text-[11px] text-[#2D1B22]/50 font-medium block">Saldo restante</span>
              <span className="text-base font-bold font-mono tabular-nums text-[#69042A]">
                {formatCurrency(remaining)}
              </span>
            </div>
          </div>

          {/* Barra de progreso de pago */}
          <div className="space-y-1.5">
            <div className="flex justify-between text-xs text-[#2D1B22]/70 font-medium">
              <span>Progreso de amortización</span>
              <span>{Math.min(100, Math.round((totalPaid / debt.amount) * 100))}%</span>
            </div>
            <div className="w-full bg-[#FAF7F8] h-2.5 rounded-full overflow-hidden border border-[#E1CCD4]/60">
              <div
                className="h-full bg-[#69042A] rounded-full transition-all duration-300"
                style={{
                  width: `${Math.min(100, Math.round((totalPaid / debt.amount) * 100))}%`,
                }}
              />
            </div>
          </div>

          {/* Información y metadatos */}
          <div className="grid grid-cols-2 gap-4 text-xs bg-[#FAF7F8] p-3.5 rounded-xl border border-[#E1CCD4]/60 text-[#2D1B22]">
            <div>
              <span className="text-[#2D1B22]/50 block">Persona asignada:</span>
              <span className="font-semibold">{getUserName(debt.assignedUserId)}</span>
            </div>
            <div>
              <span className="text-[#2D1B22]/50 block">Fecha de registro:</span>
              <span className="font-medium">{formatDate(debt.createdAt)}</span>
            </div>
            {debt.description && (
              <div className="col-span-2 pt-2 border-t border-[#E1CCD4]/40">
                <span className="text-[#2D1B22]/50 block">Descripción / Nota:</span>
                <span className="text-[#2D1B22]">{debt.description}</span>
              </div>
            )}
          </div>

          {/* Historial de abonos y pagos */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <h3 className="text-sm font-bold text-[#69042A]">Historial de abonos</h3>
                <span className="text-xs font-semibold text-[#2D1B22]/50 font-mono">
                  ({payments.length} {payments.length === 1 ? 'abono' : 'abonos'})
                </span>
              </div>
              {!isPaid && (
                <button
                  onClick={() => {
                    closeDebtDetailModal();
                    openAddPaymentModal(debt);
                  }}
                  className="px-3 py-1.5 text-xs font-semibold text-white bg-[#69042A] hover:bg-[#520320] rounded-lg transition-colors flex items-center gap-1 shadow-xs cursor-pointer"
                >
                  <Plus className="w-3.5 h-3.5" />
                  <span>Abonar a deuda</span>
                </button>
              )}
            </div>

            {payments.length === 0 ? (
              <div className="p-6 text-center bg-[#FAF7F8] rounded-xl border border-[#E1CCD4]/60 text-[#2D1B22]/60 text-xs">
                No hay abonos registrados para esta deuda aún.
              </div>
            ) : (
              <div className="space-y-2 max-h-56 overflow-y-auto pr-1">
                {payments.map((p) => (
                  <div
                    key={p.id}
                    className="p-3 bg-[#FAF7F8] rounded-xl border border-[#E1CCD4]/60 flex items-center justify-between hover:bg-white transition-colors"
                  >
                    <div className="space-y-0.5">
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold font-mono tabular-nums text-emerald-800">
                          {formatCurrency(p.amount)}
                        </span>
                        <span className="text-[11px] text-[#2D1B22]/50">
                          {formatDate(p.date)}
                        </span>
                      </div>
                      <div className="text-xs text-[#2D1B22]/70 flex items-center gap-1.5">
                        <User className="w-3 h-3 text-[#69042A]" />
                        <span>Abonado por {getUserName(p.userId)}</span>
                        {p.description && (
                          <span className="text-[#2D1B22]/60 italic">· "{p.description}"</span>
                        )}
                      </div>
                    </div>

                    <button
                      onClick={() => deletePayment(p.id)}
                      title="Eliminar este abono"
                      className="p-1.5 text-[#2D1B22]/40 hover:text-rose-700 hover:bg-rose-50 rounded-lg transition-colors cursor-pointer"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Pie de modal */}
        <div className="px-6 py-3 bg-[#FAF7F8] border-t border-[#E1CCD4]/70 flex justify-end">
          <button
            onClick={closeDebtDetailModal}
            className="px-4 py-2 text-xs font-semibold text-[#2D1B22]/70 hover:text-[#2D1B22] rounded-xl hover:bg-white transition-colors cursor-pointer"
          >
            Cerrar
          </button>
        </div>
      </div>
    </div>
  );
};
