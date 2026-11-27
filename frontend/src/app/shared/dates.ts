/*
 * Date helpers that always work in the user's local time zone.
 * The API exchanges clinic-local values without offset ("2026-12-07", "2026-12-07T09:00:00"),
 * so toISOString() (UTC) must never be used to build them.
 */

/** Date -> "yyyy-mm-dd" (local). */
export function toIsoDate(date: Date): string {
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}

/** Today as "yyyy-mm-dd" (local). After 21:00 in Brazil, toISOString() would already say tomorrow. */
export function todayIsoDate(): string {
  return toIsoDate(new Date());
}

/** Local midnight of today plus N days. */
export function startOfDayPlus(days: number): Date {
  const date = new Date();
  date.setHours(0, 0, 0, 0);
  date.setDate(date.getDate() + days);
  return date;
}
