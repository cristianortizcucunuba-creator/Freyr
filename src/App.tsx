/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';
import { AppProvider, useApp } from './context/AppContext';
import { Navbar } from './components/Navbar';
import { AuthView } from './components/AuthView';
import { Dashboard } from './components/Dashboard';
import { PersonalFinances } from './components/PersonalFinances';
import { FamilyGroups } from './components/FamilyGroups';
import { InvitationsView } from './components/InvitationsView';

// Modales globales del sistema
import { AddDebtModal } from './components/AddDebtModal';
import { AddPaymentModal } from './components/AddPaymentModal';
import { DebtDetailModal } from './components/DebtDetailModal';
import { CreateGroupModal } from './components/CreateGroupModal';
import { InvitationsModal } from './components/InvitationsModal';
import { ProfileModal } from './components/ProfileModal';

/**
 * Contenedor principal de la interfaz de usuario.
 * Gestiona el enrutamiento visual de pestañas (Panel de control, Finanzas personales,
 * Grupos familiares e Invitaciones), así como el montaje de modales globales y el pie de página.
 */
const AppContent: React.FC = () => {
  const { currentUser, activeTab } = useApp();

  if (!currentUser) {
    return <AuthView />;
  }

  return (
    <div className="min-h-screen bg-[#FAF7F8] flex flex-col text-[#2D1B22] selection:bg-[#E1CCD4] selection:text-[#69042A]">
      <Navbar />

      <main className="flex-1 pb-16">
        {activeTab === 'dashboard' && <Dashboard />}
        {activeTab === 'personal' && <PersonalFinances />}
        {activeTab === 'groups' && <FamilyGroups />}
        {activeTab === 'invitations' && <InvitationsView />}
      </main>

      {/* Modales globales del sistema */}
      <AddDebtModal />
      <AddPaymentModal />
      <DebtDetailModal />
      <CreateGroupModal />
      <InvitationsModal />
      <ProfileModal />

      {/* Pie de página con diseño minimalista */}
      <footer className="border-t border-[#E1CCD4]/60 py-6 bg-[#FAF7F8]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between text-xs text-[#2D1B22]/50 gap-2">
          <p>© {new Date().getFullYear()} Freyr · Registro financiero personal y familiar limpio</p>
          <p>Registros manuales y privados · Sin necesidad de credenciales bancarias</p>
        </div>
      </footer>
    </div>
  );
};

/**
 * Componente raíz de la aplicación.
 * Proporciona el contexto global de estado (`AppProvider`) a todos los subcomponentes.
 */
export default function App() {
  return (
    <AppProvider>
      <AppContent />
    </AppProvider>
  );
}

