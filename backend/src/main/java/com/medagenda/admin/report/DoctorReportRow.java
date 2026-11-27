package com.medagenda.admin.report;

/** One line of the report: a doctor's appointment counters in the chosen period. */
public record DoctorReportRow(
        String doctorName,
        String specialty,
        long total,
        long completed,
        long noShow,
        long cancelled) {

    /** Share of finished visits where the patient actually came (completed / (completed + no-show)), in percent. */
    public double attendanceRate() {
        long attended = completed + noShow;
        return attended == 0 ? 0 : completed * 100.0 / attended;
    }

    public boolean hasFinishedVisits() {
        return completed + noShow > 0;
    }
}
