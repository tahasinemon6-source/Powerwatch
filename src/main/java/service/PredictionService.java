package service;

import dao.OutageDAO;

public class PredictionService {
    private final OutageDAO outageDAO;

    public PredictionService() {
        this.outageDAO = new OutageDAO();
    }

    public PredictionService(OutageDAO outageDAO) {
        this.outageDAO = outageDAO;
    }

    /**
     * Predicts the restoration time in hours based on historical outage data for the same cause.
     * If no historical data exists, returns a default fallback duration based on standard guidelines.
     */
    public double predictRestorationTimeHours(String cause) {
        if (cause == null || cause.trim().isEmpty()) {
            return 2.0; // General default
        }

        double avgTime = outageDAO.getAverageRestorationTimeHours(cause);
        if (avgTime > 0) {
            // Round to 1 decimal place
            return Math.round(avgTime * 10.0) / 10.0;
        }

        // Fallback guidelines for standard causes
        switch (cause) {
            case "Transformer Failure":
                return 2.5;
            case "Grid Overload":
                return 3.0;
            case "Weather":
                return 4.0;
            case "Equipment Failure":
                return 2.0;
            case "Maintenance":
                return 1.5;
            case "Accident":
                return 3.5;
            default:
                return 2.0;
        }
    }
}
