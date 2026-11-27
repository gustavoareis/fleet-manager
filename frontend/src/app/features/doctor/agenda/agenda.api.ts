import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DoctorAppointment } from './agenda.models';

/** Commands only; the agenda itself is read with httpResource in the component. */
@Injectable({ providedIn: 'root' })
export class AgendaApi {
  private readonly http = inject(HttpClient);

  complete(id: number, notes: string): Observable<DoctorAppointment> {
    return this.http.patch<DoctorAppointment>(`/api/appointments/${id}/complete`, { notes });
  }

  markNoShow(id: number, notes: string): Observable<DoctorAppointment> {
    return this.http.patch<DoctorAppointment>(`/api/appointments/${id}/no-show`, { notes });
  }

  saveNotes(id: number, notes: string): Observable<DoctorAppointment> {
    return this.http.put<DoctorAppointment>(`/api/appointments/${id}/notes`, { notes });
  }
}
