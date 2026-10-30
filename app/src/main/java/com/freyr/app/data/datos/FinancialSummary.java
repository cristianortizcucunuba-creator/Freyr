
package com.freyr.app.data.datos;

/**
 * Modelo de resumen e historial financiero completo de Freyr.
 *
 * Calcula y almacena:
 * - Total debido: monto total de las deudas.
 * - Total pagado: monto total registrado en pagos.
 * - Total pendiente: monto que continúa pendiente por saldar.
 */
public class FinancialSummary {

    // =====================================================
    // 1. ATRIBUTOS
    // =====================================================

    private int cantidadDeudasPendientes;
    private int cantidadDeudasPagadas;

    private double totalPagosPersonales;
    private double totalDeudasFamiliaresPendientes;

    private double montoTotalDeuda;       // Total debido histórico
    private double montoTotalPagado;      // Total pagado
    private double montoTotalPendiente;   // Total pendiente


    // =====================================================
    // 2. CONSTRUCTOR POR DEFECTO
    // =====================================================

    public FinancialSummary() {
        this.cantidadDeudasPendientes = 0;
        this.cantidadDeudasPagadas = 0;
        this.totalPagosPersonales = 0.0;
        this.totalDeudasFamiliaresPendientes = 0.0;
        this.montoTotalDeuda = 0.0;
        this.montoTotalPagado = 0.0;
        this.montoTotalPendiente = 0.0;
    }


    // =====================================================
    // 3. CONSTRUCTOR CON CÁLCULO AUTOMÁTICO DEL TOTAL
    // =====================================================

    public FinancialSummary(
            int cantidadDeudasPendientes,
            int cantidadDeudasPagadas,
            double totalPagosPersonales,
            double totalDeudasFamiliaresPendientes,
            double montoTotalPendiente,
            double montoTotalPagado) {

        this.cantidadDeudasPendientes = cantidadDeudasPendientes;
        this.cantidadDeudasPagadas = cantidadDeudasPagadas;
        this.totalPagosPersonales = totalPagosPersonales;
        this.totalDeudasFamiliaresPendientes = totalDeudasFamiliaresPendientes;
        this.montoTotalPendiente = montoTotalPendiente;
        this.montoTotalPagado = montoTotalPagado;

        // El total debido se obtiene sumando lo pendiente y lo pagado.
        this.montoTotalDeuda = montoTotalPendiente + montoTotalPagado;
    }


    // =====================================================
    // 4. CONSTRUCTOR CON TODOS LOS VALORES
    // =====================================================

    public FinancialSummary(
            int cantidadDeudasPendientes,
            int cantidadDeudasPagadas,
            double totalPagosPersonales,
            double totalDeudasFamiliaresPendientes,
            double montoTotalDeuda,
            double montoTotalPagado,
            double montoTotalPendiente) {

        this.cantidadDeudasPendientes = cantidadDeudasPendientes;
        this.cantidadDeudasPagadas = cantidadDeudasPagadas;
        this.totalPagosPersonales = totalPagosPersonales;
        this.totalDeudasFamiliaresPendientes = totalDeudasFamiliaresPendientes;
        this.montoTotalDeuda = montoTotalDeuda;
        this.montoTotalPagado = montoTotalPagado;
        this.montoTotalPendiente = montoTotalPendiente;
    }


    // =====================================================
    // 5. MÉTODOS GET
    // =====================================================

    public int getCantidadDeudasPendientes() {
        return cantidadDeudasPendientes;
    }

    public int getCantidadDeudasPagadas() {
        return cantidadDeudasPagadas;
    }

    public double getTotalPagosPersonales() {
        return totalPagosPersonales;
    }

    public double getTotalDeudasFamiliaresPendientes() {
        return totalDeudasFamiliaresPendientes;
    }


    // =====================================================
    // 6. MÉTODOS PARA OBTENER EL TOTAL DEBIDO
    // =====================================================

    // Métodos en español y en inglés para compatibilidad.
    public double getTotalDebido() {
        return montoTotalDeuda > 0
                ? montoTotalDeuda
                : (montoTotalPendiente + montoTotalPagado);
    }

    public double getMontoTotalDeuda() {
        return getTotalDebido();
    }


    // =====================================================
    // 7. MÉTODOS PARA OBTENER EL TOTAL PAGADO
    // =====================================================

    public double getTotalPagado() {
        return montoTotalPagado;
    }

    public double getMontoTotalPagado() {
        return montoTotalPagado;
    }


    // =====================================================
    // 8. MÉTODOS PARA OBTENER EL TOTAL PENDIENTE
    // =====================================================

    public double getTotalPendiente() {
        return montoTotalPendiente;
    }

    public double getMontoTotalPendiente() {
        return montoTotalPendiente;
    }
}
