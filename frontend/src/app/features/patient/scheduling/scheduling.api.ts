import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Doctor } from '../../../core/api/doctor.models';
import { Page } from '../../../core/http/page';
import { Appointment, BookingRequest } from './scheduling.models';

/**
 * Commands and user-driven queries return Observables (they compose with RxJS operators such
 * as debounceTime/switchMap). Declarative reads tied to signals use httpResource in components.
 */
@Injectable({ providedIn: 'root' })
export class SchedulingApi {
  private readonly http = inject(HttpClient);

  searchDoctors(specialtyId: number, name: string): Observable<Page<Doctor>> {
    const params = new HttpParams()
      .set('specialtyId', specialtyId)
      .set('name', name)
      .set('size', 50)
      .set('sort', 'name');
    return this.http.get<Page<Doctor>>('/api/doctors', { params });
  }

  book(request: BookingRequest): Observable<Appointment> {
    return this.http.post<Appointment>('/api/appointments', request);
  }

  cancel(appointmentId: number): Observable<Appointment> {
    return this.http.patch<Appointment>(`/api/appointments/${appointmentId}/cancel`, null);
  }
}
