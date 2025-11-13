
const request = require('supertest');
const { UserService } = require('../src/application/UserService');
const express = require('express');

function createTestApp() {
  const app = express();
  app.use(express.json());
  const userService = new UserService();
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
  app.delete('/removeUser/:id', (req, res) => {
    // Access private users array for test purposes
    const users = (userService as any).users;
    const idx = users.findIndex((u: any) => u.id === req.params.id);
    if (idx !== -1) users.splice(idx, 1);
    res.send({ removed: idx !== -1 });
  });
  return { app, userService };
}

describe('Bad API Endpoints', () => {
  let app;
  let userService;
  beforeEach(() => {
    const testSetup = createTestApp();
    app = testSetup.app;
    userService = testSetup.userService;
  });

  it('should add a user', async () => {
    const res = await request(app).post('/addUser').send({ name: 'Test', email: 'test@example.com' });
    expect(res.body.name).toBe('Test');
  });

  it('should get a user', async () => {
    const user = userService.createUser({ name: 'Test2', email: 'test2@example.com' });
    const res = await request(app).get(`/getUser/${user.id}`);
    expect(res.body.email).toBe('test2@example.com');
  });

  it('should list users', async () => {
    userService.createUser({ name: 'Test3', email: 'test3@example.com' });
    const res = await request(app).get('/allUsers');
    expect(Array.isArray(res.body)).toBe(true);
  });

  it('should remove a user', async () => {
    const user = userService.createUser({ name: 'Test4', email: 'test4@example.com' });
    const res = await request(app).delete(`/removeUser/${user.id}`);
    expect(res.body.removed).toBe(true);
  });
});
