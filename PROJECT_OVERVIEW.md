# Shop Management System - Overview

A modern, high-performance desktop application designed for retail business management. Built with **JavaFX** and **SQLite**, it provides a premium user interface with a sleek light theme and robust backend capabilities.

---

## 🚀 Key Features

### 1. **Role-Based Authentication (The Abstraction Layer)**
The system maintains a clean abstraction between different levels of personnel:
- **Manager Portal**: Dedicated access for **ADMIN** and **MANAGER** roles. Provides full control over settings, finances, and user management.
- **Employee Portal**: Simplified access for **CASHIER** roles. Focused on daily operations like billing and inventory checks.
- **Security**: Password visibility toggles (👁/🙈) and role validation to prevent unauthorized access.

### 2. **Dynamic Dashboard**
- Real-time statistics on sales, customers, and stock levels.
- Visual alerts for low-stock items.

### 3. **Smart Inventory Management**
- **Product Tracking**: Manage products with barcodes, HSN codes, and pricing.
- **Stock Alerts**: Automatic highlighting of products below minimum stock levels.
- **Search & Filtering**: Lightning-fast search across the entire inventory.

### 4. **Professional Billing & POS**
- **Real-time Cart**: Add items by name or scan barcodes.
- **Calculations**: Automatic tax (GST), discounts, and total amount calculation.
- **Invoicing**: Professional PDF/Print-ready layout for customer transactions.

### 5. **Procurement & Suppliers**
- **Supplier Database**: Maintain contact details for all vendors.
- **Purchase Orders**: Create and track orders (Pending, Received, Cancelled) to restock inventory systematically.

### 6. **CRM (Customer Relationship Management)**
- Track customer history, contact details, and loyalty points.
- Quick search for existing customers during checkout.

### 7. **Finance & Expense Manager**
- Track daily business expenses (Rent, Utilities, Salary, etc.).
- Categorized expense reporting for better financial visibility.

---

## 🛠 Technology Stack

- **Language**: Java 17+
- **UI Framework**: JavaFX (with modern CSS styling)
- **Database**: SQLite (Local, high-speed, no setup required)
- **Build System**: Maven
- **Architecture**: DAO (Data Access Object) pattern for clean data separation.

---

## 🎨 UI & Aesthetics

- **Theme**: Premium White & Sky Blue light theme.
- **Buttons**: Clear visual hierarchy with "Danger" buttons highlighted in bold red (#b91c1c) for critical actions like deletion.
- **Responsiveness**: Cards and grid layouts designed to work beautifully on high-resolution screens.

---

## 🔑 Default Credentials

| Role | Username | Password |
| :--- | :--- | :--- |
| **Admin** | `.....` | `.....` |
| **Manager** | `.....` | `.....` |
| **Employee** | `.....` | `.....` |

---

## 🏗 How it Works
1. **Initialization**: On startup, `Main.java` starts the JavaFX application and `DatabaseManager` ensures the SQLite database (`shop.db`) and schema are ready.
2. **Role Selection**: The user chooses their portal (Manager vs Employee).
3. **Authentication**: `UserDAO` validates the user against the database and checks their role permissions.
4. **Main Layout**: The `MainLayout` provides a sidebar-driven interface for seamless switching between modules.
