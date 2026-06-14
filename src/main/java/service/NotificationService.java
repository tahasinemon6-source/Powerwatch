package service;

import dao.NotificationDAO;
import model.Notification;

public class NotificationService {
    private final NotificationDAO notificationDAO;

    public NotificationService() {
        this.notificationDAO = new NotificationDAO();
    }

    public NotificationService(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    public void logNotification(Integer outageId, String message) {
        Notification notif = new Notification(outageId, message);
        notificationDAO.addNotification(notif);
    }

    public void notifyNewOutage(int outageId, String area, String cause, boolean isCritical) {
        String msg;
        if (isCritical) {
            msg = "CRITICAL ALERT: New outage reported in " + area + " (Critical Infrastructure Area Affected). Cause: " + cause + ".";
        } else {
            msg = "Outage Reported: New outage in " + area + ". Cause: " + cause + ".";
        }
        logNotification(outageId, msg);
    }

    public void notifyTechnicianAssigned(int outageId, String area, String techName, String teamName) {
        String msg = "Dispatch: Technician " + techName + " (" + teamName + ") assigned to outage #" + outageId + " in " + area + ".";
        logNotification(outageId, msg);
    }

    public void notifyRepairStarted(int outageId, String area) {
        String msg = "Update: Repair started on outage #" + outageId + " in " + area + ".";
        logNotification(outageId, msg);
    }

    public void notifyPowerRestored(int outageId, String area) {
        String msg = "Restored: Power successfully restored in " + area + " (Outage #" + outageId + ").";
        logNotification(outageId, msg);
    }
}
