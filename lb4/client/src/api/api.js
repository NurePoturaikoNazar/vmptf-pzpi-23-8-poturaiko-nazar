import axios from 'axios';

const API_BASE = '/api';

function getToken() {
  return localStorage.getItem('spekulant_token');
}

function authHeaders() {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export const api = {
  async register(data) {
    const res = await axios.post(`${API_BASE}/register`, data);
    return res.data;
  },

  async login(data) {
    const res = await axios.post(`${API_BASE}/login`, data);
    return res.data;
  },

  async getMe() {
    const res = await axios.get(`${API_BASE}/me`, { headers: authHeaders() });
    return res.data;
  },

  async getCategories() {
    const res = await axios.get(`${API_BASE}/categories`);
    return res.data;
  },

  async getProducts(params = {}) {
    const res = await axios.get(`${API_BASE}/products`, { params });
    return res.data;
  },

  async createProduct(data) {
    const res = await axios.post(`${API_BASE}/products`, data, { headers: authHeaders() });
    return res.data;
  },

  async updateProduct(id, data) {
    const res = await axios.put(`${API_BASE}/products/${id}`, data, { headers: authHeaders() });
    return res.data;
  },

  async deleteProduct(id) {
    const res = await axios.delete(`${API_BASE}/products/${id}`, { headers: authHeaders() });
    return res.data;
  },

  async getOrders() {
    const res = await axios.get(`${API_BASE}/orders`, { headers: authHeaders() });
    return res.data;
  },

  async createOrder(items) {
    const res = await axios.post(`${API_BASE}/orders`, { items }, { headers: authHeaders() });
    return res.data;
  },
};
