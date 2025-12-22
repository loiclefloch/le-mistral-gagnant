import { User } from '../domain/User';

export class UserService {
  private users: User[] = [];

  // Violates: No validation, no error handling, unclear naming
  createUser(data: any): User {
    const user: User = {
      id: Math.random().toString(),
      name: data.name,
      email: data.email
    };
    this.users.push(user);
    return user;
  }

  // Violates: No error handling, unclear response
  getUser(id: string): User | undefined {
    return this.users.find(u => u.id === id);
  }

  // Violates: No pagination, no filtering
  listUsers(): User[] {
    return this.users;
  }
}
