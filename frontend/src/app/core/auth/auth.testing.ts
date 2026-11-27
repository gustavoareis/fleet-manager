import { AuthResponse, Role, UserInfo } from './auth.models';

/** Shared helpers for the auth specs: an in-memory Storage and a ready-made login response. */
export function memoryStorage(initial: Record<string, string> = {}): Storage {
  const data = new Map(Object.entries(initial));
  return {
    get length() {
      return data.size;
    },
    clear: () => data.clear(),
    getItem: (key: string) => data.get(key) ?? null,
    key: (index: number) => [...data.keys()][index] ?? null,
    removeItem: (key: string) => void data.delete(key),
    setItem: (key: string, value: string) => void data.set(key, value),
  };
}

export function userWithRole(role: Role, displayName = 'Maria Silva'): UserInfo {
  return { id: 1, email: 'x@medagenda.local', role, displayName, patientId: null, doctorId: null };
}

export function authResponse(role: Role, expiresIn = 7200, displayName = 'Maria Silva'): AuthResponse {
  return {
    accessToken: 'jwt-token',
    tokenType: 'Bearer',
    expiresIn,
    user: userWithRole(role, displayName),
  };
}
