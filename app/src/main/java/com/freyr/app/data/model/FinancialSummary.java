package com.freyr.app.data.model;

/**
 * Modelo de resumen e historial financiero completo de Freyr.
 * Calcula dinámicamente:
 * - Total debido: Monto total de todas las deudas registradas a lo largo de la historia.
 * - Total pagado: Monto total registrado en pagos.
 * - Total pendiente: Monto que continúa pendiente por saldar.
 */
public class FinancialSummary {
    private int pendingDebtsCount;
    private int paidDebtsCount;
    private double personalPaymentsTotal;
    private double familyDebtsPendingTotal;
    private double totalDebtAmount; // Total debido histórico
    private double totalPaidAmount; // Total pagado
    private double totalPendingAmount; // Total pendiente

    public FinancialSummary() {
        this.pendingDebtsCount = 0;
        this.paidDebtsCount = 0;
        this.personalPaymentsTotal = 0.0;
        this.familyDebtsPendingTotal = 0.0;
        this.totalDebtAmount = 0.0;
        this.totalPaidAmount = 0.0;
        this.totalPendingAmount = 0.0;
    }

    public FinancialSummary(int pendingDebtsCount, int paidDebtsCount, double personalPaymentsTotal,
                            double familyDebtsPendingTotal, double totalPendingAmount, double totalPaidAmount) {
        this.pendingDebtsCount = pendingDebtsCount;
        this.paidDebtsCount = paidDebtsCount;
        this.personalPaymentsTotal = personalPaymentsTotal;
        this.familyDebtsPendingTotal = familyDebtsPendingTotal;
        this.totalPendingAmount = totalPendingAmount;
        this.totalPaidAmount = totalPaidAmount;
        this.totalDebtAmount = totalPendingAmount + totalPaidAmount;
    }

    public FinancialSummary(int pendingDebtsCount, int paidDebtsCount, double personalPaymentsTotal,
                            double familyDebtsPendingTotal, double totalDebtAmount, double totalPaidAmount, double totalPendingAmount) {
        this.pendingDebtsCount = pendingDebtsCount;
        this.paidDebtsCount = paidDebtsCount;
        this.personalPaymentsTotal = personalPaymentsTotal;
        this.familyDebtsPendingTotal = familyDebtsPendingTotal;
        this.totalDebtAmount = totalDebtAmount;
        this.totalPaidAmount = totalPaidAmount;
        this.totalPendingAmount = totalPendingAmount;
    }

    public int getPendingDebtsCount() { return pendingDebtsCount; }
    public int getPaidDebtsCount() { return paidDebtsCount; }
    public double getPersonalPaymentsTotal() { return personalPaymentsTotal; }
    public double getFamilyDebtsPendingTotal() { return familyDebtsPendingTotal; }

    // Métodos en español y en inglés para máxima compatibilidad
    public double getTotalDebido() { return totalDebtAmount > 0 ? totalDebtAmount : (totalPendingAmount + totalPaidAmount); }
    public double getTotalDebtAmount() { return getTotalDebido(); }

    public double getTotalPagado() { return totalPaidAmount; }
    public double getTotalPaidAmount() { return totalPaidAmount; }

    public double getTotalPendiente() { return totalPendingAmount; }
    public double getTotalPendingAmount() { return totalPendingAmount; }
}
