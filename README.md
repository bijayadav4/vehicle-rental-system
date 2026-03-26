# Vehicle Rental System 🚗

A monolithic console-based Vehicle Rental System built in Java.
It supports Admin and Borrower roles with full rental lifecycle management.

## Features

### Admin
- Add, modify, delete, and search vehicles (Cars & Bikes)
- Sort vehicles by name or available count
- View reports: service due, price sorted, rented vs never-rented
- Change security deposit amounts

### Borrower
- Browse available vehicle catalogue
- Add/remove vehicles from cart (max 1 car + 1 bike)
- Checkout with security deposit validation
- Return vehicles with fine calculation (damage + extra km)
- Extend tenure (max 2 times), exchange vehicle, or report as lost
- View full rental history

## Modules

| Module | Description |
|--------|-------------|
| Module A | Login & Signup (role-based authentication) |
| Module B | Vehicle Inventory Management |
| Module C | Renting — Cart & Checkout |
| Module D | Fines, Extensions, Exchange & Loss |
| Module E | Reports for Admin & Borrower |

## Project Structure
```
VehicleRentalSystem/
├── src/
│   ├── Main.java
│   ├── models/
│   │   ├── Vehicle.java
│   │   ├── Car.java
│   │   ├── Bike.java
│   │   ├── User.java
│   │   ├── Borrower.java
│   │   └── Booking.java
│   ├── services/
│   │   ├── AuthService.java
│   │   ├── VehicleService.java
│   │   ├── RentalService.java
│   │   ├── FineService.java
│   │   └── ReportService.java
│   └── utils/
│       └── InputHelper.java
```

## How to Run

### Prerequisites
- Java 17 or above installed
- Any terminal (Command Prompt, Git Bash, or Terminal)

### Steps
```bash
# Step 1 — Navigate to the src folder
cd VehicleRentalSystem/src

# Step 2 — Compile all files
javac -d . models/*.java utils/*.java services/*.java Main.java

# Step 3 — Run the application
java Main
```

### Default Login Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@rental.com | admin123 |
| Borrower | user@rental.com | user123 |

## Business Rules

- Borrower starts with Rs. 30,000 security deposit
- Minimum deposit: Rs. 10,000 for Car, Rs. 3,000 for Bike
- Vehicle returned same day (extensions allowed up to 2 times)
- Extra 15% charge if driven more than 500 km/day
- Car damage fines: LOW = 20%, MEDIUM = 50%, HIGH = 75% of base rent
- Cars serviced every 3,000 km | Bikes every 1,500 km
- Unserviced vehicles hidden from catalogue

## Tech Stack

- Language: Java 17
- Architecture: Monolithic Console Application
- Data Storage: In-memory (ArrayList)
- No external libraries required

## Author

Bijaya Yadav
[GitHub Profile](https://github.com/bijayadav4)
# Compiled class files
*.class

# Java build folders
/bin/
/out/
/build/
/target/

# IDE files
.idea/
*.iml
.vscode/
*.classpath
*.project
.settings/

# OS files
.DS_Store
Thumbs.db
```
MIT License

Copyright (c) 2025 Bijaya Yadav

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
