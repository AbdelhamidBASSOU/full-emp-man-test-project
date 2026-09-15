package com.abdel.employee_management.service.implementation;

import com.abdel.employee_management.dto.NotificationDTO;
import com.abdel.employee_management.model.Notification;
import com.abdel.employee_management.repository.NotificationRepository;
import com.abdel.employee_management.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void notify(String performedBy, String actionType, String message) {
        Notification notification = new Notification();
        notification.setPerformedBy(performedBy);
        notification.setActionType(actionType);
        notification.setMessage(message);

        Notification saved = notificationRepository.save(notification);
        messagingTemplate.convertAndSend("/topic/notifications", toDTO(saved));
    }

    @Override
    public List<NotificationDTO> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount() {
        return notificationRepository.countByIsReadFalse();
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        notificationRepository.markAllAsRead();
    }

    private NotificationDTO toDTO(Notification n) {
        return new NotificationDTO(n.getId(), n.getMessage(), n.getPerformedBy(), n.getActionType(), n.isRead(), n.getCreatedAt());
    }
}