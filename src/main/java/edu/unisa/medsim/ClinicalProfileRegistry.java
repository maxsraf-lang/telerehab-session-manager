package edu.unisa.medsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Single place that knows which clinical profiles are available (Simple Factory / Registry).
 *
 * <p>The rest of the application, for example the UI that lists the profiles or the service
 * that creates a protocol, asks the registry for a profile by name and works with the
 * {@link ClinicalProfile} interface only. It never refers to a concrete profile class, so a
 * new profile is made available by registering it here.</p>
 *
 * <p>One instance is created in the composition root ({@code MainApp}) and passed to the
 * objects that need it.</p>
 */
public final class ClinicalProfileRegistry {

    private final Map<String, ClinicalProfile> profilesByName = new LinkedHashMap<>();

    /** Creates a registry containing the four profiles of the provided simulator. */
    public ClinicalProfileRegistry() {
        for (ClinicalProfileType type : ClinicalProfileType.values()) {
            register(type);
        }
    }

    /**
     * Adds a profile to the registry.
     *
     * @param profile the profile to add
     * @throws NullPointerException     if {@code profile} is null
     * @throws IllegalArgumentException if a profile with the same name is already registered
     */
    public void register(ClinicalProfile profile) {
        Objects.requireNonNull(profile, "profile must not be null");
        if (profilesByName.containsKey(profile.name())) {
            throw new IllegalArgumentException("Profile already registered: " + profile.name());
        }
        profilesByName.put(profile.name(), profile);
    }

    /**
     * Returns all registered profiles, in registration order.
     *
     * @return an unmodifiable list of profiles
     */
    public List<ClinicalProfile> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(profilesByName.values()));
    }

    /**
     * Looks up a profile by its name, for example {@code "CARDIAC_REHAB"}.
     *
     * @param name the profile name
     * @return the profile, or an empty {@link Optional} if no profile has that name
     */
    public Optional<ClinicalProfile> findByName(String name) {
        return Optional.ofNullable(profilesByName.get(name));
    }
}