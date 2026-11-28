package edu.unisa.medsim;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Records the current behaviour of the simulator into CSV files (the "golden master").
 *
 * <p>Run it ONCE, on the original code, before any refactoring. The generated files are
 * committed and must never be regenerated after the refactoring has started.</p>
 */
public final class GoldenMasterGenerator {

    private static final Path OUTPUT_DIR = Path.of("src", "test", "resources", "characterization");

    private GoldenMasterGenerator() {
    }

    public static void main(String[] args) throws IOException {
        Files.createDirectories(OUTPUT_DIR);
        for (ClinicalProfileType profile : ClinicalProfileType.values()) {
            for (long seed : CharacterizationScenario.SEEDS) {
                Path file = OUTPUT_DIR.resolve(CharacterizationScenario.fileName(profile, seed));
                Files.write(file, CharacterizationScenario.run(profile, seed));
                System.out.println("Written " + file);
            }
        }
    }
}