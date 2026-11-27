import { addDays, ageFrom, parseIsoDate, startOfWeek, toIsoDate } from './dates';

describe('dates', () => {
  it('formats a date in local time, without shifting the day', () => {
    // 22:30 local: toISOString() would already say the next day in UTC-3
    expect(toIsoDate(new Date(2026, 11, 7, 22, 30))).toBe('2026-12-07');
  });

  it('pads single-digit month and day', () => {
    expect(toIsoDate(new Date(2026, 0, 5))).toBe('2026-01-05');
  });

  it('parses an ISO date as local midnight', () => {
    const date = parseIsoDate('2026-12-07');
    expect([date.getFullYear(), date.getMonth(), date.getDate(), date.getHours()]).toEqual([
      2026, 11, 7, 0,
    ]);
  });

  it('round-trips parse and format', () => {
    expect(toIsoDate(parseIsoDate('2028-02-29'))).toBe('2028-02-29');
  });

  it('adds days across month and year boundaries', () => {
    expect(toIsoDate(addDays(new Date(2026, 11, 31), 1))).toBe('2027-01-01');
    expect(toIsoDate(addDays(new Date(2026, 2, 1), -1))).toBe('2026-02-28');
  });

  it('finds the Monday of the week, including for Sundays', () => {
    expect(toIsoDate(startOfWeek(new Date(2026, 10, 27)))).toBe('2026-11-23'); // Friday
    expect(toIsoDate(startOfWeek(new Date(2026, 10, 29)))).toBe('2026-11-23'); // Sunday
    expect(toIsoDate(startOfWeek(new Date(2026, 10, 23)))).toBe('2026-11-23'); // Monday
  });

  it('computes completed years only after the birthday', () => {
    const today = new Date(2026, 5, 15);
    expect(ageFrom('1990-06-15', today)).toBe(36);
    expect(ageFrom('1990-06-16', today)).toBe(35);
    expect(ageFrom('1990-05-01', today)).toBe(36);
  });
});
