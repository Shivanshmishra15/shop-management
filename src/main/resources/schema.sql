-- Products Table
CREATE TABLE IF NOT EXISTS products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    barcode TEXT UNIQUE,
    hsn_code TEXT,
    price_per_unit REAL NOT NULL,
    current_stock INTEGER NOT NULL DEFAULT 0,
    min_stock_alert INTEGER DEFAULT 5,
    description TEXT
);

-- Invoices Table
CREATE TABLE IF NOT EXISTS invoices (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_name TEXT,
    customer_contact TEXT,
    gst_number TEXT,
    total_amount REAL NOT NULL,
    gst_total REAL NOT NULL,
    final_amount REAL NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Invoice Items Table
CREATE TABLE IF NOT EXISTS invoice_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    invoice_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    product_name TEXT NOT NULL,
    hsn_code TEXT,
    quantity INTEGER NOT NULL,
    rate REAL NOT NULL,
    gst_percent REAL NOT NULL,
    total REAL NOT NULL,
    FOREIGN KEY(product_id) REFERENCES products(id)
);

-- Users Table (Module 1)
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role TEXT NOT NULL,
    fullname TEXT
);

-- Seed Default Users
INSERT OR IGNORE INTO users (username, password, role, fullname) VALUES ('admin', 'admin123', 'ADMIN', 'System Administrator');
INSERT OR IGNORE INTO users (username, password, role, fullname) VALUES ('manager', 'manager123', 'MANAGER', 'Store Manager');
INSERT OR IGNORE INTO users (username, password, role, fullname) VALUES ('cashier', 'cashier123', 'CASHIER', 'Cashier Employee');

-- Suppliers Table (Module 2)
CREATE TABLE IF NOT EXISTS suppliers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    contact_person TEXT,
    phone TEXT,
    email TEXT,
    address TEXT
);

-- Purchase Orders Table (Module 2)
CREATE TABLE IF NOT EXISTS purchase_orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    supplier_id INTEGER NOT NULL,
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status TEXT DEFAULT 'PENDING', -- PENDING, RECEIVED, CANCELLED
    total_amount REAL,
    FOREIGN KEY(supplier_id) REFERENCES suppliers(id)
);

-- Purchase Order Items Table (Module 2)
CREATE TABLE IF NOT EXISTS purchase_order_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    po_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    cost_price REAL NOT NULL,
    FOREIGN KEY(po_id) REFERENCES purchase_orders(id),
    FOREIGN KEY(product_id) REFERENCES products(id)
);

-- Customers Table (Module 3)
CREATE TABLE IF NOT EXISTS customers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    phone TEXT UNIQUE NOT NULL,
    email TEXT,
    address TEXT,
    loyalty_points INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Expenses Table (Module 4)
CREATE TABLE IF NOT EXISTS expenses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    category TEXT NOT NULL, -- Rent, Utilities, Salary, Misc
    description TEXT,
    amount REAL NOT NULL,
    expense_date DATETIME DEFAULT CURRENT_TIMESTAMP
);
