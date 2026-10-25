/**
 * Da formato monetario a un valor numérico utilizando el símbolo de divisa y separadores de miles.
 *
 * @param amount - Cantidad monetaria a formatear.
 * @returns Cadena de texto formateada (ej. "$3,000,000" o "$120,000.50").
 */
export function formatCurrency(amount: number): string {
  if (isNaN(amount)) return '$0';
  
  // Determina si el número tiene decimales para incluirlos en el resultado
  const hasDecimals = amount % 1 !== 0;
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    minimumFractionDigits: hasDecimals ? 2 : 0,
    maximumFractionDigits: 2,
  }).format(amount).replace('COP', '$').trim();
}

/**
 * Convierte una cadena de fecha ISO o YYYY-MM-DD en una fecha legible en español.
 *
 * @param dateStr - Cadena de fecha a convertir.
 * @returns Fecha en formato legible (ej. "15 feb 2026").
 */
export function formatDate(dateStr: string): string {
  try {
    const date = new Date(dateStr);
    return new Intl.DateTimeFormat('es-ES', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
    }).format(date);
  } catch {
    return dateStr;
  }
}

