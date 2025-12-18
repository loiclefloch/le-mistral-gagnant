import express from 'express';
import { UserService } from '../application/UserService';

export const app = express();
app.use(express.json());
const userService = new UserService();

// Violates: No versioning, poor naming, no error handling, no validation
app.post('/addUser', (req, res) => {
  const user = userService.createUser(req.body);
  res.send(user);
});

app.get('/getUser/:id', (req, res) => {
  const user = userService.getUser(req.params.id);
  res.send(user);
});

app.get('/allUsers', (req, res) => {
  const users = userService.listUsers();
  res.send(users);
});

// Another endpoint violating guidelines
app.delete('/removeUser/:id', (req, res) => {
  // No authentication, no error handling
  const idx = userService['users'].findIndex((u: any) => u.id === req.params.id);
  if (idx !== -1) userService['users'].splice(idx, 1);
  res.send({ removed: idx !== -1 });
});

const PORT = process.env.PORT ? Number(process.env.PORT) : 3000;

// Ne lance le serveur que si le fichier est exécuté directement et pas en environnement de test
if (typeof require !== 'undefined' && require.main === module && process.env.NODE_ENV !== 'test') {
  app.listen(PORT, () => {
    console.log(`Bad API demo listening on port ${PORT}`);
  });
}

export default app;
