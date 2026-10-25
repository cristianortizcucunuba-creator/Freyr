import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { X, Users, Check } from 'lucide-react';

/**
 * Modal para crear un nuevo grupo familiar en Freyr.
 * Permite al usuario administrador o cabeza de familia fundar un espacio compartido para gestionar deudas comunes.
 */
export const CreateGroupModal: React.FC = () => {
  const { isCreateGroupModalOpen, closeCreateGroupModal, createFamilyGroup, setActiveTab } =
    useApp();
  const [name, setName] = useState('');
  const [error, setError] = useState<string | null>(null);

  if (!isCreateGroupModalOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!name.trim()) {
      setError('Por favor indica un nombre para el grupo familiar.');
      return;
    }

    createFamilyGroup(name);
    setName('');
    closeCreateGroupModal();
    setActiveTab('groups');
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl max-w-md w-full border border-[#E1CCD4] shadow-xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        <div className="px-6 py-4 border-b border-[#E1CCD4]/70 flex items-center justify-between bg-[#FAF7F8]">
          <div>
            <h2 className="text-base font-bold text-[#69042A]">Crear grupo familiar</h2>
            <p className="text-xs text-[#2D1B22]/60">
              Comparte, asigna y da seguimiento a gastos familiares
            </p>
          </div>
          <button
            onClick={closeCreateGroupModal}
            className="p-1.5 rounded-lg text-[#2D1B22]/50 hover:text-[#69042A] hover:bg-[#E1CCD4]/30 transition-colors cursor-pointer"
            title="Cerrar ventana"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="p-3 bg-rose-50 border border-rose-200 rounded-xl text-rose-700 text-xs font-medium">
              {error}
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-[#2D1B22]/70 mb-1">
              Nombre del grupo familiar
            </label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Ej. Familia Gómez, Hogar Central, Hermanos Silva"
              className="w-full px-3.5 py-2.5 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-sm text-[#2D1B22] placeholder:text-[#2D1B22]/40 focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A] transition-all"
            />
          </div>

          <div className="p-3 bg-[#FAF7F8] rounded-xl border border-[#E1CCD4]/60 text-xs text-[#2D1B22]/70 space-y-1">
            <p className="font-semibold text-[#69042A]">Como creador y administrador podrás:</p>
            <p>• Invitar a otros miembros mediante su usuario o correo</p>
            <p>• Crear deudas familiares y asignarlas a miembros específicos</p>
            <p>• Mantener un registro común y transparente de todos los abonos</p>
          </div>

          <div className="pt-2 flex items-center justify-end gap-2.5">
            <button
              type="button"
              onClick={closeCreateGroupModal}
              className="px-4 py-2 text-xs font-semibold text-[#2D1B22]/70 hover:text-[#2D1B22] rounded-xl hover:bg-[#FAF7F8] transition-colors cursor-pointer"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="px-5 py-2 text-xs font-semibold text-white bg-[#69042A] hover:bg-[#520320] active:bg-[#3E0218] rounded-xl shadow-xs transition-colors flex items-center gap-1.5 cursor-pointer"
            >
              <Check className="w-3.5 h-3.5" />
              <span>Crear grupo</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
