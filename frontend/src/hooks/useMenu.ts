import {useQuery} from "@tanstack/react-query";
import {getMenuItems} from "../api/menu.ts";

export const useMenuItems = () =>
	useQuery({
		queryKey: ['menuItems'],
		queryFn: getMenuItems,
	})