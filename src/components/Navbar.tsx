import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import {
  Wallet,
  Users,
  Bell,
  UserCheck,
  LogOut,
  ChevronDown,
  LayoutDashboard,
  Shield,
  Menu,
  X,
  Plus
} from 'lucide-react';

export const Navbar: React.FC = () => {
  const {
    currentUser,
    users,
    activeTab,
    setActiveTab,
    logout,
    switchUser,
    openProfileModal,
    openAddDebtModal,
    openCreateGroupModal,
    getUserPendingInvitations,
  } = useApp();

  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const pendingInvitations = getUserPendingInvitations();

  if (!currentUser) return null;

  return (
    <header className="sticky top-0 z-30 bg-[#FAF7F8]/90 backdrop-blur-md border-b border-[#E1CCD4]/70">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Zone 1: Single text element wordmark */}
          <div className="flex items-center gap-3">
            <button
              onClick={() => {
                setActiveTab('dashboard');
                setIsMobileMenuOpen(false);
              }}
              className="group flex items-center gap-2.5 text-left focus:outline-none"
            >
              <div className="w-8 h-8 rounded-lg bg-[#69042A] text-[#FAF7F8] flex items-center justify-center font-bold text-sm tracking-wider shadow-sm transition-transform group-hover:scale-105">
                F
              </div>
              <span className="text-xl font-bold tracking-tight text-[#69042A]">
                Freyr
              </span>
            </button>
          </div>

          {/* Zone 2: 4 Clean single-line text navigation links */}
          <nav className="hidden md:flex items-center space-x-1 lg:space-x-2">
            <button
              onClick={() => setActiveTab('dashboard')}
              className={`px-3 py-1.5 text-sm font-medium rounded-lg transition-colors flex items-center gap-2 whitespace-nowrap ${
                activeTab === 'dashboard'
                  ? 'text-[#69042A] bg-[#E1CCD4]/50'
                  : 'text-[#2D1B22]/70 hover:text-[#69042A] hover:bg-[#E1CCD4]/20'
              }`}
            >
              <LayoutDashboard className="w-4 h-4" />
              <span>Inicio</span>
            </button>

            <button
              onClick={() => setActiveTab('personal')}
              className={`px-3 py-1.5 text-sm font-medium rounded-lg transition-colors flex items-center gap-2 whitespace-nowrap ${
                activeTab === 'personal'
                  ? 'text-[#69042A] bg-[#E1CCD4]/50'
                  : 'text-[#2D1B22]/70 hover:text-[#69042A] hover:bg-[#E1CCD4]/20'
              }`}
            >
              <Wallet className="w-4 h-4" />
              <span>Finanzas personales</span>
            </button>

            <button
              onClick={() => setActiveTab('groups')}
              className={`px-3 py-1.5 text-sm font-medium rounded-lg transition-colors flex items-center gap-2 whitespace-nowrap ${
                activeTab === 'groups'
                  ? 'text-[#69042A] bg-[#E1CCD4]/50'
                  : 'text-[#2D1B22]/70 hover:text-[#69042A] hover:bg-[#E1CCD4]/20'
              }`}
            >
              <Users className="w-4 h-4" />
              <span>Grupos familiares</span>
            </button>

            <button
              onClick={() => setActiveTab('invitations')}
              className={`relative px-3 py-1.5 text-sm font-medium rounded-lg transition-colors flex items-center gap-2 whitespace-nowrap ${
                activeTab === 'invitations'
                  ? 'text-[#69042A] bg-[#E1CCD4]/50'
                  : 'text-[#2D1B22]/70 hover:text-[#69042A] hover:bg-[#E1CCD4]/20'
              }`}
            >
              <Bell className="w-4 h-4" />
              <span>Invitaciones</span>
              {pendingInvitations.length > 0 && (
                <span className="ml-1 px-1.5 py-0.2 text-[11px] font-semibold bg-[#69042A] text-white rounded-full">
                  {pendingInvitations.length}
                </span>
              )}
            </button>
          </nav>

          {/* Zona 3: Acciones principales y menú de usuario */}
          <div className="flex items-center gap-2 sm:gap-3">
            <button
              onClick={() => openAddDebtModal()}
              className="hidden sm:inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-white bg-[#69042A] hover:bg-[#520320] active:bg-[#3E0218] rounded-lg transition-colors shadow-sm whitespace-nowrap"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Añadir deuda</span>
            </button>

            {/* Profile Dropdown */}
            <div className="relative">
              <button
                onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                className="flex items-center gap-2 p-1.5 rounded-lg hover:bg-[#E1CCD4]/30 transition-colors focus:outline-none"
              >
                <div
                  className="w-8 h-8 rounded-full flex items-center justify-center text-white text-xs font-semibold uppercase shadow-xs"
                  style={{ backgroundColor: currentUser.avatarColor || '#69042A' }}
                >
                  {currentUser.fullName.slice(0, 2)}
                </div>
                <div className="hidden xl:block text-left text-xs">
                  <div className="font-semibold text-[#2D1B22] truncate max-w-[110px]">
                    {currentUser.fullName}
                  </div>
                  <div className="text-[#2D1B22]/60 truncate max-w-[110px]">
                    @{currentUser.username}
                  </div>
                </div>
                <ChevronDown className="w-3.5 h-3.5 text-[#2D1B22]/60" />
              </button>

              {isUserMenuOpen && (
                <div className="absolute right-0 mt-2 w-64 bg-white rounded-xl shadow-lg border border-[#E1CCD4]/80 py-2 z-50 animate-in fade-in zoom-in-95 duration-100">
                  <div className="px-4 py-2 border-b border-[#E1CCD4]/40">
                    <p className="text-xs font-semibold text-[#2D1B22]">{currentUser.fullName}</p>
                    <p className="text-xs text-[#2D1B22]/60 truncate">{currentUser.email}</p>
                    <span className="inline-block mt-1 text-[11px] text-[#69042A] bg-[#E1CCD4]/50 px-2 py-0.5 rounded">
                      @{currentUser.username}
                    </span>
                  </div>

                  <div className="py-1">
                    <button
                      onClick={() => {
                        setIsUserMenuOpen(false);
                        openProfileModal();
                      }}
                      className="w-full text-left px-4 py-2 text-xs text-[#2D1B22] hover:bg-[#FAF7F8] flex items-center gap-2.5 transition-colors"
                    >
                      <UserCheck className="w-3.5 h-3.5 text-[#69042A]" />
                      <span>Editar perfil</span>
                    </button>

                    <button
                      onClick={() => {
                        setIsUserMenuOpen(false);
                        openCreateGroupModal();
                      }}
                      className="w-full text-left px-4 py-2 text-xs text-[#2D1B22] hover:bg-[#FAF7F8] flex items-center gap-2.5 transition-colors"
                    >
                      <Users className="w-3.5 h-3.5 text-[#69042A]" />
                      <span>Crear grupo familiar</span>
                    </button>
                  </div>

                  {/* Selector rápido de usuario para demostración y evaluación */}
                  <div className="border-t border-[#E1CCD4]/40 pt-2 pb-1">
                    <div className="px-4 py-1 text-[10px] uppercase tracking-wider text-[#2D1B22]/50 font-semibold flex items-center gap-1">
                      <Shield className="w-3 h-3" />
                      <span>Cambiar usuario (Pruebas)</span>
                    </div>
                    {users.map((u) => (
                      <button
                        key={u.id}
                        onClick={() => {
                          switchUser(u.id);
                          setIsUserMenuOpen(false);
                        }}
                        className={`w-full text-left px-4 py-1.5 text-xs flex items-center justify-between transition-colors ${
                          u.id === currentUser.id
                            ? 'bg-[#E1CCD4]/30 font-semibold text-[#69042A]'
                            : 'text-[#2D1B22]/80 hover:bg-[#FAF7F8]'
                        }`}
                      >
                        <span className="truncate">{u.fullName}</span>
                        <span className="text-[10px] text-neutral-400 font-mono">@{u.username}</span>
                      </button>
                    ))}
                  </div>

                  <div className="border-t border-[#E1CCD4]/40 pt-1">
                    <button
                      onClick={() => {
                        setIsUserMenuOpen(false);
                        logout();
                      }}
                      className="w-full text-left px-4 py-2 text-xs text-rose-700 hover:bg-rose-50 flex items-center gap-2.5 transition-colors"
                    >
                      <LogOut className="w-3.5 h-3.5" />
                      <span>Cerrar sesión</span>
                    </button>
                  </div>
                </div>
              )}
            </div>

            {/* Alternador de menú móvil */}
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="md:hidden p-2 rounded-lg text-[#2D1B22] hover:bg-[#E1CCD4]/30 transition-colors"
            >
              {isMobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>
        </div>
      </div>

      {/* Menú lateral desplegable para móvil */}
      {isMobileMenuOpen && (
        <div className="md:hidden border-t border-[#E1CCD4]/70 bg-white px-4 pt-3 pb-5 space-y-2">
          <button
            onClick={() => {
              setActiveTab('dashboard');
              setIsMobileMenuOpen(false);
            }}
            className={`w-full text-left px-3 py-2 text-sm font-medium rounded-lg flex items-center gap-2.5 ${
              activeTab === 'dashboard' ? 'bg-[#E1CCD4]/50 text-[#69042A]' : 'text-[#2D1B22]/80'
            }`}
          >
            <LayoutDashboard className="w-4 h-4" />
            <span>Inicio</span>
          </button>

          <button
            onClick={() => {
              setActiveTab('personal');
              setIsMobileMenuOpen(false);
            }}
            className={`w-full text-left px-3 py-2 text-sm font-medium rounded-lg flex items-center gap-2.5 ${
              activeTab === 'personal' ? 'bg-[#E1CCD4]/50 text-[#69042A]' : 'text-[#2D1B22]/80'
            }`}
          >
            <Wallet className="w-4 h-4" />
            <span>Finanzas personales</span>
          </button>

          <button
            onClick={() => {
              setActiveTab('groups');
              setIsMobileMenuOpen(false);
            }}
            className={`w-full text-left px-3 py-2 text-sm font-medium rounded-lg flex items-center gap-2.5 ${
              activeTab === 'groups' ? 'bg-[#E1CCD4]/50 text-[#69042A]' : 'text-[#2D1B22]/80'
            }`}
          >
            <Users className="w-4 h-4" />
            <span>Grupos familiares</span>
          </button>

          <button
            onClick={() => {
              setActiveTab('invitations');
              setIsMobileMenuOpen(false);
            }}
            className={`w-full text-left px-3 py-2 text-sm font-medium rounded-lg flex items-center justify-between ${
              activeTab === 'invitations' ? 'bg-[#E1CCD4]/50 text-[#69042A]' : 'text-[#2D1B22]/80'
            }`}
          >
            <div className="flex items-center gap-2.5">
              <Bell className="w-4 h-4" />
              <span>Invitaciones</span>
            </div>
            {pendingInvitations.length > 0 && (
              <span className="px-2 py-0.5 text-xs font-semibold bg-[#69042A] text-white rounded-full">
                {pendingInvitations.length}
              </span>
            )}
          </button>

          <div className="pt-2 border-t border-[#E1CCD4]/50 flex gap-2">
            <button
              onClick={() => {
                openAddDebtModal();
                setIsMobileMenuOpen(false);
              }}
              className="flex-1 py-2 text-xs font-semibold text-white bg-[#69042A] rounded-lg text-center"
            >
              + Añadir deuda
            </button>
            <button
              onClick={() => {
                openProfileModal();
                setIsMobileMenuOpen(false);
              }}
              className="flex-1 py-2 text-xs font-semibold text-[#69042A] bg-[#E1CCD4]/60 rounded-lg text-center"
            >
              Editar perfil
            </button>
          </div>
        </div>
      )}
    </header>
  );
};
