import React, { useState, useEffect } from 'react';
import { useApp } from '../context/AppContext';
import { formatCurrency } from '../utils/currency';
import { X, Check, DollarSign, Calendar, MessageSquare, CreditCard } from 'lucide-react';

export const AddPaymentModal: React.FC = () => {
  const {
    isAddPaymentModalOpen,
    closeAddPaymentModal,
    paymentDebtTarget,
    currentUser,
    getDebtPaidAmount,
    getDebtRemainingAmount,
    addPayment,
  } = useApp();

  const [amount, setAmount] = useState('');
  const [date, setDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [description, setDescription] = useState('');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isAddPaymentModalOpen && paymentDebtTarget) {
      setAmount('');
      setDate(new Date().toISOString().split('T')[0]);
      setDescription('');
      setError(null);
    }
  }, [isAddPaymentModalOpen, paymentDebtTarget]);

  if (!isAddPaymentModalOpen || !paymentDebtTarget || !currentUser) return null;

  const currentPaid = getDebtPaidAmount(paymentDebtTarget.id);
  const remaining = getDebtRemainingAmount(paymentDebtTarget.id);

  const numAmount = parseFloat(amount.replace(/,/g, '')) || 0;
  const projectedRemaining = Math.max(0, remaining - numAmount);
  const willComplete = numAmount >= remaining && remaining > 0;

  const handlePayFullRemaining = () => {
    setAmount(remaining.toString());
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const parsedAmount = parseFloat(amount.replace(/,/g, ''));
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      setError('Por favor ingresa un monto de pago válido mayor a 0.');
      return;
    }

    if (parsedAmount > remaining) {
      if (parsedAmount > remaining * 1.5) {
        setError(`El monto del pago no puede exceder significativamente el saldo pendiente (${formatCurrency(remaining)}).`);
        return;
      }
    }

    addPayment({
      debtId: paymentDebtTarget.id,
      amount: parsedAmount,
      date,
      description: description.trim() || undefined,
    });

    closeAddPaymentModal();
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl max-w-md w-full border border-[#E1CCD4] shadow-xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        {/* Encabezado */}
        <div className="px-6 py-4 border-b border-[#E1CCD4]/70 flex items-center justify-between bg-[#FAF7F8]">
          <div>
            <h2 className="text-base font-bold text-[#69042A]">Registrar Pago / Abono</h2>
            <p className="text-xs text-[#2D1B22]/60 truncate max-w-xs">
              Para: <span className="font-semibold text-[#2D1B22]">{paymentDebtTarget.name}</span>
            </p>
          </div>
          <button
            onClick={closeAddPaymentModal}
            className="p-1.5 rounded-lg text-[#2D1B22]/50 hover:text-[#69042A] hover:bg-[#E1CCD4]/30 transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tarjeta de Estado del Saldo */}
        <div className="p-5 bg-[#FAF7F8]/80 border-b border-[#E1CCD4]/60 grid grid-cols-3 gap-2 text-center">
          <div className="bg-white p-2.5 rounded-xl border border-[#E1CCD4]/60">
            <span className="block text-[11px] font-medium text-[#2D1B22]/50">Deuda Total</span>
            <span className="text-xs sm:text-sm font-semibold font-mono tabular-nums text-[#2D1B22]">
              {formatCurrency(paymentDebtTarget.amount)}
            </span>
          </div>

          <div className="bg-white p-2.5 rounded-xl border border-[#E1CCD4]/60">
            <span className="block text-[11px] font-medium text-[#2D1B22]/50">Pagado</span>
            <span className="text-xs sm:text-sm font-semibold font-mono tabular-nums text-emerald-700">
              {formatCurrency(currentPaid)}
            </span>
          </div>

          <div className="bg-white p-2.5 rounded-xl border border-[#E1CCD4]/60">
            <span className="block text-[11px] font-medium text-[#2D1B22]/50">Pendiente</span>
            <span className="text-xs sm:text-sm font-bold font-mono tabular-nums text-[#69042A]">
              {formatCurrency(remaining)}
            </span>
          </div>
        </div>

        {/* Formulario */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="p-3 bg-rose-50 border border-rose-200 rounded-xl text-rose-700 text-xs font-medium">
              {error}
            </div>
          )}

          {/* Monto del abono */}
          <div>
            <div className="flex items-center justify-between mb-1">
              <label className="text-xs font-semibold text-[#2D1B22]/70">
                Monto del Abono ($) *
              </label>
              {remaining > 0 && (
                <button
                  type="button"
                  onClick={handlePayFullRemaining}
                  className="text-[11px] font-medium text-[#69042A] hover:underline cursor-pointer"
                >
                  Pagar saldo restante ({formatCurrency(remaining)})
                </button>
              )}
            </div>
            <div className="relative">
              <span className="absolute left-3.5 top-2.5 text-[#2D1B22]/50 text-sm font-semibold">
                $
              </span>
              <input
                type="number"
                step="any"
                min="0.01"
                required
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="ej. 500000"
                className="w-full pl-8 pr-3.5 py-2.5 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-sm font-mono tabular-nums text-[#2D1B22] placeholder:text-[#2D1B22]/40 focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A] transition-all"
              />
            </div>

            {numAmount > 0 && (
              <div className="mt-2 text-xs flex items-center justify-between px-1 text-[#2D1B22]/70">
                <span>Saldo pendiente proyectado:</span>
                <span className="font-mono font-bold tabular-nums text-[#69042A]">
                  {formatCurrency(projectedRemaining)}
                  {willComplete && ' (Quedará como PAGADA)'}
                </span>
              </div>
            )}
          </div>

          {/* Fecha */}
          <div>
            <label className="block text-xs font-semibold text-[#2D1B22]/70 mb-1">
              Fecha del Pago
            </label>
            <input
              type="date"
              required
              value={date}
              onChange={(e) => setDate(e.target.value)}
              className="w-full px-3.5 py-2 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-xs text-[#2D1B22] focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A]"
            />
          </div>

          {/* Usuario que realiza el pago */}
          <div className="text-xs bg-[#FAF7F8] px-3.5 py-2 rounded-xl border border-[#E1CCD4]/60 flex items-center justify-between text-[#2D1B22]/70">
            <span>Abono realizado por:</span>
            <span className="font-semibold text-[#2D1B22]">{currentUser.fullName}</span>
          </div>

          {/* Descripción */}
          <div>
            <label className="block text-xs font-semibold text-[#2D1B22]/70 mb-1">
              Nota / Concepto (Opcional)
            </label>
            <input
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="ej. 1ª cuota, Transferencia bancaria, Efectivo"
              className="w-full px-3.5 py-2 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-xs text-[#2D1B22] placeholder:text-[#2D1B22]/40 focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A]"
            />
          </div>

          {/* Acciones */}
          <div className="pt-2 flex items-center justify-end gap-2.5">
            <button
              type="button"
              onClick={closeAddPaymentModal}
              className="px-4 py-2 text-xs font-semibold text-[#2D1B22]/70 hover:text-[#2D1B22] rounded-xl hover:bg-[#FAF7F8] transition-colors cursor-pointer"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="px-5 py-2 text-xs font-semibold text-white bg-[#69042A] hover:bg-[#520320] active:bg-[#3E0218] rounded-xl shadow-xs transition-colors flex items-center gap-1.5 cursor-pointer"
            >
              <Check className="w-3.5 h-3.5" />
              <span>Confirmar pago</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
