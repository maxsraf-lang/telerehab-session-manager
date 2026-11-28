package edu.unisa.medsim;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ClinicalProfileRegistry}. */
class ClinicalProfileRegistryTest {

    private ClinicalProfileRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ClinicalProfileRegistry();
    }

    @Test
    void containsTheFourProvidedProfilesInOrder() {
        List<ClinicalProfile> all = registry.getAll();

        assertEquals(4, all.size());
        assertEquals(List.of(ClinicalProfileType.ATHLETIC, ClinicalProfileType.CARDIAC_REHAB,
                ClinicalProfileType.FRAGILE_ELDERLY, ClinicalProfileType.PULMONARY_COPD), all);
    }

    @Test
    void findsAProfileByName() {
        assertSame(ClinicalProfileType.CARDIAC_REHAB, registry.findByName("CARDIAC_REHAB").orElseThrow());
    }

    @Test
    void returnsEmptyForAnUnknownOrNullName() {
        assertTrue(registry.findByName("UNKNOWN").isEmpty());
        assertTrue(registry.findByName(null).isEmpty());
    }

    @Test
    void rejectsADuplicateName() {
        assertThrows(IllegalArgumentException.class, () -> registry.register(ClinicalProfileType.ATHLETIC));
    }

    @Test
    void rejectsANullProfile() {
        assertThrows(NullPointerException.class, () -> registry.register(null));
    }

    @Test
    void returnedListCannotBeModified() {
        List<ClinicalProfile> all = registry.getAll();

        assertThrows(UnsupportedOperationException.class, () -> all.add(ClinicalProfileType.ATHLETIC));
    }
}