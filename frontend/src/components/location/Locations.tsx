import type {LocationModel} from "../models/LocationModel.ts";
import type {DeviceModel} from "../models/DeviceModel.ts";
import {useState} from "react";
import {useAutoScrollToTop} from "../utils/ComponentsFunctions.tsx";
import SearchBar from "../SearchBar.tsx";
import LocationCard from "./LocationCard.tsx";
import MapBoxCard from "./MapBoxCard.tsx";
import {useNavigate} from "react-router-dom";
import {translatedInfo} from "../utils/TranslatedInfo.ts";
import NoPermissionPopup from "../NoPermissionPopup.tsx";

type LocationsProps = {
    language: string;
    role: string;
    locations: LocationModel[];
    devices: DeviceModel[];
}

function filterLocations(locations: LocationModel[], query: string): LocationModel[] {
    if (!locations) return [];

    const searchQuery = query.toLowerCase();

    return locations.filter(location => {
        return (
            location.name.toLowerCase().includes(searchQuery) ||
            location.address?.toLowerCase().includes(searchQuery) ||
            location.phone?.toLowerCase().includes(searchQuery) ||
            location.email?.toLowerCase().includes(searchQuery) ||
            location.notes?.toLowerCase().includes(searchQuery) ||
            location.id.toLowerCase().includes(searchQuery)
        );
    });
}

export default function Locations(props: Readonly<LocationsProps>) {
    useAutoScrollToTop();
    const navigate = useNavigate();

    const [searchQuery, setSearchQuery] = useState<string>("");
    const [showNoPermission, setShowNoPermission] = useState<boolean>(false);

    function handleAddNewClick() {
        if (props.role === "VIEWER") {
            setShowNoPermission(true);
            return;
        }
        navigate(`/locations/add-new-location`);
    }

    const filteredLocations = filterLocations(props.locations, searchQuery);

    return (
        <>
            <h2>Locations</h2>

            <MapBoxCard locations={props.locations} devices={props.devices} language={props.language} />

            <div className={"search-add-new-button"}>
                <SearchBar
                    searchQuery={searchQuery}
                    setSearchQuery={setSearchQuery}
                />
                <button className="button-blue" onClick={handleAddNewClick}>{translatedInfo["New Location"][props.language]}</button>
            </div>

            {showNoPermission && (
                <NoPermissionPopup
                    language={props.language}
                    onClose={() => setShowNoPermission(false)}
                />
            )}

            <div className="location-card-container">
                {filteredLocations.map((location) => (
                    <LocationCard key={location.id} location={location} language={props.language} />
                ))}
            </div>
        </>
    )
}
