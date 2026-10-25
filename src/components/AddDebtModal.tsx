import React, { useState, useEffect } from 'react';
import { useApp } from '../context/AppContext';
import { X, DollarSign, Tag, Users, User, FileText, Check } from 'lucide-react';

export const AddDebtModal: React.FC = () => {
  const {
    isAddDebtModalOpen,
    closeAddDebtModal,
    addDebtInitialGroup,
    currentUser,
    getUserFamilyGroups,
    users,
    addDebt,
  } = useApp();

  const userGroups = getUserFamilyGroups();

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [amount, setAmount] = useState('');
  const [debtScope, setDebtScope] = useState<'personal' | 'family'>('personal');
  const [selectedGroupId, setSelectedGroupId] = useState<string>('');
  const [assignedUserId, setAssignedUserId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isAddDebtModalOpen) {
      setName('');
      setDescription('');
      setAmount('');
      setError(null);

      if (addDebtInitialGroup) {
        setDebtScope('family');
        setSelectedGroupId(addDebtInitialGroup);
        setAssignedUserId(null); // defaults to entire family or member
      } else {
        setDebtScope('personal');
        setSelectedGroupId(userGroups[0]?.id || '');
        setAssignedUserId(currentUser?.id || null);
      }
    }
  }, [isAddDebtModalOpen, addDebtInitialGroup, currentUser, userGroups]);

  if (!isAddDebtModalOpen || !currentUser) return null;

  const currentGroup = userGroups.find((g) => g.id === selectedGroupId);
  const groupMembers = currentGroup
    ? users.filter((u) => currentGroup.memberIds.includes(u.id))
    : [];

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const parsedAmount = parseFloat(amount.replace(/,/g, ''));
    if (!name.trim()) {
      setError('Por favor ingresa un nombre o concepto para la deuda.');
      return;
    }
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      setError('Por favor ingresa un monto numérico positivo válido.');
      return;
    }

    if (debtScope === 'family') {
      if (!selectedGroupId) {
        setError('Por favor selecciona o crea un grupo familiar primero.');
        return;
      }
      addDebt({
        name: name.trim(),
        description: description.trim() || undefined,
        amount: parsedAmount,
        familyGroupId: selectedGroupId,
        assignedUserId: assignedUserId, // null indica compartida por toda la familia
      });
    } else {
      // Deuda personal
      addDebt({
        name: name.trim(),
        description: description.trim() || undefined,
        amount: parsedAmount,
        familyGroupId: null,
        assignedUserId: currentUser.id,
      });
    }

    closeAddDebtModal();
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl max-w-lg w-full border border-[#E1CCD4] shadow-xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        {/* Encabezado */}
        <div className="px-6 py-4 border-b border-[#E1CCD4]/70 flex items-center justify-between bg-[#FAF7F8]">
          <div>
            <h2 className="text-lg font-bold text-[#69042A]">Añadir Nueva Deuda</h2>
            <p className="text-xs text-[#2D1B22]/60">
              Registra una obligación financiera personal o compartida
            </p>
          </div>
          <button
            onClick={closeAddDebtModal}
            className="p-1.5 rounded-lg text-[#2D1B22]/50 hover:text-[#69042A] hover:bg-[#E1CCD4]/30 transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Formulario */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="p-3 bg-rose-50 border border-rose-200 rounded-xl text-rose-700 text-xs font-medium">
              {error}
            </div>
          )}

          {/* Selector de Ámbito */}
          <div>
            <label className="block text-xs font-semibold text-[#2D1B22]/70 mb-1.5">
              Tipo de Deuda
            </label>
            <div className="grid grid-cols-2 gap-2 p-1 bg-[#FAF7F8] border border-[#E1CCD4]/70 rounded-xl">
              <button
                type="button"
                onClick={() => {
                  setDebtScope('personal');
                  setAssignedUserId(currentUser.id);
                }}
                className={`py-2 text-xs font-semibold rounded-lg transition-all flex items-center justify-center gap-2 cursor-pointer ${
                  debtScope === 'personal'
                    ? 'bg-white text-[#69042A] shadow-xs'
                    : 'text-[#2D1B22]/60 hover:text-[#2D1B22]'
                }`}
              >
                <User className="w-3.5 h-3.5" />
                <span>Deuda Personal</span>
              </button>

              <button
                type="button"
                onClick={() => {
                  if (userGroups.length === 0) {
                    setError('Debes crear o unirte a un grupo familiar primero.');
                    return;
                  }
                  setDebtScope('family');
                  if (!selectedGroupId && userGroups.length > 0) {
                    setSelectedGroupId(userGroups[0].id);
                  }
                  setAssignedUserId(null);
                }}
                className={`py-2 text-xs font-semibold rounded-lg transition-all flex items-center justify-center gap-2 cursor-pointer ${
                  debtScope === 'family'
                    ? 'bg-white text-[#69042A] shadow-xs'
                    : 'text-[#2D1B22]/60 hover:text-[#2D1B22]'
                }`}
              >
                <Users className="w-3.5 h-3.5" />
                <span>Deuda Familiar</span>
              </button>
            </div>
          </div>

          {/* Nombre / Concepto */}
          <div>
            <label htmlFor="debt-name-input" className="block text-xs font-semibold text-[#2D1B22]/70 mb-1">
              Nombre / Título de la deuda *
            </label>
            <div className="relative">
              <input
                id="debt-name-input"
                type="text"
                required
                autoFocus
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="ej. Portátil, Factura de Internet, Mercado"
                className="w-full px-3.5 py-2.5 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-sm text-[#2D1B22] placeholder:text-[#2D1B22]/40 focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A] transition-all"
              />
            </div>
          </div>

          {/* Monto */}
          <div>
            <label htmlFor="debt-amount-input" className="block text-xs font-semibold text-[#2D1B22]/70 mb-1">
              Monto total ($) *
            </label>
            <div className="relative">
              <span className="absolute left-3.5 top-2.5 text-[#2D1B22]/50 text-sm font-semibold pointer-events-none">
                $
              </span>
              <input
                id="debt-amount-input"
                type="number"
                inputMode="decimal"
                step="any"
                min="0.01"
                required
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="ej. 3000000 o 120000"
                className="w-full pl-8 pr-3.5 py-2.5 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-sm font-mono tabular-nums text-[#2D1B22] placeholder:text-[#2D1B22]/40 focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A] transition-all"
              />
            </div>
          </div>

          {/* Campos específicos de deuda familiar */}
          {debtScope === 'family' && (
            <div className="space-y-3 p-3.5 bg-[#FAF7F8] border border-[#E1CCD4]/60 rounded-xl">
              <div>
                <label className="block text-xs font-semibold text-[#2D1B22]/70 mb-1">
                  Grupo Familiar
                </label>
                <select
                  value={selectedGroupId}
                  onChange={(e) => setSelectedGroupId(e.target.value)}
                  className="w-full px-3 py-2 bg-white border border-[#E1CCD4] rounded-xl text-xs text-[#2D1B22] focus:outline-none focus:border-[#69042A]"
                >
                  {userGroups.map((g) => (
                    <option key={g.id} value={g.id}>
                      {g.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-[#2D1B22]/70 mb-1">
                  Miembro Responsable
                </label>
                <select
                  value={assignedUserId || 'all'}
                  onChange={(e) =>
                    setAssignedUserId(e.target.value === 'all' ? null : e.target.value)
                  }
                  className="w-full px-3 py-2 bg-white border border-[#E1CCD4] rounded-xl text-xs text-[#2D1B22] focus:outline-none focus:border-[#69042A]"
                >
                  <option value="all">Toda la familia (Compartida)</option>
                  {groupMembers.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.fullName} (@{m.username})
                    </option>
                  ))}
                </select>
              </div>
            </div>
          )}

          {/* Descripción / Notas */}
          <div>
            <label className="block text-xs font-semibold text-[#2D1B22]/70 mb-1">
              Descripción / Notas (Opcional)
            </label>
            <textarea
              rows={2}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="ej. Financiación a cuotas, división 50/50, a pagar antes del día 15"
              className="w-full px-3.5 py-2 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-sm text-[#2D1B22] placeholder:text-[#2D1B22]/40 focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A] transition-all"
            />
          </div>

          {/* Botones de acción */}
          <div className="pt-2 flex items-center justify-end gap-2.5">
            <button
              type="button"
              onClick={closeAddDebtModal}
              className="px-4 py-2 text-xs font-semibold text-[#2D1B22]/70 hover:text-[#2D1B22] rounded-xl hover:bg-[#FAF7F8] transition-colors cursor-pointer"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="px-5 py-2 text-xs font-semibold text-white bg-[#69042A] hover:bg-[#520320] active:bg-[#3E0218] rounded-xl shadow-xs transition-colors flex items-center gap-1.5 cursor-pointer"
            >
              <Check className="w-3.5 h-3.5" />
              <span>Guardar deuda</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
