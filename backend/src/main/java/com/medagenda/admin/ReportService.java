package com.medagenda.admin;

import com.medagenda.admin.report.DoctorReportRow;
import com.medagenda.common.error.BadRequestException;
import com.medagenda.scheduling.AppointmentRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportService {

    static final int MAX_RANGE_DAYS = 366;

    private final AppointmentRepository appointmentRepository;

    public ReportService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public record Report(LocalDate from, LocalDate to, StatusSummary summary, List<DoctorReportRow> doctors) {

        /** Same definition as in each row: completed / (completed + no-show), in percent. */
        public double attendanceRate() {
            long finished = summary.completed() + summary.noShow();
            return finished == 0 ? 0 : summary.completed() * 100.0 / finished;
        }

        public boolean hasFinishedVisits() {
            return summary.completed() + summary.noShow() > 0;
        }
    }

    /** Period [from, to], both inclusive. */
    public Report report(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new BadRequestException("A data final não pode ser anterior à data inicial.");
        }
        if (ChronoUnit.DAYS.between(from, to) >= MAX_RANGE_DAYS) {
            throw new BadRequestException("O período do relatório pode ter no máximo um ano.");
        }
        var start = from.atStartOfDay();
        var end = to.plusDays(1).atStartOfDay();
        return new Report(from, to,
                StatusSummary.from(appointmentRepository.countByStatus(start, end)),
                appointmentRepository.reportByDoctor(start, end));
    }
}
