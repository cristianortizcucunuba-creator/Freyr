package com.freyr.app.data.datos;

/**
 * Modelo de resumen e historial financiero completo de Freyr.
 * Calcula dinámicamente:
 * - Total debido: Monto total de todas las deudas registradas a lo largo de la historia.
 * - Total pagado: Monto total registrado en pagos.
 * - Total pendiente: Monto que continúa pendiente por saldar.
 */
public class FinancialSummary {
    private int cantidadDeudasPendientes;
    private int cantidadDeudasPagadas;
    private double totalPagosPersonales;
    private double totalDeudasFamiliaresPendientes;
    private double montoTotalDeuda; // Total debido histórico
    private double montoTotalPagado; // Total pagado
    private double montoTotalPendiente; // Total pendiente

    public FinancialSummary() {
        this.cantidadDeudasPendientes = 0;
        this.cantidadDeudasPagadas = 0;
        this.totalPagosPersonales = 0.0;
        this.totalDeudasFamiliaresPendientes = 0.0;
        this.montoTotalDeuda = 0.0;
        this.montoTotalPagado = 0.0;
        this.montoTotalPendiente = 0.0;
    }

    public FinancialSummary(int cantidadDeudasPendientes, int cantidadDeudasPagadas, double totalPagosPersonales,
                            double totalDeudasFamiliaresPendientes, double montoTotalPendiente, double montoTotalPagado) {
        this.cantidadDeudasPendientes = cantidadDeudasPendientes;
        this.cantidadDeudasPagadas = cantidadDeudasPagadas;
        this.totalPagosPersonales = totalPagosPersonales;
        this.totalDeudasFamiliaresPendientes = totalDeudasFamiliaresPendientes;
        this.montoTotalPendiente = montoTotalPendiente;
        this.montoTotalPagado = montoTotalPagado;
        this.montoTotalDeuda = montoTotalPendiente + montoTotalPagado;
    }

    public FinancialSummary(int cantidadDeudasPendientes, int cantidadDeudasPagadas, double totalPagosPersonales,
                            double totalDeudasFamiliaresPendientes, double montoTotalDeuda, double montoTotalPagado, double montoTotalPendiente) {
        this.cantidadDeudasPendientes = cantidadDeudasPendientes;
        this.cantidadDeudasPagadas = cantidadDeudasPagadas;
        this.totalPagosPersonales = totalPagosPersonales;
        this.totalDeudasFamiliaresPendientes = totalDeudasFamiliaresPendientes;
        this.montoTotalDeuda = montoTotalDeuda;
        this.montoTotalPagado = montoTotalPagado;
        this.montoTotalPendiente = montoTotalPendiente;
    }

    public int getCantidadDeudasPendientes() { return cantidadDeudasPendientes; }
    public int getCantidadDeudasPagadas() { return cantidadDeudasPagadas; }
    public double getTotalPagosPersonales() { return totalPagosPersonales; }
    public double getTotalDeudasFamiliaresPendientes() { return totalDeudasFamiliaresPendientes; }

    // Métodos en español y en inglés para máxima compatibilidad
    public double getTotalDebido() { return montoTotalDeuda > 0 ? montoTotalDeuda : (montoTotalPendiente + montoTotalPagado); }
    public double getMontoTotalDeuda() { return getTotalDebido(); }

    public double getTotalPagado() { return montoTotalPagado; }
    public double getMontoTotalPagado() { return montoTotalPagado; }

    public double getTotalPendiente() { return montoTotalPendiente; }
    public double getMontoTotalPendiente() { return montoTotalPendiente; }
}
