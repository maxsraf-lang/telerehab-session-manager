package edu.unisa.medsim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Characterization tests (golden master) for {@link PatientTelemetrySimulator}.
 *
 * <p>They replay a fixed scenario for every profile and seed and compare each sample with
 * the values recorded from the original code. They must stay green, without changing the
 * recorded files, during and after the Open/Closed refactoring (US-12).</p>
 */
class ClinicalProfileCharacterizationTest {

    private static final double TOLERANCE = 1e-9;

    static Stream<Arguments> profilesAndSeeds() {
        List<Arguments> cases = new ArrayList<>();
        for (ClinicalProfileType profile : ClinicalProfileType.values()) {
            for (long seed : CharacterizationScenario.SEEDS) {
                cases.add(Arguments.of(profile, seed));
            }
        }
        return cases.stream();
    }

    @ParameterizedTest(name = "{0}, seed {1}")
    @MethodSource("profilesAndSeeds")
    void simulatorReproducesRecordedTrajectory(ClinicalProfileType profile, long seed) throws IOException {
        List<String> expected = readGoldenMaster(profile, seed);
        List<String> actual = CharacterizationScenario.run(profile, seed);

        assertEquals(expected.size(), actual.size(), "number of steps");
        for (int i = 0; i < expected.size(); i++) {
            String[] e = expected.get(i).split(";");
            String[] a = actual.get(i).split(";");
            String where = profile + " seed " + seed + " step " + (i + 1);
            assertEquals(Integer.parseInt(e[0]), Integer.parseInt(a[0]), where + " elapsedSeconds");
            assertEquals(Double.parseDouble(e[1]), Double.parseDouble(a[1]), TOLERANCE, where + " heart rate");
            assertEquals(Double.parseDouble(e[2]), Double.parseDouble(a[2]), TOLERANCE, where + " SpO2");
            assertEquals(Double.parseDouble(e[3]), Double.parseDouble(a[3]), TOLERANCE, where + " fatigue");
            assertEquals(Integer.parseInt(e[4]), Integer.parseInt(a[4]), where + " pain");
        }
    }

    private static List<String> readGoldenMaster(ClinicalProfileType profile, long seed) throws IOException {
        String resource = "/characterization/" + CharacterizationScenario.fileName(profile, seed);
        try (InputStream in = ClinicalProfileCharacterizationTest.class.getResourceAsStream(resource)) {
            assertNotNull(in, "missing golden master file " + resource);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
        }
    }
}