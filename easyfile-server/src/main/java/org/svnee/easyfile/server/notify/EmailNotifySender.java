//package org.svnee.easyfile.server.notify;
//
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Set;
//import javax.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Component;
//import org.svnee.easyfile.common.bean.Notifier;
//import org.svnee.easyfile.common.constants.Constants;
//import org.svnee.easyfile.common.dictionary.UploadStatusEnum;
//import org.svnee.easyfile.common.util.StringUtils;
//import org.svnee.easyfile.server.config.BizConfig;
//
///**
// * 邮箱通知
// *
// * @author svnee
// **/
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class EmailNotifySender implements NotifySender {
//
//    private final JavaMailSender javaMailSender;
//    private final BizConfig bizConfig;
//
//    @Override
//    public NotifyWay notifyWay() {
//        return NotifyWay.EMAIL;
//    }
//
//    @Override
//    public boolean send(Notifier notifier, NotifyMessageTemplate messageTemplate) {
//        boolean alarmResult = true;
//
//        // send monitor email
//        if (messageTemplate != null && StringUtils.isNotBlank(notifier.getEmail())) {
//
//            // alarmContent
//            StringBuilder builder = new StringBuilder();
//            builder.append("【标题】").append(messageTemplate.getTitle());
//            if (messageTemplate.getUploadStatus() == UploadStatusEnum.SUCCESS) {
//                builder.append("<br>文件链接=<br>").append(messageTemplate.getContent());
//            } else {
//                builder.append("<br>失败信息=").append(messageTemplate.getErrorMsg());
//            }
//            builder.append("<br>备注<br>").append(messageTemplate.getRemark());
//
//            // email info
//            String content = builder.toString();
//
//            Set<String> emailSet = new HashSet<>(Arrays.asList(notifier.getEmail().split(Constants.COMMA_SPLITTER)));
//            for (String email : emailSet) {
//                // make mail
//                try {
//                    MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//
//                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
//                    helper.setFrom(bizConfig.getEmailFrom(), bizConfig.getEmailAdminName());
//                    helper.setTo(email);
//                    helper.setSubject("EasyFile导出结果");
//                    helper.setText(content, true);
//                    javaMailSender.send(mimeMessage);
//                } catch (Exception e) {
//                    log.error(">>>>>>>>>>>--- easyfile, email send error, registerId:{}",
//                        messageTemplate.getRegisterId(), e);
//                    alarmResult = false;
//                }
//            }
//        }
//        return alarmResult;
//    }
//
//}
