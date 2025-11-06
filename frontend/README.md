# Hoaphat Chatbot Frontend

Frontend application cho hệ thống Hoaphat Chatbot được xây dựng bằng Vue.js 3, Vite, Tailwind CSS và Pinia.

## 🚀 Tính năng

### Người dùng
- ✅ Chatbot AI thông minh (Guest & Authenticated)
- ✅ Đăng ký / Đăng nhập
- ✅ Quên mật khẩu & Reset
- ✅ Nâng cấp Premium (VNPay)
- ✅ Upload & phân tích PDF
- ✅ Lịch sử giao dịch
- ✅ Quản lý hồ sơ

### Admin
- ✅ Dashboard với thống kê
- ✅ Quản lý người dùng
- ✅ Quản lý kiến thức
- ✅ Quản lý doanh thu & hoàn tiền
- ✅ Email logs & analytics

### Real-time
- ✅ WebSocket/STOMP integration
- ✅ Live chat với typing indicators
- ✅ Real-time notifications

## 📦 Cài đặt

```bash
# Clone repository
git clone <repository-url>
cd frontend

# Cài đặt dependencies
npm install

# Chạy development server
npm run dev

# Build cho production
npm run build

# Preview production build
npm run preview
```

## 🔧 Configuration

### Environment Variables

Tạo file `.env.development` và `.env.production`:

```env
# API Configuration
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_URL=http://localhost:8080/ws

# App Configuration
VITE_APP_NAME=Hoaphat Chatbot
VITE_APP_VERSION=1.0.0
```

## 📁 Cấu trúc thư mục

```
frontend/
├── public/                    # Static assets
│   ├── hoaphat-logo.png
│   ├── vnpay-logo.png
│   └── favicon.ico
├── src/
│   ├── assets/               # Images, styles
│   ├── components/           # Vue components
│   │   ├── auth/            # Login, Register
│   │   ├── chat/            # Chatbot UI
│   │   ├── payment/         # Premium plans
│   │   ├── document/        # PDF management
│   │   ├── admin/           # Admin panels
│   │   └── common/          # Shared components
│   ├── composables/         # Vue composables
│   ├── router/              # Vue Router config
│   ├── services/            # API services
│   ├── stores/              # Pinia stores
│   ├── utils/               # Utilities
│   ├── views/               # Page components
│   ├── App.vue              # Root component
│   └── main.js              # Entry point
├── index.html
├── package.json
├── vite.config.js
├── tailwind.config.js
└── postcss.config.js
```

## 🎨 Tech Stack

- **Vue 3** - Progressive JavaScript framework
- **Vite** - Next generation frontend tooling
- **Vue Router** - Official router
- **Pinia** - State management
- **Axios** - HTTP client
- **Tailwind CSS** - Utility-first CSS framework
- **STOMP.js** - WebSocket messaging protocol
- **SockJS** - WebSocket fallback

## 🌐 API Endpoints

```javascript
// Auth
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/forgot-password
POST   /api/auth/reset-password
GET    /api/auth/me

// Chat
POST   /api/chat/guest
POST   /api/chat
GET    /api/chat/sessions/{userId}

// Payment
POST   /api/payment/create
GET    /api/payment/callback
GET    /api/payment/my-transactions

// Documents
POST   /api/documents/upload
GET    /api/documents
POST   /api/documents/{id}/ask
DELETE /api/documents/{id}

// Admin
GET    /api/admin/dashboard/stats
GET    /api/admin/users
GET    /api/admin/knowledge
GET    /api/admin/revenue/dashboard
GET    /api/admin/emails/logs
```

## 🔐 Authentication

Ứng dụng sử dụng JWT tokens cho authentication:

```javascript
// Token được lưu trong localStorage
localStorage.setItem('token', token)

// Axios interceptor tự động thêm token vào headers
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
```

## 🎯 Features Highlight

### Guest Mode
- 5 câu hỏi miễn phí
- Không cần đăng ký
- Rate limiting

### Premium Features
- Unlimited questions
- PDF analysis
- Priority support
- Advanced AI responses

### Admin Dashboard
- Real-time statistics
- User management
- Revenue tracking
- Email monitoring

## 🚀 Deployment

### Build cho production

```bash
npm run build
```

Files được build sẽ nằm trong thư mục `dist/`

### Deploy lên Netlify/Vercel

```bash
# Netlify
netlify deploy --prod --dir=dist

# Vercel
vercel --prod
```

### Nginx Configuration

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://backend:8080;
    }

    location /ws {
        proxy_pass http://backend:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

## 📝 Development Guidelines

### Component Naming
- PascalCase cho component files: `UserProfile.vue`
- kebab-case trong templates: `<user-profile />`

### State Management
- Sử dụng Pinia stores cho global state
- Composables cho reusable logic
- Props/Emit cho component communication

### Styling
- Tailwind utility classes ưu tiên
- Custom CSS trong `global.css` nếu cần
- Responsive design (mobile-first)

## 🐛 Troubleshooting

### CORS Issues
Đảm bảo backend đã config CORS cho frontend domain:

```java
@CrossOrigin(origins = "http://localhost:3000")
```

### WebSocket Connection Failed
Kiểm tra WebSocket URL trong `.env`:

```env
VITE_WS_URL=http://localhost:8080/ws
```

### Build Errors
Xóa `node_modules` và cài lại:

```bash
rm -rf node_modules
npm install
```

## 📞 Support

Nếu có vấn đề, liên hệ:
- Email: support@hoaphat.com
- Issues: GitHub Issues

## 📄 License

Copyright © 2024 Hoaphat Corporation