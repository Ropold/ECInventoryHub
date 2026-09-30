import { useRef, useEffect, useState, useMemo } from "react";
import mapboxgl from "mapbox-gl";
import "mapbox-gl/dist/mapbox-gl.css";
import "../styles/MapBox.css";
import type { LocationModel } from "../models/LocationModel.ts";
import type { DeviceModel } from "../models/DeviceModel.ts";
import { getDeviceStatsByLocation, type LocationDeviceStats } from "../utils/LocationDeviceStats.ts";
import { translatedInfo } from "../utils/TranslatedInfo.ts";
import { DEFAULT_CENTER, fetchMapboxToken, geocodeAddress } from "../utils/MapboxGeocoding.ts";

type MapBoxCardProps = {
    locations: LocationModel[];
    devices: DeviceModel[];
    language: string;
};

// Ein Standort mit fertig geocodierten Koordinaten
type MapPoint = {
    location: LocationModel;
    coordinates: [number, number];
};

const DEFAULT_ZOOM = 12;
const MARKER_COLOR = "#2563eb"; // Blau: alles in Ordnung
const MARKER_WARNING_COLOR = "#dc2626"; // Rot: Geräte defekt oder in Reparatur

// Popup-Inhalt per DOM statt HTML-String, damit Namen/Adressen nicht als HTML interpretiert werden
function createPopupContent(location: LocationModel, stats: LocationDeviceStats | undefined, language: string): HTMLElement {
    const container = document.createElement("div");
    container.className = "mapbox-popup";

    const title = document.createElement("h4");
    title.textContent = location.name;
    container.appendChild(title);

    if (location.imageUrl) {
        const image = document.createElement("img");
        image.src = location.imageUrl;
        image.alt = location.name;
        container.appendChild(image);
    }

    const address = document.createElement("p");
    address.textContent = `${translatedInfo["Address"][language]}: ${location.address}`;
    container.appendChild(address);

    const devices = document.createElement("p");
    devices.textContent = `${translatedInfo["Devices"][language]}: ${stats?.total ?? 0}`;
    container.appendChild(devices);

    if (stats && (stats.inRepair > 0 || stats.defective > 0)) {
        const warning = document.createElement("p");
        warning.className = "mapbox-popup-warning";
        warning.textContent = `${translatedInfo["In repair"][language]}: ${stats.inRepair} · ${translatedInfo["Defective"][language]}: ${stats.defective}`;
        container.appendChild(warning);
    }

    return container;
}

export default function MapBoxCard(props: Readonly<MapBoxCardProps>) {
    const mapRef = useRef<mapboxgl.Map | null>(null);
    const mapContainerRef = useRef<HTMLDivElement | null>(null);
    const markersRef = useRef<mapboxgl.Marker[]>([]);
    const [geocodeError, setGeocodeError] = useState<string | null>(null);
    const [mapboxConfig, setMapboxConfig] = useState<string | null>(null);
    const [searchQuery, setSearchQuery] = useState<string>("");

    const deviceStats = useMemo(() => getDeviceStatsByLocation(props.devices), [props.devices]);

    // Koordinaten kommen gespeichert aus der DB, kein Geocoding pro Standort mehr nötig
    const mapPoints = useMemo<MapPoint[]>(
        () =>
            props.locations
                .filter((location) => location.latitude != null && location.longitude != null)
                .map((location) => ({
                    location: location,
                    coordinates: [location.longitude as number, location.latitude as number],
                })),
        [props.locations]
    );

    const locationsWithoutCoordinates = props.locations
        .filter((location) => location.latitude == null || location.longitude == null)
        .map((location) => location.name);

    // Mapbox-Token einmalig vom Backend holen
    useEffect(() => {
        fetchMapboxToken()
            .then((token) => {
                mapboxgl.accessToken = token;
                setMapboxConfig(token);
            })
            .catch((error) => {
                console.error("Error fetching MapBox configuration:", error);
                setGeocodeError("Failed to fetch MapBox configuration");
            });
    }, []);

    // Karte beim Verlassen der Seite aufräumen
    useEffect(() => {
        return () => {
            mapRef.current?.remove();
            mapRef.current = null;
        };
    }, []);

    // Karte einmalig erzeugen, dann schon passend zu den Standorten (kein Nachspringen)
    useEffect(() => {
        if (!mapboxConfig || !mapContainerRef.current || mapRef.current) return;

        const bounds = new mapboxgl.LngLatBounds();
        mapPoints.forEach((point) => bounds.extend(point.coordinates));

        mapRef.current = new mapboxgl.Map({
            container: mapContainerRef.current,
            style: "mapbox://styles/mapbox/streets-v11",
            ...(bounds.isEmpty()
                ? { center: DEFAULT_CENTER, zoom: DEFAULT_ZOOM }
                : { bounds: bounds, fitBoundsOptions: { padding: 80, maxZoom: DEFAULT_ZOOM } }),
        });
    }, [mapboxConfig, mapPoints]);

    // Marker setzen, wenn sich Standorte oder Geräte ändern (Kartenausschnitt bleibt, wie er ist)
    useEffect(() => {
        const map = mapRef.current;
        if (!map) return;

        mapPoints.forEach((point) => {
            const stats = deviceStats[point.location.id];
            const hasWarning = !!stats && (stats.inRepair > 0 || stats.defective > 0);

            const popup = new mapboxgl.Popup({ offset: 25 })
                .setDOMContent(createPopupContent(point.location, stats, props.language));

            const marker = new mapboxgl.Marker({ color: hasWarning ? MARKER_WARNING_COLOR : MARKER_COLOR })
                .setLngLat(point.coordinates)
                .setPopup(popup)
                .addTo(map);

            markersRef.current.push(marker);
        });

        return () => {
            markersRef.current.forEach((marker) => marker.remove());
            markersRef.current = [];
        };
    }, [mapboxConfig, mapPoints, deviceStats, props.language]); // mapboxConfig: Karte wird erst nach dem Token erzeugt

    // Ort suchen und Karte darauf zentrieren
    const handleSearch = () => {
        if (searchQuery.length < 3) {
            setGeocodeError("Bitte mindestens 3 Zeichen eingeben.");
            return;
        }
        if (!mapboxConfig) return;
        setGeocodeError(null);
        geocodeAddress(searchQuery, mapboxConfig).then((coordinates) => {
            if (coordinates && mapRef.current) {
                mapRef.current.flyTo({ center: coordinates, zoom: 12 });
            } else {
                setGeocodeError("Address not found.");
            }
        });
    };

    return (
        <>
            <div className="mapbox-all-search-field">
                <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder={translatedInfo["Search for a place..."][props.language]}
                    onKeyDown={(e) => {
                        if (e.key === "Enter") {
                            handleSearch();
                        }
                    }}
                />
                <button onClick={handleSearch}>{translatedInfo["Search"][props.language]}</button>
            </div>
            <div>
                {geocodeError && <div>{geocodeError}</div>}
                {locationsWithoutCoordinates.length > 0 && (
                    <div>No coordinates: {locationsWithoutCoordinates.join(", ")}</div>
                )}
                <div ref={mapContainerRef} className="mapbox-details-container" />
            </div>
        </>
    );
}
