/**
 * MailQueueListener 是一个用于监听 RabbitMQ 邮件队列的消息的监听器类。
 *
 * 当有消息被发送到邮件队列时，该监听器将处理消息并发送邮件通知。
 */
package com.hostelms.listener;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.logging.log4j.message.SimpleMessage;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "mail")
public class MailQueueListener {

    @Resource
    JavaMailSender sender;

    @Value("${spring.mail.username}")
    String username;

    /**
     * 处理 RabbitMQ 邮件队列中的消息并发送邮件通知。
     *
     * @param data 包含邮件相关信息的 Map，包括邮件地址、验证码和邮件类型。
     */
    @RabbitHandler
    public void sendMailMessage(Map<String, Object> data) {
        String email = (String) data.get("email");
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            return;
        }
        Integer code = (Integer) data.get("code");
        String type = (String) data.get("type");
        SimpleMailMessage message = switch (type) {
            case "register" -> createMessage("欢迎注册",
                    "您的注册验证码为：" + code + "，5分钟内有效，为了账户安全，请勿向他人提供验证码。", email);
            case "reset" -> createMessage("重置密码",
                    "您当前正在进行密码重置操作，验证码为：" + code + "，5分钟内有效，如非本人操作，请忽略本邮件。", email);
            case "change" -> createMessage("更改邮箱",
                    "您当前正在进行邮箱改绑操作，验证码为：" + code + "，5分钟内有效，如非本人操作，请忽略本邮件。", email);
            default -> null;
        };
        if (message == null) {
            return;
        }
        sender.send(message);
    }

    /**
     * 创建邮件消息对象。
     *
     * @param title   邮件标题
     * @param content 邮件内容
     * @param email   收件人邮箱地址
     * @return 创建的 SimpleMailMessage 对象
     */
    private SimpleMailMessage createMessage(String title, String content, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(title);
        message.setText(content);
        message.setTo(email);
        message.setFrom(username);
        return message;
    }
}
