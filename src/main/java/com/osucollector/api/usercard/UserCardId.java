package com.osucollector.api.usercard;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserCardId implements Serializable {
    private String userId;
    private Short cardId;
}