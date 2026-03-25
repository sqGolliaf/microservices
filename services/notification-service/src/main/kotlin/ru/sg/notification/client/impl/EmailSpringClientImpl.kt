package ru.sg.notification.client.impl

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import ru.sg.notification.client.EmailClient

@Component
class EmailSpringClientImpl(
    private val javaMailSender: JavaMailSender,
    @Value($$"${spring.mail.from}") private val fromEmail: String
) : EmailClient {

    override fun sendVerificationEmail(
        toEmail: String,
        firstName: String,
        verificationToken: String,
        verificationUrl: String
    ): String {
        val message = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(fromEmail)
        helper.setTo(toEmail)
        helper.setSubject("Verify Your Email")

        val htmlTemplate = buildHtmlTemplate(firstName, "$verificationUrl?token=$verificationToken")
        helper.setText(htmlTemplate, true)

        javaMailSender.send(message)
        return "Email sent successfully"
    }

    private fun buildHtmlTemplate(firstName: String, verificationUrl: String): String {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background-color: #f5f5f5;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background-color: #ffffff;
                        border-radius: 8px;
                        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 30px 20px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: 600;
                    }
                    .content {
                        padding: 30px 20px;
                        color: #333;
                        line-height: 1.6;
                    }
                    .content p {
                        margin: 15px 0;
                    }
                    .button-container {
                        text-align: center;
                        margin: 30px 0;
                    }
                    .button {
                        display: inline-block;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 14px 32px;
                        text-decoration: none;
                        border-radius: 6px;
                        font-weight: 600;
                        font-size: 16px;
                        transition: transform 0.2s;
                    }
                    .button:hover {
                        transform: translateY(-2px);
                    }
                    .token-box {
                        background-color: #f9f9f9;
                        border-left: 4px solid #667eea;
                        padding: 12px;
                        margin: 20px 0;
                        word-break: break-all;
                        font-family: 'Courier New', monospace;
                        font-size: 12px;
                        color: #666;
                    }
                    .footer {
                        background-color: #f5f5f5;
                        padding: 20px;
                        text-align: center;
                        font-size: 12px;
                        color: #999;
                        border-top: 1px solid #e0e0e0;
                    }
                    .warning {
                        color: #ff6b6b;
                        font-size: 14px;
                        font-weight: 500;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to Our Platform!</h1>
                    </div>
                    <div class="content">
                        <p>Hi <strong>$firstName</strong>,</p>
                        <p>Thank you for registering! Please verify your email address by clicking the button below:</p>
                        
                        <div class="button-container">
                            <a href="$verificationUrl" class="button">Verify Email</a>
                        </div>
                        
                        <p>Or copy and paste this link in your browser:</p>
                        <div class="token-box">$verificationUrl</div>
                        
                        <p><span class="warning">⏱️ This link will expire in 24 hours</span></p>
                        
                        <p>If you didn't create an account, you can safely ignore this email.</p>
                        
                        <p>Best regards,<br><strong>The Support Team</strong></p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2026 Our Company. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}