/*
import { useRef, useEffect, useState } from "react";
import axios from "axios";
import mapboxgl from "mapbox-gl";
import "mapbox-gl/dist/mapbox-gl.css";
import "./styles/Mapbox.css";
import "./styles/Searchbar.css";
import type { RealEstateModel } from "./model/RealEstateModel.ts";
import {translatedInfo} from "./utils/TranslatedInfo.ts";

type MapBoxAllProps = {
    favorites: string[];
    allRealEstates: RealEstateModel[];
    toggleFavorite: (realEstateId: string) => void;
    language: string;
};

export default function MapBoxButton(props: Readonly<MapBoxAllProps>) {
    const mapRef = useRef<mapboxgl.Map | null>(null); // Referenz für die Karte
    const mapContainerRef = useRef<HTMLDivElement | null>(null); // Referenz für den Map Container
    const [geocodeError, setGeocodeError] = useState<string | null>(null); // Fehlerzustand für Geocoding
    const [mapboxConfig, setMapboxConfig] = useState<string | null>(null); // Zustand für das Mapbox-Token
    const [searchQuery, setSearchQuery] = useState<string>(""); // Zustand für die Suchabfrage

    // Funktion zum Abrufen des MapBoxButton-Konfigurationstokens
    function fetchMapBoxConfig() {
        axios
            .get("/api/mbox/72c81498-f6b2-4a8a-911c-cd217a65e0da")
            .then((response) => {
                const resp = response.data; // Holen des Tokens aus der Antwort
                setMapboxConfig(resp); // Token speichern
                mapboxgl.accessToken = resp; // Mapbox-Access-Token setzen
            })
            .catch((error) => {
                console.error("Error fetching MapBoxButton configuration:", error);
                setGeocodeError("Failed to fetch MapBoxButton configuration");
            });
    }

    // Funktion zum Geocodieren der Adresse
    const geocodeAddress = (address: string): Promise<[number, number] | null> => {
        if (!mapboxConfig) return Promise.resolve(null); // Warten auf das Mapbox-Token

        const geocodeUrl = `https://api.mapbox.com/geocoding/v5/mapbox.places/${encodeURIComponent(
            address
        )}.json?access_token=${mapboxConfig}`;

        return fetch(geocodeUrl)
            .then((response) => response.json())
            .then((data) => {
                if (data.features && data.features.length > 0) {
                    return data.features[0].geometry.coordinates; // Gibt [longitude, latitude] zurück
                }
                return null; // Kein Ergebnis für die Adresse
            })
            .catch((error) => {
                console.error("Error geocoding address:", error);
                setGeocodeError("Error geocoding address.");
                return null;
            });
    };

    // Funktion zur Initialisierung der Karte und zum Hinzufügen der Marker
    useEffect(() => {
        fetchMapBoxConfig(); // API-Aufruf beim Laden der Komponente
    }, []);

    // In useEffect, bevor wir die Karte manipulieren
    useEffect(() => {
        if (!mapboxConfig || !props.allRealEstates.length) return; // Wenn kein Mapbox-Token oder keine Immobilien vorhanden sind, nichts tun

        // Karte initialisieren
        if (mapContainerRef.current) {
            // Sicherstellen, dass die Karte nur einmal initialisiert wird
            if (!mapRef.current) {
                mapRef.current = new mapboxgl.Map({
                    container: mapContainerRef.current,
                    style: "mapbox://styles/mapbox/streets-v11", // Stil der Karte
                    center: [6.960279, 50.937531], // Standard-Zentrum (Köln)
                    zoom: 12, // Zoom-Level
                });
            }

            // Sicherstellen, dass mapRef.current nicht null ist, bevor es verwendet wird
            if (mapRef.current) {
                // Marker für jede Immobilie hinzufügen
                props.allRealEstates.forEach((realEstate) => {
                    geocodeAddress(realEstate.address).then((coordinates) => {
                        if (coordinates) {
                            const [longitude, latitude] = coordinates;

                            const isFavorite = props.favorites.includes(realEstate.id);

                            const popup = new mapboxgl.Popup({ offset: 25 })
                                .setHTML(`
                                <div style="text-align: center; max-width: 200px; color: #333;">
                                    <h4>
                                        <a href="/real-estate/${realEstate.id}" style="text-decoration: none; color: #007bff;">
                                            ${realEstate.realEstateTitle}
                                        </a>
                                    </h4>
                                    ${realEstate.imageUrl ? `<img src="${realEstate.imageUrl}" alt="${realEstate.realEstateTitle}" style="width: 100%; height: auto; border-radius: 8px;"/>` : ''}
                                    <p><strong>Adresse:</strong> ${realEstate.address}</p>
                                </div>
                            `);

                            // Markerfarbe basierend auf Favoriten
                            const markerColor = isFavorite ? '#FF6F61' : '#40b1ce'; // Rot für Favoriten, Blau für Nicht-Favoriten

                            // Erstelle den Marker mit der gewünschten Farbe
                            const marker = new mapboxgl.Marker({
                                color: markerColor, // Verwende die benutzerdefinierte Markerfarbe
                            })
                                .setLngLat([longitude, latitude])
                                .setPopup(popup) // Popup mit dem Marker verbinden
                                .addTo(mapRef.current as mapboxgl.Map); // Marker zur Karte hinzufügen

                            // Füge ein Klick-Event hinzu, um die Karte auf den Marker zu zentrieren
                            marker.getElement().addEventListener("click", () => {
                                if (mapRef.current) {
                                    mapRef.current.flyTo({
                                        center: [longitude, latitude], // Zentriere auf den Marker
                                        zoom: 15, // Optional: Passe den Zoom-Level an
                                        offset: [0, -200], // Verschiebe das Zentrum nach oben (Y-Achse)
                                        speed: 1.5, // Optional: Geschwindigkeit der Animation
                                        curve: 1, // Optional: Animation-Kurve
                                    });
                                }
                            });
                        } else {
                            setGeocodeError(`Address not found: ${realEstate.address}`);
                        }
                    });
                });
            }
        }

        // Bereinigung der Karte, wenn das Component unmontiert wird oder die Immobilien geändert werden
        return () => {
            if (mapRef.current) {
                mapRef.current.remove();
            }
        };
    }, [props.allRealEstates, mapboxConfig, props.favorites]);


    // Funktion zum Suchen eines Ortes und die Karte darauf zu zentrieren
    const handleSearch = () => {
        if (searchQuery.length < 3) {
            setGeocodeError("Bitte mindestens 3 Zeichen eingeben.");
            return;
        }
        setGeocodeError(null);
        geocodeAddress(searchQuery).then((coordinates) => {
            if (coordinates && mapRef.current) {
                const [longitude, latitude] = coordinates;
                // Karte auf die gefundenen Koordinaten zentrieren
                mapRef.current.setCenter([longitude, latitude]);
                mapRef.current.setZoom(12); // Optional: Zoom-Level anpassen
                // Optional: Marker für den gefundenen Ort hinzufügen
            } else {
                setGeocodeError("Address not found.");
            }
        });
    };

    return (
        <>
            <div className="search-bar">
                <input
                    type="text"
                    className="search-input"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder={translatedInfo["Search for a place..."][props.language]}
                    onKeyDown={(e) => {
                        if (e.key === "Enter") {
                            handleSearch();
                        }
                    }}
                />
                <button className="blue-button search-map-box" onClick={handleSearch}>{translatedInfo["Search"][props.language]}</button>
            </div>
            <div>
                <h3>{translatedInfo["Cologne ist set to Default-City"][props.language]}</h3>
                {geocodeError && <div>{geocodeError}</div>}
                <div ref={mapContainerRef} style={{ width: "100%", height: "600px" }} />
            </div>
        </>
    );
}*/
