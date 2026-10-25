import React from 'react';
import { useApp } from '../context/AppContext';
import { formatDate } from '../utils/currency';
import { Bell, Users, Check, X, Shield, Clock, ArrowRight } from 'lucide-react';

export const InvitationsView: React.FC = () => {
  const {
    currentUser,
    getUserPendingInvitations,
    familyGroups,
    users,
    respondToInvitation,
    invitations,
    setActiveTab,
  } = useApp();

  if (!currentUser) return null;

  const pendingInvites = getUserPendingInvitations();

  // Sent invitations by groups created by user
  const userCreatedGroupIds = familyGroups
    .filter((g) => g.creatorId === currentUser.id)
    .map((g) => g.id);

  const sentInvites = invitations.filter((inv) =>
    userCreatedGroupIds.includes(inv.familyGroupId)
  );

  return (
    <div className="space-y-8 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-[#69042A]">
          Invitaciones Familiares
        </h1>
        <p className="text-sm text-[#2D1B22]/70 mt-1">
          Acepta o administra invitaciones para colaborar en las finanzas familiares compartidas.
        </p>
      </div>

      {/* Invitaciones Recibidas */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-bold uppercase tracking-wider text-[#2D1B22]/70">
            Invitaciones Pendientes Recibidas ({pendingInvites.length})
          </h2>
        </div>

        {pendingInvites.length === 0 ? (
          <div className="p-10 text-center bg-white rounded-2xl border border-[#E1CCD4] space-y-2">
            <Bell className="w-8 h-8 text-[#69042A]/40 mx-auto" />
            <h3 className="text-sm font-bold text-[#2D1B22]">No tienes invitaciones pendientes</h3>
            <p className="text-xs text-[#2D1B22]/60 max-w-sm mx-auto">
              ¡Estás al día! Cuando un familiar o contacto te invite a su grupo familiar, aparecerá aquí para que puedas unirte.
            </p>
          </div>
        ) : (
          <div className="space-y-3">
            {pendingInvites.map((inv) => {
              const group = familyGroups.find((g) => g.id === inv.familyGroupId);
              const inviter = users.find((u) => u.id === inv.invitedByUserId);

              return (
                <div
                  key={inv.id}
                  className="p-5 bg-white rounded-2xl border border-[#E1CCD4] hover:border-[#69042A]/60 transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-4 shadow-xs"
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <div className="w-8 h-8 rounded-xl bg-[#FAF7F8] border border-[#E1CCD4] text-[#69042A] flex items-center justify-center">
                        <Users className="w-4 h-4" />
                      </div>
                      <div>
                        <h3 className="text-base font-bold text-[#2D1B22]">
                          {group?.name || 'Grupo familiar'}
                        </h3>
                        <p className="text-xs text-[#2D1B22]/60">
                          Invitado por <strong className="text-[#69042A]">{inviter?.fullName}</strong>{' '}
                          (@{inviter?.username}) · {formatDate(inv.createdAt)}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2.5 self-end sm:self-center">
                    <button
                      onClick={() => respondToInvitation(inv.id, false)}
                      className="px-4 py-2 text-xs font-semibold text-[#2D1B22]/70 hover:text-rose-700 hover:bg-rose-50 rounded-xl transition-colors border border-[#E1CCD4] cursor-pointer"
                    >
                      Rechazar
                    </button>
                    <button
                      onClick={() => {
                        respondToInvitation(inv.id, true);
                        setActiveTab('groups');
                      }}
                      className="px-5 py-2 text-xs font-semibold text-white bg-[#69042A] hover:bg-[#520320] active:bg-[#3E0218] rounded-xl shadow-xs transition-colors flex items-center gap-1.5 cursor-pointer"
                    >
                      <Check className="w-3.5 h-3.5" />
                      <span>Aceptar y unirse</span>
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
        <div className="space-y-4 pt-6 border-t border-[#E1CCD4]/60">
          <h2 className="text-sm font-bold uppercase tracking-wider text-[#2D1B22]/70">
            Invitaciones Enviadas por Ti ({sentInvites.length})
          </h2>

          <div className="bg-white rounded-2xl border border-[#E1CCD4] divide-y divide-[#FAF7F8] shadow-xs">
            {sentInvites.map((inv) => {
              const targetUser = users.find((u) => u.id === inv.invitedUserId);
              const group = familyGroups.find((g) => g.id === inv.familyGroupId);

              return (
                <div key={inv.id} className="p-4 flex items-center justify-between text-xs">
                  <div className="space-y-0.5">
                    <p className="font-bold text-[#2D1B22]">
                      {targetUser?.fullName} (@{targetUser?.username})
                    </p>
                    <p className="text-[#2D1B22]/50 text-[11px]">
                      Grupo: {group?.name} · Enviada el {formatDate(inv.createdAt)}
                    </p>
                  </div>

                  <span
                    className={`text-[11px] font-semibold px-2.5 py-1 rounded-full ${
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
                      : 'Pendiente de respuesta'}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};
