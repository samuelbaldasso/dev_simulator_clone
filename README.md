# DevSimulator clone

Landing page inspired by devsimulator.dev, implemented with a Next.js frontend and a Spring Boot API.

## Run the frontend

```bash
npm install
npm run dev
```

Open `http://localhost:3000`.

## Run the API

With Maven installed:

```bash
cd backend
mvn spring-boot:run
```

The API exposes `GET http://localhost:8080/api/challenges`.

Challenge access is open: the API has no authentication or paid-plan gate.

The frontend reads this endpoint at `http://localhost:8080` by default. Set
`NEXT_PUBLIC_API_URL` before starting Next.js to use another API address.
