package com.abdel.employee_management.service;

import com.abdel.employee_management.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {

    void notify(String performedBy, String actionType, String message);

    List<NotificationDTO> getAllNotifications();

    long getUnreadCount();

    void markAllAsRead();
}