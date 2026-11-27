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

/** "yyyy-mm-dd" -> Date at local midnight. (new Date("2026-12-07") would be UTC midnight: the day before in Brazil.) */
export function parseIsoDate(value: string): Date {
  const [year, month, day] = value.split('-').map(Number);
  return new Date(year, month - 1, day);
}

export function addDays(date: Date, days: number): Date {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
}

/** Monday of the week containing the date (the clinic week runs Monday to Sunday). */
export function startOfWeek(date: Date): Date {
  const daysSinceMonday = (date.getDay() + 6) % 7;
  return addDays(new Date(date.getFullYear(), date.getMonth(), date.getDate()), -daysSinceMonday);
}

/** Completed years between a "yyyy-mm-dd" birth date and today. */
export function ageFrom(birthDate: string, today: Date = new Date()): number {
  const birth = parseIsoDate(birthDate);
  const hadBirthdayThisYear =
    today.getMonth() > birth.getMonth() ||
    (today.getMonth() === birth.getMonth() && today.getDate() >= birth.getDate());
  return today.getFullYear() - birth.getFullYear() - (hadBirthdayThisYear ? 0 : 1);
}

/** Local midnight of today plus N days. */
export function startOfDayPlus(days: number): Date {
  const date = new Date();
  date.setHours(0, 0, 0, 0);
  date.setDate(date.getDate() + days);
  return date;
}
