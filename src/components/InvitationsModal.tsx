import React from 'react';
import { useApp } from '../context/AppContext';
import { formatDate } from '../utils/currency';
import { X, Check, Bell, Users, User, Clock, ArrowRight } from 'lucide-react';

export const InvitationsModal: React.FC = () => {
  const {
    isInvitationsModalOpen,
    closeInvitationsModal,
    getUserPendingInvitations,
    familyGroups,
    users,
    respondToInvitation,
    currentUser,
    invitations,
  } = useApp();

  if (!isInvitationsModalOpen || !currentUser) return null;

  const pendingInvites = getUserPendingInvitations();

  // Sent invitations by groups created by user
  const userCreatedGroupIds = familyGroups
    .filter((g) => g.creatorId === currentUser.id)
    .map((g) => g.id);

  const sentInvites = invitations.filter((inv) =>
    userCreatedGroupIds.includes(inv.familyGroupId)
  );

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl max-w-lg w-full border border-[#E1CCD4] shadow-xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        <div className="px-6 py-4 border-b border-[#E1CCD4]/70 flex items-center justify-between bg-[#FAF7F8]">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-[#E1CCD4] text-[#69042A] flex items-center justify-center">
              <Bell className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-base font-bold text-[#69042A]">
                Invitaciones a Grupos Familiares
              </h2>
              <p className="text-xs text-[#2D1B22]/60">
                Revisa y acepta invitaciones para unirte a grupos familiares
              </p>
            </div>
          </div>
          <button
            onClick={closeInvitationsModal}
            className="p-1.5 rounded-lg text-[#2D1B22]/50 hover:text-[#69042A] hover:bg-[#E1CCD4]/30 transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6 space-y-6">
          {/* Invitaciones Recibidas Pendientes */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="text-xs font-bold uppercase tracking-wider text-[#2D1B22]/70">
                Invitaciones Recibidas Pendientes ({pendingInvites.length})
              </h3>
            </div>

            {pendingInvites.length === 0 ? (
              <div className="p-6 text-center bg-[#FAF7F8] rounded-xl border border-[#E1CCD4]/60 space-y-1">
                <p className="text-xs font-semibold text-[#2D1B22]/70">
                  No tienes invitaciones pendientes en este momento
                </p>
                <p className="text-[11px] text-[#2D1B22]/50">
                  Cuando un integrante familiar te envíe una invitación a su grupo, aparecerá aquí.
                </p>
              </div>
            ) : (
              <div className="space-y-2.5">
                {pendingInvites.map((inv) => {
                  const group = familyGroups.find((g) => g.id === inv.familyGroupId);
                  const inviter = users.find((u) => u.id === inv.invitedByUserId);

                  return (
                    <div
                      key={inv.id}
                      className="p-4 bg-[#FAF7F8] rounded-xl border border-[#E1CCD4] flex flex-col sm:flex-row sm:items-center justify-between gap-3"
                    >
                      <div className="space-y-1">
                        <div className="flex items-center gap-2">
                          <Users className="w-4 h-4 text-[#69042A]" />
                          <h4 className="text-sm font-bold text-[#2D1B22]">
                            {group?.name || 'Grupo familiar'}
                          </h4>
                        </div>
                        <p className="text-xs text-[#2D1B22]/70">
                          Invitado por{' '}
                          <span className="font-semibold text-[#69042A]">
                            {inviter?.fullName || 'Un integrante familiar'}
                          </span>{' '}
                          ({inviter?.username ? `@${inviter.username}` : ''})
                        </p>
                        <p className="text-[10px] text-[#2D1B22]/50">
                          Recibida: {formatDate(inv.createdAt)}
                        </p>
                      </div>

                      <div className="flex items-center gap-2 self-end sm:self-center">
                        <button
                          onClick={() => respondToInvitation(inv.id, false)}
                          className="px-3 py-1.5 text-xs font-medium text-[#2D1B22]/70 hover:text-rose-700 hover:bg-rose-50 rounded-lg transition-colors border border-[#E1CCD4]/60 cursor-pointer"
                        >
                          Rechazar
                        </button>
                        <button
                          onClick={() => respondToInvitation(inv.id, true)}
                          className="px-3.5 py-1.5 text-xs font-semibold text-white bg-[#69042A] hover:bg-[#520320] active:bg-[#3E0218] rounded-lg shadow-xs transition-colors flex items-center gap-1 cursor-pointer"
                        >
                          <Check className="w-3.5 h-3.5" />
                          <span>Aceptar</span>
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {/* Estado de Invitaciones Enviadas */}
          {sentInvites.length > 0 && (
            <div className="space-y-3 pt-4 border-t border-[#E1CCD4]/60">
              <h3 className="text-xs font-bold uppercase tracking-wider text-[#2D1B22]/70">
                Invitaciones Enviadas por Ti ({sentInvites.length})
              </h3>
              <div className="space-y-2 max-h-40 overflow-y-auto pr-1">
                {sentInvites.map((inv) => {
                  const targetUser = users.find((u) => u.id === inv.invitedUserId);
                  const group = familyGroups.find((g) => g.id === inv.familyGroupId);

                  return (
                    <div
                      key={inv.id}
                      className="p-2.5 bg-[#FAF7F8] rounded-xl border border-[#E1CCD4]/60 flex items-center justify-between text-xs"
                    >
                      <div>
                        <span className="font-semibold text-[#2D1B22]">
                          {targetUser?.fullName || 'Usuario'}
                        </span>{' '}
                        <span className="text-[#2D1B22]/50">al grupo {group?.name}</span>
                      </div>
                      <span
                        className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${
                          inv.status === 'accepted'
                            ? 'bg-emerald-100 text-emerald-800'
                            : inv.status === 'declined'
                            ? 'bg-rose-100 text-rose-800'
                            : 'bg-[#E1CCD4]/60 text-[#69042A]'
                        }`}
                      >
                        {inv.status === 'accepted'
                          ? 'Aceptada'
                          : inv.status === 'declined'
                          ? 'Rechazada'
                          : 'Pendiente'}
                      </span>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>

        <div className="px-6 py-3 bg-[#FAF7F8] border-t border-[#E1CCD4]/70 flex justify-end">
          <button
            onClick={closeInvitationsModal}
            className="px-4 py-2 text-xs font-semibold text-[#2D1B22]/70 hover:text-[#2D1B22] rounded-xl hover:bg-white transition-colors cursor-pointer"
          >
            Cerrar
          </button>
        </div>
      </div>
    </div>
  );
};
