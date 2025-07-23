package com.itsm.notifications.application.service;

import com.itsm.notifications.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending email notifications
 * Handles email composition and delivery
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${notifications.email.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${notifications.email.from}")
    private String fromEmail;
    
    /**
     * Send notification email asynchronously
     */
    @Async
    public void sendNotificationEmail(Notification notification, String toEmail) {
        if (!emailEnabled) {
            log.debug("Email notifications are disabled");
            return;
        }
        
        if (toEmail == null || toEmail.trim().isEmpty()) {
            log.warn("No email address provided for notification: {}", notification.getId());
            return;
        }
        
        log.info("Sending email notification {} to {}", notification.getId(), toEmail);
        
        try {
            // Try to send HTML email with template
            if (sendHtmlEmail(notification, toEmail)) {
                log.info("Successfully sent HTML email notification: {}", notification.getId());
            } else {
                // Fallback to simple text email
                sendSimpleEmail(notification, toEmail);
                log.info("Successfully sent simple email notification: {}", notification.getId());
            }
            
        } catch (Exception e) {
            log.error("Failed to send email notification {}: {}", notification.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Send HTML email using Thymeleaf template
     */
    private boolean sendHtmlEmail(Notification notification, String toEmail) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(notification.getTitle());
            
            // Create template context
            Context context = new Context();
            context.setVariable("notification", notification);
            context.setVariable("title", notification.getTitle());
            context.setVariable("message", notification.getMessage());
            context.setVariable("type", notification.getType().getDisplayName());
            context.setVariable("priority", notification.getPriority().getDisplayName());
            
            // Add notification data as variables
            if (notification.getData() != null) {
                for (Map.Entry<String, Object> entry : notification.getData().entrySet()) {
                    context.setVariable(entry.getKey(), entry.getValue());
                }
            }
            
            // Select template based on notification type
            String templateName = getTemplateForNotificationType(notification.getType());
            String htmlContent = templateEngine.process(templateName, context);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            return true;
            
        } catch (Exception e) {
            log.warn("Failed to send HTML email, will try simple email: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Send simple text email as fallback
     */
    private void sendSimpleEmail(Notification notification, String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(notification.getTitle());
        
        // Create simple text content
        StringBuilder content = new StringBuilder();
        content.append(notification.getMessage()).append("\n\n");
        content.append("Type: ").append(notification.getType().getDisplayName()).append("\n");
        content.append("Priority: ").append(notification.getPriority().getDisplayName()).append("\n");
        content.append("Date: ").append(notification.getCreatedAt()).append("\n");
        
        if (notification.getTicketId() != null) {
            content.append("Ticket ID: ").append(notification.getTicketId()).append("\n");
        }
        
        content.append("\n---\n");
        content.append("This is an automated notification from the ITSM system.\n");
        content.append("Please do not reply to this email.");
        
        message.setText(content.toString());
        
        mailSender.send(message);
    }
    
    /**
     * Get template name for notification type
     */
    private String getTemplateForNotificationType(com.itsm.notifications.domain.model.NotificationType type) {
        return switch (type) {
            case TICKET_ASSIGNED -> "email/assignment-notification";
            case TICKET_REASSIGNED -> "email/reassignment-notification";
            case ASSIGNMENT_FAILED -> "email/assignment-failure-notification";
            case SLA_WARNING -> "email/sla-warning-notification";
            case TEAM_MEMBER_ADDED -> "email/team-member-notification";
            case SYSTEM_ALERT -> "email/system-alert-notification";
            default -> "email/generic-notification";
        };
    }
    
    /**
     * Send test email to verify configuration
     */
    public void sendTestEmail(String toEmail) {
        if (!emailEnabled) {
            throw new IllegalStateException("Email notifications are disabled");
        }
        
        log.info("Sending test email to: {}", toEmail);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("ITSM Notifications Test Email");
            message.setText("This is a test email from the ITSM notifications service.\n\n" +
                          "If you receive this email, the email configuration is working correctly.\n\n" +
                          "Timestamp: " + java.time.LocalDateTime.now());
            
            mailSender.send(message);
            log.info("Successfully sent test email to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send test email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send test email", e);
        }
    }
}
