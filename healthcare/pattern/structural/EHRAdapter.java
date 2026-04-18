package com.healthcare.pattern.structural;

import com.healthcare.model.Prescription;

/**
 * ADAPTER PATTERN (Structural)
 * Member: Karthikeya Thotamsetty (PES1UG23CS287)
 * Adapts our Prescription model to a legacy EHR system interface.
 */
public class EHRAdapter {

    // Legacy EHR system interface (simulated)
    public interface LegacyEHR {
        void uploadHL7(String hl7Message);
    }

    // Our internal interface
    public interface EHRExporter {
        void export(Prescription rx);
    }

    // Adapter bridges the gap
    public static class PrescriptionToEHR implements EHRExporter {
        private final LegacyEHR legacyEHR;

        public PrescriptionToEHR(LegacyEHR legacyEHR) {
            this.legacyEHR = legacyEHR;
        }

        @Override
        public void export(Prescription rx) {
            // Convert Prescription to HL7 format (simplified)
            String hl7 = "MSH|^~\\&|CLINIC|||" + rx.getRxId() + "|||RDE^O11|||2.5\n"
                    + "RXE|" + rx.getStatus().name() + "|";
            legacyEHR.uploadHL7(hl7);
        }
    }
}
