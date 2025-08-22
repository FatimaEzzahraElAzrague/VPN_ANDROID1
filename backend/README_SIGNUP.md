# 🔐 VPN Backend Sign Up - Quick Start

## 🎯 **What This Does**

Your backend already has a **complete sign up system** that includes:
- User registration with email verification
- Secure password hashing
- OTP (One-Time Password) system
- Google Sign In support
- JWT authentication

## 🚀 **Get Started in 3 Steps**

### **1. Start the Server**
```bash
# Windows (double-click this file)
start_backend.bat

# Or PowerShell
.\start_backend.ps1

# Or manually
.\gradlew.bat run
```

### **2. Test the Sign Up**
```bash
# Test everything automatically
kotlin test_signup.kt

# Or test manually with cURL
curl -X POST "http://localhost:8080/signup" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","username":"testuser"}'
```

### **3. Configure Email (Optional)**
Edit `env.txt` and update:
```bash
SMTP_USER=your-email@gmail.com
SMTP_PASS=your-app-password
```

## 📱 **API Endpoints**

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/signup` | POST | Create new user account |
| `/verify-otp` | POST | Verify email with OTP |
| `/login` | POST | Sign in user |
| `/google-auth` | POST | Google Sign In |
| `/debug/users` | GET | View all users |

## ✅ **Success Indicators**

- ✅ Server starts without errors
- ✅ Sign up returns "OTP sent" message
- ✅ Database connection works
- ✅ All debug endpoints respond

## 🆘 **Need Help?**

1. **Check the full guide**: `SIGNUP_SETUP.md`
2. **Test with the script**: `test_signup.kt`
3. **Check server logs** for error messages
4. **Verify database connection** at `/debug/db-test`

## 🔧 **Files You Need**

- `env.txt` - Configuration settings
- `start_backend.bat` - Windows starter
- `start_backend.ps1` - PowerShell starter
- `test_signup.kt` - Test script
- `SIGNUP_SETUP.md` - Full setup guide

Your sign up system is ready to use! 🎉
