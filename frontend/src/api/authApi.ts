import axiosInstance from './axiosInstance';
import type { AuthResponse, User, UserRole } from '../types';
import { decodeJwt } from '../utils';


function normaliseAuthResponse(raw: AuthResponse): { token: string; user: User } {

  const token = raw.token || raw.accessToken || raw.jwt || '';

  if (!token) {
    throw new Error('Server returned a response with no token.');
  }

 
  if (raw.user) {
    return { token, user: raw.user };
  }

  
  const payload = decodeJwt(token);
  if (!payload) {
    throw new Error('Server returned a token that could not be decoded.');
  }

  
  let role: UserRole = 'USER';
  if (typeof payload.role === 'string') {
    role = (payload.role.replace(/^ROLE_/, '') as UserRole) || 'USER';
  } else if (Array.isArray(payload.roles) && payload.roles.length > 0) {
    role = (String(payload.roles[0]).replace(/^ROLE_/, '') as UserRole) || 'USER';
  } else if (Array.isArray(payload.authorities) && payload.authorities.length > 0) {
    role = (String(payload.authorities[0]).replace(/^ROLE_/, '') as UserRole) || 'USER';
  }

  const user: User = {
    id:    typeof payload.id  === 'number' ? payload.id  : Number(payload.sub) || 0,
    email: typeof payload.email === 'string' ? payload.email : payload.sub ?? '',
    name:  typeof payload.name  === 'string' ? payload.name  : (payload.email as string) ?? payload.sub ?? '',
    role,
  };

  return { token, user };
}

export const authApi = {
  login: async (email: string, password: string) => {
    const res = await axiosInstance.post<AuthResponse>('/auth/login', { email, password });
    return normaliseAuthResponse(res.data);
  },

  register: async (fullName: string, email: string, password: string) => {
    const res = await axiosInstance.post<AuthResponse>('/auth/register', { fullName, email, password });
    return normaliseAuthResponse(res.data);
  },
};
