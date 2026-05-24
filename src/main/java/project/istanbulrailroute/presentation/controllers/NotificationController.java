package project.istanbulrailroute.presentation.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.istanbulrailroute.application.services.NotificationService;


import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<String>> getSystemAlerts() {
        return ResponseEntity.ok(notificationService.getActiveAlerts());
    }
}
