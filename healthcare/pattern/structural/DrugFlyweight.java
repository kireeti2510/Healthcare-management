package com.healthcare.pattern.structural;

import com.healthcare.model.Drug;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FLYWEIGHT PATTERN (Structural)
 * Member: K Sailakshmi Srinivas (PES1UG23CS271)
 * Shares Drug objects in-memory so the same drug isn't duplicated across thousands of prescription items.
 */
public class DrugFlyweight {
    private static final Map<String, Drug> pool = new HashMap<>();

    public static Drug getDrug(String code, String name, List<String> contraindications) {
        return pool.computeIfAbsent(code, k -> new Drug(code, name, contraindications));
    }

    public static Drug getDrug(String code) {
        return pool.get(code);
    }

    public static int poolSize() { return pool.size(); }
}
