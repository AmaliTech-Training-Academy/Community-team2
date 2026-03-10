import axiosInstance from './axiosInstance';
import type { Analytics } from '../types';

export const analyticsApi = {
  get: () => axiosInstance.get<Analytics>('/analytics').then(r => r.data),
};
