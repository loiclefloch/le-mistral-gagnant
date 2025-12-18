
const request = require('supertest');
const { app } = require('../src/infrastructure/server');

describe('Bad API Endpoints (integration with project app)', () => {
  it('should add a user', async () => {
    const res = await request(app)
      .post('/addUser')
      .send({ name: 'Test', email: 'test@example.com' });

    expect(res.status).toBe(200);
    expect(res.body.name).toBe('Test');
    expect(res.body.email).toBe('test@example.com');
  });

  it('should get a user', async () => {
    const created = await request(app)
      .post('/addUser')
      .send({ name: 'Test2', email: 'test2@example.com' });

    const id = created.body.id;

    const res = await request(app).get(`/getUser/${id}`);
    expect(res.status).toBe(200);
    expect(res.body.email).toBe('test2@example.com');
  });

  it('should list users', async () => {
    await request(app)
      .post('/addUser')
      .send({ name: 'Test3', email: 'test3@example.com' });

    const res = await request(app).get('/allUsers');
    expect(res.status).toBe(200);
    expect(Array.isArray(res.body)).toBe(true);
    expect(res.body.length).toBeGreaterThan(0);
  });

  it('should remove a user', async () => {
    const created = await request(app)
      .post('/addUser')
      .send({ name: 'Test4', email: 'test4@example.com' });

    const id = created.body.id;

    const res = await request(app).delete(`/removeUser/${id}`);
    expect(res.status).toBe(200);
    expect(res.body.removed).toBe(true);
  });
});
