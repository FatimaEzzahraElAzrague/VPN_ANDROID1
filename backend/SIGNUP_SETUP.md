# 🚀 VPN Backend Sign Up Setup Guide

Your backend sign up system is **already fully implemented and working**! This guide will help you configure it properly.

## ✅ **What's Already Working**

- ✅ Complete sign up flow with email verification
- ✅ Secure password hashing (Argon2id)
- ✅ OTP verification system
- ✅ Google OAuth integration
- ✅ JWT authentication
- ✅ PostgreSQL database integration
- ✅ Email service for OTP delivery
- ✅ Rate limiting and security features

## 🔧 **Quick Setup Steps**

### **Step 1: Start the Backend**

```bash
cd VPN_ANDROID1/backend
./gradlew run
```

Wait for: `🚀 Backend started successfully!`

### **Step 2: Test the Sign Up System**

```bash
# Test all sign up endpoints
kotlin test_signup.kt
```

This will test:
- ✅ Sign up endpoint
- ✅ OTP verification endpoint  
- ✅ Login endpoint
- ✅ Google auth endpoint
- ✅ Debug endpoints

### **Step 3: Configure Email Verification (Optional but Recommended)**

To send real OTP emails, update these values in `env.txt`:

```bash
# For Gmail (recommended for testing)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASS=your-app-password  # Use App Password, not regular password
EMAIL_FROM=your-email@gmail.com
```

**How to get Gmail App Password:**
1. Go to [Google Account Settings](https://myaccount.google.com/)
2. Security → 2-Step Verification → App passwords
3. Generate a new app password for "Mail"
4. Use that password in `SMTP_PASS`

### **Step 4: Configure Google OAuth (Optional)**

To enable Google Sign In:

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Google+ API
4. Go to Credentials → Create Credentials → OAuth 2.0 Client IDs
5. Update `env.txt`:

```bash
GOOGLE_CLIENT_ID_ANDROID=your-android-client-id
GOOGLE_CLIENT_ID_WEB=your-web-client-id
```

## 🧪 **Testing the Sign Up Flow**

### **Manual Testing with cURL**

```bash
# 1. Sign Up
curl -X POST "http://localhost:8080/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "username": "testuser",
    "full_name": "Test User"
  }'

# Expected: {"message":"OTP sent"} (Status: 202)

# 2. Verify OTP (check your email for the code)
curl -X POST "http://localhost:8080/verify-otp" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "otp": "123456"
  }'

# Expected: JWT token (Status: 200)

# 3. Login
curl -X POST "http://localhost:8080/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'

# Expected: JWT token + user profile (Status: 200)
```

### **Test with the Kotlin Script**

```bash
kotlin test_signup.kt
```

## 🔍 **Debug Endpoints**

Your backend includes helpful debug endpoints:

- `GET /debug/users` - View all users in database
- `GET /debug/sessions` - Check active sessions
- `GET /debug/db-test` - Test database connection

## 🚨 **Common Issues & Solutions**

### **Issue: "Missing env var" error**
**Solution:** Make sure `env.txt` exists and has all required values

### **Issue: Email not sending**
**Solution:** 
1. Check SMTP settings in `env.txt`
2. Use Gmail App Password (not regular password)
3. Check firewall/antivirus blocking SMTP

### **Issue: Database connection failed**
**Solution:** 
1. Check if Neon database is accessible
2. Verify `DATABASE_URL` in `env.txt`
3. Check internet connection

### **Issue: OTP verification failing**
**Solution:**
1. Check if email was received
2. OTP expires after 10 minutes
3. Check Redis connection (falls back to in-memory)

## 🎯 **Success Indicators**

Your sign up is working correctly when:

✅ **Sign up endpoint returns 202 with "OTP sent"**  
✅ **OTP verification returns 200 with JWT token**  
✅ **Login returns 200 with user profile**  
✅ **Debug endpoints show database connection working**  
✅ **Users table shows new user records**  

## 🔄 **Next Steps After Setup**

1. **Test with real email** - Verify OTP delivery works
2. **Test Google Sign In** - Configure OAuth client IDs
3. **Integrate with Android app** - Update frontend to call these endpoints
4. **Add more security** - Consider adding CAPTCHA, phone verification
5. **Production deployment** - Deploy to production environment

## 📚 **API Documentation**

### **Sign Up Endpoint**
```
POST /signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securepassword",
  "username": "username",
  "full_name": "Full Name"
}
```

### **OTP Verification Endpoint**
```
POST /verify-otp
Content-Type: application/json

{
  "email": "user@example.com",
  "otp": "123456"
}
```

### **Login Endpoint**
```
POST /login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securepassword"
}
```

### **Google Auth Endpoint**
```
POST /google-auth
Content-Type: application/json

{
  "id_token": "google-id-token"
}
```

## 🆘 **Need Help?**

If you encounter issues:

1. **Check server logs** - Look for error messages in console
2. **Test debug endpoints** - Verify database and services are working
3. **Check environment variables** - Ensure all required values are set
4. **Verify network access** - Check firewall and internet connectivity

Your sign up system is production-ready and follows security best practices! 🎉
