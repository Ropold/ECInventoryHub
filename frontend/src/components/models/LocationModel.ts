
export type LocationModel = {
    id: string;
    name: string;
    address: string | null;
    phone: string | null;
    email: string | null;
    notes: string | null;
    imageUrl: string | null;
};

export const DefaultLocation: LocationModel = {
    id: "",
    name: "",
    address: null,
    phone: null,
    email: null,
    notes: null,
    imageUrl: null,
};