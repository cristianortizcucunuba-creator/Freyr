import React, { useState, useEffect } from 'react';
import { useApp } from '../context/AppContext';
import { X, UserCheck, Check, Lock, Mail, User } from 'lucide-react';

/**
 * Modal para editar el perfil del usuario activo.
 * Permite modificar nombre completo, nombre de usuario, correo electrónico
 * y opcionalmente actualizar la contraseña de acceso.
 */
export const ProfileModal: React.FC = () => {
  const { isProfileModalOpen, closeProfileModal, currentUser, updateProfile } = useApp();

  const [fullName, setFullName] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (isProfileModalOpen && currentUser) {
      setFullName(currentUser.fullName);
      setUsername(currentUser.username);
      setEmail(currentUser.email);
      setPassword(currentUser.password || '');
      setError(null);
      setSuccess(false);
    }
  }, [isProfileModalOpen, currentUser]);

  if (!isProfileModalOpen || !currentUser) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);

    const result = updateProfile({
      fullName,
      username,
      email,
      password: password || undefined,
    });

    if (!result.success) {
      setError(result.error || 'No fue posible actualizar el perfil.');
    } else {
      setSuccess(true);
      setTimeout(() => {
        closeProfileModal();
      }, 1000);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl max-w-md w-full border border-[#E1CCD4] shadow-xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        <div className="px-6 py-4 border-b border-[#E1CCD4]/70 flex items-center justify-between bg-[#FAF7F8]">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-[#E1CCD4] text-[#69042A] flex items-center justify-center">
              <UserCheck className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-base font-bold text-[#69042A]">Editar perfil</h2>
              <p className="text-xs text-[#2D1B22]/60">
                Actualiza tus credenciales personales y datos de contacto
              </p>
            </div>
          </div>
          <button
            onClick={closeProfileModal}
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

          {success && (
            <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-emerald-700 text-xs font-medium flex items-center gap-2">
              <Check className="w-4 h-4" />
              <span>¡Perfil actualizado correctamente!</span>
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-[#2D1B22]/70 mb-1">
              Nombre completo
            </label>
            <input
              type="text"
              required
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-sm text-[#2D1B22] focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A] transition-all"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#2D1B22]/70 mb-1">
              Nombre de usuario
            </label>
            <input
              type="text"
              required
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-sm text-[#2D1B22] focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A] transition-all"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#2D1B22]/70 mb-1">
              Correo electrónico
            </label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-sm text-[#2D1B22] focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A] transition-all"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#2D1B22]/70 mb-1">
              Contraseña
            </label>
            <input
              type="password"
              minLength={6}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Dejar en blanco para mantener la actual"
              className="w-full px-3.5 py-2.5 bg-[#FAF7F8] border border-[#E1CCD4] rounded-xl text-sm text-[#2D1B22] focus:outline-none focus:ring-2 focus:ring-[#69042A]/20 focus:border-[#69042A] transition-all"
            />
          </div>

          <div className="pt-2 flex items-center justify-end gap-2.5">
            <button
              type="button"
              onClick={closeProfileModal}
              className="px-4 py-2 text-xs font-semibold text-[#2D1B22]/70 hover:text-[#2D1B22] rounded-xl hover:bg-[#FAF7F8] transition-colors cursor-pointer"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="px-5 py-2 text-xs font-semibold text-white bg-[#69042A] hover:bg-[#520320] active:bg-[#3E0218] rounded-xl shadow-xs transition-colors flex items-center gap-1.5 cursor-pointer"
            >
              <Check className="w-3.5 h-3.5" />
              <span>Guardar cambios</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
