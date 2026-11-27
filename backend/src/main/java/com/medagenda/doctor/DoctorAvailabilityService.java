package com.medagenda.doctor;

import com.medagenda.common.error.BusinessException;
import com.medagenda.common.error.NotFoundException;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DoctorAvailabilityService {

    private static final long NO_ID = -1L;
    private static final Comparator<DoctorAvailability> WEEKLY_ORDER = Comparator
            .comparing((DoctorAvailability window) -> window.getDayOfWeek())
            .thenComparing(window -> window.getStartTime());

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorService doctorService;

    public DoctorAvailabilityService(DoctorAvailabilityRepository availabilityRepository, DoctorService doctorService) {
        this.availabilityRepository = availabilityRepository;
        this.doctorService = doctorService;
    }

    /**
     * A doctor has at most a handful of weekly windows, so this list is not paginated.
     * Sorted in Java: the day is stored as text, so SQL would order it alphabetically (FRIDAY first).
     */
    public List<AvailabilityResponse> list(Long doctorId) {
        doctorService.getEntity(doctorId); // 404 for an unknown doctor instead of an empty list
        return availabilityRepository.findByDoctorId(doctorId).stream()
                .sorted(WEEKLY_ORDER)
                .map(AvailabilityResponse::from)
                .toList();
    }

    @Transactional
    public AvailabilityResponse create(Long doctorId, AvailabilityRequest request) {
        Doctor doctor = doctorService.getEntity(doctorId);
        validate(doctorId, request, NO_ID);
        DoctorAvailability availability = new DoctorAvailability(doctor, request.dayOfWeek(),
                request.startTime(), request.endTime(), request.slotDurationMinutes());
        return AvailabilityResponse.from(availabilityRepository.save(availability));
    }

    @Transactional
    public AvailabilityResponse update(Long doctorId, Long availabilityId, AvailabilityRequest request) {
        DoctorAvailability availability = getEntity(doctorId, availabilityId);
        validate(doctorId, request, availabilityId);
        availability.update(request.dayOfWeek(), request.startTime(), request.endTime(),
                request.slotDurationMinutes());
        return AvailabilityResponse.from(availability);
    }

    @Transactional
    public void delete(Long doctorId, Long availabilityId) {
        availabilityRepository.delete(getEntity(doctorId, availabilityId));
    }

    /** Rules involving several fields or other rows live here; single-field rules are Bean Validation. */
    private void validate(Long doctorId, AvailabilityRequest request, Long excludedId) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BusinessException("O horário final deve ser posterior ao horário inicial.");
        }
        long windowMinutes = Duration.between(request.startTime(), request.endTime()).toMinutes();
        if (windowMinutes < request.slotDurationMinutes()) {
            throw new BusinessException("A janela de atendimento é menor que a duração de uma consulta.");
        }
        if (availabilityRepository.existsOverlap(doctorId, request.dayOfWeek(), request.startTime(),
                request.endTime(), excludedId)) {
            throw new BusinessException("O período informado se sobrepõe a outra disponibilidade do médico.");
        }
    }

    /** Scoped by doctor so /doctors/1/availabilities/{id} can never touch another doctor's window. */
    private DoctorAvailability getEntity(Long doctorId, Long availabilityId) {
        return availabilityRepository.findByIdAndDoctorId(availabilityId, doctorId)
                .orElseThrow(() -> NotFoundException.of("Disponibilidade", availabilityId));
    }
}
