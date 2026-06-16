import axios from 'axios'

export interface AuthResponse {
	accessToken: string
	refreshToken: string
}

export interface AuthRequest {
	email: string
	password: string
}

export const login = async (request: AuthRequest): Promise<AuthResponse> => {
	const res = await axios.post('/auth/login', request)
	return res.data
}

export const register = async (request: AuthRequest): Promise<AuthResponse> => {
	const res = await axios.post('/auth/register', request)
	return res.data
}
