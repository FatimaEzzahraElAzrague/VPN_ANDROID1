package com.myapp.backend.services

import com.myapp.backend.config.Env
import java.util.Properties
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage

class EmailService {
    private val props: Properties = Properties().apply {
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true")
        put("mail.smtp.host", Env.smtpHost)
        put("mail.smtp.port", Env.smtpPort.toString())
    }

    private val session: Session by lazy {
        Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(Env.smtpUser, Env.smtpPass)
            }
        })
    }

    fun sendOtpEmail(toEmail: String, otp: String) {
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(Env.emailFrom))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
            subject = "Your verification code"
            setText("Your verification code is: $otp")
        }
        Transport.send(message)
    }
}


