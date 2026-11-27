package com.medagenda.scheduling;

import jakarta.validation.constraints.Size;

/** Clinical notes; may be empty (clears the notes). */
public record NotesRequest(@Size(max = 2000) String notes) {
}
