import { useRef, useEffect, useState, useMemo } from "react";
import axios from "axios";
import mapboxgl from "mapbox-gl";
import "mapbox-gl/dist/mapbox-gl.css";
import "../styles/MapBox.css";
import type { LocationModel } from "../models/LocationModel.ts";
import type { DeviceModel } from "../models/DeviceModel.ts";
import { getDeviceStatsByLocation, type LocationDeviceStats } from "../utils/LocationDeviceStats.ts";
import { translatedInfo } from "../utils/TranslatedInfo.ts";

type MapBoxCardProps = {
    locations: LocationModel[];
    devices: DeviceModel[];
    language: string;
};

const DEFAULT_CENTER: [number, number] = [6.6667, 51.2667]; // Meerbusch
const MARKER_COLOR = "#2563eb"; // Blau: alles in Ordnung
const MARKER_WARNING_COLOR = "#dc2626"; // Rot: Geräte defekt oder in Reparatur

// Geocodiert eine Adresse, gibt [longitude, latitude] oder null zurück
function geocodeAddress(address: string, token: string): Promise<[number, number] | null> {
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
    const geocodeCacheRef = useRef<Map<string, [number, number] | null>>(new Map()); // Adresse -> Koordinaten, damit nicht jedes Mal neu geocodiert wird
    const [geocodeError, setGeocodeError] = useState<string | null>(null);
    const [mapboxConfig, setMapboxConfig] = useState<string | null>(null);
    const [searchQuery, setSearchQuery] = useState<string>("");

    const deviceStats = useMemo(() => getDeviceStatsByLocation(props.devices), [props.devices]);

    // Mapbox-Token einmalig vom Backend holen
    useEffect(() => {
        axios
            .get("/api/mbox/72c81498-f6b2-4a8a-911c-cd217a65e0da")
            .then((response) => {
                mapboxgl.accessToken = response.data;
                setMapboxConfig(response.data);
            })
            .catch((error) => {
                console.error("Error fetching MapBox configuration:", error);
                setGeocodeError("Failed to fetch MapBox configuration");
            });
    }, []);

    // Karte einmalig initialisieren, sobald das Token da ist
    useEffect(() => {
        if (!mapboxConfig || !mapContainerRef.current) return;

        mapRef.current = new mapboxgl.Map({
            container: mapContainerRef.current,
            style: "mapbox://styles/mapbox/streets-v11",
            center: DEFAULT_CENTER,
            zoom: 12,
        });

        return () => {
            mapRef.current?.remove();
            mapRef.current = null;
        };
    }, [mapboxConfig]);

    // Marker setzen, wenn sich Standorte oder Geräte ändern (Karte bleibt bestehen)
    useEffect(() => {
        if (!mapboxConfig) return;
        let cancelled = false;

        const locationsWithAddress = props.locations.filter((location) => location.address);

        Promise.all(
            locationsWithAddress.map((location) => {
                const address = location.address as string;
                const cache = geocodeCacheRef.current;
                if (cache.has(address)) return Promise.resolve(cache.get(address) ?? null);
                return geocodeAddress(address, mapboxConfig).then((coordinates) => {
                    cache.set(address, coordinates);
                    return coordinates;
                });
            })
        ).then((allCoordinates) => {
            const map = mapRef.current;
            if (cancelled || !map) return;

            const bounds = new mapboxgl.LngLatBounds();
            const notFound: string[] = [];

            locationsWithAddress.forEach((location, index) => {
                const coordinates = allCoordinates[index];
                if (!coordinates) {
                    notFound.push(location.name);
                    return;
                }

                const stats = deviceStats[location.id];
                const hasWarning = !!stats && (stats.inRepair > 0 || stats.defective > 0);

                const popup = new mapboxgl.Popup({ offset: 25 })
                    .setDOMContent(createPopupContent(location, stats, props.language));

                const marker = new mapboxgl.Marker({ color: hasWarning ? MARKER_WARNING_COLOR : MARKER_COLOR })
                    .setLngLat(coordinates)
                    .setPopup(popup)
                    .addTo(map);

                markersRef.current.push(marker);
                bounds.extend(coordinates);
            });

            if (!bounds.isEmpty()) {
                map.fitBounds(bounds, { padding: 80, maxZoom: 14 });
            }
            setGeocodeError(notFound.length > 0 ? `Address not found: ${notFound.join(", ")}` : null);
        });

        return () => {
            cancelled = true;
            markersRef.current.forEach((marker) => marker.remove());
            markersRef.current = [];
        };
    }, [props.locations, deviceStats, props.language, mapboxConfig]);

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
                <div ref={mapContainerRef} className="mapbox-details-container" />
            </div>
        </>
    );
}
