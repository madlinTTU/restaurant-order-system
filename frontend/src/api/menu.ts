import api from './axios'
import type {MenuItem} from "../types/menu.ts";

export const getMenuItems = async (): Promise<MenuItem[]> => {
	const res = await api.get('/menu/items')
	return res.data
}