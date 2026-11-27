package com.medagenda.common.web;

import com.medagenda.common.error.BadRequestException;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class Pageables {

    private Pageables() {
    }

    /**
     * Rejects sort properties outside the allowed list. Without it, any entity path is accepted:
     * an unknown one becomes a 500, and "sort=user.passwordHash" would let a client infer hashes
     * from the ordering of the results.
     */
    public static Pageable allowSort(Pageable pageable, String... allowed) {
        Set<String> allowedSet = Set.of(allowed);
        for (Sort.Order order : pageable.getSort()) {
            if (!allowedSet.contains(order.getProperty())) {
                throw new BadRequestException("Campo de ordenação inválido: %s. Permitidos: %s."
                        .formatted(order.getProperty(), String.join(", ", allowed)));
            }
        }
        return pageable;
    }
}
