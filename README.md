# E-Commerce Platform

A full-featured e-commerce platform built with Spring Boot and PostgreSQL, supporting both Retailers and Customers with comprehensive order management, inventory tracking, and automated delivery processing.

## Features

### User Management
- **Two User Types**: Customer and Retailer with role-based access control
- **Email Authentication**: Secure login/registration with email and password
- **OTP Verification**: OTP-based authentication for enhanced security
- **Authorization**: Role-based access to platform features

### Product Management (Retailer)
- **Product Creation**: Add products with name, description, cost, MRP, and category,and Image Url using S3 Presigned Uploads
- **Input Validation**: Comprehensive validation for product data
- **Inventory Management**: Track stock levels and out-of-stock status
- **Product Listing**: View all products with sorting and filtering options
- **Stock Filters**: Filter products by stock availability

### Sales & Reporting (Retailer)
- **Sales Reports**: Generate CSV/PDF reports for specified date ranges
- **Asynchronous Processing**: Background report generation for large datasets
- **AWS S3 Integration**: Secure report storage and download links
- **Access Control**: Retailers can only access their own data

### Shopping Experience (Customer)
- **Product Browsing**: View all products from all retailers
- **Advanced Search**: Filter by search terms, category, retailer
- **Sorting Options**: Sort products by price and other criteria
- **Product Details**: Get individual product information by ID

### Cart & Order Management
- **Shopping Cart**: Add products with quantity limits (max 10 per item, 20 total)
- **Order Placement**: Convert cart to order with inventory deduction
- **Automated Delivery**: 2-minute delivery simulation with job scheduling
- **Email Invoicing**: Automatic invoice generation and email delivery
- **Order Tracking**: Complete order history for customers

### Order Lifecycle
- **Cancellation**: Cancel orders before delivery (restores inventory)
- **Returns**: Return delivered orders (restores inventory)
- **Inventory Management**: Automatic stock updates on all order actions
- **Job Management**: Cancel delivery jobs for cancelled orders

## Technology Stack

- **Backend**: Spring Boot
- **Database**: PostgreSQL
- **Cloud Storage**: AWS S3
- **Email**: SMTP integration
- **Build Tool**: Maven
- **Authentication**: JWT with OTP verification

## Prerequisites

- Java 17
- Maven 3.6+
- PostgreSQL 15+
- AWS Account with S3 access
- SMTP email service

## Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/mohammedarbaz119/ECom
cd ECom
```

### 2. Install Dependencies
```bash
mvn clean install
```

### 3. Database Setup
Create a PostgreSQL database and note the connection details.

### 4. AWS Configuration
Configure AWS credentials using AWS CLI:
```bash
aws configure
```
Enter your:
- AWS Access Key ID
- AWS Secret Access Key
- Default region
- Default output format (json)

Alternatively, create `~/.aws/credentials` file:
```
[default]
aws_access_key_id = YOUR_ACCESS_KEY
aws_secret_access_key = YOUR_SECRET_KEY
```

### 5. Environment Configuration
Create a `.env` file in the root directory and fill in your configuration values:

```env
POSTGRES_URL=jdbc:postgresql://localhost:5432/your_database_name
POSTGRES_USER=your_db_username
POSTGRES_PASS=your_db_password
SECRET_KEY=your_jwt_secret_key_min_256_bits
EXPIRATION_TIME=86400000
EMAIL_USERNAME=your_smtp_username
EMAIL_PASSWORD=your_smtp_password
AWS_BUCKET=your_s3_bucket_name
AWS_REGION=your_aws_region
```

### 6. AWS S3 Bucket Setup
1. Create an S3 bucket in your AWS console
2. Configure bucket permissions for read/write access
3. Update the `AWS_BUCKET` variable in your `.env` file

### 7. Email Service Setup
Configure SMTP settings in your `.env` file. Common providers:

**Gmail:**
- Use Gmail SMTP (smtp.gmail.com:587)
- Enable 2-factor authentication and use app password

**Other providers:**
- Update SMTP configuration in application properties if needed

## Running the Application

The application will run on port **8081** by default.

### Option 1: IntelliJ IDEA
1. Open the project in IntelliJ IDEA
2. Ensure the `.env` file is in the root directory with all required values
3. Run the main application class directly
4. The application will automatically load the `.env` file

### Option 2: VS Code
1. Open the project in VS Code
2. Install the "Spring Boot Extension Pack"
3. Ensure the `.env` file is in the root directory with all required values
4. Use the Spring Boot Dashboard to run the application
5. Or use the integrated terminal:
```bash
mvn spring-boot:run
```

### Option 3: Command Line
```bash
mvn spring-boot:run
```

## API Documentation

Once the application is running, access the API documentation at:
- API Docs: `http://localhost:8081/v3/api-docs`

## Environment Variables Details

| Variable | Description | Example |
|----------|-------------|---------|
| `POSTGRES_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/ecom` |
| `POSTGRES_USER` | Database username | `postgres` |
| `POSTGRES_PASS` | Database password | `password123` |
| `SECRET_KEY` | JWT signing key (256+ bits) | `your-very-long-secret-key-here` |
| `EXPIRATION_TIME` | JWT token expiration (ms) | `86400000` (24 hours) |
| `EMAIL_USERNAME` | SMTP username | `your-email@gmail.com` |
| `EMAIL_PASSWORD` | SMTP password/app password | `your-app-password` |
| `AWS_BUCKET` | S3 bucket name | `ecom-reports-bucket` |
| `AWS_REGION` | AWS region | `us-east-1` |

## Key Features Implementation

### Authentication Flow
1. User registers with email/password
2. OTP sent to email for verification
3. User verifies OTP to complete registration
4. Login requires email/password + OTP verification
5. JWT token issued for authenticated sessions

### Order Processing
1. Customer adds items to cart (with quantity limits)
2. Order placement triggers inventory deduction
3. Background delivery job scheduled (2-minute delay)
4. Invoice generated and emailed upon delivery
5. Order status updated throughout the process

### Report Generation
1. Retailer requests sales report with date range
2. Background job processes and generates report
3. Report uploaded to S3 with secure access
4. Retailer notified when report is ready
5. Time-limited download link provided

## Troubleshooting

### Database Connection Issues
- Verify PostgreSQL is running
- Check connection URL, username, and password
- Ensure database exists

### AWS Issues
- Verify AWS credentials are configured correctly
- Check S3 bucket permissions
- Ensure bucket exists in the specified region

### Email Issues
- Verify SMTP credentials
- Check firewall/network restrictions
- For Gmail, use app passwords instead of regular password

### Build Issues
- Run `mvn clean install` to refresh dependencies
- Check Java version compatibility
- Verify Maven configuration

## Project Structure
```
ecom/
├── src/main/java/com/ecom/
│   ├── controller/     # REST Controllers
│   ├── service/        # Business Logic
│   ├── repository/     # Data Access Layer
│   ├── model/          # Entity Classes
│   ├── dto/            # Data Transfer Objects
│   ├── config/         # Configuration Classes
│   └── util/           # Utility Classes
├── src/main/resources/
│   ├── application.yml # Application Configuration
│   └── static/         # Static Resources
├── .env                # Environment Variables
├── pom.xml            # Maven Configuration
└── README.md          # This File
```

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review application logs
3. Verify environment configuration
4. Ensure all prerequisites are installed

## License

This project is licensed under the MIT License.