import axios from "axios";

export const DEFAULT_CENTER: [number, number] = [6.6667, 51.2667]; // Meerbusch, [longitude, latitude]

// Mapbox-Token vom Backend holen
export function fetchMapboxToken(): Promise<string> {
    return axios
        .get("/api/mbox/72c81498-f6b2-4a8a-911c-cd217a65e0da")
        .then((response) => response.data);
}

// Geocodiert eine Adresse, gibt [longitude, latitude] oder null zurück
export function geocodeAddress(address: string, token: string): Promise<[number, number] | null> {
    const geocodeUrl = `https://api.mapbox.com/geocoding/v5/mapbox.places/${encodeURIComponent(
        address
    )}.json?country=de&proximity=${DEFAULT_CENTER.join(",")}&access_token=${token}`;

    return fetch(geocodeUrl)
        .then((response) => response.json())
        .then((data) => (data.features && data.features.length > 0 ? data.features[0].geometry.coordinates : null))
        .catch((error) => {
            console.error("Error geocoding address:", error);
            return null;
        });
}

export type Coordinates = {
    latitude: number | null;
    longitude: number | null;
};

// Koordinaten beim Speichern eines Standorts bestimmen (einmaliges Geocoding statt bei jedem Kartenaufruf).
// Geocodiert wird, wenn eine Adresse da ist und Koordinaten fehlen oder forceGeocode gesetzt ist (z.B. Adresse geändert).
// Findet Mapbox nichts, bleiben die übergebenen Werte erhalten.
export function resolveCoordinates(address: string | undefined, current: Coordinates, forceGeocode: boolean): Promise<Coordinates> {
    const hasCoordinates = current.latitude != null && current.longitude != null;
    if (!address || (hasCoordinates && !forceGeocode)) return Promise.resolve(current);

    return fetchMapboxToken()
        .then((token) => geocodeAddress(address, token))
        .then((coordinates) => (coordinates ? { longitude: coordinates[0], latitude: coordinates[1] } : current))
        .catch((error) => {
            console.error("Error geocoding location address:", error);
            return current;
        });
}
