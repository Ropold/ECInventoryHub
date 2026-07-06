# ECInventoryHub – An Inventory Management System for IT Equipment

<p>⚠️ **Note**: This project is hosted on a free-tier Render server.</p>
<p>⏳ It might take ~45 seconds to start after clicking the link.</p>

<p>
  <a href="https://ecinventoryhub.onrender.com">
    🔗 https://ecinventoryhub.onrender.com
  </a>
</p>

  ---

  ## 📋 About

  ECInventoryHub keeps track of company IT equipment (laptops, phones, tablets, monitors, accessories) across
  locations and employees. It replaces spreadsheet-based tracking with a single source of truth for who has
  which device, where it came from, and its current condition.

  ### Features
  - **Devices** – manage inventory with type, manufacturer, serial/inventory number, purchase date, status
    (`AVAILABLE`, `ASSIGNED`, `IN_REPAIR`, `RETIRED`) and defect flag
  - **Employees & Locations** – manage personnel and site records, each with contact details and an optional image
  - **Assignments** – hand out and return devices with a full audit trail: assigned/returned date, condition
    on handout and return, handed-out-by, and whether a copy was given to the employee / filed in the personnel file
  - **File attachments** – upload documents/images per device and per assignment (e.g. handover protocols),
    stored via Cloudinary
  - **Authentication & roles** – GitHub OAuth2 login; read access is public, write access requires
    `USER`/`ADMIN` role

  ---

  ## 📊 Code Quality

  ### ⚙️ Backend
[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=ropold_ECInventoryHub-backend)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-backend)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-backend)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-backend&metric=bugs)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-backend)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-backend&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-backend)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-backend&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-backend)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-backend&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-backend)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-backend&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-backend)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-backend&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-backend)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-backend&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-backend)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-backend&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-backend)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-backend&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-backend)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-backend&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-backend)

  ### 🎨 Frontend

[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=ropold_ECInventoryHub-frontend)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-frontend)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-frontend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-frontend)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-frontend&metric=bugs)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-frontend)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-frontend&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-frontend)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-frontend&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-frontend)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-frontend&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-frontend)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-frontend&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-frontend)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-frontend&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-frontend)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-frontend&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-frontend)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-frontend&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-frontend)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-frontend&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-frontend)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=ropold_ECInventoryHub-frontend&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=ropold_ECInventoryHub-frontend)

  ---

  ## 🛠️ Tech Stack

  ### Backend
  - **Java 21** - Modern LTS version with latest features
  - **Spring Boot 3.x** - Enterprise application framework
  - **Spring Security** - OAuth2 authentication and authorization
  - **PostgreSQL** - Open-source relational database
  - **Maven** - Dependency management and build tool
  - **JUnit 5** - Unit testing framework
  - **Cloudinary** - Cloud-based image storage and management
  - **Mapbox API** - Interactive maps and geocoding services

  ### Frontend
  - **React 18** - Modern UI library with hooks
  - **TypeScript** - Type-safe JavaScript
  - **React Router 6** - Client-side routing
  - **Axios** - HTTP client for API calls
  - **Vite** - Fast build tool and dev server
  - **CSS3** - Custom styling with responsive design

  ### DevOps & Tools
  - **Docker** - Containerization for deployment
  - **GitHub Actions** - CI/CD pipeline automation
  - **Render** - Cloud platform for hosting
  - **SonarCloud** - Code quality and security analysis
