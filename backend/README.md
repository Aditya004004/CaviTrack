# Inventory Backend

This is the backend service for the CaviTrack Inventory system, built with Node.js, Express, TypeScript, and MongoDB.

## Architecture

The backend follows a typical RESTful architectural pattern:
- **Controllers**: Handle HTTP requests and responses.
- **Services**: Contain business logic and database interactions.
- **Models**: Mongoose schemas defining database entities (Users, Components, Molds, etc.).
- **Routes**: Define the API endpoints and map them to controllers.
- **Middleware**: Used for things like JWT authentication, file uploads (`multer`), etc.

## Setup

1. Create a `.env` file in the root of the `backend/` directory.
2. Add the following environment variables:
   ```env
   PORT=5000
   MONGO_URI=mongodb://localhost:27017/inventory
   JWT_SECRET=your_jwt_secret_here
   JWT_REFRESH_SECRET=your_jwt_refresh_secret_here
   ```
3. Install dependencies:
   ```bash
   npm install
   ```

## Scripts

- **`npm run dev`**: Starts the development server using `ts-node-dev`.
- **`npm run build`**: Compiles TypeScript into JavaScript in the `dist/` directory.
- **`npm start`**: Runs the compiled output.
- **`npm run lint`**: Runs ESLint to check for code issues.
- **`npm run lint:fix`**: Automatically fixes linting errors where possible.
- **`npm test`**: Runs the test suite using Jest.

## Testing

This project uses **Jest** and **Supertest** for unit and integration testing.
- `jest.config.js` is set up to find tests that match `**/*.test.ts`.
- Mongoose models and third-party libraries (like `bcryptjs`) are typically mocked in unit tests.
- Run `npm test` to execute all test cases.

## Technologies Used

- Express (Web framework)
- TypeScript (Language)
- Mongoose (MongoDB ODM)
- Jest / Supertest (Testing)
- ESLint / Prettier (Code styling/linting)
- bcryptjs (Password hashing)
- jsonwebtoken (Authentication)
